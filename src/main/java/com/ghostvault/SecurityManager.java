package com.ghostvault.security;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.*;
import java.security.spec.KeySpec;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Security Manager for GhostVault
 * Handles encryption, decryption, secure deletion, and audit logging
 */
public class SecurityManager {
    
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final String KEY_DERIVATION = "PBKDF2WithHmacSHA256";
    private static final int KEY_SIZE = 256;
    private static final int ITERATION_COUNT = 100000;
    private static final int IV_SIZE = 16;
    private static final int SALT_SIZE = 32;
    
    // Secure deletion passes
    private static final int OVERWRITE_PASSES = 3;
    
    // File metadata storage
    private final Map<String, FileMetadata> fileRegistry = new ConcurrentHashMap<>();
    
    // Duress detection
    private final AtomicInteger failedAttempts = new AtomicInteger(0);
    private final List<Long> loginTimestamps = Collections.synchronizedList(new ArrayList<>());
    private volatile boolean duressMode = false;
    
    // Audit logger
    private final AuditLogger auditLogger;
    
    // Master encryption key
    private SecretKey masterKey;
    private byte[] masterSalt;
    
    public SecurityManager(String vaultPath) {
        this.auditLogger = new AuditLogger(vaultPath + "/audit.log.enc");
        initializeSalt(vaultPath);
    }
    
    /**
     * Initialize or load the master salt for key derivation
     */
    private void initializeSalt(String vaultPath) {
        Path saltFile = Paths.get(vaultPath, ".salt");
        try {
            if (Files.exists(saltFile)) {
                masterSalt = Files.readAllBytes(saltFile);
            } else {
                masterSalt = generateSalt();
                Files.write(saltFile, masterSalt, 
                    StandardOpenOption.CREATE_NEW, 
                    StandardOpenOption.WRITE);
                // Hide the salt file on Unix systems
                if (!System.getProperty("os.name").toLowerCase().contains("win")) {
                    Files.setAttribute(saltFile, "dos:hidden", true);
                }
            }
        } catch (IOException e) {
            // Fallback to hardcoded salt if file operations fail
            masterSalt = "GhostVaultDefaultSalt2024".getBytes();
        }
    }
    
    /**
     * Generate a cryptographically secure salt
     */
    private byte[] generateSalt() {
        byte[] salt = new byte[SALT_SIZE];
        new SecureRandom().nextBytes(salt);
        return salt;
    }
    
    /**
     * Derive encryption key from password using PBKDF2
     */
    public SecretKey deriveKey(String password) throws GeneralSecurityException {
        KeySpec spec = new PBEKeySpec(
            password.toCharArray(), 
            masterSalt, 
            ITERATION_COUNT, 
            KEY_SIZE
        );
        SecretKeyFactory factory = SecretKeyFactory.getInstance(KEY_DERIVATION);
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        
        masterKey = new SecretKeySpec(keyBytes, ALGORITHM);
        auditLogger.setKey(masterKey);
        
        return masterKey;
    }
    
    /**
     * Encrypt data with AES-256-CBC
     */
    public EncryptedData encrypt(byte[] plaintext) throws GeneralSecurityException {
        if (masterKey == null) {
            throw new IllegalStateException("Master key not initialized");
        }
        
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        
        // Generate random IV
        byte[] iv = new byte[IV_SIZE];
        new SecureRandom().nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        
        cipher.init(Cipher.ENCRYPT_MODE, masterKey, ivSpec);
        byte[] ciphertext = cipher.doFinal(plaintext);
        
        return new EncryptedData(ciphertext, iv);
    }
    
    /**
     * Decrypt data
     */
    public byte[] decrypt(EncryptedData encryptedData) throws GeneralSecurityException {
        if (masterKey == null) {
            throw new IllegalStateException("Master key not initialized");
        }
        
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        IvParameterSpec ivSpec = new IvParameterSpec(encryptedData.iv);
        cipher.init(Cipher.DECRYPT_MODE, masterKey, ivSpec);
        
        return cipher.doFinal(encryptedData.ciphertext);
    }
    
    /**
     * Encrypt and store a file
     */
    public String encryptFile(File inputFile, String vaultPath) throws Exception {
        // Read file
        byte[] fileData = Files.readAllBytes(inputFile.toPath());
        
        // Encrypt
        EncryptedData encrypted = encrypt(fileData);
        
        // Generate unique filename
        String fileId = UUID.randomUUID().toString();
        String encryptedFileName = fileId + ".enc";
        
        // Store encrypted file
        Path outputPath = Paths.get(vaultPath, "files", encryptedFileName);
        try (FileOutputStream fos = new FileOutputStream(outputPath.toFile())) {
            // Write IV first
            fos.write(encrypted.iv);
            // Write ciphertext
            fos.write(encrypted.ciphertext);
        }
        
        // Store metadata
        FileMetadata metadata = new FileMetadata(
            inputFile.getName(),
            fileId,
            inputFile.length(),
            LocalDateTime.now(),
            computeHash(fileData)
        );
        fileRegistry.put(fileId, metadata);
        saveMetadata(vaultPath);
        
        // Audit log
        auditLogger.log("File encrypted: " + inputFile.getName());
        
        return fileId;
    }
    
    /**
     * Decrypt a file from the vault
     */
    public byte[] decryptFile(String fileId, String vaultPath) throws Exception {
        Path encryptedPath = Paths.get(vaultPath, "files", fileId + ".enc");
        
        if (!Files.exists(encryptedPath)) {
            throw new FileNotFoundException("File not found in vault");
        }
        
        // Read encrypted file
        byte[] encryptedData = Files.readAllBytes(encryptedPath);
        
        // Split IV and ciphertext
        byte[] iv = Arrays.copyOfRange(encryptedData, 0, IV_SIZE);
        byte[] ciphertext = Arrays.copyOfRange(encryptedData, IV_SIZE, encryptedData.length);
        
        // Decrypt
        EncryptedData encrypted = new EncryptedData(ciphertext, iv);
        byte[] decrypted = decrypt(encrypted);
        
        // Verify integrity
        FileMetadata metadata = fileRegistry.get(fileId);
        if (metadata != null) {
            String currentHash = computeHash(decrypted);
            if (!currentHash.equals(metadata.hash)) {
                auditLogger.log("WARNING: File integrity check failed for " + fileId);
            }
        }
        
        auditLogger.log("File decrypted: " + fileId);
        return decrypted;
    }
    
    /**
     * Securely delete a file with multiple overwrite passes
     */
    public void secureDelete(File file) throws IOException {
        if (!file.exists()) return;
        
        long fileSize = file.length();
        
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw");
             FileChannel channel = raf.getChannel()) {
            
            SecureRandom random = new SecureRandom();
            ByteBuffer buffer = ByteBuffer.allocate(4096);
            
            for (int pass = 0; pass < OVERWRITE_PASSES; pass++) {
                channel.position(0);
                long written = 0;
                
                while (written < fileSize) {
                    buffer.clear();
                    
                    // Fill buffer with random data
                    byte[] randomBytes = new byte[buffer.capacity()];
                    random.nextBytes(randomBytes);
                    buffer.put(randomBytes);
                    buffer.flip();
                    
                    int bytesWritten = channel.write(buffer);
                    written += bytesWritten;
                }
                
                channel.force(true); // Force write to disk
            }
        }
        
        // Finally delete the file
        Files.delete(file.toPath());
        auditLogger.log("File securely deleted: " + file.getName());
    }
    
    /**
     * Emergency panic wipe - destroy all vault contents
     */
    public void panicWipe(String vaultPath) {
        auditLogger.log("PANIC WIPE INITIATED");
        
        try {
            Path filesDir = Paths.get(vaultPath, "files");
            if (Files.exists(filesDir)) {
                Files.walk(filesDir)
                    .filter(Files::isRegularFile)
                    .forEach(path -> {
                        try {
                            secureDelete(path.toFile());
                        } catch (IOException e) {
                            // Try regular delete as fallback
                            try {
                                Files.delete(path);
                            } catch (IOException ex) {
                                // Ignore
                            }
                        }
                    });
            }
            
            // Clear metadata
            fileRegistry.clear();
            Files.deleteIfExists(Paths.get(vaultPath, "metadata.enc"));
            
            // Clear config
            Files.deleteIfExists(Paths.get(vaultPath, "config.dat"));
            
            auditLogger.log("PANIC WIPE COMPLETED");
            
        } catch (IOException e) {
            auditLogger.log("PANIC WIPE ERROR: " + e.getMessage());
        }
    }
    
    /**
     * Detect potential duress situations
     */
    public boolean detectDuress(String event) {
        long now = System.currentTimeMillis();
        loginTimestamps.add(now);
        
        // Check for rapid login attempts (potential brute force)
        long recentAttempts = loginTimestamps.stream()
            .filter(t -> now - t < 60000) // Within last minute
            .count();
        
        if (recentAttempts > 5) {
            auditLogger.log("DURESS DETECTED: Rapid login attempts");
            duressMode = true;
            return true;
        }
        
        // Check for unusual typing patterns (placeholder for more sophisticated analysis)
        if (event.contains("unusual_pattern")) {
            auditLogger.log("DURESS DETECTED: Unusual input pattern");
            duressMode = true;
            return true;
        }
        
        return false;
    }
    
    /**
     * Generate realistic decoy files
     */
    public void generateDecoys(String vaultPath) {
        Path decoysDir = Paths.get(vaultPath, "decoys");
        try {
            Files.createDirectories(decoysDir);
            
            // Generate various decoy files
            generateTextDecoy(decoysDir, "Meeting_Notes.txt");
            generateSpreadsheetDecoy(decoysDir, "Financial_Report.csv");
            generateDocumentDecoy(decoysDir, "Project_Proposal.txt");
            
            auditLogger.log("Decoy files generated");
            
        } catch (IOException e) {
            auditLogger.log("Error generating decoys: " + e.getMessage());
        }
    }
    
    private void generateTextDecoy(Path dir, String filename) throws IOException {
        String content = String.format(
            "Meeting Notes\n" +
            "Date: %s\n\n" +
            "Attendees:\n- John Smith\n- Sarah Johnson\n- Mike Williams\n\n" +
            "Agenda:\n" +
            "1. Q3 Performance Review\n" +
            "2. Budget Allocation\n" +
            "3. Project Timeline Updates\n\n" +
            "Action Items:\n" +
            "- Review budget proposal by EOW\n" +
            "- Schedule follow-up meeting\n" +
            "- Update project documentation\n",
            LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        );
        
        Files.write(dir.resolve(filename), content.getBytes(StandardCharsets.UTF_8));
    }
    
    private void generateSpreadsheetDecoy(Path dir, String filename) throws IOException {
        String content = "Department,Q1,Q2,Q3,Q4,Total\n" +
                        "Sales,125000,135000,142000,155000,557000\n" +
                        "Marketing,45000,48000,52000,55000,200000\n" +
                        "Engineering,280000,285000,290000,295000,1150000\n" +
                        "Operations,65000,67000,70000,72000,274000\n";
        
        Files.write(dir.resolve(filename), content.getBytes(StandardCharsets.UTF_8));
    }
    
    private void generateDocumentDecoy(Path dir, String filename) throws IOException {
        String content = "PROJECT PROPOSAL\n\n" +
                        "Executive Summary:\n" +
                        "This proposal outlines the implementation strategy for the new customer management system.\n\n" +
                        "Objectives:\n" +
                        "- Improve customer response time by 40%\n" +
                        "- Reduce operational costs by 25%\n" +
                        "- Enhance data security and compliance\n\n" +
                        "Timeline: 6 months\n" +
                        "Budget: $450,000\n\n" +
                        "Submitted by: Project Management Office\n" +
                        "Date: " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        
        Files.write(dir.resolve(filename), content.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * Compute SHA-256 hash of data for integrity verification
     */
    private String computeHash(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(data);
        return Base64.getEncoder().encodeToString(hash);
    }
    
    /**
     * Save encrypted metadata
     */
    private void saveMetadata(String vaultPath) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(fileRegistry);
        oos.close();
        
        EncryptedData encrypted = encrypt(baos.toByteArray());
        Path metadataPath = Paths.get(vaultPath, "metadata.enc");
        
        try (FileOutputStream fos = new FileOutputStream(metadataPath.toFile())) {
            fos.write(encrypted.iv);
            fos.write(encrypted.ciphertext);
        }
    }
    
    /**
     * Load encrypted metadata
     */
    @SuppressWarnings("unchecked")
    public void loadMetadata(String vaultPath) throws Exception {
        Path metadataPath = Paths.get(vaultPath, "metadata.enc");
        if (!Files.exists(metadataPath)) {
            return;
        }
        
        byte[] encryptedData = Files.readAllBytes(metadataPath);
        byte[] iv = Arrays.copyOfRange(encryptedData, 0, IV_SIZE);
        byte[] ciphertext = Arrays.copyOfRange(encryptedData, IV_SIZE, encryptedData.length);
        
        EncryptedData encrypted = new EncryptedData(ciphertext, iv);
        byte[] decrypted = decrypt(encrypted);
        
        ByteArrayInputStream bais = new ByteArrayInputStream(decrypted);
        ObjectInputStream ois = new ObjectInputStream(bais);
        fileRegistry.putAll((Map<String, FileMetadata>) ois.readObject());
        ois.close();
    }
    
    // Helper classes
    
    public static class EncryptedData {
        public final byte[] ciphertext;
        public final byte[] iv;
        
        public EncryptedData(byte[] ciphertext, byte[] iv) {
            this.ciphertext = ciphertext;
            this.iv = iv;
        }
    }
    
    public static class FileMetadata implements Serializable {
        private static final long serialVersionUID = 1L;
        
        public final String originalName;
        public final String fileId;
        public final long size;
        public final LocalDateTime uploadTime;
        public final String hash;
        
        public FileMetadata(String originalName, String fileId, long size, 
                          LocalDateTime uploadTime, String hash) {
            this.originalName = originalName;
            this.fileId = fileId;
            this.size = size;
            this.uploadTime = uploadTime;
            this.hash = hash;
        }
    }
    
    /**
     * Encrypted audit logger
     */
    private class AuditLogger {
        private final String logPath;
        private SecretKey logKey;
        
        public AuditLogger(String logPath) {
            this.logPath = logPath;
        }
        
        public void setKey(SecretKey key) {
            this.logKey = key;
        }
        
        public void log(String message) {
            if (logKey == null) return;
            
            try {
                String logEntry = String.format("[%s] %s\n", 
                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    message);
                
                EncryptedData encrypted = encrypt(logEntry.getBytes(StandardCharsets.UTF_8));
                
                // Append to log file
                try (FileOutputStream fos = new FileOutputStream(logPath, true)) {
                    // Write entry size (4 bytes)
                    ByteBuffer sizeBuf = ByteBuffer.allocate(4);
                    sizeBuf.putInt(encrypted.iv.length + encrypted.ciphertext.length);
                    fos.write(sizeBuf.array());
                    
                    // Write IV and ciphertext
                    fos.write(encrypted.iv);
                    fos.write(encrypted.ciphertext);
                }
                
            } catch (Exception e) {
                // Silent fail for logging errors
            }
        }
    }
    
    // Getters for UI access
    
    public Map<String, FileMetadata> getFileRegistry() {
        return new HashMap<>(fileRegistry);
    }
    
    public boolean isDuressMode() {
        return duressMode;
    }
    
    public void resetDuressMode() {
        duressMode = false;
        failedAttempts.set(0);
        loginTimestamps.clear();
    }
}
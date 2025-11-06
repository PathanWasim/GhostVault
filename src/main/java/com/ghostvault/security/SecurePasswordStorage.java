package com.ghostvault.security;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Secure password storage using PBKDF2 hashing
 * Replaces plain text password storage with cryptographically secure hashes
 */
public class SecurePasswordStorage {
    
    public enum PasswordType {
        MASTER, DECOY, PANIC
    }
    
    /**
     * Container for password data with salts and hashes
     */
    public static class PasswordData {
        private final byte[] masterSalt;
        private final byte[] masterHash;
        private final byte[] decoySalt;
        private final byte[] decoyHash;
        private final byte[] panicSalt;
        private final byte[] panicHash;
        private final int iterations;
        
        public PasswordData(byte[] masterSalt, byte[] masterHash,
                           byte[] decoySalt, byte[] decoyHash,
                           byte[] panicSalt, byte[] panicHash,
                           int iterations) {
            this.masterSalt = masterSalt != null ? masterSalt.clone() : null;
            this.masterHash = masterHash != null ? masterHash.clone() : null;
            this.decoySalt = decoySalt != null ? decoySalt.clone() : null;
            this.decoyHash = decoyHash != null ? decoyHash.clone() : null;
            this.panicSalt = panicSalt != null ? panicSalt.clone() : null;
            this.panicHash = panicHash != null ? panicHash.clone() : null;
            this.iterations = iterations;
        }
        
        public byte[] getMasterSalt() { return masterSalt != null ? masterSalt.clone() : null; }
        public byte[] getMasterHash() { return masterHash != null ? masterHash.clone() : null; }
        public byte[] getDecoySalt() { return decoySalt != null ? decoySalt.clone() : null; }
        public byte[] getDecoyHash() { return decoyHash != null ? decoyHash.clone() : null; }
        public byte[] getPanicSalt() { return panicSalt != null ? panicSalt.clone() : null; }
        public byte[] getPanicHash() { return panicHash != null ? panicHash.clone() : null; }
        public int getIterations() { return iterations; }
        
        public byte[] getSalt(PasswordType type) {
            switch (type) {
                case MASTER: return getMasterSalt();
                case DECOY: return getDecoySalt();
                case PANIC: return getPanicSalt();
                default: throw new IllegalArgumentException("Unknown password type: " + type);
            }
        }
        
        public byte[] getHash(PasswordType type) {
            switch (type) {
                case MASTER: return getMasterHash();
                case DECOY: return getDecoyHash();
                case PANIC: return getPanicHash();
                default: throw new IllegalArgumentException("Unknown password type: " + type);
            }
        }
    }
    
    private final CryptoManager cryptoManager;
    private final Path vaultDirectory;
    private final Path encryptedPasswordFile;
    private final Path plainPasswordFile;
    
    public SecurePasswordStorage() {
        this(System.getProperty("user.home") + "/.ghostvault");
    }
    
    public SecurePasswordStorage(String vaultPath) {
        this.cryptoManager = new CryptoManager();
        this.vaultDirectory = Paths.get(vaultPath);
        this.encryptedPasswordFile = vaultDirectory.resolve(SecurityConfiguration.ENCRYPTED_PASSWORD_FILE);
        this.plainPasswordFile = vaultDirectory.resolve(SecurityConfiguration.PLAIN_PASSWORD_FILE);
    }
    
    /**
     * Store password hashes securely
     * @param masterPassword The master password
     * @param decoyPassword The decoy password
     * @param panicPassword The panic password
     * @throws Exception if storage fails
     */
    public void storePasswordHashes(String masterPassword, String decoyPassword, String panicPassword) throws Exception {
        if (masterPassword == null || decoyPassword == null || panicPassword == null) {
            throw new IllegalArgumentException("All passwords must be provided");
        }
        
        // Ensure vault directory exists
        Files.createDirectories(vaultDirectory);
        
        // Generate unique salts for each password
        byte[] masterSalt = cryptoManager.generateSalt();
        byte[] decoySalt = cryptoManager.generateSalt();
        byte[] panicSalt = cryptoManager.generateSalt();
        
        // Hash each password with its salt
        byte[] masterHash = cryptoManager.hashPassword(masterPassword, masterSalt);
        byte[] decoyHash = cryptoManager.hashPassword(decoyPassword, decoySalt);
        byte[] panicHash = cryptoManager.hashPassword(panicPassword, panicSalt);
        
        try {
            // Create password data structure
            PasswordData passwordData = new PasswordData(
                masterSalt, masterHash,
                decoySalt, decoyHash,
                panicSalt, panicHash,
                SecurityConfiguration.PBKDF2_ITERATIONS
            );
            
            // Serialize and save to encrypted file
            byte[] serializedData = serializePasswordData(passwordData);
            Files.write(encryptedPasswordFile, serializedData);
            
            System.out.println("🔐 Password hashes stored securely in: " + encryptedPasswordFile);
            
            // Remove plain text password file if it exists
            if (Files.exists(plainPasswordFile)) {
                Files.delete(plainPasswordFile);
                System.out.println("🗑️ Removed plain text password file: " + plainPasswordFile);
            }
            
        } finally {
            // Clear sensitive data from memory
            cryptoManager.secureWipe(masterHash);
            cryptoManager.secureWipe(decoyHash);
            cryptoManager.secureWipe(panicHash);
        }
    }
    
    /**
     * Verify a password against stored hash
     * @param password The password to verify
     * @param type The type of password (MASTER, DECOY, PANIC)
     * @return true if password matches
     * @throws Exception if verification fails
     */
    public boolean verifyPassword(String password, PasswordType type) throws Exception {
        PasswordData passwordData = loadPasswordData();
        if (passwordData == null) {
            return false;
        }
        
        byte[] salt = passwordData.getSalt(type);
        byte[] storedHash = passwordData.getHash(type);
        
        if (salt == null || storedHash == null) {
            return false;
        }
        
        return cryptoManager.verifyPassword(password, salt, storedHash);
    }
    
    /**
     * Load password data from encrypted storage
     * @return PasswordData or null if not found
     * @throws Exception if loading fails
     */
    public PasswordData loadPasswordData() throws Exception {
        if (!Files.exists(encryptedPasswordFile)) {
            return null;
        }
        
        byte[] serializedData = Files.readAllBytes(encryptedPasswordFile);
        return deserializePasswordData(serializedData);
    }
    
    /**
     * Check if passwords are stored securely (encrypted format)
     * @return true if using encrypted storage
     */
    public boolean isUsingSecureStorage() {
        return Files.exists(encryptedPasswordFile) && !Files.exists(plainPasswordFile);
    }
    
    /**
     * Check if plain text passwords exist and need migration
     * @return true if migration is needed
     */
    public boolean needsMigration() {
        return Files.exists(plainPasswordFile) && !Files.exists(encryptedPasswordFile);
    }
    
    /**
     * Get the salt for a specific password type (for key derivation)
     * @param type The password type
     * @return The salt bytes
     * @throws Exception if loading fails
     */
    public byte[] getSaltForKeyDerivation(PasswordType type) throws Exception {
        PasswordData passwordData = loadPasswordData();
        if (passwordData == null) {
            throw new IllegalStateException("No password data found");
        }
        return passwordData.getSalt(type);
    }
    
    /**
     * Serialize password data to byte array
     */
    private byte[] serializePasswordData(PasswordData data) {
        // Format: [MAGIC_BYTES][ITERATIONS][MASTER_SALT][MASTER_HASH][DECOY_SALT][DECOY_HASH][PANIC_SALT][PANIC_HASH]
        int totalSize = SecurityConfiguration.MAGIC_BYTES.length + 4 + // magic + iterations
                       (SecurityConfiguration.SALT_LENGTH + SecurityConfiguration.KEY_LENGTH / 8) * 3; // 3 salt+hash pairs
        
        byte[] result = new byte[totalSize];
        int offset = 0;
        
        // Magic bytes for format identification
        System.arraycopy(SecurityConfiguration.MAGIC_BYTES, 0, result, offset, SecurityConfiguration.MAGIC_BYTES.length);
        offset += SecurityConfiguration.MAGIC_BYTES.length;
        
        // Iterations (4 bytes, big-endian)
        result[offset++] = (byte) (data.getIterations() >>> 24);
        result[offset++] = (byte) (data.getIterations() >>> 16);
        result[offset++] = (byte) (data.getIterations() >>> 8);
        result[offset++] = (byte) data.getIterations();
        
        // Master salt and hash
        System.arraycopy(data.getMasterSalt(), 0, result, offset, SecurityConfiguration.SALT_LENGTH);
        offset += SecurityConfiguration.SALT_LENGTH;
        System.arraycopy(data.getMasterHash(), 0, result, offset, SecurityConfiguration.KEY_LENGTH / 8);
        offset += SecurityConfiguration.KEY_LENGTH / 8;
        
        // Decoy salt and hash
        System.arraycopy(data.getDecoySalt(), 0, result, offset, SecurityConfiguration.SALT_LENGTH);
        offset += SecurityConfiguration.SALT_LENGTH;
        System.arraycopy(data.getDecoyHash(), 0, result, offset, SecurityConfiguration.KEY_LENGTH / 8);
        offset += SecurityConfiguration.KEY_LENGTH / 8;
        
        // Panic salt and hash
        System.arraycopy(data.getPanicSalt(), 0, result, offset, SecurityConfiguration.SALT_LENGTH);
        offset += SecurityConfiguration.SALT_LENGTH;
        System.arraycopy(data.getPanicHash(), 0, result, offset, SecurityConfiguration.KEY_LENGTH / 8);
        
        return result;
    }
    
    /**
     * Deserialize password data from byte array
     */
    private PasswordData deserializePasswordData(byte[] data) throws Exception {
        if (data == null || data.length < SecurityConfiguration.MAGIC_BYTES.length + 4) {
            throw new IllegalArgumentException("Invalid password data format");
        }
        
        int offset = 0;
        
        // Verify magic bytes
        byte[] magic = new byte[SecurityConfiguration.MAGIC_BYTES.length];
        System.arraycopy(data, offset, magic, 0, SecurityConfiguration.MAGIC_BYTES.length);
        if (!Arrays.equals(magic, SecurityConfiguration.MAGIC_BYTES)) {
            throw new IllegalArgumentException("Invalid password file format");
        }
        offset += SecurityConfiguration.MAGIC_BYTES.length;
        
        // Read iterations
        int iterations = ((data[offset] & 0xFF) << 24) |
                        ((data[offset + 1] & 0xFF) << 16) |
                        ((data[offset + 2] & 0xFF) << 8) |
                        (data[offset + 3] & 0xFF);
        offset += 4;
        
        int hashLength = SecurityConfiguration.KEY_LENGTH / 8;
        
        // Read master salt and hash
        byte[] masterSalt = new byte[SecurityConfiguration.SALT_LENGTH];
        byte[] masterHash = new byte[hashLength];
        System.arraycopy(data, offset, masterSalt, 0, SecurityConfiguration.SALT_LENGTH);
        offset += SecurityConfiguration.SALT_LENGTH;
        System.arraycopy(data, offset, masterHash, 0, hashLength);
        offset += hashLength;
        
        // Read decoy salt and hash
        byte[] decoySalt = new byte[SecurityConfiguration.SALT_LENGTH];
        byte[] decoyHash = new byte[hashLength];
        System.arraycopy(data, offset, decoySalt, 0, SecurityConfiguration.SALT_LENGTH);
        offset += SecurityConfiguration.SALT_LENGTH;
        System.arraycopy(data, offset, decoyHash, 0, hashLength);
        offset += hashLength;
        
        // Read panic salt and hash
        byte[] panicSalt = new byte[SecurityConfiguration.SALT_LENGTH];
        byte[] panicHash = new byte[hashLength];
        System.arraycopy(data, offset, panicSalt, 0, SecurityConfiguration.SALT_LENGTH);
        offset += SecurityConfiguration.SALT_LENGTH;
        System.arraycopy(data, offset, panicHash, 0, hashLength);
        
        return new PasswordData(masterSalt, masterHash, decoySalt, decoyHash, panicSalt, panicHash, iterations);
    }
}
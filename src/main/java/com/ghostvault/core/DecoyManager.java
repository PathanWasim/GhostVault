package com.ghostvault.core;

import com.ghostvault.config.AppConfig;
import com.ghostvault.model.VaultFile;
import com.ghostvault.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * CRITICAL SECURITY COMPONENT: Manages decoy vault to protect real data
 * 
 * This class creates a completely separate decoy vault that appears real
 * while keeping the actual vault hidden and protected from discovery.
 * 
 * NEVER modifies or deletes real vault data!
 */
public class DecoyManager {
    
    private static final String[] BUSINESS_NAMES = {
        "Acme Corporation", "Global Industries", "Tech Solutions Inc", "Metro Consulting",
        "Prime Enterprises", "Alpha Systems", "Beta Technologies", "Gamma Corp"
    };
    
    private static final String[] PERSON_NAMES = {
        "John Smith", "Sarah Johnson", "Michael Brown", "Emily Davis",
        "David Wilson", "Lisa Anderson", "Robert Taylor", "Jennifer Martinez"
    };
    
    private static final String[] PROJECT_NAMES = {
        "Project Alpha", "Operation Beta", "Initiative Gamma", "Program Delta",
        "Campaign Epsilon", "Strategy Zeta", "Mission Eta", "Plan Theta"
    };
    
    private static final String[] DOCUMENT_TYPES = {
        "Meeting Notes", "Project Report", "Budget Analysis", "Status Update",
        "Planning Document", "Research Notes", "Training Materials", "Policy Document"
    };
    
    // CRITICAL: Separate paths for real and decoy vaults
    private final Path realVaultPath;
    private final Path decoyVaultPath;
    private final Random random;
    private final List<VaultFile> decoyFiles;
    private boolean isDecoyMode;
    private boolean decoyVaultInitialized;
    
    // Device-specific information for realistic decoys
    private final String deviceId;
    private final String userName;
    private final String osName;
    
    /**
     * CRITICAL CONSTRUCTOR: Must have separate paths to prevent data loss
     */
    public DecoyManager(Path realVaultPath, Path decoyVaultPath) {
        // SAFETY CHECK: Ensure paths are different
        if (realVaultPath.equals(decoyVaultPath)) {
            throw new SecurityException("CRITICAL: Real and decoy vault paths must be different!");
        }
        
        this.realVaultPath = realVaultPath.toAbsolutePath();
        this.decoyVaultPath = decoyVaultPath.toAbsolutePath();
        this.random = new Random();
        this.decoyFiles = new ArrayList<>();
        this.isDecoyMode = false;
        this.decoyVaultInitialized = false;
        
        // Get device information for realistic decoys
        this.deviceId = generateDeviceId();
        this.userName = System.getProperty("user.name", "user");
        this.osName = System.getProperty("os.name", "Unknown");
        
        System.out.println("✓ DecoyManager initialized");
        System.out.println("  Real vault: " + this.realVaultPath);
        System.out.println("  Decoy vault: " + this.decoyVaultPath);
        System.out.println("  Device: " + this.userName + "@" + this.osName);
    }
    
    /**
     * Generate unique device ID for consistent decoy generation
     */
    private String generateDeviceId() {
        try {
            String hostName = java.net.InetAddress.getLocalHost().getHostName();
            String userHome = System.getProperty("user.home", "");
            return Integer.toHexString((hostName + userHome).hashCode());
        } catch (Exception e) {
            return Integer.toHexString((int)(System.currentTimeMillis() % 100000));
        }
    }
    
    /**
     * CRITICAL: Switch to decoy mode safely
     */
    public void switchToDecoyMode() throws Exception {
        if (isDecoyMode) {
            System.out.println("Already in decoy mode");
            return;
        }
        
        // CRITICAL SAFETY CHECK: Verify real vault exists and is protected
        if (!Files.exists(realVaultPath)) {
            throw new SecurityException("CRITICAL: Real vault not found! Cannot switch to decoy mode safely.");
        }
        
        // Initialize decoy vault if needed
        if (!decoyVaultInitialized) {
            initializeDecoyVault();
        }
        
        // Switch to decoy mode
        isDecoyMode = true;
        
        System.out.println("🎭 SWITCHED TO DECOY MODE");
        System.out.println("  Real vault PROTECTED at: " + realVaultPath);
        System.out.println("  Decoy vault ACTIVE at: " + decoyVaultPath);
        System.out.println("  ⚠️ All operations now work on FAKE data!");
    }
    
    /**
     * CRITICAL: Switch back to real vault safely
     */
    public void switchToRealMode() throws Exception {
        if (!isDecoyMode) {
            System.out.println("Already in real vault mode");
            return;
        }
        
        // CRITICAL SAFETY CHECK: Verify real vault integrity
        if (!verifyRealVaultIntegrity()) {
            throw new SecurityException("CRITICAL: Real vault integrity check failed!");
        }
        
        isDecoyMode = false;
        
        System.out.println("✓ SWITCHED TO REAL VAULT MODE");
        System.out.println("  Real vault ACTIVE at: " + realVaultPath);
        System.out.println("  Decoy vault hidden at: " + decoyVaultPath);
    }
    
    /**
     * Get current active vault path (CRITICAL for preventing data loss)
     */
    public Path getCurrentVaultPath() {
        return isDecoyMode ? decoyVaultPath : realVaultPath;
    }
    
    /**
     * Check if currently in decoy mode
     */
    public boolean isDecoyMode() {
        return isDecoyMode;
    }
    
    /**
     * CRITICAL: Verify real vault integrity before operations
     */
    public boolean verifyRealVaultIntegrity() {
        try {
            if (!Files.exists(realVaultPath)) {
                System.err.println("⚠️ CRITICAL: Real vault not found!");
                return false;
            }
            
            if (!Files.isDirectory(realVaultPath)) {
                System.err.println("⚠️ CRITICAL: Real vault path is not a directory!");
                return false;
            }
            
            if (!Files.isReadable(realVaultPath) || !Files.isWritable(realVaultPath)) {
                System.err.println("⚠️ CRITICAL: Real vault permissions issue!");
                return false;
            }
            
            // Check if decoy path is accidentally same as real path
            if (decoyVaultPath.equals(realVaultPath)) {
                System.err.println("⚠️ CRITICAL: Decoy and real paths are identical!");
                return false;
            }
            
            return true;
        } catch (Exception e) {
            System.err.println("⚠️ CRITICAL: Vault integrity check failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Initialize decoy vault with realistic fake data
     */
    private void initializeDecoyVault() throws Exception {
        System.out.println("🎭 Initializing decoy vault...");
        
        // Create decoy vault directory structure
        Files.createDirectories(decoyVaultPath);
        Files.createDirectories(decoyVaultPath.resolve("files"));
        Files.createDirectories(decoyVaultPath.resolve("metadata"));
        Files.createDirectories(decoyVaultPath.resolve("temp"));
        
        // Generate device-specific realistic decoy files
        generateDeviceSpecificDecoys();
        
        // Generate common business/personal files
        generateRealisticDecoyFiles(8 + random.nextInt(12)); // 8-20 files
        
        // Create decoy configuration files
        createDecoyConfigFiles();
        
        decoyVaultInitialized = true;
        System.out.println("✓ Decoy vault initialized with realistic fake data");
    }
    
    /**
     * Generate device and user-specific decoy files
     */
    private void generateDeviceSpecificDecoys() throws IOException {
        System.out.println("🎭 Generating device-specific decoys for: " + userName + "@" + osName);
        
        // Create user-specific files
        String[] userSpecificFiles = {
            userName + "_documents_backup.zip",
            userName + "_desktop_files.zip", 
            userName + "_browser_bookmarks.html",
            userName + "_email_backup.pst",
            userName + "_photos_2023.zip",
            userName + "_work_files.zip",
            "system_backup_" + osName.toLowerCase().replace(" ", "_") + ".zip",
            "settings_" + deviceId + ".json"
        };
        
        Path filesDir = decoyVaultPath.resolve("files");
        
        for (String fileName : userSpecificFiles) {
            if (random.nextDouble() < 0.7) { // 70% chance to include each file
                createDecoyFile(filesDir, fileName, generateDeviceSpecificContent(fileName));
            }
        }
    }
    
    /**
     * Generate realistic decoy files
     */
    public void generateRealisticDecoyFiles(int count) throws IOException {
        Path filesDir = decoyVaultPath.resolve("files");
        
        for (int i = 0; i < count; i++) {
            String fileName = generateRealisticFileName();
            String content = generateRealisticContent(fileName);
            createDecoyFile(filesDir, fileName, content);
        }
        
        System.out.println("✓ Generated " + count + " realistic decoy files");
    }
    
    /**
     * Create a single decoy file
     */
    private void createDecoyFile(Path directory, String fileName, String content) throws IOException {
        Path filePath = directory.resolve(fileName + ".enc"); // All files are "encrypted"
        
        // Create fake encrypted content (random bytes with structure)
        byte[] fakeEncrypted = createFakeEncryptedContent(content.getBytes());
        Files.write(filePath, fakeEncrypted);
        
        // Create metadata for the decoy file
        VaultFile decoyFile = new VaultFile(
            fileName,
            UUID.randomUUID().toString(),
            fileName,
            fakeEncrypted.length,
            FileUtils.calculateSHA256(fakeEncrypted),
            System.currentTimeMillis() - random.nextInt(30 * 24 * 60 * 60) * 1000L // Random time in last 30 days
        );
        
        decoyFiles.add(decoyFile);
    }
    
    /**
     * Create fake encrypted content that looks realistic
     */
    private byte[] createFakeEncryptedContent(byte[] originalContent) {
        // Create realistic encrypted-looking data
        int totalSize = 48 + originalContent.length + random.nextInt(64); // IV + Salt + Content + Padding
        byte[] fakeEncrypted = new byte[totalSize];
        
        // Fake IV (16 bytes)
        random.nextBytes(fakeEncrypted);
        System.arraycopy(generateFakeIV(), 0, fakeEncrypted, 0, 16);
        
        // Fake Salt (32 bytes) 
        System.arraycopy(generateFakeSalt(), 0, fakeEncrypted, 16, 32);
        
        // Fake encrypted content (rest of the bytes)
        random.nextBytes(fakeEncrypted);
        
        return fakeEncrypted;
    }
    
    /**
     * Generate fake IV
     */
    private byte[] generateFakeIV() {
        byte[] iv = new byte[16];
        random.nextBytes(iv);
        return iv;
    }
    
    /**
     * Generate fake salt
     */
    private byte[] generateFakeSalt() {
        byte[] salt = new byte[32];
        random.nextBytes(salt);
        return salt;
    }
    
    /**
     * Generate device-specific content
     */
    private String generateDeviceSpecificContent(String fileName) {
        if (fileName.contains("backup")) {
            return generateBackupContent();
        } else if (fileName.contains("bookmarks")) {
            return generateBookmarksContent();
        } else if (fileName.contains("email")) {
            return generateEmailContent();
        } else if (fileName.contains("settings")) {
            return generateSettingsContent();
        } else {
            return generateGenericContent(fileName);
        }
    }
    
    /**
     * Generate backup file content
     */
    private String generateBackupContent() {
        StringBuilder backup = new StringBuilder();
        backup.append("BACKUP MANIFEST\n");
        backup.append("===============\n\n");
        backup.append("Created: ").append(LocalDateTime.now()).append("\n");
        backup.append("Device: ").append(userName).append("@").append(osName).append("\n");
        backup.append("Device ID: ").append(deviceId).append("\n\n");
        
        backup.append("Backed up files:\n");
        String[] backupItems = {
            "Documents folder (1,247 files, 2.3 GB)",
            "Desktop files (89 files, 456 MB)", 
            "Downloads folder (234 files, 1.1 GB)",
            "Pictures folder (2,891 files, 8.7 GB)",
            "Music folder (567 files, 3.2 GB)",
            "Videos folder (45 files, 12.4 GB)",
            "Browser data (bookmarks, history, passwords)",
            "Email data (inbox, sent, drafts)",
            "Application settings and preferences"
        };
        
        for (String item : backupItems) {
            backup.append("- ").append(item).append("\n");
        }
        
        backup.append("\nTotal backup size: 28.2 GB\n");
        backup.append("Compression ratio: 67%\n");
        backup.append("Encryption: AES-256-GCM\n");
        
        return backup.toString();
    }
    
    /**
     * Generate bookmarks content
     */
    private String generateBookmarksContent() {
        StringBuilder bookmarks = new StringBuilder();
        bookmarks.append("<!DOCTYPE NETSCAPE-Bookmark-file-1>\n");
        bookmarks.append("<META HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=UTF-8\">\n");
        bookmarks.append("<TITLE>Bookmarks</TITLE>\n");
        bookmarks.append("<H1>Bookmarks Menu</H1>\n\n");
        bookmarks.append("<DL><p>\n");
        
        String[] bookmarkCategories = {"Work", "News", "Shopping", "Entertainment", "Social"};
        String[][] bookmarkSites = {
            {"Gmail", "Google Drive", "Slack", "Zoom", "LinkedIn"},
            {"BBC News", "CNN", "Reuters", "TechCrunch", "Hacker News"},
            {"Amazon", "eBay", "Target", "Best Buy", "Walmart"},
            {"Netflix", "YouTube", "Spotify", "Twitch", "Reddit"},
            {"Facebook", "Twitter", "Instagram", "WhatsApp", "Discord"}
        };
        
        for (int i = 0; i < bookmarkCategories.length; i++) {
            bookmarks.append("    <DT><H3>").append(bookmarkCategories[i]).append("</H3>\n");
            bookmarks.append("    <DL><p>\n");
            
            for (String site : bookmarkSites[i]) {
                bookmarks.append("        <DT><A HREF=\"https://").append(site.toLowerCase().replace(" ", "")).append(".com\">").append(site).append("</A>\n");
            }
            
            bookmarks.append("    </DL><p>\n");
        }
        
        bookmarks.append("</DL><p>\n");
        return bookmarks.toString();
    }
    
    /**
     * Generate email content
     */
    private String generateEmailContent() {
        StringBuilder email = new StringBuilder();
        email.append("EMAIL BACKUP SUMMARY\n");
        email.append("===================\n\n");
        email.append("Account: ").append(userName).append("@example.com\n");
        email.append("Backup Date: ").append(LocalDateTime.now()).append("\n\n");
        
        email.append("Folders backed up:\n");
        email.append("- Inbox (1,234 messages, 567 MB)\n");
        email.append("- Sent Items (892 messages, 234 MB)\n");
        email.append("- Drafts (23 messages, 12 MB)\n");
        email.append("- Archive (5,678 messages, 2.1 GB)\n");
        email.append("- Spam (45 messages, 8 MB)\n");
        email.append("- Trash (156 messages, 89 MB)\n\n");
        
        email.append("Contacts: 456 entries\n");
        email.append("Calendar events: 234 entries\n");
        email.append("Rules and filters: 12 entries\n\n");
        
        email.append("Total backup size: 3.0 GB\n");
        email.append("Format: PST (Outlook)\n");
        
        return email.toString();
    }
    
    /**
     * Generate settings content
     */
    private String generateSettingsContent() {
        StringBuilder settings = new StringBuilder();
        settings.append("{\n");
        settings.append("  \"device_id\": \"").append(deviceId).append("\",\n");
        settings.append("  \"user_name\": \"").append(userName).append("\",\n");
        settings.append("  \"os_name\": \"").append(osName).append("\",\n");
        settings.append("  \"created\": \"").append(LocalDateTime.now()).append("\",\n");
        settings.append("  \"vault_settings\": {\n");
        settings.append("    \"auto_backup\": true,\n");
        settings.append("    \"encryption_level\": \"AES-256\",\n");
        settings.append("    \"session_timeout\": 1800,\n");
        settings.append("    \"auto_lock\": true\n");
        settings.append("  },\n");
        settings.append("  \"ui_preferences\": {\n");
        settings.append("    \"theme\": \"dark\",\n");
        settings.append("    \"language\": \"en\",\n");
        settings.append("    \"show_thumbnails\": true\n");
        settings.append("  }\n");
        settings.append("}\n");
        
        return settings.toString();
    }
    
    /**
     * Create decoy configuration files
     */
    private void createDecoyConfigFiles() throws IOException {
        // Create fake vault config
        Path configPath = decoyVaultPath.resolve("vault.config");
        String configContent = generateVaultConfigContent();
        Files.write(configPath, configContent.getBytes());
        
        // Create fake metadata index
        Path metadataPath = decoyVaultPath.resolve("metadata").resolve("index.json");
        String metadataContent = generateMetadataIndexContent();
        Files.write(metadataPath, metadataContent.getBytes());
        
        System.out.println("✓ Created decoy configuration files");
    }
    
    /**
     * Generate fake vault config content
     */
    private String generateVaultConfigContent() {
        StringBuilder config = new StringBuilder();
        config.append("# GhostVault Configuration (DECOY)\n");
        config.append("# Generated: ").append(LocalDateTime.now()).append("\n\n");
        config.append("vault.version=1.0.0\n");
        
        // Safe random time generation
        long randomDays = 1 + random.nextInt(89); // 1-90 days ago
        config.append("vault.created=").append(System.currentTimeMillis() - (randomDays * 24 * 60 * 60 * 1000L)).append("\n");
        
        config.append("vault.encryption=AES-256-GCM\n");
        config.append("vault.key_derivation=Argon2id\n");
        config.append("vault.file_count=").append(Math.max(1, decoyFiles.size())).append("\n");
        config.append("vault.device_id=").append(deviceId).append("\n");
        config.append("vault.user=").append(userName).append("\n");
        
        return config.toString();
    }
    
    /**
     * Generate fake metadata index content
     */
    private String generateMetadataIndexContent() {
        StringBuilder metadata = new StringBuilder();
        metadata.append("{\n");
        metadata.append("  \"version\": \"1.0.0\",\n");
        metadata.append("  \"created\": \"").append(LocalDateTime.now()).append("\",\n");
        metadata.append("  \"file_count\": ").append(decoyFiles.size()).append(",\n");
        metadata.append("  \"total_size\": ").append(random.nextInt(100000000) + 50000000).append(",\n"); // 50-150 MB
        metadata.append("  \"encryption\": \"AES-256-GCM\",\n");
        metadata.append("  \"device_id\": \"").append(deviceId).append("\",\n");
        metadata.append("  \"files\": [\n");
        
        for (int i = 0; i < decoyFiles.size(); i++) {
            VaultFile file = decoyFiles.get(i);
            metadata.append("    {\n");
            metadata.append("      \"id\": \"").append(file.getFileId()).append("\",\n");
            metadata.append("      \"name\": \"").append(file.getOriginalName()).append("\",\n");
            metadata.append("      \"size\": ").append(file.getSize()).append(",\n");
            metadata.append("      \"hash\": \"").append(file.getHash()).append("\",\n");
            metadata.append("      \"created\": ").append(file.getUploadTime()).append("\n");
            metadata.append("    }");
            if (i < decoyFiles.size() - 1) {
                metadata.append(",");
            }
            metadata.append("\n");
        }
        
        metadata.append("  ]\n");
        metadata.append("}\n");
        
        return metadata.toString();
    }
    
    /**
     * Refresh decoy vault with new fake data (prevents detection)
     */
    public void refreshDecoyVault() throws Exception {
        if (!isDecoyMode) {
            System.out.println("Not in decoy mode - no refresh needed");
            return;
        }
        
        System.out.println("🎭 Refreshing decoy vault with new fake data...");
        
        // Clear old decoy files (but keep directory structure)
        Path filesDir = decoyVaultPath.resolve("files");
        if (Files.exists(filesDir)) {
            Files.walk(filesDir)
                .filter(Files::isRegularFile)
                .forEach(file -> {
                    try {
                        Files.delete(file);
                    } catch (Exception e) {
                        // Ignore deletion errors
                    }
                });
        }
        
        // Clear old decoy file list
        decoyFiles.clear();
        
        // Generate fresh fake data
        generateDeviceSpecificDecoys();
        generateRealisticDecoyFiles(6 + random.nextInt(10)); // 6-16 files
        
        // Update configuration files
        createDecoyConfigFiles();
        
        System.out.println("✓ Decoy vault refreshed with " + decoyFiles.size() + " new fake files");
    }
    
    /**
     * Emergency switch back to real vault (bypasses all checks)
     */
    public void emergencySwitchToRealVault() {
        System.out.println("🚨 EMERGENCY: Switching to real vault");
        
        isDecoyMode = false;
        
        System.out.println("✓ Emergency switch completed");
        System.out.println("  Real vault: " + realVaultPath);
        System.out.println("  ⚠️ Verify your data integrity!");
    }
    
    /**
     * Get decoy files for UI display
     */
    public List<VaultFile> getDecoyFiles() {
        if (!isDecoyMode) {
            return new ArrayList<>(); // Return empty list if not in decoy mode
        }
        
        // Load decoy files if not already loaded
        if (decoyFiles.isEmpty() && decoyVaultInitialized) {
            try {
                loadDecoyFilesFromDisk();
            } catch (Exception e) {
                System.err.println("⚠️ Error loading decoy files: " + e.getMessage());
            }
        }
        
        return new ArrayList<>(decoyFiles);
    }
    
    /**
     * Load decoy files from disk
     */
    private void loadDecoyFilesFromDisk() throws IOException {
        Path filesDir = decoyVaultPath.resolve("files");
        if (!Files.exists(filesDir)) {
            return;
        }
        
        Files.walk(filesDir)
            .filter(Files::isRegularFile)
            .filter(path -> path.toString().endsWith(".enc"))
            .forEach(filePath -> {
                try {
                    String fileName = filePath.getFileName().toString();
                    fileName = fileName.substring(0, fileName.lastIndexOf(".enc")); // Remove .enc extension
                    
                    byte[] content = Files.readAllBytes(filePath);
                    VaultFile decoyFile = new VaultFile(
                        fileName,
                        UUID.randomUUID().toString(),
                        fileName,
                        content.length,
                        FileUtils.calculateSHA256(content),
                        Files.getLastModifiedTime(filePath).toMillis()
                    );
                    
                    decoyFiles.add(decoyFile);
                } catch (Exception e) {
                    System.err.println("⚠️ Error loading decoy file: " + filePath + " - " + e.getMessage());
                }
            });
    }
    
    /**
     * Get fake decoy file content (for UI display)
     */
    public byte[] getDecoyFileContent(String fileName) throws IOException {
        if (!isDecoyMode) {
            throw new SecurityException("Not in decoy mode!");
        }
        
        Path filePath = decoyVaultPath.resolve("files").resolve(fileName + ".enc");
        if (Files.exists(filePath)) {
            // Return fake "decrypted" content
            byte[] encryptedContent = Files.readAllBytes(filePath);
            return generateFakeDecryptedContent(fileName, encryptedContent);
        }
        
        throw new IOException("Decoy file not found: " + fileName);
    }
    
    /**
     * Generate fake decrypted content for display
     */
    private byte[] generateFakeDecryptedContent(String fileName, byte[] encryptedContent) {
        // Generate realistic fake content based on file name
        String fakeContent = generateRealisticContent(fileName);
        return fakeContent.getBytes();
    }
    
    /**
     * Auto-generate decoy files when vault is accessed from new device
     */
    public void autoGenerateForNewDevice() throws Exception {
        if (!isDecoyMode) {
            return;
        }
        
        System.out.println("🎭 Auto-generating device-specific decoys...");
        
        // Check if we already have device-specific files
        boolean hasDeviceFiles = decoyFiles.stream()
            .anyMatch(file -> file.getOriginalName().contains(userName) || 
                             file.getOriginalName().contains(deviceId));
        
        if (!hasDeviceFiles) {
            // Generate device-specific decoys
            generateDeviceSpecificDecoys();
            
            // Add some random files to make it look natural
            generateRealisticDecoyFiles(3 + random.nextInt(5));
            
            System.out.println("✓ Auto-generated " + decoyFiles.size() + " device-specific decoy files");
        }
    }
    
    /**
     * Generate realistic file name
     */
    private String generateRealisticFileName() {
        String[] extensions = {".txt", ".docx", ".pdf", ".xlsx", ".pptx"};
        String extension = extensions[random.nextInt(extensions.length)];
        
        String baseName;
        switch (random.nextInt(6)) {
            case 0:
                baseName = DOCUMENT_TYPES[random.nextInt(DOCUMENT_TYPES.length)] + 
                          "_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
                break;
            case 1:
                baseName = PROJECT_NAMES[random.nextInt(PROJECT_NAMES.length)].replace(" ", "_") + 
                          "_Report";
                break;
            case 2:
                baseName = "Budget_" + (LocalDate.now().getYear()) + "_Q" + 
                          (random.nextInt(4) + 1);
                break;
            case 3:
                baseName = "Meeting_" + LocalDate.now().format(DateTimeFormatter.ofPattern("MMM_dd"));
                break;
            case 4:
                baseName = PERSON_NAMES[random.nextInt(PERSON_NAMES.length)].replace(" ", "_") + 
                          "_Notes";
                break;
            default:
                baseName = "Document_" + 
                          LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd"));
        }
        
        return baseName + extension;
    }
    
    /**
     * Generate realistic content based on file name
     */
    private String generateRealisticContent(String fileName) {
        String extension = FileUtils.getFileExtension(fileName);
        String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
        
        switch (extension) {
            case "txt":
                return generateTextContent(baseName);
            case "docx":
                return generateDocumentContent(baseName);
            case "pdf":
                return generateReportContent(baseName);
            case "xlsx":
                return generateSpreadsheetContent(baseName);
            case "pptx":
                return generatePresentationContent(baseName);
            default:
                return generateGenericContent(baseName);
        }
    }
    
    /**
     * Generate text file content
     */
    private String generateTextContent(String baseName) {
        StringBuilder content = new StringBuilder();
        
        if (baseName.toLowerCase().contains("meeting")) {
            content.append(generateMeetingNotes());
        } else if (baseName.toLowerCase().contains("notes")) {
            content.append(generatePersonalNotes());
        } else if (baseName.toLowerCase().contains("todo") || baseName.toLowerCase().contains("task")) {
            content.append(generateTaskList());
        } else {
            content.append(generateGenericNotes());
        }
        
        return content.toString();
    }
    
    /**
     * Generate meeting notes content
     */
    private String generateMeetingNotes() {
        StringBuilder notes = new StringBuilder();
        String company = BUSINESS_NAMES[random.nextInt(BUSINESS_NAMES.length)];
        String project = PROJECT_NAMES[random.nextInt(PROJECT_NAMES.length)];
        
        notes.append("MEETING NOTES\n");
        notes.append("=============\n\n");
        notes.append("Date: ").append(LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"))).append("\n");
        notes.append("Company: ").append(company).append("\n");
        notes.append("Project: ").append(project).append("\n\n");
        
        notes.append("Attendees:\n");
        for (int i = 0; i < 3 + random.nextInt(3); i++) {
            notes.append("- ").append(PERSON_NAMES[random.nextInt(PERSON_NAMES.length)]).append("\n");
        }
        
        notes.append("\nAgenda:\n");
        String[] agendaItems = {
            "Budget review and allocation",
            "Project timeline discussion",
            "Resource planning",
            "Risk assessment",
            "Quality assurance review",
            "Stakeholder feedback",
            "Next phase planning"
        };
        
        for (int i = 0; i < 3 + random.nextInt(3); i++) {
            notes.append((i + 1)).append(". ").append(agendaItems[random.nextInt(agendaItems.length)]).append("\n");
        }
        
        notes.append("\nAction Items:\n");
        notes.append("- Review budget proposals by end of week\n");
        notes.append("- Schedule follow-up meeting with stakeholders\n");
        notes.append("- Prepare status report for management\n");
        notes.append("- Update project documentation\n");
        
        notes.append("\nNext Meeting: ").append(LocalDate.now().plusWeeks(1).format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"))).append("\n");
        
        return notes.toString();
    }
    
    /**
     * Generate personal notes content
     */
    private String generatePersonalNotes() {
        StringBuilder notes = new StringBuilder();
        
        notes.append("Personal Notes\n");
        notes.append("==============\n\n");
        notes.append("Date: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).append("\n\n");
        
        String[] noteTypes = {
            "Project ideas and brainstorming session",
            "Book recommendations and reading list",
            "Travel planning and itinerary notes",
            "Learning objectives and study plan",
            "Health and fitness goals",
            "Financial planning and budget notes"
        };
        
        notes.append(noteTypes[random.nextInt(noteTypes.length)]).append("\n\n");
        
        notes.append("Key Points:\n");
        notes.append("- Focus on long-term objectives\n");
        notes.append("- Maintain work-life balance\n");
        notes.append("- Regular progress reviews\n");
        notes.append("- Continuous learning and improvement\n");
        notes.append("- Network building and relationship management\n\n");
        
        notes.append("Next Steps:\n");
        notes.append("1. Research and gather more information\n");
        notes.append("2. Create detailed action plan\n");
        notes.append("3. Set realistic timelines\n");
        notes.append("4. Monitor progress regularly\n");
        
        return notes.toString();
    }
    
    /**
     * Generate task list content
     */
    private String generateTaskList() {
        StringBuilder tasks = new StringBuilder();
        
        tasks.append("TASK LIST\n");
        tasks.append("=========\n\n");
        tasks.append("Week of: ").append(LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"))).append("\n\n");
        
        String[] taskCategories = {"Work", "Personal", "Health", "Learning", "Finance"};
        
        for (String category : Arrays.copyOf(taskCategories, 2 + random.nextInt(3))) {
            tasks.append(category).append(":\n");
            
            String[] workTasks = {
                "Complete project documentation",
                "Review team performance metrics",
                "Prepare presentation for client meeting",
                "Update project timeline",
                "Conduct code review"
            };
            
            String[] personalTasks = {
                "Grocery shopping",
                "Schedule dentist appointment",
                "Plan weekend activities",
                "Call family members",
                "Organize home office"
            };
            
            String[] tasks_list = category.equals("Work") ? workTasks : personalTasks;
            
            for (int i = 0; i < 2 + random.nextInt(3); i++) {
                tasks.append("  [ ] ").append(tasks_list[random.nextInt(tasks_list.length)]).append("\n");
            }
            tasks.append("\n");
        }
        
        return tasks.toString();
    }
    
    /**
     * Generate generic notes content
     */
    private String generateGenericNotes() {
        StringBuilder notes = new StringBuilder();
        
        notes.append("Notes and Observations\n");
        notes.append("======================\n\n");
        notes.append("Created: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).append("\n\n");
        
        String[] topics = {
            "Market analysis and trends",
            "Technology evaluation",
            "Process improvement ideas",
            "Customer feedback summary",
            "Competitive analysis",
            "Innovation opportunities"
        };
        
        notes.append("Topic: ").append(topics[random.nextInt(topics.length)]).append("\n\n");
        
        notes.append("Summary:\n");
        notes.append("This document contains preliminary observations and analysis ");
        notes.append("based on recent research and data collection. The findings ");
        notes.append("suggest several areas for further investigation and potential ");
        notes.append("improvement opportunities.\n\n");
        
        notes.append("Key Findings:\n");
        notes.append("- Current processes show room for optimization\n");
        notes.append("- Market conditions are favorable for expansion\n");
        notes.append("- Technology adoption rates are increasing\n");
        notes.append("- Customer satisfaction metrics are stable\n\n");
        
        notes.append("Recommendations:\n");
        notes.append("1. Conduct detailed feasibility study\n");
        notes.append("2. Engage stakeholders for feedback\n");
        notes.append("3. Develop implementation timeline\n");
        notes.append("4. Allocate necessary resources\n");
        
        return notes.toString();
    }
    
    /**
     * Generate document content
     */
    private String generateDocumentContent(String baseName) {
        StringBuilder doc = new StringBuilder();
        
        doc.append("DOCUMENT HEADER\n");
        doc.append("===============\n\n");
        doc.append("Title: ").append(baseName.replace("_", " ")).append("\n");
        doc.append("Date: ").append(LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"))).append("\n");
        doc.append("Author: ").append(PERSON_NAMES[random.nextInt(PERSON_NAMES.length)]).append("\n");
        doc.append("Company: ").append(BUSINESS_NAMES[random.nextInt(BUSINESS_NAMES.length)]).append("\n\n");
        
        doc.append("EXECUTIVE SUMMARY\n");
        doc.append("=================\n\n");
        doc.append("This document provides an overview of current operations and ");
        doc.append("strategic initiatives. The analysis covers key performance ");
        doc.append("indicators, market conditions, and future opportunities.\n\n");
        
        doc.append("MAIN CONTENT\n");
        doc.append("============\n\n");
        doc.append("1. Current Status\n");
        doc.append("   - Operations are running smoothly\n");
        doc.append("   - All targets are being met\n");
        doc.append("   - Team performance is excellent\n\n");
        
        doc.append("2. Future Plans\n");
        doc.append("   - Expand into new markets\n");
        doc.append("   - Improve operational efficiency\n");
        doc.append("   - Invest in new technologies\n\n");
        
        doc.append("CONCLUSION\n");
        doc.append("==========\n\n");
        doc.append("The organization is well-positioned for continued growth ");
        doc.append("and success. Regular monitoring and adjustment of strategies ");
        doc.append("will ensure optimal performance.\n");
        
        return doc.toString();
    }
    
    /**
     * Generate report content
     */
    private String generateReportContent(String baseName) {
        return generateDocumentContent(baseName) + "\n\n[This would be a PDF report in actual implementation]";
    }
    
    /**
     * Generate spreadsheet content
     */
    private String generateSpreadsheetContent(String baseName) {
        StringBuilder sheet = new StringBuilder();
        
        sheet.append("SPREADSHEET DATA\n");
        sheet.append("================\n\n");
        sheet.append("File: ").append(baseName).append(".xlsx\n");
        sheet.append("Created: ").append(LocalDate.now()).append("\n\n");
        
        if (baseName.toLowerCase().contains("budget")) {
            sheet.append("BUDGET BREAKDOWN\n");
            sheet.append("Category\t\tAmount\t\tPercentage\n");
            sheet.append("Personnel\t\t$125,000\t45%\n");
            sheet.append("Equipment\t\t$75,000\t\t27%\n");
            sheet.append("Operations\t\t$50,000\t\t18%\n");
            sheet.append("Marketing\t\t$25,000\t\t9%\n");
            sheet.append("Miscellaneous\t$5,000\t\t1%\n");
            sheet.append("TOTAL\t\t\t$280,000\t100%\n");
        } else {
            sheet.append("DATA SUMMARY\n");
            sheet.append("Month\t\tRevenue\t\tExpenses\tProfit\n");
            for (int i = 1; i <= 6; i++) {
                int revenue = 50000 + random.nextInt(20000);
                int expenses = 30000 + random.nextInt(15000);
                int profit = revenue - expenses;
                sheet.append("Month ").append(i).append("\t\t$").append(revenue)
                     .append("\t\t$").append(expenses).append("\t\t$").append(profit).append("\n");
            }
        }
        
        sheet.append("\n[This would be an Excel file in actual implementation]");
        
        return sheet.toString();
    }
    
    /**
     * Generate presentation content
     */
    private String generatePresentationContent(String baseName) {
        StringBuilder ppt = new StringBuilder();
        
        ppt.append("PRESENTATION OUTLINE\n");
        ppt.append("====================\n\n");
        ppt.append("Title: ").append(baseName.replace("_", " ")).append("\n");
        ppt.append("Presenter: ").append(PERSON_NAMES[random.nextInt(PERSON_NAMES.length)]).append("\n");
        ppt.append("Date: ").append(LocalDate.now()).append("\n\n");
        
        ppt.append("SLIDE OUTLINE:\n\n");
        ppt.append("Slide 1: Title Slide\n");
        ppt.append("Slide 2: Agenda\n");
        ppt.append("Slide 3: Current Situation\n");
        ppt.append("Slide 4: Key Challenges\n");
        ppt.append("Slide 5: Proposed Solutions\n");
        ppt.append("Slide 6: Implementation Plan\n");
        ppt.append("Slide 7: Expected Outcomes\n");
        ppt.append("Slide 8: Next Steps\n");
        ppt.append("Slide 9: Questions & Discussion\n\n");
        
        ppt.append("KEY POINTS:\n");
        ppt.append("- Clear problem definition\n");
        ppt.append("- Data-driven analysis\n");
        ppt.append("- Practical solutions\n");
        ppt.append("- Realistic timeline\n");
        ppt.append("- Measurable outcomes\n\n");
        
        ppt.append("[This would be a PowerPoint file in actual implementation]");
        
        return ppt.toString();
    }
    
    /**
     * Generate generic content
     */
    private String generateGenericContent(String baseName) {
        return "Generic document content for: " + baseName + "\n\n" +
               "This is a placeholder document created for demonstration purposes.\n" +
               "In a real scenario, this would contain relevant business content.\n\n" +
               "Created: " + LocalDateTime.now() + "\n" +
               "Size: " + (500 + random.nextInt(1000)) + " words\n";
    }
    
    /**
     * CRITICAL: Prevent accidental operations on real vault during decoy mode
     */
    public void validateOperation(String operation) throws SecurityException {
        if (isDecoyMode) {
            System.out.println("🎭 DECOY MODE: " + operation + " performed on FAKE data");
        } else {
            System.out.println("🔒 REAL MODE: " + operation + " performed on REAL data");
        }
    }
    
    /**
     * Get vault statistics (real or decoy based on mode)
     */
    public VaultStats getVaultStats() {
        if (isDecoyMode) {
            return getDecoyStats();
        } else {
            return getRealVaultStats();
        }
    }
    
    /**
     * Get decoy vault statistics
     */
    private VaultStats getDecoyStats() {
        long totalSize = decoyFiles.stream().mapToLong(VaultFile::getSize).sum();
        
        Map<String, Integer> extensionCounts = new HashMap<>();
        for (VaultFile file : decoyFiles) {
            String extension = getFileExtension(file.getOriginalName());
            extensionCounts.put(extension, extensionCounts.getOrDefault(extension, 0) + 1);
        }
        
        return new VaultStats(decoyFiles.size(), totalSize, extensionCounts, true);
    }
    
    /**
     * Get real vault statistics (placeholder - would integrate with real vault manager)
     */
    private VaultStats getRealVaultStats() {
        // This would integrate with the actual vault manager
        // For now, return placeholder stats
        return new VaultStats(0, 0, new HashMap<>(), false);
    }
    
    /**
     * Get file extension
     */
    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot + 1).toLowerCase() : "unknown";
    }
    
    /**
     * Vault statistics class
     */
    public static class VaultStats {
        private final int fileCount;
        private final long totalSize;
        private final Map<String, Integer> extensionCounts;
        private final boolean isDecoy;
        
        public VaultStats(int fileCount, long totalSize, Map<String, Integer> extensionCounts, boolean isDecoy) {
            this.fileCount = fileCount;
            this.totalSize = totalSize;
            this.extensionCounts = new HashMap<>(extensionCounts);
            this.isDecoy = isDecoy;
        }
        
        public int getFileCount() { return fileCount; }
        public long getTotalSize() { return totalSize; }
        public Map<String, Integer> getExtensionCounts() { return new HashMap<>(extensionCounts); }
        public boolean isDecoy() { return isDecoy; }
        
        public String getFormattedSize() {
            return FileUtils.formatFileSize(totalSize);
        }
        
        @Override
        public String toString() {
            String mode = isDecoy ? "DECOY" : "REAL";
            return String.format("VaultStats{mode=%s, files=%d, size=%s, types=%d}", 
                mode, fileCount, getFormattedSize(), extensionCounts.size());
        }
    }
    
    /**
     * Search files (real or decoy based on current mode)
     */
    public List<VaultFile> searchFiles(String query) {
        if (!isDecoyMode) {
            // In real mode, would delegate to real vault manager
            return new ArrayList<>();
        }
        
        if (query == null || query.trim().isEmpty()) {
            return getDecoyFiles();
        }
        
        String lowerQuery = query.toLowerCase();
        return decoyFiles.stream()
                .filter(file -> file.getOriginalName().toLowerCase().contains(lowerQuery))
                .collect(ArrayList::new, (list, file) -> list.add(file), ArrayList::addAll);
    }
    
    /**
     * Remove file (real or decoy based on current mode)
     */
    public boolean removeFile(String fileName) {
        validateOperation("DELETE file: " + fileName);
        
        if (!isDecoyMode) {
            // In real mode, would delegate to real vault manager
            System.out.println("🔒 REAL MODE: File deletion would be handled by real vault manager");
            return false;
        }
        
        // Remove decoy file
        VaultFile decoyFile = decoyFiles.stream()
                .filter(file -> file.getOriginalName().equals(fileName))
                .findFirst()
                .orElse(null);
        
        if (decoyFile != null) {
            try {
                Path filePath = decoyVaultPath.resolve("files").resolve(fileName + ".enc");
                Files.deleteIfExists(filePath);
                decoyFiles.remove(decoyFile);
                System.out.println("🎭 DECOY: Removed fake file: " + fileName);
                return true;
            } catch (IOException e) {
                System.err.println("⚠️ Error removing decoy file: " + e.getMessage());
                return false;
            }
        }
        return false;
    }
    
    /**
     * Add file (real or decoy based on current mode)
     */
    public boolean addFile(String fileName, byte[] content) {
        validateOperation("ADD file: " + fileName);
        
        if (!isDecoyMode) {
            // In real mode, would delegate to real vault manager
            System.out.println("🔒 REAL MODE: File addition would be handled by real vault manager");
            return false;
        }
        
        // Add fake file to decoy vault
        try {
            Path filesDir = decoyVaultPath.resolve("files");
            createDecoyFile(filesDir, fileName, new String(content));
            System.out.println("🎭 DECOY: Added fake file: " + fileName);
            return true;
        } catch (IOException e) {
            System.err.println("⚠️ Error adding decoy file: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get file count (real or decoy based on current mode)
     */
    public int getFileCount() {
        return isDecoyMode ? decoyFiles.size() : 0; // Real vault count would come from real vault manager
    }
    
    /**
     * Ensure minimum decoy files exist (for realism)
     */
    public void ensureMinimumDecoyFiles(int minimumCount) throws Exception {
        if (!isDecoyMode) {
            return;
        }
        
        int currentCount = decoyFiles.size();
        if (currentCount < minimumCount) {
            int needed = minimumCount - currentCount;
            generateRealisticDecoyFiles(needed);
            System.out.println("✓ Generated " + needed + " additional decoy files to reach minimum of " + minimumCount);
        }
    }
    
    /**
     * Create a fake file object for UI display (doesn't create actual file)
     */
    public File createFakeFile(File parentDir, String fileName) {
        return new File(parentDir, fileName);
    }
    
    /**
     * Activate decoy mode
     */
    public void activateDecoyMode() {
        try {
            // Ensure decoy vault exists and is populated
            initializeDecoyVault();
            System.out.println("✓ Decoy mode activated");
        } catch (Exception e) {
            System.err.println("✗ Failed to activate decoy mode: " + e.getMessage());
        }
    }
}
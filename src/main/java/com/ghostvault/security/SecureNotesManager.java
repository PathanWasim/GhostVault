package com.ghostvault.security;

import com.ghostvault.util.FileUtils;

import javax.crypto.SecretKey;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages encrypted secure notes storage and retrieval
 */
public class SecureNotesManager {
    
    private final String vaultPath;
    private final String notesFilePath;
    private final Map<String, SecureNote> notes;
    private final CryptoManager cryptoManager;
    private SecretKey encryptionKey;
    
    public SecureNotesManager(String vaultPath) {
        this.vaultPath = vaultPath;
        this.notesFilePath = vaultPath + "/notes.enc";
        this.notes = new HashMap<>();
        this.cryptoManager = new CryptoManager();
        
        // Ensure vault directory exists
        try {
            Files.createDirectories(Paths.get(vaultPath));
        } catch (IOException e) {
            throw new RuntimeException("Failed to create vault directory", e);
        }
    }
    
    /**
     * Set encryption key for notes operations
     */
    public void setEncryptionKey(SecretKey key) {
        this.encryptionKey = key;
    }
    
    /**
     * Load notes from encrypted storage
     */
    public void loadNotes() throws Exception {
        Path notesPath = Paths.get(notesFilePath);
        
        if (!Files.exists(notesPath)) {
            // Create sample notes for demo
            createSampleNotes();
            return;
        }
        
        if (encryptionKey == null) {
            throw new IllegalStateException("Encryption key not set");
        }
        
        try {
            // Read encrypted data
            CryptoManager.EncryptedData encrypted = FileUtils.readEncryptedFile(notesPath);
            
            // Decrypt notes
            byte[] decryptedData = cryptoManager.decrypt(encrypted, encryptionKey);
            
            // Deserialize notes
            ByteArrayInputStream bais = new ByteArrayInputStream(decryptedData);
            try (ObjectInputStream ois = new ObjectInputStream(bais)) {
                @SuppressWarnings("unchecked")
                Map<String, SecureNote> loadedNotes = (Map<String, SecureNote>) ois.readObject();
                notes.clear();
                notes.putAll(loadedNotes);
            }
            
            // Clear sensitive data from memory
            Arrays.fill(decryptedData, (byte) 0);
            
        } catch (Exception e) {
            throw new Exception("Failed to load notes: " + e.getMessage(), e);
        }
    }
    
    /**
     * Save notes to encrypted storage
     */
    public void saveNotes() throws Exception {
        if (encryptionKey == null) {
            throw new IllegalStateException("Encryption key not set");
        }
        
        byte[] serializedData = null;
        
        try {
            // Serialize notes to bytes
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(new HashMap<>(notes));
            }
            
            serializedData = baos.toByteArray();
            
            // Encrypt notes
            byte[] encryptedBytes = cryptoManager.encrypt(serializedData, encryptionKey);
            CryptoManager.EncryptedData encrypted = CryptoManager.EncryptedData.fromCombinedData(encryptedBytes);
            
            // Write to file
            FileUtils.writeEncryptedFile(Paths.get(notesFilePath), encrypted);
            
        } finally {
            // Clear sensitive data from memory
            if (serializedData != null) {
                Arrays.fill(serializedData, (byte) 0);
            }
        }
    }
    
    /**
     * Add or update note
     */
    public void addNote(SecureNote note) throws Exception {
        notes.put(note.getId(), note);
        saveNotes();
    }
    
    /**
     * Remove note
     */
    public void removeNote(String id) throws Exception {
        notes.remove(id);
        saveNotes();
    }
    
    /**
     * Get note by ID
     */
    public SecureNote getNote(String id) {
        return notes.get(id);
    }
    
    /**
     * Get all notes
     */
    public List<SecureNote> getAllNotes() {
        return notes.values().stream()
                .sorted((n1, n2) -> {
                    // Pinned notes first
                    if (n1.isPinned() && !n2.isPinned()) return -1;
                    if (!n1.isPinned() && n2.isPinned()) return 1;
                    // Then by modified date (newest first)
                    return n2.getModifiedDate().compareTo(n1.getModifiedDate());
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Search notes by title or content
     */
    public List<SecureNote> searchNotes(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllNotes();
        }
        
        String lowerQuery = query.toLowerCase();
        return notes.values().stream()
                .filter(note -> 
                    (note.getTitle() != null && note.getTitle().toLowerCase().contains(lowerQuery)) ||
                    (note.getContent() != null && note.getContent().toLowerCase().contains(lowerQuery)) ||
                    (note.getCategory() != null && note.getCategory().toLowerCase().contains(lowerQuery)) ||
                    (note.getTags() != null && note.getTags().toLowerCase().contains(lowerQuery))
                )
                .sorted((n1, n2) -> {
                    // Pinned notes first
                    if (n1.isPinned() && !n2.isPinned()) return -1;
                    if (!n1.isPinned() && n2.isPinned()) return 1;
                    // Then by modified date (newest first)
                    return n2.getModifiedDate().compareTo(n1.getModifiedDate());
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Get notes by category
     */
    public List<SecureNote> getNotesByCategory(String category) {
        if (category == null || category.equals("All")) {
            return getAllNotes();
        }
        
        return notes.values().stream()
                .filter(note -> category.equals(note.getCategory()))
                .sorted((n1, n2) -> {
                    if (n1.isPinned() && !n2.isPinned()) return -1;
                    if (!n1.isPinned() && n2.isPinned()) return 1;
                    return n2.getModifiedDate().compareTo(n1.getModifiedDate());
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Get pinned notes
     */
    public List<SecureNote> getPinnedNotes() {
        return notes.values().stream()
                .filter(SecureNote::isPinned)
                .sorted((n1, n2) -> n2.getModifiedDate().compareTo(n1.getModifiedDate()))
                .collect(Collectors.toList());
    }
    
    /**
     * Get all categories
     */
    public Set<String> getAllCategories() {
        Set<String> categories = notes.values().stream()
                .map(SecureNote::getCategory)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        
        categories.add("All");
        return categories;
    }
    
    /**
     * Create sample notes for demo
     */
    private void createSampleNotes() {
        try {
            SecureNote note1 = new SecureNote("Security Protocols", 
                "Important security procedures:\n\n" +
                "1. Always use 2FA when available\n" +
                "2. Regular password updates every 90 days\n" +
                "3. VPN for all public WiFi connections\n" +
                "4. Encrypted backups weekly\n\n" +
                "Emergency contacts:\n" +
                "- IT Security: ext. 2847\n" +
                "- Backup admin: ext. 1923");
            note1.setCategory("Security");
            note1.setPinned(true);
            
            SecureNote note2 = new SecureNote("Investment Strategy", 
                "Long-term investment plan:\n\n" +
                "Portfolio allocation:\n" +
                "- 60% Index funds (S&P 500, Total Market)\n" +
                "- 25% International funds\n" +
                "- 10% Bonds\n" +
                "- 5% Individual stocks\n\n" +
                "Review quarterly and rebalance annually.\n" +
                "Target: 7-8% annual return over 20 years.");
            note2.setCategory("Finance");
            note2.setPinned(true);
            
            SecureNote note3 = new SecureNote("Account Recovery Codes", 
                "Backup codes for critical accounts:\n\n" +
                "Google: 1234-5678-9012\n" +
                "Microsoft: ABCD-EFGH-IJKL\n" +
                "GitHub: 9876-5432-1098\n\n" +
                "Store these codes securely!\n" +
                "Last updated: " + java.time.LocalDate.now());
            note3.setCategory("Security");
            
            SecureNote note4 = new SecureNote("Project Ideas", 
                "Innovative project concepts:\n\n" +
                "1. AI-powered personal finance tracker\n" +
                "2. Blockchain-based identity verification\n" +
                "3. IoT home security system\n" +
                "4. Sustainable energy monitoring app\n\n" +
                "Research needed:\n" +
                "- Market analysis\n" +
                "- Technical feasibility\n" +
                "- Funding options");
            note4.setCategory("Ideas");
            
            SecureNote note5 = new SecureNote("Backup Procedures", 
                "Critical data backup checklist:\n\n" +
                "Daily:\n" +
                "- Automated cloud sync (encrypted)\n" +
                "- Local incremental backup\n\n" +
                "Weekly:\n" +
                "- Full system backup to external drive\n" +
                "- Verify backup integrity\n\n" +
                "Monthly:\n" +
                "- Offsite backup rotation\n" +
                "- Test restore procedures\n\n" +
                "Next review: " + java.time.LocalDate.now().plusMonths(1));
            note5.setCategory("Security");
            
            notes.put(note1.getId(), note1);
            notes.put(note2.getId(), note2);
            notes.put(note3.getId(), note3);
            notes.put(note4.getId(), note4);
            notes.put(note5.getId(), note5);
            
            if (encryptionKey != null) {
                saveNotes();
            }
            
        } catch (Exception e) {
            System.err.println("Failed to create sample notes: " + e.getMessage());
        }
    }
    
    /**
     * Get notes statistics
     */
    public Map<String, Object> getNotesStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalNotes", notes.size());
        stats.put("pinnedNotes", getPinnedNotes().size());
        stats.put("categories", getAllCategories().size() - 1); // Exclude "All"
        
        // Total word count
        int totalWords = notes.values().stream()
                .mapToInt(SecureNote::getWordCount)
                .sum();
        stats.put("totalWords", totalWords);
        
        // Total character count
        int totalChars = notes.values().stream()
                .mapToInt(SecureNote::getCharacterCount)
                .sum();
        stats.put("totalCharacters", totalChars);
        
        return stats;
    }
    
    /**
     * Load data - compatibility method for CompactNotesWindow
     */
    public void loadData() throws Exception {
        loadNotes();
    }
    
    /**
     * Get notes - compatibility method for CompactNotesWindow
     */
    public List<SecureNote> getNotes() {
        return getAllNotes();
    }
    
    /**
     * Add note with parameters - compatibility method for CompactNotesWindow
     */
    public void addNote(String title, String content, String category, List<String> tags) throws Exception {
        SecureNote note = new SecureNote(title, content);
        note.setCategory(category);
        if (tags != null && !tags.isEmpty()) {
            note.setTags(String.join(", ", tags));
        }
        addNote(note);
    }
    
    /**
     * Delete note - compatibility method for CompactNotesWindow
     */
    public void deleteNote(String id) throws Exception {
        removeNote(id);
    }
}
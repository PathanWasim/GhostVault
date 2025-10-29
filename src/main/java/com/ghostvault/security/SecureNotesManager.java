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
            // No existing notes file - start with empty notes
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
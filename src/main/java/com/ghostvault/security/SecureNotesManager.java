package com.ghostvault.security;

import com.ghostvault.model.SecureNote;
import com.ghostvault.model.StoredPassword;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.crypto.SecretKey;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Secure Notes and Password Manager for GhostVault
 * Provides encrypted storage for notes, passwords, and sensitive information
 */
public class SecureNotesManager {
    
    private final String notesFilePath;
    private final String passwordsFilePath;
    private final CryptoManager cryptoManager;
    private SecretKey encryptionKey;
    
    private final ObservableList<SecureNote> notes = FXCollections.observableArrayList();
    private final ObservableList<StoredPassword> passwords = FXCollections.observableArrayList();
    
    public SecureNotesManager(String vaultPath) throws Exception {
        this.notesFilePath = vaultPath + "/secure_notes.enc";
        this.passwordsFilePath = vaultPath + "/stored_passwords.enc";
        this.cryptoManager = new CryptoManager();
    }
    
    /**
     * Set encryption key
     */
    public void setEncryptionKey(SecretKey key) {
        this.encryptionKey = key;
    }
    
    /**
     * Load encrypted notes and passwords
     */
    public void loadData() throws Exception {
        loadNotes();
        loadPasswords();
    }
    
    /**
     * Save encrypted notes and passwords
     */
    public void saveData() throws Exception {
        saveNotes();
        savePasswords();
    }
    
    // ==================== SECURE NOTES ====================
    
    /**
     * Add a new secure note
     */
    public SecureNote addNote(String title, String content, String category, List<String> tags) {
        SecureNote note = new SecureNote(
            UUID.randomUUID().toString(),
            title,
            content,
            category,
            tags,
            LocalDateTime.now(),
            LocalDateTime.now()
        );
        
        notes.add(note);
        
        try {
            saveNotes();
        } catch (Exception e) {
            System.err.println("Failed to save notes: " + e.getMessage());
        }
        
        return note;
    }
    
    /**
     * Update an existing note
     */
    public void updateNote(String noteId, String title, String content, String category, List<String> tags) {
        notes.stream()
            .filter(note -> note.getId().equals(noteId))
            .findFirst()
            .ifPresent(note -> {
                note.setTitle(title);
                note.setContent(content);
                note.setCategory(category);
                note.setTags(tags);
                note.setModifiedDate(LocalDateTime.now());
                
                try {
                    saveNotes();
                } catch (Exception e) {
                    System.err.println("Failed to save notes: " + e.getMessage());
                }
            });
    }
    
    /**
     * Delete a note
     */
    public void deleteNote(String noteId) {
        notes.removeIf(note -> note.getId().equals(noteId));
        
        try {
            saveNotes();
        } catch (Exception e) {
            System.err.println("Failed to save notes: " + e.getMessage());
        }
    }
    
    /**
     * Search notes
     */
    public List<SecureNote> searchNotes(String query) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>(notes);
        }
        
        String lowerQuery = query.toLowerCase();
        return notes.stream()
            .filter(note -> 
                note.getTitle().toLowerCase().contains(lowerQuery) ||
                note.getContent().toLowerCase().contains(lowerQuery) ||
                note.getCategory().toLowerCase().contains(lowerQuery) ||
                note.getTags().stream().anyMatch(tag -> tag.toLowerCase().contains(lowerQuery))
            )
            .collect(Collectors.toList());
    }
    
    /**
     * Get notes by category
     */
    public List<SecureNote> getNotesByCategory(String category) {
        return notes.stream()
            .filter(note -> note.getCategory().equals(category))
            .collect(Collectors.toList());
    }
    
    /**
     * Get all note categories
     */
    public List<String> getNoteCategories() {
        return notes.stream()
            .map(SecureNote::getCategory)
            .distinct()
            .sorted()
            .collect(Collectors.toList());
    }
    
    // ==================== PASSWORD MANAGER ====================
    
    /**
     * Add a new stored password
     */
    public StoredPassword addPassword(String title, String username, String password, String website, 
                                    String notes, String category, List<String> tags) {
        StoredPassword storedPassword = new StoredPassword(
            UUID.randomUUID().toString(),
            title,
            username,
            password,
            website,
            notes,
            category,
            tags,
            LocalDateTime.now(),
            LocalDateTime.now()
        );
        
        passwords.add(storedPassword);
        
        try {
            savePasswords();
        } catch (Exception e) {
            System.err.println("Failed to save passwords: " + e.getMessage());
        }
        
        return storedPassword;
    }
    
    /**
     * Update an existing password
     */
    public void updatePassword(String passwordId, String title, String username, String password, 
                             String website, String notes, String category, List<String> tags) {
        passwords.stream()
            .filter(pwd -> pwd.getId().equals(passwordId))
            .findFirst()
            .ifPresent(pwd -> {
                pwd.setTitle(title);
                pwd.setUsername(username);
                pwd.setPassword(password);
                pwd.setWebsite(website);
                pwd.setNotes(notes);
                pwd.setCategory(category);
                pwd.setTags(tags);
                pwd.setModifiedDate(LocalDateTime.now());
                
                try {
                    savePasswords();
                } catch (Exception e) {
                    System.err.println("Failed to save passwords: " + e.getMessage());
                }
            });
    }
    
    /**
     * Delete a password
     */
    public void deletePassword(String passwordId) {
        passwords.removeIf(pwd -> pwd.getId().equals(passwordId));
        
        try {
            savePasswords();
        } catch (Exception e) {
            System.err.println("Failed to save passwords: " + e.getMessage());
        }
    }
    
    /**
     * Search passwords
     */
    public List<StoredPassword> searchPasswords(String query) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>(passwords);
        }
        
        String lowerQuery = query.toLowerCase();
        return passwords.stream()
            .filter(pwd -> 
                pwd.getTitle().toLowerCase().contains(lowerQuery) ||
                pwd.getUsername().toLowerCase().contains(lowerQuery) ||
                pwd.getWebsite().toLowerCase().contains(lowerQuery) ||
                pwd.getCategory().toLowerCase().contains(lowerQuery) ||
                pwd.getTags().stream().anyMatch(tag -> tag.toLowerCase().contains(lowerQuery))
            )
            .collect(Collectors.toList());
    }
    
    /**
     * Generate secure password
     */
    public String generatePassword(int length, boolean includeUppercase, boolean includeLowercase, 
                                 boolean includeNumbers, boolean includeSymbols) {
        StringBuilder charset = new StringBuilder();
        
        if (includeUppercase) charset.append("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        if (includeLowercase) charset.append("abcdefghijklmnopqrstuvwxyz");
        if (includeNumbers) charset.append("0123456789");
        if (includeSymbols) charset.append("!@#$%^&*()_+-=[]{}|;:,.<>?");
        
        if (charset.length() == 0) {
            throw new IllegalArgumentException("At least one character type must be selected");
        }
        
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();
        
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(charset.length());
            password.append(charset.charAt(index));
        }
        
        return password.toString();
    }
    
    /**
     * Check password strength
     */
    public PasswordStrength checkPasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            return new PasswordStrength(0, "Empty password", "Very Weak");
        }
        
        int score = 0;
        List<String> feedback = new ArrayList<>();
        
        // Length check
        if (password.length() >= 12) {
            score += 25;
        } else if (password.length() >= 8) {
            score += 15;
            feedback.add("Consider using a longer password (12+ characters)");
        } else {
            score += 5;
            feedback.add("Password is too short (minimum 8 characters)");
        }
        
        // Character variety
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSymbol = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{}|;:,.<>?].*");
        
        int varietyCount = 0;
        if (hasUpper) varietyCount++;
        if (hasLower) varietyCount++;
        if (hasDigit) varietyCount++;
        if (hasSymbol) varietyCount++;
        
        score += varietyCount * 15;
        
        if (varietyCount < 3) {
            feedback.add("Use a mix of uppercase, lowercase, numbers, and symbols");
        }
        
        // Common patterns check
        if (password.matches(".*123.*") || password.matches(".*abc.*") || password.matches(".*qwe.*")) {
            score -= 10;
            feedback.add("Avoid common sequences like '123' or 'abc'");
        }
        
        // Repetition check
        if (password.matches(".*(.)\\1{2,}.*")) {
            score -= 10;
            feedback.add("Avoid repeating characters");
        }
        
        score = Math.max(0, Math.min(100, score));
        
        String strength;
        if (score >= 80) strength = "Very Strong";
        else if (score >= 60) strength = "Strong";
        else if (score >= 40) strength = "Fair";
        else if (score >= 20) strength = "Weak";
        else strength = "Very Weak";
        
        return new PasswordStrength(score, String.join("; ", feedback), strength);
    }
    
    // ==================== DATA PERSISTENCE ====================
    
    /**
     * Load encrypted notes from file
     */
    private void loadNotes() throws Exception {
        Path notesPath = Paths.get(notesFilePath);
        if (!Files.exists(notesPath)) {
            return; // No notes file yet
        }
        
        if (encryptionKey == null) {
            throw new IllegalStateException("Encryption key not set");
        }
        
        byte[] encryptedData = Files.readAllBytes(notesPath);
        byte[] decryptedData = cryptoManager.decrypt(encryptedData, encryptionKey);
        
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(decryptedData))) {
            @SuppressWarnings("unchecked")
            List<SecureNote> loadedNotes = (List<SecureNote>) ois.readObject();
            notes.clear();
            notes.addAll(loadedNotes);
        }
        
        // Clear sensitive data
        Arrays.fill(decryptedData, (byte) 0);
    }
    
    /**
     * Save encrypted notes to file
     */
    private void saveNotes() throws Exception {
        if (encryptionKey == null) {
            throw new IllegalStateException("Encryption key not set");
        }
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(new ArrayList<>(notes));
        }
        
        byte[] data = baos.toByteArray();
        byte[] encryptedData = cryptoManager.encrypt(data, encryptionKey);
        
        Files.write(Paths.get(notesFilePath), encryptedData);
        
        // Clear sensitive data
        Arrays.fill(data, (byte) 0);
    }
    
    /**
     * Load encrypted passwords from file
     */
    private void loadPasswords() throws Exception {
        Path passwordsPath = Paths.get(passwordsFilePath);
        if (!Files.exists(passwordsPath)) {
            return; // No passwords file yet
        }
        
        if (encryptionKey == null) {
            throw new IllegalStateException("Encryption key not set");
        }
        
        byte[] encryptedData = Files.readAllBytes(passwordsPath);
        byte[] decryptedData = cryptoManager.decrypt(encryptedData, encryptionKey);
        
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(decryptedData))) {
            @SuppressWarnings("unchecked")
            List<StoredPassword> loadedPasswords = (List<StoredPassword>) ois.readObject();
            passwords.clear();
            passwords.addAll(loadedPasswords);
        }
        
        // Clear sensitive data
        Arrays.fill(decryptedData, (byte) 0);
    }
    
    /**
     * Save encrypted passwords to file
     */
    private void savePasswords() throws Exception {
        if (encryptionKey == null) {
            throw new IllegalStateException("Encryption key not set");
        }
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(new ArrayList<>(passwords));
        }
        
        byte[] data = baos.toByteArray();
        byte[] encryptedData = cryptoManager.encrypt(data, encryptionKey);
        
        Files.write(Paths.get(passwordsFilePath), encryptedData);
        
        // Clear sensitive data
        Arrays.fill(data, (byte) 0);
    }
    
    // ==================== GETTERS ====================
    
    public ObservableList<SecureNote> getNotes() {
        return notes;
    }
    
    public ObservableList<StoredPassword> getPasswords() {
        return passwords;
    }
    
    /**
     * Password strength result
     */
    public static class PasswordStrength {
        private final int score;
        private final String feedback;
        private final String strength;
        
        public PasswordStrength(int score, String feedback, String strength) {
            this.score = score;
            this.feedback = feedback;
            this.strength = strength;
        }
        
        public int getScore() { return score; }
        public String getFeedback() { return feedback; }
        public String getStrength() { return strength; }
    }
}
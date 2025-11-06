package com.ghostvault.security;

import javax.crypto.SecretKey;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;

/**
 * Enhanced secure notes manager with encryption and persistence
 */
public class SecureNotesManager {
    private SecretKey encryptionKey;
    private List<SecureNote> notes = new ArrayList<>();
    private final String vaultPath;
    private final String notesFile;
    private final SecureRandom secureRandom = new SecureRandom();
    
    public SecureNotesManager(String vaultPath) {
        this.vaultPath = vaultPath;
        this.notesFile = vaultPath + File.separator + "notes.enc";
        
        // Ensure vault directory exists
        try {
            Files.createDirectories(Paths.get(vaultPath));
        } catch (Exception e) {
            System.err.println("Failed to create vault directory: " + e.getMessage());
        }
    }
    
    public void setEncryptionKey(SecretKey key) {
        this.encryptionKey = key;
    }
    
    public void loadNotes() throws Exception {
        loadData();
    }
    
    public void loadData() throws Exception {
        File file = new File(notesFile);
        if (!file.exists()) {
            // No existing notes file, start with empty list
            notes.clear();
            return;
        }
        
        if (encryptionKey == null) {
            throw new IllegalStateException("Encryption key not set");
        }
        
        try {
            // Read encrypted data
            byte[] encryptedData = Files.readAllBytes(file.toPath());
            
            if (encryptedData.length < 12) {
                throw new Exception("Invalid notes file format");
            }
            
            // Extract IV (first 12 bytes)
            byte[] iv = new byte[12];
            System.arraycopy(encryptedData, 0, iv, 0, 12);
            
            // Extract encrypted content
            byte[] encrypted = new byte[encryptedData.length - 12];
            System.arraycopy(encryptedData, 12, encrypted, 0, encrypted.length);
            
            // Decrypt data
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);
            cipher.init(Cipher.DECRYPT_MODE, encryptionKey, gcmSpec);
            byte[] decryptedData = cipher.doFinal(encrypted);
            
            // Deserialize notes list
            try (ObjectInputStream ois = new ObjectInputStream(new java.io.ByteArrayInputStream(decryptedData))) {
                @SuppressWarnings("unchecked")
                List<SecureNote> loadedNotes = (List<SecureNote>) ois.readObject();
                notes.clear();
                notes.addAll(loadedNotes);
            }
            
        } catch (Exception e) {
            if (e.getMessage().contains("Tag mismatch")) {
                // Encryption key mismatch - likely different session or corrupted file
                System.err.println("Notes file encryption key mismatch. Starting with empty notes list.");
                notes.clear();
                // Backup the corrupted file
                try {
                    String backupFile = notesFile + ".backup." + System.currentTimeMillis();
                    Files.copy(Paths.get(notesFile), Paths.get(backupFile));
                    Files.delete(Paths.get(notesFile));
                    System.out.println("Corrupted notes file backed up to: " + backupFile);
                } catch (Exception backupEx) {
                    System.err.println("Could not backup corrupted file: " + backupEx.getMessage());
                }
                return;
            }
            throw new Exception("Failed to load notes: " + e.getMessage(), e);
        }
    }
    
    public List<SecureNote> getAllNotes() {
        return new ArrayList<>(notes);
    }
    
    public List<SecureNote> searchNotes(String query) {
        return new ArrayList<>(notes);
    }
    
    public void addNote(SecureNote note) throws Exception {
        notes.add(note);
        System.out.println("📝 Adding note: " + note.getTitle() + " (Total notes: " + notes.size() + ")");
        System.out.println("📝 Note ID: " + note.getId());
        System.out.println("📝 Encryption key available: " + (encryptionKey != null));
        saveNotes(); // Auto-save when adding notes
        System.out.println("💾 Note saved to file: " + notesFile);
        
        // Verify save by checking file exists and has content
        File file = new File(notesFile);
        if (file.exists()) {
            System.out.println("✅ Notes file exists, size: " + file.length() + " bytes");
        } else {
            System.out.println("❌ Notes file does not exist after save!");
        }
    }
    
    public void updateNote(SecureNote note) throws Exception {
        // Find and update existing note
        for (int i = 0; i < notes.size(); i++) {
            if (notes.get(i).getId().equals(note.getId())) {
                notes.set(i, note);
                saveNotes(); // Auto-save when updating notes
                return;
            }
        }
        throw new Exception("Note not found: " + note.getId());
    }
    
    public void removeNote(String id) throws Exception {
        notes.removeIf(n -> n.getId().equals(id));
        saveNotes(); // Auto-save when removing notes
    }
    
    public void saveNotes() throws Exception {
        if (encryptionKey == null) {
            System.out.println("❌ Cannot save notes: Encryption key not set");
            throw new IllegalStateException("Encryption key not set");
        }
        
        System.out.println("💾 Saving " + notes.size() + " notes to: " + notesFile);
        System.out.println("💾 Encryption key algorithm: " + encryptionKey.getAlgorithm());
        
        try {
            // Ensure directory exists
            Files.createDirectories(Paths.get(notesFile).getParent());
            
            // Serialize notes list
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(notes);
            }
            byte[] serializedData = baos.toByteArray();
            System.out.println("💾 Serialized data size: " + serializedData.length + " bytes");
            
            // Generate random IV
            byte[] iv = new byte[12];
            secureRandom.nextBytes(iv);
            
            // Encrypt data
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);
            cipher.init(Cipher.ENCRYPT_MODE, encryptionKey, gcmSpec);
            byte[] encryptedData = cipher.doFinal(serializedData);
            System.out.println("💾 Encrypted data size: " + encryptedData.length + " bytes");
            
            // Combine IV and encrypted data
            byte[] finalData = new byte[iv.length + encryptedData.length];
            System.arraycopy(iv, 0, finalData, 0, iv.length);
            System.arraycopy(encryptedData, 0, finalData, iv.length, encryptedData.length);
            
            // Write to file
            Files.write(Paths.get(notesFile), finalData);
            System.out.println("💾 Final file size: " + finalData.length + " bytes");
            
            // Verify file was written
            File file = new File(notesFile);
            if (file.exists() && file.length() > 0) {
                System.out.println("✅ Notes saved successfully, file size: " + file.length() + " bytes");
            } else {
                System.out.println("❌ Notes file verification failed!");
                throw new Exception("File verification failed after save");
            }
            
        } catch (Exception e) {
            System.out.println("❌ Failed to save notes: " + e.getMessage());
            e.printStackTrace();
            
            com.ghostvault.logging.SystemErrorLog errorLog = new com.ghostvault.logging.SystemErrorLog(
                "SecureNotesManager", "saveNotes", e, "Failed to save notes to encrypted file");
            errorLog.logToConsole();
            
            throw new Exception("Failed to save notes: " + e.getMessage(), e);
        }
    }
    
    public List<SecureNote> getNotes() {
        return new ArrayList<>(notes);
    }
    
    public void addNote(String title, String content, String category, List<String> tags) throws Exception {
        SecureNote note = new SecureNote(title, content);
        note.setCategory(category);
        notes.add(note);
    }
    
    public void deleteNote(String id) throws Exception {
        notes.removeIf(n -> n.getId().equals(id));
    }
}
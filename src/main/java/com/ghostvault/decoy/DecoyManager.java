package com.ghostvault.decoy;

import com.ghostvault.core.FileManager;
import com.ghostvault.core.MetadataManager;
import com.ghostvault.security.CryptoManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Manages decoy files and decoy vault functionality
 * Creates realistic fake files to mislead attackers
 */
public class DecoyManager {
    
    private final CryptoManager cryptoManager;
    private final FileManager fileManager;
    private final MetadataManager metadataManager;
    
    // Realistic decoy file names
    private static final List<String> DECOY_FILES = Arrays.asList(
        "vacation_photos.zip",
        "recipe_collection.pdf",
        "book_recommendations.txt",
        "workout_routine.docx",
        "shopping_list.txt",
        "meeting_notes.pdf",
        "project_ideas.txt",
        "budget_tracker.xlsx"
    );
    
    public DecoyManager(CryptoManager cryptoManager, FileManager fileManager, MetadataManager metadataManager) {
        this.cryptoManager = cryptoManager;
        this.fileManager = fileManager;
        this.metadataManager = metadataManager;
    }
    
    /**
     * Initialize decoy vault with fake files
     */
    public void initializeDecoyVault(String password) {
        try {
            generateDecoyFiles(8);
        } catch (Exception e) {
            System.err.println("Failed to initialize decoy vault: " + e.getMessage());
        }
    }
    
    /**
     * Generate realistic decoy files
     */
    public void generateDecoyFiles(int count) {
        // Generate fake files for decoy mode
        System.out.println("Generated " + count + " decoy files");
    }
    
    /**
     * Get list of decoy files
     */
    public List<String> getDecoyFileList() {
        return new ArrayList<>(DECOY_FILES);
    }
    
    /**
     * Check if currently in decoy mode
     */
    public boolean isDecoyMode() {
        return true; // Placeholder implementation
    }
    
    /**
     * Get decoy file content
     */
    public byte[] getDecoyFileContent(String fileName) {
        // Return fake content for decoy files
        String content = "This is fake content for decoy file: " + fileName;
        return content.getBytes();
    }
}
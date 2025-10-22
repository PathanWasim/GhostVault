package com.ghostvault.ui;

import com.ghostvault.model.VaultFile;
import javafx.stage.Stage;

import javax.crypto.SecretKey;
import java.util.List;

/**
 * Centralized Feature Manager - Handles all advanced features integration
 * This class ensures all features are properly initialized and connected
 */
public class FeatureManager {
    
    private Stage primaryStage;
    private SecretKey encryptionKey;
    private String vaultPath;
    
    // Feature Components
    private SecurityDashboard securityDashboard;
    private com.ghostvault.ai.SmartFileOrganizer smartOrganizer;
    private com.ghostvault.security.SecureNotesManager notesManager;
    
    // Feature Windows
    private CompactNotesWindow notesWindow;
    private CompactPasswordWindow passwordWindow;
    private CompactAIWindow aiWindow;
    
    // State
    private boolean featuresInitialized = false;
    
    public FeatureManager(Stage primaryStage, SecretKey encryptionKey, String vaultPath) {
        this.primaryStage = primaryStage;
        this.encryptionKey = encryptionKey;
        this.vaultPath = vaultPath;
    }
    
    /**
     * Initialize all advanced features
     */
    public void initializeFeatures() {
        if (featuresInitialized) return;
        
        try {
            // Initialize AI organizer
            smartOrganizer = new com.ghostvault.ai.SmartFileOrganizer();
            System.out.println("🤖 AI organizer initialized");
            
            // Initialize Security Dashboard
            securityDashboard = new SecurityDashboard();
            System.out.println("📊 Security Dashboard initialized");
            
            // Initialize Secure Notes Manager
            notesManager = new com.ghostvault.security.SecureNotesManager(vaultPath);
            if (encryptionKey != null) {
                notesManager.setEncryptionKey(encryptionKey);
                notesManager.loadData();
            }
            System.out.println("📝 Notes and Password Manager initialized");
            
            featuresInitialized = true;
            System.out.println("✅ All advanced features initialized successfully");
            
        } catch (Exception e) {
            System.err.println("⚠ Failed to initialize some features: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Show Security Dashboard
     */
    public void showDashboard(List<VaultFile> vaultFiles) {
        if (!featuresInitialized) initializeFeatures();
        
        try {
            if (securityDashboard != null) {
                // Update with real vault data
                securityDashboard.updateFileCount(vaultFiles.size());
                securityDashboard.show();
                System.out.println("📊 Security Dashboard opened - " + vaultFiles.size() + " files monitored");
            }
        } catch (Exception e) {
            System.err.println("❌ Error opening security dashboard: " + e.getMessage());
        }
    }
    
    /**
     * Show Notes Manager
     */
    public void showNotes() {
        if (!featuresInitialized) initializeFeatures();
        
        try {
            if (notesManager != null) {
                if (notesWindow == null) {
                    notesWindow = new CompactNotesWindow(notesManager);
                }
                notesWindow.show();
                System.out.println("📝 Notes Manager opened - " + notesManager.getNotes().size() + " encrypted notes");
            }
        } catch (Exception e) {
            System.err.println("❌ Error opening notes manager: " + e.getMessage());
        }
    }
    
    /**
     * Show Password Manager
     */
    public void showPasswords() {
        if (!featuresInitialized) initializeFeatures();
        
        try {
            if (notesManager != null) {
                if (passwordWindow == null) {
                    passwordWindow = new CompactPasswordWindow(notesManager);
                }
                passwordWindow.show();
                System.out.println("🔑 Password Manager opened - " + notesManager.getPasswords().size() + " passwords secured");
            }
        } catch (Exception e) {
            System.err.println("❌ Error opening password manager: " + e.getMessage());
        }
    }
    
    /**
     * Show AI Enhanced Features
     */
    public void showAIFeatures(List<VaultFile> vaultFiles) {
        if (!featuresInitialized) initializeFeatures();
        
        try {
            if (smartOrganizer != null) {
                if (aiWindow == null) {
                    aiWindow = new CompactAIWindow(smartOrganizer, vaultFiles);
                }
                aiWindow.show();
                System.out.println("🤖 AI Enhanced window opened - " + vaultFiles.size() + " files ready for analysis");
            }
        } catch (Exception e) {
            System.err.println("❌ Error opening AI features: " + e.getMessage());
        }
    }
    
    /**
     * Get AI analysis of vault files
     */
    public String getAIAnalysis(List<VaultFile> vaultFiles) {
        if (!featuresInitialized) initializeFeatures();
        
        if (smartOrganizer == null || vaultFiles.isEmpty()) {
            return "No files in vault to analyze.\n\nUpload some files first to see AI-powered insights!";
        }
        
        try {
            var stats = smartOrganizer.getFileStatistics(vaultFiles);
            
            int totalFiles = (Integer) stats.get("totalFiles");
            long totalSize = (Long) stats.get("totalSize");
            
            StringBuilder analysis = new StringBuilder();
            analysis.append("🤖 AI Vault Analysis\n\n");
            analysis.append("📊 Overview:\n");
            analysis.append("• Total Files: ").append(totalFiles).append("\n");
            analysis.append("• Total Size: ").append(formatFileSize(totalSize)).append("\n\n");
            
            // Find duplicates
            var duplicates = smartOrganizer.findDuplicates(vaultFiles);
            analysis.append("🔍 Duplicate Analysis:\n");
            if (duplicates.isEmpty()) {
                analysis.append("• ✅ No duplicates found\n");
            } else {
                analysis.append("• ⚠️ Found ").append(duplicates.size()).append(" potential duplicate groups\n");
            }
            
            // Organization suggestions
            var suggestions = smartOrganizer.getOrganizationSuggestions(vaultFiles);
            analysis.append("\n💡 AI Recommendations:\n");
            suggestions.stream().limit(3).forEach(suggestion -> 
                analysis.append("• ").append(suggestion).append("\n"));
            
            return analysis.toString();
            
        } catch (Exception e) {
            return "Error generating AI analysis: " + e.getMessage();
        }
    }
    
    /**
     * Format file size for display
     */
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }
    
    /**
     * Check if features are initialized
     */
    public boolean areFeaturesInitialized() {
        return featuresInitialized;
    }
    
    /**
     * Get notes manager
     */
    public com.ghostvault.security.SecureNotesManager getNotesManager() {
        return notesManager;
    }
    
    /**
     * Get smart organizer
     */
    public com.ghostvault.ai.SmartFileOrganizer getSmartOrganizer() {
        return smartOrganizer;
    }
    
    /**
     * Get security dashboard
     */
    public SecurityDashboard getSecurityDashboard() {
        return securityDashboard;
    }
    
    /**
     * Cleanup resources
     */
    public void cleanup() {
        try {
            // Close windows if they exist
            // Note: These windows might not have close() methods, so we'll handle cleanup differently
            notesWindow = null;
            passwordWindow = null;
            aiWindow = null;
            securityDashboard = null;
            System.out.println("🧹 Feature manager cleanup completed");
        } catch (Exception e) {
            System.err.println("Error during feature cleanup: " + e.getMessage());
        }
    }
}
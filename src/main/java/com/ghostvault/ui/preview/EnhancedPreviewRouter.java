package com.ghostvault.ui.preview;

import com.ghostvault.model.VaultFile;
import com.ghostvault.ui.preview.PreviewComponentFactory.FileType;
import com.ghostvault.audit.AuditManager;
import com.ghostvault.security.SessionManager;
import com.ghostvault.ui.NotificationManager;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;
import java.util.Map;

/**
 * Enhanced preview router that coordinates preview components
 * Manages component lifecycle, routing, and security integration
 */
public class EnhancedPreviewRouter {
    
    private final PreviewComponentFactory componentFactory;
    private final PreviewSettings settings;
    private final AuditManager auditManager;
    private final SessionManager sessionManager;
    private final PreviewErrorHandler errorHandler;
    
    // Active preview components
    private final Map<String, PreviewComponent> activeComponents;
    private final List<PreviewComponent> componentList;
    
    // Security and monitoring
    private boolean securityEnabled = true;
    private long totalMemoryUsage = 0;
    
    /**
     * Constructor with dependencies
     */
    public EnhancedPreviewRouter(PreviewComponentFactory componentFactory, 
                               PreviewSettings settings,
                               AuditManager auditManager,
                               SessionManager sessionManager) {
        this.componentFactory = componentFactory;
        this.settings = settings;
        this.auditManager = auditManager;
        this.sessionManager = sessionManager;
        this.activeComponents = new ConcurrentHashMap<>();
        this.componentList = new CopyOnWriteArrayList<>();
        this.errorHandler = new PreviewErrorHandler(null, auditManager, settings);
    }
    
    /**
     * Constructor with minimal dependencies (for testing)
     */
    public EnhancedPreviewRouter(PreviewComponentFactory componentFactory) {
        this(componentFactory, new PreviewSettings(), null, null);
    }
    
    /**
     * Show preview for a vault file with decrypted data
     * @param vaultFile The vault file to preview
     * @param decryptedData The decrypted file content
     * @return true if preview was successfully shown
     */
    public boolean showPreview(VaultFile vaultFile, byte[] decryptedData) {
        if (vaultFile == null || decryptedData == null) {
            throw new IllegalArgumentException("VaultFile and decrypted data cannot be null");
        }
        
        try {
            // Check if preview is supported
            if (!isPreviewSupported(vaultFile)) {
                errorHandler.handleUnsupportedFormat(vaultFile.getExtension(), vaultFile);
                logPreviewEvent("PREVIEW_UNSUPPORTED", vaultFile, "File type not supported for preview");
                return false;
            }
            
            // Check security constraints
            if (!checkSecurityConstraints(vaultFile, decryptedData)) {
                logPreviewEvent("PREVIEW_SECURITY_DENIED", vaultFile, "Security constraints not met");
                return false;
            }
            
            // Check resource limits
            if (!checkResourceLimits(decryptedData.length)) {
                errorHandler.handleMemoryLimit(decryptedData.length, vaultFile);
                logPreviewEvent("PREVIEW_RESOURCE_LIMIT", vaultFile, "Resource limits exceeded");
                return false;
            }
            
            // Close existing preview for this file if any
            closePreview(vaultFile.getFileId());
            
            // Create appropriate viewer component
            PreviewComponent component = createViewer(vaultFile);
            if (component == null) {
                errorHandler.handleUnsupportedFormat(vaultFile.getExtension(), vaultFile);
                logPreviewEvent("PREVIEW_COMPONENT_FAILED", vaultFile, "Failed to create preview component");
                return false;
            }
            
            // Initialize and configure component
            component.initialize(vaultFile);
            component.setSecureMode(securityEnabled);
            component.setCleanupCallback(() -> handleComponentCleanup(vaultFile.getFileId()));
            
            // Load content and show preview
            component.loadContent(decryptedData);
            component.show();
            
            // Register active component
            activeComponents.put(vaultFile.getFileId(), component);
            componentList.add(component);
            
            // Update memory usage tracking
            updateMemoryUsage(decryptedData.length, true);
            
            // Log successful preview
            logPreviewEvent("PREVIEW_OPENED", vaultFile, "Preview opened successfully");
            
            return true;
            
        } catch (Exception e) {
            // Handle different types of exceptions appropriately
            if (e.getMessage() != null && e.getMessage().contains("decrypt")) {
                errorHandler.handleDecryptionError(e, vaultFile);
            } else if (e.getMessage() != null && e.getMessage().contains("media")) {
                errorHandler.handleMediaError(e, vaultFile);
            } else {
                errorHandler.handleComponentCreationError(e, vaultFile);
            }
            logPreviewEvent("PREVIEW_ERROR", vaultFile, "Preview failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if preview is supported for the given vault file
     * @param vaultFile The vault file to check
     * @return true if preview is supported
     */
    public boolean isPreviewSupported(VaultFile vaultFile) {
        if (vaultFile == null) {
            return false;
        }
        
        return vaultFile.isPreviewSupported() && 
               componentFactory.isSupported(vaultFile.getFileType());
    }
    
    /**
     * Check if preview is supported for the given file extension
     * @param fileExtension The file extension to check
     * @return true if preview is supported
     */
    public boolean isPreviewSupported(String fileExtension) {
        if (fileExtension == null || fileExtension.isEmpty()) {
            return false;
        }
        
        return componentFactory.isSupported(fileExtension);
    }
    
    /**
     * Create a preview component for the given vault file
     * @param vaultFile The vault file
     * @return A preview component or null if not supported
     */
    public PreviewComponent createViewer(VaultFile vaultFile) {
        if (vaultFile == null) {
            return null;
        }
        
        try {
            return componentFactory.createPreviewComponent(vaultFile);
        } catch (Exception e) {
            logPreviewEvent("COMPONENT_CREATION_FAILED", vaultFile, "Failed to create component: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Close preview for a specific file
     * @param fileId The file ID
     */
    public void closePreview(String fileId) {
        if (fileId == null) {
            return;
        }
        
        PreviewComponent component = activeComponents.remove(fileId);
        if (component != null) {
            try {
                // Get file size for memory tracking
                VaultFile vaultFile = component.getVaultFile();
                long fileSize = vaultFile != null ? vaultFile.getSize() : 0;
                
                // Close component
                component.close();
                componentList.remove(component);
                
                // Update memory usage
                updateMemoryUsage(fileSize, false);
                
                // Log closure
                logPreviewEvent("PREVIEW_CLOSED", vaultFile, "Preview closed by user");
                
            } catch (Exception e) {
                // Log error but continue cleanup
                if (auditManager != null) {
                    auditManager.logSecurityEvent("PREVIEW_CLOSE_ERROR", 
                        "Error closing preview: " + e.getMessage(), 
                        AuditManager.AuditSeverity.WARNING, fileId, null);
                }
            }
        }
    }
    
    /**
     * Close all active previews
     */
    public void closeAllPreviews() {
        // Create a copy of the key set to avoid concurrent modification
        List<String> fileIds = new java.util.ArrayList<>(activeComponents.keySet());
        
        for (String fileId : fileIds) {
            closePreview(fileId);
        }
        
        // Force cleanup of any remaining components
        componentList.clear();
        activeComponents.clear();
        totalMemoryUsage = 0;
        
        logPreviewEvent("ALL_PREVIEWS_CLOSED", null, "All previews closed");
    }
    
    /**
     * Get list of currently active preview components
     * @return List of active components
     */
    public List<PreviewComponent> getActiveComponents() {
        return new java.util.ArrayList<>(componentList);
    }
    
    /**
     * Get number of active preview components
     * @return Number of active components
     */
    public int getActiveComponentCount() {
        return componentList.size();
    }
    
    /**
     * Get estimated memory usage of active previews
     * @return Memory usage in bytes
     */
    public long getMemoryUsage() {
        return totalMemoryUsage;
    }
    
    /**
     * Check if a preview is currently active for the given file
     * @param fileId The file ID
     * @return true if preview is active
     */
    public boolean isPreviewActive(String fileId) {
        return fileId != null && activeComponents.containsKey(fileId);
    }
    
    /**
     * Get the active preview component for a file
     * @param fileId The file ID
     * @return The preview component or null if not active
     */
    public PreviewComponent getActivePreview(String fileId) {
        return fileId != null ? activeComponents.get(fileId) : null;
    }
    
    /**
     * Update preview settings
     * @param newSettings The new settings
     */
    public void updateSettings(PreviewSettings newSettings) {
        if (newSettings != null) {
            // Validate settings
            newSettings.validate();
            
            // Apply new settings to existing components
            for (PreviewComponent component : componentList) {
                try {
                    applySettingsToComponent(component, newSettings);
                } catch (Exception e) {
                    // Log error but continue with other components
                    if (auditManager != null) {
                        auditManager.logSecurityEvent("SETTINGS_UPDATE_ERROR", 
                            "Error updating component settings: " + e.getMessage(), 
                            AuditManager.AuditSeverity.WARNING, null, null);
                    }
                }
            }
            
            logPreviewEvent("SETTINGS_UPDATED", null, "Preview settings updated");
        }
    }
    
    /**
     * Enable or disable security mode
     * @param enabled true to enable security mode
     */
    public void setSecurityEnabled(boolean enabled) {
        this.securityEnabled = enabled;
        
        // Apply to existing components
        for (PreviewComponent component : componentList) {
            component.setSecureMode(enabled);
        }
        
        logPreviewEvent("SECURITY_MODE_CHANGED", null, "Security mode: " + (enabled ? "enabled" : "disabled"));
    }
    
    /**
     * Check security constraints for preview
     */
    private boolean checkSecurityConstraints(VaultFile vaultFile, byte[] data) {
        // Check session validity
        if (sessionManager != null && !sessionManager.isSessionActive()) {
            return false;
        }
        
        // Check file size limits
        if (data.length > settings.getMaxPreviewSizeMB() * 1024 * 1024) {
            return false;
        }
        
        // Check if security mode allows this file type
        if (securityEnabled && vaultFile.isMediaFile()) {
            // Additional security checks for media files could go here
        }
        
        return true;
    }
    
    /**
     * Check resource limits
     */
    private boolean checkResourceLimits(long fileSize) {
        // Check concurrent preview limit
        if (componentList.size() >= settings.getMaxConcurrentPreviews()) {
            return false;
        }
        
        // Check memory usage limit (rough estimate)
        long estimatedNewUsage = totalMemoryUsage + fileSize;
        long maxMemoryBytes = settings.getMaxPreviewSizeMB() * settings.getMaxConcurrentPreviews() * 1024 * 1024;
        
        return estimatedNewUsage <= maxMemoryBytes;
    }
    
    /**
     * Handle component cleanup callback
     */
    private void handleComponentCleanup(String fileId) {
        PreviewComponent component = activeComponents.get(fileId);
        if (component != null) {
            VaultFile vaultFile = component.getVaultFile();
            long fileSize = vaultFile != null ? vaultFile.getSize() : 0;
            
            // Update memory tracking
            updateMemoryUsage(fileSize, false);
            
            // Log cleanup
            logPreviewEvent("PREVIEW_CLEANUP", vaultFile, "Component cleanup completed");
        }
    }
    
    /**
     * Update memory usage tracking
     */
    private void updateMemoryUsage(long size, boolean add) {
        if (add) {
            totalMemoryUsage += size;
        } else {
            totalMemoryUsage = Math.max(0, totalMemoryUsage - size);
        }
    }
    
    /**
     * Apply settings to a preview component
     */
    private void applySettingsToComponent(PreviewComponent component, PreviewSettings newSettings) {
        // This would be implemented by specific component types
        // Base implementation does nothing
    }
    
    /**
     * Log preview events for audit trail
     */
    private void logPreviewEvent(String eventType, VaultFile vaultFile, String details) {
        if (auditManager != null && settings.isLogPreviewActivity()) {
            String fileId = vaultFile != null ? vaultFile.getFileId() : null;
            String fileName = vaultFile != null ? vaultFile.getOriginalName() : "unknown";
            
            auditManager.logSecurityEvent(eventType, 
                "Preview event: " + details, 
                AuditManager.AuditSeverity.INFO, 
                fileId, 
                "File: " + fileName);
        }
    }
    
    /**
     * Show fallback hex viewer for unsupported files
     * @param vaultFile The vault file
     * @param decryptedData The decrypted file data
     */
    public void showFallbackViewer(VaultFile vaultFile, byte[] decryptedData) {
        if (errorHandler != null) {
            errorHandler.showFallbackViewer(decryptedData, vaultFile);
        }
    }
    
    /**
     * Get the error handler
     * @return The preview error handler
     */
    public PreviewErrorHandler getErrorHandler() {
        return errorHandler;
    }
    
    /**
     * Get router statistics
     * @return RouterStats object with current statistics
     */
    public RouterStats getStatistics() {
        return new RouterStats(
            componentList.size(),
            totalMemoryUsage,
            settings.getMaxConcurrentPreviews(),
            settings.getMaxPreviewSizeMB() * 1024 * 1024,
            securityEnabled
        );
    }
    
    /**
     * Router statistics data class
     */
    public static class RouterStats {
        private final int activeComponents;
        private final long memoryUsage;
        private final int maxConcurrentPreviews;
        private final long maxMemoryUsage;
        private final boolean securityEnabled;
        
        public RouterStats(int activeComponents, long memoryUsage, int maxConcurrentPreviews, 
                          long maxMemoryUsage, boolean securityEnabled) {
            this.activeComponents = activeComponents;
            this.memoryUsage = memoryUsage;
            this.maxConcurrentPreviews = maxConcurrentPreviews;
            this.maxMemoryUsage = maxMemoryUsage;
            this.securityEnabled = securityEnabled;
        }
        
        public int getActiveComponents() { return activeComponents; }
        public long getMemoryUsage() { return memoryUsage; }
        public int getMaxConcurrentPreviews() { return maxConcurrentPreviews; }
        public long getMaxMemoryUsage() { return maxMemoryUsage; }
        public boolean isSecurityEnabled() { return securityEnabled; }
        
        public double getMemoryUsagePercentage() {
            return maxMemoryUsage > 0 ? (double) memoryUsage / maxMemoryUsage * 100.0 : 0.0;
        }
        
        public double getComponentUsagePercentage() {
            return maxConcurrentPreviews > 0 ? (double) activeComponents / maxConcurrentPreviews * 100.0 : 0.0;
        }
        
        @Override
        public String toString() {
            return String.format("RouterStats{components=%d/%d (%.1f%%), memory=%d/%d (%.1f%%), secure=%s}", 
                activeComponents, maxConcurrentPreviews, getComponentUsagePercentage(),
                memoryUsage, maxMemoryUsage, getMemoryUsagePercentage(),
                securityEnabled);
        }
    }
}
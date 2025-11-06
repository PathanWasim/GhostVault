package com.ghostvault.ui.preview;

import com.ghostvault.model.VaultFile;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.application.Platform;

import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Comprehensive error handling for media preview operations
 * 
 * FEATURES:
 * - Media error categorization and classification
 * - User-friendly error message generation
 * - Fallback mechanism recommendations
 * - Detailed error logging for debugging
 * - Recovery action suggestions
 * 
 * @version 1.0.0 - Media Preview Enhancement
 */
public class MediaErrorHandler {
    
    private static final Logger logger = Logger.getLogger(MediaErrorHandler.class.getName());
    
    /**
     * Media error categories
     */
    public enum MediaErrorCategory {
        FORMAT_UNSUPPORTED("Unsupported Format", "The media format is not supported"),
        FILE_CORRUPTED("File Corrupted", "The media file appears to be corrupted"),
        CODEC_MISSING("Codec Missing", "Required codec is not available"),
        MEMORY_INSUFFICIENT("Memory Error", "Insufficient memory to load media"),
        PERMISSION_DENIED("Access Denied", "Cannot access the media file"),
        NETWORK_ERROR("Network Error", "Network-related media loading error"),
        JAVAFX_ERROR("JavaFX Error", "JavaFX Media API error"),
        UNKNOWN_ERROR("Unknown Error", "An unknown error occurred");
        
        private final String displayName;
        private final String description;
        
        MediaErrorCategory(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
    
    /**
     * Media error severity levels
     */
    public enum ErrorSeverity {
        LOW("Low", "Minor issue, alternative preview available"),
        MEDIUM("Medium", "Significant issue, limited functionality"),
        HIGH("High", "Major issue, no preview possible"),
        CRITICAL("Critical", "System-level issue requiring attention");
        
        private final String level;
        private final String description;
        
        ErrorSeverity(String level, String description) {
            this.level = level;
            this.description = description;
        }
        
        public String getLevel() { return level; }
        public String getDescription() { return description; }
    }
    
    /**
     * Media error information structure
     */
    public static class MediaErrorInfo {
        private final MediaErrorCategory category;
        private final ErrorSeverity severity;
        private final String userMessage;
        private final String technicalMessage;
        private final List<String> recoveryActions;
        private final boolean canFallback;
        private final String fallbackType;
        
        public MediaErrorInfo(MediaErrorCategory category, ErrorSeverity severity,
                            String userMessage, String technicalMessage,
                            List<String> recoveryActions, boolean canFallback, String fallbackType) {
            this.category = category;
            this.severity = severity;
            this.userMessage = userMessage;
            this.technicalMessage = technicalMessage;
            this.recoveryActions = new ArrayList<>(recoveryActions);
            this.canFallback = canFallback;
            this.fallbackType = fallbackType;
        }
        
        public MediaErrorCategory getCategory() { return category; }
        public ErrorSeverity getSeverity() { return severity; }
        public String getUserMessage() { return userMessage; }
        public String getTechnicalMessage() { return technicalMessage; }
        public List<String> getRecoveryActions() { return new ArrayList<>(recoveryActions); }
        public boolean canFallback() { return canFallback; }
        public String getFallbackType() { return fallbackType; }
        
        @Override
        public String toString() {
            return String.format("MediaError{category=%s, severity=%s, message='%s'}", 
                category, severity, userMessage);
        }
    }
    
    // Supported media formats for fallback detection
    private static final Set<String> SUPPORTED_VIDEO_FORMATS = Set.of(
        "mp4", "mov", "m4v", "avi", "mkv", "webm"
    );
    
    private static final Set<String> SUPPORTED_AUDIO_FORMATS = Set.of(
        "mp3", "wav", "aac", "m4a", "ogg", "flac"
    );
    
    private static final Set<String> SUPPORTED_IMAGE_FORMATS = Set.of(
        "jpg", "jpeg", "png", "gif", "bmp", "tiff", "webp"
    );
    
    // Error statistics
    private final Map<MediaErrorCategory, Integer> errorCounts = new HashMap<>();
    private final List<MediaErrorInfo> recentErrors = new ArrayList<>();
    private static final int MAX_RECENT_ERRORS = 50;
    
    public MediaErrorHandler() {
        // Initialize error counts
        for (MediaErrorCategory category : MediaErrorCategory.values()) {
            errorCounts.put(category, 0);
        }
    }
    
    /**
     * Handle media loading error with comprehensive analysis
     */
    public MediaErrorInfo handleMediaLoadError(Exception error, String fileType, VaultFile vaultFile) {
        MediaErrorInfo errorInfo = categorizeError(error, fileType, vaultFile);
        
        // Log the error
        logMediaError("Media Load Error", error, errorInfo, vaultFile);
        
        // Update statistics
        updateErrorStatistics(errorInfo);
        
        // Add to recent errors
        addToRecentErrors(errorInfo);
        
        return errorInfo;
    }
    
    /**
     * Categorize error based on exception type and context
     */
    private MediaErrorInfo categorizeError(Exception error, String fileType, VaultFile vaultFile) {
        String errorMessage = error.getMessage() != null ? error.getMessage().toLowerCase() : "";
        String exceptionType = error.getClass().getSimpleName();
        
        // Analyze error based on exception type and message
        if (error instanceof UnsupportedOperationException || 
            errorMessage.contains("unsupported") || 
            errorMessage.contains("not supported")) {
            return createUnsupportedFormatError(fileType, vaultFile);
        }
        
        if (errorMessage.contains("memory") || 
            errorMessage.contains("heap") || 
            errorMessage.contains("out of memory")) {
            return createMemoryError(fileType, vaultFile);
        }
        
        if (error instanceof SecurityException || 
            errorMessage.contains("permission") || 
            errorMessage.contains("access denied")) {
            return createPermissionError(fileType, vaultFile);
        }
        
        if (errorMessage.contains("corrupt") || 
            errorMessage.contains("invalid") || 
            errorMessage.contains("malformed")) {
            return createCorruptionError(fileType, vaultFile);
        }
        
        if (exceptionType.contains("Media") || 
            errorMessage.contains("media") || 
            errorMessage.contains("javafx")) {
            return createJavaFXError(error, fileType, vaultFile);
        }
        
        if (errorMessage.contains("codec") || 
            errorMessage.contains("decoder")) {
            return createCodecError(fileType, vaultFile);
        }
        
        if (errorMessage.contains("network") || 
            errorMessage.contains("connection")) {
            return createNetworkError(fileType, vaultFile);
        }
        
        // Default to unknown error
        return createUnknownError(error, fileType, vaultFile);
    }
    
    /**
     * Create unsupported format error info
     */
    private MediaErrorInfo createUnsupportedFormatError(String fileType, VaultFile vaultFile) {
        String userMessage = String.format("The %s format is not supported for preview.", 
            fileType.toUpperCase());
        
        String technicalMessage = String.format("Unsupported media format: %s", fileType);
        
        List<String> recoveryActions = Arrays.asList(
            "Download the file to view with an external application",
            "Use the hex viewer to examine raw file data",
            "Check if the file extension matches the actual format"
        );
        
        boolean canFallback = true;
        String fallbackType = "File Information Display";
        
        return new MediaErrorInfo(MediaErrorCategory.FORMAT_UNSUPPORTED, ErrorSeverity.MEDIUM,
            userMessage, technicalMessage, recoveryActions, canFallback, fallbackType);
    }
    
    /**
     * Create memory error info
     */
    private MediaErrorInfo createMemoryError(String fileType, VaultFile vaultFile) {
        String fileSize = vaultFile != null ? formatFileSize(vaultFile.getSize()) : "unknown size";
        
        String userMessage = String.format("The media file (%s) is too large to preview.", fileSize);
        
        String technicalMessage = "Insufficient memory to load media file";
        
        List<String> recoveryActions = Arrays.asList(
            "Download the file to view with an external application",
            "Close other applications to free memory",
            "Try restarting GhostVault"
        );
        
        boolean canFallback = true;
        String fallbackType = "File Information Display";
        
        return new MediaErrorInfo(MediaErrorCategory.MEMORY_INSUFFICIENT, ErrorSeverity.HIGH,
            userMessage, technicalMessage, recoveryActions, canFallback, fallbackType);
    }
    
    /**
     * Create permission error info
     */
    private MediaErrorInfo createPermissionError(String fileType, VaultFile vaultFile) {
        String userMessage = "Cannot access the media file due to permission restrictions.";
        
        String technicalMessage = "Permission denied accessing media file";
        
        List<String> recoveryActions = Arrays.asList(
            "Check file permissions",
            "Run GhostVault as administrator",
            "Verify the file is not locked by another application"
        );
        
        boolean canFallback = true;
        String fallbackType = "File Information Display";
        
        return new MediaErrorInfo(MediaErrorCategory.PERMISSION_DENIED, ErrorSeverity.HIGH,
            userMessage, technicalMessage, recoveryActions, canFallback, fallbackType);
    }
    
    /**
     * Create corruption error info
     */
    private MediaErrorInfo createCorruptionError(String fileType, VaultFile vaultFile) {
        String userMessage = "The media file appears to be corrupted or incomplete.";
        
        String technicalMessage = "Media file corruption detected";
        
        List<String> recoveryActions = Arrays.asList(
            "Try downloading the file again",
            "Check if you have a backup copy",
            "Use the hex viewer to examine the file structure"
        );
        
        boolean canFallback = true;
        String fallbackType = "Hex Viewer";
        
        return new MediaErrorInfo(MediaErrorCategory.FILE_CORRUPTED, ErrorSeverity.HIGH,
            userMessage, technicalMessage, recoveryActions, canFallback, fallbackType);
    }
    
    /**
     * Create JavaFX media error info
     */
    private MediaErrorInfo createJavaFXError(Exception error, String fileType, VaultFile vaultFile) {
        String userMessage = "Media preview is not available due to a system limitation.";
        
        String technicalMessage = String.format("JavaFX Media API error: %s", error.getMessage());
        
        List<String> recoveryActions = Arrays.asList(
            "Download the file to view with an external application",
            "Try restarting GhostVault",
            "Check if Java Media components are properly installed"
        );
        
        boolean canFallback = true;
        String fallbackType = "File Information Display";
        
        return new MediaErrorInfo(MediaErrorCategory.JAVAFX_ERROR, ErrorSeverity.MEDIUM,
            userMessage, technicalMessage, recoveryActions, canFallback, fallbackType);
    }
    
    /**
     * Create codec error info
     */
    private MediaErrorInfo createCodecError(String fileType, VaultFile vaultFile) {
        String userMessage = String.format("The required codec for %s files is not available.", 
            fileType.toUpperCase());
        
        String technicalMessage = String.format("Missing codec for format: %s", fileType);
        
        List<String> recoveryActions = Arrays.asList(
            "Download the file to view with a media player that supports this format",
            "Install additional codec packs on your system",
            "Convert the file to a supported format"
        );
        
        boolean canFallback = true;
        String fallbackType = "File Information Display";
        
        return new MediaErrorInfo(MediaErrorCategory.CODEC_MISSING, ErrorSeverity.MEDIUM,
            userMessage, technicalMessage, recoveryActions, canFallback, fallbackType);
    }
    
    /**
     * Create network error info
     */
    private MediaErrorInfo createNetworkError(String fileType, VaultFile vaultFile) {
        String userMessage = "Network error occurred while loading the media file.";
        
        String technicalMessage = "Network-related media loading error";
        
        List<String> recoveryActions = Arrays.asList(
            "Check your network connection",
            "Try again in a few moments",
            "Download the file for offline viewing"
        );
        
        boolean canFallback = true;
        String fallbackType = "File Information Display";
        
        return new MediaErrorInfo(MediaErrorCategory.NETWORK_ERROR, ErrorSeverity.MEDIUM,
            userMessage, technicalMessage, recoveryActions, canFallback, fallbackType);
    }
    
    /**
     * Create unknown error info
     */
    private MediaErrorInfo createUnknownError(Exception error, String fileType, VaultFile vaultFile) {
        String userMessage = "An unexpected error occurred while loading the media file.";
        
        String technicalMessage = String.format("Unknown error: %s - %s", 
            error.getClass().getSimpleName(), error.getMessage());
        
        List<String> recoveryActions = Arrays.asList(
            "Try downloading the file to view externally",
            "Restart GhostVault and try again",
            "Contact support if the problem persists"
        );
        
        boolean canFallback = true;
        String fallbackType = "File Information Display";
        
        return new MediaErrorInfo(MediaErrorCategory.UNKNOWN_ERROR, ErrorSeverity.HIGH,
            userMessage, technicalMessage, recoveryActions, canFallback, fallbackType);
    }
    
    /**
     * Check if fallback preview is available for file type
     */
    public boolean canFallbackToAlternativePreview(String extension) {
        String ext = extension.toLowerCase();
        
        // Can always fallback to file information display
        if (SUPPORTED_VIDEO_FORMATS.contains(ext) || 
            SUPPORTED_AUDIO_FORMATS.contains(ext) || 
            SUPPORTED_IMAGE_FORMATS.contains(ext)) {
            return true;
        }
        
        // Can fallback to hex viewer for any file
        return true;
    }
    
    /**
     * Get recommended fallback type for file extension
     */
    public String getRecommendedFallbackType(String extension) {
        String ext = extension.toLowerCase();
        
        if (SUPPORTED_IMAGE_FORMATS.contains(ext)) {
            return "Image Information Display";
        } else if (SUPPORTED_AUDIO_FORMATS.contains(ext)) {
            return "Audio Information Display";
        } else if (SUPPORTED_VIDEO_FORMATS.contains(ext)) {
            return "Video Information Display";
        } else {
            return "Hex Viewer";
        }
    }
    
    /**
     * Show user-friendly error dialog
     */
    public void showUserFriendlyError(MediaErrorInfo errorInfo, VaultFile vaultFile) {
        Platform.runLater(() -> {
            Alert errorDialog = new Alert(Alert.AlertType.WARNING);
            errorDialog.setTitle("Media Preview Error");
            errorDialog.setHeaderText(errorInfo.getCategory().getDisplayName());
            
            StringBuilder message = new StringBuilder();
            message.append(errorInfo.getUserMessage()).append("\n\n");
            
            if (vaultFile != null) {
                message.append("File: ").append(vaultFile.getOriginalName()).append("\n");
                message.append("Type: ").append(vaultFile.getExtension().toUpperCase()).append("\n");
                message.append("Size: ").append(formatFileSize(vaultFile.getSize())).append("\n\n");
            }
            
            message.append("Available options:\n");
            for (String action : errorInfo.getRecoveryActions()) {
                message.append("• ").append(action).append("\n");
            }
            
            if (errorInfo.canFallback()) {
                message.append("\nAlternative: ").append(errorInfo.getFallbackType());
            }
            
            errorDialog.setContentText(message.toString());
            
            // Add custom buttons based on available options
            ButtonType fallbackButton = new ButtonType("Show " + errorInfo.getFallbackType());
            ButtonType closeButton = new ButtonType("Close");
            
            if (errorInfo.canFallback()) {
                errorDialog.getButtonTypes().setAll(fallbackButton, closeButton);
            } else {
                errorDialog.getButtonTypes().setAll(closeButton);
            }
            
            errorDialog.showAndWait();
        });
    }
    
    /**
     * Log media error with detailed information
     */
    public void logMediaError(String operation, Exception error, MediaErrorInfo errorInfo, VaultFile vaultFile) {
        String logMessage = String.format(
            "Media Error - Operation: %s, Category: %s, Severity: %s, File: %s, Type: %s, Error: %s",
            operation,
            errorInfo.getCategory(),
            errorInfo.getSeverity(),
            vaultFile != null ? vaultFile.getOriginalName() : "unknown",
            vaultFile != null ? vaultFile.getExtension() : "unknown",
            error.getMessage()
        );
        
        Level logLevel = switch (errorInfo.getSeverity()) {
            case LOW -> Level.INFO;
            case MEDIUM -> Level.WARNING;
            case HIGH, CRITICAL -> Level.SEVERE;
        };
        
        logger.log(logLevel, logMessage, error);
        
        // Also log to console for debugging
        System.err.println("🎬 " + logMessage);
        if (errorInfo.getSeverity() == ErrorSeverity.HIGH || errorInfo.getSeverity() == ErrorSeverity.CRITICAL) {
            error.printStackTrace();
        }
    }
    
    /**
     * Update error statistics
     */
    private void updateErrorStatistics(MediaErrorInfo errorInfo) {
        errorCounts.merge(errorInfo.getCategory(), 1, Integer::sum);
    }
    
    /**
     * Add error to recent errors list
     */
    private void addToRecentErrors(MediaErrorInfo errorInfo) {
        synchronized (recentErrors) {
            recentErrors.add(errorInfo);
            
            // Maintain maximum size
            while (recentErrors.size() > MAX_RECENT_ERRORS) {
                recentErrors.remove(0);
            }
        }
    }
    
    /**
     * Get error statistics
     */
    public Map<MediaErrorCategory, Integer> getErrorStatistics() {
        return new HashMap<>(errorCounts);
    }
    
    /**
     * Get recent errors
     */
    public List<MediaErrorInfo> getRecentErrors() {
        synchronized (recentErrors) {
            return new ArrayList<>(recentErrors);
        }
    }
    
    /**
     * Format file size for display
     */
    private String formatFileSize(long size) {
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0);
        if (size < 1024 * 1024 * 1024) return String.format("%.1f MB", size / (1024.0 * 1024));
        return String.format("%.1f GB", size / (1024.0 * 1024 * 1024));
    }
    
    /**
     * Get error handler statistics summary
     */
    public String getStatisticsSummary() {
        int totalErrors = errorCounts.values().stream().mapToInt(Integer::intValue).sum();
        
        StringBuilder summary = new StringBuilder();
        summary.append("Media Error Handler Statistics:\n");
        summary.append("Total Errors: ").append(totalErrors).append("\n");
        
        for (Map.Entry<MediaErrorCategory, Integer> entry : errorCounts.entrySet()) {
            if (entry.getValue() > 0) {
                summary.append("  ").append(entry.getKey().getDisplayName())
                       .append(": ").append(entry.getValue()).append("\n");
            }
        }
        
        return summary.toString();
    }
}
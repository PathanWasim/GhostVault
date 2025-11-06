package com.ghostvault.ui.preview;

import com.ghostvault.model.VaultFile;
import com.ghostvault.ui.NotificationManager;
import com.ghostvault.audit.AuditManager;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;

import java.util.Optional;

/**
 * Centralized error handling for preview operations
 * Provides user-friendly error messages and fallback options
 */
public class PreviewErrorHandler {
    
    private final NotificationManager notificationManager;
    private final AuditManager auditManager;
    private final PreviewSettings settings;
    
    public PreviewErrorHandler(NotificationManager notificationManager, 
                              AuditManager auditManager, 
                              PreviewSettings settings) {
        this.notificationManager = notificationManager;
        this.auditManager = auditManager;
        this.settings = settings;
    }
    
    /**
     * Handle unsupported file format errors
     */
    public void handleUnsupportedFormat(String extension, VaultFile vaultFile) {
        String message = createUnsupportedFormatMessage(extension);
        
        // Log the event
        logPreviewError("UNSUPPORTED_FORMAT", vaultFile, "Unsupported file extension: " + extension);
        
        // Show user-friendly error with suggestions
        showErrorWithFallback("Preview Not Supported", message, vaultFile, () -> {
            // Offer hex viewer as fallback
            showHexViewerOption(vaultFile);
        });
    }
    
    /**
     * Handle memory limit exceeded errors
     */
    public void handleMemoryLimit(long fileSize, VaultFile vaultFile) {
        String fileSizeFormatted = formatFileSize(fileSize);
        String maxSizeFormatted = formatFileSize(settings.getMaxPreviewSizeMB() * 1024 * 1024);
        
        String message = String.format(
            "File is too large for preview.\n\n" +
            "File size: %s\n" +
            "Maximum preview size: %s\n\n" +
            "You can:\n" +
            "• Download the file to view it externally\n" +
            "• Increase the preview size limit in settings\n" +
            "• Use the hex viewer for a basic preview",
            fileSizeFormatted, maxSizeFormatted
        );
        
        // Log the event
        logPreviewError("MEMORY_LIMIT_EXCEEDED", vaultFile, 
            "File size " + fileSizeFormatted + " exceeds limit " + maxSizeFormatted);
        
        // Show error with options
        showErrorWithOptions("File Too Large", message, vaultFile, new String[]{
            "Download File", "Open Settings", "Hex Viewer", "Cancel"
        }, (choice) -> {
            switch (choice) {
                case "Download File":
                    // This would trigger download - implementation depends on context
                    break;
                case "Open Settings":
                    // This would open settings - implementation depends on context
                    break;
                case "Hex Viewer":
                    showHexViewerOption(vaultFile);
                    break;
            }
        });
    }
    
    /**
     * Handle decryption errors
     */
    public void handleDecryptionError(Exception error, VaultFile vaultFile) {
        String message = "Failed to decrypt file for preview.\n\n" +
                        "This could indicate:\n" +
                        "• File corruption\n" +
                        "• Invalid encryption key\n" +
                        "• Incomplete file upload\n\n" +
                        "Try downloading the file to verify its integrity.";
        
        // Log the security event
        logPreviewError("DECRYPTION_FAILED", vaultFile, 
            "Decryption error: " + error.getMessage());
        
        // Show security-aware error message
        showSecurityError("Decryption Failed", message, vaultFile);
    }
    
    /**
     * Handle media playback errors
     */
    public void handleMediaError(Exception error, VaultFile vaultFile) {
        String extension = vaultFile.getExtension().toLowerCase();
        String message = createMediaErrorMessage(extension, error);
        
        // Log the event
        logPreviewError("MEDIA_PLAYBACK_FAILED", vaultFile, 
            "Media error: " + error.getMessage());
        
        // Show error with codec information
        showErrorWithFallback("Media Playback Failed", message, vaultFile, () -> {
            showMediaInfo(vaultFile, extension);
        });
    }
    
    /**
     * Handle component creation failures
     */
    public void handleComponentCreationError(Exception error, VaultFile vaultFile) {
        String message = "Failed to create preview component.\n\n" +
                        "Error: " + error.getMessage() + "\n\n" +
                        "This is likely a system issue. Try:\n" +
                        "• Restarting the application\n" +
                        "• Downloading the file to view externally";
        
        // Log the technical error
        logPreviewError("COMPONENT_CREATION_FAILED", vaultFile, 
            "Component creation error: " + error.getMessage());
        
        // Show technical error
        showTechnicalError("Preview System Error", message, vaultFile);
    }
    
    /**
     * Show fallback hex viewer for unsupported files
     */
    public void showFallbackViewer(byte[] data, VaultFile vaultFile) {
        if (data == null || data.length == 0) {
            showError("No Data", "No file data available for preview.");
            return;
        }
        
        try {
            // Create hex viewer dialog
            Dialog<Void> hexDialog = new Dialog<>();
            hexDialog.setTitle("Hex Viewer - " + vaultFile.getOriginalName());
            hexDialog.setHeaderText("Binary File Preview");
            
            // Create hex dump
            String hexDump = createHexDump(data, Math.min(data.length, 8192)); // Limit to 8KB
            
            TextArea hexArea = new TextArea(hexDump);
            hexArea.setEditable(false);
            hexArea.setStyle("-fx-font-family: 'Courier New', monospace; -fx-font-size: 10px;");
            hexArea.setPrefSize(700, 500);
            
            VBox content = new VBox(10);
            content.getChildren().addAll(
                new Label("Showing first " + Math.min(data.length, 8192) + " bytes of " + data.length + " total bytes:"),
                hexArea
            );
            
            hexDialog.getDialogPane().setContent(content);
            hexDialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            
            // Apply dark theme
            hexDialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/ghostvault-dark.css").toExternalForm());
            
            hexDialog.showAndWait();
            
            // Log hex viewer usage
            logPreviewError("HEX_VIEWER_USED", vaultFile, "Hex viewer fallback used");
            
        } catch (Exception e) {
            showError("Hex Viewer Error", "Failed to create hex viewer: " + e.getMessage());
        }
    }
    
    /**
     * Create hex dump of binary data
     */
    private String createHexDump(byte[] data, int maxBytes) {
        StringBuilder sb = new StringBuilder();
        int bytesToShow = Math.min(data.length, maxBytes);
        
        for (int i = 0; i < bytesToShow; i += 16) {
            // Address
            sb.append(String.format("%08X: ", i));
            
            // Hex bytes
            for (int j = 0; j < 16; j++) {
                if (i + j < bytesToShow) {
                    sb.append(String.format("%02X ", data[i + j] & 0xFF));
                } else {
                    sb.append("   ");
                }
                
                if (j == 7) sb.append(" "); // Extra space in middle
            }
            
            sb.append(" |");
            
            // ASCII representation
            for (int j = 0; j < 16 && i + j < bytesToShow; j++) {
                byte b = data[i + j];
                if (b >= 32 && b <= 126) {
                    sb.append((char) b);
                } else {
                    sb.append('.');
                }
            }
            
            sb.append("|\n");
        }
        
        if (bytesToShow < data.length) {
            sb.append(String.format("\n... (%d more bytes not shown)", data.length - bytesToShow));
        }
        
        return sb.toString();
    }
    
    /**
     * Create unsupported format message with suggestions
     */
    private String createUnsupportedFormatMessage(String extension) {
        StringBuilder message = new StringBuilder();
        message.append("Preview is not supported for ").append(extension.toUpperCase()).append(" files.\n\n");
        
        // Provide format-specific suggestions
        switch (extension.toLowerCase()) {
            case "exe":
            case "msi":
            case "dmg":
                message.append("This appears to be an executable file.\n")
                       .append("⚠️ For security reasons, executable files cannot be previewed.\n")
                       .append("Download and scan with antivirus before running.");
                break;
                
            case "zip":
            case "rar":
            case "7z":
            case "tar":
            case "gz":
                message.append("This appears to be an archive file.\n")
                       .append("Download and extract with an archive tool to view contents.");
                break;
                
            case "docx":
            case "xlsx":
            case "pptx":
                message.append("This appears to be a Microsoft Office document.\n")
                       .append("Download and open with Microsoft Office or LibreOffice.");
                break;
                
            default:
                message.append("You can:\n")
                       .append("• Download the file to view it with an appropriate application\n")
                       .append("• Use the hex viewer to see the raw file data");
                break;
        }
        
        return message.toString();
    }
    
    /**
     * Create media error message with codec information
     */
    private String createMediaErrorMessage(String extension, Exception error) {
        StringBuilder message = new StringBuilder();
        message.append("Failed to play ").append(extension.toUpperCase()).append(" media file.\n\n");
        
        // Provide codec-specific information
        switch (extension.toLowerCase()) {
            case "flac":
                message.append("FLAC files may require additional codec support.\n")
                       .append("Try converting to WAV or MP3 format.");
                break;
                
            case "mkv":
                message.append("MKV files may contain unsupported codecs.\n")
                       .append("Try converting to MP4 format.");
                break;
                
            case "avi":
                message.append("AVI files may use legacy codecs.\n")
                       .append("Try converting to MP4 format.");
                break;
                
            default:
                message.append("The media format or codec may not be supported.\n")
                       .append("Try downloading and playing with an external media player.");
                break;
        }
        
        message.append("\n\nTechnical error: ").append(error.getMessage());
        
        return message.toString();
    }
    
    /**
     * Show error dialog with fallback option
     */
    private void showErrorWithFallback(String title, String message, VaultFile vaultFile, Runnable fallbackAction) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText("Preview Failed");
        alert.setContentText(message);
        
        // Add fallback button
        ButtonType fallbackButton = new ButtonType("Show Hex Viewer");
        ButtonType cancelButton = ButtonType.CANCEL;
        
        alert.getButtonTypes().setAll(fallbackButton, cancelButton);
        
        // Apply dark theme
        alert.getDialogPane().getStylesheets().add(
            getClass().getResource("/ghostvault-dark.css").toExternalForm());
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == fallbackButton) {
            fallbackAction.run();
        }
    }
    
    /**
     * Show error dialog with multiple options
     */
    private void showErrorWithOptions(String title, String message, VaultFile vaultFile, 
                                    String[] options, OptionHandler handler) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText("Preview Failed");
        alert.setContentText(message);
        
        // Create button types for options
        ButtonType[] buttonTypes = new ButtonType[options.length];
        for (int i = 0; i < options.length; i++) {
            buttonTypes[i] = new ButtonType(options[i]);
        }
        
        alert.getButtonTypes().setAll(buttonTypes);
        
        // Apply dark theme
        alert.getDialogPane().getStylesheets().add(
            getClass().getResource("/ghostvault-dark.css").toExternalForm());
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent()) {
            handler.handleOption(result.get().getText());
        }
    }
    
    /**
     * Show security-related error
     */
    private void showSecurityError(String title, String message, VaultFile vaultFile) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText("Security Error");
        alert.setContentText(message);
        
        // Apply dark theme
        alert.getDialogPane().getStylesheets().add(
            getClass().getResource("/ghostvault-dark.css").toExternalForm());
        
        alert.showAndWait();
    }
    
    /**
     * Show technical error
     */
    private void showTechnicalError(String title, String message, VaultFile vaultFile) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText("System Error");
        alert.setContentText(message);
        
        // Apply dark theme
        alert.getDialogPane().getStylesheets().add(
            getClass().getResource("/ghostvault-dark.css").toExternalForm());
        
        alert.showAndWait();
    }
    
    /**
     * Show basic error dialog
     */
    private void showError(String title, String message) {
        if (notificationManager != null) {
            notificationManager.showError(title, message);
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setContentText(message);
            alert.showAndWait();
        }
    }
    
    /**
     * Show hex viewer option dialog
     */
    private void showHexViewerOption(VaultFile vaultFile) {
        // This would need to be implemented with access to file data
        // For now, show a message
        showError("Hex Viewer", "Hex viewer functionality requires file data access.");
    }
    
    /**
     * Show media information dialog
     */
    private void showMediaInfo(VaultFile vaultFile, String extension) {
        String info = "Media File Information:\n\n" +
                     "File: " + vaultFile.getOriginalName() + "\n" +
                     "Type: " + extension.toUpperCase() + "\n" +
                     "Size: " + formatFileSize(vaultFile.getSize()) + "\n\n" +
                     "Supported formats:\n" +
                     "• Audio: MP3, WAV, AAC, M4A\n" +
                     "• Video: MP4, MOV, M4V\n\n" +
                     "For other formats, download and use an external player.";
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Media Information");
        alert.setHeaderText("Format Details");
        alert.setContentText(info);
        
        // Apply dark theme
        alert.getDialogPane().getStylesheets().add(
            getClass().getResource("/ghostvault-dark.css").toExternalForm());
        
        alert.showAndWait();
    }
    
    /**
     * Log preview error events
     */
    private void logPreviewError(String errorType, VaultFile vaultFile, String details) {
        if (auditManager != null) {
            String fileId = vaultFile != null ? vaultFile.getFileId() : null;
            String fileName = vaultFile != null ? vaultFile.getOriginalName() : "unknown";
            
            auditManager.logSecurityEvent("PREVIEW_ERROR_" + errorType, 
                "Preview error: " + details, 
                AuditManager.AuditSeverity.WARNING, 
                fileId, 
                "File: " + fileName);
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
     * Interface for handling option selections
     */
    @FunctionalInterface
    public interface OptionHandler {
        void handleOption(String option);
    }
}
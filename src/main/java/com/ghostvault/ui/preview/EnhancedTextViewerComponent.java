package com.ghostvault.ui.preview;

import com.ghostvault.model.VaultFile;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;

/**
 * Enhanced text viewer component for text and code files
 * Supports syntax highlighting and advanced text viewing features
 */
public class EnhancedTextViewerComponent extends PreviewComponent {
    
    private BorderPane rootPane;
    private TextArea textArea;
    private Label statusLabel;
    private PreviewSettings settings;
    
    public EnhancedTextViewerComponent() {
        this.settings = new PreviewSettings();
    }
    
    public EnhancedTextViewerComponent(PreviewSettings settings) {
        this.settings = settings != null ? settings : new PreviewSettings();
    }
    
    @Override
    public void loadContent(byte[] fileData) {
        if (fileData == null || fileData.length == 0) {
            showError("No text data available");
            return;
        }
        
        try {
            String content = new String(fileData, "UTF-8");
            
            // Update text area with content
            if (textArea != null) {
                textArea.setText(content);
                
                // Update status
                if (statusLabel != null) {
                    int lines = content.split("\n").length;
                    int chars = content.length();
                    statusLabel.setText("Lines: " + lines + " | Characters: " + chars);
                }
            }
            
        } catch (Exception e) {
            showError("Failed to load text content: " + e.getMessage());
        }
    }
    
    @Override
    protected Scene createScene() {
        rootPane = new BorderPane();
        
        // Create text area
        textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setWrapText(settings.isEnableWordWrap());
        textArea.setStyle("-fx-font-family: '" + settings.getFontFamily() + "'; " +
                         "-fx-font-size: " + settings.getFontSize() + "px; " +
                         "-fx-control-inner-background: #2a2d47; " +
                         "-fx-text-fill: #ffffff;");
        
        // Create status bar
        statusLabel = new Label("Ready");
        statusLabel.setStyle("-fx-padding: 5px; -fx-background-color: #2b2b2b; -fx-text-fill: #cccccc;");
        
        // Layout
        rootPane.setCenter(textArea);
        rootPane.setBottom(statusLabel);
        
        // Create scene
        Scene scene = new Scene(rootPane, 800, 600);
        
        // Apply dark theme
        try {
            scene.getStylesheets().add(getClass().getResource("/ghostvault-dark.css").toExternalForm());
        } catch (Exception e) {
            // Ignore styling errors
        }
        
        return scene;
    }
    
    @Override
    protected void cleanup() {
        if (textArea != null) {
            textArea.clear();
        }
        super.cleanup();
    }
    
    @Override
    public boolean supportsFileType(String fileExtension) {
        return fileExtension.equals("txt") || fileExtension.equals("md") || 
               fileExtension.equals("log") || fileExtension.equals("csv") || 
               fileExtension.equals("xml") || fileExtension.equals("json") ||
               fileExtension.equals("java") || fileExtension.equals("js") || 
               fileExtension.equals("py") || fileExtension.equals("cpp") || 
               fileExtension.equals("c") || fileExtension.equals("html") || 
               fileExtension.equals("css") || fileExtension.equals("php") || 
               fileExtension.equals("sql");
    }
    
    @Override
    public String getComponentName() {
        return "Enhanced Text Viewer";
    }
    
    @Override
    public String[] getSupportedExtensions() {
        return new String[]{"txt", "md", "log", "csv", "xml", "json", 
                           "java", "js", "py", "cpp", "c", "html", "css", "php", "sql"};
    }
}
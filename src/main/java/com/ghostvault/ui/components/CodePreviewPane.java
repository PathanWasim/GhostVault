package com.ghostvault.ui.components;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

/**
 * Code preview component for text and code files
 */
public class CodePreviewPane extends VBox {
    
    private Label fileNameLabel;
    private TextArea codeArea;
    private ScrollPane scrollPane;
    private Label statusLabel;
    
    private File currentFile;
    
    // Supported code file extensions
    private static final List<String> CODE_EXTENSIONS = Arrays.asList(
        ".java", ".js", ".html", ".css", ".xml", ".json", ".txt", ".md",
        ".py", ".cpp", ".c", ".h", ".cs", ".php", ".rb", ".go", ".rs",
        ".sql", ".yml", ".yaml", ".properties", ".ini", ".cfg", ".conf"
    );
    
    public CodePreviewPane() {
        super();
        initializeComponents();
        setupLayout();
        applyStyles();
    }
    
    private void initializeComponents() {
        fileNameLabel = new Label("No file selected");
        
        codeArea = new TextArea();
        codeArea.setEditable(false);
        codeArea.setWrapText(false);
        codeArea.setFont(Font.font("Consolas", 12));
        
        scrollPane = new ScrollPane(codeArea);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        
        statusLabel = new Label("Ready");
    }
    
    private void setupLayout() {
        this.setSpacing(5);
        this.setPadding(new Insets(10));
        
        VBox.setVgrow(scrollPane, javafx.scene.layout.Priority.ALWAYS);
        
        this.getChildren().addAll(fileNameLabel, scrollPane, statusLabel);
    }
    
    private void applyStyles() {
        this.getStyleClass().add("code-preview-pane");
        
        this.setStyle("-fx-background-color: white;");
        
        fileNameLabel.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #333;"
        );
        
        codeArea.setStyle(
            "-fx-background-color: #f8f8f8;" +
            "-fx-border-color: #ddd;" +
            "-fx-border-width: 1px;" +
            "-fx-font-family: 'Consolas', 'Monaco', monospace;" +
            "-fx-font-size: 12px;"
        );
        
        statusLabel.setStyle(
            "-fx-font-size: 11px;" +
            "-fx-text-fill: #666;"
        );
    }
    
    public void loadFile(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            clear();
            return;
        }
        
        if (!isCodeFile(file)) {
            showUnsupportedFile(file);
            return;
        }
        
        try {
            this.currentFile = file;
            fileNameLabel.setText(file.getName());
            
            // Read file content
            String content = Files.readString(file.toPath());
            codeArea.setText(content);
            
            // Update status
            long lines = content.lines().count();
            long size = file.length();
            statusLabel.setText(String.format("Lines: %d | Size: %s", lines, formatFileSize(size)));
            
        } catch (IOException e) {
            showError("Error reading file: " + e.getMessage());
        }
    }
    
    private boolean isCodeFile(File file) {
        String fileName = file.getName().toLowerCase();
        return CODE_EXTENSIONS.stream().anyMatch(fileName::endsWith);
    }
    
    private void showUnsupportedFile(File file) {
        fileNameLabel.setText(file.getName());
        codeArea.setText("This file type is not supported for code preview.\\n\\n" +
                        "Supported formats: " + String.join(", ", CODE_EXTENSIONS));
        statusLabel.setText("Unsupported file type");
    }
    
    private void showError(String message) {
        fileNameLabel.setText("Error");
        codeArea.setText("Error loading file:\\n" + message);
        statusLabel.setText("Error");
    }
    
    public void clear() {
        currentFile = null;
        fileNameLabel.setText("No file selected");
        codeArea.clear();
        statusLabel.setText("Ready");
    }
    
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }
    
    public File getCurrentFile() {
        return currentFile;
    }
    
    public String getContent() {
        return codeArea.getText();
    }
    
    public void setContent(String content) {
        codeArea.setText(content);
    }
    
    public boolean hasContent() {
        return currentFile != null && !codeArea.getText().isEmpty();
    }
    
    public static boolean canPreview(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            return false;
        }
        String fileName = file.getName().toLowerCase();
        return CODE_EXTENSIONS.stream().anyMatch(fileName::endsWith);
    }
    
    public boolean isShowLineNumbers() {
        // Simple implementation - could be enhanced
        return false;
    }
}
package com.ghostvault.ui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

/**
 * Batch operations toolbar for bulk file operations
 */
public class BatchOperationsBar extends VBox {
    
    private HBox buttonContainer;
    private HBox progressContainer;
    
    private Button selectAllButton;
    private Button deselectAllButton;
    private Button deleteSelectedButton;
    private Button moveSelectedButton;
    private Button copySelectedButton;
    private Button encryptSelectedButton;
    
    private Label statusLabel;
    private ProgressBar progressBar;
    
    private Consumer<List<File>> onDeleteSelected;
    private Consumer<List<File>> onMoveSelected;
    private Consumer<List<File>> onCopySelected;
    private Consumer<List<File>> onEncryptSelected;
    private Runnable onSelectAll;
    private Runnable onDeselectAll;
    
    public BatchOperationsBar() {
        super();
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        applyStyles();
    }
    
    private void initializeComponents() {
        // Buttons
        selectAllButton = new Button("Select All");
        deselectAllButton = new Button("Deselect All");
        deleteSelectedButton = new Button("Delete Selected");
        moveSelectedButton = new Button("Move Selected");
        copySelectedButton = new Button("Copy Selected");
        encryptSelectedButton = new Button("Encrypt Selected");
        
        // Progress components
        statusLabel = new Label("Ready");
        progressBar = new ProgressBar(0);
        progressBar.setVisible(false);
        
        // Containers
        buttonContainer = new HBox(5);
        progressContainer = new HBox(10);
    }
    
    private void setupLayout() {
        this.setSpacing(5);
        this.setPadding(new Insets(5));
        
        buttonContainer.setAlignment(Pos.CENTER_LEFT);
        buttonContainer.getChildren().addAll(
            selectAllButton, deselectAllButton,
            deleteSelectedButton, moveSelectedButton, 
            copySelectedButton, encryptSelectedButton
        );
        
        progressContainer.setAlignment(Pos.CENTER_LEFT);
        progressContainer.getChildren().addAll(statusLabel, progressBar);
        HBox.setHgrow(progressBar, Priority.ALWAYS);
        
        this.getChildren().addAll(buttonContainer, progressContainer);
    }
    
    private void setupEventHandlers() {
        selectAllButton.setOnAction(e -> {
            if (onSelectAll != null) {
                onSelectAll.run();
            }
        });
        
        deselectAllButton.setOnAction(e -> {
            if (onDeselectAll != null) {
                onDeselectAll.run();
            }
        });
        
        deleteSelectedButton.setOnAction(e -> {
            if (onDeleteSelected != null) {
                onDeleteSelected.accept(null); // Files will be provided by caller
            }
        });
        
        moveSelectedButton.setOnAction(e -> {
            if (onMoveSelected != null) {
                onMoveSelected.accept(null);
            }
        });
        
        copySelectedButton.setOnAction(e -> {
            if (onCopySelected != null) {
                onCopySelected.accept(null);
            }
        });
        
        encryptSelectedButton.setOnAction(e -> {
            if (onEncryptSelected != null) {
                onEncryptSelected.accept(null);
            }
        });
    }
    
    private void applyStyles() {
        this.getStyleClass().add("batch-operations-bar");
        
        this.setStyle(
            "-fx-background-color: #f5f5f5;" +
            "-fx-border-color: #ddd;" +
            "-fx-border-width: 0 0 1px 0;"
        );
        
        String buttonStyle = 
            "-fx-background-color: #4CAF50;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 4px;" +
            "-fx-border-radius: 4px;" +
            "-fx-padding: 6px 12px;";
            
        selectAllButton.setStyle(buttonStyle);
        deselectAllButton.setStyle(buttonStyle.replace("#4CAF50", "#2196F3"));
        deleteSelectedButton.setStyle(buttonStyle.replace("#4CAF50", "#f44336"));
        moveSelectedButton.setStyle(buttonStyle.replace("#4CAF50", "#FF9800"));
        copySelectedButton.setStyle(buttonStyle.replace("#4CAF50", "#9C27B0"));
        encryptSelectedButton.setStyle(buttonStyle.replace("#4CAF50", "#607D8B"));
    }
    
    public void updateProgress(double progress, String status) {
        statusLabel.setText(status);
        progressBar.setProgress(progress);
        progressBar.setVisible(progress > 0 && progress < 1);
    }
    
    public void setOperationInProgress(boolean inProgress) {
        buttonContainer.setDisable(inProgress);
        progressBar.setVisible(inProgress);
    }
    
    // Setters for callbacks
    public void setOnDeleteSelected(Consumer<List<File>> callback) {
        this.onDeleteSelected = callback;
    }
    
    public void setOnMoveSelected(Consumer<List<File>> callback) {
        this.onMoveSelected = callback;
    }
    
    public void setOnCopySelected(Consumer<List<File>> callback) {
        this.onCopySelected = callback;
    }
    
    public void setOnEncryptSelected(Consumer<List<File>> callback) {
        this.onEncryptSelected = callback;
    }
    
    public void setOnSelectAll(Runnable callback) {
        this.onSelectAll = callback;
    }
    
    public void setOnDeselectAll(Runnable callback) {
        this.onDeselectAll = callback;
    }
    
    public void setOnDownload(Consumer<List<Object>> callback) {
        // Convert to File consumer
        this.onCopySelected = files -> {
            if (callback != null) {
                List<Object> objectList = new ArrayList<>();
                objectList.addAll(files);
                callback.accept(objectList);
            }
        };
    }
    
    public void setOnDelete(Consumer<List<Object>> callback) {
        // Convert to File consumer
        this.onDeleteSelected = files -> {
            if (callback != null) {
                List<Object> objectList = new ArrayList<>();
                objectList.addAll(files);
                callback.accept(objectList);
            }
        };
    }
    
    public void setOnMove(Consumer<List<Object>> callback) {
        // Convert to File consumer
        this.onMoveSelected = files -> {
            if (callback != null) {
                List<Object> objectList = new ArrayList<>();
                objectList.addAll(files);
                callback.accept(objectList);
            }
        };
    }
    
    public void setOnCopy(Consumer<List<Object>> callback) {
        // Convert to File consumer
        this.onCopySelected = files -> {
            if (callback != null) {
                List<Object> objectList = new ArrayList<>();
                objectList.addAll(files);
                callback.accept(objectList);
            }
        };
    }
    
    public void updateSelection(List<File> selectedFiles) {
        boolean hasSelection = selectedFiles != null && !selectedFiles.isEmpty();
        
        deleteSelectedButton.setDisable(!hasSelection);
        moveSelectedButton.setDisable(!hasSelection);
        copySelectedButton.setDisable(!hasSelection);
        encryptSelectedButton.setDisable(!hasSelection);
        
        if (hasSelection) {
            statusLabel.setText(selectedFiles.size() + " files selected");
        } else {
            statusLabel.setText("No files selected");
        }
    }
}
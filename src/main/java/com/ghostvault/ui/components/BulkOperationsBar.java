package com.ghostvault.ui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

/**
 * Toolbar for bulk file operations
 */
public class BulkOperationsBar extends HBox {
    
    private Label selectionLabel;
    private Button selectAllButton;
    private Button selectNoneButton;
    private Button deleteSelectedButton;
    private Button copySelectedButton;
    private Button moveSelectedButton;
    private Separator separator;
    
    private Consumer<Void> onSelectAll;
    private Consumer<Void> onSelectNone;
    private Consumer<List<File>> onDeleteSelected;
    private Consumer<List<File>> onCopySelected;
    private Consumer<List<File>> onMoveSelected;
    
    private List<File> selectedFiles;
    private boolean visible = false;
    
    public BulkOperationsBar() {
        super();
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        applyStyles();
        setVisible(false);
    }
    
    private void initializeComponents() {
        selectionLabel = new Label("0 files selected");
        
        selectAllButton = new Button("Select All");
        selectAllButton.setTooltip(new Tooltip("Select all files"));
        
        selectNoneButton = new Button("Select None");
        selectNoneButton.setTooltip(new Tooltip("Clear selection"));
        
        deleteSelectedButton = new Button("Delete");
        deleteSelectedButton.setTooltip(new Tooltip("Delete selected files"));
        deleteSelectedButton.getStyleClass().add("danger-button");
        
        copySelectedButton = new Button("Copy");
        copySelectedButton.setTooltip(new Tooltip("Copy selected files"));
        
        moveSelectedButton = new Button("Move");
        moveSelectedButton.setTooltip(new Tooltip("Move selected files"));
        
        separator = new Separator();
    }
    
    private void setupLayout() {
        this.setSpacing(10);
        this.setAlignment(Pos.CENTER_LEFT);
        this.setPadding(new Insets(8, 15, 8, 15));
        
        // Selection info and controls
        HBox selectionBox = new HBox(10);
        selectionBox.setAlignment(Pos.CENTER_LEFT);
        selectionBox.getChildren().addAll(
            selectionLabel,
            separator,
            selectAllButton,
            selectNoneButton
        );
        
        // Action buttons
        HBox actionsBox = new HBox(8);
        actionsBox.setAlignment(Pos.CENTER_LEFT);
        actionsBox.getChildren().addAll(
            copySelectedButton,
            moveSelectedButton,
            deleteSelectedButton
        );
        
        this.getChildren().addAll(selectionBox, actionsBox);
        HBox.setHgrow(selectionBox, Priority.ALWAYS);
    }
    
    private void setupEventHandlers() {
        selectAllButton.setOnAction(e -> {
            if (onSelectAll != null) {
                onSelectAll.accept(null);
            }
        });
        
        selectNoneButton.setOnAction(e -> {
            if (onSelectNone != null) {
                onSelectNone.accept(null);
            }
        });
        
        deleteSelectedButton.setOnAction(e -> {
            if (selectedFiles != null && !selectedFiles.isEmpty() && onDeleteSelected != null) {
                // Show confirmation dialog
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Delete Files");
                alert.setHeaderText("Are you sure you want to delete the selected files?");
                alert.setContentText(String.format("This will permanently delete %d file(s).", selectedFiles.size()));
                
                alert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        onDeleteSelected.accept(selectedFiles);
                    }
                });
            }
        });
        
        copySelectedButton.setOnAction(e -> {
            if (selectedFiles != null && !selectedFiles.isEmpty() && onCopySelected != null) {
                onCopySelected.accept(selectedFiles);
            }
        });
        
        moveSelectedButton.setOnAction(e -> {
            if (selectedFiles != null && !selectedFiles.isEmpty() && onMoveSelected != null) {
                onMoveSelected.accept(selectedFiles);
            }
        });
    }
    
    private void applyStyles() {
        this.getStyleClass().add("bulk-operations-bar");
        
        this.setStyle(
            "-fx-background-color: #e3f2fd;" +
            "-fx-border-color: #2196f3;" +
            "-fx-border-width: 1px 0 1px 0;" +
            "-fx-effect: dropshadow(gaussian, rgba(33, 150, 243, 0.3), 4, 0, 0, 1);"
        );
        
        selectionLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #1976d2;");
        
        // Style buttons
        selectAllButton.getStyleClass().add("bulk-action-button");
        selectNoneButton.getStyleClass().add("bulk-action-button");
        copySelectedButton.getStyleClass().add("bulk-action-button");
        moveSelectedButton.getStyleClass().add("bulk-action-button");
        
        deleteSelectedButton.setStyle(
            "-fx-background-color: #f44336;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 4px;" +
            "-fx-border-radius: 4px;" +
            "-fx-font-weight: bold;"
        );
        
        deleteSelectedButton.setOnMouseEntered(e -> 
            deleteSelectedButton.setStyle(
                "-fx-background-color: #d32f2f;" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 4px;" +
                "-fx-border-radius: 4px;" +
                "-fx-font-weight: bold;"
            )
        );
        
        deleteSelectedButton.setOnMouseExited(e -> 
            deleteSelectedButton.setStyle(
                "-fx-background-color: #f44336;" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 4px;" +
                "-fx-border-radius: 4px;" +
                "-fx-font-weight: bold;"
            )
        );
    }
    
    /**
     * Update the selection and show/hide the bar
     */
    public void updateSelection(List<File> selectedFiles) {
        this.selectedFiles = selectedFiles;
        
        if (selectedFiles == null || selectedFiles.isEmpty()) {
            hideBar();
        } else {
            showBar();
            updateSelectionLabel(selectedFiles.size());
            updateButtonStates();
        }
    }
    
    /**
     * Show the bulk operations bar
     */
    private void showBar() {
        if (!visible) {
            setVisible(true);
            setManaged(true);
            visible = true;
        }
    }
    
    /**
     * Hide the bulk operations bar
     */
    private void hideBar() {
        if (visible) {
            setVisible(false);
            setManaged(false);
            visible = false;
        }
    }
    
    /**
     * Update the selection count label
     */
    private void updateSelectionLabel(int count) {
        if (count == 1) {
            selectionLabel.setText("1 file selected");
        } else {
            selectionLabel.setText(count + " files selected");
        }
    }
    
    /**
     * Update button enabled states based on selection
     */
    private void updateButtonStates() {
        boolean hasSelection = selectedFiles != null && !selectedFiles.isEmpty();
        
        deleteSelectedButton.setDisable(!hasSelection);
        copySelectedButton.setDisable(!hasSelection);
        moveSelectedButton.setDisable(!hasSelection);
    }
    
    /**
     * Get the number of selected files
     */
    public int getSelectionCount() {
        return selectedFiles != null ? selectedFiles.size() : 0;
    }
    
    /**
     * Check if the bar is currently visible
     */
    public boolean isBarVisible() {
        return visible;
    }
    
    // Callback setters
    public void setOnSelectAll(Consumer<Void> callback) {
        this.onSelectAll = callback;
    }
    
    public void setOnSelectNone(Consumer<Void> callback) {
        this.onSelectNone = callback;
    }
    
    public void setOnDeleteSelected(Consumer<List<File>> callback) {
        this.onDeleteSelected = callback;
    }
    
    public void setOnCopySelected(Consumer<List<File>> callback) {
        this.onCopySelected = callback;
    }
    
    public void setOnMoveSelected(Consumer<List<File>> callback) {
        this.onMoveSelected = callback;
    }
}
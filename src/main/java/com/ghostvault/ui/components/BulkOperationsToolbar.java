package com.ghostvault.ui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

/**
 * Toolbar for bulk file operations
 */
public class BulkOperationsToolbar extends HBox {
    
    private Label selectionLabel;
    private Button selectAllButton;
    private Button selectNoneButton;
    private Button deleteSelectedButton;
    private Button moveSelectedButton;
    private Button copySelectedButton;
    private Button downloadSelectedButton;
    
    private Consumer<List<File>> onDeleteSelected;
    private Consumer<List<File>> onMoveSelected;
    private Consumer<List<File>> onCopySelected;
    private Consumer<List<File>> onDownloadSelected;
    private Runnable onSelectAll;
    private Runnable onSelectNone;
    
    private List<File> selectedFiles;
    
    public BulkOperationsToolbar() {
        initializeComponents();
        setupLayout();
        setupStyling();
        updateVisibility();
    }
    
    private void initializeComponents() {
        // Selection info
        selectionLabel = new Label("0 files selected");
        selectionLabel.getStyleClass().add("selection-label");
        
        // Selection controls
        selectAllButton = new Button("Select All");
        selectAllButton.getStyleClass().addAll("button", "ghost");
        selectAllButton.setOnAction(e -> {
            if (onSelectAll != null) onSelectAll.run();
        });
        
        selectNoneButton = new Button("Select None");
        selectNoneButton.getStyleClass().addAll("button", "ghost");
        selectNoneButton.setOnAction(e -> {
            if (onSelectNone != null) onSelectNone.run();
        });
        
        // Bulk operations
        deleteSelectedButton = new Button(ModernIcons.DELETE + " Delete");
        deleteSelectedButton.getStyleClass().addAll("button", "danger");
        deleteSelectedButton.setOnAction(e -> {
            if (selectedFiles != null && !selectedFiles.isEmpty() && onDeleteSelected != null) {
                showDeleteConfirmation();
            }
        });
        
        moveSelectedButton = new Button(ModernIcons.MOVE + " Move");
        moveSelectedButton.getStyleClass().addAll("button");
        moveSelectedButton.setOnAction(e -> {
            if (selectedFiles != null && !selectedFiles.isEmpty() && onMoveSelected != null) {
                onMoveSelected.accept(selectedFiles);
            }
        });
        
        copySelectedButton = new Button(ModernIcons.COPY + " Copy");
        copySelectedButton.getStyleClass().addAll("button");
        copySelectedButton.setOnAction(e -> {
            if (selectedFiles != null && !selectedFiles.isEmpty() && onCopySelected != null) {
                onCopySelected.accept(selectedFiles);
            }
        });
        
        downloadSelectedButton = new Button(ModernIcons.DOWNLOAD + " Download");
        downloadSelectedButton.getStyleClass().addAll("button", "primary");
        downloadSelectedButton.setOnAction(e -> {
            if (selectedFiles != null && !selectedFiles.isEmpty() && onDownloadSelected != null) {
                onDownloadSelected.accept(selectedFiles);
            }
        });
    }
    
    private void setupLayout() {
        this.setAlignment(Pos.CENTER_LEFT);
        this.setPadding(new Insets(8, 16, 8, 16));
        this.setSpacing(12);
        
        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);
        
        Region spacer2 = new Region();
        spacer2.setPrefWidth(20);
        
        this.getChildren().addAll(
            selectionLabel,
            spacer2,
            selectAllButton,
            selectNoneButton,
            spacer1,
            copySelectedButton,
            moveSelectedButton,
            downloadSelectedButton,
            deleteSelectedButton
        );
    }
    
    private void setupStyling() {
        this.getStyleClass().add("bulk-operations-toolbar");
    }
    
    public void updateSelection(List<File> selectedFiles) {
        this.selectedFiles = selectedFiles;
        
        int count = selectedFiles != null ? selectedFiles.size() : 0;
        selectionLabel.setText(count + " file" + (count != 1 ? "s" : "") + " selected");
        
        // Enable/disable buttons based on selection
        boolean hasSelection = count > 0;
        deleteSelectedButton.setDisable(!hasSelection);
        moveSelectedButton.setDisable(!hasSelection);
        copySelectedButton.setDisable(!hasSelection);
        downloadSelectedButton.setDisable(!hasSelection);
        selectNoneButton.setDisable(!hasSelection);
        
        updateVisibility();
    }
    
    private void updateVisibility() {
        boolean hasSelection = selectedFiles != null && !selectedFiles.isEmpty();
        this.setVisible(hasSelection);
        this.setManaged(hasSelection);
    }
    
    private void showDeleteConfirmation() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Files");
        alert.setHeaderText("Delete Selected Files");
        alert.setContentText(String.format(
            "Are you sure you want to delete %d file%s? This action cannot be undone.",
            selectedFiles.size(),
            selectedFiles.size() != 1 ? "s" : ""
        ));
        
        // Style the alert
        alert.getDialogPane().getStylesheets().add(
            getClass().getResource("/css/ultra-modern-theme.css").toExternalForm()
        );
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK && onDeleteSelected != null) {
                onDeleteSelected.accept(selectedFiles);
            }
        });
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
    
    public void setOnDownloadSelected(Consumer<List<File>> callback) {
        this.onDownloadSelected = callback;
    }
    
    public void setOnSelectAll(Runnable callback) {
        this.onSelectAll = callback;
    }
    
    public void setOnSelectNone(Runnable callback) {
        this.onSelectNone = callback;
    }
    
    public List<File> getSelectedFiles() {
        return selectedFiles;
    }
}
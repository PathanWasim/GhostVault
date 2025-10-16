package com.ghostvault.ui.components;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Consumer;

/**
 * Enhanced file list view with custom styling and functionality
 */
public class EnhancedFileListView extends ListView<File> {
    
    private Consumer<File> onFileSelected;
    private Consumer<File> onFileDoubleClicked;
    private Consumer<List<File>> onSelectionChanged;
    private FileContextMenuManager contextMenuManager;
    private List<File> originalFiles;
    private SortCriteria currentSortCriteria = SortCriteria.NAME;
    private boolean sortAscending = true;
    
    public enum SortCriteria {
        NAME, SIZE, DATE, TYPE
    }
    
    public EnhancedFileListView() {
        super();
        initialize();
    }
    
    private void initialize() {
        this.contextMenuManager = new FileContextMenuManager();
        
        // Enable multiple selection
        this.getSelectionModel().setSelectionMode(javafx.scene.control.SelectionMode.MULTIPLE);
        
        // Set custom cell factory
        this.setCellFactory(listView -> new FileListCell());
        
        // Handle selection changes
        this.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null && onFileSelected != null) {
                onFileSelected.accept(newSelection);
            }
        });
        
        // Handle multiple selection changes
        this.getSelectionModel().getSelectedItems().addListener((javafx.collections.ListChangeListener<File>) change -> {
            if (onSelectionChanged != null) {
                List<File> selectedFiles = new ArrayList<>(this.getSelectionModel().getSelectedItems());
                onSelectionChanged.accept(selectedFiles);
            }
        });
        
        // Handle double clicks
        this.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                File selectedFile = this.getSelectionModel().getSelectedItem();
                if (selectedFile != null && onFileDoubleClicked != null) {
                    onFileDoubleClicked.accept(selectedFile);
                }
            }
        });
        
        // Apply styling
        this.getStyleClass().add("enhanced-file-list");
    }
    
    /**
     * Update the file list
     */
    public void updateFiles(List<File> files) {
        this.originalFiles = new ArrayList<>(files);
        sortAndDisplayFiles();
    }
    
    /**
     * Sort files by the specified criteria
     */
    public void sortBy(SortCriteria criteria) {
        if (currentSortCriteria == criteria) {
            // Toggle sort order if same criteria
            sortAscending = !sortAscending;
        } else {
            // New criteria, default to ascending
            currentSortCriteria = criteria;
            sortAscending = true;
        }
        sortAndDisplayFiles();
    }
    
    /**
     * Sort and display files based on current criteria
     */
    private void sortAndDisplayFiles() {
        if (originalFiles == null) {
            return;
        }
        
        List<File> sortedFiles = new ArrayList<>(originalFiles);
        
        Comparator<File> comparator = getComparator(currentSortCriteria);
        if (!sortAscending) {
            comparator = comparator.reversed();
        }
        
        // Always sort directories first
        sortedFiles.sort((f1, f2) -> {
            if (f1.isDirectory() && !f2.isDirectory()) {
                return -1;
            } else if (!f1.isDirectory() && f2.isDirectory()) {
                return 1;
            } else {
                return comparator.compare(f1, f2);
            }
        });
        
        ObservableList<File> fileList = FXCollections.observableArrayList(sortedFiles);
        this.setItems(fileList);
    }
    
    /**
     * Get comparator for the specified sort criteria
     */
    private Comparator<File> getComparator(SortCriteria criteria) {
        switch (criteria) {
            case NAME:
                return Comparator.comparing(File::getName, String.CASE_INSENSITIVE_ORDER);
            
            case SIZE:
                return Comparator.comparingLong(File::length);
            
            case DATE:
                return Comparator.comparingLong(File::lastModified);
            
            case TYPE:
                return Comparator.comparing(this::getFileExtension, String.CASE_INSENSITIVE_ORDER);
            
            default:
                return Comparator.comparing(File::getName, String.CASE_INSENSITIVE_ORDER);
        }
    }
    
    /**
     * Get file extension for sorting
     */
    private String getFileExtension(File file) {
        if (file.isDirectory()) {
            return ""; // Directories have no extension
        }
        
        String name = file.getName();
        int lastDot = name.lastIndexOf('.');
        return lastDot > 0 ? name.substring(lastDot + 1) : "";
    }
    
    /**
     * Custom list cell for files
     */
    private class FileListCell extends ListCell<File> {
        private HBox content;
        private Label nameLabel;
        private Label sizeLabel;
        private Label dateLabel;
        
        public FileListCell() {
            super();
            createContent();
        }
        
        private void createContent() {
            nameLabel = new Label();
            nameLabel.getStyleClass().add("file-name-label");
            
            sizeLabel = new Label();
            sizeLabel.getStyleClass().add("file-size-label");
            sizeLabel.setMinWidth(80);
            sizeLabel.setAlignment(Pos.CENTER_RIGHT);
            
            dateLabel = new Label();
            dateLabel.getStyleClass().add("file-date-label");
            dateLabel.setMinWidth(120);
            dateLabel.setAlignment(Pos.CENTER_RIGHT);
            
            content = new HBox(10);
            content.setAlignment(Pos.CENTER_LEFT);
            content.setPadding(new Insets(5));
            
            HBox.setHgrow(nameLabel, Priority.ALWAYS);
            content.getChildren().addAll(nameLabel, sizeLabel, dateLabel);
        }
        
        @Override
        protected void updateItem(File file, boolean empty) {
            super.updateItem(file, empty);
            
            if (empty || file == null) {
                setGraphic(null);
                setContextMenu(null);
            } else {
                // Update labels
                nameLabel.setText(file.getName());
                
                if (file.isDirectory()) {
                    sizeLabel.setText("--");
                    nameLabel.setText("📁 " + file.getName());
                } else {
                    sizeLabel.setText(formatFileSize(file.length()));
                    nameLabel.setText("📄 " + file.getName());
                }
                
                // Format date
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
                dateLabel.setText(dateFormat.format(new Date(file.lastModified())));
                
                setGraphic(content);
                
                // Set context menu
                setContextMenu(contextMenuManager.createFileContextMenu(file));
            }
        }
    }
    
    /**
     * Format file size for display
     */
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }
    
    // Getters and setters
    public void setOnFileSelected(Consumer<File> onFileSelected) {
        this.onFileSelected = onFileSelected;
    }
    
    public void setOnFileDoubleClicked(Consumer<File> onFileDoubleClicked) {
        this.onFileDoubleClicked = onFileDoubleClicked;
    }
    
    public FileContextMenuManager getContextMenuManager() {
        return contextMenuManager;
    }
    
    /**
     * Get current sort criteria
     */
    public SortCriteria getCurrentSortCriteria() {
        return currentSortCriteria;
    }
    
    /**
     * Check if currently sorting in ascending order
     */
    public boolean isSortAscending() {
        return sortAscending;
    }
    
    /**
     * Get sort criteria display name
     */
    public static String getSortCriteriaDisplayName(SortCriteria criteria) {
        switch (criteria) {
            case NAME: return "Name";
            case SIZE: return "Size";
            case DATE: return "Date Modified";
            case TYPE: return "Type";
            default: return "Name";
        }
    }
    
    /**
     * Select all files
     */
    public void selectAll() {
        this.getSelectionModel().selectAll();
    }
    
    /**
     * Clear all selections
     */
    public void selectNone() {
        this.getSelectionModel().clearSelection();
    }
    
    /**
     * Get currently selected files
     */
    public List<File> getSelectedFiles() {
        return new ArrayList<>(this.getSelectionModel().getSelectedItems());
    }
    
    /**
     * Get the number of selected files
     */
    public int getSelectedCount() {
        return this.getSelectionModel().getSelectedItems().size();
    }
    
    /**
     * Check if multiple files are selected
     */
    public boolean hasMultipleSelection() {
        return this.getSelectionModel().getSelectedItems().size() > 1;
    }
    
    /**
     * Set callback for selection changes (multiple selection)
     */
    public void setOnSelectionChanged(Consumer<List<File>> onSelectionChanged) {
        this.onSelectionChanged = onSelectionChanged;
    }
}
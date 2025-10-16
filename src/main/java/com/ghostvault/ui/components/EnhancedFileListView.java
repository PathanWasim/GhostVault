package com.ghostvault.ui.components;

import com.ghostvault.model.VaultFile;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

/**
 * Enhanced file list view with thumbnails, detailed information, and professional styling
 */
public class EnhancedFileListView extends VBox {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
    private static final List<String> IMAGE_EXTENSIONS = Arrays.asList(".jpg", ".jpeg", ".png", ".gif", ".bmp", ".tiff");
    private static final List<String> VIDEO_EXTENSIONS = Arrays.asList(".mp4", ".avi", ".mkv", ".mov", ".wmv");
    private static final List<String> AUDIO_EXTENSIONS = Arrays.asList(".mp3", ".wav", ".aac", ".flac", ".ogg");
    private static final List<String> CODE_EXTENSIONS = Arrays.asList(".java", ".py", ".cpp", ".js", ".html", ".css", ".xml", ".json");
    
    // UI Components
    private TextField searchField;
    private ComboBox<String> filterComboBox;
    private ComboBox<String> sortComboBox;
    private TableView<VaultFile> fileTable;
    private Label statusLabel;
    private ProgressBar operationProgress;
    
    // Data
    private final ObservableList<VaultFile> allFiles;
    private final FilteredList<VaultFile> filteredFiles;
    private final SortedList<VaultFile> sortedFiles;
    
    // Callbacks
    private Consumer<VaultFile> onFileSelected;
    private Consumer<VaultFile> onFileDoubleClicked;
    private Consumer<List<VaultFile>> onFilesSelected;
    
    public EnhancedFileListView() {
        super(10);
        setPadding(new Insets(10));
        
        // Initialize data collections
        allFiles = FXCollections.observableArrayList();
        filteredFiles = new FilteredList<>(allFiles);
        sortedFiles = new SortedList<>(filteredFiles);
        
        // Create UI components
        HBox searchAndFilterBox = createSearchAndFilterSection();
        fileTable = createFileTable();
        HBox statusBox = createStatusSection();
        
        VBox.setVgrow(fileTable, Priority.ALWAYS);
        
        getChildren().addAll(searchAndFilterBox, fileTable, statusBox);
        
        // Apply professional styling
        getStyleClass().add("professional-panel");
        
        // Setup search and filtering
        setupSearchAndFiltering();
        
        // Setup table selection handling
        setupTableSelection();
    }
    
    /**
     * Create search and filter section
     */
    private HBox createSearchAndFilterSection() {
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.setPadding(new Insets(5));
        
        // Search field
        searchField = new TextField();
        searchField.setPromptText("🔍 Search files...");
        searchField.setPrefWidth(300);
        searchField.getStyleClass().add("search-field");
        
        // Filter dropdown
        Label filterLabel = new Label("Filter:");
        filterLabel.setStyle("-fx-text-fill: #cccccc; -fx-font-weight: 500;");
        
        filterComboBox = new ComboBox<>();
        filterComboBox.getItems().addAll("All Files", "Images", "Videos", "Audio", "Documents", "Code Files", "Archives");
        filterComboBox.setValue("All Files");
        filterComboBox.getStyleClass().add("professional-combo");
        
        // Sort dropdown
        Label sortLabel = new Label("Sort:");
        sortLabel.setStyle("-fx-text-fill: #cccccc; -fx-font-weight: 500;");
        
        sortComboBox = new ComboBox<>();
        sortComboBox.getItems().addAll("Name ↑", "Name ↓", "Size ↑", "Size ↓", "Date ↑", "Date ↓", "Type ↑", "Type ↓");
        sortComboBox.setValue("Name ↑");
        sortComboBox.getStyleClass().add("professional-combo");
        
        // Spacer
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // File count label
        Label fileCountLabel = new Label();
        fileCountLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 12px;");
        
        // Update file count when filtered list changes
        filteredFiles.addListener((javafx.collections.ListChangeListener<VaultFile>) change -> {
            Platform.runLater(() -> {
                int total = allFiles.size();
                int filtered = filteredFiles.size();
                if (total == filtered) {
                    fileCountLabel.setText(String.format("%d files", total));
                } else {
                    fileCountLabel.setText(String.format("%d of %d files", filtered, total));
                }
            });
        });
        
        searchBox.getChildren().addAll(
            searchField, filterLabel, filterComboBox, sortLabel, sortComboBox, spacer, fileCountLabel
        );
        
        return searchBox;
    }
    
    /**
     * Create the main file table
     */
    private TableView<VaultFile> createFileTable() {
        TableView<VaultFile> table = new TableView<>();
        table.getStyleClass().add("professional-table");
        table.setRowFactory(this::createTableRow);
        
        // Thumbnail column
        TableColumn<VaultFile, VaultFile> thumbnailCol = new TableColumn<>("");
        thumbnailCol.setPrefWidth(50);
        thumbnailCol.setMinWidth(50);
        thumbnailCol.setMaxWidth(50);
        thumbnailCol.setResizable(false);
        thumbnailCol.setSortable(false);
        thumbnailCol.setCellValueFactory(param -> new javafx.beans.property.SimpleObjectProperty<>(param.getValue()));
        thumbnailCol.setCellFactory(param -> new ThumbnailTableCell());
        
        // Name column
        TableColumn<VaultFile, String> nameCol = new TableColumn<>("Name");
        nameCol.setPrefWidth(300);
        nameCol.setCellValueFactory(new PropertyValueFactory<>("originalName"));
        nameCol.setCellFactory(param -> new FileNameTableCell());
        
        // Size column
        TableColumn<VaultFile, String> sizeCol = new TableColumn<>("Size");
        sizeCol.setPrefWidth(100);
        sizeCol.setCellValueFactory(param -> new SimpleStringProperty(formatFileSize(param.getValue().getSize())));
        sizeCol.setComparator(Comparator.comparing(this::parseFileSize));
        
        // Type column
        TableColumn<VaultFile, String> typeCol = new TableColumn<>("Type");
        typeCol.setPrefWidth(120);
        typeCol.setCellValueFactory(param -> new SimpleStringProperty(getFileType(param.getValue().getOriginalName())));
        
        // Modified column
        TableColumn<VaultFile, String> modifiedCol = new TableColumn<>("Modified");
        modifiedCol.setPrefWidth(150);
        modifiedCol.setCellValueFactory(param -> {
            LocalDateTime dateTime = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(param.getValue().getUploadTime()), 
                ZoneId.systemDefault()
            );
            return new SimpleStringProperty(dateTime.format(DATE_FORMATTER));
        });
        
        // Actions column
        TableColumn<VaultFile, VaultFile> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(100);
        actionsCol.setMinWidth(100);
        actionsCol.setSortable(false);
        actionsCol.setCellValueFactory(param -> new javafx.beans.property.SimpleObjectProperty<>(param.getValue()));
        actionsCol.setCellFactory(param -> new ActionsTableCell());
        
        table.getColumns().addAll(thumbnailCol, nameCol, sizeCol, typeCol, modifiedCol, actionsCol);
        
        // Bind sorted list to table
        table.setItems(sortedFiles);
        sortedFiles.comparatorProperty().bind(table.comparatorProperty());
        
        return table;
    }
    
    /**
     * Create status section
     */
    private HBox createStatusSection() {
        HBox statusBox = new HBox(10);
        statusBox.setAlignment(Pos.CENTER_LEFT);
        statusBox.setPadding(new Insets(5));
        
        statusLabel = new Label("Ready");
        statusLabel.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 12px;");
        
        operationProgress = new ProgressBar();
        operationProgress.setPrefWidth(200);
        operationProgress.setVisible(false);
        operationProgress.getStyleClass().add("operation-progress");
        
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        statusBox.getChildren().addAll(statusLabel, spacer, operationProgress);
        
        return statusBox;
    }
    
    /**
     * Setup search and filtering functionality
     */
    private void setupSearchAndFiltering() {
        // Search functionality
        searchField.textProperty().addListener((obs, oldText, newText) -> {
            updateFilter();
        });
        
        // Filter functionality
        filterComboBox.valueProperty().addListener((obs, oldValue, newValue) -> {
            updateFilter();
        });
        
        // Sort functionality
        sortComboBox.valueProperty().addListener((obs, oldValue, newValue) -> {
            updateSort(newValue);
        });
    }
    
    /**
     * Update file filter based on search and filter criteria
     */
    private void updateFilter() {
        String searchText = searchField.getText().toLowerCase().trim();
        String filterType = filterComboBox.getValue();
        
        filteredFiles.setPredicate(file -> {
            // Search filter
            if (!searchText.isEmpty()) {
                if (!file.getOriginalName().toLowerCase().contains(searchText)) {
                    return false;
                }
            }
            
            // Type filter
            if (!"All Files".equals(filterType)) {
                String extension = getFileExtension(file.getOriginalName()).toLowerCase();
                switch (filterType) {
                    case "Images":
                        return IMAGE_EXTENSIONS.contains(extension);
                    case "Videos":
                        return VIDEO_EXTENSIONS.contains(extension);
                    case "Audio":
                        return AUDIO_EXTENSIONS.contains(extension);
                    case "Code Files":
                        return CODE_EXTENSIONS.contains(extension);
                    case "Documents":
                        return Arrays.asList(".pdf", ".doc", ".docx", ".txt", ".rtf").contains(extension);
                    case "Archives":
                        return Arrays.asList(".zip", ".rar", ".7z", ".tar", ".gz").contains(extension);
                }
            }
            
            return true;
        });
    }
    
    /**
     * Update sort order
     */
    private void updateSort(String sortOption) {
        Comparator<VaultFile> comparator = null;
        
        switch (sortOption) {
            case "Name ↑":
                comparator = Comparator.comparing(VaultFile::getOriginalName, String.CASE_INSENSITIVE_ORDER);
                break;
            case "Name ↓":
                comparator = Comparator.comparing(VaultFile::getOriginalName, String.CASE_INSENSITIVE_ORDER).reversed();
                break;
            case "Size ↑":
                comparator = Comparator.comparing(VaultFile::getSize);
                break;
            case "Size ↓":
                comparator = Comparator.comparing(VaultFile::getSize).reversed();
                break;
            case "Date ↑":
                comparator = Comparator.comparing(VaultFile::getUploadTime);
                break;
            case "Date ↓":
                comparator = Comparator.comparing(VaultFile::getUploadTime).reversed();
                break;
            case "Type ↑":
                comparator = Comparator.comparing(file -> getFileType(file.getOriginalName()));
                break;
            case "Type ↓":
                comparator = Comparator.comparing((VaultFile file) -> getFileType(file.getOriginalName())).reversed();
                break;
        }
        
        if (comparator != null) {
            sortedFiles.setComparator(comparator);
        }
    }
    
    /**
     * Setup table selection handling
     */
    private void setupTableSelection() {
        fileTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null && onFileSelected != null) {
                onFileSelected.accept(newSelection);
            }
        });
        
        fileTable.setRowFactory(tv -> {
            TableRow<VaultFile> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty() && onFileDoubleClicked != null) {
                    onFileDoubleClicked.accept(row.getItem());
                }
            });
            return row;
        });
        
        // Multi-selection support
        fileTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        fileTable.getSelectionModel().getSelectedItems().addListener(
            (javafx.collections.ListChangeListener<VaultFile>) change -> {
                if (onFilesSelected != null) {
                    onFilesSelected.accept(fileTable.getSelectionModel().getSelectedItems());
                }
            }
        );
    }
    
    /**
     * Custom table row factory
     */
    private TableRow<VaultFile> createTableRow(TableView<VaultFile> tableView) {
        TableRow<VaultFile> row = new TableRow<VaultFile>() {
            @Override
            protected void updateItem(VaultFile file, boolean empty) {
                super.updateItem(file, empty);
                
                if (empty || file == null) {
                    setStyle("");
                } else {
                    // Add hover effect
                    setOnMouseEntered(e -> {
                        if (!isSelected()) {
                            setStyle("-fx-background-color: rgba(255, 255, 255, 0.05);");
                        }
                    });
                    
                    setOnMouseExited(e -> {
                        if (!isSelected()) {
                            setStyle("");
                        }
                    });
                }
            }
        };
        
        return row;
    }
    
    /**
     * Thumbnail table cell
     */
    private class ThumbnailTableCell extends TableCell<VaultFile, VaultFile> {
        private final ImageView imageView;
        private final Label iconLabel;
        
        public ThumbnailTableCell() {
            imageView = new ImageView();
            imageView.setFitWidth(32);
            imageView.setFitHeight(32);
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);
            
            iconLabel = new Label();
            iconLabel.setStyle("-fx-font-size: 24px;");
            
            setAlignment(Pos.CENTER);
        }
        
        @Override
        protected void updateItem(VaultFile file, boolean empty) {
            super.updateItem(file, empty);
            
            if (empty || file == null) {
                setGraphic(null);
            } else {
                String extension = getFileExtension(file.getOriginalName()).toLowerCase();
                
                // Show appropriate icon or thumbnail
                if (IMAGE_EXTENSIONS.contains(extension)) {
                    // For images, we could load actual thumbnails here
                    iconLabel.setText("🖼️");
                    setGraphic(iconLabel);
                } else if (VIDEO_EXTENSIONS.contains(extension)) {
                    iconLabel.setText("🎬");
                    setGraphic(iconLabel);
                } else if (AUDIO_EXTENSIONS.contains(extension)) {
                    iconLabel.setText("🎵");
                    setGraphic(iconLabel);
                } else if (CODE_EXTENSIONS.contains(extension)) {
                    iconLabel.setText("💻");
                    setGraphic(iconLabel);
                } else if (Arrays.asList(".pdf").contains(extension)) {
                    iconLabel.setText("📄");
                    setGraphic(iconLabel);
                } else if (Arrays.asList(".zip", ".rar", ".7z").contains(extension)) {
                    iconLabel.setText("📦");
                    setGraphic(iconLabel);
                } else {
                    iconLabel.setText("📄");
                    setGraphic(iconLabel);
                }
            }
        }
    }
    
    /**
     * File name table cell with styling
     */
    private class FileNameTableCell extends TableCell<VaultFile, String> {
        @Override
        protected void updateItem(String fileName, boolean empty) {
            super.updateItem(fileName, empty);
            
            if (empty || fileName == null) {
                setText(null);
                setStyle("");
            } else {
                setText(fileName);
                setStyle("-fx-text-fill: #ffffff; -fx-font-weight: 500;");
            }
        }
    }
    
    /**
     * Actions table cell with buttons
     */
    private class ActionsTableCell extends TableCell<VaultFile, VaultFile> {
        private final HBox actionBox;
        private final Button downloadButton;
        private final Button deleteButton;
        
        public ActionsTableCell() {
            downloadButton = new Button("💾");
            downloadButton.getStyleClass().addAll("professional-button", "button-success");
            downloadButton.setTooltip(new Tooltip("Download file"));
            downloadButton.setPrefSize(30, 25);
            
            deleteButton = new Button("🗑️");
            deleteButton.getStyleClass().addAll("professional-button", "button-danger");
            deleteButton.setTooltip(new Tooltip("Delete file"));
            deleteButton.setPrefSize(30, 25);
            
            actionBox = new HBox(5, downloadButton, deleteButton);
            actionBox.setAlignment(Pos.CENTER);
        }
        
        @Override
        protected void updateItem(VaultFile file, boolean empty) {
            super.updateItem(file, empty);
            
            if (empty || file == null) {
                setGraphic(null);
            } else {
                // Setup button actions
                downloadButton.setOnAction(e -> {
                    // Handle download
                    showStatus("Downloading " + file.getOriginalName() + "...");
                });
                
                deleteButton.setOnAction(e -> {
                    // Handle delete with confirmation
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Delete File");
                    confirm.setHeaderText("Delete " + file.getOriginalName() + "?");
                    confirm.setContentText("This action cannot be undone.");
                    
                    if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                        showStatus("Deleting " + file.getOriginalName() + "...");
                        // Perform delete operation
                    }
                });
                
                setGraphic(actionBox);
            }
        }
    }
    
    // Public API methods
    
    /**
     * Set the list of files to display
     */
    public void setFiles(List<VaultFile> files) {
        Platform.runLater(() -> {
            allFiles.setAll(files);
            showStatus(String.format("Loaded %d files", files.size()));
        });
    }
    
    /**
     * Add a file to the list
     */
    public void addFile(VaultFile file) {
        Platform.runLater(() -> {
            allFiles.add(file);
            showStatus("Added " + file.getOriginalName());
        });
    }
    
    /**
     * Remove a file from the list
     */
    public void removeFile(VaultFile file) {
        Platform.runLater(() -> {
            allFiles.remove(file);
            showStatus("Removed " + file.getOriginalName());
        });
    }
    
    /**
     * Get selected files
     */
    public List<VaultFile> getSelectedFiles() {
        return fileTable.getSelectionModel().getSelectedItems();
    }
    
    /**
     * Set file selection callback
     */
    public void setOnFileSelected(Consumer<VaultFile> callback) {
        this.onFileSelected = callback;
    }
    
    /**
     * Set file double-click callback
     */
    public void setOnFileDoubleClicked(Consumer<VaultFile> callback) {
        this.onFileDoubleClicked = callback;
    }
    
    /**
     * Set multiple files selection callback
     */
    public void setOnFilesSelected(Consumer<List<VaultFile>> callback) {
        this.onFilesSelected = callback;
    }
    
    /**
     * Show operation progress
     */
    public void showProgress(boolean show) {
        Platform.runLater(() -> operationProgress.setVisible(show));
    }
    
    /**
     * Update progress
     */
    public void updateProgress(double progress) {
        Platform.runLater(() -> operationProgress.setProgress(progress));
    }
    
    /**
     * Show status message
     */
    public void showStatus(String message) {
        Platform.runLater(() -> statusLabel.setText(message));
    }
    
    // Utility methods
    
    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot == -1 ? "" : fileName.substring(lastDot);
    }
    
    private String getFileType(String fileName) {
        String extension = getFileExtension(fileName).toLowerCase();
        
        if (IMAGE_EXTENSIONS.contains(extension)) return "Image";
        if (VIDEO_EXTENSIONS.contains(extension)) return "Video";
        if (AUDIO_EXTENSIONS.contains(extension)) return "Audio";
        if (CODE_EXTENSIONS.contains(extension)) return "Code";
        if (Arrays.asList(".pdf", ".doc", ".docx", ".txt").contains(extension)) return "Document";
        if (Arrays.asList(".zip", ".rar", ".7z").contains(extension)) return "Archive";
        
        return "File";
    }
    
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }
    
    private long parseFileSize(String sizeStr) {
        // Parse formatted file size back to bytes for sorting
        if (sizeStr.endsWith(" B")) {
            return Long.parseLong(sizeStr.substring(0, sizeStr.length() - 2));
        } else if (sizeStr.endsWith(" KB")) {
            return (long) (Double.parseDouble(sizeStr.substring(0, sizeStr.length() - 3)) * 1024);
        } else if (sizeStr.endsWith(" MB")) {
            return (long) (Double.parseDouble(sizeStr.substring(0, sizeStr.length() - 3)) * 1024 * 1024);
        } else if (sizeStr.endsWith(" GB")) {
            return (long) (Double.parseDouble(sizeStr.substring(0, sizeStr.length() - 3)) * 1024 * 1024 * 1024);
        }
        return 0;
    }
} 
   /**
     * Real-time search and filtering implementation
     */
    private void updateFilter() {
        String searchText = searchField.getText().toLowerCase().trim();
        String filterType = filterComboBox.getValue();
        
        filteredFiles.setPredicate(file -> {
            // Search filter
            if (!searchText.isEmpty()) {
                boolean nameMatch = file.getName().toLowerCase().contains(searchText);
                boolean extensionMatch = getFileExtension(file.getName()).toLowerCase().contains(searchText);
                if (!nameMatch && !extensionMatch) {
                    return false;
                }
            }
            
            // Type filter
            if (filterType != null && !filterType.equals("All Files")) {
                String extension = getFileExtension(file.getName()).toLowerCase();
                switch (filterType) {
                    case "Images":
                        if (!IMAGE_EXTENSIONS.contains(extension)) return false;
                        break;
                    case "Videos":
                        if (!VIDEO_EXTENSIONS.contains(extension)) return false;
                        break;
                    case "Audio":
                        if (!AUDIO_EXTENSIONS.contains(extension)) return false;
                        break;
                    case "Documents":
                        if (!extension.matches("\\.(pdf|doc|docx|txt|rtf|odt)")) return false;
                        break;
                    case "Code":
                        if (!CODE_EXTENSIONS.contains(extension)) return false;
                        break;
                    case "Archives":
                        if (!extension.matches("\\.(zip|rar|7z|tar|gz|bz2)")) return false;
                        break;
                }
            }
            
            return true;
        });
        
        // Update status
        int totalFiles = allFiles.size();
        int filteredCount = filteredFiles.size();
        
        if (filteredCount == totalFiles) {
            statusLabel.setText(String.format("Showing %d files", totalFiles));
        } else {
            statusLabel.setText(String.format("Showing %d of %d files", filteredCount, totalFiles));
        }
    }
    
    /**
     * Advanced search with multiple criteria
     */
    public void performAdvancedSearch(String name, String extension, long minSize, long maxSize, 
                                    LocalDateTime fromDate, LocalDateTime toDate) {
        filteredFiles.setPredicate(file -> {
            // Name filter
            if (name != null && !name.trim().isEmpty()) {
                if (!file.getName().toLowerCase().contains(name.toLowerCase().trim())) {
                    return false;
                }
            }
            
            // Extension filter
            if (extension != null && !extension.trim().isEmpty()) {
                String fileExt = getFileExtension(file.getName()).toLowerCase();
                if (!fileExt.equals(extension.toLowerCase().trim())) {
                    return false;
                }
            }
            
            // Size filter
            if (file.isFile()) {
                long fileSize = file.length();
                if (minSize > 0 && fileSize < minSize) return false;
                if (maxSize > 0 && fileSize > maxSize) return false;
            }
            
            // Date filter
            if (fromDate != null || toDate != null) {
                try {
                    LocalDateTime fileDate = LocalDateTime.ofInstant(
                        java.nio.file.Files.getLastModifiedTime(file.toPath()).toInstant(),
                        java.time.ZoneId.systemDefault()
                    );
                    
                    if (fromDate != null && fileDate.isBefore(fromDate)) return false;
                    if (toDate != null && fileDate.isAfter(toDate)) return false;
                    
                } catch (Exception e) {
                    // If we can't get the date, include the file
                }
            }
            
            return true;
        });
    }
    
    /**
     * Quick filter methods
     */
    public void showOnlyImages() {
        filterComboBox.setValue("Images");
    }
    
    public void showOnlyVideos() {
        filterComboBox.setValue("Videos");
    }
    
    public void showOnlyAudio() {
        filterComboBox.setValue("Audio");
    }
    
    public void showOnlyDocuments() {
        filterComboBox.setValue("Documents");
    }
    
    public void showOnlyCode() {
        filterComboBox.setValue("Code");
    }
    
    public void clearFilters() {
        searchField.clear();
        filterComboBox.setValue("All Files");
        sortComboBox.setValue("Name");
    }
    
    /**
     * Get search statistics
     */
    public SearchStats getSearchStats() {
        return new SearchStats(
            allFiles.size(),
            filteredFiles.size(),
            searchField.getText(),
            filterComboBox.getValue()
        );
    }
    
    /**
     * Search statistics class
     */
    public static class SearchStats {
        public final int totalFiles;
        public final int filteredFiles;
        public final String searchTerm;
        public final String filterType;
        
        public SearchStats(int totalFiles, int filteredFiles, String searchTerm, String filterType) {
            this.totalFiles = totalFiles;
            this.filteredFiles = filteredFiles;
            this.searchTerm = searchTerm;
            this.filterType = filterType;
        }
    }
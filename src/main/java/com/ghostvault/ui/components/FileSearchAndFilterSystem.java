package com.ghostvault.ui.components;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Comprehensive file search and filtering system with real-time capabilities
 */
public class FileSearchAndFilterSystem extends VBox {
    
    // UI Components
    private ModernSearchBar searchBar;
    private HBox filterBar;
    private ComboBox<String> fileTypeFilter;
    private ComboBox<String> sizeFilter;
    private ComboBox<String> dateFilter;
    private ComboBox<String> sortByFilter;
    private CheckBox showHiddenFiles;
    private Button clearFiltersButton;
    private Label resultsCountLabel;
    private ProgressIndicator searchProgress;
    
    // Data
    private List<File> allFiles = new ArrayList<>();
    private List<File> filteredFiles = new ArrayList<>();
    private StringProperty searchText = new SimpleStringProperty("");
    
    // Callbacks
    private java.util.function.Consumer<List<File>> onResultsChanged;
    
    // Search configuration
    private boolean caseSensitive = false;
    private boolean searchInContent = false;
    private boolean includeSubfolders = true;
    
    public FileSearchAndFilterSystem() {
        initializeComponents();
        setupLayout();
        setupStyling();
        setupEventHandlers();
    }
    
    /**
     * Initialize all components
     */
    private void initializeComponents() {
        // Search bar
        searchBar = new ModernSearchBar();
        searchBar.setRealTimeSearch(true);
        searchBar.setMinSearchLength(1);
        searchBar.setSearchDelay(300);
        
        // Filter bar
        filterBar = new HBox(8);
        filterBar.setAlignment(Pos.CENTER_LEFT);
        filterBar.getStyleClass().add("filter-bar");
        
        // File type filter
        fileTypeFilter = new ComboBox<>();
        fileTypeFilter.getItems().addAll(
            "All Files", "Images", "Videos", "Audio", "Documents", 
            "Archives", "Code Files", "Text Files", "Executables"
        );
        fileTypeFilter.setValue("All Files");
        fileTypeFilter.getStyleClass().add("filter-combo");
        
        // Size filter
        sizeFilter = new ComboBox<>();
        sizeFilter.getItems().addAll(
            "Any Size", "Empty", "Tiny (< 16 KB)", "Small (< 1 MB)", 
            "Medium (1-100 MB)", "Large (100 MB - 1 GB)", "Huge (> 1 GB)"
        );
        sizeFilter.setValue("Any Size");
        sizeFilter.getStyleClass().add("filter-combo");
        
        // Date filter
        dateFilter = new ComboBox<>();
        dateFilter.getItems().addAll(
            "Any Date", "Today", "Yesterday", "This Week", "This Month", 
            "This Year", "Older than 1 Year"
        );
        dateFilter.setValue("Any Date");
        dateFilter.getStyleClass().add("filter-combo");
        
        // Sort by filter
        sortByFilter = new ComboBox<>();
        sortByFilter.getItems().addAll(
            "Name (A-Z)", "Name (Z-A)", "Size (Smallest)", "Size (Largest)",
            "Date (Newest)", "Date (Oldest)", "Type (A-Z)", "Type (Z-A)"
        );
        sortByFilter.setValue("Name (A-Z)");
        sortByFilter.getStyleClass().add("filter-combo");
        
        // Show hidden files
        showHiddenFiles = new CheckBox("Show hidden files");
        showHiddenFiles.getStyleClass().add("filter-checkbox");
        
        // Clear filters button
        clearFiltersButton = new Button("Clear Filters");
        clearFiltersButton.getStyleClass().addAll("button", "ghost", "clear-filters-button");
        
        filterBar.getChildren().addAll(
            new Label("Type:"), fileTypeFilter,
            new Label("Size:"), sizeFilter,
            new Label("Date:"), dateFilter,
            new Label("Sort:"), sortByFilter,
            showHiddenFiles,
            clearFiltersButton
        );
        
        // Results info
        HBox resultsBar = new HBox(8);
        resultsBar.setAlignment(Pos.CENTER_LEFT);
        resultsBar.getStyleClass().add("results-bar");
        
        resultsCountLabel = new Label("0 files");
        resultsCountLabel.getStyleClass().add("results-count-label");
        
        searchProgress = new ProgressIndicator();
        searchProgress.getStyleClass().add("search-progress");
        searchProgress.setPrefSize(16, 16);
        searchProgress.setVisible(false);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        resultsBar.getChildren().addAll(resultsCountLabel, spacer, searchProgress);
        
        this.getChildren().addAll(searchBar, filterBar, resultsBar);
    }
    
    /**
     * Setup layout properties
     */
    private void setupLayout() {
        this.setSpacing(8);
        this.setPadding(new Insets(8));
    }
    
    /**
     * Setup component styling
     */
    private void setupStyling() {
        this.getStyleClass().add("file-search-filter-system");
    }
    
    /**
     * Setup event handlers
     */
    private void setupEventHandlers() {
        // Search bar events
        searchBar.setOnSearchResults(this::handleSearchResults);
        searchBar.setOnSearchTextChanged(this::handleSearchTextChanged);
        
        // Filter change events
        fileTypeFilter.setOnAction(e -> applyFilters());
        sizeFilter.setOnAction(e -> applyFilters());
        dateFilter.setOnAction(e -> applyFilters());
        sortByFilter.setOnAction(e -> applyFilters());
        showHiddenFiles.setOnAction(e -> applyFilters());
        
        // Clear filters
        clearFiltersButton.setOnAction(e -> clearAllFilters());
        
        // Bind search text property
        searchText.bind(searchBar.searchTextProperty());
    }
    
    /**
     * Set the list of files to search through
     */
    public void setFiles(List<File> files) {
        this.allFiles = new ArrayList<>(files);
        searchBar.setItems(allFiles);
        applyFilters();
    }
    
    /**
     * Handle search results from search bar
     */
    private void handleSearchResults(List<File> results) {
        this.filteredFiles = new ArrayList<>(results);
        applyAdditionalFilters();
    }
    
    /**
     * Handle search text changes
     */
    private void handleSearchTextChanged(String searchText) {
        if (searchText.isEmpty()) {
            // No search text, show all files with filters applied
            this.filteredFiles = new ArrayList<>(allFiles);
            applyAdditionalFilters();
        }
    }
    
    /**
     * Apply all filters to the current file list
     */
    private void applyFilters() {
        showSearchProgress();
        
        Task<List<File>> filterTask = new Task<List<File>>() {
            @Override
            protected List<File> call() throws Exception {
                List<File> result = new ArrayList<>(allFiles);
                
                // Apply search text filter if present
                String searchText = searchBar.getSearchText();
                if (!searchText.isEmpty()) {
                    result = filterBySearchText(result, searchText);
                }
                
                // Apply additional filters
                result = applyFileTypeFilter(result);
                result = applySizeFilter(result);
                result = applyDateFilter(result);
                result = applyHiddenFilesFilter(result);
                
                // Apply sorting
                result = applySorting(result);
                
                return result;
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    filteredFiles = getValue();
                    updateResultsDisplay();
                    hideSearchProgress();
                    
                    if (onResultsChanged != null) {
                        onResultsChanged.accept(filteredFiles);
                    }
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    hideSearchProgress();
                });
            }
        };
        
        Thread filterThread = new Thread(filterTask);
        filterThread.setDaemon(true);
        filterThread.start();
    }
    
    /**
     * Apply additional filters after search
     */
    private void applyAdditionalFilters() {
        showSearchProgress();
        
        Task<List<File>> filterTask = new Task<List<File>>() {
            @Override
            protected List<File> call() throws Exception {
                List<File> result = new ArrayList<>(filteredFiles);
                
                result = applyFileTypeFilter(result);
                result = applySizeFilter(result);
                result = applyDateFilter(result);
                result = applyHiddenFilesFilter(result);
                result = applySorting(result);
                
                return result;
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    filteredFiles = getValue();
                    updateResultsDisplay();
                    hideSearchProgress();
                    
                    if (onResultsChanged != null) {
                        onResultsChanged.accept(filteredFiles);
                    }
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    hideSearchProgress();
                });
            }
        };
        
        Thread filterThread = new Thread(filterTask);
        filterThread.setDaemon(true);
        filterThread.start();
    }
    
    /**
     * Filter files by search text
     */
    private List<File> filterBySearchText(List<File> files, String searchText) {
        String lowerSearchText = caseSensitive ? searchText : searchText.toLowerCase();
        
        return files.stream()
            .filter(file -> {
                String fileName = caseSensitive ? file.getName() : file.getName().toLowerCase();
                return fileName.contains(lowerSearchText);
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Apply file type filter
     */
    private List<File> applyFileTypeFilter(List<File> files) {
        String selectedType = fileTypeFilter.getValue();
        if ("All Files".equals(selectedType)) {
            return files;
        }
        
        return files.stream()
            .filter(file -> matchesFileType(file, selectedType))
            .collect(Collectors.toList());
    }
    
    /**
     * Check if file matches the selected type
     */
    private boolean matchesFileType(File file, String type) {
        if (file.isDirectory()) {
            return false; // Directories don't match specific file types
        }
        
        String extension = getFileExtension(file.getName()).toLowerCase();
        
        switch (type) {
            case "Images":
                return extension.matches("jpg|jpeg|png|gif|bmp|svg|webp|tiff|tif");
            case "Videos":
                return extension.matches("mp4|avi|mkv|mov|wmv|flv|webm|m4v");
            case "Audio":
                return extension.matches("mp3|wav|flac|aac|ogg|wma|m4a");
            case "Documents":
                return extension.matches("pdf|doc|docx|xls|xlsx|ppt|pptx|txt|rtf|odt");
            case "Archives":
                return extension.matches("zip|rar|7z|tar|gz|bz2");
            case "Code Files":
                return extension.matches("java|py|js|html|css|cpp|c|h|php|rb|go|rs");
            case "Text Files":
                return extension.matches("txt|md|log|cfg|ini|conf|json|xml|yaml|yml");
            case "Executables":
                return extension.matches("exe|msi|app|deb|rpm|dmg");
            default:
                return true;
        }
    }
    
    /**
     * Apply size filter
     */
    private List<File> applySizeFilter(List<File> files) {
        String selectedSize = sizeFilter.getValue();
        if ("Any Size".equals(selectedSize)) {
            return files;
        }
        
        return files.stream()
            .filter(file -> matchesFileSize(file, selectedSize))
            .collect(Collectors.toList());
    }
    
    /**
     * Check if file matches the selected size range
     */
    private boolean matchesFileSize(File file, String sizeRange) {
        long fileSize = file.length();
        
        switch (sizeRange) {
            case "Empty":
                return fileSize == 0;
            case "Tiny (< 16 KB)":
                return fileSize < 16 * 1024;
            case "Small (< 1 MB)":
                return fileSize < 1024 * 1024;
            case "Medium (1-100 MB)":
                return fileSize >= 1024 * 1024 && fileSize < 100 * 1024 * 1024;
            case "Large (100 MB - 1 GB)":
                return fileSize >= 100 * 1024 * 1024 && fileSize < 1024 * 1024 * 1024;
            case "Huge (> 1 GB)":
                return fileSize >= 1024 * 1024 * 1024;
            default:
                return true;
        }
    }
    
    /**
     * Apply date filter
     */
    private List<File> applyDateFilter(List<File> files) {
        String selectedDate = dateFilter.getValue();
        if ("Any Date".equals(selectedDate)) {
            return files;
        }
        
        return files.stream()
            .filter(file -> matchesDateRange(file, selectedDate))
            .collect(Collectors.toList());
    }
    
    /**
     * Check if file matches the selected date range
     */
    private boolean matchesDateRange(File file, String dateRange) {
        long fileTime = file.lastModified();
        long currentTime = System.currentTimeMillis();
        long dayInMillis = 24 * 60 * 60 * 1000;
        
        switch (dateRange) {
            case "Today":
                return currentTime - fileTime < dayInMillis;
            case "Yesterday":
                return currentTime - fileTime >= dayInMillis && currentTime - fileTime < 2 * dayInMillis;
            case "This Week":
                return currentTime - fileTime < 7 * dayInMillis;
            case "This Month":
                return currentTime - fileTime < 30 * dayInMillis;
            case "This Year":
                return currentTime - fileTime < 365 * dayInMillis;
            case "Older than 1 Year":
                return currentTime - fileTime >= 365 * dayInMillis;
            default:
                return true;
        }
    }
    
    /**
     * Apply hidden files filter
     */
    private List<File> applyHiddenFilesFilter(List<File> files) {
        if (showHiddenFiles.isSelected()) {
            return files;
        }
        
        return files.stream()
            .filter(file -> !file.isHidden() && !file.getName().startsWith("."))
            .collect(Collectors.toList());
    }
    
    /**
     * Apply sorting to the file list
     */
    private List<File> applySorting(List<File> files) {
        String sortBy = sortByFilter.getValue();
        
        Comparator<File> comparator;
        
        switch (sortBy) {
            case "Name (A-Z)":
                comparator = Comparator.comparing(File::getName, String.CASE_INSENSITIVE_ORDER);
                break;
            case "Name (Z-A)":
                comparator = Comparator.comparing(File::getName, String.CASE_INSENSITIVE_ORDER).reversed();
                break;
            case "Size (Smallest)":
                comparator = Comparator.comparingLong(File::length);
                break;
            case "Size (Largest)":
                comparator = Comparator.comparingLong(File::length).reversed();
                break;
            case "Date (Newest)":
                comparator = Comparator.comparingLong(File::lastModified).reversed();
                break;
            case "Date (Oldest)":
                comparator = Comparator.comparingLong(File::lastModified);
                break;
            case "Type (A-Z)":
                comparator = Comparator.comparing(this::getFileType, String.CASE_INSENSITIVE_ORDER);
                break;
            case "Type (Z-A)":
                comparator = Comparator.comparing(this::getFileType, String.CASE_INSENSITIVE_ORDER).reversed();
                break;
            default:
                comparator = Comparator.comparing(File::getName, String.CASE_INSENSITIVE_ORDER);
        }
        
        // Always put directories first
        comparator = Comparator.comparing(File::isDirectory).reversed().thenComparing(comparator);
        
        return files.stream()
            .sorted(comparator)
            .collect(Collectors.toList());
    }
    
    /**
     * Clear all filters and reset to defaults
     */
    private void clearAllFilters() {
        fileTypeFilter.setValue("All Files");
        sizeFilter.setValue("Any Size");
        dateFilter.setValue("Any Date");
        sortByFilter.setValue("Name (A-Z)");
        showHiddenFiles.setSelected(false);
        searchBar.setSearchText("");
        
        applyFilters();
    }
    
    /**
     * Update results display
     */
    private void updateResultsDisplay() {
        int count = filteredFiles.size();
        if (count == 0) {
            resultsCountLabel.setText("No files found");
        } else if (count == 1) {
            resultsCountLabel.setText("1 file");
        } else {
            resultsCountLabel.setText(count + " files");
        }
    }
    
    /**
     * Show search progress
     */
    private void showSearchProgress() {
        searchProgress.setVisible(true);
    }
    
    /**
     * Hide search progress
     */
    private void hideSearchProgress() {
        searchProgress.setVisible(false);
    }
    
    // Helper methods
    
    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return (lastDot > 0 && lastDot < fileName.length() - 1) ? 
               fileName.substring(lastDot + 1) : "";
    }
    
    private String getFileType(File file) {
        if (file.isDirectory()) {
            return "Folder";
        }
        String extension = getFileExtension(file.getName());
        return extension.isEmpty() ? "File" : extension.toUpperCase();
    }
    
    // Getters and Setters
    
    public List<File> getFilteredFiles() {
        return new ArrayList<>(filteredFiles);
    }
    
    public void setOnResultsChanged(java.util.function.Consumer<List<File>> callback) {
        this.onResultsChanged = callback;
    }
    
    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
        if (!searchBar.getSearchText().isEmpty()) {
            applyFilters();
        }
    }
    
    public boolean isCaseSensitive() {
        return caseSensitive;
    }
    
    public void setSearchInContent(boolean searchInContent) {
        this.searchInContent = searchInContent;
    }
    
    public boolean isSearchInContent() {
        return searchInContent;
    }
    
    public void setIncludeSubfolders(boolean includeSubfolders) {
        this.includeSubfolders = includeSubfolders;
    }
    
    public boolean isIncludeSubfolders() {
        return includeSubfolders;
    }
    
    public StringProperty searchTextProperty() {
        return searchText;
    }
    
    public String getSearchText() {
        return searchText.get();
    }
}
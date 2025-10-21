package com.ghostvault.ui.components;

import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Virtualized file list view with efficient scrolling and pagination for large file lists
 */
public class VirtualizedFileListView extends VBox {
    
    // UI Components
    private ListView<File> fileListView;
    private HBox paginationBar;
    private Label itemsInfoLabel;
    private Button firstPageButton;
    private Button prevPageButton;
    private ComboBox<Integer> pageSelector;
    private Button nextPageButton;
    private Button lastPageButton;
    private ComboBox<Integer> itemsPerPageSelector;
    private ProgressIndicator loadingIndicator;
    
    // Data management
    private List<File> allFiles = new ArrayList<>();
    private ObservableList<File> displayedFiles = FXCollections.observableArrayList();
    
    // Pagination properties
    private SimpleIntegerProperty currentPage = new SimpleIntegerProperty(1);
    private SimpleIntegerProperty totalPages = new SimpleIntegerProperty(1);
    private SimpleIntegerProperty itemsPerPage = new SimpleIntegerProperty(100);
    private SimpleIntegerProperty totalItems = new SimpleIntegerProperty(0);
    
    // Selection
    private SimpleObjectProperty<File> selectedFile = new SimpleObjectProperty<>();
    
    // Callbacks
    private Consumer<File> onFileSelected;
    private Consumer<File> onFileDoubleClicked;
    private Runnable onSelectionChanged;
    
    // Configuration
    private boolean virtualFlowEnabled = true;
    private boolean showPagination = true;
    
    public VirtualizedFileListView() {
        initializeComponents();
        setupLayout();
        setupStyling();
        setupEventHandlers();
        setupBindings();
    }
    
    /**
     * Initialize all components
     */
    private void initializeComponents() {
        // File list view with virtual flow
        fileListView = new ListView<>();
        fileListView.setItems(displayedFiles);
        fileListView.getStyleClass().add("virtualized-file-list");
        fileListView.setCellFactory(listView -> new FileListCell());
        
        // Pagination bar
        paginationBar = new HBox(8);
        paginationBar.setAlignment(Pos.CENTER);
        paginationBar.getStyleClass().add("pagination-bar");
        
        // Items info
        itemsInfoLabel = new Label("0 items");
        itemsInfoLabel.getStyleClass().add("items-info-label");
        
        // Navigation buttons
        firstPageButton = new Button("⏮");
        firstPageButton.getStyleClass().addAll("button", "icon", "pagination-button");
        firstPageButton.setTooltip(new Tooltip("First page"));
        
        prevPageButton = new Button("◀");
        prevPageButton.getStyleClass().addAll("button", "icon", "pagination-button");
        prevPageButton.setTooltip(new Tooltip("Previous page"));
        
        nextPageButton = new Button("▶");
        nextPageButton.getStyleClass().addAll("button", "icon", "pagination-button");
        nextPageButton.setTooltip(new Tooltip("Next page"));
        
        lastPageButton = new Button("⏭");
        lastPageButton.getStyleClass().addAll("button", "icon", "pagination-button");
        lastPageButton.setTooltip(new Tooltip("Last page"));
        
        // Page selector
        pageSelector = new ComboBox<>();
        pageSelector.getStyleClass().add("page-selector");
        pageSelector.setEditable(false);
        
        // Items per page selector
        itemsPerPageSelector = new ComboBox<>();
        itemsPerPageSelector.getItems().addAll(25, 50, 100, 200, 500);
        itemsPerPageSelector.setValue(100);
        itemsPerPageSelector.getStyleClass().add("items-per-page-selector");
        
        // Loading indicator
        loadingIndicator = new ProgressIndicator();
        loadingIndicator.getStyleClass().add("list-loading-indicator");
        loadingIndicator.setPrefSize(24, 24);
        loadingIndicator.setVisible(false);
        
        // Spacers
        Region leftSpacer = new Region();
        HBox.setHgrow(leftSpacer, Priority.ALWAYS);
        
        Region rightSpacer = new Region();
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);
        
        paginationBar.getChildren().addAll(
            itemsInfoLabel,
            leftSpacer,
            firstPageButton,
            prevPageButton,
            new Label("Page:"),
            pageSelector,
            nextPageButton,
            lastPageButton,
            rightSpacer,
            new Label("Items per page:"),
            itemsPerPageSelector,
            loadingIndicator
        );
        
        this.getChildren().addAll(fileListView, paginationBar);
    }
    
    /**
     * Setup layout properties
     */
    private void setupLayout() {
        this.setSpacing(8);
        this.setPadding(new Insets(0));
        VBox.setVgrow(fileListView, Priority.ALWAYS);
    }
    
    /**
     * Setup component styling
     */
    private void setupStyling() {
        this.getStyleClass().add("virtualized-file-list-view");
    }
    
    /**
     * Setup event handlers
     */
    private void setupEventHandlers() {
        // File selection
        fileListView.getSelectionModel().selectedItemProperty().addListener((obs, oldFile, newFile) -> {
            selectedFile.set(newFile);
            if (onFileSelected != null && newFile != null) {
                onFileSelected.accept(newFile);
            }
            if (onSelectionChanged != null) {
                onSelectionChanged.run();
            }
        });
        
        // Double-click handling
        fileListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                File selected = fileListView.getSelectionModel().getSelectedItem();
                if (selected != null && onFileDoubleClicked != null) {
                    onFileDoubleClicked.accept(selected);
                }
            }
        });
        
        // Pagination controls
        firstPageButton.setOnAction(e -> goToPage(1));
        prevPageButton.setOnAction(e -> goToPage(currentPage.get() - 1));
        nextPageButton.setOnAction(e -> goToPage(currentPage.get() + 1));
        lastPageButton.setOnAction(e -> goToPage(totalPages.get()));
        
        pageSelector.setOnAction(e -> {
            Integer selectedPage = pageSelector.getValue();
            if (selectedPage != null) {
                goToPage(selectedPage);
            }
        });
        
        itemsPerPageSelector.setOnAction(e -> {
            Integer newItemsPerPage = itemsPerPageSelector.getValue();
            if (newItemsPerPage != null) {
                setItemsPerPage(newItemsPerPage);
            }
        });
    }
    
    /**
     * Setup property bindings
     */
    private void setupBindings() {
        // Update pagination controls based on current state
        currentPage.addListener((obs, oldPage, newPage) -> updatePaginationControls());
        totalPages.addListener((obs, oldPages, newPages) -> updatePaginationControls());
        totalItems.addListener((obs, oldCount, newCount) -> updateItemsInfo());
        
        // Update items per page
        itemsPerPage.addListener((obs, oldCount, newCount) -> {
            if (newCount.intValue() != oldCount.intValue()) {
                updatePagination();
            }
        });
    }
    
    /**
     * Set the list of files to display
     */
    public void setFiles(List<File> files) {
        this.allFiles = new ArrayList<>(files);
        this.totalItems.set(files.size());
        updatePagination();
    }
    
    /**
     * Update pagination and refresh display
     */
    private void updatePagination() {
        showLoadingIndicator();
        
        Task<Void> paginationTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                // Calculate pagination
                int itemCount = allFiles.size();
                int itemsPerPageValue = itemsPerPage.get();
                int totalPagesValue = (int) Math.ceil((double) itemCount / itemsPerPageValue);
                
                Platform.runLater(() -> {
                    totalPages.set(Math.max(1, totalPagesValue));
                    
                    // Ensure current page is valid
                    if (currentPage.get() > totalPages.get()) {
                        currentPage.set(totalPages.get());
                    }
                    
                    updatePageSelector();
                    loadCurrentPage();
                });
                
                return null;
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> hideLoadingIndicator());
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> hideLoadingIndicator());
            }
        };
        
        Thread paginationThread = new Thread(paginationTask);
        paginationThread.setDaemon(true);
        paginationThread.start();
    }
    
    /**
     * Load and display the current page
     */
    private void loadCurrentPage() {
        showLoadingIndicator();
        
        Task<List<File>> loadTask = new Task<List<File>>() {
            @Override
            protected List<File> call() throws Exception {
                int pageNum = currentPage.get();
                int itemsPerPageValue = itemsPerPage.get();
                int startIndex = (pageNum - 1) * itemsPerPageValue;
                int endIndex = Math.min(startIndex + itemsPerPageValue, allFiles.size());
                
                if (startIndex >= allFiles.size()) {
                    return new ArrayList<>();
                }
                
                return new ArrayList<>(allFiles.subList(startIndex, endIndex));
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    displayedFiles.setAll(getValue());
                    hideLoadingIndicator();
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    hideLoadingIndicator();
                });
            }
        };
        
        Thread loadThread = new Thread(loadTask);
        loadThread.setDaemon(true);
        loadThread.start();
    }
    
    /**
     * Go to specific page
     */
    public void goToPage(int page) {
        if (page >= 1 && page <= totalPages.get()) {
            currentPage.set(page);
            loadCurrentPage();
        }
    }
    
    /**
     * Set items per page
     */
    public void setItemsPerPage(int items) {
        itemsPerPage.set(items);
    }
    
    /**
     * Update pagination controls
     */
    private void updatePaginationControls() {
        int currentPageValue = currentPage.get();
        int totalPagesValue = totalPages.get();
        
        // Update button states
        firstPageButton.setDisable(currentPageValue <= 1);
        prevPageButton.setDisable(currentPageValue <= 1);
        nextPageButton.setDisable(currentPageValue >= totalPagesValue);
        lastPageButton.setDisable(currentPageValue >= totalPagesValue);
        
        // Update page selector
        if (pageSelector.getValue() == null || !pageSelector.getValue().equals(currentPageValue)) {
            pageSelector.setValue(currentPageValue);
        }
    }
    
    /**
     * Update page selector items
     */
    private void updatePageSelector() {
        pageSelector.getItems().clear();
        for (int i = 1; i <= totalPages.get(); i++) {
            pageSelector.getItems().add(i);
        }
        pageSelector.setValue(currentPage.get());
    }
    
    /**
     * Update items info label
     */
    private void updateItemsInfo() {
        int total = totalItems.get();
        int currentPageValue = currentPage.get();
        int itemsPerPageValue = itemsPerPage.get();
        int startIndex = (currentPageValue - 1) * itemsPerPageValue + 1;
        int endIndex = Math.min(currentPageValue * itemsPerPageValue, total);
        
        if (total == 0) {
            itemsInfoLabel.setText("No items");
        } else {
            itemsInfoLabel.setText(String.format("Showing %d-%d of %d items", startIndex, endIndex, total));
        }
    }
    
    /**
     * Show loading indicator
     */
    private void showLoadingIndicator() {
        loadingIndicator.setVisible(true);
    }
    
    /**
     * Hide loading indicator
     */
    private void hideLoadingIndicator() {
        loadingIndicator.setVisible(false);
    }
    
    /**
     * Custom list cell for files
     */
    private class FileListCell extends ListCell<File> {
        private HBox content;
        private ImageView iconView;
        private VBox textContainer;
        private Label nameLabel;
        private Label detailsLabel;
        
        public FileListCell() {
            createContent();
        }
        
        private void createContent() {
            content = new HBox(8);
            content.setAlignment(Pos.CENTER_LEFT);
            content.setPadding(new Insets(4, 8, 4, 8));
            content.getStyleClass().add("file-list-cell-content");
            
            iconView = new ImageView();
            iconView.setFitWidth(24);
            iconView.setFitHeight(24);
            iconView.setPreserveRatio(true);
            
            textContainer = new VBox(2);
            textContainer.setAlignment(Pos.CENTER_LEFT);
            
            nameLabel = new Label();
            nameLabel.getStyleClass().add("file-name-label");
            
            detailsLabel = new Label();
            detailsLabel.getStyleClass().add("file-details-label");
            
            textContainer.getChildren().addAll(nameLabel, detailsLabel);
            content.getChildren().addAll(iconView, textContainer);
        }
        
        @Override
        protected void updateItem(File file, boolean empty) {
            super.updateItem(file, empty);
            
            if (empty || file == null) {
                setGraphic(null);
            } else {
                nameLabel.setText(file.getName());
                
                if (file.isDirectory()) {
                    File[] children = file.listFiles();
                    int itemCount = children != null ? children.length : 0;
                    detailsLabel.setText(itemCount + " items");
                } else {
                    detailsLabel.setText(formatFileSize(file.length()) + " • " + 
                                       formatDate(file.lastModified()));
                }
                
                // Set icon
                iconView.setImage(FileIconProvider.getFileIconImage(file));
                
                setGraphic(content);
            }
        }
        
        private String formatFileSize(long bytes) {
            if (bytes < 1024) return bytes + " B";
            if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
            if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
            return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
        }
        
        private String formatDate(long timestamp) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy");
            return sdf.format(new java.util.Date(timestamp));
        }
    }
    
    // Getters and Setters
    
    public void setOnFileSelected(Consumer<File> callback) {
        this.onFileSelected = callback;
    }
    
    public void setOnFileDoubleClicked(Consumer<File> callback) {
        this.onFileDoubleClicked = callback;
    }
    
    public void setOnSelectionChanged(Runnable callback) {
        this.onSelectionChanged = callback;
    }
    
    public File getSelectedFile() {
        return selectedFile.get();
    }
    
    public SimpleObjectProperty<File> selectedFileProperty() {
        return selectedFile;
    }
    
    public int getCurrentPage() {
        return currentPage.get();
    }
    
    public SimpleIntegerProperty currentPageProperty() {
        return currentPage;
    }
    
    public int getTotalPages() {
        return totalPages.get();
    }
    
    public SimpleIntegerProperty totalPagesProperty() {
        return totalPages;
    }
    
    public int getItemsPerPage() {
        return itemsPerPage.get();
    }
    
    public SimpleIntegerProperty itemsPerPageProperty() {
        return itemsPerPage;
    }
    
    public int getTotalItems() {
        return totalItems.get();
    }
    
    public SimpleIntegerProperty totalItemsProperty() {
        return totalItems;
    }
    
    public void setShowPagination(boolean show) {
        this.showPagination = show;
        paginationBar.setVisible(show);
        paginationBar.setManaged(show);
    }
    
    public boolean isShowPagination() {
        return showPagination;
    }
    
    public void setVirtualFlowEnabled(boolean enabled) {
        this.virtualFlowEnabled = enabled;
        // Virtual flow is enabled by default in ListView
    }
    
    public boolean isVirtualFlowEnabled() {
        return virtualFlowEnabled;
    }
    
    /**
     * Refresh the current page
     */
    public void refresh() {
        loadCurrentPage();
    }
    
    /**
     * Clear all files
     */
    public void clear() {
        allFiles.clear();
        displayedFiles.clear();
        totalItems.set(0);
        currentPage.set(1);
        totalPages.set(1);
        updatePaginationControls();
        updateItemsInfo();
    }
    
    /**
     * Get the selection model for this list view
     */
    public javafx.scene.control.MultipleSelectionModel<File> getSelectionModel() {
        return fileListView.getSelectionModel();
    }
    
    /**
     * Select all files
     */
    public void selectAll() {
        fileListView.getSelectionModel().selectAll();
    }
    
    /**
     * Clear selection
     */
    public void clearSelection() {
        fileListView.getSelectionModel().clearSelection();
    }
}
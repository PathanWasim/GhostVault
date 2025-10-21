package com.ghostvault.ui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.util.function.Consumer;

/**
 * Modern search bar component
 */
public class ModernSearchBar extends HBox {
    
    private TextField searchField;
    private Button searchButton;
    private Button clearButton;
    private ComboBox<String> filterComboBox;
    
    private Consumer<String> onSearch;
    private Consumer<String> onFilterChanged;
    
    public ModernSearchBar() {
        super();
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        applyStyles();
    }
    
    private void initializeComponents() {
        searchField = new TextField();
        searchField.setPromptText("Search files...");
        
        searchButton = new Button("🔍");
        searchButton.setTooltip(new Tooltip("Search"));
        
        clearButton = new Button("✕");
        clearButton.setTooltip(new Tooltip("Clear search"));
        clearButton.setVisible(false);
        
        filterComboBox = new ComboBox<>();
        filterComboBox.getItems().addAll("All Files", "Images", "Documents", "Videos", "Audio");
        filterComboBox.setValue("All Files");
        filterComboBox.setTooltip(new Tooltip("Filter by file type"));
    }
    
    private void setupLayout() {
        this.setSpacing(5);
        this.setAlignment(Pos.CENTER_LEFT);
        this.setPadding(new Insets(5));
        
        HBox searchBox = new HBox(2);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.getChildren().addAll(searchField, clearButton, searchButton);
        
        this.getChildren().addAll(searchBox, filterComboBox);
        HBox.setHgrow(searchBox, Priority.ALWAYS);
        HBox.setHgrow(searchField, Priority.ALWAYS);
    }
    
    private void setupEventHandlers() {
        searchButton.setOnAction(e -> performSearch());
        
        searchField.setOnAction(e -> performSearch());
        
        searchField.textProperty().addListener((obs, oldText, newText) -> {
            clearButton.setVisible(!newText.isEmpty());
            if (newText.isEmpty() && onSearch != null) {
                onSearch.accept("");
            }
        });
        
        clearButton.setOnAction(e -> {
            searchField.clear();
            if (onSearch != null) {
                onSearch.accept("");
            }
        });
        
        filterComboBox.setOnAction(e -> {
            if (onFilterChanged != null) {
                onFilterChanged.accept(filterComboBox.getValue());
            }
        });
    }
    
    private void applyStyles() {
        this.getStyleClass().add("modern-search-bar");
        
        this.setStyle(
            "-fx-background-color: #f8f8f8;" +
            "-fx-border-color: #e0e0e0;" +
            "-fx-border-width: 0 0 1px 0;"
        );
        
        searchField.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: #ddd;" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 4px;" +
            "-fx-background-radius: 4px;" +
            "-fx-padding: 6px;"
        );
        
        searchButton.setStyle(
            "-fx-background-color: #4CAF50;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 4px;" +
            "-fx-border-radius: 4px;" +
            "-fx-padding: 6px 12px;"
        );
        
        clearButton.setStyle(
            "-fx-background-color: #f44336;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 4px;" +
            "-fx-border-radius: 4px;" +
            "-fx-padding: 6px 8px;"
        );
    }
    
    private void performSearch() {
        String searchText = searchField.getText().trim();
        if (onSearch != null) {
            onSearch.accept(searchText);
        }
    }
    
    public void setOnSearch(Consumer<String> callback) {
        this.onSearch = callback;
    }
    
    public void setOnFilterChanged(Consumer<String> callback) {
        this.onFilterChanged = callback;
    }
    
    public String getSearchText() {
        return searchField.getText();
    }
    
    public void setSearchText(String text) {
        searchField.setText(text);
    }
    
    public String getCurrentFilter() {
        return filterComboBox.getValue();
    }
    
    public void setCurrentFilter(String filter) {
        filterComboBox.setValue(filter);
    }
    
    public void clear() {
        searchField.clear();
        filterComboBox.setValue("All Files");
    }
    
    // Additional methods for compatibility with FileSearchAndFilterSystem
    public void setRealTimeSearch(boolean realTime) {
        // Implementation for real-time search
    }
    
    public void setMinSearchLength(int length) {
        // Implementation for minimum search length
    }
    
    public void setSearchDelay(int delay) {
        // Implementation for search delay
    }
    
    public void setOnSearchResults(Consumer<java.util.List<Object>> callback) {
        // Implementation for search results callback
    }
    
    public void setOnSearchTextChanged(Consumer<String> callback) {
        // Set the existing onSearch callback
        setOnSearch(callback);
    }
    
    public javafx.beans.property.StringProperty searchTextProperty() {
        return searchField.textProperty();
    }
    
    public void setItems(java.util.List<?> items) {
        // Implementation for setting searchable items
    }
}
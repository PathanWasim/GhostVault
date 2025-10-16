package com.ghostvault.ui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.util.function.Consumer;

/**
 * Toolbar for file sorting controls
 */
public class FileSortingToolbar extends HBox {
    
    private ComboBox<EnhancedFileListView.SortCriteria> sortComboBox;
    private Button sortOrderButton;
    private Label sortStatusLabel;
    private Consumer<EnhancedFileListView.SortCriteria> onSortCriteriaChanged;
    private Runnable onSortOrderToggled;
    
    private boolean ascending = true;
    
    public FileSortingToolbar() {
        super();
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        applyStyles();
    }
    
    private void initializeComponents() {
        sortComboBox = new ComboBox<>();
        sortComboBox.getItems().addAll(EnhancedFileListView.SortCriteria.values());
        sortComboBox.setValue(EnhancedFileListView.SortCriteria.NAME);
        sortComboBox.setPromptText("Sort by...");
        
        sortOrderButton = new Button("↑");
        sortOrderButton.setTooltip(new Tooltip("Toggle sort order"));
        sortOrderButton.setPrefSize(30, 30);
        
        sortStatusLabel = new Label("Sorted by Name (A-Z)");
        sortStatusLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");
    }
    
    private void setupLayout() {
        this.setSpacing(10);
        this.setAlignment(Pos.CENTER_LEFT);
        this.setPadding(new Insets(5, 10, 5, 10));
        
        Label sortLabel = new Label("Sort:");
        sortLabel.setStyle("-fx-font-weight: bold;");
        
        this.getChildren().addAll(
            sortLabel,
            sortComboBox,
            sortOrderButton,
            sortStatusLabel
        );
        
        HBox.setHgrow(sortStatusLabel, Priority.ALWAYS);
    }
    
    private void setupEventHandlers() {
        sortComboBox.setOnAction(e -> {
            EnhancedFileListView.SortCriteria criteria = sortComboBox.getValue();
            if (criteria != null && onSortCriteriaChanged != null) {
                onSortCriteriaChanged.accept(criteria);
                updateSortStatus();
            }
        });
        
        sortOrderButton.setOnAction(e -> {
            toggleSortOrder();
            if (onSortOrderToggled != null) {
                onSortOrderToggled.run();
            }
            updateSortStatus();
        });
    }
    
    private void applyStyles() {
        this.getStyleClass().add("file-sorting-toolbar");
        
        this.setStyle(
            "-fx-background-color: #f8f8f8;" +
            "-fx-border-color: #e0e0e0;" +
            "-fx-border-width: 0 0 1px 0;"
        );
        
        sortComboBox.getStyleClass().add("sort-combo-box");
        sortOrderButton.getStyleClass().add("sort-order-button");
        sortStatusLabel.getStyleClass().add("sort-status-label");
    }
    
    /**
     * Toggle sort order between ascending and descending
     */
    private void toggleSortOrder() {
        ascending = !ascending;
        sortOrderButton.setText(ascending ? "↑" : "↓");
        sortOrderButton.setTooltip(new Tooltip(ascending ? "Sort descending" : "Sort ascending"));
    }
    
    /**
     * Update the sort status label
     */
    private void updateSortStatus() {
        EnhancedFileListView.SortCriteria criteria = sortComboBox.getValue();
        if (criteria != null) {
            String criteriaName = EnhancedFileListView.getSortCriteriaDisplayName(criteria);
            String orderText = ascending ? "A-Z" : "Z-A";
            
            // Adjust order text based on criteria
            switch (criteria) {
                case SIZE:
                    orderText = ascending ? "Small to Large" : "Large to Small";
                    break;
                case DATE:
                    orderText = ascending ? "Oldest First" : "Newest First";
                    break;
                case TYPE:
                    orderText = ascending ? "A-Z" : "Z-A";
                    break;
            }
            
            sortStatusLabel.setText(String.format("Sorted by %s (%s)", criteriaName, orderText));
        }
    }
    
    /**
     * Set the current sort criteria
     */
    public void setSortCriteria(EnhancedFileListView.SortCriteria criteria) {
        sortComboBox.setValue(criteria);
        updateSortStatus();
    }
    
    /**
     * Set the current sort order
     */
    public void setSortAscending(boolean ascending) {
        this.ascending = ascending;
        sortOrderButton.setText(ascending ? "↑" : "↓");
        sortOrderButton.setTooltip(new Tooltip(ascending ? "Sort descending" : "Sort ascending"));
        updateSortStatus();
    }
    
    /**
     * Set callback for sort criteria changes
     */
    public void setOnSortCriteriaChanged(Consumer<EnhancedFileListView.SortCriteria> callback) {
        this.onSortCriteriaChanged = callback;
    }
    
    /**
     * Set callback for sort order toggle
     */
    public void setOnSortOrderToggled(Runnable callback) {
        this.onSortOrderToggled = callback;
    }
    
    /**
     * Get current sort criteria
     */
    public EnhancedFileListView.SortCriteria getCurrentSortCriteria() {
        return sortComboBox.getValue();
    }
    
    /**
     * Check if currently sorting in ascending order
     */
    public boolean isSortAscending() {
        return ascending;
    }
    
    /**
     * Update display to reflect external sort changes
     */
    public void updateFromFileList(EnhancedFileListView fileList) {
        setSortCriteria(fileList.getCurrentSortCriteria());
        setSortAscending(fileList.isSortAscending());
    }
}
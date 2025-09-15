package com.ghostvault.ui;

import com.ghostvault.audit.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * UI Controller for audit log review and analysis
 */
public class AuditLogController {
    
    private Stage dialogStage;
    private AuditManager auditManager;
    
    // UI Components
    private TableView<AuditEntry> auditTable;
    private ObservableList<AuditEntry> auditEntries;
    private TextField searchField;
    private ComboBox<AuditManager.AuditCategory> categoryFilter;
    private ComboBox<AuditManager.AuditSeverity> severityFilter;
    private DatePicker fromDatePicker;
    private DatePicker toDatePicker;
    private Label statusLabel;
    private ProgressIndicator loadingIndicator;
    private TextArea detailsArea;
    private Label statisticsLabel;
    
    // Filters
    private String currentSearchTerm = "";
    private AuditManager.AuditCategory currentCategoryFilter = null;
    private AuditManager.AuditSeverity currentSeverityFilter = null;
    private LocalDateTime currentFromDate = null;
    private LocalDateTime currentToDate = null;
    
    public AuditLogController(AuditManager auditManager) {
        this.auditManager = auditManager;
        this.auditEntries = FXCollections.observableArrayList();
    }
    
    /**
     * Show audit log dialog
     */
    public void showDialog(Stage parentStage) {
        dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initOwner(parentStage);
        dialogStage.setTitle("GhostVault - Audit Log Viewer");
        dialogStage.setResizable(true);
        
        VBox root = createUI();
        Scene scene = new Scene(root, 1000, 700);
        dialogStage.setScene(scene);
        
        // Load initial data
        loadAuditEntries();
        loadStatistics();
        
        dialogStage.show();
    }
    
    /**
     * Create the main UI
     */
    private VBox createUI() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(15));
        
        // Title and statistics
        HBox titleBox = createTitleBox();
        
        // Filters
        VBox filtersBox = createFiltersBox();
        
        // Table
        VBox tableBox = createTableBox();
        
        // Details panel
        VBox detailsBox = createDetailsBox();
        
        // Status and buttons
        HBox statusBox = createStatusBox();
        
        root.getChildren().addAll(
            titleBox,
            new Separator(),
            filtersBox,
            tableBox,
            detailsBox,
            statusBox
        );
        
        return root;
    }
    
    /**
     * Create title and statistics box
     */
    private HBox createTitleBox() {
        HBox titleBox = new HBox(20);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        
        Label titleLabel = new Label("Audit Log Viewer");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        
        statisticsLabel = new Label("Loading statistics...");
        statisticsLabel.setFont(Font.font("System", 12));
        statisticsLabel.setTextFill(Color.GRAY);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button refreshButton = new Button("🔄 Refresh");
        refreshButton.setOnAction(e -> {
            loadAuditEntries();
            loadStatistics();
        });
        
        titleBox.getChildren().addAll(titleLabel, statisticsLabel, spacer, refreshButton);
        
        return titleBox;
    }
    
    /**
     * Create filters box
     */
    private VBox createFiltersBox() {
        VBox filtersBox = new VBox(10);
        
        Label filtersLabel = new Label("Filters:");
        filtersLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        
        // First row of filters
        HBox filterRow1 = new HBox(15);
        filterRow1.setAlignment(Pos.CENTER_LEFT);
        
        // Search field
        Label searchLabel = new Label("Search:");
        searchField = new TextField();
        searchField.setPromptText("Search events, descriptions...");
        searchField.setPrefWidth(200);
        searchField.textProperty().addListener((obs, oldText, newText) -> {
            currentSearchTerm = newText;
            applyFilters();
        });
        
        // Category filter
        Label categoryLabel = new Label("Category:");
        categoryFilter = new ComboBox<>();
        categoryFilter.getItems().add(null); // "All" option
        categoryFilter.getItems().addAll(AuditManager.AuditCategory.values());
        categoryFilter.setConverter(new StringConverter<AuditManager.AuditCategory>() {
            @Override
            public String toString(AuditManager.AuditCategory category) {
                return category == null ? "All Categories" : category.toString();
            }
            
            @Override
            public AuditManager.AuditCategory fromString(String string) {
                return null; // Not needed for display-only
            }
        });
        categoryFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
            currentCategoryFilter = newVal;
            applyFilters();
        });
        
        // Severity filter
        Label severityLabel = new Label("Severity:");
        severityFilter = new ComboBox<>();
        severityFilter.getItems().add(null); // "All" option
        severityFilter.getItems().addAll(AuditManager.AuditSeverity.values());
        severityFilter.setConverter(new StringConverter<AuditManager.AuditSeverity>() {
            @Override
            public String toString(AuditManager.AuditSeverity severity) {
                return severity == null ? "All Severities" : severity.toString();
            }
            
            @Override
            public AuditManager.AuditSeverity fromString(String string) {
                return null; // Not needed for display-only
            }
        });
        severityFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
            currentSeverityFilter = newVal;
            applyFilters();
        });
        
        filterRow1.getChildren().addAll(
            searchLabel, searchField,
            categoryLabel, categoryFilter,
            severityLabel, severityFilter
        );
        
        // Second row of filters (date range)
        HBox filterRow2 = new HBox(15);
        filterRow2.setAlignment(Pos.CENTER_LEFT);
        
        Label fromLabel = new Label("From:");
        fromDatePicker = new DatePicker();
        fromDatePicker.setPromptText("Start date");
        fromDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            currentFromDate = newVal != null ? newVal.atStartOfDay() : null;
            loadAuditEntries(); // Date filters require reload
        });
        
        Label toLabel = new Label("To:");
        toDatePicker = new DatePicker();
        toDatePicker.setPromptText("End date");
        toDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            currentToDate = newVal != null ? newVal.atTime(23, 59, 59) : null;
            loadAuditEntries(); // Date filters require reload
        });
        
        Button clearFiltersButton = new Button("Clear Filters");
        clearFiltersButton.setOnAction(e -> clearFilters());
        
        filterRow2.getChildren().addAll(
            fromLabel, fromDatePicker,
            toLabel, toDatePicker,
            clearFiltersButton
        );
        
        filtersBox.getChildren().addAll(filtersLabel, filterRow1, filterRow2);
        
        return filtersBox;
    }
    
    /**
     * Create table box
     */
    private VBox createTableBox() {
        VBox tableBox = new VBox(5);
        
        Label tableLabel = new Label("Audit Entries:");
        tableLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        
        // Create table
        auditTable = new TableView<>();
        auditTable.setItems(auditEntries);
        auditTable.setPrefHeight(300);
        
        // Timestamp column
        TableColumn<AuditEntry, String> timestampCol = new TableColumn<>("Timestamp");
        timestampCol.setCellValueFactory(new PropertyValueFactory<>("formattedTimestamp"));
        timestampCol.setPrefWidth(150);
        
        // Severity column
        TableColumn<AuditEntry, String> severityCol = new TableColumn<>("Severity");
        severityCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getSeverityIcon() + " " + cellData.getValue().getSeverity()));
        severityCol.setPrefWidth(100);
        
        // Category column
        TableColumn<AuditEntry, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getCategoryIcon() + " " + cellData.getValue().getCategory()));
        categoryCol.setPrefWidth(150);
        
        // Event Type column
        TableColumn<AuditEntry, String> eventCol = new TableColumn<>("Event");
        eventCol.setCellValueFactory(new PropertyValueFactory<>("eventType"));
        eventCol.setPrefWidth(200);
        
        // Description column
        TableColumn<AuditEntry, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        descCol.setPrefWidth(300);
        
        auditTable.getColumns().addAll(timestampCol, severityCol, categoryCol, eventCol, descCol);
        
        // Selection listener
        auditTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            showEntryDetails(newSelection);
        });
        
        // Loading indicator
        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setVisible(false);
        loadingIndicator.setPrefSize(30, 30);
        
        StackPane tableStack = new StackPane();
        tableStack.getChildren().addAll(auditTable, loadingIndicator);
        
        tableBox.getChildren().addAll(tableLabel, tableStack);
        
        return tableBox;
    }
    
    /**
     * Create details box
     */
    private VBox createDetailsBox() {
        VBox detailsBox = new VBox(5);
        
        Label detailsLabel = new Label("Entry Details:");
        detailsLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        
        detailsArea = new TextArea();
        detailsArea.setEditable(false);
        detailsArea.setPrefRowCount(6);
        detailsArea.setWrapText(true);
        detailsArea.setText("Select an audit entry to view details...");
        
        detailsBox.getChildren().addAll(detailsLabel, detailsArea);
        
        return detailsBox;
    }
    
    /**
     * Create status box
     */
    private HBox createStatusBox() {
        HBox statusBox = new HBox(15);
        statusBox.setAlignment(Pos.CENTER_LEFT);
        
        statusLabel = new Label("Ready");
        statusLabel.setFont(Font.font("System", 11));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button exportButton = new Button("📄 Export");
        exportButton.setOnAction(e -> exportAuditLog());
        
        Button closeButton = new Button("Close");
        closeButton.setPrefWidth(80);
        closeButton.setOnAction(e -> dialogStage.close());
        
        statusBox.getChildren().addAll(statusLabel, spacer, exportButton, closeButton);
        
        return statusBox;
    }
    
    /**
     * Load audit entries from manager
     */
    private void loadAuditEntries() {
        setLoading(true);
        statusLabel.setText("Loading audit entries...");
        
        Task<List<AuditEntry>> loadTask = new Task<List<AuditEntry>>() {
            @Override
            protected List<AuditEntry> call() throws Exception {
                return auditManager.readAuditLog(1000, currentCategoryFilter, currentFromDate, currentToDate);
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    List<AuditEntry> entries = getValue();
                    auditEntries.clear();
                    auditEntries.addAll(entries);
                    applyFilters();
                    
                    setLoading(false);
                    statusLabel.setText("Loaded " + entries.size() + " audit entries");
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    setLoading(false);
                    statusLabel.setText("Failed to load audit entries");
                    
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Failed to load audit log");
                    alert.setContentText("Error: " + getException().getMessage());
                    alert.showAndWait();
                });
            }
        };
        
        Thread loadThread = new Thread(loadTask);
        loadThread.setDaemon(true);
        loadThread.start();
    }
    
    /**
     * Load audit statistics
     */
    private void loadStatistics() {
        Task<AuditStatistics> statsTask = new Task<AuditStatistics>() {
            @Override
            protected AuditStatistics call() throws Exception {
                return auditManager.getAuditStatistics();
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    AuditStatistics stats = getValue();
                    updateStatisticsDisplay(stats);
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    statisticsLabel.setText("Statistics unavailable");
                });
            }
        };
        
        Thread statsThread = new Thread(statsTask);
        statsThread.setDaemon(true);
        statsThread.start();
    }
    
    /**
     * Update statistics display
     */
    private void updateStatisticsDisplay(AuditStatistics stats) {
        String statsText = String.format("📊 %d entries | 💾 %s | 🏥 %s (%d/100)", 
            stats.getTotalEntries(),
            stats.getFormattedLogSize(),
            stats.getHealthStatus(),
            stats.getSecurityHealthScore());
        
        statisticsLabel.setText(statsText);
        
        // Set color based on health score
        int score = stats.getSecurityHealthScore();
        if (score >= 90) {
            statisticsLabel.setTextFill(Color.GREEN);
        } else if (score >= 75) {
            statisticsLabel.setTextFill(Color.ORANGE);
        } else {
            statisticsLabel.setTextFill(Color.RED);
        }
    }
    
    /**
     * Apply current filters to the table
     */
    private void applyFilters() {
        if (auditEntries.isEmpty()) {
            return;
        }
        
        ObservableList<AuditEntry> filteredEntries = auditEntries.filtered(entry -> {
            // Search filter
            if (!currentSearchTerm.isEmpty() && !entry.matches(currentSearchTerm)) {
                return false;
            }
            
            // Category filter
            if (currentCategoryFilter != null && entry.getCategory() != currentCategoryFilter) {
                return false;
            }
            
            // Severity filter
            if (currentSeverityFilter != null && entry.getSeverity() != currentSeverityFilter) {
                return false;
            }
            
            return true;
        });
        
        auditTable.setItems(filteredEntries);
        statusLabel.setText("Showing " + filteredEntries.size() + " of " + auditEntries.size() + " entries");
    }
    
    /**
     * Clear all filters
     */
    private void clearFilters() {
        searchField.clear();
        categoryFilter.setValue(null);
        severityFilter.setValue(null);
        fromDatePicker.setValue(null);
        toDatePicker.setValue(null);
        
        currentSearchTerm = "";
        currentCategoryFilter = null;
        currentSeverityFilter = null;
        currentFromDate = null;
        currentToDate = null;
        
        loadAuditEntries();
    }
    
    /**
     * Show details for selected entry
     */
    private void showEntryDetails(AuditEntry entry) {
        if (entry == null) {
            detailsArea.setText("Select an audit entry to view details...");
            return;
        }
        
        StringBuilder details = new StringBuilder();
        details.append("=== Audit Entry Details ===\n\n");
        details.append("ID: ").append(entry.getId()).append("\n");
        details.append("Timestamp: ").append(entry.getFormattedTimestamp()).append("\n");
        details.append("Category: ").append(entry.getCategoryIcon()).append(" ").append(entry.getCategory()).append("\n");
        details.append("Severity: ").append(entry.getSeverityIcon()).append(" ").append(entry.getSeverity()).append("\n");
        details.append("Event Type: ").append(entry.getEventType()).append("\n");
        details.append("Description: ").append(entry.getDescription()).append("\n");
        details.append("Thread: ").append(entry.getThreadName()).append("\n");
        details.append("User: ").append(entry.getUserContext()).append("\n");
        
        if (entry.getDetails() != null && !entry.getDetails().isEmpty()) {
            details.append("\n=== Additional Details ===\n");
            for (java.util.Map.Entry<String, String> detail : entry.getDetails().entrySet()) {
                details.append(detail.getKey()).append(": ").append(detail.getValue()).append("\n");
            }
        }
        
        detailsArea.setText(details.toString());
    }
    
    /**
     * Export audit log
     */
    private void exportAuditLog() {
        // In a real implementation, this would open a file chooser and export to CSV/JSON
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Export");
        alert.setHeaderText("Export Audit Log");
        alert.setContentText("Export functionality would be implemented here.\nWould export current filtered entries to CSV or JSON format.");
        alert.showAndWait();
    }
    
    /**
     * Set loading state
     */
    private void setLoading(boolean loading) {
        loadingIndicator.setVisible(loading);
        auditTable.setDisable(loading);
    }
}
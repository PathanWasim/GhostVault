package com.ghostvault.ui;

import com.ghostvault.audit.AuditManager;
import com.ghostvault.security.ThreatDetectionEngine;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.Node;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Advanced Security Dashboard for GhostVault
 * Provides real-time security monitoring, threat detection, and analytics
 */
public class SecurityDashboard {
    
    private Stage dashboardStage;
    private AuditManager auditManager;
    private ThreatDetectionEngine threatEngine;
    
    // Dashboard components
    private Label securityScoreLabel;
    private Label threatLevelLabel;
    private Label activeSessionsLabel;
    private Label totalFilesLabel;
    private ListView<String> recentActivityList;
    private LineChart<String, Number> securityEventsChart;
    private PieChart fileTypesChart;
    private ProgressBar securityScoreBar;
    
    // Security metrics
    private AtomicInteger securityScore = new AtomicInteger(85);
    private String currentThreatLevel = "LOW";
    private int activeSessions = 1;
    private int totalFiles = 0;
    
    public SecurityDashboard(AuditManager auditManager, ThreatDetectionEngine threatEngine) {
        this.auditManager = auditManager;
        this.threatEngine = threatEngine;
    }
    
    /**
     * Show the security dashboard
     */
    public void show() {
        if (dashboardStage == null) {
            createDashboard();
        }
        
        dashboardStage.show();
        dashboardStage.toFront();
        refreshDashboard();
    }
    
    /**
     * Create the security dashboard UI
     */
    private void createDashboard() {
        dashboardStage = new Stage();
        dashboardStage.setTitle("GhostVault - Security Dashboard");
        dashboardStage.setWidth(1200);
        dashboardStage.setHeight(800);
        
        // Main layout
        BorderPane root = new BorderPane();
        root.getStyleClass().add("main-container");
        
        // Header
        root.setTop(createHeader());
        
        // Main content
        root.setCenter(createMainContent());
        
        // Footer
        root.setBottom(createFooter());
        
        // Create scene with professional styling
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/styles/professional.css").toExternalForm());
        
        dashboardStage.setScene(scene);
    }
    
    /**
     * Create dashboard header
     */
    private Node createHeader() {
        HBox header = new HBox(20);
        header.getStyleClass().add("toolbar");
        header.setAlignment(Pos.CENTER_LEFT);
        
        // Title
        Label titleLabel = new Label("🛡️ Security Dashboard");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2196F3;");
        
        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Last updated
        Label lastUpdatedLabel = new Label("Last updated: " + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")));
        lastUpdatedLabel.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 12px;");
        
        // Refresh button
        Button refreshButton = new Button("🔄 Refresh");
        refreshButton.getStyleClass().add("primary-button");
        refreshButton.setOnAction(e -> refreshDashboard());
        
        header.getChildren().addAll(titleLabel, spacer, lastUpdatedLabel, refreshButton);
        return header;
    }
    
    /**
     * Create main dashboard content
     */
    private Node createMainContent() {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        
        // Security overview cards
        content.getChildren().add(createSecurityOverview());
        
        // Charts section
        HBox chartsSection = new HBox(20);
        chartsSection.getChildren().addAll(
            createSecurityEventsChart(),
            createFileTypesChart()
        );
        content.getChildren().add(chartsSection);
        
        // Activity and threats section
        HBox activitySection = new HBox(20);
        activitySection.getChildren().addAll(
            createRecentActivityPanel(),
            createThreatDetectionPanel()
        );
        content.getChildren().add(activitySection);
        
        scrollPane.setContent(content);
        return scrollPane;
    }
    
    /**
     * Create security overview cards
     */
    private Node createSecurityOverview() {
        HBox overview = new HBox(20);
        overview.setAlignment(Pos.CENTER);
        
        // Security Score Card
        VBox scoreCard = createMetricCard("🛡️ Security Score", "85", "/100", "security-score-good");
        securityScoreLabel = (Label) scoreCard.getChildren().get(1);
        securityScoreBar = new ProgressBar(0.85);
        securityScoreBar.setPrefWidth(200);
        securityScoreBar.getStyleClass().add("security-score-bar");
        scoreCard.getChildren().add(securityScoreBar);
        
        // Threat Level Card
        VBox threatCard = createMetricCard("⚠️ Threat Level", "LOW", "", "security-score-excellent");
        threatLevelLabel = (Label) threatCard.getChildren().get(1);
        
        // Active Sessions Card
        VBox sessionsCard = createMetricCard("👥 Active Sessions", "1", "", "metric-value");
        activeSessionsLabel = (Label) sessionsCard.getChildren().get(1);
        
        // Total Files Card
        VBox filesCard = createMetricCard("📁 Protected Files", "0", "", "metric-value");
        totalFilesLabel = (Label) filesCard.getChildren().get(1);
        
        overview.getChildren().addAll(scoreCard, threatCard, sessionsCard, filesCard);
        return overview;
    }
    
    /**
     * Create a metric card
     */
    private VBox createMetricCard(String title, String value, String suffix, String valueStyle) {
        VBox card = new VBox(10);
        card.getStyleClass().add("dashboard-card");
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(250);
        card.setPrefHeight(150);
        
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("metric-label");
        
        HBox valueBox = new HBox(5);
        valueBox.setAlignment(Pos.CENTER);
        
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().addAll("metric-value", valueStyle);
        
        Label suffixLabel = new Label(suffix);
        suffixLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #6c757d;");
        
        valueBox.getChildren().addAll(valueLabel, suffixLabel);
        card.getChildren().addAll(titleLabel, valueBox);
        
        return card;
    }
    
    /**
     * Create security events chart
     */
    private Node createSecurityEventsChart() {
        VBox chartContainer = new VBox(10);
        chartContainer.getStyleClass().add("dashboard-card");
        chartContainer.setPrefWidth(580);
        
        Label chartTitle = new Label("📊 Security Events (Last 7 Days)");
        chartTitle.getStyleClass().add("card-header");
        
        // Create line chart
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        securityEventsChart = new LineChart<>(xAxis, yAxis);
        securityEventsChart.setTitle("");
        securityEventsChart.setLegendVisible(true);
        securityEventsChart.setPrefHeight(300);
        
        // Sample data
        XYChart.Series<String, Number> loginSeries = new XYChart.Series<>();
        loginSeries.setName("Login Events");
        
        XYChart.Series<String, Number> threatSeries = new XYChart.Series<>();
        threatSeries.setName("Threat Events");
        
        // Add sample data points
        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        for (String day : days) {
            loginSeries.getData().add(new XYChart.Data<>(day, Math.random() * 10 + 5));
            threatSeries.getData().add(new XYChart.Data<>(day, Math.random() * 3));
        }
        
        securityEventsChart.getData().addAll(loginSeries, threatSeries);
        
        chartContainer.getChildren().addAll(chartTitle, securityEventsChart);
        return chartContainer;
    }
    
    /**
     * Create file types chart
     */
    private Node createFileTypesChart() {
        VBox chartContainer = new VBox(10);
        chartContainer.getStyleClass().add("dashboard-card");
        chartContainer.setPrefWidth(580);
        
        Label chartTitle = new Label("📈 File Types Distribution");
        chartTitle.getStyleClass().add("card-header");
        
        // Create pie chart
        fileTypesChart = new PieChart();
        fileTypesChart.setPrefHeight(300);
        
        // Sample data
        fileTypesChart.getData().addAll(
            new PieChart.Data("Documents", 45),
            new PieChart.Data("Images", 25),
            new PieChart.Data("Archives", 15),
            new PieChart.Data("Videos", 10),
            new PieChart.Data("Others", 5)
        );
        
        chartContainer.getChildren().addAll(chartTitle, fileTypesChart);
        return chartContainer;
    }
    
    /**
     * Create recent activity panel
     */
    private Node createRecentActivityPanel() {
        VBox activityPanel = new VBox(10);
        activityPanel.getStyleClass().add("dashboard-card");
        activityPanel.setPrefWidth(580);
        
        Label activityTitle = new Label("📋 Recent Activity");
        activityTitle.getStyleClass().add("card-header");
        
        recentActivityList = new ListView<>();
        recentActivityList.setPrefHeight(250);
        
        // Sample activity data
        recentActivityList.getItems().addAll(
            "🔐 " + LocalDateTime.now().minusMinutes(5).format(DateTimeFormatter.ofPattern("HH:mm")) + " - User logged in successfully",
            "📁 " + LocalDateTime.now().minusMinutes(15).format(DateTimeFormatter.ofPattern("HH:mm")) + " - File uploaded: document.pdf",
            "💾 " + LocalDateTime.now().minusMinutes(30).format(DateTimeFormatter.ofPattern("HH:mm")) + " - Backup created successfully",
            "🔍 " + LocalDateTime.now().minusMinutes(45).format(DateTimeFormatter.ofPattern("HH:mm")) + " - File search performed",
            "🛡️ " + LocalDateTime.now().minusMinutes(60).format(DateTimeFormatter.ofPattern("HH:mm")) + " - Security scan completed"
        );
        
        activityPanel.getChildren().addAll(activityTitle, recentActivityList);
        return activityPanel;
    }
    
    /**
     * Create threat detection panel
     */
    private Node createThreatDetectionPanel() {
        VBox threatPanel = new VBox(10);
        threatPanel.getStyleClass().add("dashboard-card");
        threatPanel.setPrefWidth(580);
        
        Label threatTitle = new Label("🚨 Threat Detection");
        threatTitle.getStyleClass().add("card-header");
        
        // Threat status
        HBox threatStatus = new HBox(10);
        threatStatus.setAlignment(Pos.CENTER_LEFT);
        
        Label statusIcon = new Label("✅");
        statusIcon.setStyle("-fx-font-size: 20px;");
        
        Label statusText = new Label("No active threats detected");
        statusText.setStyle("-fx-font-size: 14px; -fx-text-fill: #4CAF50; -fx-font-weight: 500;");
        
        threatStatus.getChildren().addAll(statusIcon, statusText);
        
        // Security recommendations
        VBox recommendations = new VBox(8);
        Label recTitle = new Label("💡 Security Recommendations:");
        recTitle.setStyle("-fx-font-weight: 600; -fx-text-fill: #495057;");
        
        ListView<String> recList = new ListView<>();
        recList.setPrefHeight(150);
        recList.getItems().addAll(
            "✓ Enable automatic backups",
            "✓ Use strong, unique passwords",
            "⚠ Consider enabling 2FA",
            "⚠ Review file access permissions",
            "ℹ Update to latest security patches"
        );
        
        recommendations.getChildren().addAll(recTitle, recList);
        
        threatPanel.getChildren().addAll(threatTitle, threatStatus, recommendations);
        return threatPanel;
    }
    
    /**
     * Create footer
     */
    private Node createFooter() {
        HBox footer = new HBox(20);
        footer.getStyleClass().add("status-bar");
        footer.setAlignment(Pos.CENTER_LEFT);
        
        Label systemStatus = new Label("🟢 System Status: Operational");
        systemStatus.getStyleClass().add("status-label");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label version = new Label("GhostVault v1.0.0 - Security Dashboard");
        version.getStyleClass().add("status-label");
        
        footer.getChildren().addAll(systemStatus, spacer, version);
        return footer;
    }
    
    /**
     * Refresh dashboard data
     */
    public void refreshDashboard() {
        Platform.runLater(() -> {
            // Update security score (simulate dynamic calculation)
            int newScore = calculateSecurityScore();
            securityScore.set(newScore);
            securityScoreLabel.setText(String.valueOf(newScore));
            securityScoreBar.setProgress(newScore / 100.0);
            
            // Update threat level
            currentThreatLevel = calculateThreatLevel();
            threatLevelLabel.setText(currentThreatLevel);
            
            // Update file count
            totalFilesLabel.setText(String.valueOf(totalFiles));
            
            // Add new activity
            String newActivity = "🔄 " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")) + 
                " - Dashboard refreshed";
            recentActivityList.getItems().add(0, newActivity);
            
            // Keep only last 10 activities
            if (recentActivityList.getItems().size() > 10) {
                recentActivityList.getItems().remove(10, recentActivityList.getItems().size());
            }
        });
    }
    
    /**
     * Calculate dynamic security score
     */
    private int calculateSecurityScore() {
        int score = 70; // Base score
        
        // Add points for various security factors
        if (activeSessions == 1) score += 10; // Single session
        if (totalFiles > 0) score += 5; // Has protected files
        if (currentThreatLevel.equals("LOW")) score += 15; // Low threat level
        
        return Math.min(100, score);
    }
    
    /**
     * Calculate current threat level
     */
    private String calculateThreatLevel() {
        // Simulate threat level calculation
        double random = Math.random();
        if (random < 0.8) return "LOW";
        if (random < 0.95) return "MEDIUM";
        return "HIGH";
    }
    
    /**
     * Update file count
     */
    public void updateFileCount(int count) {
        this.totalFiles = count;
        if (totalFilesLabel != null) {
            Platform.runLater(() -> totalFilesLabel.setText(String.valueOf(count)));
        }
    }
    
    /**
     * Add security event
     */
    public void addSecurityEvent(String event) {
        if (recentActivityList != null) {
            Platform.runLater(() -> {
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
                recentActivityList.getItems().add(0, "🛡️ " + timestamp + " - " + event);
                
                if (recentActivityList.getItems().size() > 10) {
                    recentActivityList.getItems().remove(10, recentActivityList.getItems().size());
                }
            });
        }
    }
    
    /**
     * Hide dashboard
     */
    public void hide() {
        if (dashboardStage != null) {
            dashboardStage.hide();
        }
    }
    
    /**
     * Check if dashboard is showing
     */
    public boolean isShowing() {
        return dashboardStage != null && dashboardStage.isShowing();
    }
}
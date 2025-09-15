package com.ghostvault.ui;

import javafx.application.Preloader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Splash screen preloader for GhostVault
 * Shows loading progress during application startup
 */
public class SplashScreenPreloader extends Preloader {
    
    private Stage preloaderStage;
    private ProgressBar progressBar;
    private Label statusLabel;
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        this.preloaderStage = primaryStage;
        
        // Create splash screen UI
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #1a1a1a, #2d2d2d); " +
                     "-fx-padding: 40px;");
        
        // Title
        Label titleLabel = new Label("GhostVault");
        titleLabel.setStyle("-fx-font-size: 36px; " +
                           "-fx-font-weight: bold; " +
                           "-fx-text-fill: #ffffff;");
        
        // Subtitle
        Label subtitleLabel = new Label("Secure File Vault");
        subtitleLabel.setStyle("-fx-font-size: 16px; " +
                              "-fx-text-fill: #cccccc;");
        
        // Progress bar
        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(300);
        progressBar.setStyle("-fx-accent: #4CAF50;");
        
        // Status label
        statusLabel = new Label("Initializing...");
        statusLabel.setStyle("-fx-font-size: 12px; " +
                            "-fx-text-fill: #aaaaaa;");
        
        root.getChildren().addAll(titleLabel, subtitleLabel, progressBar, statusLabel);
        
        Scene scene = new Scene(root, 400, 250);
        scene.setFill(Color.TRANSPARENT);
        
        primaryStage.setScene(scene);
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.setAlwaysOnTop(true);
        primaryStage.show();
    }
    
    @Override
    public void handleProgressNotification(ProgressNotification info) {
        if (progressBar != null) {
            progressBar.setProgress(info.getProgress());
        }
    }
    
    @Override
    public void handleStateChangeNotification(StateChangeNotification info) {
        if (statusLabel != null) {
            switch (info.getType()) {
                case BEFORE_LOAD:
                    statusLabel.setText("Loading components...");
                    break;
                case BEFORE_INIT:
                    statusLabel.setText("Initializing security...");
                    break;
                case BEFORE_START:
                    statusLabel.setText("Starting application...");
                    break;
            }
        }
    }
    
    @Override
    public void handleApplicationNotification(PreloaderNotification info) {
        if (info instanceof ProgressNotification) {
            handleProgressNotification((ProgressNotification) info);
        }
    }
}
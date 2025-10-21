package com.ghostvault;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Simple test application to verify JavaFX is working
 */
public class GhostVaultSimpleTest extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        try {
            System.out.println("🚀 Starting simple test application...");
            
            // Create a simple UI
            VBox root = new VBox(20);
            root.setStyle("-fx-padding: 50; -fx-alignment: center; -fx-background-color: #2b2b2b;");
            
            Label titleLabel = new Label("GhostVault - UI Test");
            titleLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: white; -fx-font-weight: bold;");
            
            Label statusLabel = new Label("✅ JavaFX is working correctly!");
            statusLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #4CAF50;");
            
            Label instructionLabel = new Label("If you can see this, the basic UI system is functional.");
            instructionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #cccccc;");
            
            root.getChildren().addAll(titleLabel, statusLabel, instructionLabel);
            
            // Create scene
            Scene scene = new Scene(root, 600, 400);
            
            // Setup stage
            primaryStage.setTitle("GhostVault - Simple Test");
            primaryStage.setScene(scene);
            primaryStage.show();
            
            System.out.println("✅ Simple test application started successfully!");
            
        } catch (Exception e) {
            System.err.println("❌ Error starting simple test application: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
package com.ghostvault;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

/**
 * Minimal GhostVault with proper window controls
 */
public class SimpleGhostVault extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        System.out.println("🚀 SimpleGhostVault starting...");
        
        try {
            // Create simple login UI
            VBox root = new VBox(20);
            root.setAlignment(Pos.CENTER);
            root.setStyle("-fx-background-color: #2b2b2b; -fx-padding: 50;");
            
            Label title = new Label("🔒 GhostVault");
            title.setStyle("-fx-font-size: 24px; -fx-text-fill: white; -fx-font-weight: bold;");
            
            PasswordField passwordField = new PasswordField();
            passwordField.setPromptText("Enter vault password");
            passwordField.setPrefWidth(300);
            passwordField.setPrefHeight(40);
            passwordField.setStyle("-fx-font-size: 14px;");
            
            Button loginButton = new Button("Unlock Vault");
            loginButton.setPrefWidth(150);
            loginButton.setPrefHeight(40);
            loginButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px;");
            
            Button exitButton = new Button("Exit");
            exitButton.setPrefWidth(100);
            exitButton.setPrefHeight(40);
            exitButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 14px;");
            
            HBox buttonBox = new HBox(10);
            buttonBox.setAlignment(Pos.CENTER);
            buttonBox.getChildren().addAll(exitButton, loginButton);
            
            Label statusLabel = new Label("");
            statusLabel.setStyle("-fx-text-fill: #ff9800; -fx-font-size: 12px;");
            
            root.getChildren().addAll(title, passwordField, buttonBox, statusLabel);
            
            // Button actions
            loginButton.setOnAction(e -> {
                String password = passwordField.getText();
                if (password.isEmpty()) {
                    statusLabel.setText("Please enter a password");
                } else {
                    statusLabel.setText("Password entered: " + password.length() + " characters");
                    System.out.println("Login attempted with: " + password);
                }
            });
            
            exitButton.setOnAction(e -> {
                System.out.println("Exit button clicked");
                Platform.exit();
            });
            
            passwordField.setOnAction(e -> loginButton.fire());
            
            // Create scene
            Scene scene = new Scene(root, 500, 400);
            
            // Setup stage - CRITICAL: Do this in the right order
            primaryStage.setTitle("GhostVault - Simple Test");
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);
            primaryStage.setMinWidth(400);
            primaryStage.setMinHeight(300);
            
            // Show the stage
            primaryStage.show();
            
            // Focus password field
            Platform.runLater(() -> passwordField.requestFocus());
            
            System.out.println("✅ SimpleGhostVault window should be fully functional");
            
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
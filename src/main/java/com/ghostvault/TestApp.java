package com.ghostvault;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Minimal test application to verify JavaFX is working
 */
public class TestApp extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        System.out.println("🚀 TestApp starting...");
        
        try {
            // Create simple UI
            VBox root = new VBox(20);
            root.setStyle("-fx-padding: 50; -fx-alignment: center; -fx-background-color: #333333;");
            
            Label title = new Label("JavaFX Test - GhostVault");
            title.setStyle("-fx-font-size: 20px; -fx-text-fill: white; -fx-font-weight: bold;");
            
            Button testButton = new Button("Click Me!");
            testButton.setStyle("-fx-font-size: 14px; -fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 10;");
            testButton.setOnAction(e -> {
                System.out.println("✅ Button clicked! JavaFX is working!");
                title.setText("Button Clicked! ✅");
            });
            
            root.getChildren().addAll(title, testButton);
            
            Scene scene = new Scene(root, 400, 300);
            
            primaryStage.setTitle("GhostVault - JavaFX Test");
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);
            primaryStage.show();
            
            System.out.println("✅ TestApp window should be visible now");
            
        } catch (Exception e) {
            System.err.println("❌ Error in TestApp: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        System.out.println("🔧 Launching TestApp...");
        launch(args);
    }
}
package com.ghostvault.ui;

import com.ghostvault.security.PasswordManager;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Reusable password strength meter component with visual feedback
 */
public class PasswordStrengthMeter extends VBox {
    
    private final PasswordField passwordField;
    private final ProgressBar strengthBar;
    private final Label strengthLabel;
    private final Label feedbackLabel;
    
    private int currentStrength = 0;
    
    public PasswordStrengthMeter(String title, String prompt) {
        this(title, prompt, 400);
    }
    
    public PasswordStrengthMeter(String title, String prompt, double fieldWidth) {
        super(5);
        
        // Title
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", 14));
        titleLabel.setStyle("-fx-font-weight: bold;");
        
        // Password field
        Label promptLabel = new Label(prompt);
        passwordField = new PasswordField();
        passwordField.setPrefWidth(fieldWidth);
        
        // Strength meter
        HBox strengthBox = new HBox(10);
        strengthBox.setAlignment(Pos.CENTER_LEFT);
        
        strengthBar = new ProgressBar(0);
        strengthBar.setPrefWidth(200);
        strengthBar.setPrefHeight(8);
        
        strengthLabel = new Label("");
        strengthLabel.setFont(Font.font("System", 12));
        
        strengthBox.getChildren().addAll(strengthBar, strengthLabel);
        
        // Feedback label
        feedbackLabel = new Label("");
        feedbackLabel.setWrapText(true);
        feedbackLabel.setFont(Font.font("System", 10));
        feedbackLabel.setTextFill(Color.DARKRED);
        
        // Add all components
        getChildren().addAll(titleLabel, promptLabel, passwordField, strengthBox, feedbackLabel);
        
        // Setup real-time monitoring
        setupPasswordMonitoring();
    }
    
    /**
     * Setup real-time password strength monitoring
     */
    private void setupPasswordMonitoring() {
        passwordField.textProperty().addListener((obs, oldText, newText) -> {
            updatePasswordStrength(newText);
        });
    }
    
    /**
     * Update password strength meter and feedback
     */
    private void updatePasswordStrength(String password) {
        currentStrength = PasswordManager.getPasswordStrength(password);
        double progress = currentStrength / 5.0;
        
        Platform.runLater(() -> {
            // Update progress bar
            strengthBar.setProgress(progress);
            
            // Update strength description
            strengthLabel.setText(PasswordManager.getPasswordStrengthDescription(currentStrength));
            
            // Update strength bar color
            String color = PasswordManager.getPasswordStrengthColor(currentStrength);
            strengthBar.setStyle("-fx-accent: " + color + ";");
            
            // Update feedback text
            String feedback = PasswordManager.getPasswordStrengthFeedback(password);
            feedbackLabel.setText(feedback);
            feedbackLabel.setTextFill("Strong password!".equals(feedback) ? Color.GREEN : Color.DARKRED);
        });
    }
    
    /**
     * Get the password field
     */
    public PasswordField getPasswordField() {
        return passwordField;
    }
    
    /**
     * Get current password
     */
    public String getPassword() {
        return passwordField.getText();
    }
    
    /**
     * Set password
     */
    public void setPassword(String password) {
        passwordField.setText(password);
    }
    
    /**
     * Clear password
     */
    public void clear() {
        passwordField.clear();
    }
    
    /**
     * Get current strength score (0-5)
     */
    public int getCurrentStrength() {
        return currentStrength;
    }
    
    /**
     * Check if password meets minimum strength requirement
     */
    public boolean meetsMinimumStrength(int minimumStrength) {
        return currentStrength >= minimumStrength;
    }
    
    /**
     * Set field width
     */
    public void setFieldWidth(double width) {
        passwordField.setPrefWidth(width);
    }
    
    /**
     * Set strength bar width
     */
    public void setStrengthBarWidth(double width) {
        strengthBar.setPrefWidth(width);
    }
    
    /**
     * Enable or disable the password field
     */
    public void setDisable(boolean disable) {
        passwordField.setDisable(disable);
    }
    
    /**
     * Check if password field is empty
     */
    public boolean isEmpty() {
        return passwordField.getText().isEmpty();
    }
    
    /**
     * Add listener for password changes
     */
    public void addPasswordChangeListener(Runnable listener) {
        passwordField.textProperty().addListener((obs, oldText, newText) -> {
            listener.run();
        });
    }
}
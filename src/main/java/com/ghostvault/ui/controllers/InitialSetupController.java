package com.ghostvault.ui.controllers;

import javafx.stage.Stage;
import java.util.function.Consumer;

/**
 * Initial setup controller for first-time setup
 */
public class InitialSetupController {
    private Stage parentStage;
    private Consumer<Boolean> onSetupComplete;
    
    public InitialSetupController(Stage parentStage) {
        this.parentStage = parentStage;
    }
    
    public void setOnSetupComplete(Consumer<Boolean> callback) {
        this.onSetupComplete = callback;
    }
    
    public void show() {
        System.out.println("🔧 Setup wizard would be displayed here");
        // Simulate successful setup
        if (onSetupComplete != null) {
            onSetupComplete.accept(true);
        }
    }
}
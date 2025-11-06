package com.ghostvault.ui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Keyboard shortcut help dialog
 */
public class ShortcutHelpDialog {
    
    /**
     * Show keyboard shortcuts help dialog
     */
    public static void show(javafx.stage.Window owner) {
        javafx.stage.Stage dialog = new javafx.stage.Stage();
        dialog.setTitle("Keyboard Shortcuts");
        dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        if (owner != null) {
            dialog.initOwner(owner);
        }
        
        VBox content = new VBox(16);
        content.setPadding(new javafx.geometry.Insets(20));
        content.getStyleClass().add("shortcut-help-dialog");
        
        // Title
        Label title = new Label("Keyboard Shortcuts");
        title.getStyleClass().add("dialog-title");
        
        // Create shortcut categories
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(400);
        
        VBox shortcutsContainer = new VBox(12);
        
        // File Operations
        shortcutsContainer.getChildren().add(createShortcutCategory("File Operations", new String[][]{
            {"Upload Files", "Ctrl+U"},
            {"Download Files", "Ctrl+D"},
            {"Delete Files", "Delete"},
            {"Secure Delete", "Shift+Delete"},
            {"New Folder", "Ctrl+Shift+N"},
            {"Backup Vault", "Ctrl+B"},
            {"Restore Vault", "Ctrl+R"}
        }));
        
        // Selection
        shortcutsContainer.getChildren().add(createShortcutCategory("Selection", new String[][]{
            {"Select All", "Ctrl+A"},
            {"Deselect All", "Ctrl+D"},
            {"Copy", "Ctrl+C"},
            {"Cut", "Ctrl+X"},
            {"Paste", "Ctrl+V"}
        }));
        
        // Navigation
        shortcutsContainer.getChildren().add(createShortcutCategory("Navigation", new String[][]{
            {"Search", "Ctrl+F"},
            {"Go Up", "Alt+Up"},
            {"Go Back", "Alt+Left"},
            {"Go Forward", "Alt+Right"},
            {"Refresh", "F5"}
        }));
        
        // Preview
        shortcutsContainer.getChildren().add(createShortcutCategory("Preview", new String[][]{
            {"Preview File", "Space"},
            {"Close Preview", "Escape"},
            {"Zoom In", "Ctrl++"},
            {"Zoom Out", "Ctrl+-"},
            {"Fit to Window", "Ctrl+0"},
            {"Actual Size", "Ctrl+1"}
        }));
        
        // Application
        shortcutsContainer.getChildren().add(createShortcutCategory("Application", new String[][]{
            {"Settings", "Ctrl+,"},
            {"Help", "F1"},
            {"Logout", "Ctrl+L"},
            {"Exit", "Alt+F4"},
            {"Emergency Mode", "Ctrl+Shift+F12"}
        }));
        
        scrollPane.setContent(shortcutsContainer);
        
        // Close button
        HBox buttonContainer = new HBox();
        buttonContainer.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        
        Button closeButton = new Button("Close");
        closeButton.getStyleClass().addAll("button", "primary");
        closeButton.setOnAction(e -> dialog.close());
        
        buttonContainer.getChildren().add(closeButton);
        
        content.getChildren().addAll(title, scrollPane, buttonContainer);
        
        Scene scene = new Scene(content, 500, 600);
        try {
            scene.getStylesheets().add(ShortcutHelpDialog.class.getResource("/css/ultra-modern-theme.css").toExternalForm());
        } catch (Exception e) {
            // Fallback if CSS not found
        }
        
        dialog.setScene(scene);
        dialog.showAndWait();
    }
    
    /**
     * Create shortcut category section
     */
    private static VBox createShortcutCategory(String categoryName, String[][] shortcuts) {
        VBox category = new VBox(4);
        category.getStyleClass().add("shortcut-category");
        
        Label categoryLabel = new Label(categoryName);
        categoryLabel.getStyleClass().add("shortcut-category-title");
        
        GridPane grid = new GridPane();
        grid.getStyleClass().add("shortcut-grid");
        grid.setHgap(20);
        grid.setVgap(4);
        
        for (int i = 0; i < shortcuts.length; i++) {
            Label actionLabel = new Label(shortcuts[i][0]);
            actionLabel.getStyleClass().add("shortcut-action");
            
            Label keyLabel = new Label(shortcuts[i][1]);
            keyLabel.getStyleClass().add("shortcut-key");
            
            grid.add(actionLabel, 0, i);
            grid.add(keyLabel, 1, i);
        }
        
        category.getChildren().addAll(categoryLabel, grid);
        return category;
    }
}
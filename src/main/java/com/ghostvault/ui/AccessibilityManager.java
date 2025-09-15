package com.ghostvault.ui;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

/**
 * Manages accessibility features for GhostVault
 * Provides keyboard navigation, screen reader support, and accessibility enhancements
 */
public class AccessibilityManager {
    
    private Scene currentScene;
    private boolean highContrastMode = false;
    private boolean screenReaderMode = false;
    
    /**
     * Initialize accessibility features for a scene
     */
    public void initializeAccessibility(Scene scene) {
        this.currentScene = scene;
        
        // Set up keyboard navigation
        setupKeyboardNavigation(scene);
        
        // Set up accessibility properties
        setupAccessibilityProperties(scene);
        
        // Set up keyboard shortcuts
        setupKeyboardShortcuts(scene);
        
        System.out.println("♿ Accessibility features initialized");
    }
    
    /**
     * Set up keyboard navigation
     */
    private void setupKeyboardNavigation(Scene scene) {
        // Enable focus traversal
        scene.getRoot().setFocusTraversable(true);
        
        // Set up tab navigation
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.TAB) {
                // Handle tab navigation
                handleTabNavigation(event.isShiftDown());
            } else if (event.getCode() == KeyCode.ESCAPE) {
                // Handle escape key
                handleEscapeKey();
            }
        });
    }
    
    /**
     * Set up accessibility properties for UI elements
     */
    private void setupAccessibilityProperties(Scene scene) {
        // Recursively set accessibility properties
        setAccessibilityProperties(scene.getRoot());
    }
    
    /**
     * Recursively set accessibility properties for nodes
     */
    private void setAccessibilityProperties(Node node) {
        // Set accessibility text for common controls
        if (node instanceof Button) {
            Button button = (Button) node;
            if (button.getAccessibleText() == null && button.getText() != null) {
                button.setAccessibleText("Button: " + button.getText());
            }
        } else if (node instanceof TextField) {
            TextField textField = (TextField) node;
            if (textField.getAccessibleText() == null) {
                String prompt = textField.getPromptText();
                textField.setAccessibleText("Text field" + (prompt != null ? ": " + prompt : ""));
            }
        } else if (node instanceof PasswordField) {
            PasswordField passwordField = (PasswordField) node;
            if (passwordField.getAccessibleText() == null) {
                passwordField.setAccessibleText("Password field");
            }
        } else if (node instanceof Label) {
            Label label = (Label) node;
            if (label.getAccessibleText() == null && label.getText() != null) {
                label.setAccessibleText("Label: " + label.getText());
            }
        } else if (node instanceof ListView) {
            ListView<?> listView = (ListView<?>) node;
            if (listView.getAccessibleText() == null) {
                listView.setAccessibleText("List view with " + listView.getItems().size() + " items");
            }
        }
        
        // Process child nodes
        if (node instanceof javafx.scene.Parent) {
            javafx.scene.Parent parent = (javafx.scene.Parent) node;
            parent.getChildrenUnmodifiable().forEach(this::setAccessibilityProperties);
        }
    }
    
    /**
     * Set up keyboard shortcuts
     */
    private void setupKeyboardShortcuts(Scene scene) {
        // Ctrl+H for high contrast toggle
        scene.getAccelerators().put(
            new KeyCodeCombination(KeyCode.H, KeyCombination.CONTROL_DOWN),
            this::toggleHighContrast
        );
        
        // Ctrl+R for screen reader mode toggle
        scene.getAccelerators().put(
            new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN),
            this::toggleScreenReaderMode
        );
        
        // F1 for help
        scene.getAccelerators().put(
            new KeyCodeCombination(KeyCode.F1),
            this::showAccessibilityHelp
        );
    }
    
    /**
     * Handle tab navigation
     */
    private void handleTabNavigation(boolean reverse) {
        // Implementation for custom tab navigation if needed
        // JavaFX handles this automatically in most cases
    }
    
    /**
     * Handle escape key
     */
    private void handleEscapeKey() {
        // Close dialogs or return to previous screen
        if (currentScene != null) {
            Node focusOwner = currentScene.getFocusOwner();
            if (focusOwner instanceof Dialog) {
                ((Dialog<?>) focusOwner).close();
            }
        }
    }
    
    /**
     * Toggle high contrast mode
     */
    public void toggleHighContrast() {
        highContrastMode = !highContrastMode;
        
        if (currentScene != null) {
            if (highContrastMode) {
                currentScene.getStylesheets().add(
                    getClass().getResource("/styles/high_contrast.css").toExternalForm());
            } else {
                currentScene.getStylesheets().removeIf(stylesheet -> 
                    stylesheet.contains("high_contrast.css"));
            }
        }
        
        System.out.println("🎨 High contrast mode: " + (highContrastMode ? "ON" : "OFF"));
    }
    
    /**
     * Toggle screen reader mode
     */
    public void toggleScreenReaderMode() {
        screenReaderMode = !screenReaderMode;
        
        if (currentScene != null) {
            // Enhanced accessibility properties for screen readers
            setScreenReaderProperties(currentScene.getRoot(), screenReaderMode);
        }
        
        System.out.println("🔊 Screen reader mode: " + (screenReaderMode ? "ON" : "OFF"));
    }
    
    /**
     * Set screen reader specific properties
     */
    private void setScreenReaderProperties(Node node, boolean enhanced) {
        if (enhanced) {
            // Add more detailed accessibility descriptions
            if (node instanceof Button) {
                Button button = (Button) node;
                String text = button.getText();
                if (text != null) {
                    button.setAccessibleText("Clickable button: " + text + ". Press Enter or Space to activate.");
                }
            } else if (node instanceof TextField) {
                TextField textField = (TextField) node;
                textField.setAccessibleText("Editable text field. Type to enter text.");
            } else if (node instanceof PasswordField) {
                PasswordField passwordField = (PasswordField) node;
                passwordField.setAccessibleText("Password field. Characters will be hidden as you type.");
            }
        }
        
        // Process child nodes
        if (node instanceof javafx.scene.Parent) {
            javafx.scene.Parent parent = (javafx.scene.Parent) node;
            parent.getChildrenUnmodifiable().forEach(child -> 
                setScreenReaderProperties(child, enhanced));
        }
    }
    
    /**
     * Show accessibility help dialog
     */
    private void showAccessibilityHelp() {
        Alert helpDialog = new Alert(Alert.AlertType.INFORMATION);
        helpDialog.setTitle("Accessibility Help");
        helpDialog.setHeaderText("GhostVault Accessibility Features");
        
        String helpText = """
            Keyboard Shortcuts:
            • Tab / Shift+Tab: Navigate between controls
            • Enter / Space: Activate buttons and controls
            • Escape: Close dialogs or cancel operations
            • Ctrl+H: Toggle high contrast mode
            • Ctrl+R: Toggle enhanced screen reader mode
            • F1: Show this help dialog
            
            Navigation:
            • Use Tab to move forward through controls
            • Use Shift+Tab to move backward
            • Arrow keys navigate within lists and menus
            • Enter activates the focused control
            
            Screen Reader Support:
            • All controls have descriptive labels
            • Progress and status updates are announced
            • Error messages are clearly identified
            
            High Contrast Mode:
            • Increases color contrast for better visibility
            • Useful for users with visual impairments
            • Toggle with Ctrl+H
            """;
        
        helpDialog.setContentText(helpText);
        helpDialog.getDialogPane().setPrefWidth(500);
        helpDialog.showAndWait();
    }
    
    /**
     * Set focus to specific node with announcement
     */
    public void setFocusWithAnnouncement(Node node, String announcement) {
        if (node != null) {
            node.requestFocus();
            
            if (screenReaderMode && announcement != null) {
                // Set temporary accessible text for announcement
                String originalText = node.getAccessibleText();
                node.setAccessibleText(announcement);
                
                // Restore original text after a delay
                javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                    new javafx.animation.KeyFrame(
                        javafx.util.Duration.seconds(2),
                        e -> node.setAccessibleText(originalText)
                    )
                );
                timeline.play();
            }
        }
    }
    
    /**
     * Announce message to screen reader
     */
    public void announceToScreenReader(String message) {
        if (screenReaderMode && currentScene != null) {
            // Create temporary invisible label for announcement
            Label announcement = new Label(message);
            announcement.setVisible(false);
            announcement.setAccessibleText(message);
            
            if (currentScene.getRoot() instanceof javafx.scene.Parent) {
                javafx.scene.Parent parent = (javafx.scene.Parent) currentScene.getRoot();
                if (parent instanceof javafx.scene.layout.Pane) {
                    javafx.scene.layout.Pane pane = (javafx.scene.layout.Pane) parent;
                    pane.getChildren().add(announcement);
                    
                    // Remove after announcement
                    javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                        new javafx.animation.KeyFrame(
                            javafx.util.Duration.seconds(3),
                            e -> pane.getChildren().remove(announcement)
                        )
                    );
                    timeline.play();
                }
            }
        }
    }
    
    /**
     * Check if high contrast mode is enabled
     */
    public boolean isHighContrastMode() {
        return highContrastMode;
    }
    
    /**
     * Check if screen reader mode is enabled
     */
    public boolean isScreenReaderMode() {
        return screenReaderMode;
    }
}
package com.ghostvault.ui.components;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Centralized keyboard shortcut manager for the application
 */
public class KeyboardShortcutManager {
    
    private static KeyboardShortcutManager instance;
    private Map<KeyCombination, Runnable> shortcuts = new HashMap<>();
    private Map<String, KeyCombination> namedShortcuts = new HashMap<>();
    private Scene currentScene;
    
    private KeyboardShortcutManager() {
        initializeDefaultShortcuts();
    }
    
    /**
     * Get singleton instance
     */
    public static KeyboardShortcutManager getInstance() {
        if (instance == null) {
            instance = new KeyboardShortcutManager();
        }
        return instance;
    }
    
    /**
     * Initialize with scene
     */
    public static void initialize(Scene scene) {
        getInstance().setScene(scene);
    }
    
    /**
     * Register a keyboard shortcut
     */
    public static void register(String name, KeyCombination combination, Runnable action) {
        getInstance().registerShortcut(name, combination, action);
    }
    
    /**
     * Register a keyboard shortcut with key codes
     */
    public static void register(String name, KeyCode keyCode, Runnable action) {
        KeyCombination combination = new KeyCodeCombination(keyCode);
        getInstance().registerShortcut(name, combination, action);
    }
    
    /**
     * Register a keyboard shortcut with modifier
     */
    public static void register(String name, KeyCode keyCode, KeyCombination.Modifier modifier, Runnable action) {
        KeyCombination combination = new KeyCodeCombination(keyCode, modifier);
        getInstance().registerShortcut(name, combination, action);
    }
    
    /**
     * Unregister a keyboard shortcut
     */
    public static void unregister(String name) {
        getInstance().unregisterShortcut(name);
    }
    
    /**
     * Get key combination for named shortcut
     */
    public static KeyCombination getShortcut(String name) {
        return getInstance().namedShortcuts.get(name);
    }
    
    /**
     * Get shortcut text for display
     */
    public static String getShortcutText(String name) {
        KeyCombination combination = getInstance().namedShortcuts.get(name);
        return combination != null ? combination.getDisplayText() : "";
    }
    
    // Private implementation methods
    
    private void setScene(Scene scene) {
        this.currentScene = scene;
        setupKeyEventHandlers();
    }
    
    private void registerShortcut(String name, KeyCombination combination, Runnable action) {
        shortcuts.put(combination, action);
        namedShortcuts.put(name, combination);
    }
    
    private void unregisterShortcut(String name) {
        KeyCombination combination = namedShortcuts.remove(name);
        if (combination != null) {
            shortcuts.remove(combination);
        }
    }
    
    private void setupKeyEventHandlers() {
        if (currentScene == null) return;
        
        currentScene.setOnKeyPressed(event -> {
            for (Map.Entry<KeyCombination, Runnable> entry : shortcuts.entrySet()) {
                if (entry.getKey().match(event)) {
                    event.consume();
                    entry.getValue().run();
                    break;
                }
            }
        });
    }
    
    /**
     * Initialize default application shortcuts
     */
    private void initializeDefaultShortcuts() {
        // File operations
        namedShortcuts.put("upload", new KeyCodeCombination(KeyCode.U, KeyCombination.CONTROL_DOWN));
        namedShortcuts.put("download", new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN));
        namedShortcuts.put("delete", new KeyCodeCombination(KeyCode.DELETE));
        namedShortcuts.put("secure_delete", new KeyCodeCombination(KeyCode.DELETE, KeyCombination.SHIFT_DOWN));
        namedShortcuts.put("new_folder", new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
        
        // Selection operations
        namedShortcuts.put("select_all", new KeyCodeCombination(KeyCode.A, KeyCombination.CONTROL_DOWN));
        namedShortcuts.put("deselect_all", new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN));
        namedShortcuts.put("invert_selection", new KeyCodeCombination(KeyCode.I, KeyCombination.CONTROL_DOWN));
        
        // Navigation
        namedShortcuts.put("go_up", new KeyCodeCombination(KeyCode.UP, KeyCombination.ALT_DOWN));
        namedShortcuts.put("go_back", new KeyCodeCombination(KeyCode.LEFT, KeyCombination.ALT_DOWN));
        namedShortcuts.put("go_forward", new KeyCodeCombination(KeyCode.RIGHT, KeyCombination.ALT_DOWN));
        namedShortcuts.put("go_home", new KeyCodeCombination(KeyCode.HOME, KeyCombination.ALT_DOWN));
        
        // Search and filter
        namedShortcuts.put("search", new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN));
        namedShortcuts.put("advanced_search", new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
        namedShortcuts.put("clear_search", new KeyCodeCombination(KeyCode.ESCAPE));
        
        // Preview operations
        namedShortcuts.put("preview", new KeyCodeCombination(KeyCode.SPACE));
        namedShortcuts.put("close_preview", new KeyCodeCombination(KeyCode.ESCAPE));
        namedShortcuts.put("zoom_in", new KeyCodeCombination(KeyCode.PLUS, KeyCombination.CONTROL_DOWN));
        namedShortcuts.put("zoom_out", new KeyCodeCombination(KeyCode.MINUS, KeyCombination.CONTROL_DOWN));
        namedShortcuts.put("zoom_fit", new KeyCodeCombination(KeyCode.DIGIT0, KeyCombination.CONTROL_DOWN));
        namedShortcuts.put("zoom_actual", new KeyCodeCombination(KeyCode.DIGIT1, KeyCombination.CONTROL_DOWN));
        
        // Backup and restore
        namedShortcuts.put("backup", new KeyCodeCombination(KeyCode.B, KeyCombination.CONTROL_DOWN));
        namedShortcuts.put("restore", new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN));
        
        // Application controls
        namedShortcuts.put("settings", new KeyCodeCombination(KeyCode.COMMA, KeyCombination.CONTROL_DOWN));
        namedShortcuts.put("help", new KeyCodeCombination(KeyCode.F1));
        namedShortcuts.put("logout", new KeyCodeCombination(KeyCode.L, KeyCombination.CONTROL_DOWN));
        namedShortcuts.put("exit", new KeyCodeCombination(KeyCode.F4, KeyCombination.ALT_DOWN));
        
        // Mode switching (emergency)
        namedShortcuts.put("panic_mode", new KeyCodeCombination(KeyCode.F12, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
        
        // View options
        namedShortcuts.put("refresh", new KeyCodeCombination(KeyCode.F5));
        namedShortcuts.put("toggle_details", new KeyCodeCombination(KeyCode.F6));
        namedShortcuts.put("toggle_preview", new KeyCodeCombination(KeyCode.F7));
        namedShortcuts.put("toggle_sidebar", new KeyCodeCombination(KeyCode.F9));
        namedShortcuts.put("fullscreen", new KeyCodeCombination(KeyCode.F11));
        
        // Copy/paste operations
        namedShortcuts.put("copy", new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN));
        namedShortcuts.put("cut", new KeyCodeCombination(KeyCode.X, KeyCombination.CONTROL_DOWN));
        namedShortcuts.put("paste", new KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_DOWN));
        
        // Undo/redo
        namedShortcuts.put("undo", new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN));
        namedShortcuts.put("redo", new KeyCodeCombination(KeyCode.Y, KeyCombination.CONTROL_DOWN));
    }
    
    /**
     * Get all registered shortcuts for help display
     */
    public Map<String, KeyCombination> getAllShortcuts() {
        return new HashMap<>(namedShortcuts);
    }
    
    /**
     * Clear all shortcuts
     */
    public void clearAll() {
        shortcuts.clear();
        namedShortcuts.clear();
        initializeDefaultShortcuts();
    }
}

/**
 * Predefined shortcut actions for common operations
 */
public class ShortcutActions {
    
    private static ShortcutActions instance;
    
    // Action callbacks
    private Runnable onUpload;
    private Runnable onDownload;
    private Runnable onDelete;
    private Runnable onSecureDelete;
    private Runnable onNewFolder;
    private Runnable onSelectAll;
    private Runnable onDeselectAll;
    private Runnable onSearch;
    private Runnable onPreview;
    private Runnable onZoomIn;
    private Runnable onZoomOut;
    private Runnable onZoomFit;
    private Runnable onBackup;
    private Runnable onRestore;
    private Runnable onSettings;
    private Runnable onHelp;
    private Runnable onLogout;
    private Runnable onExit;
    private Runnable onRefresh;
    private Runnable onPanicMode;
    
    private ShortcutActions() {
        // Private constructor for singleton
    }
    
    /**
     * Get singleton instance
     */
    public static ShortcutActions getInstance() {
        if (instance == null) {
            instance = new ShortcutActions();
        }
        return instance;
    }
    
    /**
     * Initialize shortcut actions with scene
     */
    public static void initialize(Scene scene) {
        KeyboardShortcutManager.initialize(scene);
        getInstance().registerAllActions();
    }
    
    /**
     * Register all shortcut actions
     */
    private void registerAllActions() {
        // File operations
        KeyboardShortcutManager.register("upload", 
            KeyboardShortcutManager.getInstance().namedShortcuts.get("upload"), 
            () -> { if (onUpload != null) onUpload.run(); });
            
        KeyboardShortcutManager.register("download", 
            KeyboardShortcutManager.getInstance().namedShortcuts.get("download"), 
            () -> { if (onDownload != null) onDownload.run(); });
            
        KeyboardShortcutManager.register("delete", 
            KeyboardShortcutManager.getInstance().namedShortcuts.get("delete"), 
            () -> { if (onDelete != null) onDelete.run(); });
            
        KeyboardShortcutManager.register("secure_delete", 
            KeyboardShortcutManager.getInstance().namedShortcuts.get("secure_delete"), 
            () -> { if (onSecureDelete != null) onSecureDelete.run(); });
            
        KeyboardShortcutManager.register("new_folder", 
            KeyboardShortcutManager.getInstance().namedShortcuts.get("new_folder"), 
            () -> { if (onNewFolder != null) onNewFolder.run(); });
        
        // Selection operations
        KeyboardShortcutManager.register("select_all", 
            KeyboardShortcutManager.getInstance().namedShortcuts.get("select_all"), 
            () -> { if (onSelectAll != null) onSelectAll.run(); });
            
        KeyboardShortcutManager.register("deselect_all", 
            KeyboardShortcutManager.getInstance().namedShortcuts.get("deselect_all"), 
            () -> { if (onDeselectAll != null) onDeselectAll.run(); });
        
        // Search
        KeyboardShortcutManager.register("search", 
            KeyboardShortcutManager.getInstance().namedShortcuts.get("search"), 
            () -> { if (onSearch != null) onSearch.run(); });
        
        // Preview operations
        KeyboardShortcutManager.register("preview", 
            KeyboardShortcutManager.getInstance().namedShortcuts.get("preview"), 
            () -> { if (onPreview != null) onPreview.run(); });
            
        KeyboardShortcutManager.register("zoom_in", 
            KeyboardShortcutManager.getInstance().namedShortcuts.get("zoom_in"), 
            () -> { if (onZoomIn != null) onZoomIn.run(); });
            
        KeyboardShortcutManager.register("zoom_out", 
            KeyboardShortcutManager.getInstance().namedShortcuts.get("zoom_out"), 
            () -> { if (onZoomOut != null) onZoomOut.run(); });
            
        KeyboardShortcutManager.register("zoom_fit", 
            KeyboardShortcutManager.getInstance().namedShortcuts.get("zoom_fit"), 
            () -> { if (onZoomFit != null) onZoomFit.run(); });
        
        // Backup operations
        KeyboardShortcutManager.register("backup", 
            KeyboardShortcutManager.getInstance().namedShortcuts.get("backup"), 
            () -> { if (onBackup != null) onBackup.run(); });
            
        KeyboardShortcutManager.register("restore", 
            KeyboardShortcutManager.getInstance().namedShortcuts.get("restore"), 
            () -> { if (onRestore != null) onRestore.run(); });
        
        // Application controls
        KeyboardShortcutManager.register("settings", 
            KeyboardShortcutManager.getInstance().namedShortcuts.get("settings"), 
            () -> { if (onSettings != null) onSettings.run(); });
            
        KeyboardShortcutManager.register("help", 
            KeyboardShortcutManager.getInstance().namedShortcuts.get("help"), 
            () -> { if (onHelp != null) onHelp.run(); });
            
        KeyboardShortcutManager.register("logout", 
            KeyboardShortcutManager.getInstance().namedShortcuts.get("logout"), 
            () -> { if (onLogout != null) onLogout.run(); });
            
        KeyboardShortcutManager.register("exit", 
            KeyboardShortcutManager.getInstance().namedShortcuts.get("exit"), 
            () -> { if (onExit != null) onExit.run(); });
            
        KeyboardShortcutManager.register("refresh", 
            KeyboardShortcutManager.getInstance().namedShortcuts.get("refresh"), 
            () -> { if (onRefresh != null) onRefresh.run(); });
            
        // Emergency panic mode
        KeyboardShortcutManager.register("panic_mode", 
            KeyboardShortcutManager.getInstance().namedShortcuts.get("panic_mode"), 
            () -> { if (onPanicMode != null) onPanicMode.run(); });
    }
    
    // Setter methods for action callbacks
    
    public void setOnUpload(Runnable action) { this.onUpload = action; }
    public void setOnDownload(Runnable action) { this.onDownload = action; }
    public void setOnDelete(Runnable action) { this.onDelete = action; }
    public void setOnSecureDelete(Runnable action) { this.onSecureDelete = action; }
    public void setOnNewFolder(Runnable action) { this.onNewFolder = action; }
    public void setOnSelectAll(Runnable action) { this.onSelectAll = action; }
    public void setOnDeselectAll(Runnable action) { this.onDeselectAll = action; }
    public void setOnSearch(Runnable action) { this.onSearch = action; }
    public void setOnPreview(Runnable action) { this.onPreview = action; }
    public void setOnZoomIn(Runnable action) { this.onZoomIn = action; }
    public void setOnZoomOut(Runnable action) { this.onZoomOut = action; }
    public void setOnZoomFit(Runnable action) { this.onZoomFit = action; }
    public void setOnBackup(Runnable action) { this.onBackup = action; }
    public void setOnRestore(Runnable action) { this.onRestore = action; }
    public void setOnSettings(Runnable action) { this.onSettings = action; }
    public void setOnHelp(Runnable action) { this.onHelp = action; }
    public void setOnLogout(Runnable action) { this.onLogout = action; }
    public void setOnExit(Runnable action) { this.onExit = action; }
    public void setOnRefresh(Runnable action) { this.onRefresh = action; }
    public void setOnPanicMode(Runnable action) { this.onPanicMode = action; }
}

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
        scene.getStylesheets().add(ShortcutHelpDialog.class.getResource("/css/ultra-modern-theme.css").toExternalForm());
        
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
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
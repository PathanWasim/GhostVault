package com.ghostvault.ui.components;

import javafx.scene.Scene;

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
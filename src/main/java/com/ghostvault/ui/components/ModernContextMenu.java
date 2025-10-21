package com.ghostvault.ui.components;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

/**
 * Modern styled context menu for file operations
 */
public class ModernContextMenu extends ContextMenu {
    
    private MenuItem openItem;
    private MenuItem previewItem;
    private MenuItem editItem;
    private MenuItem copyItem;
    private MenuItem cutItem;
    private MenuItem pasteItem;
    private MenuItem deleteItem;
    private MenuItem renameItem;
    private MenuItem propertiesItem;
    private MenuItem encryptItem;
    private MenuItem decryptItem;
    private MenuItem compressItem;
    private MenuItem shareItem;
    
    private Consumer<File> onOpen;
    private Consumer<File> onPreview;
    private Consumer<File> onEdit;
    private Consumer<File> onCopy;
    private Consumer<File> onCut;
    private Runnable onPaste;
    private Consumer<List<File>> onDelete;
    private Consumer<File> onRename;
    private Consumer<File> onProperties;
    private Consumer<File> onEncrypt;
    private Consumer<File> onDecrypt;
    private Consumer<File> onCompress;
    private Consumer<File> onShare;
    
    public ModernContextMenu() {
        super();
        initializeMenuItems();
        setupEventHandlers();
        applyStyles();
    }
    
    private void initializeMenuItems() {
        openItem = new MenuItem("Open");
        previewItem = new MenuItem("Preview");
        editItem = new MenuItem("Edit");
        
        copyItem = new MenuItem("Copy");
        cutItem = new MenuItem("Cut");
        pasteItem = new MenuItem("Paste");
        
        deleteItem = new MenuItem("Delete");
        renameItem = new MenuItem("Rename");
        propertiesItem = new MenuItem("Properties");
        
        encryptItem = new MenuItem("Encrypt");
        decryptItem = new MenuItem("Decrypt");
        compressItem = new MenuItem("Compress");
        shareItem = new MenuItem("Share");
        
        this.getItems().addAll(
            openItem,
            previewItem,
            editItem,
            new SeparatorMenuItem(),
            copyItem,
            cutItem,
            pasteItem,
            new SeparatorMenuItem(),
            deleteItem,
            renameItem,
            new SeparatorMenuItem(),
            encryptItem,
            decryptItem,
            compressItem,
            shareItem,
            new SeparatorMenuItem(),
            propertiesItem
        );
    }
    
    private void setupEventHandlers() {
        openItem.setOnAction(e -> {
            if (onOpen != null) {
                onOpen.accept(null); // File will be provided by caller
            }
        });
        
        previewItem.setOnAction(e -> {
            if (onPreview != null) {
                onPreview.accept(null);
            }
        });
        
        editItem.setOnAction(e -> {
            if (onEdit != null) {
                onEdit.accept(null);
            }
        });
        
        copyItem.setOnAction(e -> {
            if (onCopy != null) {
                onCopy.accept(null);
            }
        });
        
        cutItem.setOnAction(e -> {
            if (onCut != null) {
                onCut.accept(null);
            }
        });
        
        pasteItem.setOnAction(e -> {
            if (onPaste != null) {
                onPaste.run();
            }
        });
        
        deleteItem.setOnAction(e -> {
            if (onDelete != null) {
                onDelete.accept(null);
            }
        });
        
        renameItem.setOnAction(e -> {
            if (onRename != null) {
                onRename.accept(null);
            }
        });
        
        propertiesItem.setOnAction(e -> {
            if (onProperties != null) {
                onProperties.accept(null);
            }
        });
        
        encryptItem.setOnAction(e -> {
            if (onEncrypt != null) {
                onEncrypt.accept(null);
            }
        });
        
        decryptItem.setOnAction(e -> {
            if (onDecrypt != null) {
                onDecrypt.accept(null);
            }
        });
        
        compressItem.setOnAction(e -> {
            if (onCompress != null) {
                onCompress.accept(null);
            }
        });
        
        shareItem.setOnAction(e -> {
            if (onShare != null) {
                onShare.accept(null);
            }
        });
    }
    
    private void applyStyles() {
        this.getStyleClass().add("modern-context-menu");
        
        // Apply modern styling to menu items
        String itemStyle = 
            "-fx-background-color: white;" +
            "-fx-text-fill: #333;" +
            "-fx-padding: 8px 16px;";
            
        this.getItems().forEach(item -> {
            if (item instanceof MenuItem) {
                item.getStyleClass().add("modern-menu-item");
            }
        });
    }
    
    public void updateForSelection(List<File> selectedFiles) {
        boolean hasSelection = selectedFiles != null && !selectedFiles.isEmpty();
        boolean singleSelection = hasSelection && selectedFiles.size() == 1;
        
        openItem.setDisable(!singleSelection);
        previewItem.setDisable(!singleSelection);
        editItem.setDisable(!singleSelection);
        renameItem.setDisable(!singleSelection);
        propertiesItem.setDisable(!singleSelection);
        
        copyItem.setDisable(!hasSelection);
        cutItem.setDisable(!hasSelection);
        deleteItem.setDisable(!hasSelection);
        encryptItem.setDisable(!hasSelection);
        decryptItem.setDisable(!hasSelection);
        compressItem.setDisable(!hasSelection);
        shareItem.setDisable(!hasSelection);
    }
    
    // Setters for callbacks
    public void setOnOpen(Consumer<File> callback) {
        this.onOpen = callback;
    }
    
    public void setOnPreview(Consumer<File> callback) {
        this.onPreview = callback;
    }
    
    public void setOnEdit(Consumer<File> callback) {
        this.onEdit = callback;
    }
    
    public void setOnCopy(Consumer<File> callback) {
        this.onCopy = callback;
    }
    
    public void setOnCut(Consumer<File> callback) {
        this.onCut = callback;
    }
    
    public void setOnPaste(Runnable callback) {
        this.onPaste = callback;
    }
    
    public void setOnDelete(Consumer<List<File>> callback) {
        this.onDelete = callback;
    }
    
    public void setOnRename(Consumer<File> callback) {
        this.onRename = callback;
    }
    
    public void setOnProperties(Consumer<File> callback) {
        this.onProperties = callback;
    }
    
    public void setOnEncrypt(Consumer<File> callback) {
        this.onEncrypt = callback;
    }
    
    public void setOnDecrypt(Consumer<File> callback) {
        this.onDecrypt = callback;
    }
    
    public void setOnCompress(Consumer<File> callback) {
        this.onCompress = callback;
    }
    
    public void setOnShare(Consumer<File> callback) {
        this.onShare = callback;
    }
}
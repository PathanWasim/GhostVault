package com.ghostvault.ui.components;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Menu;

import java.io.File;
import java.util.function.Consumer;

/**
 * Enhanced context menu manager for file operations
 */
public class FileContextMenuManager {
    
    private Consumer<File> onOpenFile;
    private Consumer<File> onPreviewFile;
    private Consumer<File> onEditFile;
    private Consumer<File> onDeleteFile;
    private Consumer<File> onRenameFile;
    private Consumer<File> onCopyFile;
    private Consumer<File> onMoveFile;
    private Consumer<File> onShowProperties;
    private Consumer<File> onShowInExplorer;
    private Consumer<File> onCompressFile;
    private Consumer<File> onEncryptFile;
    private Consumer<File> onShareFile;
    
    /**
     * Create enhanced context menu for a file
     */
    public ContextMenu createFileContextMenu(File file) {
        ContextMenu contextMenu = new ContextMenu();
        
        if (file.isDirectory()) {
            createDirectoryContextMenu(contextMenu, file);
        } else {
            createFileContextMenu(contextMenu, file);
        }
        
        return contextMenu;
    }
    
    /**
     * Create context menu for files
     */
    private void createFileContextMenu(ContextMenu contextMenu, File file) {
        // Open actions
        MenuItem openItem = new MenuItem("Open");
        openItem.setOnAction(e -> {
            if (onOpenFile != null) {
                onOpenFile.accept(file);
            }
        });
        
        MenuItem previewItem = new MenuItem("Preview");
        previewItem.setOnAction(e -> {
            if (onPreviewFile != null) {
                onPreviewFile.accept(file);
            }
        });
        
        MenuItem editItem = new MenuItem("Edit");
        editItem.setOnAction(e -> {
            if (onEditFile != null) {
                onEditFile.accept(file);
            }
        });
        
        // File operations
        MenuItem renameItem = new MenuItem("Rename");
        renameItem.setOnAction(e -> {
            if (onRenameFile != null) {
                onRenameFile.accept(file);
            }
        });
        
        MenuItem copyItem = new MenuItem("Copy");
        copyItem.setOnAction(e -> {
            if (onCopyFile != null) {
                onCopyFile.accept(file);
            }
        });
        
        MenuItem moveItem = new MenuItem("Move");
        moveItem.setOnAction(e -> {
            if (onMoveFile != null) {
                onMoveFile.accept(file);
            }
        });
        
        // Advanced operations submenu
        Menu advancedMenu = new Menu("Advanced");
        
        MenuItem compressItem = new MenuItem("Compress");
        compressItem.setOnAction(e -> {
            if (onCompressFile != null) {
                onCompressFile.accept(file);
            }
        });
        
        MenuItem encryptItem = new MenuItem("Encrypt");
        encryptItem.setOnAction(e -> {
            if (onEncryptFile != null) {
                onEncryptFile.accept(file);
            }
        });
        
        MenuItem shareItem = new MenuItem("Share");
        shareItem.setOnAction(e -> {
            if (onShareFile != null) {
                onShareFile.accept(file);
            }
        });
        
        advancedMenu.getItems().addAll(compressItem, encryptItem, shareItem);
        
        // System operations
        MenuItem showInExplorerItem = new MenuItem("Show in Explorer");
        showInExplorerItem.setOnAction(e -> {
            if (onShowInExplorer != null) {
                onShowInExplorer.accept(file);
            }
        });
        
        MenuItem propertiesItem = new MenuItem("Properties");
        propertiesItem.setOnAction(e -> {
            if (onShowProperties != null) {
                onShowProperties.accept(file);
            }
        });
        
        // Delete
        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setOnAction(e -> {
            if (onDeleteFile != null) {
                onDeleteFile.accept(file);
            }
        });
        
        // Add items to context menu
        contextMenu.getItems().addAll(
            openItem,
            previewItem,
            editItem,
            new SeparatorMenuItem(),
            renameItem,
            copyItem,
            moveItem,
            new SeparatorMenuItem(),
            advancedMenu,
            new SeparatorMenuItem(),
            showInExplorerItem,
            propertiesItem,
            new SeparatorMenuItem(),
            deleteItem
        );
        
        // Enable/disable items based on file type
        String extension = getFileExtension(file).toLowerCase();
        boolean isTextFile = isTextFile(extension);
        boolean isImageFile = isImageFile(extension);
        
        editItem.setDisable(!isTextFile);
        previewItem.setDisable(!isImageFile && !isTextFile);
    }
    
    /**
     * Create context menu for directories
     */
    private void createDirectoryContextMenu(ContextMenu contextMenu, File directory) {
        // Open directory
        MenuItem openItem = new MenuItem("Open");
        openItem.setOnAction(e -> {
            if (onOpenFile != null) {
                onOpenFile.accept(directory);
            }
        });
        
        MenuItem showInExplorerItem = new MenuItem("Show in Explorer");
        showInExplorerItem.setOnAction(e -> {
            if (onShowInExplorer != null) {
                onShowInExplorer.accept(directory);
            }
        });
        
        // Directory operations
        MenuItem renameItem = new MenuItem("Rename");
        renameItem.setOnAction(e -> {
            if (onRenameFile != null) {
                onRenameFile.accept(directory);
            }
        });
        
        MenuItem copyItem = new MenuItem("Copy");
        copyItem.setOnAction(e -> {
            if (onCopyFile != null) {
                onCopyFile.accept(directory);
            }
        });
        
        MenuItem moveItem = new MenuItem("Move");
        moveItem.setOnAction(e -> {
            if (onMoveFile != null) {
                onMoveFile.accept(directory);
            }
        });
        
        // Advanced operations
        Menu advancedMenu = new Menu("Advanced");
        
        MenuItem compressItem = new MenuItem("Compress Folder");
        compressItem.setOnAction(e -> {
            if (onCompressFile != null) {
                onCompressFile.accept(directory);
            }
        });
        
        MenuItem encryptItem = new MenuItem("Encrypt Folder");
        encryptItem.setOnAction(e -> {
            if (onEncryptFile != null) {
                onEncryptFile.accept(directory);
            }
        });
        
        advancedMenu.getItems().addAll(compressItem, encryptItem);
        
        // Properties
        MenuItem propertiesItem = new MenuItem("Properties");
        propertiesItem.setOnAction(e -> {
            if (onShowProperties != null) {
                onShowProperties.accept(directory);
            }
        });
        
        // Delete
        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setOnAction(e -> {
            if (onDeleteFile != null) {
                onDeleteFile.accept(directory);
            }
        });
        
        contextMenu.getItems().addAll(
            openItem,
            showInExplorerItem,
            new SeparatorMenuItem(),
            renameItem,
            copyItem,
            moveItem,
            new SeparatorMenuItem(),
            advancedMenu,
            new SeparatorMenuItem(),
            propertiesItem,
            new SeparatorMenuItem(),
            deleteItem
        );
    }
    
    /**
     * Get file extension
     */
    private String getFileExtension(File file) {
        String name = file.getName();
        int lastDot = name.lastIndexOf('.');
        return lastDot > 0 ? name.substring(lastDot + 1) : "";
    }
    
    /**
     * Check if file is a text file
     */
    private boolean isTextFile(String extension) {
        return extension.matches("txt|md|java|js|html|css|xml|json|py|cpp|c|h|php|sql|log");
    }
    
    /**
     * Check if file is an image file
     */
    private boolean isImageFile(String extension) {
        return extension.matches("jpg|jpeg|png|gif|bmp|svg|tiff|tif|webp");
    }
    
    // Setters for callbacks
    public void setOnOpenFile(Consumer<File> onOpenFile) {
        this.onOpenFile = onOpenFile;
    }
    
    public void setOnPreviewFile(Consumer<File> onPreviewFile) {
        this.onPreviewFile = onPreviewFile;
    }
    
    public void setOnEditFile(Consumer<File> onEditFile) {
        this.onEditFile = onEditFile;
    }
    
    public void setOnDeleteFile(Consumer<File> onDeleteFile) {
        this.onDeleteFile = onDeleteFile;
    }
    
    public void setOnRenameFile(Consumer<File> onRenameFile) {
        this.onRenameFile = onRenameFile;
    }
    
    public void setOnCopyFile(Consumer<File> onCopyFile) {
        this.onCopyFile = onCopyFile;
    }
    
    public void setOnMoveFile(Consumer<File> onMoveFile) {
        this.onMoveFile = onMoveFile;
    }
    
    public void setOnShowProperties(Consumer<File> onShowProperties) {
        this.onShowProperties = onShowProperties;
    }
    
    public void setOnShowInExplorer(Consumer<File> onShowInExplorer) {
        this.onShowInExplorer = onShowInExplorer;
    }
    
    public void setOnCompressFile(Consumer<File> onCompressFile) {
        this.onCompressFile = onCompressFile;
    }
    
    public void setOnEncryptFile(Consumer<File> onEncryptFile) {
        this.onEncryptFile = onEncryptFile;
    }
    
    public void setOnShareFile(Consumer<File> onShareFile) {
        this.onShareFile = onShareFile;
    }
}
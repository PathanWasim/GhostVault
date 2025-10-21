package com.ghostvault.ui.components;

import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.util.List;

/**
 * Modern styled file chooser wrapper
 */
public class ModernFileChooser {
    
    private FileChooser fileChooser;
    
    public ModernFileChooser() {
        this.fileChooser = new FileChooser();
        applyModernStyling();
    }
    
    private void applyModernStyling() {
        fileChooser.setTitle("Choose File");
    }
    
    public void setTitle(String title) {
        fileChooser.setTitle(title);
    }
    
    public void setInitialDirectory(File directory) {
        fileChooser.setInitialDirectory(directory);
    }
    
    public void setInitialFileName(String fileName) {
        fileChooser.setInitialFileName(fileName);
    }
    
    public void addExtensionFilter(String description, String... extensions) {
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter(description, extensions);
        fileChooser.getExtensionFilters().add(filter);
    }
    
    public File showOpenDialog(Window ownerWindow) {
        return fileChooser.showOpenDialog(ownerWindow);
    }
    
    public List<File> showOpenMultipleDialog(Window ownerWindow) {
        return fileChooser.showOpenMultipleDialog(ownerWindow);
    }
    
    public File showSaveDialog(Window ownerWindow) {
        return fileChooser.showSaveDialog(ownerWindow);
    }
    
    public FileChooser getFileChooser() {
        return fileChooser;
    }
    
    public void setMultipleSelection(boolean multiple) {
        // FileChooser doesn't have this method directly
        // This is handled by using showOpenMultipleDialog vs showOpenDialog
    }
    
    public void setDirectorySelection(boolean directory) {
        // FileChooser doesn't support directory selection directly
        // Would need to use DirectoryChooser for this
    }
    
    public void setOnFilesSelected(java.util.function.Consumer<List<File>> callback) {
        // This would be handled by the calling code after showOpenDialog
    }
    
    public List<File> showDialog(javafx.stage.Stage stage) {
        List<File> result = showOpenMultipleDialog(stage);
        return result != null ? result : java.util.Collections.emptyList();
    }
}
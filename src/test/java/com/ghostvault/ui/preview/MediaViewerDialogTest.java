package com.ghostvault.ui.preview;

import com.ghostvault.model.VaultFile;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

/**
 * Simple test to verify MediaViewerDialog works correctly
 */
public class MediaViewerDialogTest extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        // Create a test VaultFile
        VaultFile testFile = new VaultFile(
            "test-image.jpg",    // originalName
            "test-file-id-123",  // fileId
            "encrypted-name",    // encryptedName
            1024,                // size
            "test-hash-123",     // hash
            System.currentTimeMillis() // uploadTime
        );
        
        // Create sample image data
        byte[] sampleData = "Sample JPG image data for testing".getBytes();
        
        try {
            // Test MediaViewerDialog creation
            MediaViewerDialog dialog = new MediaViewerDialog(testFile, sampleData);
            dialog.show();
            
            System.out.println("✅ MediaViewerDialog created and shown successfully!");
            
            // Close after 3 seconds
            Platform.runLater(() -> {
                try {
                    Thread.sleep(3000);
                    dialog.close();
                    Platform.exit();
                } catch (InterruptedException e) {
                    Platform.exit();
                }
            });
            
        } catch (Exception e) {
            System.err.println("❌ Error creating MediaViewerDialog: " + e.getMessage());
            e.printStackTrace();
            Platform.exit();
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
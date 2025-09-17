package com.ghostvault.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Comprehensive help system for GhostVault
 * Provides user documentation, tutorials, and contextual help
 */
public class HelpSystem {
    
    private Stage helpStage;
    private TabPane helpTabPane;
    
    /**
     * Show main help window
     */
    public void showHelp(Stage parentStage) {
        if (helpStage != null && helpStage.isShowing()) {
            helpStage.toFront();
            return;
        }
        
        helpStage = new Stage();
        helpStage.initModality(Modality.NONE);
        helpStage.initOwner(parentStage);
        helpStage.setTitle("GhostVault Help & Documentation");
        
        createHelpContent();
        
        Scene scene = new Scene(helpTabPane, 800, 600);
        scene.getStylesheets().add(getClass().getResource("/styles/common.css").toExternalForm());
        
        helpStage.setScene(scene);
        helpStage.show();
    }
    
    /**
     * Create help content with tabs
     */
    private void createHelpContent() {
        helpTabPane = new TabPane();
        
        // Getting Started tab
        Tab gettingStartedTab = new Tab("Getting Started");
        gettingStartedTab.setClosable(false);
        gettingStartedTab.setContent(createGettingStartedContent());
        
        // Security Features tab
        Tab securityTab = new Tab("Security Features");
        securityTab.setClosable(false);
        securityTab.setContent(createSecurityContent());
        
        // File Management tab
        Tab fileManagementTab = new Tab("File Management");
        fileManagementTab.setClosable(false);
        fileManagementTab.setContent(createFileManagementContent());
        
        // Backup & Restore tab
        Tab backupTab = new Tab("Backup & Restore");
        backupTab.setClosable(false);
        backupTab.setContent(createBackupContent());
        
        // Troubleshooting tab
        Tab troubleshootingTab = new Tab("Troubleshooting");
        troubleshootingTab.setClosable(false);
        troubleshootingTab.setContent(createTroubleshootingContent());
        
        // Keyboard Shortcuts tab
        Tab shortcutsTab = new Tab("Keyboard Shortcuts");
        shortcutsTab.setClosable(false);
        shortcutsTab.setContent(createShortcutsContent());
        
        helpTabPane.getTabs().addAll(
            gettingStartedTab,
            securityTab,
            fileManagementTab,
            backupTab,
            troubleshootingTab,
            shortcutsTab
        );
    }
    
    /**
     * Create getting started content
     */
    private ScrollPane createGettingStartedContent() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        
        Label title = new Label("Getting Started with GhostVault");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        Label intro = new Label("Welcome to GhostVault - your secure file storage solution.");
        intro.setWrapText(true);
        
        String gettingStartedText = 
            "GhostVault is a secure file vault that protects your sensitive files with military-grade encryption.\n\n" +
            "First Time Setup:\n" +
            "1. When you first run GhostVault, you'll be prompted to create three passwords:\n" +
            "   • Master Password: Provides full access to your vault\n" +
            "   • Panic Password: Triggers emergency data destruction\n" +
            "   • Decoy Password: Shows fake files to mislead attackers\n\n" +
            "2. All passwords must be strong and different from each other\n" +
            "3. Remember your master password - it cannot be recovered if lost\n\n" +
            "Basic Usage:\n" +
            "• Upload files using the Upload button - they are automatically encrypted\n" +
            "• Download files using the Download button - they are decrypted for you\n" +
            "• Delete files securely using the Delete button - they are overwritten multiple times\n" +
            "• Search for files using the search box\n\n" +
            "Security Features:\n" +
            "• All files are encrypted with AES-256 encryption\n" +
            "• Passwords are hashed with PBKDF2 (100,000+ iterations)\n" +
            "• Files are stored with random names to hide their identity\n" +
            "• Secure deletion overwrites files multiple times\n" +
            "• Session timeout automatically locks the vault";
        
        TextArea textArea = new TextArea(gettingStartedText);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefRowCount(20);
        
        content.getChildren().addAll(title, intro, textArea);
        
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        return scrollPane;
    }
    
    /**
     * Create security features content
     */
    private ScrollPane createSecurityContent() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        
        Label title = new Label("Security Features");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        String securityText = 
            "GhostVault implements multiple layers of security to protect your data:\n\n" +
            "Encryption:\n" +
            "• AES-256-CBC encryption for all files\n" +
            "• PBKDF2WithHmacSHA256 for password hashing (100,000+ iterations)\n" +
            "• Cryptographically secure random IV generation\n" +
            "• SHA-256 file integrity verification\n\n" +
            "Password Security:\n" +
            "• Three-password system (Master, Panic, Decoy)\n" +
            "• Strong password requirements enforced\n" +
            "• Passwords stored as salted hashes only\n" +
            "• Memory wiping after password use\n\n" +
            "Panic Mode:\n" +
            "• Triggered by entering the panic password\n" +
            "• Silently destroys all vault data\n" +
            "• Overwrites files multiple times (DoD 5220.22-M standard)\n" +
            "• Appears as normal failed login to observers\n\n" +
            "Decoy Mode:\n" +
            "• Triggered by entering the decoy password\n" +
            "• Shows fake files to mislead attackers\n" +
            "• Completely separate from real vault data\n" +
            "• Maintains plausible deniability";
        
        TextArea textArea = new TextArea(securityText);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefRowCount(25);
        
        content.getChildren().addAll(title, textArea);
        
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        return scrollPane;
    }
    
    /**
     * Create file management content
     */
    private ScrollPane createFileManagementContent() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        
        Label title = new Label("File Management");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        String fileManagementText = 
            "Managing Files in GhostVault:\n\n" +
            "Uploading Files:\n" +
            "1. Click the Upload button\n" +
            "2. Select the file you want to encrypt and store\n" +
            "3. The file will be encrypted and added to your vault\n\n" +
            "Downloading Files:\n" +
            "1. Select a file from the list\n" +
            "2. Click the Download button\n" +
            "3. Choose where to save the decrypted file\n\n" +
            "Deleting Files:\n" +
            "1. Select a file from the list\n" +
            "2. Click the Delete button\n" +
            "3. Confirm the deletion in the dialog\n\n" +
            "Searching Files:\n" +
            "• Use the search box to find files by name\n" +
            "• Search is performed on original file names\n" +
            "• Results update in real-time as you type";
        
        TextArea textArea = new TextArea(fileManagementText);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefRowCount(25);
        
        content.getChildren().addAll(title, textArea);
        
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        return scrollPane;
    }
    
    /**
     * Create backup content
     */
    private ScrollPane createBackupContent() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        
        Label title = new Label("Backup & Restore");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        String backupText = 
            "Protecting Your Vault with Backups:\n\n" +
            "Creating Backups:\n" +
            "1. Click the Backup button\n" +
            "2. Choose a location to save the backup file\n" +
            "3. Enter your master password to confirm\n" +
            "4. Backup file is created with .gvbackup extension\n\n" +
            "Restoring from Backup:\n" +
            "1. Click the Restore button\n" +
            "2. Select the .gvbackup file to restore from\n" +
            "3. Enter your master password\n" +
            "4. Choose whether to merge or replace existing files\n\n" +
            "Best Practices:\n" +
            "• Create regular backups (weekly or monthly)\n" +
            "• Store backups in multiple secure locations\n" +
            "• Test restore process periodically\n" +
            "• Keep backups encrypted and password-protected";
        
        TextArea textArea = new TextArea(backupText);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefRowCount(25);
        
        content.getChildren().addAll(title, textArea);
        
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        return scrollPane;
    }
    
    /**
     * Create troubleshooting content
     */
    private ScrollPane createTroubleshootingContent() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        
        Label title = new Label("Troubleshooting");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        String troubleshootingText = 
            "Common Issues and Solutions:\n\n" +
            "Login Problems:\n" +
            "• Invalid password error: Verify caps lock is off, check for extra spaces\n" +
            "• Too many failed attempts: Wait for lockout period to expire\n\n" +
            "File Upload Issues:\n" +
            "• Invalid file name error: Use only letters, numbers, spaces, dots, and dashes\n" +
            "• File already exists error: Choose a different name for the file\n\n" +
            "Performance Issues:\n" +
            "• Slow encryption/decryption: Large files take longer to process\n" +
            "• Application freezing: Wait for current operation to complete\n\n" +
            "Security Concerns:\n" +
            "• Forgot master password: No recovery possible - this is by design\n" +
            "• Suspicious activity detected: Check audit logs for details\n\n" +
            "Getting Help:\n" +
            "• Check this help documentation first\n" +
            "• Review console output for error details\n" +
            "• Note exact error messages and steps to reproduce";
        
        TextArea textArea = new TextArea(troubleshootingText);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefRowCount(30);
        
        content.getChildren().addAll(title, textArea);
        
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        return scrollPane;
    }
    
    /**
     * Create keyboard shortcuts content
     */
    private ScrollPane createShortcutsContent() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        
        Label title = new Label("Keyboard Shortcuts");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        String shortcutsText = 
            "Keyboard Shortcuts and Navigation:\n\n" +
            "General Navigation:\n" +
            "• Tab / Shift+Tab: Navigate between controls\n" +
            "• Enter: Activate focused button or control\n" +
            "• Escape: Close dialogs or cancel operations\n" +
            "• F1: Show help documentation\n\n" +
            "File Operations:\n" +
            "• Ctrl+U: Upload file\n" +
            "• Ctrl+D: Download selected file\n" +
            "• Delete: Secure delete selected file\n" +
            "• Ctrl+F: Focus search field\n\n" +
            "Vault Operations:\n" +
            "• Ctrl+B: Create backup\n" +
            "• Ctrl+R: Restore from backup\n" +
            "• Ctrl+L: Logout and lock vault\n" +
            "• Ctrl+Q: Quit application\n\n" +
            "Theme and Accessibility:\n" +
            "• Ctrl+T: Toggle dark/light theme\n" +
            "• Ctrl+H: Toggle high contrast mode";
        
        TextArea textArea = new TextArea(shortcutsText);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefRowCount(30);
        
        content.getChildren().addAll(title, textArea);
        
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        return scrollPane;
    }
}
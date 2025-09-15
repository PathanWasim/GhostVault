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
        
        String gettingStartedText = """
            GhostVault is a secure file vault that protects your sensitive files with military-grade encryption.
            
            First Time Setup:
            1. When you first run GhostVault, you'll be prompted to create three passwords:
               • Master Password: Provides full access to your vault
               • Panic Password: Triggers emergency data destruction
               • Decoy Password: Shows fake files to mislead attackers
            
            2. All passwords must be strong and different from each other
            3. Remember your master password - it cannot be recovered if lost
            
            Basic Usage:
            • Upload files using the Upload button - they are automatically encrypted
            • Download files using the Download button - they are decrypted for you
            • Delete files securely using the Delete button - they are overwritten multiple times
            • Search for files using the search box
            
            Security Features:
            • All files are encrypted with AES-256 encryption
            • Passwords are hashed with PBKDF2 (100,000+ iterations)
            • Files are stored with random names to hide their identity
            • Secure deletion overwrites files multiple times
            • Session timeout automatically locks the vault
            """;
        
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
        
        String securityText = """
            GhostVault implements multiple layers of security to protect your data:
            
            Encryption:
            • AES-256-CBC encryption for all files
            • PBKDF2WithHmacSHA256 for password hashing (100,000+ iterations)
            • Cryptographically secure random IV generation
            • SHA-256 file integrity verification
            
            Password Security:
            • Three-password system (Master, Panic, Decoy)
            • Strong password requirements enforced
            • Passwords stored as salted hashes only
            • Memory wiping after password use
            
            Panic Mode:
            • Triggered by entering the panic password
            • Silently destroys all vault data
            • Overwrites files multiple times (DoD 5220.22-M standard)
            • Appears as normal failed login to observers
            
            Decoy Mode:
            • Triggered by entering the decoy password
            • Shows fake files to mislead attackers
            • Completely separate from real vault data
            • Maintains plausible deniability
            
            Session Security:
            • Automatic session timeout (configurable)
            • Activity monitoring (mouse/keyboard)
            • Failed login attempt tracking
            • Automatic decoy mode after max failed attempts
            
            File Security:
            • Files stored with random UUID names
            • Metadata encrypted separately
            • Secure deletion with multiple overwrites
            • File integrity verification on access
            
            Advanced Security:
            • Memory protection and secure wiping
            • Anti-debugging measures
            • Threat detection and monitoring
            • File system protection
            """;
        
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
        
        String fileManagementText = """
            Managing Files in GhostVault:
            
            Uploading Files:
            1. Click the "Upload" button (📁)
            2. Select the file you want to encrypt and store
            3. The file will be encrypted and added to your vault
            4. Original file names are preserved in encrypted metadata
            
            Downloading Files:
            1. Select a file from the list
            2. Click the "Download" button (💾)
            3. Choose where to save the decrypted file
            4. File integrity is automatically verified during download
            
            Deleting Files:
            1. Select a file from the list
            2. Click the "Delete" button (🗑️)
            3. Confirm the deletion in the dialog
            4. File is securely overwritten multiple times
            
            Searching Files:
            • Use the search box to find files by name
            • Search is performed on original file names
            • Results update in real-time as you type
            
            File Information:
            • File list shows original names, sizes, and upload dates
            • All files are automatically encrypted with AES-256
            • File integrity is verified with SHA-256 hashes
            • Maximum file name length is 100 characters
            
            Supported File Types:
            • All file types are supported
            • No file size limits (limited only by available disk space)
            • Binary and text files are handled equally
            • File extensions are preserved
            
            File Storage:
            • Files are stored in encrypted form with random names
            • Metadata is stored separately and encrypted
            • Original file structure is not preserved (flat storage)
            • Files cannot be accessed without the master password
            """;
        
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
        
        String backupText = """
            Protecting Your Vault with Backups:
            
            Creating Backups:
            1. Click the "Backup" button (📦)
            2. Choose a location to save the backup file
            3. Enter your master password to confirm
            4. Backup file is created with .gvbackup extension
            
            Backup Contents:
            • All encrypted files in your vault
            • Encrypted metadata and file information
            • Vault configuration (excluding passwords)
            • Audit logs and security information
            
            Backup Security:
            • Backup files are encrypted with your master password
            • Same AES-256 encryption as your vault files
            • Backup integrity is verified with checksums
            • Passwords are never stored in backups
            
            Restoring from Backup:
            1. Click the "Restore" button (📥)
            2. Select the .gvbackup file to restore from
            3. Enter your master password
            4. Choose whether to merge or replace existing files
            
            Restore Options:
            • Merge: Adds backup files to existing vault
            • Replace: Completely replaces vault with backup contents
            • Conflict resolution for duplicate file names
            • Integrity verification during restore process
            
            Best Practices:
            • Create regular backups (weekly or monthly)
            • Store backups in multiple secure locations
            • Test restore process periodically
            • Keep backups encrypted and password-protected
            • Never store backup passwords with backup files
            
            Backup File Format:
            • Custom encrypted format (.gvbackup)
            • Cannot be opened by other applications
            • Includes version information for compatibility
            • Compressed to reduce file size
            
            Recovery Scenarios:
            • Hardware failure or disk corruption
            • Accidental vault deletion
            • Moving vault to new computer
            • Recovering from panic mode activation
            """;
        
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
        
        String troubleshootingText = """
            Common Issues and Solutions:
            
            Login Problems:
            • "Invalid password" error:
              - Verify caps lock is off
              - Check for extra spaces
              - Ensure correct password type (master/decoy)
            
            • Too many failed attempts:
              - Wait for lockout period to expire
              - Application may switch to decoy mode automatically
              - Restart application to reset attempt counter
            
            File Upload Issues:
            • "Invalid file name" error:
              - Use only letters, numbers, spaces, dots, and dashes
              - Keep file names under 100 characters
              - Avoid special characters like / \ : * ? " < > |
            
            • "File already exists" error:
              - Choose a different name for the file
              - Delete the existing file first (if intended)
              - Check for hidden characters in file name
            
            Performance Issues:
            • Slow encryption/decryption:
              - Large files take longer to process
              - Close other applications to free memory
              - Ensure sufficient disk space available
            
            • Application freezing:
              - Wait for current operation to complete
              - Restart application if unresponsive
              - Check system resources (CPU, memory)
            
            Backup/Restore Problems:
            • "Backup failed" error:
              - Ensure sufficient disk space for backup
              - Check write permissions to backup location
              - Verify master password is correct
            
            • "Restore failed" error:
              - Verify backup file is not corrupted
              - Ensure backup file is from compatible version
              - Check available disk space in vault directory
            
            Security Concerns:
            • Forgot master password:
              - No recovery possible - this is by design
              - Restore from backup if available
              - Panic mode can be used to destroy data securely
            
            • Suspicious activity detected:
              - Check audit logs for details
              - Change passwords if compromise suspected
              - Consider creating new vault with fresh passwords
            
            Technical Issues:
            • Application won't start:
              - Check Java version (requires Java 11+)
              - Verify JavaFX is properly installed
              - Check console output for error messages
            
            • Vault directory corruption:
              - Restore from backup immediately
              - Do not attempt manual file recovery
              - Contact support if backup is unavailable
            
            Getting Help:
            • Check this help documentation first
            • Review console output for error details
            • Note exact error messages and steps to reproduce
            • Consider creating a backup before troubleshooting
            """;
        
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
        
        String shortcutsText = """
            Keyboard Shortcuts and Navigation:
            
            General Navigation:
            • Tab / Shift+Tab: Navigate between controls
            • Enter: Activate focused button or control
            • Escape: Close dialogs or cancel operations
            • F1: Show help documentation
            
            File Operations:
            • Ctrl+U: Upload file
            • Ctrl+D: Download selected file
            • Delete: Secure delete selected file
            • Ctrl+F: Focus search field
            • Ctrl+A: Select all files (in file list)
            
            Vault Operations:
            • Ctrl+B: Create backup
            • Ctrl+R: Restore from backup
            • Ctrl+L: Logout and lock vault
            • Ctrl+Q: Quit application
            
            Theme and Accessibility:
            • Ctrl+T: Toggle dark/light theme
            • Ctrl+H: Toggle high contrast mode
            • Ctrl+Shift+R: Toggle screen reader mode
            • Ctrl+Plus: Increase font size
            • Ctrl+Minus: Decrease font size
            
            Security Shortcuts:
            • Ctrl+Shift+L: Force immediate logout
            • Ctrl+Shift+P: Show panic mode information
            • Ctrl+Shift+D: Show decoy mode information
            
            List Navigation:
            • Up/Down arrows: Navigate file list
            • Home: Go to first file
            • End: Go to last file
            • Page Up/Down: Scroll file list
            
            Text Field Navigation:
            • Ctrl+A: Select all text
            • Ctrl+C: Copy selected text
            • Ctrl+V: Paste text
            • Ctrl+X: Cut selected text
            • Ctrl+Z: Undo last action
            
            Dialog Navigation:
            • Enter: Confirm/OK button
            • Escape: Cancel/Close button
            • Tab: Move between dialog controls
            
            Accessibility Features:
            • All controls are keyboard accessible
            • Screen reader compatible
            • High contrast mode available
            • Tooltips provide additional information
            • Focus indicators show current control
            
            Tips:
            • Hold Shift while pressing Tab to navigate backwards
            • Use arrow keys within lists and menus
            • Press Space to activate checkboxes and buttons
            • Use Ctrl+Tab to switch between application areas
            """;
        
        TextArea textArea = new TextArea(shortcutsText);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefRowCount(30);
        
        content.getChildren().addAll(title, textArea);
        
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        return scrollPane;
    }
package com.ghostvault.ui.components;

/**
 * Predefined tooltips for common UI elements
 */
public class CommonTooltips {
    
    // File operations
    public static final String UPLOAD_FILES = "Upload files to the vault";
    public static final String UPLOAD_FILES_DESC = "Select one or more files from your computer to securely store in the vault. Supported formats include documents, images, videos, and archives.";
    public static final String UPLOAD_SHORTCUT = "Ctrl+U";
    
    public static final String DOWNLOAD_FILES = "Download selected files";
    public static final String DOWNLOAD_FILES_DESC = "Download the selected files to your computer. Files will be decrypted during the download process.";
    public static final String DOWNLOAD_SHORTCUT = "Ctrl+D";
    
    public static final String DELETE_FILES = "Delete selected files";
    public static final String DELETE_FILES_DESC = "Permanently delete the selected files from the vault. This action cannot be undone.";
    public static final String DELETE_SHORTCUT = "Delete";
    
    public static final String SECURE_DELETE = "Secure delete";
    public static final String SECURE_DELETE_DESC = "Securely overwrite file data multiple times before deletion to prevent recovery. Use for sensitive files.";
    
    // Search and navigation
    public static final String SEARCH_FILES = "Search files and folders";
    public static final String SEARCH_FILES_DESC = "Search for files by name, content, or metadata. Use filters to narrow down results by file type, size, or date.";
    public static final String SEARCH_SHORTCUT = "Ctrl+F";
    
    public static final String FILTER_FILES = "Filter files";
    public static final String FILTER_FILES_DESC = "Apply filters to show only specific types of files, sizes, or date ranges.";
    
    public static final String SORT_FILES = "Sort files";
    public static final String SORT_FILES_DESC = "Change the sorting order of files by name, size, date modified, or file type.";
    
    // Security features
    public static final String MASTER_MODE = "Master Mode";
    public static final String MASTER_MODE_DESC = "Full access to all vault features and real data. Use your master password to access this mode.";
    
    public static final String PANIC_MODE = "Panic Mode";
    public static final String PANIC_MODE_DESC = "Emergency data destruction mode. Activates when panic password is entered. All vault data will be permanently destroyed.";
    
    public static final String DECOY_MODE = "Decoy Mode";
    public static final String DECOY_MODE_DESC = "Fake vault with dummy data. Provides plausible deniability by showing convincing but fake files and folders.";
    
    public static final String BACKUP_VAULT = "Create encrypted backup";
    public static final String BACKUP_VAULT_DESC = "Create a secure, encrypted backup of your vault data. Backups are compressed and can be restored later.";
    public static final String BACKUP_SHORTCUT = "Ctrl+B";
    
    public static final String RESTORE_VAULT = "Restore from backup";
    public static final String RESTORE_VAULT_DESC = "Restore vault data from a previously created encrypted backup file.";
    public static final String RESTORE_SHORTCUT = "Ctrl+R";
    
    // Preview and viewing
    public static final String PREVIEW_FILE = "Preview file";
    public static final String PREVIEW_FILE_DESC = "View file contents without downloading. Supports images, videos, audio, documents, and code files.";
    public static final String PREVIEW_SHORTCUT = "Space";
    
    public static final String ZOOM_IN = "Zoom in";
    public static final String ZOOM_IN_DESC = "Increase the zoom level of the preview.";
    public static final String ZOOM_IN_SHORTCUT = "Ctrl++";
    
    public static final String ZOOM_OUT = "Zoom out";
    public static final String ZOOM_OUT_DESC = "Decrease the zoom level of the preview.";
    public static final String ZOOM_OUT_SHORTCUT = "Ctrl+-";
    
    public static final String FIT_TO_WINDOW = "Fit to window";
    public static final String FIT_TO_WINDOW_DESC = "Resize the preview to fit within the available space.";
    public static final String FIT_TO_WINDOW_SHORTCUT = "Ctrl+0";
    
    // Batch operations
    public static final String SELECT_ALL = "Select all files";
    public static final String SELECT_ALL_DESC = "Select all visible files in the current folder for batch operations.";
    public static final String SELECT_ALL_SHORTCUT = "Ctrl+A";
    
    public static final String DESELECT_ALL = "Deselect all files";
    public static final String DESELECT_ALL_DESC = "Clear the current file selection.";
    public static final String DESELECT_ALL_SHORTCUT = "Ctrl+D";
    
    public static final String BATCH_DOWNLOAD = "Download selected files";
    public static final String BATCH_DOWNLOAD_DESC = "Download all selected files as a single operation. Large selections may be compressed into an archive.";
    
    public static final String BATCH_DELETE = "Delete selected files";
    public static final String BATCH_DELETE_DESC = "Delete all selected files. You will be prompted for confirmation before deletion.";
    
    // Settings and configuration
    public static final String SETTINGS = "Application settings";
    public static final String SETTINGS_DESC = "Configure vault settings, security options, and user preferences.";
    public static final String SETTINGS_SHORTCUT = "Ctrl+,";
    
    public static final String LOGOUT = "Logout";
    public static final String LOGOUT_DESC = "End the current session and return to the login screen. All cached data will be cleared.";
    public static final String LOGOUT_SHORTCUT = "Ctrl+L";
    
    public static final String EXIT = "Exit application";
    public static final String EXIT_DESC = "Close the application completely. All data will be securely cleared from memory.";
    public static final String EXIT_SHORTCUT = "Alt+F4";
    
    // Help and information
    public static final String HELP = "Help and documentation";
    public static final String HELP_DESC = "Access user guides, keyboard shortcuts, and troubleshooting information.";
    public static final String HELP_SHORTCUT = "F1";
    
    public static final String ABOUT = "About GhostVault";
    public static final String ABOUT_DESC = "View application version, license information, and credits.";
    
    // Error and warning messages
    public static final String NETWORK_ERROR = "Network connection error";
    public static final String NETWORK_ERROR_DESC = "Unable to connect to the server. Check your internet connection and try again.";
    
    public static final String PERMISSION_ERROR = "Permission denied";
    public static final String PERMISSION_ERROR_DESC = "You don't have permission to perform this operation. Contact your administrator if needed.";
    
    public static final String DISK_SPACE_WARNING = "Low disk space";
    public static final String DISK_SPACE_WARNING_DESC = "Available disk space is running low. Consider freeing up space or moving files to another location.";
    
    public static final String UNSUPPORTED_FORMAT = "Unsupported file format";
    public static final String UNSUPPORTED_FORMAT_DESC = "This file format is not supported for preview. You can still download and open it with an appropriate application.";
}
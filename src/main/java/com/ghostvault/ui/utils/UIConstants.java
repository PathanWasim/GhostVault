package com.ghostvault.ui.utils;

import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * UI Constants for consistent styling and behavior
 */
public final class UIConstants {
    
    // Private constructor to prevent instantiation
    private UIConstants() {}
    
    // Color Palette
    public static final class Colors {
        public static final Color PRIMARY = Color.web("#4CAF50");
        public static final Color PRIMARY_DARK = Color.web("#45a049");
        public static final Color PRIMARY_LIGHT = Color.web("#5CBF60");
        
        public static final Color SECONDARY = Color.web("#666666");
        public static final Color SECONDARY_DARK = Color.web("#555555");
        public static final Color SECONDARY_LIGHT = Color.web("#777777");
        
        public static final Color DANGER = Color.web("#f44336");
        public static final Color DANGER_DARK = Color.web("#d32f2f");
        public static final Color DANGER_LIGHT = Color.web("#f66356");
        
        public static final Color WARNING = Color.web("#ff9800");
        public static final Color INFO = Color.web("#2196f3");
        public static final Color SUCCESS = Color.web("#4CAF50");
        
        public static final Color BACKGROUND = Color.web("#2b2b2b");
        public static final Color BACKGROUND_LIGHT = Color.web("#3c3c3c");
        public static final Color BACKGROUND_DARK = Color.web("#1e1e1e");
        
        public static final Color TEXT_PRIMARY = Color.web("#ffffff");
        public static final Color TEXT_SECONDARY = Color.web("#cccccc");
        public static final Color TEXT_MUTED = Color.web("#aaaaaa");
        
        public static final Color BORDER = Color.web("#555555");
        public static final Color BORDER_LIGHT = Color.web("#666666");
        public static final Color BORDER_DARK = Color.web("#444444");
    }
    
    // Font Sizes
    public static final class FontSizes {
        public static final double EXTRA_LARGE = 24.0;
        public static final double LARGE = 18.0;
        public static final double MEDIUM = 14.0;
        public static final double NORMAL = 12.0;
        public static final double SMALL = 11.0;
        public static final double EXTRA_SMALL = 10.0;
    }
    
    // Spacing
    public static final class Spacing {
        public static final double EXTRA_LARGE = 30.0;
        public static final double LARGE = 20.0;
        public static final double MEDIUM = 15.0;
        public static final double NORMAL = 10.0;
        public static final double SMALL = 5.0;
        public static final double EXTRA_SMALL = 2.0;
    }
    
    // Border Radius
    public static final class BorderRadius {
        public static final double LARGE = 8.0;
        public static final double MEDIUM = 6.0;
        public static final double NORMAL = 4.0;
        public static final double SMALL = 2.0;
    }
    
    // Animation Durations
    public static final class Animations {
        public static final Duration FAST = Duration.millis(150);
        public static final Duration NORMAL = Duration.millis(300);
        public static final Duration SLOW = Duration.millis(500);
        public static final Duration EXTRA_SLOW = Duration.millis(800);
    }
    
    // Component Sizes
    public static final class Sizes {
        // Button sizes
        public static final double BUTTON_HEIGHT = 32.0;
        public static final double BUTTON_HEIGHT_SMALL = 24.0;
        public static final double BUTTON_HEIGHT_LARGE = 40.0;
        
        // Icon sizes
        public static final double ICON_SMALL = 16.0;
        public static final double ICON_MEDIUM = 24.0;
        public static final double ICON_LARGE = 32.0;
        public static final double ICON_EXTRA_LARGE = 48.0;
        
        // Thumbnail sizes
        public static final double THUMBNAIL_SMALL = 64.0;
        public static final double THUMBNAIL_MEDIUM = 120.0;
        public static final double THUMBNAIL_LARGE = 200.0;
        
        // Panel widths
        public static final double PANEL_WIDTH_SMALL = 250.0;
        public static final double PANEL_WIDTH_MEDIUM = 350.0;
        public static final double PANEL_WIDTH_LARGE = 450.0;
    }
    
    // File Size Limits
    public static final class FileLimits {
        public static final long MAX_PREVIEW_SIZE = 50 * 1024 * 1024; // 50MB
        public static final long MAX_THUMBNAIL_SIZE = 10 * 1024 * 1024; // 10MB
        public static final long LARGE_FILE_THRESHOLD = 100 * 1024 * 1024; // 100MB
        
        public static final int MAX_FILES_BULK_OPERATION = 1000;
        public static final int MAX_SEARCH_RESULTS = 10000;
    }
    
    // UI Behavior Constants
    public static final class Behavior {
        public static final int TOOLTIP_DELAY_MS = 500;
        public static final int NOTIFICATION_DURATION_MS = 5000;
        public static final int PROGRESS_UPDATE_INTERVAL_MS = 100;
        
        public static final double ZOOM_FACTOR = 1.2;
        public static final double MIN_ZOOM = 0.1;
        public static final double MAX_ZOOM = 5.0;
        
        public static final int SEARCH_DEBOUNCE_MS = 300;
        public static final int AUTO_SAVE_INTERVAL_MS = 30000;
    }
    
    // CSS Style Classes
    public static final class StyleClasses {
        // Component classes
        public static final String PROFESSIONAL_HEADER = "professional-header";
        public static final String FILE_TABLE = "file-table";
        public static final String SEARCH_FIELD = "search-field";
        public static final String CODE_PREVIEW = "code-preview-component";
        public static final String IMAGE_PREVIEW = "image-preview-component";
        public static final String DRAG_DROP_ZONE = "drag-drop-zone";
        
        // Button classes
        public static final String BUTTON_PRIMARY = "button-primary";
        public static final String BUTTON_SECONDARY = "button-secondary";
        public static final String BUTTON_DANGER = "danger-button";
        public static final String BUTTON_SUCCESS = "success-button";
        
        // Status classes
        public static final String STATUS_SUCCESS = "status-badge-success";
        public static final String STATUS_ERROR = "status-badge-error";
        public static final String STATUS_WARNING = "status-badge-warning";
        public static final String STATUS_INFO = "status-badge-info";
        
        // Text classes
        public static final String TEXT_TITLE = "panel-title";
        public static final String TEXT_SUBTITLE = "section-title";
        public static final String TEXT_MUTED = "text-muted";
        public static final String TEXT_ERROR = "error-text";
        
        // Layout classes
        public static final String PANEL = "panel";
        public static final String TOOLBAR = "toolbar";
        public static final String STATUS_BAR = "status-bar";
        public static final String OPERATIONS_PANEL = "operations-panel";
    }
    
    // File Type Categories
    public static final class FileTypes {
        public static final String[] IMAGE_EXTENSIONS = {
            "jpg", "jpeg", "png", "gif", "bmp", "svg", "tiff", "tif", "webp"
        };
        
        public static final String[] VIDEO_EXTENSIONS = {
            "mp4", "avi", "mkv", "mov", "wmv", "flv", "webm", "m4v"
        };
        
        public static final String[] AUDIO_EXTENSIONS = {
            "mp3", "wav", "flac", "aac", "ogg", "wma", "m4a"
        };
        
        public static final String[] DOCUMENT_EXTENSIONS = {
            "pdf", "doc", "docx", "txt", "rtf", "odt", "pages"
        };
        
        public static final String[] SPREADSHEET_EXTENSIONS = {
            "xls", "xlsx", "csv", "ods", "numbers"
        };
        
        public static final String[] PRESENTATION_EXTENSIONS = {
            "ppt", "pptx", "odp", "key"
        };
        
        public static final String[] ARCHIVE_EXTENSIONS = {
            "zip", "rar", "7z", "tar", "gz", "bz2", "xz"
        };
        
        public static final String[] CODE_EXTENSIONS = {
            "java", "cpp", "c", "h", "py", "js", "html", "css", "xml", "json",
            "php", "rb", "go", "rs", "swift", "kt", "scala", "sh", "bat"
        };
    }
    
    // Security Constants
    public static final class Security {
        public static final int MIN_PASSWORD_LENGTH = 8;
        public static final int MAX_FAILED_ATTEMPTS = 3;
        public static final int SESSION_TIMEOUT_MINUTES = 30;
        
        public static final String ENCRYPTION_ALGORITHM = "AES";
        public static final String ENCRYPTION_TRANSFORMATION = "AES/CBC/PKCS5Padding";
        public static final int ENCRYPTION_KEY_LENGTH = 256;
        public static final int ENCRYPTION_IV_LENGTH = 16;
        
        public static final String BACKUP_EXTENSION = ".gvbackup";
        public static final String TEMP_FILE_PREFIX = "ghostvault_";
    }
    
    // Application Constants
    public static final class Application {
        public static final String NAME = "GhostVault";
        public static final String VERSION = "2.0.0";
        public static final String AUTHOR = "GhostVault Team";
        
        public static final String CONFIG_DIR = ".ghostvault";
        public static final String CACHE_DIR = "cache";
        public static final String LOGS_DIR = "logs";
        public static final String BACKUPS_DIR = "backups";
        
        public static final String MAIN_CONFIG_FILE = "config.properties";
        public static final String THEME_CONFIG_FILE = "theme.properties";
        public static final String SHORTCUTS_CONFIG_FILE = "shortcuts.properties";
    }
    
    // Keyboard Shortcuts
    public static final class Shortcuts {
        public static final String UPLOAD = "Ctrl+U";
        public static final String DOWNLOAD = "Ctrl+S";
        public static final String DELETE = "Delete";
        public static final String RENAME = "F2";
        public static final String REFRESH = "F5";
        public static final String SELECT_ALL = "Ctrl+A";
        public static final String SEARCH = "Ctrl+F";
        public static final String NEW_FOLDER = "Ctrl+Shift+N";
        public static final String BACKUP = "Ctrl+B";
        public static final String RESTORE = "Ctrl+R";
        public static final String PROPERTIES = "Alt+Enter";
        public static final String PREVIEW = "Space";
        public static final String ZOOM_IN = "Ctrl+Plus";
        public static final String ZOOM_OUT = "Ctrl+Minus";
        public static final String ZOOM_RESET = "Ctrl+0";
        public static final String FIT_TO_WINDOW = "Ctrl+Shift+F";
    }
    
    // Error Messages
    public static final class ErrorMessages {
        public static final String FILE_NOT_FOUND = "File not found or inaccessible";
        public static final String PERMISSION_DENIED = "Permission denied";
        public static final String DISK_FULL = "Insufficient disk space";
        public static final String INVALID_FILE_TYPE = "Unsupported file type";
        public static final String OPERATION_CANCELLED = "Operation cancelled by user";
        public static final String NETWORK_ERROR = "Network connection error";
        public static final String AUTHENTICATION_FAILED = "Authentication failed";
        public static final String ENCRYPTION_ERROR = "Encryption/decryption error";
        public static final String BACKUP_FAILED = "Backup operation failed";
        public static final String RESTORE_FAILED = "Restore operation failed";
    }
    
    // Success Messages
    public static final class SuccessMessages {
        public static final String FILE_UPLOADED = "File uploaded successfully";
        public static final String FILE_DOWNLOADED = "File downloaded successfully";
        public static final String FILE_DELETED = "File deleted successfully";
        public static final String BACKUP_CREATED = "Backup created successfully";
        public static final String BACKUP_RESTORED = "Backup restored successfully";
        public static final String OPERATION_COMPLETED = "Operation completed successfully";
    }
}
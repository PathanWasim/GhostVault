package com.ghostvault.ui.components;

import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.HashMap;
import java.util.Map;

/**
 * Modern icon system for GhostVault with consistent iconography
 */
public class ModernIcons {
    
    // Icon font sizes
    public static final double ICON_SMALL = 12;
    public static final double ICON_MEDIUM = 16;
    public static final double ICON_LARGE = 20;
    public static final double ICON_EXTRA_LARGE = 24;
    public static final double ICON_HUGE = 32;
    
    // Icon colors
    public static final Color ICON_PRIMARY = Color.web("#0078d4");
    public static final Color ICON_SUCCESS = Color.web("#107c10");
    public static final Color ICON_WARNING = Color.web("#ff8c00");
    public static final Color ICON_DANGER = Color.web("#d13438");
    public static final Color ICON_INFO = Color.web("#00bcf2");
    public static final Color ICON_MUTED = Color.web("#888888");
    public static final Color ICON_WHITE = Color.WHITE;
    
    // Security and Vault Icons
    public static final String VAULT_LOCKED = "🔒";
    public static final String VAULT_UNLOCKED = "🔓";
    public static final String SHIELD = "🛡️";
    public static final String KEY = "🔑";
    public static final String FINGERPRINT = "👆";
    public static final String SECURE = "🔐";
    public static final String WARNING = "⚠️";
    public static final String DANGER = "🚨";
    public static final String ENCRYPTED = "🔒";
    public static final String DECRYPTED = "🔓";
    
    // File Type Icons
    public static final String FILE_GENERIC = "📄";
    public static final String FILE_IMAGE = "🖼️";
    public static final String FILE_VIDEO = "🎬";
    public static final String FILE_AUDIO = "🎵";
    public static final String FILE_DOCUMENT = "📝";
    public static final String FILE_PDF = "📄";
    public static final String FILE_ARCHIVE = "📦";
    public static final String FILE_CODE = "💻";
    public static final String FILE_SPREADSHEET = "📊";
    public static final String FILE_PRESENTATION = "📽️";
    public static final String FILE_TEXT = "📃";
    public static final String FILE_EXECUTABLE = "⚙️";
    
    // Folder Icons
    public static final String FOLDER = "📁";
    public static final String FOLDER_OPEN = "📂";
    public static final String FOLDER_SECURE = "🔐";
    public static final String FOLDER_SHARED = "📁";
    public static final String FOLDER_SYNC = "🔄";
    
    // Action Icons
    public static final String ADD = "➕";
    public static final String REMOVE = "➖";
    public static final String DELETE = "🗑️";
    public static final String EDIT = "✏️";
    public static final String SAVE = "💾";
    public static final String DOWNLOAD = "⬇️";
    public static final String UPLOAD = "⬆️";
    public static final String COPY = "📋";
    public static final String CUT = "✂️";
    public static final String PASTE = "📋";
    public static final String SEARCH = "🔍";
    public static final String FILTER = "🔽";
    public static final String SORT = "🔀";
    public static final String REFRESH = "🔄";
    public static final String SYNC = "🔄";
    public static final String BACKUP = "💾";
    public static final String RESTORE = "⚡";
    
    // Navigation Icons
    public static final String HOME = "🏠";
    public static final String BACK = "⬅️";
    public static final String FORWARD = "➡️";
    public static final String UP = "⬆️";
    public static final String DOWN = "⬇️";
    public static final String LEFT = "⬅️";
    public static final String RIGHT = "➡️";
    public static final String EXPAND = "📖";
    public static final String COLLAPSE = "📕";
    
    // Media Control Icons
    public static final String PLAY = "▶️";
    public static final String PAUSE = "⏸️";
    public static final String STOP = "⏹️";
    public static final String RECORD = "⏺️";
    public static final String FAST_FORWARD = "⏩";
    public static final String REWIND = "⏪";
    public static final String VOLUME_UP = "🔊";
    public static final String VOLUME_DOWN = "🔉";
    public static final String VOLUME_MUTE = "🔇";
    
    // Status Icons
    public static final String SUCCESS = "✅";
    public static final String ERROR = "❌";
    public static final String INFO = "ℹ️";
    public static final String QUESTION = "❓";
    public static final String EXCLAMATION = "❗";
    public static final String LOADING = "⏳";
    public static final String PROGRESS = "⏳";
    public static final String COMPLETE = "✅";
    public static final String PENDING = "⏳";
    
    // User and Account Icons
    public static final String USER = "👤";
    public static final String USERS = "👥";
    public static final String ADMIN = "👑";
    public static final String GUEST = "👤";
    public static final String PROFILE = "👤";
    public static final String ACCOUNT = "👤";
    public static final String LOGIN = "🔑";
    public static final String LOGOUT = "🚪";
    
    // Settings and Configuration Icons
    public static final String SETTINGS = "⚙️";
    public static final String PREFERENCES = "⚙️";
    public static final String CONFIG = "🔧";
    public static final String TOOLS = "🔧";
    public static final String ADVANCED = "⚙️";
    public static final String CUSTOMIZE = "🎨";
    
    // Communication Icons
    public static final String EMAIL = "📧";
    public static final String MESSAGE = "💬";
    public static final String NOTIFICATION = "🔔";
    public static final String ALERT = "🚨";
    public static final String BELL = "🔔";
    public static final String CHAT = "💬";
    
    // Network and Connection Icons
    public static final String NETWORK = "🌐";
    public static final String WIFI = "📶";
    public static final String OFFLINE = "📴";
    public static final String ONLINE = "🌐";
    public static final String CONNECTED = "🔗";
    public static final String DISCONNECTED = "🔌";
    public static final String CLOUD = "☁️";
    public static final String SERVER = "🖥️";
    
    // Time and Calendar Icons
    public static final String CLOCK = "🕐";
    public static final String CALENDAR = "📅";
    public static final String DATE = "📅";
    public static final String TIME = "⏰";
    public static final String SCHEDULE = "📅";
    public static final String TIMER = "⏱️";
    
    // Special Mode Icons
    public static final String DECOY_MODE = "🎭";
    public static final String PANIC_MODE = "🚨";
    public static final String MASTER_MODE = "🔐";
    public static final String STEALTH_MODE = "👻";
    public static final String INVISIBLE = "👻";
    public static final String HIDDEN = "🙈";
    
    // Quality and Rating Icons
    public static final String STAR = "⭐";
    public static final String FAVORITE = "❤️";
    public static final String LIKE = "👍";
    public static final String DISLIKE = "👎";
    public static final String RATING = "⭐";
    public static final String BOOKMARK = "🔖";
    
    // Development and Code Icons
    public static final String CODE = "💻";
    public static final String BUG = "🐛";
    public static final String DEBUG = "🔍";
    public static final String BUILD = "🔨";
    public static final String COMPILE = "⚙️";
    public static final String VERSION = "🏷️";
    public static final String BRANCH = "🌿";
    public static final String COMMIT = "💾";
    
    // File extension to icon mapping
    private static final Map<String, String> FILE_EXTENSION_ICONS = new HashMap<>();
    static {
        // Images
        FILE_EXTENSION_ICONS.put(".jpg", FILE_IMAGE);
        FILE_EXTENSION_ICONS.put(".jpeg", FILE_IMAGE);
        FILE_EXTENSION_ICONS.put(".png", FILE_IMAGE);
        FILE_EXTENSION_ICONS.put(".gif", FILE_IMAGE);
        FILE_EXTENSION_ICONS.put(".bmp", FILE_IMAGE);
        FILE_EXTENSION_ICONS.put(".tiff", FILE_IMAGE);
        FILE_EXTENSION_ICONS.put(".svg", FILE_IMAGE);
        FILE_EXTENSION_ICONS.put(".webp", FILE_IMAGE);
        
        // Videos
        FILE_EXTENSION_ICONS.put(".mp4", FILE_VIDEO);
        FILE_EXTENSION_ICONS.put(".avi", FILE_VIDEO);
        FILE_EXTENSION_ICONS.put(".mkv", FILE_VIDEO);
        FILE_EXTENSION_ICONS.put(".mov", FILE_VIDEO);
        FILE_EXTENSION_ICONS.put(".wmv", FILE_VIDEO);
        FILE_EXTENSION_ICONS.put(".flv", FILE_VIDEO);
        FILE_EXTENSION_ICONS.put(".webm", FILE_VIDEO);
        FILE_EXTENSION_ICONS.put(".m4v", FILE_VIDEO);
        
        // Audio
        FILE_EXTENSION_ICONS.put(".mp3", FILE_AUDIO);
        FILE_EXTENSION_ICONS.put(".wav", FILE_AUDIO);
        FILE_EXTENSION_ICONS.put(".aac", FILE_AUDIO);
        FILE_EXTENSION_ICONS.put(".flac", FILE_AUDIO);
        FILE_EXTENSION_ICONS.put(".ogg", FILE_AUDIO);
        FILE_EXTENSION_ICONS.put(".m4a", FILE_AUDIO);
        FILE_EXTENSION_ICONS.put(".wma", FILE_AUDIO);
        
        // Documents
        FILE_EXTENSION_ICONS.put(".pdf", FILE_PDF);
        FILE_EXTENSION_ICONS.put(".doc", FILE_DOCUMENT);
        FILE_EXTENSION_ICONS.put(".docx", FILE_DOCUMENT);
        FILE_EXTENSION_ICONS.put(".txt", FILE_TEXT);
        FILE_EXTENSION_ICONS.put(".rtf", FILE_DOCUMENT);
        FILE_EXTENSION_ICONS.put(".odt", FILE_DOCUMENT);
        
        // Spreadsheets
        FILE_EXTENSION_ICONS.put(".xls", FILE_SPREADSHEET);
        FILE_EXTENSION_ICONS.put(".xlsx", FILE_SPREADSHEET);
        FILE_EXTENSION_ICONS.put(".csv", FILE_SPREADSHEET);
        FILE_EXTENSION_ICONS.put(".ods", FILE_SPREADSHEET);
        
        // Presentations
        FILE_EXTENSION_ICONS.put(".ppt", FILE_PRESENTATION);
        FILE_EXTENSION_ICONS.put(".pptx", FILE_PRESENTATION);
        FILE_EXTENSION_ICONS.put(".odp", FILE_PRESENTATION);
        
        // Archives
        FILE_EXTENSION_ICONS.put(".zip", FILE_ARCHIVE);
        FILE_EXTENSION_ICONS.put(".rar", FILE_ARCHIVE);
        FILE_EXTENSION_ICONS.put(".7z", FILE_ARCHIVE);
        FILE_EXTENSION_ICONS.put(".tar", FILE_ARCHIVE);
        FILE_EXTENSION_ICONS.put(".gz", FILE_ARCHIVE);
        FILE_EXTENSION_ICONS.put(".bz2", FILE_ARCHIVE);
        
        // Code files
        FILE_EXTENSION_ICONS.put(".java", FILE_CODE);
        FILE_EXTENSION_ICONS.put(".py", FILE_CODE);
        FILE_EXTENSION_ICONS.put(".cpp", FILE_CODE);
        FILE_EXTENSION_ICONS.put(".c", FILE_CODE);
        FILE_EXTENSION_ICONS.put(".h", FILE_CODE);
        FILE_EXTENSION_ICONS.put(".js", FILE_CODE);
        FILE_EXTENSION_ICONS.put(".html", FILE_CODE);
        FILE_EXTENSION_ICONS.put(".css", FILE_CODE);
        FILE_EXTENSION_ICONS.put(".xml", FILE_CODE);
        FILE_EXTENSION_ICONS.put(".json", FILE_CODE);
        FILE_EXTENSION_ICONS.put(".php", FILE_CODE);
        FILE_EXTENSION_ICONS.put(".rb", FILE_CODE);
        FILE_EXTENSION_ICONS.put(".go", FILE_CODE);
        FILE_EXTENSION_ICONS.put(".rs", FILE_CODE);
        FILE_EXTENSION_ICONS.put(".swift", FILE_CODE);
        FILE_EXTENSION_ICONS.put(".kt", FILE_CODE);
        
        // Executables
        FILE_EXTENSION_ICONS.put(".exe", FILE_EXECUTABLE);
        FILE_EXTENSION_ICONS.put(".msi", FILE_EXECUTABLE);
        FILE_EXTENSION_ICONS.put(".app", FILE_EXECUTABLE);
        FILE_EXTENSION_ICONS.put(".deb", FILE_EXECUTABLE);
        FILE_EXTENSION_ICONS.put(".rpm", FILE_EXECUTABLE);
    }
    
    /**
     * Create an icon label with specified icon and size
     */
    public static Label createIcon(String icon, double size) {
        Label iconLabel = new Label(icon);
        iconLabel.setStyle(String.format("-fx-font-size: %.0fpx;", size));
        return iconLabel;
    }
    
    /**
     * Create an icon label with specified icon, size, and color
     */
    public static Label createIcon(String icon, double size, Color color) {
        Label iconLabel = createIcon(icon, size);
        iconLabel.setTextFill(color);
        return iconLabel;
    }
    
    /**
     * Create a colored icon label
     */
    public static Label createColoredIcon(String icon, double size, String colorHex) {
        Label iconLabel = createIcon(icon, size);
        iconLabel.setStyle(String.format("-fx-font-size: %.0fpx; -fx-text-fill: %s;", size, colorHex));
        return iconLabel;
    }
    
    /**
     * Get icon for file extension
     */
    public static String getFileIcon(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return FILE_GENERIC;
        }
        
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot == -1) {
            return FILE_GENERIC;
        }
        
        String extension = fileName.substring(lastDot).toLowerCase();
        return FILE_EXTENSION_ICONS.getOrDefault(extension, FILE_GENERIC);
    }
    
    /**
     * Create file icon label
     */
    public static Label createFileIcon(String fileName, double size) {
        String icon = getFileIcon(fileName);
        return createIcon(icon, size);
    }
    
    /**
     * Create status icon with appropriate color
     */
    public static Label createStatusIcon(String status, double size) {
        String icon;
        Color color;
        
        switch (status.toLowerCase()) {
            case "success":
            case "complete":
            case "ok":
                icon = SUCCESS;
                color = ICON_SUCCESS;
                break;
            case "error":
            case "failed":
            case "danger":
                icon = ERROR;
                color = ICON_DANGER;
                break;
            case "warning":
            case "caution":
                icon = WARNING;
                color = ICON_WARNING;
                break;
            case "info":
            case "information":
                icon = INFO;
                color = ICON_INFO;
                break;
            case "loading":
            case "progress":
            case "pending":
                icon = LOADING;
                color = ICON_PRIMARY;
                break;
            default:
                icon = INFO;
                color = ICON_MUTED;
        }
        
        return createIcon(icon, size, color);
    }
    
    /**
     * Create security mode icon
     */
    public static Label createModeIcon(String mode, double size) {
        String icon;
        Color color;
        
        switch (mode.toLowerCase()) {
            case "master":
                icon = MASTER_MODE;
                color = ICON_SUCCESS;
                break;
            case "decoy":
                icon = DECOY_MODE;
                color = ICON_WARNING;
                break;
            case "panic":
                icon = PANIC_MODE;
                color = ICON_DANGER;
                break;
            case "stealth":
                icon = STEALTH_MODE;
                color = ICON_MUTED;
                break;
            default:
                icon = SECURE;
                color = ICON_PRIMARY;
        }
        
        return createIcon(icon, size, color);
    }
    
    /**
     * Create action button icon
     */
    public static Label createActionIcon(String action, double size) {
        String icon = getActionIcon(action);
        return createIcon(icon, size, ICON_PRIMARY);
    }
    
    /**
     * Get action icon string
     */
    public static String getActionIcon(String action) {
        switch (action.toLowerCase()) {
            case "add": case "create": case "new":
                return ADD;
            case "remove": case "delete":
                return DELETE;
            case "edit": case "modify":
                return EDIT;
            case "save":
                return SAVE;
            case "download":
                return DOWNLOAD;
            case "upload":
                return UPLOAD;
            case "copy":
                return COPY;
            case "cut":
                return CUT;
            case "paste":
                return PASTE;
            case "search": case "find":
                return SEARCH;
            case "filter":
                return FILTER;
            case "sort":
                return SORT;
            case "refresh": case "reload":
                return REFRESH;
            case "sync":
                return SYNC;
            case "backup":
                return BACKUP;
            case "restore":
                return RESTORE;
            case "settings": case "config":
                return SETTINGS;
            case "home":
                return HOME;
            case "back":
                return BACK;
            case "forward":
                return FORWARD;
            default:
                return INFO;
        }
    }
    
    /**
     * Create navigation icon
     */
    public static Label createNavIcon(String direction, double size) {
        String icon;
        switch (direction.toLowerCase()) {
            case "up":
                icon = UP;
                break;
            case "down":
                icon = DOWN;
                break;
            case "left":
            case "back":
                icon = LEFT;
                break;
            case "right":
            case "forward":
                icon = RIGHT;
                break;
            case "home":
                icon = HOME;
                break;
            case "expand":
                icon = EXPAND;
                break;
            case "collapse":
                icon = COLLAPSE;
                break;
            default:
                icon = INFO;
        }
        
        return createIcon(icon, size, ICON_PRIMARY);
    }
    
    /**
     * Create media control icon
     */
    public static Label createMediaIcon(String control, double size) {
        String icon;
        switch (control.toLowerCase()) {
            case "play":
                icon = PLAY;
                break;
            case "pause":
                icon = PAUSE;
                break;
            case "stop":
                icon = STOP;
                break;
            case "record":
                icon = RECORD;
                break;
            case "forward":
            case "fast_forward":
                icon = FAST_FORWARD;
                break;
            case "rewind":
                icon = REWIND;
                break;
            case "volume_up":
                icon = VOLUME_UP;
                break;
            case "volume_down":
                icon = VOLUME_DOWN;
                break;
            case "volume_mute":
            case "mute":
                icon = VOLUME_MUTE;
                break;
            default:
                icon = PLAY;
        }
        
        return createIcon(icon, size, ICON_PRIMARY);
    }
    
    /**
     * Apply icon styling to existing label
     */
    public static void styleAsIcon(Label label, double size, Color color) {
        label.setStyle(String.format("-fx-font-size: %.0fpx;", size));
        if (color != null) {
            label.setTextFill(color);
        }
    }
    
    /**
     * Apply icon styling to existing label with hex color
     */
    public static void styleAsIcon(Label label, double size, String colorHex) {
        label.setStyle(String.format("-fx-font-size: %.0fpx; -fx-text-fill: %s;", size, colorHex));
    }
}
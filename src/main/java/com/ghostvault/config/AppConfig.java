package com.ghostvault.config;

/**
 * Application configuration constants
 */
public class AppConfig {
    
    // Application metadata
    public static final String APP_NAME = "GhostVault";
    public static final String APP_VERSION = "1.0.0";
    
    // Vault configuration - dynamic path based on permissions
    public static String getVaultDir() {
        return System.getProperty("ghostvault.vault.path", System.getProperty("user.home") + "/.ghostvault");
    }
    
    public static final String VAULT_DIR = getVaultDir();
    public static final String FILES_DIR = getVaultDir() + "/files";
    public static final String DECOYS_DIR = getVaultDir() + "/decoys";
    public static final String CONFIG_FILE = getVaultDir() + "/config.enc";
    public static final String METADATA_FILE = getVaultDir() + "/metadata.enc";
    public static final String SALT_FILE = getVaultDir() + "/.salt";
    public static final String LOG_FILE = getVaultDir() + "/audit.log.enc";
    
    // Security settings
    public static final int MAX_LOGIN_ATTEMPTS = 3;
    public static final int SESSION_TIMEOUT_MINUTES = 15;
    public static final int PASSWORD_MIN_STRENGTH = 4;
    public static final int SECURE_DELETE_PASSES = 3;
    
    // Encryption settings
    public static final String ENCRYPTION_ALGORITHM = "AES";
    public static final String ENCRYPTION_TRANSFORMATION = "AES/CBC/PKCS5Padding";
    public static final String KEY_DERIVATION_ALGORITHM = "PBKDF2WithHmacSHA256";
    public static final int KEY_SIZE = 256;
    public static final int PBKDF2_ITERATIONS = 100000;
    public static final int IV_SIZE = 16;
    public static final int SALT_SIZE = 32;
    
    // UI settings
    public static final int WINDOW_WIDTH = 900;
    public static final int WINDOW_HEIGHT = 700;
    public static final int LOGIN_WINDOW_WIDTH = 500;
    public static final int LOGIN_WINDOW_HEIGHT = 400;
    
    // File validation
    public static final int MAX_FILENAME_LENGTH = 100;
    public static final String VALID_FILENAME_PATTERN = "^[\\w\\-. ]+$";
    
    // Decoy file names
    public static final String[] DECOY_FILES = {
        "Budget_2024.xlsx", "Meeting_Notes.txt", "Project_Plan.docx",
        "Vacation_Photos.jpg", "Resume_Draft.pdf", "Contact_List.csv",
        "Shopping_List.txt", "Recipe_Collection.pdf", "Book_Notes.docx",
        "Travel_Itinerary.txt", "Workout_Plan.pdf", "Garden_Journal.docx"
    };
    
    private AppConfig() {
        // Utility class - prevent instantiation
    }
}
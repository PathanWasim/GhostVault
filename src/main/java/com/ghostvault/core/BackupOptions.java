package com.ghostvault.core;

/**
 * Options for backup operations
 */
public class BackupOptions {
    private String encryptionPassword;
    private boolean compressionEnabled = true;
    private boolean verificationEnabled = true;
    
    public String getEncryptionPassword() {
        return encryptionPassword;
    }
    
    public void setEncryptionPassword(String encryptionPassword) {
        this.encryptionPassword = encryptionPassword;
    }
    
    public boolean isCompressionEnabled() {
        return compressionEnabled;
    }
    
    public void setCompressionEnabled(boolean compressionEnabled) {
        this.compressionEnabled = compressionEnabled;
    }
    
    public boolean isVerificationEnabled() {
        return verificationEnabled;
    }
    
    public void setVerificationEnabled(boolean verificationEnabled) {
        this.verificationEnabled = verificationEnabled;
    }
}
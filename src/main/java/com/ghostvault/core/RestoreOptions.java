package com.ghostvault.core;

/**
 * Options for restore operations
 */
public class RestoreOptions {
    private String decryptionPassword;
    private boolean verificationEnabled = true;
    
    public String getDecryptionPassword() {
        return decryptionPassword;
    }
    
    public void setDecryptionPassword(String decryptionPassword) {
        this.decryptionPassword = decryptionPassword;
    }
    
    public boolean isVerificationEnabled() {
        return verificationEnabled;
    }
    
    public void setVerificationEnabled(boolean verificationEnabled) {
        this.verificationEnabled = verificationEnabled;
    }
}
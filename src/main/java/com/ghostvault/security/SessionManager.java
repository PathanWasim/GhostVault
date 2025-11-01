package com.ghostvault.security;

/**
 * Simple SessionManager stub for compilation
 */
public class SessionManager {
    private boolean sessionActive = false;
    
    public void startSession() {
        sessionActive = true;
    }
    
    public void endSession() {
        sessionActive = false;
    }
    
    public boolean isSessionActive() {
        return sessionActive;
    }
    
    public void extendSession() {
        // Stub implementation
    }
}
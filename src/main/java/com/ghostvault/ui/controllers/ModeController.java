package com.ghostvault.ui.controllers;

/**
 * Simple stub for ModeController to maintain compatibility
 * Mode-specific features simplified for basic password manager
 */
public class ModeController {
    
    public enum VaultMode {
        MASTER, PANIC, DECOY
    }
    
    public static class ModeChangeEvent {
        private final VaultMode newMode;
        
        public ModeChangeEvent(VaultMode newMode) {
            this.newMode = newMode;
        }
        
        public VaultMode getNewMode() {
            return newMode;
        }
    }
    
    public ModeController() {
        // Simple stub constructor
    }
    
    public VaultMode getCurrentMode() {
        return VaultMode.MASTER; // Default to master mode
    }
    
    public void switchMode(VaultMode mode) {
        // Stub implementation
        System.out.println("Mode switched to: " + mode);
    }
}
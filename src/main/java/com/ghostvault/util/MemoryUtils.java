package com.ghostvault.util;

/**
 * Simple memory utilities
 */
public class MemoryUtils {
    
    /**
     * Securely wipe data from memory
     */
    public static void secureWipe(byte[] data) {
        if (data != null) {
            for (int i = 0; i < data.length; i++) {
                data[i] = 0;
            }
        }
    }
}
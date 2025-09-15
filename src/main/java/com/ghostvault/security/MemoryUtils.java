package com.ghostvault.security;

import java.util.Arrays;

/**
 * Utility class for secure memory management and sensitive data handling
 */
public class MemoryUtils {
    
    /**
     * Securely wipe a byte array by overwriting with zeros
     */
    public static void secureWipe(byte[] data) {
        if (data != null) {
            Arrays.fill(data, (byte) 0);
        }
    }
    
    /**
     * Securely wipe a char array by overwriting with zeros
     */
    public static void secureWipe(char[] data) {
        if (data != null) {
            Arrays.fill(data, '\0');
        }
    }
    
    /**
     * Securely wipe multiple byte arrays
     */
    public static void secureWipe(byte[]... arrays) {
        for (byte[] array : arrays) {
            secureWipe(array);
        }
    }
    
    /**
     * Securely wipe multiple char arrays
     */
    public static void secureWipe(char[]... arrays) {
        for (char[] array : arrays) {
            secureWipe(array);
        }
    }
    
    /**
     * Create a secure copy of a byte array
     */
    public static byte[] secureCopy(byte[] source) {
        if (source == null) {
            return null;
        }
        return source.clone();
    }
    
    /**
     * Create a secure copy of a char array
     */
    public static char[] secureCopy(char[] source) {
        if (source == null) {
            return null;
        }
        return source.clone();
    }
    
    /**
     * Constant-time comparison of byte arrays to prevent timing attacks
     */
    public static boolean constantTimeEquals(byte[] a, byte[] b) {
        if (a == null || b == null) {
            return a == b;
        }
        
        if (a.length != b.length) {
            return false;
        }
        
        int result = 0;
        for (int i = 0; i < a.length; i++) {
            result |= a[i] ^ b[i];
        }
        return result == 0;
    }
    
    /**
     * Constant-time comparison of char arrays to prevent timing attacks
     */
    public static boolean constantTimeEquals(char[] a, char[] b) {
        if (a == null || b == null) {
            return a == b;
        }
        
        if (a.length != b.length) {
            return false;
        }
        
        int result = 0;
        for (int i = 0; i < a.length; i++) {
            result |= a[i] ^ b[i];
        }
        return result == 0;
    }
    
    /**
     * Convert string to char array for secure handling
     */
    public static char[] toCharArray(String str) {
        if (str == null) {
            return null;
        }
        return str.toCharArray();
    }
    
    /**
     * Convert char array to string and immediately wipe the char array
     */
    public static String toStringAndWipe(char[] chars) {
        if (chars == null) {
            return null;
        }
        
        try {
            return new String(chars);
        } finally {
            secureWipe(chars);
        }
    }
    
    /**
     * Perform secure garbage collection hint
     * Note: This doesn't guarantee immediate GC, but suggests it
     */
    public static void secureGC() {
        System.gc();
        System.runFinalization();
        System.gc();
    }
    
    /**
     * Check if array contains only zeros (for testing wipe effectiveness)
     */
    public static boolean isWiped(byte[] data) {
        if (data == null) {
            return true;
        }
        
        for (byte b : data) {
            if (b != 0) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Check if array contains only null chars (for testing wipe effectiveness)
     */
    public static boolean isWiped(char[] data) {
        if (data == null) {
            return true;
        }
        
        for (char c : data) {
            if (c != '\0') {
                return false;
            }
        }
        return true;
    }
    
    private MemoryUtils() {
        // Utility class - prevent instantiation
    }
}
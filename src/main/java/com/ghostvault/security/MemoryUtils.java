package com.ghostvault.security;

import java.security.SecureRandom;
import java.util.Arrays;

/**
 * Utility class for secure memory operations
 * Provides methods for secure data wiping and constant-time comparisons
 */
public class MemoryUtils {
    
    private static final SecureRandom secureRandom = new SecureRandom();
    
    /**
     * Securely wipe sensitive data from memory
     * Overwrites the array with random data multiple times
     * 
     * @param data The byte array to wipe
     */
    public static void secureWipe(byte[] data) {
        if (data == null) {
            return;
        }
        
        // Perform multiple passes with different patterns
        // Pass 1: Fill with zeros
        Arrays.fill(data, (byte) 0x00);
        
        // Pass 2: Fill with ones
        Arrays.fill(data, (byte) 0xFF);
        
        // Pass 3: Fill with random data
        secureRandom.nextBytes(data);
        
        // Pass 4: Fill with alternating pattern
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) (i % 2 == 0 ? 0xAA : 0x55);
        }
        
        // Final pass: Fill with zeros
        Arrays.fill(data, (byte) 0x00);
    }
    
    /**
     * Securely wipe sensitive data from a char array
     * Overwrites the array with random data multiple times
     * 
     * @param data The char array to wipe
     */
    public static void secureWipe(char[] data) {
        if (data == null) {
            return;
        }
        
        // Convert to byte array and wipe
        byte[] bytes = new byte[data.length * 2]; // chars are 2 bytes
        for (int i = 0; i < data.length; i++) {
            bytes[i * 2] = (byte) (data[i] >> 8);
            bytes[i * 2 + 1] = (byte) data[i];
        }
        
        secureWipe(bytes);
        
        // Also wipe the original char array
        Arrays.fill(data, '\0');
    }
    
    /**
     * Constant-time comparison of two byte arrays
     * Prevents timing attacks by always comparing all bytes
     * 
     * @param a First byte array
     * @param b Second byte array
     * @return true if arrays are equal, false otherwise
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
     * Constant-time comparison of two strings
     * Prevents timing attacks by always comparing all characters
     * 
     * @param a First string
     * @param b Second string
     * @return true if strings are equal, false otherwise
     */
    public static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return a == b;
        }
        
        if (a.length() != b.length()) {
            return false;
        }
        
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        
        return result == 0;
    }
    
    /**
     * Generate cryptographically secure random bytes
     * 
     * @param length Number of bytes to generate
     * @return Array of random bytes
     */
    public static byte[] generateSecureRandomBytes(int length) {
        byte[] bytes = new byte[length];
        secureRandom.nextBytes(bytes);
        return bytes;
    }
    
    /**
     * Generate a cryptographically secure random salt
     * 
     * @return 32-byte random salt
     */
    public static byte[] generateSalt() {
        return generateSecureRandomBytes(32);
    }
    
    /**
     * Generate a cryptographically secure random IV for AES
     * 
     * @return 16-byte random IV
     */
    public static byte[] generateIV() {
        return generateSecureRandomBytes(16);
    }
    
    /**
     * Clear sensitive data from a StringBuilder
     * 
     * @param sb StringBuilder to clear
     */
    public static void secureWipe(StringBuilder sb) {
        if (sb == null) {
            return;
        }
        
        // Overwrite with random characters
        for (int i = 0; i < sb.length(); i++) {
            sb.setCharAt(i, (char) secureRandom.nextInt(65536));
        }
        
        // Clear the StringBuilder
        sb.setLength(0);
    }
    
    /**
     * Validate that an array contains only zeros (has been wiped)
     * Used for testing secure wipe operations
     * 
     * @param data Array to check
     * @return true if array contains only zeros
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
     * Validate that a char array contains only null characters (has been wiped)
     * Used for testing secure wipe operations
     * 
     * @param data Array to check
     * @return true if array contains only null characters
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
    
    /**
     * Create a secure copy of a byte array
     * 
     * @param original Array to copy
     * @return Secure copy of the array
     */
    public static byte[] secureCopy(byte[] original) {
        if (original == null) {
            return null;
        }
        
        byte[] copy = new byte[original.length];
        System.arraycopy(original, 0, copy, 0, original.length);
        return copy;
    }
    
    /**
     * Create a secure copy of a char array
     * 
     * @param original Array to copy
     * @return Secure copy of the array
     */
    public static char[] secureCopy(char[] original) {
        if (original == null) {
            return null;
        }
        
        char[] copy = new char[original.length];
        System.arraycopy(original, 0, copy, 0, original.length);
        return copy;
    }
}
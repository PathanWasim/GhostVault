package com.ghostvault.util;

import java.security.MessageDigest;

/**
 * Utility methods for hashing and other common operations.
 */
public class HashUtil {
    /**
     * Returns the SHA-256 hash of the given data as a hex string.
     */
    public static String getSHA256(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }
}

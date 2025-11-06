package com.ghostvault.ui.preview;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Advanced character set detection utility
 * Provides better encoding detection than simple heuristics
 */
public class CharsetDetector {
    
    // Common byte order marks
    private static final byte[] UTF8_BOM = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
    private static final byte[] UTF16_LE_BOM = {(byte) 0xFF, (byte) 0xFE};
    private static final byte[] UTF16_BE_BOM = {(byte) 0xFE, (byte) 0xFF};
    private static final byte[] UTF32_LE_BOM = {(byte) 0xFF, (byte) 0xFE, (byte) 0x00, (byte) 0x00};
    private static final byte[] UTF32_BE_BOM = {(byte) 0x00, (byte) 0x00, (byte) 0xFE, (byte) 0xFF};
    
    /**
     * Detect character encoding from byte array
     * @param data The byte data to analyze
     * @return Detected charset
     */
    public static Charset detectCharset(byte[] data) {
        if (data == null || data.length == 0) {
            return StandardCharsets.UTF_8;
        }
        
        // Check for BOM (Byte Order Mark)
        Charset bomCharset = detectByBOM(data);
        if (bomCharset != null) {
            return bomCharset;
        }
        
        // Check for null bytes (indicates binary or UTF-16/32)
        if (containsNullBytes(data)) {
            return detectUnicodeWithoutBOM(data);
        }
        
        // Statistical analysis for text encodings
        return detectTextEncoding(data);
    }
    
    /**
     * Detect encoding by Byte Order Mark
     */
    private static Charset detectByBOM(byte[] data) {
        if (data.length >= 4 && startsWith(data, UTF32_LE_BOM)) {
            return Charset.forName("UTF-32LE");
        }
        if (data.length >= 4 && startsWith(data, UTF32_BE_BOM)) {
            return Charset.forName("UTF-32BE");
        }
        if (data.length >= 3 && startsWith(data, UTF8_BOM)) {
            return StandardCharsets.UTF_8;
        }
        if (data.length >= 2 && startsWith(data, UTF16_LE_BOM)) {
            return StandardCharsets.UTF_16LE;
        }
        if (data.length >= 2 && startsWith(data, UTF16_BE_BOM)) {
            return StandardCharsets.UTF_16BE;
        }
        
        return null;
    }
    
    /**
     * Check if data starts with given prefix
     */
    private static boolean startsWith(byte[] data, byte[] prefix) {
        if (data.length < prefix.length) {
            return false;
        }
        
        for (int i = 0; i < prefix.length; i++) {
            if (data[i] != prefix[i]) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Check if data contains null bytes
     */
    private static boolean containsNullBytes(byte[] data) {
        for (byte b : data) {
            if (b == 0) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Detect Unicode encoding without BOM
     */
    private static Charset detectUnicodeWithoutBOM(byte[] data) {
        // Simple heuristic: check for alternating null bytes
        if (data.length >= 4) {
            // UTF-16LE pattern: non-null, null, non-null, null
            if (data[1] == 0 && data[3] == 0 && data[0] != 0 && data[2] != 0) {
                return StandardCharsets.UTF_16LE;
            }
            
            // UTF-16BE pattern: null, non-null, null, non-null
            if (data[0] == 0 && data[2] == 0 && data[1] != 0 && data[3] != 0) {
                return StandardCharsets.UTF_16BE;
            }
        }
        
        // Default to UTF-8 for binary data
        return StandardCharsets.UTF_8;
    }
    
    /**
     * Detect text encoding using statistical analysis
     */
    private static Charset detectTextEncoding(byte[] data) {
        // Try UTF-8 first
        if (isValidUTF8(data)) {
            return StandardCharsets.UTF_8;
        }
        
        // Check for high-bit characters (indicates extended ASCII)
        boolean hasHighBits = false;
        for (byte b : data) {
            if ((b & 0x80) != 0) {
                hasHighBits = true;
                break;
            }
        }
        
        if (!hasHighBits) {
            // Pure ASCII
            return StandardCharsets.US_ASCII;
        }
        
        // Analyze byte patterns for common encodings
        return analyzeBytePatterns(data);
    }
    
    /**
     * Check if byte array is valid UTF-8
     */
    private static boolean isValidUTF8(byte[] data) {
        try {
            // Try to decode as UTF-8
            String decoded = new String(data, StandardCharsets.UTF_8);
            
            // Re-encode and compare
            byte[] reencoded = decoded.getBytes(StandardCharsets.UTF_8);
            
            // If lengths differ significantly, probably not UTF-8
            if (Math.abs(data.length - reencoded.length) > data.length * 0.1) {
                return false;
            }
            
            // Check for replacement characters (indicates invalid UTF-8)
            return !decoded.contains("\uFFFD");
            
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Analyze byte patterns to determine encoding
     */
    private static Charset analyzeBytePatterns(byte[] data) {
        int[] byteFreq = new int[256];
        
        // Count byte frequencies
        for (byte b : data) {
            byteFreq[b & 0xFF]++;
        }
        
        // Analyze patterns
        int highBitCount = 0;
        int controlCharCount = 0;
        
        for (int i = 128; i < 256; i++) {
            highBitCount += byteFreq[i];
        }
        
        for (int i = 0; i < 32; i++) {
            if (i != 9 && i != 10 && i != 13) { // Exclude tab, LF, CR
                controlCharCount += byteFreq[i];
            }
        }
        
        double highBitRatio = (double) highBitCount / data.length;
        double controlCharRatio = (double) controlCharCount / data.length;
        
        // If many control characters, likely binary
        if (controlCharRatio > 0.05) {
            return StandardCharsets.UTF_8; // Default for binary
        }
        
        // If moderate high-bit usage, likely Windows-1252 or ISO-8859-1
        if (highBitRatio > 0.05 && highBitRatio < 0.3) {
            // Check for common Windows-1252 characters
            if (byteFreq[0x80] > 0 || byteFreq[0x82] > 0 || byteFreq[0x83] > 0 || 
                byteFreq[0x84] > 0 || byteFreq[0x85] > 0 || byteFreq[0x86] > 0) {
                return Charset.forName("windows-1252");
            }
            
            return StandardCharsets.ISO_8859_1;
        }
        
        // Default to UTF-8
        return StandardCharsets.UTF_8;
    }
    
    /**
     * Get BOM length for a charset
     */
    public static int getBOMLength(Charset charset) {
        if (charset == null) {
            return 0;
        }
        
        String name = charset.name().toLowerCase();
        switch (name) {
            case "utf-8":
                return 3;
            case "utf-16":
            case "utf-16le":
            case "utf-16be":
                return 2;
            case "utf-32":
            case "utf-32le":
            case "utf-32be":
                return 4;
            default:
                return 0;
        }
    }
    
    /**
     * Remove BOM from byte array if present
     */
    public static byte[] removeBOM(byte[] data, Charset charset) {
        int bomLength = getBOMLength(charset);
        if (bomLength == 0 || data.length < bomLength) {
            return data;
        }
        
        // Check if BOM is actually present
        byte[] expectedBOM = getBOMBytes(charset);
        if (expectedBOM != null && startsWith(data, expectedBOM)) {
            return Arrays.copyOfRange(data, bomLength, data.length);
        }
        
        return data;
    }
    
    /**
     * Get BOM bytes for a charset
     */
    private static byte[] getBOMBytes(Charset charset) {
        if (charset == null) {
            return null;
        }
        
        String name = charset.name().toLowerCase();
        switch (name) {
            case "utf-8":
                return UTF8_BOM;
            case "utf-16le":
                return UTF16_LE_BOM;
            case "utf-16be":
                return UTF16_BE_BOM;
            case "utf-32le":
                return UTF32_LE_BOM;
            case "utf-32be":
                return UTF32_BE_BOM;
            default:
                return null;
        }
    }
    
    /**
     * Get confidence score for charset detection (0.0 to 1.0)
     */
    public static double getConfidence(byte[] data, Charset charset) {
        if (data == null || charset == null) {
            return 0.0;
        }
        
        try {
            // Try to decode and re-encode
            String decoded = new String(data, charset);
            byte[] reencoded = decoded.getBytes(charset);
            
            // Calculate similarity
            int matches = 0;
            int minLength = Math.min(data.length, reencoded.length);
            
            for (int i = 0; i < minLength; i++) {
                if (data[i] == reencoded[i]) {
                    matches++;
                }
            }
            
            double similarity = (double) matches / Math.max(data.length, reencoded.length);
            
            // Penalize for replacement characters
            long replacementCount = decoded.chars().filter(c -> c == 0xFFFD).count();
            double replacementPenalty = (double) replacementCount / decoded.length();
            
            return Math.max(0.0, similarity - replacementPenalty);
            
        } catch (Exception e) {
            return 0.0;
        }
    }
    
    /**
     * Detect multiple possible charsets with confidence scores
     */
    public static CharsetCandidate[] detectCandidates(byte[] data) {
        Charset[] candidates = {
            StandardCharsets.UTF_8,
            StandardCharsets.UTF_16,
            StandardCharsets.ISO_8859_1,
            Charset.forName("windows-1252"),
            StandardCharsets.US_ASCII
        };
        
        CharsetCandidate[] results = new CharsetCandidate[candidates.length];
        
        for (int i = 0; i < candidates.length; i++) {
            double confidence = getConfidence(data, candidates[i]);
            results[i] = new CharsetCandidate(candidates[i], confidence);
        }
        
        // Sort by confidence (highest first)
        Arrays.sort(results, (a, b) -> Double.compare(b.confidence, a.confidence));
        
        return results;
    }
    
    /**
     * Charset candidate with confidence score
     */
    public static class CharsetCandidate {
        public final Charset charset;
        public final double confidence;
        
        public CharsetCandidate(Charset charset, double confidence) {
            this.charset = charset;
            this.confidence = confidence;
        }
        
        @Override
        public String toString() {
            return String.format("%s (%.2f)", charset.name(), confidence);
        }
    }
}
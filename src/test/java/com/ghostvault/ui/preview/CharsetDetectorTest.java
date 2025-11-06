package com.ghostvault.ui.preview;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Unit tests for CharsetDetector
 */
class CharsetDetectorTest {
    
    @Test
    @DisplayName("Should detect UTF-8 BOM")
    void testDetectUTF8BOM() {
        byte[] data = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF, 'H', 'e', 'l', 'l', 'o'};
        Charset detected = CharsetDetector.detectCharset(data);
        assertEquals(StandardCharsets.UTF_8, detected);
    }
    
    @Test
    @DisplayName("Should detect UTF-16LE BOM")
    void testDetectUTF16LEBOM() {
        byte[] data = {(byte) 0xFF, (byte) 0xFE, 'H', 0, 'e', 0, 'l', 0, 'l', 0, 'o', 0};
        Charset detected = CharsetDetector.detectCharset(data);
        assertEquals(StandardCharsets.UTF_16LE, detected);
    }
    
    @Test
    @DisplayName("Should detect UTF-16BE BOM")
    void testDetectUTF16BEBOM() {
        byte[] data = {(byte) 0xFE, (byte) 0xFF, 0, 'H', 0, 'e', 0, 'l', 0, 'l', 0, 'o'};
        Charset detected = CharsetDetector.detectCharset(data);
        assertEquals(StandardCharsets.UTF_16BE, detected);
    }
    
    @Test
    @DisplayName("Should detect plain ASCII")
    void testDetectASCII() {
        byte[] data = "Hello World".getBytes(StandardCharsets.US_ASCII);
        Charset detected = CharsetDetector.detectCharset(data);
        // Should detect as UTF-8 (superset of ASCII) or ASCII
        assertTrue(detected == StandardCharsets.UTF_8 || detected == StandardCharsets.US_ASCII);
    }
    
    @Test
    @DisplayName("Should detect valid UTF-8")
    void testDetectValidUTF8() {
        String text = "Hello 世界 🌍";
        byte[] data = text.getBytes(StandardCharsets.UTF_8);
        Charset detected = CharsetDetector.detectCharset(data);
        assertEquals(StandardCharsets.UTF_8, detected);
    }
    
    @Test
    @DisplayName("Should handle empty data")
    void testEmptyData() {
        byte[] data = new byte[0];
        Charset detected = CharsetDetector.detectCharset(data);
        assertEquals(StandardCharsets.UTF_8, detected);
    }
    
    @Test
    @DisplayName("Should handle null data")
    void testNullData() {
        Charset detected = CharsetDetector.detectCharset(null);
        assertEquals(StandardCharsets.UTF_8, detected);
    }
    
    @Test
    @DisplayName("Should get correct BOM length")
    void testGetBOMLength() {
        assertEquals(3, CharsetDetector.getBOMLength(StandardCharsets.UTF_8));
        assertEquals(2, CharsetDetector.getBOMLength(StandardCharsets.UTF_16LE));
        assertEquals(2, CharsetDetector.getBOMLength(StandardCharsets.UTF_16BE));
        assertEquals(0, CharsetDetector.getBOMLength(StandardCharsets.ISO_8859_1));
        assertEquals(0, CharsetDetector.getBOMLength(null));
    }
    
    @Test
    @DisplayName("Should remove BOM correctly")
    void testRemoveBOM() {
        // UTF-8 BOM
        byte[] dataWithBOM = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF, 'H', 'e', 'l', 'l', 'o'};
        byte[] dataWithoutBOM = CharsetDetector.removeBOM(dataWithBOM, StandardCharsets.UTF_8);
        assertArrayEquals(new byte[]{'H', 'e', 'l', 'l', 'o'}, dataWithoutBOM);
        
        // Data without BOM should remain unchanged
        byte[] plainData = {'H', 'e', 'l', 'l', 'o'};
        byte[] result = CharsetDetector.removeBOM(plainData, StandardCharsets.UTF_8);
        assertArrayEquals(plainData, result);
    }
    
    @Test
    @DisplayName("Should calculate confidence scores")
    void testGetConfidence() {
        String text = "Hello World";
        byte[] utf8Data = text.getBytes(StandardCharsets.UTF_8);
        
        double utf8Confidence = CharsetDetector.getConfidence(utf8Data, StandardCharsets.UTF_8);
        double iso88591Confidence = CharsetDetector.getConfidence(utf8Data, StandardCharsets.ISO_8859_1);
        
        assertTrue(utf8Confidence >= 0.0 && utf8Confidence <= 1.0);
        assertTrue(iso88591Confidence >= 0.0 && iso88591Confidence <= 1.0);
        
        // UTF-8 should have higher confidence for UTF-8 encoded data
        assertTrue(utf8Confidence >= iso88591Confidence);
    }
    
    @Test
    @DisplayName("Should detect multiple candidates")
    void testDetectCandidates() {
        String text = "Hello World";
        byte[] data = text.getBytes(StandardCharsets.UTF_8);
        
        CharsetDetector.CharsetCandidate[] candidates = CharsetDetector.detectCandidates(data);
        
        assertNotNull(candidates);
        assertTrue(candidates.length > 0);
        
        // Should be sorted by confidence (highest first)
        for (int i = 1; i < candidates.length; i++) {
            assertTrue(candidates[i-1].confidence >= candidates[i].confidence);
        }
        
        // All confidence scores should be valid
        for (CharsetDetector.CharsetCandidate candidate : candidates) {
            assertTrue(candidate.confidence >= 0.0 && candidate.confidence <= 1.0);
            assertNotNull(candidate.charset);
        }
    }
    
    @Test
    @DisplayName("Should handle binary data")
    void testBinaryData() {
        // Create some binary data with null bytes
        byte[] binaryData = {0x00, 0x01, 0x02, 0x03, 0x00, 0xFF, 0xFE, 0xFD};
        
        Charset detected = CharsetDetector.detectCharset(binaryData);
        assertNotNull(detected);
        
        // Should not crash and should return a valid charset
        double confidence = CharsetDetector.getConfidence(binaryData, detected);
        assertTrue(confidence >= 0.0 && confidence <= 1.0);
    }
}
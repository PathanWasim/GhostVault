package com.ghostvault.core;

import com.ghostvault.security.SecurityConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.security.SecureRandom;

/**
 * Unit tests for EncryptedFileData
 */
public class EncryptedFileDataTest {
    
    private SecureRandom random;
    private byte[] testSalt;
    private byte[] testIv;
    private byte[] testCiphertext;
    
    @BeforeEach
    void setUp() {
        random = new SecureRandom();
        testSalt = new byte[SecurityConfiguration.SALT_LENGTH];
        testIv = new byte[SecurityConfiguration.IV_LENGTH];
        testCiphertext = new byte[64]; // Includes auth tag
        
        random.nextBytes(testSalt);
        random.nextBytes(testIv);
        random.nextBytes(testCiphertext);
    }
    
    @Test
    @DisplayName("Should create valid encrypted file data")
    void shouldCreateValidEncryptedFileData() {
        EncryptedFileData data = new EncryptedFileData(testSalt, testIv, testCiphertext);
        
        assertTrue(data.isValid());
        assertArrayEquals(testSalt, data.getSalt());
        assertArrayEquals(testIv, data.getIv());
        assertArrayEquals(testCiphertext, data.getCiphertext());
    }
    
    @Test
    @DisplayName("Should validate format requirements")
    void shouldValidateFormatRequirements() {
        // Valid data
        EncryptedFileData validData = new EncryptedFileData(testSalt, testIv, testCiphertext);
        assertTrue(validData.isValid());
        
        // Invalid salt length
        byte[] invalidSalt = new byte[16]; // Wrong length
        EncryptedFileData invalidSaltData = new EncryptedFileData(invalidSalt, testIv, testCiphertext);
        assertFalse(invalidSaltData.isValid());
        
        // Invalid IV length
        byte[] invalidIv = new byte[16]; // Wrong length for GCM
        EncryptedFileData invalidIvData = new EncryptedFileData(testSalt, invalidIv, testCiphertext);
        assertFalse(invalidIvData.isValid());
        
        // Ciphertext too small (no auth tag)
        byte[] tooSmallCiphertext = new byte[8]; // Smaller than GCM tag
        EncryptedFileData invalidCiphertextData = new EncryptedFileData(testSalt, testIv, tooSmallCiphertext);
        assertFalse(invalidCiphertextData.isValid());
        
        // Null values
        EncryptedFileData nullData = new EncryptedFileData(null, testIv, testCiphertext);
        assertFalse(nullData.isValid());
    }
    
    @Test
    @DisplayName("Should serialize and deserialize correctly")
    void shouldSerializeAndDeserializeCorrectly() {
        EncryptedFileData original = new EncryptedFileData(testSalt, testIv, testCiphertext);
        
        byte[] serialized = original.toByteArray();
        EncryptedFileData deserialized = EncryptedFileData.fromByteArray(serialized);
        
        assertTrue(deserialized.isValid());
        assertEquals(original, deserialized);
        assertArrayEquals(original.getSalt(), deserialized.getSalt());
        assertArrayEquals(original.getIv(), deserialized.getIv());
        assertArrayEquals(original.getCiphertext(), deserialized.getCiphertext());
    }
    
    @Test
    @DisplayName("Should detect encrypted file format correctly")
    void shouldDetectEncryptedFileFormatCorrectly() {
        EncryptedFileData data = new EncryptedFileData(testSalt, testIv, testCiphertext);
        byte[] serialized = data.toByteArray();
        
        assertTrue(EncryptedFileData.isEncryptedFileFormat(serialized));
        
        // Test with invalid data
        byte[] invalidData = "This is not encrypted data".getBytes();
        assertFalse(EncryptedFileData.isEncryptedFileFormat(invalidData));
        
        // Test with null
        assertFalse(EncryptedFileData.isEncryptedFileFormat(null));
        
        // Test with too small data
        byte[] tooSmall = new byte[10];
        assertFalse(EncryptedFileData.isEncryptedFileFormat(tooSmall));
    }
    
    @Test
    @DisplayName("Should calculate total size correctly")
    void shouldCalculateTotalSizeCorrectly() {
        EncryptedFileData data = new EncryptedFileData(testSalt, testIv, testCiphertext);
        
        int expectedSize = 4 + // Magic bytes
                          SecurityConfiguration.SALT_LENGTH +
                          SecurityConfiguration.IV_LENGTH +
                          testCiphertext.length;
        
        assertEquals(expectedSize, data.getTotalSize());
        assertEquals(expectedSize, data.toByteArray().length);
    }
    
    @Test
    @DisplayName("Should handle invalid serialization gracefully")
    void shouldHandleInvalidSerializationGracefully() {
        // Invalid data should not serialize
        EncryptedFileData invalidData = new EncryptedFileData(null, testIv, testCiphertext);
        
        assertThrows(IllegalStateException.class, () -> {
            invalidData.toByteArray();
        });
    }
    
    @Test
    @DisplayName("Should handle invalid deserialization gracefully")
    void shouldHandleInvalidDeserializationGracefully() {
        // Null data
        assertThrows(IllegalArgumentException.class, () -> {
            EncryptedFileData.fromByteArray(null);
        });
        
        // Too small data
        byte[] tooSmall = new byte[10];
        assertThrows(IllegalArgumentException.class, () -> {
            EncryptedFileData.fromByteArray(tooSmall);
        });
        
        // Wrong magic bytes
        byte[] wrongMagic = new byte[100];
        random.nextBytes(wrongMagic);
        assertThrows(IllegalArgumentException.class, () -> {
            EncryptedFileData.fromByteArray(wrongMagic);
        });
    }
    
    @Test
    @DisplayName("Should provide format information")
    void shouldProvideFormatInformation() {
        EncryptedFileData data = new EncryptedFileData(testSalt, testIv, testCiphertext);
        
        String formatInfo = data.getFormatInfo();
        assertNotNull(formatInfo);
        assertTrue(formatInfo.contains("salt"));
        assertTrue(formatInfo.contains("IV"));
        assertTrue(formatInfo.contains("ciphertext"));
        
        // Invalid data should return error message
        EncryptedFileData invalidData = new EncryptedFileData(null, testIv, testCiphertext);
        assertTrue(invalidData.getFormatInfo().contains("Invalid"));
    }
    
    @Test
    @DisplayName("Should implement equals and hashCode correctly")
    void shouldImplementEqualsAndHashCodeCorrectly() {
        EncryptedFileData data1 = new EncryptedFileData(testSalt, testIv, testCiphertext);
        EncryptedFileData data2 = new EncryptedFileData(testSalt.clone(), testIv.clone(), testCiphertext.clone());
        EncryptedFileData data3 = new EncryptedFileData(testSalt, testIv, new byte[64]);
        
        assertEquals(data1, data2);
        assertEquals(data1.hashCode(), data2.hashCode());
        
        assertNotEquals(data1, data3);
        assertNotEquals(data1, null);
        assertNotEquals(data1, "not an EncryptedFileData");
    }
    
    @Test
    @DisplayName("Should return defensive copies")
    void shouldReturnDefensiveCopies() {
        EncryptedFileData data = new EncryptedFileData(testSalt, testIv, testCiphertext);
        
        byte[] returnedSalt = data.getSalt();
        byte[] returnedIv = data.getIv();
        byte[] returnedCiphertext = data.getCiphertext();
        
        // Modify returned arrays
        returnedSalt[0] = (byte) ~returnedSalt[0];
        returnedIv[0] = (byte) ~returnedIv[0];
        returnedCiphertext[0] = (byte) ~returnedCiphertext[0];
        
        // Original data should be unchanged
        assertArrayEquals(testSalt, data.getSalt());
        assertArrayEquals(testIv, data.getIv());
        assertArrayEquals(testCiphertext, data.getCiphertext());
    }
}
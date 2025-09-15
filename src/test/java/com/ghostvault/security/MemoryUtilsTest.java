package com.ghostvault.security;

import java.util.Arrays;

/**
 * Basic test for MemoryUtils functionality
 */
public class MemoryUtilsTest {
    
    public static void main(String[] args) {
        MemoryUtilsTest test = new MemoryUtilsTest();
        
        try {
            test.testByteArrayWipe();
            test.testCharArrayWipe();
            test.testConstantTimeEquals();
            test.testSecureCopy();
            test.testMultipleArrayWipe();
            
            System.out.println("✅ All MemoryUtils tests passed!");
            
        } catch (Exception e) {
            System.err.println("❌ Test failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    private void testByteArrayWipe() {
        System.out.println("Testing byte array wipe...");
        
        byte[] data = "Sensitive data".getBytes();
        byte[] original = data.clone();
        
        assert !MemoryUtils.isWiped(data) : "Data should not be wiped initially";
        
        MemoryUtils.secureWipe(data);
        
        assert MemoryUtils.isWiped(data) : "Data should be wiped after secureWipe";
        assert !Arrays.equals(original, data) : "Data should be different after wipe";
        
        // Test null safety
        MemoryUtils.secureWipe((byte[]) null);
        
        System.out.println("✓ Byte array wipe test passed");
    }
    
    private void testCharArrayWipe() {
        System.out.println("Testing char array wipe...");
        
        char[] data = "Sensitive password".toCharArray();
        char[] original = data.clone();
        
        assert !MemoryUtils.isWiped(data) : "Data should not be wiped initially";
        
        MemoryUtils.secureWipe(data);
        
        assert MemoryUtils.isWiped(data) : "Data should be wiped after secureWipe";
        assert !Arrays.equals(original, data) : "Data should be different after wipe";
        
        // Test null safety
        MemoryUtils.secureWipe((char[]) null);
        
        System.out.println("✓ Char array wipe test passed");
    }
    
    private void testConstantTimeEquals() {
        System.out.println("Testing constant-time equals...");
        
        byte[] data1 = "test data".getBytes();
        byte[] data2 = "test data".getBytes();
        byte[] data3 = "different".getBytes();
        
        assert MemoryUtils.constantTimeEquals(data1, data2) : "Same data should be equal";
        assert !MemoryUtils.constantTimeEquals(data1, data3) : "Different data should not be equal";
        assert !MemoryUtils.constantTimeEquals(data1, null) : "Data and null should not be equal";
        assert MemoryUtils.constantTimeEquals((byte[]) null, (byte[]) null) : "Null and null should be equal";
        
        // Test char arrays
        char[] chars1 = "password".toCharArray();
        char[] chars2 = "password".toCharArray();
        char[] chars3 = "different".toCharArray();
        
        assert MemoryUtils.constantTimeEquals(chars1, chars2) : "Same char data should be equal";
        assert !MemoryUtils.constantTimeEquals(chars1, chars3) : "Different char data should not be equal";
        
        System.out.println("✓ Constant-time equals test passed");
    }
    
    private void testSecureCopy() {
        System.out.println("Testing secure copy...");
        
        byte[] original = "test data".getBytes();
        byte[] copy = MemoryUtils.secureCopy(original);
        
        assert copy != null : "Copy should not be null";
        assert copy != original : "Copy should be different object";
        assert Arrays.equals(original, copy) : "Copy should have same content";
        
        // Test null safety
        byte[] nullCopy = MemoryUtils.secureCopy((byte[]) null);
        assert nullCopy == null : "Copy of null should be null";
        
        // Test char arrays
        char[] originalChars = "password".toCharArray();
        char[] copyChars = MemoryUtils.secureCopy(originalChars);
        
        assert copyChars != null : "Char copy should not be null";
        assert copyChars != originalChars : "Char copy should be different object";
        assert Arrays.equals(originalChars, copyChars) : "Char copy should have same content";
        
        System.out.println("✓ Secure copy test passed");
    }
    
    private void testMultipleArrayWipe() {
        System.out.println("Testing multiple array wipe...");
        
        byte[] data1 = "data1".getBytes();
        byte[] data2 = "data2".getBytes();
        byte[] data3 = "data3".getBytes();
        
        MemoryUtils.secureWipe(data1, data2, data3);
        
        assert MemoryUtils.isWiped(data1) : "Data1 should be wiped";
        assert MemoryUtils.isWiped(data2) : "Data2 should be wiped";
        assert MemoryUtils.isWiped(data3) : "Data3 should be wiped";
        
        // Test char arrays
        char[] chars1 = "chars1".toCharArray();
        char[] chars2 = "chars2".toCharArray();
        
        MemoryUtils.secureWipe(chars1, chars2);
        
        assert MemoryUtils.isWiped(chars1) : "Chars1 should be wiped";
        assert MemoryUtils.isWiped(chars2) : "Chars2 should be wiped";
        
        System.out.println("✓ Multiple array wipe test passed");
    }
}
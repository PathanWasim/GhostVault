package com.ghostvault.security;

import org.junit.Test;
import static org.junit.Assert.*;
import javax.crypto.SecretKey;

public class SecurityManagerTest {
    @Test
    public void testEncryptionDecryption() throws Exception {
        String password = "StrongP@ssw0rd!";
        String data = "Sensitive Data";
        SecurityManager sm = new SecurityManager();
        SecretKey key = sm.deriveKey(password);
        sm.setEncryptionKey(key);
        byte[] encrypted = sm.encrypt(data.getBytes());
        byte[] decrypted = sm.decrypt(encrypted);
        assertEquals(data, new String(decrypted));
    }

    @Test
    public void testHashPasswordConsistency() throws Exception {
        String password = "StrongP@ssw0rd!";
        SecurityManager sm = new SecurityManager();
        byte[] hash1 = sm.hashPassword(password);
        byte[] hash2 = sm.hashPassword(password);
        assertArrayEquals(hash1, hash2);
    }

    // Add more tests for secureDelete and file ops as needed
}

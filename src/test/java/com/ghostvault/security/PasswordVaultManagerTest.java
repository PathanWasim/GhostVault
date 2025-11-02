package com.ghostvault.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Unit tests for password manager functionality
 */
public class PasswordVaultManagerTest {
    
    @TempDir
    Path tempDir;
    
    private PasswordVaultManager passwordManager;
    private SecretKey testKey;
    
    @BeforeEach
    void setUp() throws Exception {
        // Generate test encryption key
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        testKey = keyGen.generateKey();
        
        // Initialize password manager with temp directory
        passwordManager = new PasswordVaultManager(tempDir.toString());
        passwordManager.setEncryptionKey(testKey);
    }
    
    @Test
    @DisplayName("Should add password entry successfully")
    void shouldAddPasswordEntrySuccessfully() throws Exception {
        // Given
        PasswordEntry entry = new PasswordEntry("Test Service", "testuser", "testpass123", "https://test.com");
        
        // When
        passwordManager.addPassword(entry);
        
        // Then
        List<PasswordEntry> passwords = passwordManager.getAllPasswords();
        assertEquals(1, passwords.size());
        assertEquals("Test Service", passwords.get(0).getServiceName());
    }
    
    @Test
    @DisplayName("Should update existing password entry")
    void shouldUpdateExistingPasswordEntry() throws Exception {
        // Given
        PasswordEntry entry = new PasswordEntry("Test Service", "testuser", "oldpass", "https://test.com");
        passwordManager.addPassword(entry);
        
        // When
        entry.setPassword("newpass123");
        passwordManager.updatePassword(entry);
        
        // Then
        List<PasswordEntry> passwords = passwordManager.getAllPasswords();
        assertEquals(1, passwords.size());
        assertEquals("newpass123", passwords.get(0).getPassword());
    }
    
    @Test
    @DisplayName("Should delete password entry successfully")
    void shouldDeletePasswordEntrySuccessfully() throws Exception {
        // Given
        PasswordEntry entry = new PasswordEntry("Test Service", "testuser", "testpass", "https://test.com");
        passwordManager.addPassword(entry);
        String entryId = entry.getId();
        
        // When
        passwordManager.deletePassword(entryId);
        
        // Then
        List<PasswordEntry> passwords = passwordManager.getAllPasswords();
        assertTrue(passwords.isEmpty());
    }
    
    @Test
    @DisplayName("Should search passwords by service name")
    void shouldSearchPasswordsByServiceName() throws Exception {
        // Given
        passwordManager.addPassword(new PasswordEntry("Gmail", "user1", "pass1", "https://gmail.com"));
        passwordManager.addPassword(new PasswordEntry("Facebook", "user2", "pass2", "https://facebook.com"));
        passwordManager.addPassword(new PasswordEntry("GitHub", "user3", "pass3", "https://github.com"));
        
        // When
        List<PasswordEntry> results = passwordManager.searchPasswords("git");
        
        // Then
        assertEquals(1, results.size());
        assertEquals("GitHub", results.get(0).getServiceName());
    }
    
    @Test
    @DisplayName("Should search passwords by username")
    void shouldSearchPasswordsByUsername() throws Exception {
        // Given
        passwordManager.addPassword(new PasswordEntry("Service1", "john.doe", "pass1", "https://service1.com"));
        passwordManager.addPassword(new PasswordEntry("Service2", "jane.smith", "pass2", "https://service2.com"));
        
        // When
        List<PasswordEntry> results = passwordManager.searchPasswords("john");
        
        // Then
        assertEquals(1, results.size());
        assertEquals("john.doe", results.get(0).getUsername());
    }
    
    @Test
    @DisplayName("Should calculate password strength correctly")
    void shouldCalculatePasswordStrengthCorrectly() {
        // Test weak password
        int weakScore = passwordManager.calculatePasswordStrength("123");
        assertTrue(weakScore < 40);
        
        // Test medium password
        int mediumScore = passwordManager.calculatePasswordStrength("password123");
        assertTrue(mediumScore >= 20 && mediumScore < 80);
        
        // Test strong password
        int strongScore = passwordManager.calculatePasswordStrength("MyStr0ng!P@ssw0rd");
        assertTrue(strongScore >= 60);
    }
    
    @Test
    @DisplayName("Should generate secure password with default settings")
    void shouldGenerateSecurePasswordWithDefaultSettings() {
        // When
        String password = passwordManager.generateSecurePassword(12, true);
        
        // Then
        assertNotNull(password);
        assertEquals(12, password.length());
        assertTrue(password.matches(".*[A-Z].*")); // Contains uppercase
        assertTrue(password.matches(".*[a-z].*")); // Contains lowercase
        assertTrue(password.matches(".*\\d.*"));   // Contains digit
    }
    
    @Test
    @DisplayName("Should generate password without symbols when requested")
    void shouldGeneratePasswordWithoutSymbolsWhenRequested() {
        // When
        String password = passwordManager.generateSecurePassword(10, false);
        
        // Then
        assertNotNull(password);
        assertEquals(10, password.length());
        assertFalse(password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{}|;:,.<>?].*"));
    }
    
    @Test
    @DisplayName("Should save and load passwords with encryption")
    void shouldSaveAndLoadPasswordsWithEncryption() throws Exception {
        // Given
        PasswordEntry entry1 = new PasswordEntry("Service1", "user1", "pass1", "https://service1.com");
        PasswordEntry entry2 = new PasswordEntry("Service2", "user2", "pass2", "https://service2.com");
        
        passwordManager.addPassword(entry1);
        passwordManager.addPassword(entry2);
        
        // When - create new manager instance and load
        PasswordVaultManager newManager = new PasswordVaultManager(tempDir.toString());
        newManager.setEncryptionKey(testKey);
        newManager.loadPasswords();
        
        // Then
        List<PasswordEntry> loadedPasswords = newManager.getAllPasswords();
        assertEquals(2, loadedPasswords.size());
    }
    
    @Test
    @DisplayName("Should provide password statistics")
    void shouldProvidePasswordStatistics() throws Exception {
        // Given
        passwordManager.addPassword(new PasswordEntry("Service1", "user1", "weak", "https://service1.com"));
        passwordManager.addPassword(new PasswordEntry("Service2", "user2", "StrongP@ssw0rd!", "https://service2.com"));
        passwordManager.addPassword(new PasswordEntry("Service3", "user3", "AnotherStr0ng!Pass", "https://service3.com"));
        
        // When
        Map<String, Object> stats = passwordManager.getPasswordStatistics();
        
        // Then
        assertEquals(3, stats.get("total"));
        assertTrue((Integer) stats.get("weak") >= 0);
        assertTrue((Integer) stats.get("strong") >= 0);
    }
    
    @Test
    @DisplayName("Should schedule password expiration")
    void shouldSchedulePasswordExpiration() throws Exception {
        // Given
        PasswordEntry entry = new PasswordEntry("Test Service", "testuser", "testpass", "https://test.com");
        passwordManager.addPassword(entry);
        LocalDateTime expiration = LocalDateTime.now().plusDays(30);
        
        // When
        passwordManager.schedulePasswordExpiration(entry.getId(), expiration);
        
        // Then
        List<PasswordEntry> passwords = passwordManager.getAllPasswords();
        assertEquals(expiration.toLocalDate(), passwords.get(0).getExpirationDate().toLocalDate());
    }
    
    @Test
    @DisplayName("Should get expiring passwords")
    void shouldGetExpiringPasswords() throws Exception {
        // Given
        PasswordEntry expiringSoon = new PasswordEntry("Expiring Service", "user", "pass", "https://expiring.com");
        expiringSoon.setExpirationDate(LocalDateTime.now().plusDays(5));
        
        PasswordEntry notExpiring = new PasswordEntry("Safe Service", "user", "pass", "https://safe.com");
        notExpiring.setExpirationDate(LocalDateTime.now().plusDays(60));
        
        passwordManager.addPassword(expiringSoon);
        passwordManager.addPassword(notExpiring);
        
        // When
        List<PasswordEntry> expiringPasswords = passwordManager.getExpiringPasswords(30);
        
        // Then
        assertEquals(1, expiringPasswords.size());
        assertEquals("Expiring Service", expiringPasswords.get(0).getServiceName());
    }
    
    @Test
    @DisplayName("Should handle encryption key not set error")
    void shouldHandleEncryptionKeyNotSetError() {
        // Given
        PasswordVaultManager managerWithoutKey = new PasswordVaultManager(tempDir.toString());
        
        // When/Then
        assertThrows(IllegalStateException.class, () -> {
            managerWithoutKey.savePasswords();
        });
    }
    
    @Test
    @DisplayName("Should handle invalid password entry updates")
    void shouldHandleInvalidPasswordEntryUpdates() {
        // When/Then
        assertThrows(IllegalArgumentException.class, () -> {
            passwordManager.updatePassword(null);
        });
        
        assertThrows(Exception.class, () -> {
            PasswordEntry invalidEntry = new PasswordEntry();
            invalidEntry.setId("nonexistent");
            passwordManager.updatePassword(invalidEntry);
        });
    }
    
    @Test
    @DisplayName("Should analyze password strength with suggestions")
    void shouldAnalyzePasswordStrengthWithSuggestions() {
        // When
        PasswordVaultManager.PasswordStrengthAnalysis analysis = 
            passwordManager.analyzePasswordStrength("weak");
        
        // Then
        assertNotNull(analysis);
        assertTrue(analysis.getScore() < 50);
        assertEquals("Very Weak", analysis.getLevel());
        assertFalse(analysis.getSuggestions().isEmpty());
    }
    
    @Test
    @DisplayName("Should handle empty password list gracefully")
    void shouldHandleEmptyPasswordListGracefully() throws Exception {
        // When
        List<PasswordEntry> passwords = passwordManager.getAllPasswords();
        List<PasswordEntry> searchResults = passwordManager.searchPasswords("anything");
        Map<String, Object> stats = passwordManager.getPasswordStatistics();
        
        // Then
        assertTrue(passwords.isEmpty());
        assertTrue(searchResults.isEmpty());
        assertEquals(0, stats.get("total"));
    }
}
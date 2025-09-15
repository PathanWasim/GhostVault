package com.ghostvault.security;

/**
 * Test for PasswordValidator functionality
 */
public class PasswordValidatorTest {
    
    public static void main(String[] args) {
        PasswordValidatorTest test = new PasswordValidatorTest();
        
        try {
            test.testPasswordValidation();
            test.testPasswordStrength();
            test.testPasswordSimilarity();
            test.testValidationResult();
            
            System.out.println("✅ All PasswordValidator tests passed!");
            
        } catch (Exception e) {
            System.err.println("❌ Test failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    private void testPasswordValidation() {
        System.out.println("Testing password validation...");
        
        // Test empty password
        PasswordValidator.ValidationResult result = PasswordValidator.validatePassword("", 4);
        assert !result.isValid() : "Empty password should be invalid";
        assert result.getScore() == 0 : "Empty password should have score 0";
        
        // Test weak password
        result = PasswordValidator.validatePassword("weak", 4);
        assert !result.isValid() : "Weak password should be invalid";
        assert result.getScore() < 4 : "Weak password should have low score";
        assert !result.getIssues().isEmpty() : "Weak password should have issues";
        
        // Test strong password
        result = PasswordValidator.validatePassword("StrongPassword123!", 4);
        assert result.isValid() : "Strong password should be valid";
        assert result.getScore() >= 4 : "Strong password should have high score";
        
        // Test very strong password
        result = PasswordValidator.validatePassword("VeryStrongPassword123!@#$", 4);
        assert result.isValid() : "Very strong password should be valid";
        assert result.getScore() == 5 : "Very strong password should have max score";
        
        System.out.println("✓ Password validation test passed");
    }
    
    private void testPasswordStrength() {
        System.out.println("Testing password strength calculation...");
        
        // Test various strength levels
        assert PasswordValidator.getPasswordStrength("") == 0 : "Empty password strength";
        assert PasswordValidator.getPasswordStrength("weak") == 1 : "Very weak password strength";
        assert PasswordValidator.getPasswordStrength("Password") == 2 : "Weak password strength";
        assert PasswordValidator.getPasswordStrength("Password123") == 3 : "Fair password strength";
        assert PasswordValidator.getPasswordStrength("Password123!") >= 4 : "Strong password strength";
        assert PasswordValidator.getPasswordStrength("VeryStrongPassword123!@#") == 5 : "Very strong password strength";
        
        System.out.println("✓ Password strength test passed");
    }
    
    private void testPasswordSimilarity() {
        System.out.println("Testing password similarity...");
        
        // Test identical passwords
        assert !PasswordValidator.arePasswordsSufficientlyDifferent("password", "password", "different") : 
            "Identical passwords should not be sufficiently different";
        
        // Test different passwords
        assert PasswordValidator.arePasswordsSufficientlyDifferent("MasterPass123!", "PanicPass456@", "DecoyPass789#") : 
            "Different passwords should be sufficiently different";
        
        // Test similar passwords
        assert !PasswordValidator.arePasswordsSufficientlyDifferent("password123", "password124", "different") : 
            "Very similar passwords should not be sufficiently different";
        
        System.out.println("✓ Password similarity test passed");
    }
    
    private void testValidationResult() {
        System.out.println("Testing validation result...");
        
        PasswordValidator.ValidationResult result = PasswordValidator.validatePassword("TestPassword123!", 4);
        
        // Test result properties
        assert result.getScore() >= 0 && result.getScore() <= 5 : "Score should be between 0 and 5";
        assert result.getIssues() != null : "Issues list should not be null";
        assert result.getSuggestions() != null : "Suggestions list should not be null";
        
        // Test strength description
        String description = result.getStrengthDescription();
        assert description != null && !description.isEmpty() : "Strength description should not be empty";
        
        // Test strength color
        String color = result.getStrengthColor();
        assert color != null && color.startsWith("#") : "Strength color should be a hex color";
        
        System.out.println("✓ Validation result test passed");
    }
}
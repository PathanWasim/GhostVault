package de.mkammerer.argon2;

/**
 * Simple Argon2Factory stub for compilation
 */
public class Argon2Factory {
    
    public enum Argon2Types {
        ARGON2id
    }
    
    public static Argon2 create() {
        return new SimpleArgon2Impl();
    }
    
    public static Argon2 create(Argon2Types type) {
        return new SimpleArgon2Impl();
    }
    
    private static class SimpleArgon2Impl implements Argon2 {
        @Override
        public String hash(int iterations, int memory, int parallelism, String password, java.nio.charset.Charset charset) {
            // Simple hash simulation
            return "argon2_hash_" + password.hashCode();
        }
        
        @Override
        public boolean verify(String hash, String password, java.nio.charset.Charset charset) {
            // Simple verification
            String expectedHash = "argon2_hash_" + password.hashCode();
            return expectedHash.equals(hash);
        }
    }
}
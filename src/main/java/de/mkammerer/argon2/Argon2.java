package de.mkammerer.argon2;

import java.nio.charset.Charset;

/**
 * Simple Argon2 stub for compilation
 */
public interface Argon2 {
    String hash(int iterations, int memory, int parallelism, String password, Charset charset);
    boolean verify(String hash, String password, Charset charset);
}
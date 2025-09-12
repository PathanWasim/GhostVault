package com.ghostvault.security;

import com.ghostvault.config.AppConfig;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.SecureRandom;

/**
 * Provides secure file deletion with multiple overwrite passes
 * Implements DoD 5220.22-M standard for secure deletion
 */
public class SecureDeletion {
    
    private static final SecureRandom secureRandom = new SecureRandom();
    
    /**
     * Securely delete a file with multiple overwrite passes
     */
    public static void secureDelete(File file) throws IOException {
        if (file == null || !file.exists() || !file.isFile()) {
            return;
        }
        
        long fileSize = file.length();
        if (fileSize == 0) {
            file.delete();
            return;
        }
        
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw");
             FileChannel channel = raf.getChannel()) {
            
            // Perform multiple overwrite passes
            for (int pass = 0; pass < AppConfig.SECURE_DELETE_PASSES; pass++) {
                overwriteFile(channel, fileSize, pass);
            }
            
            // Force all changes to be written to disk
            channel.force(true);
        }
        
        // Finally delete the file
        if (!file.delete()) {
            throw new IOException("Failed to delete file after secure overwrite");
        }
    }
    
    /**
     * Overwrite file with specific pattern based on pass number
     */
    private static void overwriteFile(FileChannel channel, long fileSize, int passNumber) throws IOException {
        channel.position(0);
        
        ByteBuffer buffer = ByteBuffer.allocate(4096);
        long written = 0;
        
        while (written < fileSize) {
            buffer.clear();
            
            // Fill buffer with pattern based on pass number
            fillBufferWithPattern(buffer, passNumber);
            buffer.flip();
            
            // Adjust buffer limit if remaining bytes are less than buffer capacity
            long remaining = fileSize - written;
            if (remaining < buffer.remaining()) {
                buffer.limit((int) remaining);
            }
            
            int bytesWritten = channel.write(buffer);
            written += bytesWritten;
        }
        
        // Force write to disk
        channel.force(true);
    }
    
    /**
     * Fill buffer with overwrite pattern
     * Pass 0: Random data
     * Pass 1: All zeros
     * Pass 2: All ones (0xFF)
     */
    private static void fillBufferWithPattern(ByteBuffer buffer, int passNumber) {
        byte[] data = new byte[buffer.capacity()];
        
        switch (passNumber % 3) {
            case 0:
                // Random data
                secureRandom.nextBytes(data);
                break;
            case 1:
                // All zeros
                // data array is already initialized with zeros
                break;
            case 2:
                // All ones
                for (int i = 0; i < data.length; i++) {
                    data[i] = (byte) 0xFF;
                }
                break;
        }
        
        buffer.put(data);
    }
    
    /**
     * Securely delete directory and all contents
     */
    public static void secureDeleteDirectory(File directory) throws IOException {
        if (directory == null || !directory.exists()) {
            return;
        }
        
        if (directory.isFile()) {
            secureDelete(directory);
            return;
        }
        
        // Recursively delete contents
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    secureDeleteDirectory(file);
                } else {
                    secureDelete(file);
                }
            }
        }
        
        // Delete the directory itself
        if (!directory.delete()) {
            throw new IOException("Failed to delete directory: " + directory.getPath());
        }
    }
    
    /**
     * Wipe free space on the drive (best effort)
     * Creates temporary files filled with random data to overwrite free space
     */
    public static void wipeFreeSpace(File directory, long maxBytes) {
        if (directory == null || !directory.exists() || !directory.isDirectory()) {
            return;
        }
        
        try {
            long availableSpace = directory.getFreeSpace();
            long bytesToWrite = Math.min(availableSpace - (100 * 1024 * 1024), maxBytes); // Leave 100MB free
            
            if (bytesToWrite <= 0) {
                return;
            }
            
            File tempFile = new File(directory, ".ghostvault_wipe_" + System.currentTimeMillis());
            
            try (RandomAccessFile raf = new RandomAccessFile(tempFile, "rw")) {
                byte[] buffer = new byte[1024 * 1024]; // 1MB buffer
                long written = 0;
                
                while (written < bytesToWrite) {
                    secureRandom.nextBytes(buffer);
                    
                    int toWrite = (int) Math.min(buffer.length, bytesToWrite - written);
                    raf.write(buffer, 0, toWrite);
                    written += toWrite;
                }
                
                raf.getFD().sync(); // Force write to disk
            }
            
            // Securely delete the temporary file
            secureDelete(tempFile);
            
        } catch (Exception e) {
            // Best effort - ignore errors
        }
    }
}
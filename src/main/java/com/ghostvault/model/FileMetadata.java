package com.ghostvault.model;

/**
 * Represents metadata for an encrypted file in the vault.
 */
public class FileMetadata {
    public final String name;
    public final String encryptedName;
    public final String hash;
    public final String tags;

    public FileMetadata(String name, String encryptedName, String hash, String tags) {
        this.name = name;
        this.encryptedName = encryptedName;
        this.hash = hash;
        this.tags = tags;
    }
}

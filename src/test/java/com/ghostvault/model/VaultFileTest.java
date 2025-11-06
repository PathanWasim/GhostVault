package com.ghostvault.model;

import com.ghostvault.ui.preview.PreviewComponentFactory.FileType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for VaultFile model extensions
 */
class VaultFileTest {
    
    private VaultFile javaFile;
    private VaultFile mp3File;
    private VaultFile markdownFile;
    private VaultFile imageFile;
    private VaultFile unknownFile;
    
    @BeforeEach
    void setUp() {
        javaFile = new VaultFile("Main.java", "id1", "enc1", 1024, "hash1", System.currentTimeMillis());
        mp3File = new VaultFile("song.mp3", "id2", "enc2", 5242880, "hash2", System.currentTimeMillis());
        markdownFile = new VaultFile("README.md", "id3", "enc3", 2048, "hash3", System.currentTimeMillis());
        imageFile = new VaultFile("photo.png", "id4", "enc4", 1048576, "hash4", System.currentTimeMillis());
        unknownFile = new VaultFile("data.xyz", "id5", "enc5", 512, "hash5", System.currentTimeMillis());
    }
    
    @Test
    @DisplayName("Should extract file extensions correctly")
    void testGetExtension() {
        assertEquals("java", javaFile.getExtension());
        assertEquals("mp3", mp3File.getExtension());
        assertEquals("md", markdownFile.getExtension());
        assertEquals("png", imageFile.getExtension());
        assertEquals("xyz", unknownFile.getExtension());
        
        // Test file without extension
        VaultFile noExt = new VaultFile("README", "id", "enc", 100, "hash", System.currentTimeMillis());
        assertEquals("", noExt.getExtension());
    }
    
    @Test
    @DisplayName("Should get correct file categories")
    void testGetCategory() {
        assertEquals(VaultFile.FileCategory.CODE, javaFile.getCategory());
        assertEquals(VaultFile.FileCategory.MEDIA, mp3File.getCategory());
        assertEquals(VaultFile.FileCategory.TEXT, markdownFile.getCategory());
        assertEquals(VaultFile.FileCategory.IMAGE, imageFile.getCategory());
        assertEquals(VaultFile.FileCategory.OTHER, unknownFile.getCategory());
    }
    
    @Test
    @DisplayName("Should get correct file types")
    void testGetFileType() {
        assertEquals(FileType.CODE_JAVA, javaFile.getFileType());
        assertEquals(FileType.AUDIO_MP3, mp3File.getFileType());
        assertEquals(FileType.TEXT_MARKDOWN, markdownFile.getFileType());
        assertEquals(FileType.IMAGE_PNG, imageFile.getFileType());
        assertEquals(FileType.UNKNOWN, unknownFile.getFileType());
    }
    
    @Test
    @DisplayName("Should get correct code languages")
    void testGetCodeLanguage() {
        assertEquals(VaultFile.CodeLanguage.JAVA, javaFile.getCodeLanguage());
        assertEquals(VaultFile.CodeLanguage.UNKNOWN, mp3File.getCodeLanguage());
        assertEquals(VaultFile.CodeLanguage.MARKDOWN, markdownFile.getCodeLanguage());
        assertEquals(VaultFile.CodeLanguage.UNKNOWN, imageFile.getCodeLanguage());
        
        // Test other code files
        VaultFile pythonFile = new VaultFile("script.py", "id", "enc", 100, "hash", System.currentTimeMillis());
        assertEquals(VaultFile.CodeLanguage.PYTHON, pythonFile.getCodeLanguage());
        
        VaultFile jsFile = new VaultFile("app.js", "id", "enc", 100, "hash", System.currentTimeMillis());
        assertEquals(VaultFile.CodeLanguage.JAVASCRIPT, jsFile.getCodeLanguage());
    }
    
    @Test
    @DisplayName("Should correctly identify media files")
    void testIsMediaFile() {
        assertFalse(javaFile.isMediaFile());
        assertTrue(mp3File.isMediaFile());
        assertFalse(markdownFile.isMediaFile());
        assertFalse(imageFile.isMediaFile());
        assertFalse(unknownFile.isMediaFile());
        
        // Test video file
        VaultFile videoFile = new VaultFile("movie.mp4", "id", "enc", 100, "hash", System.currentTimeMillis());
        assertTrue(videoFile.isMediaFile());
    }
    
    @Test
    @DisplayName("Should correctly identify code files")
    void testIsCodeFile() {
        assertTrue(javaFile.isCodeFile());
        assertFalse(mp3File.isCodeFile());
        assertFalse(markdownFile.isCodeFile());
        assertFalse(imageFile.isCodeFile());
        assertFalse(unknownFile.isCodeFile());
        
        // Test other code files
        VaultFile jsonFile = new VaultFile("config.json", "id", "enc", 100, "hash", System.currentTimeMillis());
        assertTrue(jsonFile.isCodeFile());
    }
    
    @Test
    @DisplayName("Should correctly identify text files")
    void testIsTextFile() {
        assertFalse(javaFile.isTextFile());
        assertFalse(mp3File.isTextFile());
        assertTrue(markdownFile.isTextFile());
        assertFalse(imageFile.isTextFile());
        assertFalse(unknownFile.isTextFile());
        
        // Test plain text file
        VaultFile txtFile = new VaultFile("readme.txt", "id", "enc", 100, "hash", System.currentTimeMillis());
        assertTrue(txtFile.isTextFile());
    }
    
    @Test
    @DisplayName("Should correctly identify image files")
    void testIsImageFile() {
        assertFalse(javaFile.isImageFile());
        assertFalse(mp3File.isImageFile());
        assertFalse(markdownFile.isImageFile());
        assertTrue(imageFile.isImageFile());
        assertFalse(unknownFile.isImageFile());
        
        // Test other image formats
        VaultFile jpegFile = new VaultFile("photo.jpg", "id", "enc", 100, "hash", System.currentTimeMillis());
        assertTrue(jpegFile.isImageFile());
    }
    
    @Test
    @DisplayName("Should correctly identify document files")
    void testIsDocumentFile() {
        assertFalse(javaFile.isDocumentFile());
        assertFalse(mp3File.isDocumentFile());
        assertFalse(markdownFile.isDocumentFile());
        assertFalse(imageFile.isDocumentFile());
        assertFalse(unknownFile.isDocumentFile());
        
        // Test PDF file
        VaultFile pdfFile = new VaultFile("document.pdf", "id", "enc", 100, "hash", System.currentTimeMillis());
        assertTrue(pdfFile.isDocumentFile());
    }
    
    @Test
    @DisplayName("Should get correct MIME types")
    void testGetMimeType() {
        assertEquals("text/x-java-source", javaFile.getMimeType());
        assertEquals("audio/mpeg", mp3File.getMimeType());
        assertEquals("text/markdown", markdownFile.getMimeType());
        assertEquals("image/png", imageFile.getMimeType());
        assertEquals("application/octet-stream", unknownFile.getMimeType());
    }
    
    @Test
    @DisplayName("Should check preview support correctly")
    void testIsPreviewSupported() {
        assertTrue(javaFile.isPreviewSupported());
        assertTrue(mp3File.isPreviewSupported());
        assertTrue(markdownFile.isPreviewSupported());
        assertTrue(imageFile.isPreviewSupported());
        assertFalse(unknownFile.isPreviewSupported());
    }
    
    @Test
    @DisplayName("Should get appropriate file icons")
    void testGetIcon() {
        assertEquals("💻", javaFile.getIcon());
        assertEquals("🎵", mp3File.getIcon());
        assertEquals("📝", markdownFile.getIcon());
        assertEquals("🖼️", imageFile.getIcon());
        assertEquals("📁", unknownFile.getIcon());
        
        // Test video file icon
        VaultFile videoFile = new VaultFile("movie.mp4", "id", "enc", 100, "hash", System.currentTimeMillis());
        assertEquals("🎬", videoFile.getIcon());
    }
    
    @Test
    @DisplayName("Should format file sizes correctly")
    void testGetDisplayName() {
        assertTrue(javaFile.getDisplayName().contains("1.0 KB"));
        assertTrue(mp3File.getDisplayName().contains("5.0 MB"));
        assertTrue(markdownFile.getDisplayName().contains("2.0 KB"));
        assertTrue(imageFile.getDisplayName().contains("1.0 MB"));
        assertTrue(unknownFile.getDisplayName().contains("512 B"));
    }
    
    @Test
    @DisplayName("Should match search queries correctly")
    void testMatchesSearch() {
        assertTrue(javaFile.matchesSearch("Main"));
        assertTrue(javaFile.matchesSearch("java"));
        assertTrue(javaFile.matchesSearch("MAIN")); // Case insensitive
        assertFalse(javaFile.matchesSearch("python"));
        
        // Test with tags
        javaFile.setTags("source code programming");
        assertTrue(javaFile.matchesSearch("programming"));
        assertTrue(javaFile.matchesSearch("source"));
        
        // Test empty/null queries
        assertTrue(javaFile.matchesSearch(""));
        assertTrue(javaFile.matchesSearch(null));
    }
    
    @Test
    @DisplayName("FileCategory enum should work correctly")
    void testFileCategoryEnum() {
        assertEquals("Media Files", VaultFile.FileCategory.MEDIA.getDisplayName());
        assertEquals("🎵", VaultFile.FileCategory.MEDIA.getIcon());
        
        assertEquals("Code Files", VaultFile.FileCategory.CODE.getDisplayName());
        assertEquals("💻", VaultFile.FileCategory.CODE.getIcon());
        
        assertEquals("Text Files", VaultFile.FileCategory.TEXT.getDisplayName());
        assertEquals("📝", VaultFile.FileCategory.TEXT.getIcon());
    }
    
    @Test
    @DisplayName("CodeLanguage enum should work correctly")
    void testCodeLanguageEnum() {
        assertEquals("Java", VaultFile.CodeLanguage.JAVA.getDisplayName());
        assertArrayEquals(new String[]{"java"}, VaultFile.CodeLanguage.JAVA.getExtensions());
        
        assertEquals("Python", VaultFile.CodeLanguage.PYTHON.getDisplayName());
        assertArrayEquals(new String[]{"py"}, VaultFile.CodeLanguage.PYTHON.getExtensions());
        
        // Test fromExtension method
        assertEquals(VaultFile.CodeLanguage.JAVA, VaultFile.CodeLanguage.fromExtension("java"));
        assertEquals(VaultFile.CodeLanguage.PYTHON, VaultFile.CodeLanguage.fromExtension("py"));
        assertEquals(VaultFile.CodeLanguage.YAML, VaultFile.CodeLanguage.fromExtension("yml"));
        assertEquals(VaultFile.CodeLanguage.UNKNOWN, VaultFile.CodeLanguage.fromExtension("xyz"));
        assertEquals(VaultFile.CodeLanguage.UNKNOWN, VaultFile.CodeLanguage.fromExtension(""));
        assertEquals(VaultFile.CodeLanguage.UNKNOWN, VaultFile.CodeLanguage.fromExtension(null));
        
        // Test fromFileType method
        assertEquals(VaultFile.CodeLanguage.JAVA, VaultFile.CodeLanguage.fromFileType(FileType.CODE_JAVA));
        assertEquals(VaultFile.CodeLanguage.PYTHON, VaultFile.CodeLanguage.fromFileType(FileType.CODE_PYTHON));
        assertEquals(VaultFile.CodeLanguage.MARKDOWN, VaultFile.CodeLanguage.fromFileType(FileType.TEXT_MARKDOWN));
        assertEquals(VaultFile.CodeLanguage.UNKNOWN, VaultFile.CodeLanguage.fromFileType(FileType.AUDIO_MP3));
    }
    
    @Test
    @DisplayName("Should handle equals and hashCode correctly")
    void testEqualsAndHashCode() {
        VaultFile file1 = new VaultFile("test.txt", "id1", "enc1", 100, "hash1", 123456);
        VaultFile file2 = new VaultFile("other.txt", "id1", "enc2", 200, "hash2", 789012);
        VaultFile file3 = new VaultFile("test.txt", "id2", "enc1", 100, "hash1", 123456);
        
        // Same file ID should be equal
        assertEquals(file1, file2);
        assertEquals(file1.hashCode(), file2.hashCode());
        
        // Different file ID should not be equal
        assertNotEquals(file1, file3);
        
        // Null and different class checks
        assertNotEquals(file1, null);
        assertNotEquals(file1, "string");
    }
}
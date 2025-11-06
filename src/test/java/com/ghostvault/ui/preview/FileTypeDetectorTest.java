package com.ghostvault.ui.preview;

import com.ghostvault.model.VaultFile;
import com.ghostvault.ui.preview.PreviewComponentFactory.FileType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for FileTypeDetector
 */
class FileTypeDetectorTest {
    
    private VaultFile mockVaultFile;
    
    @BeforeEach
    void setUp() {
        mockVaultFile = mock(VaultFile.class);
    }
    
    @Test
    @DisplayName("Should detect file type from VaultFile extension")
    void testDetectFileTypeFromVaultFile() {
        when(mockVaultFile.getExtension()).thenReturn("java");
        
        FileType fileType = FileTypeDetector.detectFileType(mockVaultFile);
        assertEquals(FileType.CODE_JAVA, fileType);
    }
    
    @Test
    @DisplayName("Should detect file type from filename")
    void testDetectFromFilename() {
        assertEquals(FileType.CODE_PYTHON, FileTypeDetector.detectFromFilename("script.py"));
        assertEquals(FileType.AUDIO_MP3, FileTypeDetector.detectFromFilename("song.mp3"));
        assertEquals(FileType.VIDEO_MP4, FileTypeDetector.detectFromFilename("video.mp4"));
        assertEquals(FileType.TEXT_MARKDOWN, FileTypeDetector.detectFromFilename("readme.md"));
        assertEquals(FileType.IMAGE_PNG, FileTypeDetector.detectFromFilename("image.png"));
    }
    
    @Test
    @DisplayName("Should handle files without extensions")
    void testFilesWithoutExtension() {
        assertEquals(FileType.UNKNOWN, FileTypeDetector.detectFromFilename("README"));
        assertEquals(FileType.UNKNOWN, FileTypeDetector.detectFromFilename("Makefile"));
        assertEquals(FileType.UNKNOWN, FileTypeDetector.detectFromFilename(""));
        assertEquals(FileType.UNKNOWN, FileTypeDetector.detectFromFilename(null));
    }
    
    @Test
    @DisplayName("Should detect file type from content using Tika")
    void testDetectFromContent() {
        // JSON content
        String jsonContent = "{\"name\": \"test\", \"value\": 123}";
        byte[] jsonBytes = jsonContent.getBytes();
        FileType jsonType = FileTypeDetector.detectFileType(jsonBytes, "test.json");
        assertEquals(FileType.CODE_JSON, jsonType);
        
        // XML content
        String xmlContent = "<?xml version=\"1.0\"?><root><item>test</item></root>";
        byte[] xmlBytes = xmlContent.getBytes();
        FileType xmlType = FileTypeDetector.detectFileType(xmlBytes, "test.xml");
        assertEquals(FileType.CODE_XML, xmlType);
    }
    
    @Test
    @DisplayName("Should get correct MIME types")
    void testGetMimeType() {
        assertEquals("audio/mpeg", FileTypeDetector.getMimeTypeForFileType(FileType.AUDIO_MP3));
        assertEquals("video/mp4", FileTypeDetector.getMimeTypeForFileType(FileType.VIDEO_MP4));
        assertEquals("application/json", FileTypeDetector.getMimeTypeForFileType(FileType.CODE_JSON));
        assertEquals("text/markdown", FileTypeDetector.getMimeTypeForFileType(FileType.TEXT_MARKDOWN));
        assertEquals("image/png", FileTypeDetector.getMimeTypeForFileType(FileType.IMAGE_PNG));
    }
    
    @Test
    @DisplayName("Should get MIME type from filename")
    void testGetMimeTypeFromFilename() {
        assertEquals("audio/mpeg", FileTypeDetector.getMimeTypeFromFilename("song.mp3"));
        assertEquals("application/json", FileTypeDetector.getMimeTypeFromFilename("data.json"));
        assertEquals("text/markdown", FileTypeDetector.getMimeTypeFromFilename("readme.md"));
        assertEquals("application/octet-stream", FileTypeDetector.getMimeTypeFromFilename("unknown.xyz"));
    }
    
    @Test
    @DisplayName("Should check preview support correctly")
    void testPreviewSupport() {
        assertTrue(FileTypeDetector.isPreviewSupported(FileType.AUDIO_MP3));
        assertTrue(FileTypeDetector.isPreviewSupported(FileType.CODE_JAVA));
        assertTrue(FileTypeDetector.isPreviewSupported(FileType.TEXT_MARKDOWN));
        assertTrue(FileTypeDetector.isPreviewSupported(FileType.IMAGE_PNG));
        assertFalse(FileTypeDetector.isPreviewSupported(FileType.UNKNOWN));
        
        assertTrue(FileTypeDetector.isPreviewSupported("mp3"));
        assertTrue(FileTypeDetector.isPreviewSupported("java"));
        assertFalse(FileTypeDetector.isPreviewSupported("xyz"));
    }
    
    @Test
    @DisplayName("Should get correct file categories")
    void testGetFileCategory() {
        assertEquals(FileTypeDetector.FileCategory.MEDIA, FileTypeDetector.getFileCategory(FileType.AUDIO_MP3));
        assertEquals(FileTypeDetector.FileCategory.MEDIA, FileTypeDetector.getFileCategory(FileType.VIDEO_MP4));
        assertEquals(FileTypeDetector.FileCategory.CODE, FileTypeDetector.getFileCategory(FileType.CODE_JAVA));
        assertEquals(FileTypeDetector.FileCategory.TEXT, FileTypeDetector.getFileCategory(FileType.TEXT_MARKDOWN));
        assertEquals(FileTypeDetector.FileCategory.IMAGE, FileTypeDetector.getFileCategory(FileType.IMAGE_PNG));
        assertEquals(FileTypeDetector.FileCategory.DOCUMENT, FileTypeDetector.getFileCategory(FileType.DOCUMENT_PDF));
        assertEquals(FileTypeDetector.FileCategory.OTHER, FileTypeDetector.getFileCategory(FileType.UNKNOWN));
    }
    
    @Test
    @DisplayName("Should handle empty or null content gracefully")
    void testEmptyContent() {
        FileType type1 = FileTypeDetector.detectFileType(new byte[0], "test.txt");
        assertEquals(FileType.TEXT_PLAIN, type1);
        
        FileType type2 = FileTypeDetector.detectFileType(null, "test.json");
        assertEquals(FileType.CODE_JSON, type2);
    }
    
    @Test
    @DisplayName("Should handle case insensitive extensions")
    void testCaseInsensitiveExtensions() {
        assertEquals(FileType.CODE_JAVA, FileTypeDetector.detectFromFilename("Test.JAVA"));
        assertEquals(FileType.AUDIO_MP3, FileTypeDetector.detectFromFilename("Song.MP3"));
        assertEquals(FileType.IMAGE_PNG, FileTypeDetector.detectFromFilename("Image.PNG"));
    }
    
    @Test
    @DisplayName("Should detect various audio formats")
    void testAudioFormats() {
        assertEquals(FileType.AUDIO_MP3, FileTypeDetector.detectFromFilename("song.mp3"));
        assertEquals(FileType.AUDIO_WAV, FileTypeDetector.detectFromFilename("sound.wav"));
        assertEquals(FileType.AUDIO_AAC, FileTypeDetector.detectFromFilename("audio.aac"));
        assertEquals(FileType.AUDIO_M4A, FileTypeDetector.detectFromFilename("track.m4a"));
        assertEquals(FileType.AUDIO_FLAC, FileTypeDetector.detectFromFilename("music.flac"));
    }
    
    @Test
    @DisplayName("Should detect various video formats")
    void testVideoFormats() {
        assertEquals(FileType.VIDEO_MP4, FileTypeDetector.detectFromFilename("video.mp4"));
        assertEquals(FileType.VIDEO_AVI, FileTypeDetector.detectFromFilename("movie.avi"));
        assertEquals(FileType.VIDEO_MOV, FileTypeDetector.detectFromFilename("clip.mov"));
        assertEquals(FileType.VIDEO_MKV, FileTypeDetector.detectFromFilename("film.mkv"));
        assertEquals(FileType.VIDEO_M4V, FileTypeDetector.detectFromFilename("video.m4v"));
    }
    
    @Test
    @DisplayName("Should detect various code formats")
    void testCodeFormats() {
        assertEquals(FileType.CODE_JAVA, FileTypeDetector.detectFromFilename("Main.java"));
        assertEquals(FileType.CODE_PYTHON, FileTypeDetector.detectFromFilename("script.py"));
        assertEquals(FileType.CODE_JAVASCRIPT, FileTypeDetector.detectFromFilename("app.js"));
        assertEquals(FileType.CODE_TYPESCRIPT, FileTypeDetector.detectFromFilename("component.ts"));
        assertEquals(FileType.CODE_HTML, FileTypeDetector.detectFromFilename("index.html"));
        assertEquals(FileType.CODE_CSS, FileTypeDetector.detectFromFilename("style.css"));
        assertEquals(FileType.CODE_JSON, FileTypeDetector.detectFromFilename("config.json"));
        assertEquals(FileType.CODE_XML, FileTypeDetector.detectFromFilename("data.xml"));
        assertEquals(FileType.CODE_YAML, FileTypeDetector.detectFromFilename("config.yaml"));
        assertEquals(FileType.CODE_YML, FileTypeDetector.detectFromFilename("docker.yml"));
        assertEquals(FileType.CODE_SQL, FileTypeDetector.detectFromFilename("query.sql"));
        assertEquals(FileType.CODE_SHELL, FileTypeDetector.detectFromFilename("script.sh"));
        assertEquals(FileType.CODE_BATCH, FileTypeDetector.detectFromFilename("build.bat"));
        assertEquals(FileType.CODE_POWERSHELL, FileTypeDetector.detectFromFilename("deploy.ps1"));
    }
    
    @Test
    @DisplayName("Should detect various text formats")
    void testTextFormats() {
        assertEquals(FileType.TEXT_PLAIN, FileTypeDetector.detectFromFilename("readme.txt"));
        assertEquals(FileType.TEXT_MARKDOWN, FileTypeDetector.detectFromFilename("README.md"));
        assertEquals(FileType.TEXT_LOG, FileTypeDetector.detectFromFilename("app.log"));
        assertEquals(FileType.TEXT_INI, FileTypeDetector.detectFromFilename("config.ini"));
        assertEquals(FileType.TEXT_TOML, FileTypeDetector.detectFromFilename("pyproject.toml"));
        assertEquals(FileType.TEXT_PROPERTIES, FileTypeDetector.detectFromFilename("app.properties"));
    }
    
    @Test
    @DisplayName("FileCategory enum should have correct properties")
    void testFileCategoryEnum() {
        FileTypeDetector.FileCategory media = FileTypeDetector.FileCategory.MEDIA;
        assertEquals("Media Files", media.getDisplayName());
        assertEquals("🎵", media.getIcon());
        
        FileTypeDetector.FileCategory code = FileTypeDetector.FileCategory.CODE;
        assertEquals("Code Files", code.getDisplayName());
        assertEquals("💻", code.getIcon());
    }
}
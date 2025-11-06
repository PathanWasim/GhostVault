package com.ghostvault.ai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import com.ghostvault.model.VaultFile;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Unit tests for AI file analysis functionality
 */
public class AIFileAnalyzerTest {
    
    private AIFileAnalyzer analyzer;
    
    @BeforeEach
    void setUp() {
        analyzer = new AIFileAnalyzer();
    }
    
    @Test
    @DisplayName("Should categorize PDF files as documents")
    void shouldCategorizePdfFilesAsDocuments() {
        // Given
        FileCategory category = analyzer.categorizeByExtension("pdf");
        
        // Then
        assertEquals(FileCategory.DOCUMENT, category);
    }
    
    @Test
    @DisplayName("Should categorize JPG files as images")
    void shouldCategorizeJpgFilesAsImages() {
        // Given
        FileCategory category = analyzer.categorizeByExtension("jpg");
        
        // Then
        assertEquals(FileCategory.IMAGE, category);
    }
    
    @Test
    @DisplayName("Should categorize MP4 files as videos")
    void shouldCategorizeMp4FilesAsVideos() {
        // Given
        FileCategory category = analyzer.categorizeByExtension("mp4");
        
        // Then
        assertEquals(FileCategory.VIDEO, category);
    }
    
    @Test
    @DisplayName("Should categorize MP3 files as audio")
    void shouldCategorizeMp3FilesAsAudio() {
        // Given
        FileCategory category = analyzer.categorizeByExtension("mp3");
        
        // Then
        assertEquals(FileCategory.AUDIO, category);
    }
    
    @Test
    @DisplayName("Should categorize ZIP files as archives")
    void shouldCategorizeZipFilesAsArchives() {
        // Given
        FileCategory category = analyzer.categorizeByExtension("zip");
        
        // Then
        assertEquals(FileCategory.ARCHIVE, category);
    }
    
    @Test
    @DisplayName("Should categorize EXE files as executables")
    void shouldCategorizeExeFilesAsExecutables() {
        // Given
        FileCategory category = analyzer.categorizeByExtension("exe");
        
        // Then
        assertEquals(FileCategory.EXECUTABLE, category);
    }
    
    @Test
    @DisplayName("Should categorize Java files as code")
    void shouldCategorizeJavaFilesAsCode() {
        // Given
        FileCategory category = analyzer.categorizeByExtension("java");
        
        // Then
        assertEquals(FileCategory.CODE, category);
    }
    
    @Test
    @DisplayName("Should categorize unknown extensions as other")
    void shouldCategorizeUnknownExtensionsAsOther() {
        // Given
        FileCategory category = analyzer.categorizeByExtension("unknown");
        
        // Then
        assertEquals(FileCategory.OTHER, category);
    }
    
    @Test
    @DisplayName("Should analyze small file sizes correctly")
    void shouldAnalyzeSmallFileSizesCorrectly() {
        // Given
        long smallFileSize = 500 * 1024; // 500KB
        
        // When
        SizeCategory sizeCategory = analyzer.analyzeSizeThreshold(smallFileSize);
        
        // Then
        assertEquals(SizeCategory.SMALL, sizeCategory);
    }
    
    @Test
    @DisplayName("Should analyze medium file sizes correctly")
    void shouldAnalyzeMediumFileSizesCorrectly() {
        // Given
        long mediumFileSize = 10 * 1024 * 1024; // 10MB
        
        // When
        SizeCategory sizeCategory = analyzer.analyzeSizeThreshold(mediumFileSize);
        
        // Then
        assertEquals(SizeCategory.MEDIUM, sizeCategory);
    }
    
    @Test
    @DisplayName("Should analyze large file sizes correctly")
    void shouldAnalyzeLargeFileSizesCorrectly() {
        // Given
        long largeFileSize = 100 * 1024 * 1024; // 100MB
        
        // When
        SizeCategory sizeCategory = analyzer.analyzeSizeThreshold(largeFileSize);
        
        // Then
        assertEquals(SizeCategory.LARGE, sizeCategory);
    }
    
    @Test
    @DisplayName("Should assess executable files as high risk")
    void shouldAssessExecutableFilesAsHighRisk() {
        // Given
        VaultFile exeFile = createTestVaultFile("malware.exe", "exe", 1024);
        
        // When
        SecurityRisk risk = analyzer.assessSecurityRisk(exeFile);
        
        // Then
        assertEquals(SecurityRisk.HIGH, risk);
    }
    
    @Test
    @DisplayName("Should assess large files as medium risk")
    void shouldAssessLargeFilesAsMediumRisk() {
        // Given
        VaultFile largeFile = createTestVaultFile("large.zip", "zip", 150 * 1024 * 1024); // 150MB
        
        // When
        SecurityRisk risk = analyzer.assessSecurityRisk(largeFile);
        
        // Then
        assertEquals(SecurityRisk.MEDIUM, risk);
    }
    
    @Test
    @DisplayName("Should assess normal files as safe")
    void shouldAssessNormalFilesAsSafe() {
        // Given
        VaultFile normalFile = createTestVaultFile("document.txt", "txt", 1024);
        
        // When
        SecurityRisk risk = analyzer.assessSecurityRisk(normalFile);
        
        // Then
        assertEquals(SecurityRisk.SAFE, risk);
    }
    
    @Test
    @DisplayName("Should perform complete file analysis")
    void shouldPerformCompleteFileAnalysis() {
        // Given
        VaultFile testFile = createTestVaultFile("test.pdf", "pdf", 2 * 1024 * 1024); // 2MB
        
        // When
        FileAnalysisResult result = analyzer.analyzeFile(testFile);
        
        // Then
        assertNotNull(result);
        assertEquals(FileCategory.DOCUMENT, result.getCategory());
        assertEquals(SizeCategory.MEDIUM, result.getSizeCategory());
        assertEquals(SecurityRisk.SAFE, result.getRiskLevel());
        assertNotNull(result.getFlags());
    }
    
    @Test
    @DisplayName("Should generate flags for suspicious files")
    void shouldGenerateFlagsForSuspiciousFiles() {
        // Given
        VaultFile suspiciousFile = createTestVaultFile("virus.exe", "exe", 100 * 1024 * 1024); // 100MB exe
        
        // When
        FileAnalysisResult result = analyzer.analyzeFile(suspiciousFile);
        
        // Then
        assertTrue(result.hasAnyFlag());
        assertTrue(result.hasFlag(FileFlag.EXECUTABLE_FILE));
        assertTrue(result.hasFlag(FileFlag.LARGE_SIZE));
    }
    
    @Test
    @DisplayName("Should handle case insensitive extensions")
    void shouldHandleCaseInsensitiveExtensions() {
        // Given
        FileCategory upperCase = analyzer.categorizeByExtension("PDF");
        FileCategory lowerCase = analyzer.categorizeByExtension("pdf");
        FileCategory mixedCase = analyzer.categorizeByExtension("PdF");
        
        // Then
        assertEquals(FileCategory.DOCUMENT, upperCase);
        assertEquals(FileCategory.DOCUMENT, lowerCase);
        assertEquals(FileCategory.DOCUMENT, mixedCase);
    }
    
    @Test
    @DisplayName("Should perform security analysis with multiple factors")
    void shouldPerformSecurityAnalysisWithMultipleFactors() {
        // Given
        VaultFile testFile = createTestVaultFile("suspicious.exe", "exe", 1024);
        
        // When
        SecurityAnalysis analysis = analyzer.performSecurityAnalysis(testFile);
        
        // Then
        assertNotNull(analysis);
        assertTrue(analysis.getOverallRiskScore() > 0);
        assertEquals(SecurityRisk.HIGH, analysis.getRiskLevel());
        assertFalse(analysis.getRiskFactors().isEmpty());
    }
    
    /**
     * Helper method to create test VaultFile objects
     */
    private VaultFile createTestVaultFile(String name, String extension, long size) {
        VaultFile file = new VaultFile();
        file.setOriginalName(name);
        file.setExtension(extension);
        file.setSize(size);
        file.setCreatedDate(LocalDateTime.now());
        return file;
    }
}
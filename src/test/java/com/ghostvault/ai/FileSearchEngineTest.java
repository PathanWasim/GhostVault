package com.ghostvault.ai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import com.ghostvault.model.VaultFile;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

/**
 * Unit tests for file search functionality
 */
public class FileSearchEngineTest {
    
    private FileSearchEngine searchEngine;
    private List<VaultFile> testFiles;
    
    @BeforeEach
    void setUp() {
        searchEngine = new FileSearchEngine();
        testFiles = createTestFiles();
    }
    
    @Test
    @DisplayName("Should return all files when search query is empty")
    void shouldReturnAllFilesWhenSearchQueryIsEmpty() {
        // When
        List<VaultFile> results = searchEngine.searchFiles("", testFiles);
        
        // Then
        assertEquals(testFiles.size(), results.size());
    }
    
    @Test
    @DisplayName("Should return all files when search query is null")
    void shouldReturnAllFilesWhenSearchQueryIsNull() {
        // When
        List<VaultFile> results = searchEngine.searchFiles(null, testFiles);
        
        // Then
        assertEquals(testFiles.size(), results.size());
    }
    
    @Test
    @DisplayName("Should search files by name")
    void shouldSearchFilesByName() {
        // When
        List<VaultFile> results = searchEngine.searchFiles("document", testFiles);
        
        // Then
        assertEquals(1, results.size());
        assertTrue(results.get(0).getOriginalName().toLowerCase().contains("document"));
    }
    
    @Test
    @DisplayName("Should search files by extension")
    void shouldSearchFilesByExtension() {
        // When
        List<VaultFile> results = searchEngine.searchFiles("pdf", testFiles);
        
        // Then
        assertTrue(results.size() > 0);
        assertTrue(results.stream().anyMatch(f -> f.getExtension().equals("pdf")));
    }
    
    @Test
    @DisplayName("Should perform case insensitive search")
    void shouldPerformCaseInsensitiveSearch() {
        // When
        List<VaultFile> lowerResults = searchEngine.searchFiles("document", testFiles);
        List<VaultFile> upperResults = searchEngine.searchFiles("DOCUMENT", testFiles);
        List<VaultFile> mixedResults = searchEngine.searchFiles("Document", testFiles);
        
        // Then
        assertEquals(lowerResults.size(), upperResults.size());
        assertEquals(lowerResults.size(), mixedResults.size());
    }
    
    @Test
    @DisplayName("Should filter files by name pattern")
    void shouldFilterFilesByNamePattern() {
        // When
        List<VaultFile> results = searchEngine.filterByName("test", testFiles);
        
        // Then
        assertTrue(results.size() > 0);
        assertTrue(results.stream().allMatch(f -> 
            f.getOriginalName().toLowerCase().contains("test")));
    }
    
    @Test
    @DisplayName("Should filter files by extension")
    void shouldFilterFilesByExtension() {
        // When
        List<VaultFile> results = searchEngine.filterByExtension("jpg", testFiles);
        
        // Then
        assertTrue(results.size() > 0);
        assertTrue(results.stream().allMatch(f -> f.getExtension().equals("jpg")));
    }
    
    @Test
    @DisplayName("Should filter files by extension with dot prefix")
    void shouldFilterFilesByExtensionWithDotPrefix() {
        // When
        List<VaultFile> results = searchEngine.filterByExtension(".jpg", testFiles);
        
        // Then
        assertTrue(results.size() > 0);
        assertTrue(results.stream().allMatch(f -> f.getExtension().equals("jpg")));
    }
    
    @Test
    @DisplayName("Should filter files by category")
    void shouldFilterFilesByCategory() {
        // When
        List<VaultFile> results = searchEngine.filterByCategory(FileCategory.IMAGE, testFiles);
        
        // Then
        assertTrue(results.size() > 0);
        // Verify that all results are actually images
        AIFileAnalyzer analyzer = new AIFileAnalyzer();
        assertTrue(results.stream().allMatch(f -> 
            analyzer.analyzeFile(f).getCategory() == FileCategory.IMAGE));
    }
    
    @Test
    @DisplayName("Should filter files by security risk")
    void shouldFilterFilesBySecurityRisk() {
        // When
        List<VaultFile> results = searchEngine.filterBySecurityRisk(SecurityRisk.SAFE, testFiles);
        
        // Then
        assertTrue(results.size() > 0);
        // Verify that all results are actually safe
        AIFileAnalyzer analyzer = new AIFileAnalyzer();
        assertTrue(results.stream().allMatch(f -> 
            analyzer.analyzeFile(f).getRiskLevel() == SecurityRisk.SAFE));
    }
    
    @Test
    @DisplayName("Should filter files by size category")
    void shouldFilterFilesBySizeCategory() {
        // When
        List<VaultFile> results = searchEngine.filterBySizeCategory(SizeCategory.SMALL, testFiles);
        
        // Then
        assertTrue(results.size() > 0);
        // Verify that all results are actually small
        AIFileAnalyzer analyzer = new AIFileAnalyzer();
        assertTrue(results.stream().allMatch(f -> 
            analyzer.analyzeFile(f).getSizeCategory() == SizeCategory.SMALL));
    }
    
    @Test
    @DisplayName("Should perform advanced search with multiple criteria")
    void shouldPerformAdvancedSearchWithMultipleCriteria() {
        // Given
        SearchCriteria criteria = new SearchCriteria()
            .withNameQuery("test")
            .withExtension("jpg")
            .withCategory(FileCategory.IMAGE);
        
        // When
        SearchResult result = searchEngine.advancedSearch(criteria, testFiles);
        
        // Then
        assertNotNull(result);
        assertTrue(result.hasResults());
        assertTrue(result.getFiles().stream().allMatch(f -> 
            f.getOriginalName().toLowerCase().contains("test") && 
            f.getExtension().equals("jpg")));
    }
    
    @Test
    @DisplayName("Should handle empty search results")
    void shouldHandleEmptySearchResults() {
        // When
        List<VaultFile> results = searchEngine.searchFiles("nonexistent", testFiles);
        
        // Then
        assertTrue(results.isEmpty());
    }
    
    @Test
    @DisplayName("Should generate search suggestions")
    void shouldGenerateSearchSuggestions() {
        // When
        List<String> suggestions = searchEngine.getSearchSuggestions(testFiles);
        
        // Then
        assertFalse(suggestions.isEmpty());
        assertTrue(suggestions.contains(".pdf"));
        assertTrue(suggestions.contains(".jpg"));
        assertTrue(suggestions.contains("document"));
        assertTrue(suggestions.contains("image"));
    }
    
    @Test
    @DisplayName("Should search in AI-determined categories")
    void shouldSearchInAIDeterminedCategories() {
        // When
        List<VaultFile> results = searchEngine.searchFiles("document", testFiles);
        
        // Then
        assertTrue(results.size() > 0);
        // Should find files that are categorized as documents by AI
        AIFileAnalyzer analyzer = new AIFileAnalyzer();
        assertTrue(results.stream().anyMatch(f -> 
            analyzer.analyzeFile(f).getCategory() == FileCategory.DOCUMENT));
    }
    
    @Test
    @DisplayName("Should search in security risk levels")
    void shouldSearchInSecurityRiskLevels() {
        // When
        List<VaultFile> results = searchEngine.searchFiles("safe", testFiles);
        
        // Then
        assertTrue(results.size() > 0);
        // Should find files that are categorized as safe by AI
        AIFileAnalyzer analyzer = new AIFileAnalyzer();
        assertTrue(results.stream().anyMatch(f -> 
            analyzer.analyzeFile(f).getRiskLevel() == SecurityRisk.SAFE));
    }
    
    @Test
    @DisplayName("Should handle size range filtering in advanced search")
    void shouldHandleSizeRangeFilteringInAdvancedSearch() {
        // Given
        SearchCriteria criteria = new SearchCriteria()
            .withSizeRange(1000, 100000); // 1KB to 100KB
        
        // When
        SearchResult result = searchEngine.advancedSearch(criteria, testFiles);
        
        // Then
        assertNotNull(result);
        assertTrue(result.getFiles().stream().allMatch(f -> 
            f.getSize() >= 1000 && f.getSize() <= 100000));
    }
    
    @Test
    @DisplayName("Should provide search result statistics")
    void shouldProvideSearchResultStatistics() {
        // Given
        SearchCriteria criteria = new SearchCriteria().withNameQuery("test");
        
        // When
        SearchResult result = searchEngine.advancedSearch(criteria, testFiles);
        SearchResult.SearchStatistics stats = result.getStatistics();
        
        // Then
        assertNotNull(stats);
        assertNotNull(stats.getCategoryDistribution());
        assertNotNull(stats.getSizeDistribution());
        assertNotNull(stats.getRiskDistribution());
        assertTrue(stats.getTotalSize() >= 0);
    }
    
    /**
     * Helper method to create test files
     */
    private List<VaultFile> createTestFiles() {
        List<VaultFile> files = new ArrayList<>();
        
        // Document files
        files.add(createTestFile("document.pdf", "pdf", 50000));
        files.add(createTestFile("test_document.txt", "txt", 5000));
        
        // Image files
        files.add(createTestFile("photo.jpg", "jpg", 2000000));
        files.add(createTestFile("test_image.png", "png", 1500000));
        
        // Executable files
        files.add(createTestFile("program.exe", "exe", 10000000));
        
        // Archive files
        files.add(createTestFile("backup.zip", "zip", 50000000));
        
        return files;
    }
    
    /**
     * Helper method to create a test VaultFile
     */
    private VaultFile createTestFile(String name, String extension, long size) {
        VaultFile file = new VaultFile();
        file.setOriginalName(name);
        file.setExtension(extension);
        file.setSize(size);
        file.setCreatedDate(LocalDateTime.now());
        return file;
    }
}
package com.ghostvault.ai;

import com.ghostvault.model.VaultFile;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Enhanced file search engine with AI-powered filtering capabilities
 */
public class FileSearchEngine {
    
    private final AIFileAnalyzer aiAnalyzer;
    
    public FileSearchEngine() {
        this.aiAnalyzer = new AIFileAnalyzer();
    }
    
    /**
     * Search files by query string with multiple criteria
     */
    public List<VaultFile> searchFiles(String query, List<VaultFile> files) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>(files);
        }
        
        String searchQuery = query.toLowerCase().trim();
        
        return files.stream()
            .filter(file -> matchesSearchCriteria(file, searchQuery))
            .collect(Collectors.toList());
    }
    
    /**
     * Filter files by name pattern
     */
    public List<VaultFile> filterByName(String namePattern, List<VaultFile> files) {
        if (namePattern == null || namePattern.trim().isEmpty()) {
            return new ArrayList<>(files);
        }
        
        String pattern = namePattern.toLowerCase().trim();
        
        return files.stream()
            .filter(file -> file.getOriginalName().toLowerCase().contains(pattern))
            .collect(Collectors.toList());
    }
    
    /**
     * Filter files by extension
     */
    public List<VaultFile> filterByExtension(String extension, List<VaultFile> files) {
        if (extension == null || extension.trim().isEmpty()) {
            return new ArrayList<>(files);
        }
        
        String ext = extension.toLowerCase().trim();
        if (ext.startsWith(".")) {
            ext = ext.substring(1);
        }
        
        final String finalExt = ext;
        return files.stream()
            .filter(file -> file.getExtension().toLowerCase().equals(finalExt))
            .collect(Collectors.toList());
    }
    
    /**
     * Filter files by AI-determined category
     */
    public List<VaultFile> filterByCategory(FileCategory category, List<VaultFile> files) {
        if (category == null) {
            return new ArrayList<>(files);
        }
        
        return files.stream()
            .filter(file -> {
                FileAnalysisResult analysis = aiAnalyzer.analyzeFile(file);
                return analysis.getCategory() == category;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Filter files by security risk level
     */
    public List<VaultFile> filterBySecurityRisk(SecurityRisk riskLevel, List<VaultFile> files) {
        if (riskLevel == null) {
            return new ArrayList<>(files);
        }
        
        return files.stream()
            .filter(file -> {
                FileAnalysisResult analysis = aiAnalyzer.analyzeFile(file);
                return analysis.getRiskLevel() == riskLevel;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Filter files by size category
     */
    public List<VaultFile> filterBySizeCategory(SizeCategory sizeCategory, List<VaultFile> files) {
        if (sizeCategory == null) {
            return new ArrayList<>(files);
        }
        
        return files.stream()
            .filter(file -> {
                FileAnalysisResult analysis = aiAnalyzer.analyzeFile(file);
                return analysis.getSizeCategory() == sizeCategory;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Advanced search with multiple filters
     */
    public SearchResult advancedSearch(SearchCriteria criteria, List<VaultFile> files) {
        List<VaultFile> results = new ArrayList<>(files);
        
        // Apply name filter
        if (criteria.getNameQuery() != null && !criteria.getNameQuery().trim().isEmpty()) {
            results = filterByName(criteria.getNameQuery(), results);
        }
        
        // Apply extension filter
        if (criteria.getExtension() != null && !criteria.getExtension().trim().isEmpty()) {
            results = filterByExtension(criteria.getExtension(), results);
        }
        
        // Apply category filter
        if (criteria.getCategory() != null) {
            results = filterByCategory(criteria.getCategory(), results);
        }
        
        // Apply security risk filter
        if (criteria.getSecurityRisk() != null) {
            results = filterBySecurityRisk(criteria.getSecurityRisk(), results);
        }
        
        // Apply size category filter
        if (criteria.getSizeCategory() != null) {
            results = filterBySizeCategory(criteria.getSizeCategory(), results);
        }
        
        // Apply size range filter
        if (criteria.getMinSize() > 0 || criteria.getMaxSize() < Long.MAX_VALUE) {
            results = results.stream()
                .filter(file -> file.getSize() >= criteria.getMinSize() && 
                               file.getSize() <= criteria.getMaxSize())
                .collect(Collectors.toList());
        }
        
        return new SearchResult(results, criteria);
    }
    
    /**
     * Check if a file matches the search criteria
     */
    private boolean matchesSearchCriteria(VaultFile file, String searchQuery) {
        // Search in file name
        if (file.getOriginalName().toLowerCase().contains(searchQuery)) {
            return true;
        }
        
        // Search in file extension
        if (file.getExtension().toLowerCase().contains(searchQuery)) {
            return true;
        }
        
        // Search in AI-determined category
        FileAnalysisResult analysis = aiAnalyzer.analyzeFile(file);
        if (analysis.getCategory().getDisplayName().toLowerCase().contains(searchQuery)) {
            return true;
        }
        
        // Search in size category
        if (analysis.getSizeCategory().getDisplayName().toLowerCase().contains(searchQuery)) {
            return true;
        }
        
        // Search in security risk level
        if (analysis.getRiskLevel().getDisplayName().toLowerCase().contains(searchQuery)) {
            return true;
        }
        
        // Search in file flags
        for (FileFlag flag : analysis.getFlags()) {
            if (flag.getDisplayName().toLowerCase().contains(searchQuery)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Get search suggestions based on available files
     */
    public List<String> getSearchSuggestions(List<VaultFile> files) {
        List<String> suggestions = new ArrayList<>();
        
        // Add common extensions
        files.stream()
            .map(file -> file.getExtension().toLowerCase())
            .distinct()
            .forEach(ext -> suggestions.add("." + ext));
        
        // Add categories
        files.stream()
            .map(file -> aiAnalyzer.analyzeFile(file).getCategory().getDisplayName().toLowerCase())
            .distinct()
            .forEach(suggestions::add);
        
        // Add size categories
        suggestions.add("small");
        suggestions.add("medium");
        suggestions.add("large");
        
        // Add security levels
        suggestions.add("safe");
        suggestions.add("low risk");
        suggestions.add("medium risk");
        suggestions.add("high risk");
        
        return suggestions;
    }
}
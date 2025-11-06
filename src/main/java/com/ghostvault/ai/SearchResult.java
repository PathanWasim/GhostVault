package com.ghostvault.ai;

import com.ghostvault.model.VaultFile;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Result of a file search operation with metadata
 */
public class SearchResult {
    private final List<VaultFile> files;
    private final SearchCriteria criteria;
    private final long searchTime;
    
    public SearchResult(List<VaultFile> files, SearchCriteria criteria) {
        this.files = files;
        this.criteria = criteria;
        this.searchTime = System.currentTimeMillis();
    }
    
    public List<VaultFile> getFiles() {
        return files;
    }
    
    public SearchCriteria getCriteria() {
        return criteria;
    }
    
    public long getSearchTime() {
        return searchTime;
    }
    
    public int getResultCount() {
        return files.size();
    }
    
    public boolean isEmpty() {
        return files.isEmpty();
    }
    
    public boolean hasResults() {
        return !files.isEmpty();
    }
    
    /**
     * Get statistics about the search results
     */
    public SearchStatistics getStatistics() {
        if (files.isEmpty()) {
            return new SearchStatistics();
        }
        
        AIFileAnalyzer analyzer = new AIFileAnalyzer();
        
        // Category distribution
        Map<FileCategory, Long> categoryCount = files.stream()
            .collect(Collectors.groupingBy(
                file -> analyzer.analyzeFile(file).getCategory(),
                Collectors.counting()
            ));
        
        // Size distribution
        Map<SizeCategory, Long> sizeCount = files.stream()
            .collect(Collectors.groupingBy(
                file -> analyzer.analyzeFile(file).getSizeCategory(),
                Collectors.counting()
            ));
        
        // Risk distribution
        Map<SecurityRisk, Long> riskCount = files.stream()
            .collect(Collectors.groupingBy(
                file -> analyzer.analyzeFile(file).getRiskLevel(),
                Collectors.counting()
            ));
        
        // Total size
        long totalSize = files.stream().mapToLong(VaultFile::getSize).sum();
        
        return new SearchStatistics(categoryCount, sizeCount, riskCount, totalSize);
    }
    
    @Override
    public String toString() {
        return "SearchResult{" +
                "resultCount=" + files.size() +
                ", criteria=" + criteria +
                ", searchTime=" + searchTime +
                '}';
    }
    
    /**
     * Statistics about search results
     */
    public static class SearchStatistics {
        private final Map<FileCategory, Long> categoryDistribution;
        private final Map<SizeCategory, Long> sizeDistribution;
        private final Map<SecurityRisk, Long> riskDistribution;
        private final long totalSize;
        
        public SearchStatistics() {
            this.categoryDistribution = Map.of();
            this.sizeDistribution = Map.of();
            this.riskDistribution = Map.of();
            this.totalSize = 0;
        }
        
        public SearchStatistics(Map<FileCategory, Long> categoryDistribution,
                               Map<SizeCategory, Long> sizeDistribution,
                               Map<SecurityRisk, Long> riskDistribution,
                               long totalSize) {
            this.categoryDistribution = categoryDistribution;
            this.sizeDistribution = sizeDistribution;
            this.riskDistribution = riskDistribution;
            this.totalSize = totalSize;
        }
        
        public Map<FileCategory, Long> getCategoryDistribution() {
            return categoryDistribution;
        }
        
        public Map<SizeCategory, Long> getSizeDistribution() {
            return sizeDistribution;
        }
        
        public Map<SecurityRisk, Long> getRiskDistribution() {
            return riskDistribution;
        }
        
        public long getTotalSize() {
            return totalSize;
        }
        
        public String getFormattedTotalSize() {
            if (totalSize < 1024) return totalSize + " B";
            if (totalSize < 1024 * 1024) return String.format("%.1f KB", totalSize / 1024.0);
            if (totalSize < 1024 * 1024 * 1024) return String.format("%.1f MB", totalSize / (1024.0 * 1024));
            return String.format("%.1f GB", totalSize / (1024.0 * 1024 * 1024));
        }
    }
}
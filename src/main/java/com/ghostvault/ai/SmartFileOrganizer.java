package com.ghostvault.ai;

import com.ghostvault.model.VaultFile;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * AI-powered file organization system for GhostVault
 * Provides intelligent categorization, duplicate detection, and smart search
 */
public class SmartFileOrganizer {
    
    // File categories
    public enum FileCategory {
        DOCUMENTS("Documents", "📄", Arrays.asList("pdf", "doc", "docx", "txt", "rtf", "odt")),
        SPREADSHEETS("Spreadsheets", "📊", Arrays.asList("xls", "xlsx", "csv", "ods")),
        PRESENTATIONS("Presentations", "📊", Arrays.asList("ppt", "pptx", "odp")),
        IMAGES("Images", "🖼️", Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "svg", "webp")),
        VIDEOS("Videos", "🎬", Arrays.asList("mp4", "avi", "mkv", "mov", "wmv", "flv", "webm")),
        AUDIO("Audio", "🎵", Arrays.asList("mp3", "wav", "flac", "aac", "ogg", "wma")),
        ARCHIVES("Archives", "📦", Arrays.asList("zip", "rar", "7z", "tar", "gz", "bz2")),
        CODE("Code", "💻", Arrays.asList("java", "js", "py", "cpp", "c", "html", "css", "php")),
        FINANCIAL("Financial", "💰", Arrays.asList()),
        PERSONAL("Personal", "👤", Arrays.asList()),
        WORK("Work", "💼", Arrays.asList()),
        MEDICAL("Medical", "🏥", Arrays.asList()),
        LEGAL("Legal", "⚖️", Arrays.asList()),
        EDUCATION("Education", "🎓", Arrays.asList()),
        OTHER("Other", "📁", Arrays.asList());
        
        private final String displayName;
        private final String icon;
        private final List<String> extensions;
        
        FileCategory(String displayName, String icon, List<String> extensions) {
            this.displayName = displayName;
            this.icon = icon;
            this.extensions = extensions;
        }
        
        public String getDisplayName() { return displayName; }
        public String getIcon() { return icon; }
        public List<String> getExtensions() { return extensions; }
    }
    
    // Smart patterns for content-based categorization
    private static final Map<FileCategory, List<Pattern>> CONTENT_PATTERNS = new HashMap<>();
    
    static {
        // Financial patterns
        CONTENT_PATTERNS.put(FileCategory.FINANCIAL, Arrays.asList(
            Pattern.compile("(?i).*(tax|invoice|receipt|bank|statement|budget|expense|income|salary|payment).*"),
            Pattern.compile("(?i).*(financial|accounting|money|dollar|euro|currency|profit|loss).*")
        ));
        
        // Work patterns
        CONTENT_PATTERNS.put(FileCategory.WORK, Arrays.asList(
            Pattern.compile("(?i).*(meeting|project|proposal|contract|report|presentation|memo).*"),
            Pattern.compile("(?i).*(business|corporate|company|client|deadline|schedule).*")
        ));
        
        // Personal patterns
        CONTENT_PATTERNS.put(FileCategory.PERSONAL, Arrays.asList(
            Pattern.compile("(?i).*(family|personal|private|diary|journal|photo|vacation|holiday).*"),
            Pattern.compile("(?i).*(birthday|anniversary|wedding|graduation|celebration).*")
        ));
        
        // Medical patterns
        CONTENT_PATTERNS.put(FileCategory.MEDICAL, Arrays.asList(
            Pattern.compile("(?i).*(medical|health|doctor|hospital|prescription|treatment|diagnosis).*"),
            Pattern.compile("(?i).*(insurance|claim|patient|clinic|surgery|medication).*")
        ));
        
        // Legal patterns
        CONTENT_PATTERNS.put(FileCategory.LEGAL, Arrays.asList(
            Pattern.compile("(?i).*(legal|law|court|attorney|lawyer|contract|agreement|will).*"),
            Pattern.compile("(?i).*(lawsuit|litigation|settlement|judgment|evidence|testimony).*")
        ));
        
        // Education patterns
        CONTENT_PATTERNS.put(FileCategory.EDUCATION, Arrays.asList(
            Pattern.compile("(?i).*(school|university|college|course|assignment|homework|exam|grade).*"),
            Pattern.compile("(?i).*(student|teacher|professor|lecture|study|research|thesis).*")
        ));
    }
    
    /**
     * Automatically categorize a file based on extension and filename
     */
    public FileCategory categorizeFile(VaultFile file) {
        String extension = file.getExtension().toLowerCase();
        String filename = file.getOriginalName().toLowerCase();
        
        // First, try extension-based categorization
        for (FileCategory category : FileCategory.values()) {
            if (category.getExtensions().contains(extension)) {
                return category;
            }
        }
        
        // Then, try content-based categorization using filename
        for (Map.Entry<FileCategory, List<Pattern>> entry : CONTENT_PATTERNS.entrySet()) {
            for (Pattern pattern : entry.getValue()) {
                if (pattern.matcher(filename).matches()) {
                    return entry.getKey();
                }
            }
        }
        
        return FileCategory.OTHER;
    }
    
    /**
     * Group files by category
     */
    public Map<FileCategory, List<VaultFile>> groupFilesByCategory(List<VaultFile> files) {
        return files.stream()
            .collect(Collectors.groupingBy(this::categorizeFile));
    }
    
    /**
     * Smart search with natural language processing
     */
    public List<VaultFile> smartSearch(List<VaultFile> files, String query) {
        if (query == null || query.trim().isEmpty()) {
            return files;
        }
        
        String lowerQuery = query.toLowerCase().trim();
        
        // Handle natural language queries
        if (lowerQuery.startsWith("find") || lowerQuery.startsWith("show me") || lowerQuery.startsWith("search for")) {
            lowerQuery = extractSearchTerms(lowerQuery);
        }
        
        // Handle category-based searches
        FileCategory categoryMatch = matchCategory(lowerQuery);
        if (categoryMatch != null) {
            return files.stream()
                .filter(file -> categorizeFile(file) == categoryMatch)
                .collect(Collectors.toList());
        }
        
        // Handle time-based searches
        if (lowerQuery.contains("today") || lowerQuery.contains("recent")) {
            long oneDayAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000);
            return files.stream()
                .filter(file -> file.getUploadTime() > oneDayAgo)
                .collect(Collectors.toList());
        }
        
        if (lowerQuery.contains("this week")) {
            long oneWeekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000);
            return files.stream()
                .filter(file -> file.getUploadTime() > oneWeekAgo)
                .collect(Collectors.toList());
        }
        
        // Handle size-based searches
        if (lowerQuery.contains("large") || lowerQuery.contains("big")) {
            return files.stream()
                .filter(file -> file.getSize() > 10 * 1024 * 1024) // > 10MB
                .collect(Collectors.toList());
        }
        
        if (lowerQuery.contains("small")) {
            return files.stream()
                .filter(file -> file.getSize() < 1024 * 1024) // < 1MB
                .collect(Collectors.toList());
        }
        
        // Standard text search with fuzzy matching
        final String finalQuery = lowerQuery;
        return files.stream()
            .filter(file -> fuzzyMatch(file, finalQuery))
            .sorted((f1, f2) -> Integer.compare(
                calculateRelevanceScore(f2, finalQuery),
                calculateRelevanceScore(f1, finalQuery)
            ))
            .collect(Collectors.toList());
    }
    
    /**
     * Extract search terms from natural language query
     */
    private String extractSearchTerms(String query) {
        // Remove common natural language prefixes
        return query.replaceAll("(?i)^(find|show me|search for|look for|get me)\\s+", "")
                   .replaceAll("(?i)\\s+(files?|documents?)\\s*$", "")
                   .trim();
    }
    
    /**
     * Match query to file category
     */
    private FileCategory matchCategory(String query) {
        for (FileCategory category : FileCategory.values()) {
            if (query.contains(category.getDisplayName().toLowerCase()) ||
                query.contains(category.name().toLowerCase())) {
                return category;
            }
        }
        
        // Handle synonyms
        if (query.contains("picture") || query.contains("photo")) return FileCategory.IMAGES;
        if (query.contains("movie") || query.contains("film")) return FileCategory.VIDEOS;
        if (query.contains("music") || query.contains("song")) return FileCategory.AUDIO;
        if (query.contains("zip") || query.contains("compressed")) return FileCategory.ARCHIVES;
        if (query.contains("program") || query.contains("source")) return FileCategory.CODE;
        if (query.contains("money") || query.contains("finance")) return FileCategory.FINANCIAL;
        if (query.contains("office") || query.contains("business")) return FileCategory.WORK;
        
        return null;
    }
    
    /**
     * Fuzzy matching for file search
     */
    private boolean fuzzyMatch(VaultFile file, String query) {
        String filename = file.getOriginalName().toLowerCase();
        String[] queryTerms = query.split("\\s+");
        
        // Check if all query terms are present (in any order)
        for (String term : queryTerms) {
            if (!filename.contains(term) && !file.getTags().toLowerCase().contains(term)) {
                // Try partial matching
                if (!hasPartialMatch(filename, term) && !hasPartialMatch(file.getTags().toLowerCase(), term)) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    /**
     * Check for partial string matching
     */
    private boolean hasPartialMatch(String text, String term) {
        if (term.length() < 3) return false;
        
        // Check for substring matches
        for (int i = 0; i <= text.length() - term.length(); i++) {
            int matches = 0;
            for (int j = 0; j < term.length() && i + j < text.length(); j++) {
                if (text.charAt(i + j) == term.charAt(j)) {
                    matches++;
                }
            }
            if (matches >= term.length() * 0.8) { // 80% character match
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Calculate relevance score for search results
     */
    private int calculateRelevanceScore(VaultFile file, String query) {
        int score = 0;
        String filename = file.getOriginalName().toLowerCase();
        String[] queryTerms = query.split("\\s+");
        
        for (String term : queryTerms) {
            // Exact filename match
            if (filename.equals(term)) score += 100;
            
            // Filename starts with term
            else if (filename.startsWith(term)) score += 50;
            
            // Filename contains term
            else if (filename.contains(term)) score += 25;
            
            // Tags contain term
            else if (file.getTags().toLowerCase().contains(term)) score += 15;
            
            // Extension match
            else if (file.getExtension().toLowerCase().equals(term)) score += 10;
        }
        
        // Boost score for recent files
        long daysSinceUpload = (System.currentTimeMillis() - file.getUploadTime()) / (24 * 60 * 60 * 1000);
        if (daysSinceUpload < 7) score += 5;
        
        return score;
    }
    
    /**
     * Detect duplicate files
     */
    public Map<String, List<VaultFile>> findDuplicates(List<VaultFile> files) {
        Map<String, List<VaultFile>> duplicates = new HashMap<>();
        
        // Group by hash (exact duplicates)
        Map<String, List<VaultFile>> hashGroups = files.stream()
            .collect(Collectors.groupingBy(VaultFile::getHash));
        
        // Find groups with more than one file
        hashGroups.entrySet().stream()
            .filter(entry -> entry.getValue().size() > 1)
            .forEach(entry -> duplicates.put("Exact: " + entry.getKey(), entry.getValue()));
        
        // Group by similar names (potential duplicates)
        Map<String, List<VaultFile>> nameGroups = new HashMap<>();
        for (VaultFile file : files) {
            String baseName = getBaseName(file.getOriginalName());
            nameGroups.computeIfAbsent(baseName, k -> new ArrayList<>()).add(file);
        }
        
        nameGroups.entrySet().stream()
            .filter(entry -> entry.getValue().size() > 1)
            .forEach(entry -> duplicates.put("Similar: " + entry.getKey(), entry.getValue()));
        
        return duplicates;
    }
    
    /**
     * Get base name without version numbers or common suffixes
     */
    private String getBaseName(String filename) {
        // Remove extension
        int lastDot = filename.lastIndexOf('.');
        String nameWithoutExt = lastDot > 0 ? filename.substring(0, lastDot) : filename;
        
        // Remove common version patterns
        return nameWithoutExt
            .replaceAll("(?i)\\s*[-_]?\\s*(copy|duplicate|backup|v\\d+|version\\s*\\d+|\\(\\d+\\))\\s*$", "")
            .replaceAll("(?i)\\s*[-_]?\\s*(final|draft|temp|temporary|old|new)\\s*$", "")
            .trim()
            .toLowerCase();
    }
    
    /**
     * Suggest file organization improvements
     */
    public List<String> getOrganizationSuggestions(List<VaultFile> files) {
        List<String> suggestions = new ArrayList<>();
        
        Map<FileCategory, List<VaultFile>> categories = groupFilesByCategory(files);
        
        // Suggest creating folders for large categories
        categories.entrySet().stream()
            .filter(entry -> entry.getValue().size() >= 5)
            .forEach(entry -> suggestions.add(
                "Consider organizing " + entry.getValue().size() + 
                " " + entry.getKey().getDisplayName().toLowerCase() + 
                " into a dedicated folder"));
        
        // Suggest tagging untagged files
        long untaggedCount = files.stream()
            .filter(file -> file.getTags().isEmpty())
            .count();
        
        if (untaggedCount > 0) {
            suggestions.add("Add tags to " + untaggedCount + " untagged files for better organization");
        }
        
        // Suggest duplicate cleanup
        Map<String, List<VaultFile>> duplicates = findDuplicates(files);
        if (!duplicates.isEmpty()) {
            suggestions.add("Found " + duplicates.size() + " groups of potential duplicate files");
        }
        
        // Suggest archiving old files
        long oldFilesCount = files.stream()
            .filter(file -> {
                long daysSinceUpload = (System.currentTimeMillis() - file.getUploadTime()) / (24 * 60 * 60 * 1000);
                return daysSinceUpload > 365; // Older than 1 year
            })
            .count();
        
        if (oldFilesCount > 0) {
            suggestions.add("Consider archiving " + oldFilesCount + " files older than 1 year");
        }
        
        return suggestions;
    }
    
    /**
     * Get file statistics
     */
    public Map<String, Object> getFileStatistics(List<VaultFile> files) {
        Map<String, Object> stats = new HashMap<>();
        
        // Basic counts
        stats.put("totalFiles", files.size());
        stats.put("totalSize", files.stream().mapToLong(VaultFile::getSize).sum());
        
        // Category distribution
        Map<FileCategory, Long> categoryStats = files.stream()
            .collect(Collectors.groupingBy(this::categorizeFile, Collectors.counting()));
        stats.put("categoryDistribution", categoryStats);
        
        // Extension distribution
        Map<String, Long> extensionStats = files.stream()
            .collect(Collectors.groupingBy(VaultFile::getExtension, Collectors.counting()));
        stats.put("extensionDistribution", extensionStats);
        
        // Size distribution
        long smallFiles = files.stream().filter(f -> f.getSize() < 1024 * 1024).count(); // < 1MB
        long mediumFiles = files.stream().filter(f -> f.getSize() >= 1024 * 1024 && f.getSize() < 10 * 1024 * 1024).count(); // 1-10MB
        long largeFiles = files.stream().filter(f -> f.getSize() >= 10 * 1024 * 1024).count(); // > 10MB
        
        Map<String, Long> sizeStats = new HashMap<>();
        sizeStats.put("small", smallFiles);
        sizeStats.put("medium", mediumFiles);
        sizeStats.put("large", largeFiles);
        stats.put("sizeDistribution", sizeStats);
        
        return stats;
    }
}
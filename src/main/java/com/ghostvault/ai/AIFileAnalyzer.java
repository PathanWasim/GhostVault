package com.ghostvault.ai;

import com.ghostvault.model.VaultFile;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * AI-powered file analysis engine for categorization and security assessment
 */
public class AIFileAnalyzer {
    
    /**
     * Analyze a file and return comprehensive analysis results
     */
    public FileAnalysisResult analyzeFile(VaultFile file) {
        String extension = file.getExtension().toLowerCase();
        long size = file.getSize();
        
        FileCategory category = categorizeByExtension(extension);
        SizeCategory sizeCategory = analyzeSizeThreshold(size);
        List<FileFlag> flags = generateFileFlags(file);
        SecurityRisk riskLevel = assessSecurityRisk(file);
        
        return new FileAnalysisResult(category, sizeCategory, flags, riskLevel);
    }
    
    /**
     * Categorize file by extension
     */
    public FileCategory categorizeByExtension(String extension) {
        switch (extension.toLowerCase()) {
            case "pdf": case "doc": case "docx": case "txt": case "rtf": case "odt":
                return FileCategory.DOCUMENT;
            case "jpg": case "jpeg": case "png": case "gif": case "bmp": case "svg": case "webp":
                return FileCategory.IMAGE;
            case "mp4": case "avi": case "mkv": case "mov": case "wmv": case "flv": case "webm":
                return FileCategory.VIDEO;
            case "mp3": case "wav": case "flac": case "aac": case "ogg": case "m4a":
                return FileCategory.AUDIO;
            case "zip": case "rar": case "7z": case "tar": case "gz": case "bz2":
                return FileCategory.ARCHIVE;
            case "exe": case "msi": case "dmg": case "deb": case "rpm": case "app":
                return FileCategory.EXECUTABLE;
            case "java": case "py": case "js": case "html": case "css": case "cpp": case "c": case "php": case "rb":
                return FileCategory.CODE;
            default:
                return FileCategory.OTHER;
        }
    }
    
    /**
     * Analyze file size and categorize
     */
    public SizeCategory analyzeSizeThreshold(long fileSize) {
        if (fileSize < SizeCategory.SMALL.getMaxSize()) {
            return SizeCategory.SMALL;
        } else if (fileSize < SizeCategory.MEDIUM.getMaxSize()) {
            return SizeCategory.MEDIUM;
        } else {
            return SizeCategory.LARGE;
        }
    }
    
    /**
     * Assess security risk level for a file
     */
    public SecurityRisk assessSecurityRisk(VaultFile file) {
        String extension = file.getExtension().toLowerCase();
        long size = file.getSize();
        
        // High risk files
        if (isExecutableFile(extension)) {
            return SecurityRisk.HIGH;
        }
        
        // Medium risk files
        if (isArchiveFile(extension) || size > 100 * 1024 * 1024) { // > 100MB
            return SecurityRisk.MEDIUM;
        }
        
        // Low risk files
        if (isDocumentFile(extension) && size > 10 * 1024 * 1024) { // Large documents
            return SecurityRisk.LOW;
        }
        
        // Safe files
        return SecurityRisk.SAFE;
    }
    
    /**
     * Generate flags for a file based on various criteria
     */
    private List<FileFlag> generateFileFlags(VaultFile file) {
        List<FileFlag> flags = new ArrayList<>();
        String extension = file.getExtension().toLowerCase();
        String fileName = file.getOriginalName().toLowerCase();
        long size = file.getSize();
        
        // Flag suspicious extensions
        if (isSuspiciousExtension(extension)) {
            flags.add(FileFlag.SUSPICIOUS_EXTENSION);
        }
        
        // Flag large files (different thresholds for different types)
        if (isLargeForType(extension, size)) {
            flags.add(FileFlag.LARGE_SIZE);
        }
        
        // Flag executable files
        if (isExecutableFile(extension)) {
            flags.add(FileFlag.EXECUTABLE_FILE);
        }
        
        // Flag encrypted archives
        if (isArchiveFile(extension)) {
            flags.add(FileFlag.ENCRYPTED_ARCHIVE);
        }
        
        // Flag system files
        if (isSystemFile(extension)) {
            flags.add(FileFlag.SYSTEM_FILE);
        }
        
        // Flag potential malware based on name patterns
        if (isPotentialMalware(fileName, extension)) {
            flags.add(FileFlag.POTENTIAL_MALWARE);
        }
        
        return flags;
    }
    
    /**
     * Enhanced security analysis with multiple factors
     */
    public SecurityAnalysis performSecurityAnalysis(VaultFile file) {
        String extension = file.getExtension().toLowerCase();
        String fileName = file.getOriginalName().toLowerCase();
        long size = file.getSize();
        
        SecurityAnalysis analysis = new SecurityAnalysis();
        
        // Analyze file extension risk
        analysis.addFactor("Extension Risk", assessExtensionRisk(extension));
        
        // Analyze file size anomalies
        analysis.addFactor("Size Anomaly", assessSizeAnomaly(extension, size));
        
        // Analyze filename patterns
        analysis.addFactor("Filename Pattern", assessFilenamePattern(fileName));
        
        // Calculate overall risk score
        analysis.calculateOverallRisk();
        
        return analysis;
    }
    
    // Helper methods for file type detection
    
    private boolean isExecutableFile(String extension) {
        return extension.matches("exe|msi|bat|cmd|scr|com|pif|app|dmg|deb|rpm");
    }
    
    private boolean isArchiveFile(String extension) {
        return extension.matches("zip|rar|7z|tar|gz|bz2|xz");
    }
    
    private boolean isDocumentFile(String extension) {
        return extension.matches("pdf|doc|docx|txt|rtf|odt");
    }
    
    private boolean isSuspiciousExtension(String extension) {
        return extension.matches("exe|bat|cmd|scr|com|pif|vbs|js|jar");
    }
    
    private boolean isSystemFile(String extension) {
        return extension.matches("sys|dll|ini|cfg|conf|log");
    }
    
    private boolean isLargeForType(String extension, long size) {
        // Different size thresholds for different file types
        switch (categorizeByExtension(extension)) {
            case IMAGE:
                return size > 10 * 1024 * 1024; // > 10MB for images
            case AUDIO:
                return size > 50 * 1024 * 1024; // > 50MB for audio
            case VIDEO:
                return size > 500 * 1024 * 1024; // > 500MB for video
            case DOCUMENT:
                return size > 5 * 1024 * 1024; // > 5MB for documents
            default:
                return size > 50 * 1024 * 1024; // > 50MB for others
        }
    }
    
    private boolean isPotentialMalware(String fileName, String extension) {
        // Check for suspicious filename patterns
        String[] suspiciousPatterns = {
            "virus", "trojan", "malware", "keylog", "backdoor", 
            "crack", "keygen", "patch", "loader", "inject"
        };
        
        for (String pattern : suspiciousPatterns) {
            if (fileName.contains(pattern)) {
                return true;
            }
        }
        
        // Check for double extensions (e.g., file.txt.exe)
        if (fileName.matches(".*\\.[a-z]{2,4}\\.(exe|scr|bat|cmd)$")) {
            return true;
        }
        
        return false;
    }
    
    private int assessExtensionRisk(String extension) {
        if (extension.matches("exe|bat|cmd|scr|com|pif")) return 90;
        if (extension.matches("msi|jar|app|dmg")) return 70;
        if (extension.matches("zip|rar|7z")) return 50;
        if (extension.matches("pdf|doc|docx")) return 30;
        return 10;
    }
    
    private int assessSizeAnomaly(String extension, long size) {
        if (isLargeForType(extension, size)) return 60;
        if (size < 100) return 40; // Suspiciously small files
        return 10;
    }
    
    private int assessFilenamePattern(String fileName) {
        if (isPotentialMalware(fileName, "")) return 80;
        if (fileName.matches(".*[0-9]{8,}.*")) return 30; // Random numbers
        if (fileName.length() > 100) return 40; // Very long names
        return 10;
    }
}
package com.ghostvault.ai;

import java.util.List;

/**
 * Result of AI file analysis containing all analysis data
 */
public class FileAnalysisResult {
    private final FileCategory category;
    private final SizeCategory sizeCategory;
    private final List<FileFlag> flags;
    private final SecurityRisk riskLevel;
    
    public FileAnalysisResult(FileCategory category, SizeCategory sizeCategory, 
                             List<FileFlag> flags, SecurityRisk riskLevel) {
        this.category = category;
        this.sizeCategory = sizeCategory;
        this.flags = flags;
        this.riskLevel = riskLevel;
    }
    
    public FileCategory getCategory() {
        return category;
    }
    
    public SizeCategory getSizeCategory() {
        return sizeCategory;
    }
    
    public List<FileFlag> getFlags() {
        return flags;
    }
    
    public SecurityRisk getRiskLevel() {
        return riskLevel;
    }
    
    public boolean hasFlag(FileFlag flag) {
        return flags.contains(flag);
    }
    
    public boolean hasAnyFlag() {
        return !flags.isEmpty();
    }
    
    public String getDisplayString() {
        StringBuilder display = new StringBuilder();
        display.append(category.toString()).append(" | ");
        display.append(sizeCategory.getDisplayName()).append(" | ");
        display.append(riskLevel.toString());
        
        if (hasAnyFlag()) {
            display.append(" | Flags: ");
            for (int i = 0; i < flags.size(); i++) {
                if (i > 0) display.append(", ");
                display.append(flags.get(i).getIcon());
            }
        }
        
        return display.toString();
    }
    
    @Override
    public String toString() {
        return getDisplayString();
    }
}
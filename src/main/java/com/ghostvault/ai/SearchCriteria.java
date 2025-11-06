package com.ghostvault.ai;

/**
 * Search criteria for advanced file searching
 */
public class SearchCriteria {
    private String nameQuery;
    private String extension;
    private FileCategory category;
    private SecurityRisk securityRisk;
    private SizeCategory sizeCategory;
    private long minSize = 0;
    private long maxSize = Long.MAX_VALUE;
    
    public SearchCriteria() {
    }
    
    public SearchCriteria(String nameQuery) {
        this.nameQuery = nameQuery;
    }
    
    // Getters and setters
    public String getNameQuery() {
        return nameQuery;
    }
    
    public void setNameQuery(String nameQuery) {
        this.nameQuery = nameQuery;
    }
    
    public String getExtension() {
        return extension;
    }
    
    public void setExtension(String extension) {
        this.extension = extension;
    }
    
    public FileCategory getCategory() {
        return category;
    }
    
    public void setCategory(FileCategory category) {
        this.category = category;
    }
    
    public SecurityRisk getSecurityRisk() {
        return securityRisk;
    }
    
    public void setSecurityRisk(SecurityRisk securityRisk) {
        this.securityRisk = securityRisk;
    }
    
    public SizeCategory getSizeCategory() {
        return sizeCategory;
    }
    
    public void setSizeCategory(SizeCategory sizeCategory) {
        this.sizeCategory = sizeCategory;
    }
    
    public long getMinSize() {
        return minSize;
    }
    
    public void setMinSize(long minSize) {
        this.minSize = minSize;
    }
    
    public long getMaxSize() {
        return maxSize;
    }
    
    public void setMaxSize(long maxSize) {
        this.maxSize = maxSize;
    }
    
    // Builder pattern methods
    public SearchCriteria withNameQuery(String nameQuery) {
        this.nameQuery = nameQuery;
        return this;
    }
    
    public SearchCriteria withExtension(String extension) {
        this.extension = extension;
        return this;
    }
    
    public SearchCriteria withCategory(FileCategory category) {
        this.category = category;
        return this;
    }
    
    public SearchCriteria withSecurityRisk(SecurityRisk securityRisk) {
        this.securityRisk = securityRisk;
        return this;
    }
    
    public SearchCriteria withSizeCategory(SizeCategory sizeCategory) {
        this.sizeCategory = sizeCategory;
        return this;
    }
    
    public SearchCriteria withSizeRange(long minSize, long maxSize) {
        this.minSize = minSize;
        this.maxSize = maxSize;
        return this;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("SearchCriteria{");
        if (nameQuery != null) sb.append("name='").append(nameQuery).append("', ");
        if (extension != null) sb.append("ext='").append(extension).append("', ");
        if (category != null) sb.append("category=").append(category).append(", ");
        if (securityRisk != null) sb.append("risk=").append(securityRisk).append(", ");
        if (sizeCategory != null) sb.append("size=").append(sizeCategory).append(", ");
        if (minSize > 0 || maxSize < Long.MAX_VALUE) {
            sb.append("sizeRange=[").append(minSize).append("-").append(maxSize).append("], ");
        }
        sb.append("}");
        return sb.toString();
    }
}
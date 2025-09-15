package com.ghostvault.audit;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Container for audit entry details and additional information
 */
public class AuditDetails implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final Map<String, String> details;
    
    public AuditDetails() {
        this.details = new LinkedHashMap<>();
    }
    
    /**
     * Add a detail key-value pair
     */
    public void addDetail(String key, String value) {
        if (key != null && value != null) {
            details.put(key, value);
        }
    }
    
    /**
     * Get detail value by key
     */
    public String getDetail(String key) {
        return details.get(key);
    }
    
    /**
     * Check if detail exists
     */
    public boolean hasDetail(String key) {
        return details.containsKey(key);
    }
    
    /**
     * Get all detail keys
     */
    public Set<String> getKeys() {
        return details.keySet();
    }
    
    /**
     * Get all details as map
     */
    public Map<String, String> getAllDetails() {
        return new LinkedHashMap<>(details);
    }
    
    /**
     * Get entry set for iteration
     */
    public Set<Map.Entry<String, String>> entrySet() {
        return details.entrySet();
    }
    
    /**
     * Check if details are empty
     */
    public boolean isEmpty() {
        return details.isEmpty();
    }
    
    /**
     * Get number of details
     */
    public int size() {
        return details.size();
    }
    
    /**
     * Clear all details
     */
    public void clear() {
        details.clear();
    }
    
    /**
     * Remove a detail
     */
    public String removeDetail(String key) {
        return details.remove(key);
    }
    
    /**
     * Add multiple details from another AuditDetails
     */
    public void addAll(AuditDetails other) {
        if (other != null) {
            details.putAll(other.details);
        }
    }
    
    /**
     * Add multiple details from a map
     */
    public void addAll(Map<String, String> detailsMap) {
        if (detailsMap != null) {
            details.putAll(detailsMap);
        }
    }
    
    /**
     * Get formatted details string
     */
    public String getFormattedDetails() {
        if (details.isEmpty()) {
            return "";
        }
        
        StringBuilder formatted = new StringBuilder();
        boolean first = true;
        
        for (Map.Entry<String, String> entry : details.entrySet()) {
            if (!first) {
                formatted.append(", ");
            }
            formatted.append(entry.getKey()).append("=").append(entry.getValue());
            first = false;
        }
        
        return formatted.toString();
    }
    
    /**
     * Get details as JSON-like string
     */
    public String toJsonString() {
        if (details.isEmpty()) {
            return "{}";
        }
        
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        
        for (Map.Entry<String, String> entry : details.entrySet()) {
            if (!first) {
                json.append(", ");
            }
            json.append("\"").append(escapeJson(entry.getKey())).append("\": \"")
                .append(escapeJson(entry.getValue())).append("\"");
            first = false;
        }
        
        json.append("}");
        return json.toString();
    }
    
    /**
     * Create AuditDetails from key-value pairs
     */
    public static AuditDetails of(String... keyValuePairs) {
        AuditDetails details = new AuditDetails();
        
        if (keyValuePairs.length % 2 != 0) {
            throw new IllegalArgumentException("Key-value pairs must be even number of arguments");
        }
        
        for (int i = 0; i < keyValuePairs.length; i += 2) {
            details.addDetail(keyValuePairs[i], keyValuePairs[i + 1]);
        }
        
        return details;
    }
    
    /**
     * Create AuditDetails from map
     */
    public static AuditDetails fromMap(Map<String, String> map) {
        AuditDetails details = new AuditDetails();
        if (map != null) {
            details.addAll(map);
        }
        return details;
    }
    
    /**
     * Escape JSON special characters
     */
    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        
        return value.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
    
    @Override
    public String toString() {
        return getFormattedDetails();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        AuditDetails that = (AuditDetails) obj;
        return details.equals(that.details);
    }
    
    @Override
    public int hashCode() {
        return details.hashCode();
    }
}
package com.ghostvault.ai;

import java.util.HashMap;
import java.util.Map;

/**
 * Detailed security analysis result for files
 */
public class SecurityAnalysis {
    private final Map<String, Integer> riskFactors = new HashMap<>();
    private int overallRiskScore = 0;
    private SecurityRisk riskLevel = SecurityRisk.SAFE;
    
    public void addFactor(String factorName, int riskScore) {
        riskFactors.put(factorName, riskScore);
    }
    
    public void calculateOverallRisk() {
        if (riskFactors.isEmpty()) {
            overallRiskScore = 0;
            riskLevel = SecurityRisk.SAFE;
            return;
        }
        
        // Calculate weighted average of risk factors
        int totalScore = riskFactors.values().stream().mapToInt(Integer::intValue).sum();
        overallRiskScore = totalScore / riskFactors.size();
        
        // Determine risk level based on score
        if (overallRiskScore >= 80) {
            riskLevel = SecurityRisk.CRITICAL;
        } else if (overallRiskScore >= 60) {
            riskLevel = SecurityRisk.HIGH;
        } else if (overallRiskScore >= 40) {
            riskLevel = SecurityRisk.MEDIUM;
        } else if (overallRiskScore >= 20) {
            riskLevel = SecurityRisk.LOW;
        } else {
            riskLevel = SecurityRisk.SAFE;
        }
    }
    
    public Map<String, Integer> getRiskFactors() {
        return new HashMap<>(riskFactors);
    }
    
    public int getOverallRiskScore() {
        return overallRiskScore;
    }
    
    public SecurityRisk getRiskLevel() {
        return riskLevel;
    }
    
    public String getDetailedReport() {
        StringBuilder report = new StringBuilder();
        report.append("Security Analysis Report\n");
        report.append("Overall Risk: ").append(riskLevel.toString()).append(" (").append(overallRiskScore).append("/100)\n\n");
        
        report.append("Risk Factors:\n");
        riskFactors.forEach((factor, score) -> 
            report.append("• ").append(factor).append(": ").append(score).append("/100\n"));
        
        return report.toString();
    }
    
    @Override
    public String toString() {
        return riskLevel.toString() + " (" + overallRiskScore + "/100)";
    }
}
package com.ghostvault.security;

import com.ghostvault.audit.AuditManager;
import com.ghostvault.config.AppConfig;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Advanced threat detection engine for GhostVault
 * Implements behavioral analysis and anomaly detection
 */
public class ThreatDetectionEngine {
    
    private final AuditManager auditManager;
    private final Map<String, ThreatIndicator> threatIndicators;
    private final Map<String, AtomicInteger> eventCounts;
    private final Map<String, LocalDateTime> lastEventTimes;
    private final AtomicLong totalEvents;
    private boolean monitoringActive;
    
    // Threat detection thresholds
    private static final int MAX_FAILED_LOGINS_PER_MINUTE = 5;
    private static final int MAX_FILE_OPERATIONS_PER_MINUTE = 100;
    private static final int MAX_MEMORY_USAGE_PERCENT = 90;
    private static final int MAX_CPU_USAGE_PERCENT = 95;
    private static final long MAX_SESSION_DURATION_HOURS = 12;
    
    public enum ThreatLevel {
        LOW(1, "Low risk - monitoring"),
        MEDIUM(2, "Medium risk - increased vigilance"),
        HIGH(3, "High risk - security measures activated"),
        CRITICAL(4, "Critical risk - immediate response required");
        
        private final int level;
        private final String description;
        
        ThreatLevel(int level, String description) {
            this.level = level;
            this.description = description;
        }
        
        public int getLevel() { return level; }
        public String getDescription() { return description; }
    }
    
    public enum ThreatType {
        BRUTE_FORCE_ATTACK("Brute force login attempts detected"),
        EXCESSIVE_FILE_ACCESS("Unusual file access patterns detected"),
        MEMORY_EXHAUSTION("Memory exhaustion attack detected"),
        CPU_EXHAUSTION("CPU exhaustion attack detected"),
        SESSION_HIJACKING("Potential session hijacking detected"),
        FILE_SYSTEM_TAMPERING("File system tampering detected"),
        PROCESS_INJECTION("Process injection attempt detected"),
        DEBUGGER_ATTACHMENT("Debugger attachment detected"),
        NETWORK_INTRUSION("Network intrusion attempt detected"),
        DATA_EXFILTRATION("Potential data exfiltration detected");
        
        private final String description;
        
        ThreatType(String description) {
            this.description = description;
        }
        
        public String getDescription() { return description; }
    }
    
    public ThreatDetectionEngine(AuditManager auditManager) {
        this.auditManager = auditManager;
        this.threatIndicators = new ConcurrentHashMap<>();
        this.eventCounts = new ConcurrentHashMap<>();
        this.lastEventTimes = new ConcurrentHashMap<>();
        this.totalEvents = new AtomicLong(0);
        this.monitoringActive = false;
        
        initializeThreatIndicators();
    }
    
    /**
     * Initialize threat indicators
     */
    private void initializeThreatIndicators() {
        // Initialize all threat types with LOW level
        for (ThreatType type : ThreatType.values()) {
            threatIndicators.put(type.name(), new ThreatIndicator(type, ThreatLevel.LOW));
        }
    }
    
    /**
     * Start threat detection monitoring
     */
    public void startMonitoring() {
        if (monitoringActive) {
            return;
        }
        
        monitoringActive = true;
        
        // Start monitoring threads
        startSystemMonitoring();
        startBehavioralAnalysis();
        startAnomalyDetection();
        
        if (auditManager != null) {
            auditManager.logSecurityEvent("THREAT_DETECTION_STARTED", 
                "Threat detection engine activated", AuditManager.AuditSeverity.INFO, 
                null, "All monitoring systems online");
        }
        
        System.out.println("🛡️ Threat detection engine started");
    }
    
    /**
     * Stop threat detection monitoring
     */
    public void stopMonitoring() {
        monitoringActive = false;
        
        if (auditManager != null) {
            auditManager.logSecurityEvent("THREAT_DETECTION_STOPPED", 
                "Threat detection engine deactivated", AuditManager.AuditSeverity.INFO, 
                null, "Monitoring systems offline");
        }
        
        System.out.println("🛡️ Threat detection engine stopped");
    }
    
    /**
     * Record security event for analysis
     */
    public void recordSecurityEvent(String eventType, String source, Map<String, String> metadata) {
        if (!monitoringActive) {
            return;
        }
        
        totalEvents.incrementAndGet();
        eventCounts.computeIfAbsent(eventType, k -> new AtomicInteger(0)).incrementAndGet();
        lastEventTimes.put(eventType, LocalDateTime.now());
        
        // Analyze the event for threats
        analyzeSecurityEvent(eventType, source, metadata);
    }
    
    /**
     * Analyze security event for threat indicators
     */
    private void analyzeSecurityEvent(String eventType, String source, Map<String, String> metadata) {
        switch (eventType) {
            case "LOGIN_FAILED":
                analyzeFailedLogin(source, metadata);
                break;
            case "FILE_ACCESS":
                analyzeFileAccess(source, metadata);
                break;
            case "MEMORY_USAGE":
                analyzeMemoryUsage(metadata);
                break;
            case "CPU_USAGE":
                analyzeCpuUsage(metadata);
                break;
            case "SESSION_ACTIVITY":
                analyzeSessionActivity(source, metadata);
                break;
            case "FILE_MODIFIED":
                analyzeFileModification(source, metadata);
                break;
            case "PROCESS_CREATED":
                analyzeProcessCreation(metadata);
                break;
            case "NETWORK_CONNECTION":
                analyzeNetworkConnection(source, metadata);
                break;
        }
    }
    
    /**
     * Analyze failed login attempts
     */
    private void analyzeFailedLogin(String source, Map<String, String> metadata) {
        String key = "failed_login_" + source;
        AtomicInteger count = eventCounts.computeIfAbsent(key, k -> new AtomicInteger(0));
        count.incrementAndGet();
        
        // Check for brute force attack
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneMinuteAgo = now.minus(1, ChronoUnit.MINUTES);
        
        // Count recent failed attempts
        int recentAttempts = countRecentEvents(key, oneMinuteAgo);
        
        if (recentAttempts >= MAX_FAILED_LOGINS_PER_MINUTE) {
            ThreatLevel level = recentAttempts >= MAX_FAILED_LOGINS_PER_MINUTE * 2 ? 
                ThreatLevel.CRITICAL : ThreatLevel.HIGH;
            
            updateThreatLevel(ThreatType.BRUTE_FORCE_ATTACK, level);
            
            if (auditManager != null) {
                auditManager.logSecurityEvent("BRUTE_FORCE_DETECTED", 
                    "Brute force attack detected from " + source, 
                    AuditManager.AuditSeverity.CRITICAL, source, 
                    "Failed attempts: " + recentAttempts);
            }
        }
    }
    
    /**
     * Analyze file access patterns
     */
    private void analyzeFileAccess(String source, Map<String, String> metadata) {
        String key = "file_access_" + source;
        AtomicInteger count = eventCounts.computeIfAbsent(key, k -> new AtomicInteger(0));
        count.incrementAndGet();
        
        // Check for excessive file access
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneMinuteAgo = now.minus(1, ChronoUnit.MINUTES);
        
        int recentAccess = countRecentEvents(key, oneMinuteAgo);
        
        if (recentAccess >= MAX_FILE_OPERATIONS_PER_MINUTE) {
            ThreatLevel level = recentAccess >= MAX_FILE_OPERATIONS_PER_MINUTE * 2 ? 
                ThreatLevel.HIGH : ThreatLevel.MEDIUM;
            
            updateThreatLevel(ThreatType.EXCESSIVE_FILE_ACCESS, level);
            
            if (auditManager != null) {
                auditManager.logSecurityEvent("EXCESSIVE_FILE_ACCESS", 
                    "Unusual file access pattern detected", 
                    AuditManager.AuditSeverity.WARNING, source, 
                    "File operations: " + recentAccess);
            }
        }
        
        // Check for sensitive file access
        String fileName = metadata.get("filename");
        if (fileName != null && isSensitiveFile(fileName)) {
            updateThreatLevel(ThreatType.DATA_EXFILTRATION, ThreatLevel.MEDIUM);
            
            if (auditManager != null) {
                auditManager.logSecurityEvent("SENSITIVE_FILE_ACCESS", 
                    "Access to sensitive file detected", 
                    AuditManager.AuditSeverity.WARNING, source, 
                    "File: " + fileName);
            }
        }
    }
    
    /**
     * Analyze memory usage
     */
    private void analyzeMemoryUsage(Map<String, String> metadata) {
        try {
            String usageStr = metadata.get("usage_percent");
            if (usageStr != null) {
                int usage = Integer.parseInt(usageStr);
                
                if (usage >= MAX_MEMORY_USAGE_PERCENT) {
                    ThreatLevel level = usage >= 98 ? ThreatLevel.CRITICAL : ThreatLevel.HIGH;
                    updateThreatLevel(ThreatType.MEMORY_EXHAUSTION, level);
                    
                    if (auditManager != null) {
                        auditManager.logSecurityEvent("HIGH_MEMORY_USAGE", 
                            "High memory usage detected", 
                            AuditManager.AuditSeverity.WARNING, null, 
                            "Usage: " + usage + "%");
                    }
                }
            }
        } catch (NumberFormatException e) {
            // Ignore invalid usage data
        }
    }
    
    /**
     * Analyze CPU usage
     */
    private void analyzeCpuUsage(Map<String, String> metadata) {
        try {
            String usageStr = metadata.get("usage_percent");
            if (usageStr != null) {
                int usage = Integer.parseInt(usageStr);
                
                if (usage >= MAX_CPU_USAGE_PERCENT) {
                    ThreatLevel level = usage >= 99 ? ThreatLevel.CRITICAL : ThreatLevel.HIGH;
                    updateThreatLevel(ThreatType.CPU_EXHAUSTION, level);
                    
                    if (auditManager != null) {
                        auditManager.logSecurityEvent("HIGH_CPU_USAGE", 
                            "High CPU usage detected", 
                            AuditManager.AuditSeverity.WARNING, null, 
                            "Usage: " + usage + "%");
                    }
                }
            }
        } catch (NumberFormatException e) {
            // Ignore invalid usage data
        }
    }
    
    /**
     * Analyze session activity
     */
    private void analyzeSessionActivity(String source, Map<String, String> metadata) {
        String sessionId = metadata.get("session_id");
        String startTimeStr = metadata.get("start_time");
        
        if (sessionId != null && startTimeStr != null) {
            try {
                LocalDateTime startTime = LocalDateTime.parse(startTimeStr);
                LocalDateTime now = LocalDateTime.now();
                
                long hours = ChronoUnit.HOURS.between(startTime, now);
                
                if (hours >= MAX_SESSION_DURATION_HOURS) {
                    ThreatLevel level = hours >= MAX_SESSION_DURATION_HOURS * 2 ? 
                        ThreatLevel.HIGH : ThreatLevel.MEDIUM;
                    
                    updateThreatLevel(ThreatType.SESSION_HIJACKING, level);
                    
                    if (auditManager != null) {
                        auditManager.logSecurityEvent("LONG_SESSION_DETECTED", 
                            "Unusually long session detected", 
                            AuditManager.AuditSeverity.WARNING, source, 
                            "Duration: " + hours + " hours");
                    }
                }
            } catch (Exception e) {
                // Ignore parsing errors
            }
        }
    }
    
    /**
     * Analyze file modification events
     */
    private void analyzeFileModification(String source, Map<String, String> metadata) {
        String fileName = metadata.get("filename");
        String operation = metadata.get("operation");
        
        if (fileName != null && isSystemFile(fileName)) {
            updateThreatLevel(ThreatType.FILE_SYSTEM_TAMPERING, ThreatLevel.HIGH);
            
            if (auditManager != null) {
                auditManager.logSecurityEvent("SYSTEM_FILE_MODIFIED", 
                    "System file modification detected", 
                    AuditManager.AuditSeverity.CRITICAL, source, 
                    "File: " + fileName + ", Operation: " + operation);
            }
        }
    }
    
    /**
     * Analyze process creation events
     */
    private void analyzeProcessCreation(Map<String, String> metadata) {
        String processName = metadata.get("process_name");
        String commandLine = metadata.get("command_line");
        
        if (processName != null && isSuspiciousProcess(processName)) {
            updateThreatLevel(ThreatType.PROCESS_INJECTION, ThreatLevel.HIGH);
            
            if (auditManager != null) {
                auditManager.logSecurityEvent("SUSPICIOUS_PROCESS", 
                    "Suspicious process creation detected", 
                    AuditManager.AuditSeverity.CRITICAL, null, 
                    "Process: " + processName + ", Command: " + commandLine);
            }
        }
    }
    
    /**
     * Analyze network connection events
     */
    private void analyzeNetworkConnection(String source, Map<String, String> metadata) {
        String remoteAddress = metadata.get("remote_address");
        String port = metadata.get("port");
        
        if (remoteAddress != null && isSuspiciousAddress(remoteAddress)) {
            updateThreatLevel(ThreatType.NETWORK_INTRUSION, ThreatLevel.MEDIUM);
            
            if (auditManager != null) {
                auditManager.logSecurityEvent("SUSPICIOUS_CONNECTION", 
                    "Suspicious network connection detected", 
                    AuditManager.AuditSeverity.WARNING, source, 
                    "Remote: " + remoteAddress + ":" + port);
            }
        }
    }
    
    /**
     * Start system monitoring thread
     */
    private void startSystemMonitoring() {
        Thread systemMonitor = new Thread(() -> {
            while (monitoringActive) {
                try {
                    // Monitor system resources
                    monitorSystemResources();
                    
                    // Monitor file system
                    monitorFileSystem();
                    
                    Thread.sleep(10000); // Check every 10 seconds
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    System.err.println("Error in system monitoring: " + e.getMessage());
                }
            }
        });
        
        systemMonitor.setDaemon(true);
        systemMonitor.setName("ThreatDetection-SystemMonitor");
        systemMonitor.start();
    }
    
    /**
     * Start behavioral analysis thread
     */
    private void startBehavioralAnalysis() {
        Thread behaviorAnalyzer = new Thread(() -> {
            while (monitoringActive) {
                try {
                    // Analyze user behavior patterns
                    analyzeBehaviorPatterns();
                    
                    Thread.sleep(30000); // Analyze every 30 seconds
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    System.err.println("Error in behavioral analysis: " + e.getMessage());
                }
            }
        });
        
        behaviorAnalyzer.setDaemon(true);
        behaviorAnalyzer.setName("ThreatDetection-BehaviorAnalyzer");
        behaviorAnalyzer.start();
    }
    
    /**
     * Start anomaly detection thread
     */
    private void startAnomalyDetection() {
        Thread anomalyDetector = new Thread(() -> {
            while (monitoringActive) {
                try {
                    // Detect anomalies in system behavior
                    detectAnomalies();
                    
                    Thread.sleep(60000); // Check every minute
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    System.err.println("Error in anomaly detection: " + e.getMessage());
                }
            }
        });
        
        anomalyDetector.setDaemon(true);
        anomalyDetector.setName("ThreatDetection-AnomalyDetector");
        anomalyDetector.start();
    }
    
    /**
     * Monitor system resources
     */
    private void monitorSystemResources() {
        try {
            // Monitor memory usage
            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            int memoryUsage = (int) ((usedMemory * 100) / runtime.maxMemory());
            
            Map<String, String> memoryMetadata = new HashMap<>();
            memoryMetadata.put("usage_percent", String.valueOf(memoryUsage));
            memoryMetadata.put("used_bytes", String.valueOf(usedMemory));
            recordSecurityEvent("MEMORY_USAGE", "system", memoryMetadata);
            
            // Monitor CPU usage
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
                com.sun.management.OperatingSystemMXBean sunOsBean = 
                    (com.sun.management.OperatingSystemMXBean) osBean;
                
                double cpuUsage = sunOsBean.getProcessCpuLoad() * 100;
                
                Map<String, String> cpuMetadata = new HashMap<>();
                cpuMetadata.put("usage_percent", String.valueOf((int) cpuUsage));
                recordSecurityEvent("CPU_USAGE", "system", cpuMetadata);
            }
            
        } catch (Exception e) {
            // Ignore monitoring errors
        }
    }
    
    /**
     * Monitor file system for changes
     */
    private void monitorFileSystem() {
        try {
            Path vaultPath = Paths.get(AppConfig.VAULT_DIR);
            if (Files.exists(vaultPath)) {
                // Check for unexpected files or modifications
                Files.walk(vaultPath)
                    .filter(Files::isRegularFile)
                    .forEach(this::checkFileIntegrity);
            }
        } catch (Exception e) {
            // Ignore file system monitoring errors
        }
    }
    
    /**
     * Check individual file integrity
     */
    private void checkFileIntegrity(Path filePath) {
        try {
            // Check file permissions and attributes
            if (Files.isWritable(filePath) && Files.isExecutable(filePath)) {
                // Suspicious: file is both writable and executable
                Map<String, String> metadata = new HashMap<>();
                metadata.put("filename", filePath.toString());
                metadata.put("issue", "writable_executable");
                recordSecurityEvent("FILE_MODIFIED", "filesystem", metadata);
            }
        } catch (Exception e) {
            // Ignore individual file check errors
        }
    }
    
    /**
     * Analyze behavior patterns
     */
    private void analyzeBehaviorPatterns() {
        // Analyze event frequency patterns
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourAgo = now.minus(1, ChronoUnit.HOURS);
        
        // Check for unusual activity spikes
        long recentEvents = totalEvents.get();
        if (recentEvents > 1000) { // Threshold for unusual activity
            updateThreatLevel(ThreatType.DATA_EXFILTRATION, ThreatLevel.MEDIUM);
        }
    }
    
    /**
     * Detect system anomalies
     */
    private void detectAnomalies() {
        // Detect unusual patterns in threat indicators
        for (ThreatIndicator indicator : threatIndicators.values()) {
            if (indicator.getLevel().getLevel() >= ThreatLevel.HIGH.getLevel()) {
                // High threat level detected
                if (auditManager != null) {
                    auditManager.logSecurityEvent("THREAT_LEVEL_HIGH", 
                        "High threat level detected: " + indicator.getType().name(), 
                        AuditManager.AuditSeverity.CRITICAL, null, 
                        indicator.getType().getDescription());
                }
            }
        }
    }
    
    /**
     * Update threat level for a specific threat type
     */
    private void updateThreatLevel(ThreatType type, ThreatLevel level) {
        ThreatIndicator indicator = threatIndicators.get(type.name());
        if (indicator != null && level.getLevel() > indicator.getLevel().getLevel()) {
            indicator.setLevel(level);
            indicator.setLastUpdated(LocalDateTime.now());
            
            System.out.printf("🚨 Threat level updated: %s -> %s%n", type.name(), level.name());
        }
    }
    
    /**
     * Count recent events of a specific type
     */
    private int countRecentEvents(String eventType, LocalDateTime since) {
        // Simplified implementation - in production would use time-based counting
        AtomicInteger count = eventCounts.get(eventType);
        return count != null ? count.get() : 0;
    }
    
    /**
     * Check if file is sensitive
     */
    private boolean isSensitiveFile(String fileName) {
        String lowerName = fileName.toLowerCase();
        return lowerName.contains("password") || lowerName.contains("key") || 
               lowerName.contains("secret") || lowerName.contains("private") ||
               lowerName.endsWith(".key") || lowerName.endsWith(".pem");
    }
    
    /**
     * Check if file is a system file
     */
    private boolean isSystemFile(String fileName) {
        String lowerName = fileName.toLowerCase();
        return lowerName.contains("system") || lowerName.contains("config") ||
               lowerName.contains("registry") || lowerName.startsWith("/etc/") ||
               lowerName.startsWith("c:\\windows\\");
    }
    
    /**
     * Check if process is suspicious
     */
    private boolean isSuspiciousProcess(String processName) {
        String lowerName = processName.toLowerCase();
        return lowerName.contains("debug") || lowerName.contains("inject") ||
               lowerName.contains("dump") || lowerName.contains("crack") ||
               lowerName.contains("hack");
    }
    
    /**
     * Check if network address is suspicious
     */
    private boolean isSuspiciousAddress(String address) {
        // Check for known malicious IP ranges or suspicious patterns
        return address.startsWith("10.0.0.") || address.startsWith("192.168.1.") ||
               address.contains("tor") || address.contains("proxy");
    }
    
    /**
     * Get current threat assessment
     */
    public ThreatAssessment getCurrentThreatAssessment() {
        ThreatLevel overallLevel = ThreatLevel.LOW;
        Map<ThreatType, ThreatLevel> currentThreats = new HashMap<>();
        
        for (Map.Entry<String, ThreatIndicator> entry : threatIndicators.entrySet()) {
            ThreatIndicator indicator = entry.getValue();
            ThreatType type = indicator.getType();
            ThreatLevel level = indicator.getLevel();
            
            currentThreats.put(type, level);
            
            if (level.getLevel() > overallLevel.getLevel()) {
                overallLevel = level;
            }
        }
        
        return new ThreatAssessment(overallLevel, currentThreats, totalEvents.get());
    }
    
    /**
     * Threat indicator class
     */
    private static class ThreatIndicator {
        private final ThreatType type;
        private ThreatLevel level;
        private LocalDateTime lastUpdated;
        
        public ThreatIndicator(ThreatType type, ThreatLevel level) {
            this.type = type;
            this.level = level;
            this.lastUpdated = LocalDateTime.now();
        }
        
        public ThreatType getType() { return type; }
        public ThreatLevel getLevel() { return level; }
        public LocalDateTime getLastUpdated() { return lastUpdated; }
        
        public void setLevel(ThreatLevel level) { this.level = level; }
        public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
    }
    
    /**
     * Threat assessment result
     */
    public static class ThreatAssessment {
        private final ThreatLevel overallLevel;
        private final Map<ThreatType, ThreatLevel> threats;
        private final long totalEvents;
        private final LocalDateTime timestamp;
        
        public ThreatAssessment(ThreatLevel overallLevel, Map<ThreatType, ThreatLevel> threats, long totalEvents) {
            this.overallLevel = overallLevel;
            this.threats = new HashMap<>(threats);
            this.totalEvents = totalEvents;
            this.timestamp = LocalDateTime.now();
        }
        
        public ThreatLevel getOverallLevel() { return overallLevel; }
        public Map<ThreatType, ThreatLevel> getThreats() { return new HashMap<>(threats); }
        public long getTotalEvents() { return totalEvents; }
        public LocalDateTime getTimestamp() { return timestamp; }
        
        public String getAssessmentReport() {
            StringBuilder report = new StringBuilder();
            report.append("=== Threat Assessment Report ===\n");
            report.append("Timestamp: ").append(timestamp).append("\n");
            report.append("Overall Threat Level: ").append(overallLevel.name())
                  .append(" (").append(overallLevel.getDescription()).append(")\n");
            report.append("Total Events Processed: ").append(totalEvents).append("\n\n");
            
            report.append("Individual Threat Levels:\n");
            for (Map.Entry<ThreatType, ThreatLevel> entry : threats.entrySet()) {
                ThreatType type = entry.getKey();
                ThreatLevel level = entry.getValue();
                
                String status = level == ThreatLevel.LOW ? "✅" : 
                               level == ThreatLevel.MEDIUM ? "⚠️" : 
                               level == ThreatLevel.HIGH ? "🚨" : "🔥";
                
                report.append("  ").append(status).append(" ")
                      .append(type.name()).append(": ").append(level.name())
                      .append(" - ").append(type.getDescription()).append("\n");
            }
            
            return report.toString();
        }
    }
}
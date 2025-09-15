package com.ghostvault.core;

import com.ghostvault.config.AppConfig;
import com.ghostvault.model.VaultFile;
import com.ghostvault.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Manages decoy files and fake content to create realistic fake vault
 */
public class DecoyManager {
    
    private static final String[] BUSINESS_NAMES = {
        "Acme Corporation", "Global Industries", "Tech Solutions Inc", "Metro Consulting",
        "Prime Enterprises", "Alpha Systems", "Beta Technologies", "Gamma Corp"
    };
    
    private static final String[] PERSON_NAMES = {
        "John Smith", "Sarah Johnson", "Michael Brown", "Emily Davis",
        "David Wilson", "Lisa Anderson", "Robert Taylor", "Jennifer Martinez"
    };
    
    private static final String[] PROJECT_NAMES = {
        "Project Alpha", "Operation Beta", "Initiative Gamma", "Program Delta",
        "Campaign Epsilon", "Strategy Zeta", "Mission Eta", "Plan Theta"
    };
    
    private static final String[] DOCUMENT_TYPES = {
        "Meeting Notes", "Project Report", "Budget Analysis", "Status Update",
        "Planning Document", "Research Notes", "Training Materials", "Policy Document"
    };
    
    private final String decoyPath;
    private final Random random;
    private final List<VaultFile> decoyFiles;
    
    public DecoyManager() {
        this.decoyPath = AppConfig.DECOYS_DIR;
        this.random = new Random();
        this.decoyFiles = new ArrayList<>();
        
        try {
            FileUtils.ensureDirectoryExists(decoyPath);
            loadExistingDecoyFiles();
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize DecoyManager", e);
        }
    }
    
    /**
     * Generate realistic decoy files
     */
    public void generateDecoyFiles(int count) throws IOException {
        for (int i = 0; i < count; i++) {
            generateSingleDecoyFile();
        }
    }
    
    /**
     * Generate a single realistic decoy file
     */
    public VaultFile generateSingleDecoyFile() throws IOException {
        String fileName = generateRealisticFileName();
        String content = generateRealisticContent(fileName);
        
        // Create decoy file
        Path decoyFilePath = Paths.get(decoyPath, fileName);
        Files.write(decoyFilePath, content.getBytes());
        
        // Create VaultFile metadata for decoy
        VaultFile decoyFile = new VaultFile(
            fileName,
            UUID.randomUUID().toString(),
            fileName, // Decoys use original names
            content.length(),
            FileUtils.calculateSHA256(content.getBytes()),
            System.currentTimeMillis()
        );
        
        decoyFiles.add(decoyFile);
        return decoyFile;
    }
    
    /**
     * Generate realistic file name
     */
    private String generateRealisticFileName() {
        String[] extensions = {".txt", ".docx", ".pdf", ".xlsx", ".pptx"};
        String extension = extensions[random.nextInt(extensions.length)];
        
        String baseName;
        switch (random.nextInt(6)) {
            case 0:
                baseName = DOCUMENT_TYPES[random.nextInt(DOCUMENT_TYPES.length)] + 
                          "_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
                break;
            case 1:
                baseName = PROJECT_NAMES[random.nextInt(PROJECT_NAMES.length)].replace(" ", "_") + 
                          "_Report";
                break;
            case 2:
                baseName = "Budget_" + (LocalDate.now().getYear()) + "_Q" + 
                          (random.nextInt(4) + 1);
                break;
            case 3:
                baseName = "Meeting_" + LocalDate.now().format(DateTimeFormatter.ofPattern("MMM_dd"));
                break;
            case 4:
                baseName = PERSON_NAMES[random.nextInt(PERSON_NAMES.length)].replace(" ", "_") + 
                          "_Notes";
                break;
            default:
                baseName = "Document_" + 
                          LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd"));
        }
        
        return baseName + extension;
    }
    
    /**
     * Generate realistic content based on file name
     */
    private String generateRealisticContent(String fileName) {
        String extension = FileUtils.getFileExtension(fileName);
        String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
        
        switch (extension) {
            case "txt":
                return generateTextContent(baseName);
            case "docx":
                return generateDocumentContent(baseName);
            case "pdf":
                return generateReportContent(baseName);
            case "xlsx":
                return generateSpreadsheetContent(baseName);
            case "pptx":
                return generatePresentationContent(baseName);
            default:
                return generateGenericContent(baseName);
        }
    }
    
    /**
     * Generate text file content
     */
    private String generateTextContent(String baseName) {
        StringBuilder content = new StringBuilder();
        
        if (baseName.toLowerCase().contains("meeting")) {
            content.append(generateMeetingNotes());
        } else if (baseName.toLowerCase().contains("notes")) {
            content.append(generatePersonalNotes());
        } else if (baseName.toLowerCase().contains("todo") || baseName.toLowerCase().contains("task")) {
            content.append(generateTaskList());
        } else {
            content.append(generateGenericNotes());
        }
        
        return content.toString();
    }
    
    /**
     * Generate meeting notes content
     */
    private String generateMeetingNotes() {
        StringBuilder notes = new StringBuilder();
        String company = BUSINESS_NAMES[random.nextInt(BUSINESS_NAMES.length)];
        String project = PROJECT_NAMES[random.nextInt(PROJECT_NAMES.length)];
        
        notes.append("MEETING NOTES\n");
        notes.append("=============\n\n");
        notes.append("Date: ").append(LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"))).append("\n");
        notes.append("Company: ").append(company).append("\n");
        notes.append("Project: ").append(project).append("\n\n");
        
        notes.append("Attendees:\n");
        for (int i = 0; i < 3 + random.nextInt(3); i++) {
            notes.append("- ").append(PERSON_NAMES[random.nextInt(PERSON_NAMES.length)]).append("\n");
        }
        
        notes.append("\nAgenda:\n");
        String[] agendaItems = {
            "Budget review and allocation",
            "Project timeline discussion",
            "Resource planning",
            "Risk assessment",
            "Quality assurance review",
            "Stakeholder feedback",
            "Next phase planning"
        };
        
        for (int i = 0; i < 3 + random.nextInt(3); i++) {
            notes.append((i + 1)).append(". ").append(agendaItems[random.nextInt(agendaItems.length)]).append("\n");
        }
        
        notes.append("\nAction Items:\n");
        notes.append("- Review budget proposals by end of week\n");
        notes.append("- Schedule follow-up meeting with stakeholders\n");
        notes.append("- Prepare status report for management\n");
        notes.append("- Update project documentation\n");
        
        notes.append("\nNext Meeting: ").append(LocalDate.now().plusWeeks(1).format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"))).append("\n");
        
        return notes.toString();
    }
    
    /**
     * Generate personal notes content
     */
    private String generatePersonalNotes() {
        StringBuilder notes = new StringBuilder();
        
        notes.append("Personal Notes\n");
        notes.append("==============\n\n");
        notes.append("Date: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).append("\n\n");
        
        String[] noteTypes = {
            "Project ideas and brainstorming session",
            "Book recommendations and reading list",
            "Travel planning and itinerary notes",
            "Learning objectives and study plan",
            "Health and fitness goals",
            "Financial planning and budget notes"
        };
        
        notes.append(noteTypes[random.nextInt(noteTypes.length)]).append("\n\n");
        
        notes.append("Key Points:\n");
        notes.append("- Focus on long-term objectives\n");
        notes.append("- Maintain work-life balance\n");
        notes.append("- Regular progress reviews\n");
        notes.append("- Continuous learning and improvement\n");
        notes.append("- Network building and relationship management\n\n");
        
        notes.append("Next Steps:\n");
        notes.append("1. Research and gather more information\n");
        notes.append("2. Create detailed action plan\n");
        notes.append("3. Set realistic timelines\n");
        notes.append("4. Monitor progress regularly\n");
        
        return notes.toString();
    }
    
    /**
     * Generate task list content
     */
    private String generateTaskList() {
        StringBuilder tasks = new StringBuilder();
        
        tasks.append("TASK LIST\n");
        tasks.append("=========\n\n");
        tasks.append("Week of: ").append(LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"))).append("\n\n");
        
        String[] taskCategories = {"Work", "Personal", "Health", "Learning", "Finance"};
        
        for (String category : Arrays.copyOf(taskCategories, 2 + random.nextInt(3))) {
            tasks.append(category).append(":\n");
            
            String[] workTasks = {
                "Complete project documentation",
                "Review team performance metrics",
                "Prepare presentation for client meeting",
                "Update project timeline",
                "Conduct code review"
            };
            
            String[] personalTasks = {
                "Grocery shopping",
                "Schedule dentist appointment",
                "Plan weekend activities",
                "Call family members",
                "Organize home office"
            };
            
            String[] tasks_list = category.equals("Work") ? workTasks : personalTasks;
            
            for (int i = 0; i < 2 + random.nextInt(3); i++) {
                tasks.append("  [ ] ").append(tasks_list[random.nextInt(tasks_list.length)]).append("\n");
            }
            tasks.append("\n");
        }
        
        return tasks.toString();
    }
    
    /**
     * Generate generic notes content
     */
    private String generateGenericNotes() {
        StringBuilder notes = new StringBuilder();
        
        notes.append("Notes and Observations\n");
        notes.append("======================\n\n");
        notes.append("Created: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).append("\n\n");
        
        String[] topics = {
            "Market analysis and trends",
            "Technology evaluation",
            "Process improvement ideas",
            "Customer feedback summary",
            "Competitive analysis",
            "Innovation opportunities"
        };
        
        notes.append("Topic: ").append(topics[random.nextInt(topics.length)]).append("\n\n");
        
        notes.append("Summary:\n");
        notes.append("This document contains preliminary observations and analysis ");
        notes.append("based on recent research and data collection. The findings ");
        notes.append("suggest several areas for further investigation and potential ");
        notes.append("improvement opportunities.\n\n");
        
        notes.append("Key Findings:\n");
        notes.append("- Current processes show room for optimization\n");
        notes.append("- Market conditions are favorable for expansion\n");
        notes.append("- Technology adoption rates are increasing\n");
        notes.append("- Customer satisfaction metrics are stable\n\n");
        
        notes.append("Recommendations:\n");
        notes.append("1. Conduct detailed feasibility study\n");
        notes.append("2. Engage stakeholders for feedback\n");
        notes.append("3. Develop implementation timeline\n");
        notes.append("4. Allocate necessary resources\n");
        
        return notes.toString();
    }
    
    /**
     * Generate document content
     */
    private String generateDocumentContent(String baseName) {
        StringBuilder doc = new StringBuilder();
        
        doc.append("DOCUMENT HEADER\n");
        doc.append("===============\n\n");
        doc.append("Title: ").append(baseName.replace("_", " ")).append("\n");
        doc.append("Date: ").append(LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"))).append("\n");
        doc.append("Author: ").append(PERSON_NAMES[random.nextInt(PERSON_NAMES.length)]).append("\n");
        doc.append("Company: ").append(BUSINESS_NAMES[random.nextInt(BUSINESS_NAMES.length)]).append("\n\n");
        
        doc.append("EXECUTIVE SUMMARY\n");
        doc.append("=================\n\n");
        doc.append("This document provides an overview of current operations and ");
        doc.append("strategic initiatives. The analysis covers key performance ");
        doc.append("indicators, market conditions, and future opportunities.\n\n");
        
        doc.append("MAIN CONTENT\n");
        doc.append("============\n\n");
        doc.append("1. Current Status\n");
        doc.append("   - Operations are running smoothly\n");
        doc.append("   - All targets are being met\n");
        doc.append("   - Team performance is excellent\n\n");
        
        doc.append("2. Future Plans\n");
        doc.append("   - Expand into new markets\n");
        doc.append("   - Improve operational efficiency\n");
        doc.append("   - Invest in new technologies\n\n");
        
        doc.append("CONCLUSION\n");
        doc.append("==========\n\n");
        doc.append("The organization is well-positioned for continued growth ");
        doc.append("and success. Regular monitoring and adjustment of strategies ");
        doc.append("will ensure optimal performance.\n");
        
        return doc.toString();
    }
    
    /**
     * Generate report content
     */
    private String generateReportContent(String baseName) {
        return generateDocumentContent(baseName) + "\n\n[This would be a PDF report in actual implementation]";
    }
    
    /**
     * Generate spreadsheet content
     */
    private String generateSpreadsheetContent(String baseName) {
        StringBuilder sheet = new StringBuilder();
        
        sheet.append("SPREADSHEET DATA\n");
        sheet.append("================\n\n");
        sheet.append("File: ").append(baseName).append(".xlsx\n");
        sheet.append("Created: ").append(LocalDate.now()).append("\n\n");
        
        if (baseName.toLowerCase().contains("budget")) {
            sheet.append("BUDGET BREAKDOWN\n");
            sheet.append("Category\t\tAmount\t\tPercentage\n");
            sheet.append("Personnel\t\t$125,000\t45%\n");
            sheet.append("Equipment\t\t$75,000\t\t27%\n");
            sheet.append("Operations\t\t$50,000\t\t18%\n");
            sheet.append("Marketing\t\t$25,000\t\t9%\n");
            sheet.append("Miscellaneous\t$5,000\t\t1%\n");
            sheet.append("TOTAL\t\t\t$280,000\t100%\n");
        } else {
            sheet.append("DATA SUMMARY\n");
            sheet.append("Month\t\tRevenue\t\tExpenses\tProfit\n");
            for (int i = 1; i <= 6; i++) {
                int revenue = 50000 + random.nextInt(20000);
                int expenses = 30000 + random.nextInt(15000);
                int profit = revenue - expenses;
                sheet.append("Month ").append(i).append("\t\t$").append(revenue)
                     .append("\t\t$").append(expenses).append("\t\t$").append(profit).append("\n");
            }
        }
        
        sheet.append("\n[This would be an Excel file in actual implementation]");
        
        return sheet.toString();
    }
    
    /**
     * Generate presentation content
     */
    private String generatePresentationContent(String baseName) {
        StringBuilder ppt = new StringBuilder();
        
        ppt.append("PRESENTATION OUTLINE\n");
        ppt.append("====================\n\n");
        ppt.append("Title: ").append(baseName.replace("_", " ")).append("\n");
        ppt.append("Presenter: ").append(PERSON_NAMES[random.nextInt(PERSON_NAMES.length)]).append("\n");
        ppt.append("Date: ").append(LocalDate.now()).append("\n\n");
        
        ppt.append("SLIDE OUTLINE:\n\n");
        ppt.append("Slide 1: Title Slide\n");
        ppt.append("Slide 2: Agenda\n");
        ppt.append("Slide 3: Current Situation\n");
        ppt.append("Slide 4: Key Challenges\n");
        ppt.append("Slide 5: Proposed Solutions\n");
        ppt.append("Slide 6: Implementation Plan\n");
        ppt.append("Slide 7: Expected Outcomes\n");
        ppt.append("Slide 8: Next Steps\n");
        ppt.append("Slide 9: Questions & Discussion\n\n");
        
        ppt.append("KEY POINTS:\n");
        ppt.append("- Clear problem definition\n");
        ppt.append("- Data-driven analysis\n");
        ppt.append("- Practical solutions\n");
        ppt.append("- Realistic timeline\n");
        ppt.append("- Measurable outcomes\n\n");
        
        ppt.append("[This would be a PowerPoint file in actual implementation]");
        
        return ppt.toString();
    }
    
    /**
     * Generate generic content
     */
    private String generateGenericContent(String baseName) {
        return "Generic document content for: " + baseName + "\n\n" +
               "This is a placeholder document created for demonstration purposes.\n" +
               "In a real scenario, this would contain relevant business content.\n\n" +
               "Created: " + LocalDateTime.now() + "\n" +
               "Size: " + (500 + random.nextInt(1000)) + " words\n";
    }
    
    /**
     * Load existing decoy files
     */
    private void loadExistingDecoyFiles() {
        File decoyDir = new File(decoyPath);
        if (!decoyDir.exists()) {
            return;
        }
        
        File[] files = decoyDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    try {
                        byte[] content = Files.readAllBytes(file.toPath());
                        VaultFile decoyFile = new VaultFile(
                            file.getName(),
                            UUID.randomUUID().toString(),
                            file.getName(),
                            file.length(),
                            FileUtils.calculateSHA256(content),
                            file.lastModified()
                        );
                        decoyFiles.add(decoyFile);
                    } catch (IOException e) {
                        // Skip files that can't be read
                    }
                }
            }
        }
    }
    
    /**
     * Get all decoy files
     */
    public List<VaultFile> getDecoyFiles() {
        return new ArrayList<>(decoyFiles);
    }
    
    /**
     * Get decoy file count
     */
    public int getDecoyFileCount() {
        return decoyFiles.size();
    }
    
    /**
     * Get decoy file by name
     */
    public VaultFile getDecoyFile(String fileName) {
        return decoyFiles.stream()
                .filter(file -> file.getOriginalName().equals(fileName))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Remove decoy file
     */
    public boolean removeDecoyFile(String fileName) {
        VaultFile decoyFile = getDecoyFile(fileName);
        if (decoyFile != null) {
            try {
                Path filePath = Paths.get(decoyPath, fileName);
                Files.deleteIfExists(filePath);
                decoyFiles.remove(decoyFile);
                return true;
            } catch (IOException e) {
                return false;
            }
        }
        return false;
    }
    
    /**
     * Get decoy file content
     */
    public byte[] getDecoyFileContent(String fileName) throws IOException {
        Path filePath = Paths.get(decoyPath, fileName);
        if (Files.exists(filePath)) {
            return Files.readAllBytes(filePath);
        }
        throw new IOException("Decoy file not found: " + fileName);
    }
    
    /**
     * Search decoy files
     */
    public List<VaultFile> searchDecoyFiles(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getDecoyFiles();
        }
        
        String lowerQuery = query.toLowerCase();
        return decoyFiles.stream()
                .filter(file -> file.getOriginalName().toLowerCase().contains(lowerQuery))
                .collect(ArrayList::new, (list, file) -> list.add(file), ArrayList::addAll);
    }
    
    /**
     * Ensure minimum number of decoy files exist
     */
    public void ensureMinimumDecoyFiles(int minimumCount) throws IOException {
        int currentCount = getDecoyFileCount();
        if (currentCount < minimumCount) {
            int needed = minimumCount - currentCount;
            generateDecoyFiles(needed);
        }
    }
    
    /**
     * Get decoy statistics
     */
    public DecoyStats getDecoyStats() {
        long totalSize = decoyFiles.stream()
                .mapToLong(VaultFile::getSize)
                .sum();
        
        Map<String, Integer> extensionCounts = new HashMap<>();
        for (VaultFile file : decoyFiles) {
            String extension = file.getExtension();
            extensionCounts.put(extension, extensionCounts.getOrDefault(extension, 0) + 1);
        }
        
        return new DecoyStats(decoyFiles.size(), totalSize, extensionCounts);
    }
    
    /**
     * Decoy statistics data class
     */
    public static class DecoyStats {
        private final int fileCount;
        private final long totalSize;
        private final Map<String, Integer> extensionCounts;
        
        public DecoyStats(int fileCount, long totalSize, Map<String, Integer> extensionCounts) {
            this.fileCount = fileCount;
            this.totalSize = totalSize;
            this.extensionCounts = new HashMap<>(extensionCounts);
        }
        
        public int getFileCount() { return fileCount; }
        public long getTotalSize() { return totalSize; }
        public Map<String, Integer> getExtensionCounts() { return new HashMap<>(extensionCounts); }
        
        public String getFormattedSize() {
            return FileUtils.formatFileSize(totalSize);
        }
        
        @Override
        public String toString() {
            return String.format("DecoyStats{files=%d, size=%s, types=%d}", 
                fileCount, getFormattedSize(), extensionCounts.size());
        }
    }
}
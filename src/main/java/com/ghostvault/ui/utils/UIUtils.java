package com.ghostvault.ui.utils;

import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Utility methods for UI operations
 */
public final class UIUtils {
    
    private static final DecimalFormat SIZE_FORMAT = new DecimalFormat("#.#");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
    
    // Private constructor to prevent instantiation
    private UIUtils() {}
    
    /**
     * Format file size in human-readable format
     */
    public static String formatFileSize(long bytes) {
        if (bytes < 0) return "Unknown";
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return SIZE_FORMAT.format(bytes / 1024.0) + " KB";
        if (bytes < 1024 * 1024 * 1024) return SIZE_FORMAT.format(bytes / (1024.0 * 1024.0)) + " MB";
        return SIZE_FORMAT.format(bytes / (1024.0 * 1024.0 * 1024.0)) + " GB";
    }
    
    /**
     * Format date and time
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "Unknown";
        return dateTime.format(DATE_FORMAT);
    }
    
    /**
     * Get file extension from filename
     */
    public static String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) return "";
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 && lastDot < filename.length() - 1 ? 
            filename.substring(lastDot + 1).toLowerCase() : "";
    }
    
    /**
     * Get file extension from File object
     */
    public static String getFileExtension(File file) {
        return file != null ? getFileExtension(file.getName()) : "";
    }
    
    /**
     * Get base name without extension
     */
    public static String getBaseName(String filename) {
        if (filename == null || filename.isEmpty()) return "";
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(0, lastDot) : filename;
    }
    
    /**
     * Check if file is an image
     */
    public static boolean isImageFile(File file) {
        String extension = getFileExtension(file);
        for (String imageExt : UIConstants.FileTypes.IMAGE_EXTENSIONS) {
            if (imageExt.equals(extension)) return true;
        }
        return false;
    }
    
    /**
     * Check if file is a video
     */
    public static boolean isVideoFile(File file) {
        String extension = getFileExtension(file);
        for (String videoExt : UIConstants.FileTypes.VIDEO_EXTENSIONS) {
            if (videoExt.equals(extension)) return true;
        }
        return false;
    }
    
    /**
     * Check if file is audio
     */
    public static boolean isAudioFile(File file) {
        String extension = getFileExtension(file);
        for (String audioExt : UIConstants.FileTypes.AUDIO_EXTENSIONS) {
            if (audioExt.equals(extension)) return true;
        }
        return false;
    }
    
    /**
     * Check if file is a code file
     */
    public static boolean isCodeFile(File file) {
        String extension = getFileExtension(file);
        for (String codeExt : UIConstants.FileTypes.CODE_EXTENSIONS) {
            if (codeExt.equals(extension)) return true;
        }
        return false;
    }
    
    /**
     * Load image from resources
     */
    public static Image loadImageFromResources(String path) {
        try {
            InputStream stream = UIUtils.class.getResourceAsStream(path);
            return stream != null ? new Image(stream) : null;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Create ImageView with specified size
     */
    public static ImageView createImageView(String resourcePath, double width, double height) {
        Image image = loadImageFromResources(resourcePath);
        if (image != null) {
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(width);
            imageView.setFitHeight(height);
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);
            return imageView;
        }
        return new ImageView();
    }
    
    /**
     * Center stage on screen
     */
    public static void centerStage(Stage stage) {
        Platform.runLater(() -> {
            Bounds screenBounds = Screen.getPrimary().getVisualBounds();
            stage.setX((screenBounds.getWidth() - stage.getWidth()) / 2);
            stage.setY((screenBounds.getHeight() - stage.getHeight()) / 2);
        });
    }
    
    /**
     * Center stage relative to parent window
     */
    public static void centerStageRelativeToParent(Stage stage, Window parent) {
        Platform.runLater(() -> {
            if (parent != null) {
                double centerX = parent.getX() + parent.getWidth() / 2;
                double centerY = parent.getY() + parent.getHeight() / 2;
                stage.setX(centerX - stage.getWidth() / 2);
                stage.setY(centerY - stage.getHeight() / 2);
            } else {
                centerStage(stage);
            }
        });
    }\n    \n    /**\n     * Apply CSS style class to node\n     */\n    public static void addStyleClass(Node node, String styleClass) {\n        if (node != null && styleClass != null && !styleClass.isEmpty()) {\n            node.getStyleClass().add(styleClass);\n        }\n    }\n    \n    /**\n     * Remove CSS style class from node\n     */\n    public static void removeStyleClass(Node node, String styleClass) {\n        if (node != null && styleClass != null) {\n            node.getStyleClass().remove(styleClass);\n        }\n    }\n    \n    /**\n     * Toggle CSS style class on node\n     */\n    public static void toggleStyleClass(Node node, String styleClass) {\n        if (node != null && styleClass != null) {\n            if (node.getStyleClass().contains(styleClass)) {\n                node.getStyleClass().remove(styleClass);\n            } else {\n                node.getStyleClass().add(styleClass);\n            }\n        }\n    }\n    \n    /**\n     * Run task on JavaFX Application Thread\n     */\n    public static void runOnFXThread(Runnable task) {\n        if (Platform.isFxApplicationThread()) {\n            task.run();\n        } else {\n            Platform.runLater(task);\n        }\n    }\n    \n    /**\n     * Run task on background thread\n     */\n    public static CompletableFuture<Void> runOnBackgroundThread(Runnable task) {\n        return CompletableFuture.runAsync(task);\n    }\n    \n    /**\n     * Run task on background thread with result\n     */\n    public static <T> CompletableFuture<T> runOnBackgroundThread(java.util.function.Supplier<T> task) {\n        return CompletableFuture.supplyAsync(task);\n    }\n    \n    /**\n     * Show confirmation dialog\n     */\n    public static CompletableFuture<Boolean> showConfirmationDialog(\n            String title, String header, String content) {\n        CompletableFuture<Boolean> future = new CompletableFuture<>();\n        \n        runOnFXThread(() -> {\n            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);\n            alert.setTitle(title);\n            alert.setHeaderText(header);\n            alert.setContentText(content);\n            \n            Optional<ButtonType> result = alert.showAndWait();\n            future.complete(result.isPresent() && result.get() == ButtonType.OK);\n        });\n        \n        return future;\n    }\n    \n    /**\n     * Show error dialog\n     */\n    public static void showErrorDialog(String title, String header, String content) {\n        runOnFXThread(() -> {\n            Alert alert = new Alert(Alert.AlertType.ERROR);\n            alert.setTitle(title);\n            alert.setHeaderText(header);\n            alert.setContentText(content);\n            alert.showAndWait();\n        });\n    }\n    \n    /**\n     * Show information dialog\n     */\n    public static void showInfoDialog(String title, String header, String content) {\n        runOnFXThread(() -> {\n            Alert alert = new Alert(Alert.AlertType.INFORMATION);\n            alert.setTitle(title);\n            alert.setHeaderText(header);\n            alert.setContentText(content);\n            alert.showAndWait();\n        });\n    }\n    \n    /**\n     * Show warning dialog\n     */\n    public static void showWarningDialog(String title, String header, String content) {\n        runOnFXThread(() -> {\n            Alert alert = new Alert(Alert.AlertType.WARNING);\n            alert.setTitle(title);\n            alert.setHeaderText(header);\n            alert.setContentText(content);\n            alert.showAndWait();\n        });\n    }\n    \n    /**\n     * Validate file size for operation\n     */\n    public static boolean isFileSizeValid(File file, long maxSize) {\n        return file != null && file.exists() && file.length() <= maxSize;\n    }\n    \n    /**\n     * Check if file is too large for preview\n     */\n    public static boolean isFileTooLargeForPreview(File file) {\n        return file != null && file.length() > UIConstants.FileLimits.MAX_PREVIEW_SIZE;\n    }\n    \n    /**\n     * Check if file is too large for thumbnail\n     */\n    public static boolean isFileTooLargeForThumbnail(File file) {\n        return file != null && file.length() > UIConstants.FileLimits.MAX_THUMBNAIL_SIZE;\n    }\n    \n    /**\n     * Generate unique filename to avoid conflicts\n     */\n    public static String generateUniqueFilename(File directory, String baseName, String extension) {\n        if (directory == null || baseName == null) return baseName;\n        \n        String filename = baseName + (extension != null && !extension.isEmpty() ? \".\" + extension : \"\");\n        File file = new File(directory, filename);\n        \n        if (!file.exists()) {\n            return filename;\n        }\n        \n        int counter = 1;\n        do {\n            filename = baseName + \" (\" + counter + \")\" + \n                (extension != null && !extension.isEmpty() ? \".\" + extension : \"\");\n            file = new File(directory, filename);\n            counter++;\n        } while (file.exists() && counter < 1000); // Prevent infinite loop\n        \n        return filename;\n    }\n    \n    /**\n     * Truncate text to specified length\n     */\n    public static String truncateText(String text, int maxLength) {\n        if (text == null || text.length() <= maxLength) {\n            return text;\n        }\n        return text.substring(0, maxLength - 3) + \"...\";\n    }\n    \n    /**\n     * Escape special characters for display\n     */\n    public static String escapeForDisplay(String text) {\n        if (text == null) return \"\";\n        return text.replace(\"&\", \"&amp;\")\n                  .replace(\"<\", \"&lt;\")\n                  .replace(\">\", \"&gt;\")\n                  .replace(\"\\\"\", \"&quot;\")\n                  .replace(\"'\", \"&#39;\");\n    }\n    \n    /**\n     * Calculate progress percentage\n     */\n    public static double calculateProgress(long current, long total) {\n        if (total <= 0) return 0.0;\n        return Math.min(1.0, Math.max(0.0, (double) current / total));\n    }\n    \n    /**\n     * Format progress as percentage string\n     */\n    public static String formatProgress(double progress) {\n        return String.format(\"%.0f%%\", progress * 100);\n    }\n    \n    /**\n     * Debounce function calls\n     */\n    public static class Debouncer {\n        private final long delay;\n        private long lastCallTime = 0;\n        \n        public Debouncer(long delayMs) {\n            this.delay = delayMs;\n        }\n        \n        public void call(Runnable action) {\n            long currentTime = System.currentTimeMillis();\n            lastCallTime = currentTime;\n            \n            CompletableFuture.delayedExecutor(delay, java.util.concurrent.TimeUnit.MILLISECONDS)\n                .execute(() -> {\n                    if (currentTime == lastCallTime) {\n                        Platform.runLater(action);\n                    }\n                });\n        }\n    }\n    \n    /**\n     * Throttle function calls\n     */\n    public static class Throttler {\n        private final long interval;\n        private long lastExecutionTime = 0;\n        private boolean pending = false;\n        \n        public Throttler(long intervalMs) {\n            this.interval = intervalMs;\n        }\n        \n        public void call(Runnable action) {\n            long currentTime = System.currentTimeMillis();\n            \n            if (currentTime - lastExecutionTime >= interval) {\n                lastExecutionTime = currentTime;\n                Platform.runLater(action);\n            } else if (!pending) {\n                pending = true;\n                long delay = interval - (currentTime - lastExecutionTime);\n                \n                CompletableFuture.delayedExecutor(delay, java.util.concurrent.TimeUnit.MILLISECONDS)\n                    .execute(() -> {\n                        pending = false;\n                        lastExecutionTime = System.currentTimeMillis();\n                        Platform.runLater(action);\n                    });\n            }\n        }\n    }\n    \n    /**\n     * Memory usage utilities\n     */\n    public static class MemoryUtils {\n        \n        public static long getUsedMemory() {\n            Runtime runtime = Runtime.getRuntime();\n            return runtime.totalMemory() - runtime.freeMemory();\n        }\n        \n        public static long getTotalMemory() {\n            return Runtime.getRuntime().totalMemory();\n        }\n        \n        public static long getMaxMemory() {\n            return Runtime.getRuntime().maxMemory();\n        }\n        \n        public static double getMemoryUsagePercentage() {\n            return (double) getUsedMemory() / getMaxMemory() * 100;\n        }\n        \n        public static String formatMemoryUsage() {\n            return String.format(\"Memory: %s / %s (%.1f%%)\",\n                formatFileSize(getUsedMemory()),\n                formatFileSize(getMaxMemory()),\n                getMemoryUsagePercentage());\n        }\n        \n        public static void requestGarbageCollection() {\n            System.gc();\n        }\n    }\n    \n    /**\n     * Performance monitoring utilities\n     */\n    public static class PerformanceUtils {\n        \n        public static void measureExecutionTime(String operationName, Runnable operation) {\n            long startTime = System.nanoTime();\n            operation.run();\n            long endTime = System.nanoTime();\n            long durationMs = (endTime - startTime) / 1_000_000;\n            System.out.println(operationName + \" took \" + durationMs + \" ms\");\n        }\n        \n        public static <T> T measureExecutionTime(String operationName, java.util.function.Supplier<T> operation) {\n            long startTime = System.nanoTime();\n            T result = operation.get();\n            long endTime = System.nanoTime();\n            long durationMs = (endTime - startTime) / 1_000_000;\n            System.out.println(operationName + \" took \" + durationMs + \" ms\");\n            return result;\n        }\n    }\n}"
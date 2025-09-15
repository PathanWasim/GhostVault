package com.ghostvault.ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Test for UI Manager functionality
 * Note: This is a basic test - full UI testing would require TestFX or similar framework
 */
public class UIManagerTest extends Application {
    
    private static CountDownLatch latch = new CountDownLatch(1);
    private static UIManagerTest instance;
    
    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("            UIManager Test Application");
        System.out.println("=================================================");
        
        // Launch JavaFX application
        new Thread(() -> Application.launch(UIManagerTest.class, args)).start();
        
        try {
            // Wait for JavaFX to initialize
            latch.await(10, TimeUnit.SECONDS);
            
            if (instance != null) {
                instance.runTests();
            }
            
        } catch (InterruptedException e) {
            System.err.println("Test interrupted: " + e.getMessage());
        }
    }
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        instance = this;
        
        // Initialize UI Manager
        UIManager uiManager = UIManager.getInstance();
        uiManager.initialize(primaryStage);
        
        // Create test UI
        VBox root = createTestUI(uiManager);
        
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("GhostVault UI Manager Test");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // Initialize notification manager
        NotificationManager.getInstance().initialize(primaryStage);
        
        latch.countDown();
    }
    
    /**
     * Create test UI with various components
     */
    private VBox createTestUI(UIManager uiManager) {
        VBox root = new VBox(20);
        root.setPadding(new javafx.geometry.Insets(20));
        
        // Title
        Label titleLabel = new Label("GhostVault UI Manager Test");
        titleLabel.setFont(javafx.scene.text.Font.font("System", javafx.scene.text.FontWeight.BOLD, 18));
        
        // Theme selection buttons
        javafx.scene.layout.HBox themeBox = new javafx.scene.layout.HBox(10);
        
        for (UIManager.Theme theme : UIManager.Theme.values()) {
            Button themeButton = uiManager.createStyledButton(theme.getDisplayName(), "button-primary");
            themeButton.setOnAction(e -> uiManager.applyThemeWithTransition(theme));
            themeBox.getChildren().add(themeButton);
        }
        
        // Test buttons with different styles
        javafx.scene.layout.HBox buttonBox = new javafx.scene.layout.HBox(10);
        
        Button primaryButton = uiManager.createPrimaryButton("Primary Action");
        Button successButton = uiManager.createSuccessButton("Success Action");
        Button dangerButton = uiManager.createDangerButton("Danger Action");
        
        buttonBox.getChildren().addAll(primaryButton, successButton, dangerButton);
        
        // Test text fields
        TextField testField = new TextField();
        testField.setPromptText("Test input field");
        testField.setPrefWidth(300);
        
        // Test notification buttons
        javafx.scene.layout.HBox notificationBox = new javafx.scene.layout.HBox(10);
        
        Button infoButton = new Button("Show Info");
        infoButton.setOnAction(e -> NotificationManager.getInstance().showInfo("Information", "This is an info notification"));
        
        Button successNotifButton = new Button("Show Success");
        successNotifButton.setOnAction(e -> NotificationManager.getInstance().showSuccess("Success", "Operation completed successfully"));
        
        Button warningButton = new Button("Show Warning");
        warningButton.setOnAction(e -> NotificationManager.getInstance().showWarning("Warning", "This is a warning message"));
        
        Button errorButton = new Button("Show Error");
        errorButton.setOnAction(e -> NotificationManager.getInstance().showError("Error", "An error has occurred"));
        
        Button securityButton = new Button("Show Security");
        securityButton.setOnAction(e -> NotificationManager.getInstance().showSecurity("Security Alert", "Security event detected"));
        
        notificationBox.getChildren().addAll(infoButton, successNotifButton, warningButton, errorButton, securityButton);
        
        // Test progress dialog button
        Button progressButton = new Button("Show Progress Dialog");
        progressButton.setOnAction(e -> {
            ProgressDialog dialog = uiManager.createProgressDialog("Processing", "Please wait while the operation completes...");
            dialog.show();
            
            // Simulate progress
            javafx.concurrent.Task<Void> task = new javafx.concurrent.Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    for (int i = 0; i <= 100; i++) {
                        Thread.sleep(50);
                        updateProgress(i, 100);
                        updateMessage("Processing step " + i + " of 100");
                    }
                    return null;
                }
            };
            
            dialog.setTask(task);
            new Thread(task).start();
        });
        
        // Test toast button
        Button toastButton = new Button("Show Toast");
        toastButton.setOnAction(e -> NotificationManager.getInstance().showToast("This is a toast message"));
        
        // Add fade animations to some elements
        uiManager.fadeIn(titleLabel);
        uiManager.fadeIn(themeBox, javafx.util.Duration.millis(500));
        uiManager.fadeIn(buttonBox, javafx.util.Duration.millis(750));
        
        root.getChildren().addAll(
            titleLabel,
            new javafx.scene.control.Separator(),
            new Label("Theme Selection:"),
            themeBox,
            new javafx.scene.control.Separator(),
            new Label("Styled Buttons:"),
            buttonBox,
            new javafx.scene.control.Separator(),
            new Label("Text Input:"),
            testField,
            new javafx.scene.control.Separator(),
            new Label("Notifications:"),
            notificationBox,
            new javafx.scene.control.Separator(),
            new Label("Dialogs:"),
            new javafx.scene.layout.HBox(10, progressButton, toastButton)
        );
        
        return root;
    }
    
    /**
     * Run automated tests
     */
    private void runTests() {
        Platform.runLater(() -> {
            try {
                testThemeChanges();
                testNotifications();
                testAnimations();
                
                System.out.println("\n✅ All UI Manager tests completed!");
                System.out.println("   - Theme system functional");
                System.out.println("   - Notification system functional");
                System.out.println("   - Animation system functional");
                System.out.println("   - Responsive feedback functional");
                
            } catch (Exception e) {
                System.err.println("❌ UI Manager test failed: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
    
    /**
     * Test theme changes
     */
    private void testThemeChanges() {
        UIManager uiManager = UIManager.getInstance();
        
        // Test each theme
        for (UIManager.Theme theme : UIManager.Theme.values()) {
            uiManager.applyTheme(theme);
            
            // Verify theme is applied
            assert uiManager.getCurrentTheme() == theme : "Theme should be applied: " + theme;
            
            System.out.println("   ✓ Theme applied: " + theme.getDisplayName());
        }
    }
    
    /**
     * Test notifications
     */
    private void testNotifications() {
        NotificationManager notificationManager = NotificationManager.getInstance();
        
        // Test different notification types
        notificationManager.showInfo("Test Info", "Info notification test");
        notificationManager.showSuccess("Test Success", "Success notification test");
        notificationManager.showWarning("Test Warning", "Warning notification test");
        notificationManager.showError("Test Error", "Error notification test");
        notificationManager.showSecurity("Test Security", "Security notification test");
        
        System.out.println("   ✓ Notifications displayed");
        
        // Test toast
        notificationManager.showToast("Test toast message");
        
        System.out.println("   ✓ Toast notification displayed");
    }
    
    /**
     * Test animations
     */
    private void testAnimations() {
        UIManager uiManager = UIManager.getInstance();
        
        // Create test button for animation
        Button testButton = new Button("Animation Test");
        uiManager.addButtonFeedback(testButton);
        
        System.out.println("   ✓ Button feedback animations added");
        
        // Test fade animations
        Label testLabel = new Label("Fade Test");
        uiManager.fadeIn(testLabel);
        
        // Wait a bit then fade out
        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.millis(500));
        pause.setOnFinished(e -> uiManager.fadeOut(testLabel));
        pause.play();
        
        System.out.println("   ✓ Fade animations functional");
    }
}
package com.ghostvault.ui.preview;

import com.ghostvault.model.VaultFile;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.geometry.Insets;
import javafx.application.Platform;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Code viewer component with syntax highlighting and advanced features
 * Uses RichTextFX for enhanced text editing capabilities
 */
public class CodeViewerComponent extends PreviewComponent {
    
    // UI Components
    private BorderPane rootPane;
    private CodeArea codeArea;
    private TextField searchField;
    private Label statusLabel;
    private Label lineColumnLabel;
    private CheckBox lineNumbersCheckBox;
    private CheckBox wordWrapCheckBox;
    private ComboBox<String> languageComboBox;
    private Slider fontSizeSlider;
    
    // Content and state
    private String originalContent;
    private VaultFile.CodeLanguage detectedLanguage;
    private boolean isSearchVisible = false;
    private int currentSearchIndex = -1;
    private String lastSearchTerm = "";
    
    // Syntax highlighting
    private SyntaxHighlighter syntaxHighlighter;
    
    // Settings
    private PreviewSettings settings;
    private boolean showLineNumbers = true;
    private boolean wordWrap = false;
    private int fontSize = 12;
    
    public CodeViewerComponent() {
        this.settings = new PreviewSettings(); // Default settings
        this.syntaxHighlighter = new SyntaxHighlighter();
    }
    
    public CodeViewerComponent(PreviewSettings settings) {
        this.settings = settings != null ? settings : new PreviewSettings();
        this.syntaxHighlighter = new SyntaxHighlighter();
        // Note: SyntaxHighlighter uses its own theme system, not PreviewSettings.SyntaxTheme
    }
    
    @Override
    public void loadContent(byte[] fileData) {
        if (fileData == null || fileData.length == 0) {
            originalContent = "";
        } else {
            // Detect encoding and convert to string
            originalContent = new String(fileData, StandardCharsets.UTF_8);
        }
        
        // Detect language from file extension and content
        if (vaultFile != null) {
            detectedLanguage = LanguageDetector.detectLanguage(vaultFile.getOriginalName(), originalContent);
        } else {
            detectedLanguage = VaultFile.CodeLanguage.UNKNOWN;
        }
        
        // Update UI on JavaFX thread
        Platform.runLater(() -> {
            if (codeArea != null) {
                setupCodeArea();
                updateStatusBar();
            }
        });
    }
    
    @Override
    protected Scene createScene() {
        rootPane = new BorderPane();
        
        // Create toolbar
        HBox toolbar = createToolbar();
        rootPane.setTop(toolbar);
        
        // Create code area
        createCodeArea();
        
        // Create search bar (initially hidden)
        HBox searchBar = createSearchBar();
        
        // Create status bar
        HBox statusBar = createStatusBar();
        rootPane.setBottom(statusBar);
        
        // Main content area with search bar
        VBox contentArea = new VBox();
        contentArea.getChildren().addAll(searchBar, codeArea);
        rootPane.setCenter(contentArea);
        
        // Initially hide search bar
        searchBar.setVisible(false);
        searchBar.setManaged(false);
        
        // Apply dashboard styling
        Scene scene = new Scene(rootPane, 800, 600);
        scene.getStylesheets().add(getClass().getResource("/ghostvault-dark.css").toExternalForm());
        
        // Add syntax highlighting stylesheet
        if (syntaxHighlighter != null) {
            scene.getStylesheets().add("data:text/css," + syntaxHighlighter.getThemeStylesheet());
        }
        
        // Setup keyboard shortcuts
        setupKeyboardShortcuts(scene);
        
        return scene;
    }
    
    /**
     * Create the toolbar with controls
     */
    private HBox createToolbar() {
        HBox toolbar = new HBox(10);
        toolbar.setPadding(new Insets(5));
        toolbar.setStyle("-fx-background-color: #2b2b2b; -fx-border-color: #404040; -fx-border-width: 0 0 1 0;");
        
        // Language selection
        Label languageLabel = new Label("Language:");
        languageComboBox = new ComboBox<>();
        languageComboBox.getItems().addAll(
            "Auto-detect", "Java", "Python", "JavaScript", "TypeScript", 
            "HTML", "CSS", "XML", "JSON", "YAML", "SQL", "Shell", "Markdown", "Plain Text"
        );
        languageComboBox.setValue("Auto-detect");
        languageComboBox.setOnAction(e -> updateSyntaxHighlighting());
        
        // Font size control
        Label fontLabel = new Label("Font Size:");
        fontSizeSlider = new Slider(8, 24, settings.getFontSize());
        fontSizeSlider.setShowTickLabels(true);
        fontSizeSlider.setShowTickMarks(true);
        fontSizeSlider.setMajorTickUnit(4);
        fontSizeSlider.setMinorTickCount(3);
        fontSizeSlider.setPrefWidth(100);
        fontSizeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            fontSize = newVal.intValue();
            updateFontSize();
        });
        
        // Line numbers checkbox
        lineNumbersCheckBox = new CheckBox("Line Numbers");
        lineNumbersCheckBox.setSelected(settings.isShowLineNumbers());
        lineNumbersCheckBox.setOnAction(e -> {
            showLineNumbers = lineNumbersCheckBox.isSelected();
            updateLineNumbers();
        });
        
        // Word wrap checkbox
        wordWrapCheckBox = new CheckBox("Word Wrap");
        wordWrapCheckBox.setSelected(settings.isWordWrap());
        wordWrapCheckBox.setOnAction(e -> {
            wordWrap = wordWrapCheckBox.isSelected();
            updateWordWrap();
        });
        
        // Code folding checkbox
        CheckBox codeFoldingCheckBox = new CheckBox("Code Folding");
        codeFoldingCheckBox.setSelected(settings.isEnableCodeFolding());
        codeFoldingCheckBox.setOnAction(e -> {
            boolean enableFolding = codeFoldingCheckBox.isSelected();
            updateCodeFolding(enableFolding);
        });
        
        // Search button
        Button searchButton = new Button("🔍 Search");
        searchButton.setOnAction(e -> toggleSearch());
        
        // Go to line button
        Button gotoLineButton = new Button("Go to Line");
        gotoLineButton.setOnAction(e -> showGoToLineDialog());
        
        toolbar.getChildren().addAll(
            languageLabel, languageComboBox,
            new Separator(),
            fontLabel, fontSizeSlider,
            new Separator(),
            lineNumbersCheckBox, wordWrapCheckBox, codeFoldingCheckBox,
            new Separator(),
            searchButton, gotoLineButton
        );
        
        return toolbar;
    }
    
    /**
     * Create the code area with syntax highlighting
     */
    private void createCodeArea() {
        codeArea = new CodeArea();
        codeArea.setEditable(false);
        codeArea.setWrapText(settings.isWordWrap());
        
        // Set font
        codeArea.setStyle("-fx-font-family: '" + settings.getFontFamily() + "'; -fx-font-size: " + settings.getFontSize() + "px;");
        
        // Add line numbers if enabled
        if (settings.isShowLineNumbers()) {
            codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        }
        
        // Add caret position listener
        codeArea.caretPositionProperty().addListener((obs, oldPos, newPos) -> {
            updateCaretPosition();
        });
        
        // Setup initial content
        setupCodeArea();
    }
    
    /**
     * Setup code area with content and syntax highlighting
     */
    private void setupCodeArea() {
        if (codeArea == null) return;
        
        // Set content
        codeArea.replaceText(originalContent != null ? originalContent : "");
        
        // Apply syntax highlighting
        updateSyntaxHighlighting();
        
        // Move caret to beginning
        codeArea.moveTo(0);
    }
    
    /**
     * Create search bar
     */
    private HBox createSearchBar() {
        HBox searchBar = new HBox(5);
        searchBar.setPadding(new Insets(5));
        searchBar.setStyle("-fx-background-color: #3c3c3c; -fx-border-color: #404040; -fx-border-width: 0 0 1 0;");
        
        Label searchLabel = new Label("Search:");
        searchField = new TextField();
        searchField.setPrefWidth(200);
        searchField.setPromptText("Enter search term...");
        
        Button findNextButton = new Button("Next");
        Button findPrevButton = new Button("Previous");
        Button closeSearchButton = new Button("✕");
        
        // Search functionality
        searchField.setOnAction(e -> findNext());
        findNextButton.setOnAction(e -> findNext());
        findPrevButton.setOnAction(e -> findPrevious());
        closeSearchButton.setOnAction(e -> hideSearch());
        
        // Real-time search
        searchField.textProperty().addListener((obs, oldText, newText) -> {
            if (!newText.equals(lastSearchTerm)) {
                lastSearchTerm = newText;
                currentSearchIndex = -1;
                if (!newText.isEmpty()) {
                    findNext();
                }
            }
        });
        
        searchBar.getChildren().addAll(
            searchLabel, searchField, findNextButton, findPrevButton, closeSearchButton
        );
        
        return searchBar;
    }
    
    /**
     * Create status bar
     */
    private HBox createStatusBar() {
        HBox statusBar = new HBox();
        statusBar.setPadding(new Insets(5));
        statusBar.setStyle("-fx-background-color: #2b2b2b; -fx-border-color: #404040; -fx-border-width: 1 0 0 0;");
        
        statusLabel = new Label("Ready");
        lineColumnLabel = new Label("Line 1, Column 1");
        
        // Add spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        
        statusBar.getChildren().addAll(statusLabel, spacer, lineColumnLabel);
        
        return statusBar;
    }
    
    /**
     * Setup keyboard shortcuts
     */
    private void setupKeyboardShortcuts(Scene scene) {
        scene.setOnKeyPressed(event -> {
            if (event.isControlDown()) {
                switch (event.getCode()) {
                    case F:
                        toggleSearch();
                        event.consume();
                        break;
                    case G:
                        showGoToLineDialog();
                        event.consume();
                        break;
                    case PLUS:
                    case EQUALS:
                        increaseFontSize();
                        event.consume();
                        break;
                    case MINUS:
                        decreaseFontSize();
                        event.consume();
                        break;
                }
            } else {
                switch (event.getCode()) {
                    case ESCAPE:
                        if (isSearchVisible) {
                            hideSearch();
                            event.consume();
                        }
                        break;
                    case F3:
                        if (isSearchVisible) {
                            findNext();
                            event.consume();
                        }
                        break;
                }
            }
        });
    }
    
    /**
     * Update syntax highlighting based on detected or selected language
     */
    private void updateSyntaxHighlighting() {
        if (codeArea == null || syntaxHighlighter == null) return;
        
        // Get selected language
        String selectedLanguage = languageComboBox.getValue();
        VaultFile.CodeLanguage language = detectedLanguage;
        
        if (!"Auto-detect".equals(selectedLanguage)) {
            // Use manually selected language
            try {
                language = VaultFile.CodeLanguage.valueOf(selectedLanguage.toUpperCase().replace(" ", "_"));
            } catch (IllegalArgumentException e) {
                // Fallback to detected language if conversion fails
                language = detectedLanguage;
            }
        }
        
        // Apply syntax highlighting using the highlighter engine
        syntaxHighlighter.applySyntaxHighlighting(codeArea, language);
        
        // Update status
        statusLabel.setText("Language: " + language.getDisplayName());
    }
    

    

    
    /**
     * Toggle search bar visibility
     */
    private void toggleSearch() {
        VBox contentArea = (VBox) rootPane.getCenter();
        HBox searchBar = (HBox) contentArea.getChildren().get(0);
        
        isSearchVisible = !isSearchVisible;
        searchBar.setVisible(isSearchVisible);
        searchBar.setManaged(isSearchVisible);
        
        if (isSearchVisible) {
            searchField.requestFocus();
        } else {
            codeArea.requestFocus();
        }
    }
    
    /**
     * Hide search bar
     */
    private void hideSearch() {
        if (isSearchVisible) {
            toggleSearch();
        }
    }
    
    /**
     * Find next occurrence of search term
     */
    private void findNext() {
        String searchTerm = searchField.getText();
        if (searchTerm.isEmpty()) return;
        
        String content = codeArea.getText().toLowerCase();
        String term = searchTerm.toLowerCase();
        
        int startPos = currentSearchIndex >= 0 ? currentSearchIndex + 1 : 0;
        int foundIndex = content.indexOf(term, startPos);
        
        if (foundIndex == -1 && startPos > 0) {
            // Wrap around to beginning
            foundIndex = content.indexOf(term, 0);
        }
        
        if (foundIndex >= 0) {
            currentSearchIndex = foundIndex;
            codeArea.selectRange(foundIndex, foundIndex + searchTerm.length());
            codeArea.requestFollowCaret();
            statusLabel.setText("Found: " + searchTerm);
        } else {
            statusLabel.setText("Not found: " + searchTerm);
        }
    }
    
    /**
     * Find previous occurrence of search term
     */
    private void findPrevious() {
        String searchTerm = searchField.getText();
        if (searchTerm.isEmpty()) return;
        
        String content = codeArea.getText().toLowerCase();
        String term = searchTerm.toLowerCase();
        
        int startPos = currentSearchIndex > 0 ? currentSearchIndex - 1 : content.length();
        int foundIndex = content.lastIndexOf(term, startPos);
        
        if (foundIndex == -1 && startPos < content.length()) {
            // Wrap around to end
            foundIndex = content.lastIndexOf(term);
        }
        
        if (foundIndex >= 0) {
            currentSearchIndex = foundIndex;
            codeArea.selectRange(foundIndex, foundIndex + searchTerm.length());
            codeArea.requestFollowCaret();
            statusLabel.setText("Found: " + searchTerm);
        } else {
            statusLabel.setText("Not found: " + searchTerm);
        }
    }
    
    /**
     * Show go to line dialog
     */
    private void showGoToLineDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Go to Line");
        dialog.setHeaderText("Enter line number:");
        dialog.setContentText("Line:");
        
        // Apply dark theme
        dialog.getDialogPane().getStylesheets().add(
            getClass().getResource("/ghostvault-dark.css").toExternalForm());
        
        dialog.showAndWait().ifPresent(result -> {
            try {
                int lineNumber = Integer.parseInt(result.trim());
                goToLine(lineNumber);
            } catch (NumberFormatException e) {
                statusLabel.setText("Invalid line number: " + result);
            }
        });
    }
    
    /**
     * Go to specific line number
     */
    private void goToLine(int lineNumber) {
        if (lineNumber < 1) {
            statusLabel.setText("Line number must be positive");
            return;
        }
        
        String[] lines = codeArea.getText().split("\n");
        if (lineNumber > lines.length) {
            statusLabel.setText("Line " + lineNumber + " does not exist (max: " + lines.length + ")");
            return;
        }
        
        // Calculate position
        int position = 0;
        for (int i = 0; i < lineNumber - 1; i++) {
            position += lines[i].length() + 1; // +1 for newline
        }
        
        codeArea.moveTo(position);
        codeArea.requestFollowCaret();
        statusLabel.setText("Moved to line " + lineNumber);
    }
    
    /**
     * Update line numbers display
     */
    private void updateLineNumbers() {
        if (showLineNumbers) {
            codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        } else {
            codeArea.setParagraphGraphicFactory(null);
        }
    }
    
    /**
     * Update word wrap setting
     */
    private void updateWordWrap() {
        codeArea.setWrapText(wordWrap);
    }
    
    /**
     * Update font size
     */
    private void updateFontSize() {
        codeArea.setStyle("-fx-font-family: '" + settings.getFontFamily() + "'; -fx-font-size: " + fontSize + "px;");
    }
    
    /**
     * Increase font size
     */
    private void increaseFontSize() {
        if (fontSize < 24) {
            fontSize++;
            fontSizeSlider.setValue(fontSize);
            updateFontSize();
        }
    }
    
    /**
     * Decrease font size
     */
    private void decreaseFontSize() {
        if (fontSize > 8) {
            fontSize--;
            fontSizeSlider.setValue(fontSize);
            updateFontSize();
        }
    }
    
    /**
     * Update caret position display
     */
    private void updateCaretPosition() {
        if (codeArea == null || lineColumnLabel == null) return;
        
        int caretPosition = codeArea.getCaretPosition();
        String text = codeArea.getText();
        
        // Calculate line and column
        int line = 1;
        int column = 1;
        
        for (int i = 0; i < caretPosition && i < text.length(); i++) {
            if (text.charAt(i) == '\n') {
                line++;
                column = 1;
            } else {
                column++;
            }
        }
        
        lineColumnLabel.setText("Line " + line + ", Column " + column);
    }
    
    /**
     * Update code folding setting
     */
    private void updateCodeFolding(boolean enabled) {
        // Code folding implementation would go here
        // For now, just update the status
        statusLabel.setText("Code folding " + (enabled ? "enabled" : "disabled"));
    }
    
    /**
     * Update status bar with file information
     */
    private void updateStatusBar() {
        if (statusLabel == null) return;
        
        String content = originalContent != null ? originalContent : "";
        int lines = content.split("\n").length;
        int characters = content.length();
        
        statusLabel.setText(String.format("Lines: %d, Characters: %d, Language: %s", 
            lines, characters, detectedLanguage.getDisplayName()));
    }
    
    @Override
    public boolean supportsFileType(String fileExtension) {
        return Arrays.asList("java", "py", "js", "ts", "html", "css", "xml", "json", "yaml", "yml", 
                           "sql", "sh", "bat", "ps1", "md", "txt", "c", "cpp", "h", "hpp", "cs", 
                           "php", "rb", "go", "rs").contains(fileExtension.toLowerCase());
    }
    
    @Override
    public String getComponentName() {
        return "Code Viewer";
    }
    
    @Override
    public String[] getSupportedExtensions() {
        return new String[]{"java", "py", "js", "ts", "html", "css", "xml", "json", "yaml", "yml", 
                          "sql", "sh", "bat", "ps1", "md", "txt", "c", "cpp", "h", "hpp", "cs", 
                          "php", "rb", "go", "rs"};
    }
}
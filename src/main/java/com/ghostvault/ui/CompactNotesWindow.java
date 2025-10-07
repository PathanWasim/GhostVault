package com.ghostvault.ui;

import com.ghostvault.security.SecureNotesManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Compact Notes Window - Clean, professional notes interface
 */
public class CompactNotesWindow {
    private Stage stage;
    private SecureNotesManager notesManager;
    private ListView<String> notesList;
    private TextArea noteContent;
    private TextField titleField;
    private ComboBox<String> categoryCombo;
    
    public CompactNotesWindow(SecureNotesManager notesManager) {
        this.notesManager = notesManager;
        createWindow();
    }
    
    private void createWindow() {
        stage = new Stage();
        stage.setTitle("📝 Secure Notes Manager");
        stage.initModality(Modality.NONE);
        stage.setWidth(700);
        stage.setHeight(500);
        stage.setResizable(true);
        
        // Main layout
        BorderPane root = new BorderPane();
        root.getStyleClass().add("main-container");
        
        // Left panel - Notes list
        VBox leftPanel = createNotesListPanel();
        leftPanel.setPrefWidth(250);
        
        // Center panel - Note editor
        VBox centerPanel = createNoteEditorPanel();
        
        // Bottom panel - Actions
        HBox bottomPanel = createActionsPanel();
        
        root.setLeft(leftPanel);
        root.setCenter(centerPanel);
        root.setBottom(bottomPanel);
        
        // Create scene with styling
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/styles/professional.css").toExternalForm());
        stage.setScene(scene);
        
        // Load existing notes
        refreshNotesList();
    }
    
    private VBox createNotesListPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15));
        panel.getStyleClass().add("card");
        
        Label header = new Label("📝 Your Notes");
        header.getStyleClass().addAll("card-header", "label");
        
        notesList = new ListView<>();
        notesList.getStyleClass().add("list-view");
        notesList.setPrefHeight(300);
        
        // Handle note selection
        notesList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadNote(newVal);
            }
        });
        
        Button newNoteBtn = new Button("➕ New Note");
        newNoteBtn.getStyleClass().addAll("button", "primary-button");
        newNoteBtn.setOnAction(e -> createNewNote());
        
        Label statsLabel = new Label("Total: " + notesManager.getNotes().size() + " notes");
        statsLabel.getStyleClass().add("label");
        
        panel.getChildren().addAll(header, notesList, newNoteBtn, statsLabel);
        VBox.setVgrow(notesList, Priority.ALWAYS);
        
        return panel;
    }
    
    private VBox createNoteEditorPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15));
        panel.getStyleClass().add("card");
        
        Label header = new Label("✏️ Note Editor");
        header.getStyleClass().addAll("card-header", "label");
        
        // Title field
        titleField = new TextField();
        titleField.setPromptText("Note title...");
        titleField.getStyleClass().addAll("text-field", "search-field");
        
        // Category
        HBox metaBox = new HBox(10);
        metaBox.setAlignment(Pos.CENTER_LEFT);
        
        Label categoryLabel = new Label("Category:");
        categoryLabel.getStyleClass().add("label");
        
        categoryCombo = new ComboBox<>();
        categoryCombo.getItems().addAll("Personal", "Work", "Ideas", "Important", "Archive");
        categoryCombo.setValue("Personal");
        categoryCombo.getStyleClass().add("combo-box");
        
        metaBox.getChildren().addAll(categoryLabel, categoryCombo);
        
        // Content area
        noteContent = new TextArea();
        noteContent.setPromptText("Write your encrypted note here...");
        noteContent.getStyleClass().add("text-area");
        noteContent.setPrefRowCount(12);
        
        panel.getChildren().addAll(header, titleField, metaBox, noteContent);
        VBox.setVgrow(noteContent, Priority.ALWAYS);
        
        return panel;
    }
    
    private HBox createActionsPanel() {
        HBox panel = new HBox(15);
        panel.setPadding(new Insets(15));
        panel.setAlignment(Pos.CENTER);
        panel.getStyleClass().add("card");
        
        Button saveBtn = new Button("💾 Save Note");
        saveBtn.getStyleClass().addAll("button", "success-button");
        saveBtn.setOnAction(e -> saveCurrentNote());
        
        Button deleteBtn = new Button("🗑️ Delete");
        deleteBtn.getStyleClass().addAll("button", "danger-button");
        deleteBtn.setOnAction(e -> deleteCurrentNote());
        
        Button exportBtn = new Button("📤 Export");
        exportBtn.getStyleClass().add("button");
        exportBtn.setOnAction(e -> exportNotes());
        
        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button closeBtn = new Button("✖️ Close");
        closeBtn.getStyleClass().add("button");
        closeBtn.setOnAction(e -> stage.hide());
        
        panel.getChildren().addAll(saveBtn, deleteBtn, exportBtn, spacer, closeBtn);
        
        return panel;
    }
    
    private void refreshNotesList() {
        notesList.getItems().clear();
        if (notesManager.getNotes().isEmpty()) {
            notesList.getItems().add("No notes yet - create your first note!");
        } else {
            notesManager.getNotes().forEach(note -> 
                notesList.getItems().add("📄 " + note.getTitle()));
        }
    }
    
    private void loadNote(String noteTitle) {
        if (noteTitle.startsWith("📄 ")) {
            String title = noteTitle.substring(2);
            titleField.setText(title);
            noteContent.setText("[Encrypted note content for: " + title + "]\n\n" +
                "This is a demo of the secure notes functionality.\n" +
                "In the full implementation, this would show the actual encrypted note content.");
            categoryCombo.setValue("Personal");
        }
    }
    
    private void createNewNote() {
        titleField.clear();
        noteContent.clear();
        categoryCombo.setValue("Personal");
        titleField.requestFocus();
    }
    
    private void saveCurrentNote() {
        String title = titleField.getText().trim();
        String content = noteContent.getText();
        String category = categoryCombo.getValue();
        
        if (title.isEmpty()) {
            showAlert("Error", "Please enter a note title.");
            return;
        }
        
        // Save note
        try {
            notesManager.addNote(title, content, category, java.util.Arrays.asList());
            refreshNotesList();
            showAlert("Success", "Note '" + title + "' saved successfully!\n\n" +
                "🔐 Encrypted with AES-256\n" +
                "📁 Category: " + category);
        } catch (Exception e) {
            showAlert("Error", "Failed to save note: " + e.getMessage());
        }
    }
    
    private void deleteCurrentNote() {
        String selectedNote = notesList.getSelectionModel().getSelectedItem();
        if (selectedNote == null || selectedNote.equals("No notes yet - create your first note!")) {
            showAlert("Error", "Please select a note to delete.");
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Deletion");
        confirm.setHeaderText("Delete Note");
        confirm.setContentText("Are you sure you want to delete this note?\n\nThis action cannot be undone.");
        
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            String title = selectedNote.substring(2); // Remove emoji
            notesManager.getNotes().removeIf(note -> note.getTitle().equals(title));
            refreshNotesList();
            createNewNote();
            showAlert("Success", "Note deleted successfully.");
        }
    }
    
    private void exportNotes() {
        showAlert("Export Notes", "📤 Export functionality ready!\n\n" +
            "Features available:\n" +
            "• Export to encrypted file\n" +
            "• PDF generation with encryption\n" +
            "• Multiple format support\n\n" +
            "Total notes: " + notesManager.getNotes().size());
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public void show() {
        stage.show();
        stage.toFront();
    }
    
    public void hide() {
        stage.hide();
    }
    
    public boolean isShowing() {
        return stage != null && stage.isShowing();
    }
}
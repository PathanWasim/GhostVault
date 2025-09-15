package com.ghostvault.ui;

import com.ghostvault.core.FileManager;
import com.ghostvault.core.MetadataManager;
import com.ghostvault.model.VaultFile;
import com.ghostvault.util.FileUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.crypto.SecretKey;
import java.io.File;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Comprehensive file management interface for the real vault
 */
public class FileManagementInterface {
    
    private Stage primaryStage;
    private FileManager fileManager;
    private MetadataManager metadataManager;
    private SecretKey encryptionKey;
    
    private TableView<VaultFile> fileTable;
    private ObservableList<VaultFile> fileList;
    private TextField searchField;
    private Label statusLabel;
    private Label statsLabel;
    private ProgressBar operationProgress;
    private Label progressLabel;
    
    public FileManagementInterface(Stage primaryStage, FileManager fileManager, 
                                 MetadataManager metadataManager, SecretKey encryptionKey) {
        this.primaryStage = primaryStage;
        this.fileManager = fileManager;
        this.metadataManager = metadataManager;
        this.encryptionKey = encryptionKey;
        this.fileList = FXCollections.observableArrayList();
        
        refreshFileList();
    }
    
    /**
     * Create and show the file management interface
     */
    public Scene createFileManagementScene() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(20));
        
        // Title bar
        HBox titleBar = createTitleBar();
        
        // Toolbar
        HBox toolbar = createToolbar();
        
        // Progress section
        VBox progressSection = createProgressSection();
        
        // File table
        VBox tableSection = createFileTableSection();
        
        // Status bar
        HBox statusBar = createStatusBar();
        
        root.getChildren().addAll(titleBar, toolbar, progressSection, tableSection, statusBar);
        
    
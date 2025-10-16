# GhostVault - Build and Run Guide

## Prerequisites

- Java 17 or higher
- JavaFX 17 or higher
- Maven 3.6+ (if using Maven build)
- Git (for version control)

## Quick Start

### 1. Build the Project
```bash
# Using Maven
mvn clean compile

# Using Gradle
./gradlew build
```

### 2. Run the Application
```bash
# Using Maven
mvn javafx:run

# Using Java directly
java --module-path /path/to/javafx/lib --add-modules javafx.controls,javafx.fxml -cp target/classes com.ghostvault.ui.GhostVaultMainWindow

# Using the provided batch file (Windows)
./dist/GhostVault.bat
```

### 3. Run Tests
```bash
# Run all tests
mvn test

# Run specific test suites
mvn test -Dtest=UIComponentTestSuite
mvn test -Dtest=SecurityFeatureTests
```

## Project Structure

```
src/
├── main/java/com/ghostvault/
│   ├── ui/
│   │   ├── components/     # UI Components
│   │   ├── controllers/    # Mode Controllers
│   │   ├── utils/         # Utility Classes
│   │   └── GhostVaultMainWindow.java
│   └── resources/
│       └── css/           # Theme Files
└── test/java/com/ghostvault/
    ├── security/          # Security Tests
    └── ui/components/     # UI Tests
```

## Configuration

The application uses several configuration files:
- `modern-theme.css` - UI styling
- `UIConstants.java` - Application constants
- `UIUtils.java` - Utility methods

## Features Implemented

✅ Professional Theme System
✅ Advanced File Preview (Code, Images, Audio, Video)
✅ Enhanced File Management with Search & Filtering
✅ Modern File Operations with Progress Tracking
✅ Security & Mode Management (Master/Panic/Decoy)
✅ User Experience Enhancements (Animations, Notifications, Shortcuts)
✅ Comprehensive Testing Suite

## Next Steps

1. Integrate with existing GhostVault backend
2. Add database connectivity
3. Implement user authentication
4. Deploy to production environment

For detailed implementation information, see IMPLEMENTATION_SUMMARY.md
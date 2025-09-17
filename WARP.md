# WARP.md

This file provides guidance to WARP (warp.dev) when working with code in this repository.

## Project Overview

GhostVault is a comprehensive secure file storage application with military-grade encryption, built using JavaFX for the UI and implementing advanced security features including panic mode, decoy functionality, and secure deletion.

**Key Technologies:**
- Language: Java 17
- UI Framework: JavaFX 17.0.2
- Build Tool: Maven 3.6+
- Encryption: AES-256-CBC with PBKDF2
- Testing: JUnit 5, TestFX, Mockito
- Logging: SLF4J with Logback

## Development Commands

### Building and Running

```bash
# Clean and compile the project
mvn clean compile

# Run the application via Maven
mvn javafx:run

# Build JAR package
mvn clean package

# Run JAR directly
java -jar target/ghostvault-1.0.0.jar

# Run with specific JavaFX modules (if needed)
java --module-path /path/to/javafx/lib --add-modules javafx.controls,javafx.fxml -jar target/ghostvault-1.0.0.jar
```

### Testing

```bash
# Run all tests
mvn test

# Run specific test suites
mvn test -Dtest=SecurityValidationTest
mvn test -Dtest=PerformanceValidationTest
mvn test -Dtest=IntegrationTest

# Run with coverage report
mvn test -Ptest

# Run comprehensive test runner (standalone)
java -cp "build/classes/main:build/classes/test" com.ghostvault.ComprehensiveTestRunner

# Windows test runner
run-tests.bat

# Linux/Mac test runner
./run-tests.sh
```

### Building for Production

```bash
# Production build with optimizations
mvn clean package -Pprod

# Create native installer (Windows/Mac/Linux)
mvn jpackage:jpackage

# Create custom runtime with JLink
mvn jlink:jlink
```

### Running Individual Test Categories

```bash
# Security tests only
mvn test -Dtest=com.ghostvault.security.*Test

# Performance tests only
mvn test -Dtest=com.ghostvault.PerformanceTestFramework

# UI tests only (requires display)
mvn test -Dtest=com.ghostvault.ui.*Test -Dtestfx.headless=false
```

## Architecture Overview

### Core Package Structure

The application follows a modular architecture with clear separation of concerns:

- **com.ghostvault** - Main application entry points
  - `GhostVault.java` - Primary application launcher
  - `GhostVaultApplication.java` - JavaFX application class
  - `SecurityManager.java` - Central security orchestration

- **com.ghostvault.core** - Core business logic
  - `VaultManager` - Central vault operations controller
  - `FileManager` - File encryption/decryption operations
  - `MetadataManager` - File metadata and integrity management
  - `BackupManager` - Backup and restore functionality
  - `DecoyManager` - Decoy mode implementation
  - `VaultInitializer` - Vault setup and initialization

- **com.ghostvault.security** - Security implementations
  - `CryptoManager` - AES-256-CBC encryption engine
  - `PasswordManager` - Password hashing and validation
  - `SessionManager` - Session timeout and activity monitoring
  - `AdvancedSecurityManager` - Advanced threat detection
  - `PanicModeExecutor` - Emergency data destruction
  - `ThreatDetectionEngine` - Real-time security monitoring
  - `SecureDeletion` - DoD 5220.22-M secure file deletion

- **com.ghostvault.ui** - JavaFX UI components
  - `LoginController` - Authentication interface
  - `VaultMainController` - Main application interface
  - `FileManagementController` - File operations UI
  - `BackupRestoreController` - Backup/restore operations
  - `InitialSetupController` - First-run setup wizard
  - `UIManager` - UI state and theme management
  - `AccessibilityManager` - Accessibility features
  - `AnimationManager` - UI animations and transitions

- **com.ghostvault.audit** - Audit and logging
  - `AuditManager` - Audit trail management
  - `AuditLogger` - Encrypted logging implementation

- **com.ghostvault.exception** - Custom exceptions
  - `GhostVaultException` - Base exception class
  - `AuthenticationException` - Authentication failures
  - `CryptographicException` - Encryption/decryption errors
  - `ValidationException` - Input validation errors

### Security Architecture

#### Multi-Password System
- **Master Password**: Full vault access with all features
- **Panic Password**: Triggers immediate secure data destruction
- **Decoy Password**: Opens fake vault with decoy files

#### Encryption Implementation
- **Algorithm**: AES-256-CBC with PKCS5 padding
- **Key Derivation**: PBKDF2WithHmacSHA256 (100,000+ iterations)
- **Salt Generation**: 32-byte cryptographically secure random
- **IV Generation**: 16-byte unique per encryption operation
- **File Integrity**: SHA-256 hash verification

#### Security Features
- **Session Management**: Configurable timeout with activity monitoring
- **Memory Protection**: Secure memory wiping after operations
- **Anti-Debugging**: Protection against reverse engineering
- **Threat Detection**: Real-time anomaly detection
- **Secure Deletion**: Multi-pass overwriting (DoD 5220.22-M)

### UI Architecture

#### JavaFX Components
- **FXML-based layouts** for maintainability
- **CSS styling** with dark/light theme support
- **Responsive design** with window scaling
- **Accessibility support** including screen readers

#### Key UI Patterns
- **MVC Pattern**: Controllers separate business logic from UI
- **Event-driven**: JavaFX event system for user interactions
- **Progressive disclosure**: Complex features hidden until needed
- **Error handling**: User-friendly error dialogs with recovery options

### Data Storage

#### Vault Structure
```
~/.ghostvault/
├── config.enc          # Encrypted configuration
├── metadata.enc        # File metadata database
├── audit.log.enc       # Encrypted audit logs
├── .salt              # Password salt storage
├── files/             # Encrypted file storage
│   └── [UUID].enc     # Files stored with random UUIDs
└── decoys/            # Decoy files for plausible deniability
```

#### File Management
- Files stored with random UUID names for security
- Original filenames encrypted in metadata
- Flat storage structure (no directory preservation)
- File integrity verified on every access

## Important Files and Configurations

### Build Configuration
- `pom.xml` - Maven project configuration with all dependencies
- `pom-simple.xml` - Simplified build configuration

### Resources
- `src/main/resources/application.properties` - Application settings
- `src/main/resources/logback.xml` - Logging configuration
- `src/main/resources/icons/` - Application icons
- `src/main/resources/fxml/` - JavaFX UI layouts
- `src/main/resources/css/` - Application styling

### Test Framework
- `src/test/java/com/ghostvault/ComprehensiveTestRunner.java` - Main test orchestrator
- `src/test/java/com/ghostvault/TestSuite.java` - Unit test suite
- `src/test/java/com/ghostvault/SecurityValidationFramework.java` - Security tests
- `src/test/java/com/ghostvault/PerformanceTestFramework.java` - Performance benchmarks

### Configuration Constants
- `com.ghostvault.config.AppConfig` - Central configuration constants
  - Encryption parameters
  - UI dimensions
  - Security settings
  - File validation rules

## Development Guidelines

### Code Organization
- Follow standard Java package naming conventions
- Keep classes focused with single responsibility
- Use dependency injection for testability
- Implement proper exception handling with custom exceptions

### Security Best Practices
- Never log sensitive information (passwords, keys)
- Always use SecureRandom for cryptographic operations
- Wipe sensitive data from memory after use
- Validate all user inputs before processing
- Use constant-time comparisons for security checks

### Testing Requirements
- Write unit tests for all new features
- Include security tests for security-related code
- Performance test any code that handles large files
- Integration tests for cross-component functionality

### JavaFX Development
- Use FXML for complex layouts
- Keep controllers thin, delegate to services
- Handle all UI updates on JavaFX Application Thread
- Implement proper error handling with user feedback
- Support both keyboard and mouse navigation

## Common Development Tasks

### Adding a New Feature
1. Create feature branch from main
2. Implement feature with tests
3. Run full test suite: `mvn test`
4. Update documentation as needed
5. Create pull request with description

### Debugging
```bash
# Run with debug output
java -Dcom.ghostvault.debug=true -jar target/ghostvault-1.0.0.jar

# Enable JavaFX debugging
java -Djavafx.verbose=true -Dprism.verbose=true -jar target/ghostvault-1.0.0.jar

# Profile memory usage
java -Xmx1G -XX:+PrintGC -XX:+PrintGCDetails -jar target/ghostvault-1.0.0.jar
```

### Performance Optimization
```bash
# Run with performance flags
java -XX:+UseG1GC -Xmx1G -Xms512M -jar target/ghostvault-1.0.0.jar

# JavaFX performance tuning
java -Djavafx.animation.fullspeed=true -Dprism.lcdtext=false -jar target/ghostvault-1.0.0.jar
```

## Environment Variables and Properties

### System Properties
```bash
# Vault location override
-Dghostault.vault.dir=/custom/path

# Session timeout (minutes)
-Dghostault.session.timeout=30

# Debug mode
-Dcom.ghostvault.debug=true

# Headless testing
-Dtestfx.headless=true
```

### Application Properties (application.properties)
```properties
app.name=GhostVault
app.version=1.0.0
vault.location=${user.home}/.ghostvault
encryption.algorithm=AES
encryption.keysize=256
encryption.iterations=100000
panic.overwrite.passes=3
duress.max.attempts=3
```

## Troubleshooting

### Build Issues
```bash
# Clean Maven cache
mvn dependency:purge-local-repository

# Force update dependencies
mvn clean install -U

# Skip tests during build
mvn clean package -DskipTests
```

### JavaFX Issues
```bash
# Module not found errors
java --add-modules javafx.controls,javafx.fxml -jar ghostvault.jar

# Graphics rendering issues
java -Dprism.order=sw -jar ghostvault.jar

# High DPI display issues
java -Dglass.gtk.uiScale=2.0 -jar ghostvault.jar
```

### Test Failures
```bash
# Run tests with more memory
mvn test -DargLine="-Xmx1G"

# Run tests in debug mode
mvn -Dmaven.surefire.debug test

# Generate test reports
mvn surefire-report:report
```

## Security Considerations

### Sensitive Operations
- Password operations use `char[]` arrays, not Strings
- Memory is wiped after cryptographic operations
- Timing-resistant comparisons for authentication
- No sensitive data in logs or stack traces

### Threat Model Protection
- **Data at rest**: AES-256 encryption
- **Memory attacks**: Secure memory wiping
- **Timing attacks**: Constant-time operations
- **Forensic analysis**: Secure file deletion
- **Coercion**: Panic and decoy modes

## Performance Benchmarks

### Expected Metrics
- **Encryption**: 50-100 MB/s for typical files
- **Key derivation**: <500ms per operation
- **File operations**: >10 files/second
- **Memory usage**: <100MB for standard operations
- **Startup time**: <3 seconds on modern hardware

### Optimization Targets
- Minimize garbage collection impact
- Stream large files to avoid memory issues
- Use background threads for I/O operations
- Cache frequently accessed metadata
# GhostVault

![Java](https://img.shields.io/badge/language-Java-blue.svg) ![Platform](https://img.shields.io/badge/platform-Windows%20%7C%20Linux%20%7C%20macOS-green.svg) ![Build](https://img.shields.io/badge/build-Maven-blue)

**A modern, secure, and cross-platform file vault with panic mode, decoy vault, and strong encryption.**

---

## Table of Contents
- [Project Structure](#project-structure)
- [Setup Instructions](#step-by-step-setup-instructions)
- [Build & Run](#first-build-and-run)
- [Troubleshooting](#troubleshooting-common-issues)
- [Configuration Files](#configuration-files-optional)
- [IDE Setup](#ide-setup)
- [Deployment Options](#deployment-options)
- [Security Best Practices](#security-best-practices)
- [Getting Help](#getting-help)
- [Quick Command Reference](#quick-command-reference)
- [Legal Notice](#legal-notice)

---

## Project Structure

```text
GhostVault/
│
├── pom.xml            # Maven configuration
├── README.md          # Documentation
├── build.sh           # Unix/Linux/Mac build script
├── build.bat          # Windows build script
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── ghostvault/
│   │   │           ├── GhostVault.java      # Main application
│   │   │           └── SecurityManager.java # Security utilities
│   │   └── resources/
│   │       ├── icon.png        # App icon (optional)
│   │       ├── icon.ico        # Windows icon (optional)
│   │       └── logback.xml     # Logging config (optional)
│   └── test/
│       └── java/
│           └── com/
│               └── ghostvault/
│                   └── GhostVaultTest.java # Test suite
└── target/             # Build output
    └── (build artifacts)
```

## Step-by-Step Setup Instructions

### 1. Prerequisites Installation

#### Windows
- Download Java 17+ from [Adoptium](https://adoptium.net/)
- Download Maven from [Apache Maven](https://maven.apache.org/download.cgi)
- Add Java and Maven to your PATH environment variable

#### macOS
```bash
# Using Homebrew
brew install openjdk@17
brew install maven

# Set JAVA_HOME
export JAVA_HOME=/usr/local/opt/openjdk@17
```

#### Linux (Ubuntu/Debian)
```bash
# Install Java 17
sudo apt update
sudo apt install openjdk-17-jdk

# Install Maven
sudo apt install maven

# Verify installations
java -version
mvn -version
```

---

### 2. Project Setup

1. **Create project directory:**
    ```bash
    mkdir GhostVault
    cd GhostVault
    ```

2. **Create directory structure:**
    ```bash
    # Unix/Linux/Mac
    mkdir -p src/main/java/com/ghostvault
    mkdir -p src/main/resources
    mkdir -p src/test/java/com/ghostvault

    # Windows
    mkdir src\main\java\com\ghostvault
    mkdir src\main\resources
    mkdir src\test\java\com\ghostvault
    ```

3. **Copy the provided files:**
    - `pom.xml` to project root
    - `GhostVault.java` to `src/main/java/com/ghostvault/`
    - `SecurityManager.java` to `src/main/java/com/ghostvault/`
    - `GhostVaultTest.java` to `src/test/java/com/ghostvault/`
    - Build scripts to project root

4. **Make scripts executable (Unix/Linux/Mac):**
    ```bash
    chmod +x build.sh
    ```

---

### 3. First Build and Run

#### Option A: Using Build Scripts

**Windows:**
```batch
build.bat
REM Then select option 6 for Quick Start
```

**Unix/Linux/Mac:**
```bash
./build.sh
# Then select option 7 for Quick Start
```

---

#### Option B: Using Maven Directly

```bash
# Download dependencies
mvn dependency:resolve

# Compile the project
mvn clean compile

# Run the application
mvn javafx:run

# Run tests
mvn test

# Create executable JAR
mvn clean package
```

---

### 4. Running the Application

After building, you can run GhostVault in several ways:

- **Using Maven:**
  ```bash
  mvn javafx:run
  ```
- **Using JAR file:**
  ```bash
  java -jar target/ghostvault-1.0.0-jar-with-dependencies.jar
  ```
- **Using build scripts:**
  - Run `build.sh` (Unix/Linux/Mac) or `build.bat` (Windows)
  - Select option 2 to run the application

---

## Troubleshooting Common Issues

### Issue: JavaFX not found
**Solution:** The pom.xml includes JavaFX dependencies. Run:
```bash
mvn clean compile
```

### Issue: Module not found errors
**Solution:** Ensure you're using Java 17 or higher:
```bash
java -version
```

### Issue: Maven not found
**Solution:** Install Maven or use the Maven wrapper:
```bash
# Download Maven wrapper
curl -o mvnw https://raw.githubusercontent.com/takari/maven-wrapper/master/mvnw
chmod +x mvnw
./mvnw clean compile
```

### Issue: Application won't start on Linux
**Solution:** Install JavaFX runtime:
```bash
sudo apt install openjfx
```

### Issue: Tests fail on first run
**Solution:** This is normal as the test vault is created. Run tests again:
```bash
mvn test
```

## Configuration Files (Optional)

### Logging Configuration (src/main/resources/logback.xml)
```xml
<configuration>
    <appender name="FILE" class="ch.qos.logback.core.FileAppender">
        <file>ghostvault.log</file>
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <root level="INFO">
        <appender-ref ref="FILE" />
        <appender-ref ref="STDOUT" />
    </root>
</configuration>
```

### Application Properties (src/main/resources/application.properties)
```properties
# GhostVault Configuration
app.name=GhostVault
app.version=1.0.0
vault.location=${user.home}/.ghostvault
encryption.algorithm=AES
encryption.keysize=256
encryption.iterations=100000
panic.overwrite.passes=3
duress.max.attempts=3
```

## IDE Setup

### IntelliJ IDEA
1. Open IntelliJ IDEA
2. Select "Open" and choose the project directory
3. IntelliJ will automatically detect the Maven project
4. Right-click on `GhostVault.java` and select "Run"

### Eclipse
1. Open Eclipse
2. File → Import → Maven → Existing Maven Projects
3. Select the project directory
4. Right-click on project → Run As → Java Application

### Visual Studio Code
1. Install Java Extension Pack
2. Open the project folder
3. Open `GhostVault.java`
4. Click "Run" above the main method

## Deployment Options

### 1. Standalone JAR
```bash
mvn clean package
# Distribute: target/ghostvault-1.0.0-jar-with-dependencies.jar
```

### 2. Native Installer (requires Java 14+)
```bash
mvn clean compile javafx:jlink
mvn jpackage:jpackage
# Installer in: target/dist/
```

### 3. Docker Container
Create `Dockerfile`:
```dockerfile
FROM openjdk:17-jdk-slim
COPY target/ghostvault-1.0.0.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

Build and run:
```bash
docker build -t ghostvault .
docker run -it ghostvault
```

## Security Best Practices

1. **Never commit sensitive data:**
   - Add `.ghostvault/` to `.gitignore`
   - Never commit passwords or keys

2. **Production deployment:**
   - Use environment variables for configuration
   - Enable security manager
   - Implement rate limiting
   - Add intrusion detection

3. **Regular updates:**
   ```bash
   mvn versions:display-dependency-updates
   mvn dependency:analyze
   ```

## Getting Help

- Check the README.md for usage instructions
- Run tests to verify functionality: `mvn test`
- Review logs in `~/.ghostvault/audit.log.enc`
- For issues, check Java and JavaFX versions

## Quick Command Reference

```bash
# Build
mvn clean compile

# Run
mvn javafx:run

# Test
mvn test

# Package
mvn package

# Clean
mvn clean

# Check for updates
mvn versions:display-dependency-updates

# Dependency tree
mvn dependency:tree

# Run specific test
mvn test -Dtest=GhostVaultTest#testEncryption

# Skip tests during build
mvn package -DskipTests
```

## Legal Notice

This software is for legitimate privacy protection only. Users must comply with all applicable laws and regulations. Do not use for illegal purposes.

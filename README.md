# 🔐 GhostVault

**Military-grade file encryption system with AI-powered organization and secure credential management**

![Java](https://img.shields.io/badge/Java-17+-orange.svg)
![JavaFX](https://img.shields.io/badge/JavaFX-17+-blue.svg)
![Status](https://img.shields.io/badge/Status-Production%20Ready-brightgreen.svg)
![License](https://img.shields.io/badge/License-MIT-blue.svg)

## 🌟 Features

### 🔒 **File Encryption & Security**
- **AES-256-GCM Encryption** - Military-grade file protection
- **Drag & Drop Interface** - Encrypt files instantly by dropping them
- **Batch Operations** - Handle multiple files simultaneously
- **Secure File Preview** - View encrypted files without decryption
- **Zero-Knowledge Architecture** - Your data, your keys only
- **Memory Protection** - Automatic cleanup of sensitive data

### 📝 **Secure Notes Manager**
- **Encrypted Note Storage** - AES-256 protected notes with categories
- **Real-time Search** - Find notes instantly as you type
- **Auto-tagging** - Smart content-based tagging system
- **Rich Categories** - Personal, Work, Ideas, Important, Archive
- **Export Options** - Secure backup in encrypted formats
- **Full-text Search** - Search through note titles and content

### 🔑 **Password Manager**
- **Encrypted Credential Storage** - Secure login management
- **Password Generator** - Create strong, unique passwords (16+ chars)
- **Security Audit** - Real-time analysis of password strength
- **Breach Detection** - Monitor for compromised credentials
- **Category Management** - Banking, Social, Work, Shopping, etc.
- **Auto-fill Ready** - Easy password retrieval and copying

### 🤖 **AI-Powered Features**
- **Smart File Organization** - Automatic categorization by content
- **Natural Language Search** - "find my work documents from last month"
- **Content Analysis** - Intelligent file type and purpose detection
- **Auto-suggestions** - Smart recommendations based on usage
- **Pattern Recognition** - Learn from your file organization habits

### 🛡️ **Advanced Security**
- **Security Dashboard** - Real-time threat monitoring and analysis
- **Session Management** - Secure login sessions with timeouts
- **Decoy Mode** - Hide your real vault with convincing fake data
- **Panic Mode** - Emergency data wipe capability
- **System Tray Integration** - Background protection monitoring
- **Comprehensive Logging** - Detailed audit trails for all actions

## 🚀 Installation & Setup

### **Step 1: Install Java with JavaFX**

**⚠️ IMPORTANT**: GhostVault requires Java with JavaFX support. Regular Java installations don't include JavaFX.

#### **Recommended: Bellsoft Liberica JDK (Easiest)**
1. **Download**: Go to https://bell-sw.com/pages/downloads/
2. **Select**: 
   - Version: **Java 21** (or 17+)
   - Operating System: **Windows**
   - Architecture: **x86 64-bit**
   - Package: **Full JDK** (⚠️ NOT Standard JDK)
3. **Download File**: `bellsoft-jdk21.0.8+12-windows-amd64-full.msi`
4. **Install**: Run the MSI installer with default settings
5. **Verify**: Open Command Prompt and run:
   ```bash
   java -version
   java --list-modules | findstr javafx
   ```

#### **Alternative Java Distributions with JavaFX**
- **Azul Zulu FX**: https://www.azul.com/downloads/ (Choose "JDK FX")
- **Oracle JDK**: https://www.oracle.com/java/technologies/downloads/
- **Amazon Corretto**: https://aws.amazon.com/corretto/ (requires separate JavaFX)

### **Step 2: Download GhostVault**

#### **Option A: Download Release (Recommended)**
1. Go to [Releases](https://github.com/PathanWasim/GhostVault/releases)
2. Download the latest `GhostVault-dist.zip`
3. Extract to your desired location

#### **Option B: Build from Source**
```bash
git clone https://github.com/PathanWasim/GhostVault.git
cd GhostVault
build-executable.bat
```

### **Step 3: Run GhostVault**

#### **Windows**
```bash
# Navigate to the dist folder
cd dist

# Run GhostVault
java -jar GhostVault.jar

# Or use the launcher
GhostVault.bat
```

#### **Linux/Mac**
```bash
cd dist
java -jar GhostVault.jar
```

### **First Time Setup**
1. **Launch Application** - Double-click or run the JAR
2. **Create Master Password** - Choose a strong password (this encrypts everything!)
3. **Vault Creation** - Your vault will be created at `~/.ghostvault/`
4. **Start Using** - Drag & drop files to encrypt them instantly

## 🎯 How to Use

### **File Encryption**
1. **Drag & Drop** - Drop files into the main window
2. **Upload Button** - Click "📁 Upload" to select files
3. **Batch Upload** - Select multiple files at once
4. **Download** - Click files in the list and "💾 Download" to decrypt

### **Notes Manager**
1. **Open Notes** - Click "📝 Notes" button
2. **Create Note** - Click "➕ New Note"
3. **Categories** - Choose from Personal, Work, Ideas, Important, Archive
4. **Search** - Use the search box for real-time filtering
5. **Auto-tagging** - Notes are automatically tagged based on content

### **Password Manager**
1. **Open Passwords** - Click "🔑 Passwords" button
2. **Add Password** - Fill in website, username, password
3. **Generate Password** - Click "🎲" for strong password generation
4. **Security Audit** - Click "🔍 Audit" for password analysis
5. **Categories** - Organize by Banking, Work, Social, Shopping, etc.

### **AI Features**
1. **Smart Search** - Click "🤖 AI Enhanced"
2. **Natural Language** - Try "find my tax documents" or "show recent photos"
3. **Auto-organization** - Let AI categorize your files automatically

## 🔧 Troubleshooting

### **JavaFX Errors**
```
Error: JavaFX runtime components are missing
```
**Solution**: Install Bellsoft Liberica "Full JDK" from https://bell-sw.com/

### **Module Not Found**
```
java.lang.module.FindException: Module javafx.controls not found
```
**Solution**: Your Java doesn't include JavaFX. Install Bellsoft Liberica Full JDK.

### **Application Won't Start**
1. **Check Java Version**: `java -version` (should be 17+)
2. **Check JavaFX**: `java --list-modules | findstr javafx`
3. **Try Direct Run**: `java -jar GhostVault.jar`
4. **Check Permissions**: Run as administrator if needed

### **Performance Issues**
- **Memory**: Ensure you have at least 1GB RAM available
- **Storage**: Check available disk space for encrypted files
- **Java Heap**: For large files, use: `java -Xmx2g -jar GhostVault.jar`

## 🏗️ Building from Source

### **Prerequisites**
- **Java 17+** with JavaFX (Bellsoft Liberica recommended)
- **Maven 3.6+** ([Download](https://maven.apache.org/download.cgi))

### **Build Commands**
```bash
# Clone repository
git clone https://github.com/PathanWasim/GhostVault.git
cd GhostVault

# Build distribution (Windows)
build-executable.bat

# Build distribution (Linux/Mac)
mvn clean package -Dmaven.test.skip=true
mkdir -p dist
cp target/ghostvault-1.0.0.jar dist/GhostVault.jar

# Run from source
mvn javafx:run
```

### **Build Output**
The build creates a `dist/` folder with:
- **GhostVault.jar** - Complete application (~50MB)
- **GhostVault.bat** - Windows launcher script
- **README.txt** - User documentation

## � Diistribution & Sharing

### **Sharing with Others**
1. **Copy** the entire `dist/` folder
2. **Recipient needs**: Java 17+ with JavaFX (Bellsoft Liberica recommended)
3. **Run**: `java -jar GhostVault.jar` or `GhostVault.bat`

### **System Requirements**
- **OS**: Windows 10+, macOS 10.14+, Linux (Ubuntu 18.04+)
- **Java**: 17+ with JavaFX (Bellsoft Liberica Full JDK recommended)
- **RAM**: 512MB minimum, 1GB recommended
- **Storage**: 100MB + space for encrypted files
- **Network**: None required (fully offline application)

## 🔒 Security Details

### **Encryption Standards**
- **Algorithm**: AES-256-GCM (Galois/Counter Mode)
- **Key Derivation**: PBKDF2 with SHA-256
- **Salt**: 32-byte cryptographically secure random salt
- **Iterations**: 100,000+ PBKDF2 iterations
- **Memory Protection**: Automatic cleanup of sensitive data

### **Vault Structure**
```
~/.ghostvault/
├── config.enc              # Encrypted configuration
├── files/                  # Encrypted file storage
│   ├── [uuid].enc         # Individual encrypted files
├── secure_notes.enc       # Encrypted notes database
├── stored_passwords.enc   # Encrypted password database
└── metadata/              # Encrypted file metadata
```

### **Privacy Features**
- **Zero-Knowledge**: We never see your data or passwords
- **Offline Operation**: No internet connection required
- **Local Storage**: All data stays on your machine
- **Open Source**: Code is auditable and transparent
- **No Telemetry**: No data collection or tracking

## 🆘 Support & Help

### **Getting Help**
- **🐛 Bug Reports**: [GitHub Issues](https://github.com/PathanWasim/GhostVault/issues)
- **💬 Questions**: [GitHub Discussions](https://github.com/PathanWasim/GhostVault/discussions)
- **📖 Documentation**: Check `dist/README.txt` for detailed user guide

### **Common Issues**
- **Forgot Master Password**: Cannot be recovered (by design for security)
- **JavaFX Missing**: Install Bellsoft Liberica Full JDK
- **Files Won't Encrypt**: Check vault folder permissions
- **Slow Performance**: Increase Java heap size with `-Xmx2g`

## 📊 Technical Specifications

- **Codebase**: 15,000+ lines of Java code
- **Architecture**: 82 classes with modular design
- **UI Framework**: JavaFX 17+ with professional styling
- **Build System**: Maven with dependency management
- **Distribution Size**: ~50MB (includes all dependencies)
- **Supported Platforms**: Windows, macOS, Linux

## 🤝 Contributing

We welcome contributions! Please:
1. **Fork** the repository
2. **Create** a feature branch
3. **Test** thoroughly with JavaFX
4. **Submit** a pull request

## 📄 License

This project is licensed under the **MIT License** - see [LICENSE](LICENSE) for details.

---

## ⭐ Why Choose GhostVault?

- **🔒 Military-Grade Security** - AES-256 encryption trusted by governments
- **🤖 AI-Powered Intelligence** - Smart organization and natural language search
- **🎯 Zero-Knowledge Privacy** - Your data never leaves your machine
- **📱 Professional Interface** - Clean, intuitive design
- **🌍 Cross-Platform** - Works on Windows, Mac, and Linux
- **📦 Self-Contained** - Single JAR file, no complex installation
- **🔓 Open Source** - Transparent, auditable, trustworthy

**⚠️ Security Notice**: Your master password cannot be recovered if lost. Keep it safe and consider writing it down in a secure location.

**🔐 Your Privacy, Your Security, Your Control.**
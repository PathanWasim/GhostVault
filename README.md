# 🔐 GhostVault

**Military-grade file encryption system with AI-powered organization and secure credential management**

![Java](https://img.shields.io/badge/Java-17+-orange.svg)
![JavaFX](https://img.shields.io/badge/JavaFX-17+-blue.svg)
![Status](https://img.shields.io/badge/Status-Production%20Ready-brightgreen.svg)
![License](https://img.shields.io/badge/License-MIT-blue.svg)

## 🌟 Features

### 🔒 **File Encryption & Security**
- **AES-256-GCM Encryption** - Military-grade file protection
- **Drag & Drop Interface** - Encrypt files instantly
- **Batch Operations** - Handle multiple files at once
- **Enhanced File Preview** - View encrypted files with robust media support
- **External Player Integration** - Open videos with VLC, Windows Media Player, etc.
- **Zero-Knowledge Architecture** - Your data, your keys only
- **Memory Protection** - Secure cleanup of sensitive data

### 📝 **Secure Notes Manager**
- **Encrypted Note Storage** - AES-256 protected notes
- **Real-time Search** - Find notes instantly as you type
- **Category Organization** - Organize by Personal, Work, Ideas, etc.
- **Auto-tagging** - Smart content-based tagging
- **Rich Text Support** - Format your notes beautifully
- **Export Options** - Secure backup and sharing

### 🔑 **Password Manager**
- **Encrypted Credential Storage** - Secure login management
- **Password Generator** - Create strong, unique passwords
- **Security Audit** - Analyze password strength and duplicates
- **Breach Detection** - Monitor for compromised passwords
- **Auto-fill Ready** - Easy password retrieval
- **Category Management** - Organize by Banking, Social, Work, etc.

### 🤖 **AI-Powered Features**
- **Smart File Organization** - Automatic categorization
- **Natural Language Search** - "find my work documents"
- **Content Analysis** - Intelligent file understanding
- **Auto-suggestions** - Smart recommendations
- **Pattern Recognition** - Learn from your usage

### 🛡️ **Advanced Security**
- **Security Dashboard** - Real-time threat monitoring
- **Password Attempt Limiting** - 3-attempt lockout with 30-second timer
- **Session Management** - Secure login sessions with timeout protection
- **Configuration Validation** - Robust config detection and recovery
- **Decoy Mode** - Hide your real vault with fake data
- **Panic Mode** - Emergency data wipe capability
- **System Tray Integration** - Background protection
- **Comprehensive Logging** - Detailed security audit trails

### 🎯 **User Experience**
- **Professional Dark Theme** - Easy on the eyes
- **Intuitive Interface** - Clean, modern design
- **Keyboard Shortcuts** - Power user friendly
- **Multi-platform** - Windows, macOS, Linux
- **Offline Operation** - No internet required
- **Portable** - Single JAR file distribution

## ✨ Key Features (Current Version)

### 🔐 **Core Security**
- **Triple-Password Authentication** - Master, Decoy, and Panic password modes
- **AES-256-GCM Encryption** - Authenticated encryption with 128-bit authentication tags
- **Argon2id Key Derivation** - Memory-hard KDF resistant to GPU attacks
- **KEK-Wrapped VMK Architecture** - Cryptographic erasure-capable design
- **Secure File Storage** - All files encrypted at rest with unique IVs
- **Memory Protection** - Sensitive data cleared from memory with secure zeroization

### 🎬 **Media Preview System**
- **Video Preview** - MP4, MOV, and other formats with JavaFX MediaPlayer
- **External Player Support** - "Open with External Player" for VLC, Windows Media Player
- **Image Preview** - JPG, PNG, GIF, and other image formats
- **Text File Preview** - Code files, documents, and plain text
- **Smart Error Handling** - Graceful fallbacks when preview fails

### 🔒 **Authentication Security**
- **Brute Force Protection** - 3-attempt limit with 30-second lockout
- **Real-time Feedback** - Live countdown timer and attempt counter
- **Session Management** - Secure login sessions with timeout
- **Configuration Validation** - Automatic config integrity checking

## 🆕 Recent Improvements (v2.0)

### 🔒 **Enhanced Security Features**
- **Password Attempt Limiting** - Prevents brute force attacks with 3-attempt lockout
- **Real-time Security Feedback** - Live countdown timer and attempt counter on login
- **Configuration Validation** - Robust detection and recovery of corrupted configs
- **Comprehensive Security Logging** - Detailed audit trails with automatic log rotation

### 🎬 **Improved Media Preview**
- **Enhanced Video Support** - Better handling of MP4, MOV, and other video formats
- **External Player Integration** - "Open with External Player" option for unsupported formats
- **Graceful Error Handling** - User-friendly messages instead of technical errors
- **Smart Fallback System** - Automatic detection and recovery from preview failures

### 🛠️ **System Stability**
- **Robust Error Recovery** - Comprehensive error handling throughout the application
- **Memory Management** - Improved cleanup and resource management
- **Configuration Backup** - Automatic backup and integrity checking
- **Cross-session Security** - Persistent security state across application restarts

## 🚀 Quick Start

### Prerequisites
- **Java 17+** ([Download from Adoptium](https://adoptium.net/))

### Installation & Usage

**Option 1: Download & Run**
```bash
# Download the latest release
# Extract and run
java -jar GhostVault.jar
```

**Option 2: Build from Source**
```bash
git clone https://github.com/PathanWasim/GhostVault.git
cd GhostVault
mvn javafx:run
```

### First Time Setup
1. **Launch GhostVault** - Run the application
2. **Create Master Password** - This encrypts everything (remember it!)
3. **Start Encrypting** - Drag & drop files to encrypt them
4. **Explore Features** - Try Notes, Passwords, and AI search

## 📱 Screenshots & Demo

### Main Interface
- **File Encryption**: Drag & drop files for instant AES-256 encryption
- **Smart Search**: Use natural language like "find my tax documents"
- **Security Dashboard**: Real-time monitoring of vault security

### Notes Manager
- **Create & Edit**: Rich text notes with categories
- **Search & Filter**: Find notes instantly with real-time search
- **Auto-tagging**: Smart tags based on content analysis

### Password Manager
- **Secure Storage**: Military-grade encryption for credentials
- **Password Generator**: Create strong, unique passwords
- **Security Audit**: Analyze and improve password security

## 🏗️ Technical Details

### Architecture
- **Clean, Modular Design** - Well-structured Java codebase with 100+ classes
- **JavaFX 17 UI** - Modern, responsive interface with system tray integration
- **Maven Build System** - Professional build with dependency management
- **Layered Security Architecture**:
  - **Cryptographic Layer**: AES-256-GCM + Argon2id
  - **Business Logic Layer**: File management, threat detection, backup
  - **UI Layer**: JavaFX controllers with integrated media players
  - **Storage Layer**: Master vault, decoy vault, encrypted backups
- **Enhanced Preview System** - Robust media handling with external player fallbacks
- **Component Integration**: 30+ UI components, comprehensive security modules

### Security Implementation
- **Encryption**: AES-256-GCM (Authenticated Encryption with Associated Data)
  - 12-byte (96-bit) IV for optimal GCM performance
  - 128-bit authentication tag for integrity verification
  - No padding oracle vulnerabilities (NoPadding mode)
- **Key Derivation**: Argon2id with adaptive parameters
  - Memory-hard function (64MB default)
  - Resistant to GPU/ASIC attacks
  - Benchmarked parameters for optimal security/performance balance
- **Password Architecture**: KEK-wrapped VMK design
  - Master/Decoy: KEK-wrapped Vault Master Keys (allows vault access)
  - Panic: Verifier-only (no key recovery - enables cryptographic erasure)
  - Constant-time password detection with timing parity
- **Salt Generation**: 32-byte cryptographically secure random salts per vault
- **Memory Safety**: Secure zeroization of sensitive data (char[] for passwords, never String)
- **File Protection**: Encrypted metadata and content with SHA-256 integrity hashing
- **Session Security**: Secure authentication with attempt limiting and lockout
- **Brute Force Protection**: 3-attempt limit with 30-second lockout + jitter
- **Configuration Security**: Integrity checking, automatic backup, and recovery

### Performance
- **Fast Encryption**: Optimized for large files
- **Smart Caching**: Efficient memory usage
- **Background Processing**: Non-blocking operations
- **Minimal Footprint**: ~50MB distribution size

## 🛠️ Building

### Quick Build
```bash
# Build and run
mvn clean compile
mvn javafx:run

# Package for distribution
mvn clean package -Dmaven.test.skip=true
```

### Development
```bash
# Run from source
mvn javafx:run

# Compile only
mvn compile
```

Run `mvn javafx:run` to start the application in development mode.

## 📦 Distribution

Build the application with Maven:
```bash
mvn clean package
```

This creates a JAR file in the `target/` directory. Run with:
```bash
java -jar target/ghostvault-1.0.0.jar
```

**Requirements**: Java 17+ must be installed on the target system.

## 🔧 Configuration

### Vault Location
- **Default**: `~/.ghostvault/`
- **Files**: Encrypted file storage in `vault/` directory
- **Notes**: `secure_notes.enc` (AES-256-GCM encrypted)
- **Passwords**: `stored_passwords.enc` (AES-256-GCM encrypted)
- **Config**: `config.enc` (KEK-wrapped VMK + verifiers)
- **Metadata**: `metadata.json` (encrypted file metadata)
- **Backups**: `backups/` directory with versioned encrypted backups
- **Logs**: `logs/` directory with security audit trails

### Security Settings
- **Encryption**: 
  - Algorithm: AES-256-GCM (Authenticated Encryption)
  - IV: 12 bytes (96 bits) per encryption
  - Authentication Tag: 16 bytes (128 bits)
  - No padding (stream cipher mode)
- **Key Derivation**: 
  - Algorithm: Argon2id (memory-hard KDF)
  - Memory: 64MB (65536 KB)
  - Iterations: 3 (time cost)
  - Parallelism: 1 thread
  - Output: 32 bytes (256 bits)
- **Salt**: 
  - Length: 32 bytes (256 bits)
  - Generation: Cryptographically secure random per vault
  - Reused for all passwords in same vault
- **Password Architecture**:
  - Master/Decoy: KEK-wrapped VMK (allows vault access)
  - Panic: Verifier-only (no key recovery)
  - Timing: 900ms + 0-300ms jitter per attempt
- **Session Management**:
  - Timeout: Configurable (default 15 minutes)
  - Failed Attempts: 3 attempts before 30-second lockout
  - Lockout: Exponential backoff on repeated failures

## 🆘 System Requirements

- **Operating System**: Windows 10+, macOS 10.14+, Linux
- **Java Runtime**: 17 or higher
- **Memory**: 512MB minimum, 1GB recommended
- **Storage**: 100MB + space for encrypted files
- **Network**: None required (fully offline)

## 🤝 Contributing

We welcome contributions! Please:

1. **Fork** the repository
2. **Create** a feature branch
3. **Test** your changes thoroughly
4. **Submit** a pull request

## 📄 License

This project is licensed under the **MIT License** - see [LICENSE](LICENSE) for details.

## 🆘 Support

- **🐛 Bug Reports**: [GitHub Issues](https://github.com/PathanWasim/GhostVault/issues)
- **💬 Discussions**: [GitHub Discussions](https://github.com/PathanWasim/GhostVault/discussions)
- **📖 Documentation**: This README contains all usage information

## ⭐ Why Choose GhostVault?

- **🔒 Military-Grade Security** - AES-256 encryption used by governments
- **🤖 AI-Powered** - Smart organization and search capabilities
- **🎯 Zero-Knowledge** - We never see your data or passwords
- **📱 User-Friendly** - Professional interface, easy to use
- **🌍 Cross-Platform** - Works on Windows, Mac, and Linux
- **📦 Portable** - Single file executable, no installation required
- **🔓 Open Source** - Transparent, auditable code

---

**⚠️ Security Notice**: Your master password cannot be recovered if lost. Keep it safe!

**🔐 Your Privacy, Your Security, Your Control.**

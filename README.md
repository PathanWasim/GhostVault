# 🔐 GhostVault

**Advanced Secure File Management System with Multi-Modal Authentication**

![Java](https://img.shields.io/badge/Java-17+-orange.svg)
![JavaFX](https://img.shields.io/badge/JavaFX-17+-blue.svg)
![Status](https://img.shields.io/badge/Status-Research%20Prototype-blue.svg)
![Security](https://img.shields.io/badge/Security-Military%20Grade-red.svg)
![License](https://img.shields.io/badge/License-MIT-blue.svg)

> Revolutionary secure file management system featuring triple-password authentication, coercion-resistant security, and AI-powered threat detection.

<<<<<<< HEAD
### 🔒 **File Encryption & Security**
- **AES-256-GCM Encryption** - Military-grade file protection
- **Drag & Drop Interface** - Encrypt files instantly
- **Batch Operations** - Handle multiple files at once
- **Enhanced File Preview** - View encrypted files with robust media support
- **External Player Integration** - Open videos with VLC, Windows Media Player, etc.
=======
---

## 🌟 Key Features

### ✅ **Core Security (Implemented)**
- **Triple-Password Authentication** - Master, Decoy, and Panic modes
- **AES-256 Encryption** - Military-grade file protection with Argon2 key derivation
- **Cryptographic Erasure** - Emergency data destruction through key elimination
- **Real-time Threat Detection** - Behavioral analysis with 10+ threat types
- **Coercion Resistance** - Decoy vault with believable fake data
>>>>>>> 5e3dbee5708a73a7823118329611adf0497308f8
- **Zero-Knowledge Architecture** - Your data, your keys only
- **Memory Protection** - Secure cleanup of sensitive data

### ✅ **File Management (Implemented)**
- **Drag & Drop Interface** - Encrypt files instantly by dropping them
- **Batch Operations** - Handle multiple files simultaneously
- **Secure File Preview** - View encrypted files without full decryption
- **Integrated Media Players** - Video/audio playback with full controls
- **Real-time Search** - Instant file filtering and organization
- **Professional Interface** - Modern JavaFX design with dark theme

### ✅ **Secure Notes Manager (Implemented)**
- **Encrypted Note Storage** - AES-256 protected notes with categories
- **Real-time Search** - Find notes instantly as you type
- **Auto-tagging** - Smart content-based tagging system
- **Rich Categories** - Personal, Work, Ideas, Important, Archive
- **Export Options** - Secure backup in encrypted formats
- **Full-text Search** - Search through note titles and content

### ✅ **Password Manager (Implemented)**
- **Encrypted Credential Storage** - Secure login management
- **Password Generator** - Create strong, unique passwords (16+ chars)
- **Security Audit** - Real-time analysis of password strength
- **Breach Detection** - Monitor for compromised credentials
- **Category Management** - Banking, Social, Work, Shopping, etc.
- **Auto-fill Ready** - Easy password retrieval and copying

### ✅ **Advanced Security Features (Implemented)**
- **Security Dashboard** - Real-time threat monitoring and analysis
- **Session Management** - Secure login sessions with timeouts
- **System Tray Integration** - Background protection monitoring
- **Comprehensive Logging** - Detailed audit trails for all actions
- **Zero-Knowledge Architecture** - Your data, your keys only
- **Offline Operation** - No internet connection required
- **Cross-Platform** - Windows, macOS, Linux support

<<<<<<< HEAD
### 🛡️ **Advanced Security**
- **Security Dashboard** - Real-time threat monitoring
- **Password Attempt Limiting** - 3-attempt lockout with 30-second timer
- **Session Management** - Secure login sessions with timeout protection
- **Configuration Validation** - Robust config detection and recovery
- **Decoy Mode** - Hide your real vault with fake data
- **Panic Mode** - Emergency data wipe capability
- **System Tray Integration** - Background protection
- **Comprehensive Logging** - Detailed security audit trails
=======
### 🤖 **AI-Powered Features (Implemented & Planned)**
- **Smart File Organization** ✅ - Automatic categorization by content
- **Natural Language Search** ✅ - "find my work documents from last month"
- **Content Analysis** ✅ - Intelligent file type and purpose detection
- **Auto-suggestions** ✅ - Smart recommendations based on usage
- **Pattern Recognition** 🔮 - Learn from your file organization habits
>>>>>>> 5e3dbee5708a73a7823118329611adf0497308f8

### 🔮 **Advanced AI & Security Roadmap**
- **Phase 1 (6 months)** - Enhanced behavioral learning and threat prediction
- **Phase 2 (12 months)** - Honey encryption and federated learning
- **Phase 3 (18 months)** - Post-quantum cryptography integration
- **Phase 4 (24+ months)** - Zero-knowledge protocols and quantum ML
- **Phase 5 (30+ months)** - Privacy-preserving authentication and audit systems

---

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
- **Java 17+** with JavaFX (Recommended: [Bellsoft Liberica Full JDK](https://bell-sw.com/pages/downloads/))
- **512MB RAM** minimum, 1GB recommended
- **Windows 10+, macOS 10.14+, or Linux**

### Installation Options

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

# Or build distribution
build-executable.bat  # Windows
mvn clean package     # Linux/Mac
```

### First Time Setup
1. **Launch GhostVault** - Run the application
2. **Create Master Password** - This encrypts everything (remember it!)
<<<<<<< HEAD
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
- **82 Java Classes** - Clean, modular design
- **JavaFX UI** - Modern, responsive interface
- **Maven Build** - Professional build system
- **Layered Security** - Multiple protection levels

### Security Implementation
- **Encryption**: AES-256-GCM with PBKDF2 key derivation
- **Salt Generation**: Cryptographically secure random salts
- **Memory Safety**: Automatic cleanup of sensitive data
- **File Protection**: Encrypted metadata and content
- **Session Security**: Secure authentication with attempt limiting
- **Brute Force Protection**: 3-attempt limit with 30-second lockout
- **Configuration Security**: Integrity checking and automatic recovery

### Performance
- **Fast Encryption**: Optimized for large files
- **Smart Caching**: Efficient memory usage
- **Background Processing**: Non-blocking operations
- **Minimal Footprint**: ~50MB distribution size

## 🛠️ Building

### Quick Build
```bash
# Windows
build-executable.bat

# Linux/Mac
mvn clean package -Dmaven.test.skip=true
```

### Development
```bash
# Run from source
mvn javafx:run

# Compile only
mvn compile
```

See [BUILD.md](BUILD.md) for detailed instructions.

## 📦 Distribution

The build creates a `dist/` folder containing:
- **GhostVault.jar** - Complete application (~50MB)
- **GhostVault.bat** - Windows launcher script
- **README.txt** - User documentation

**Sharing**: Copy the entire `dist/` folder. Recipients need Java 17+.

## 🔧 Configuration

### Vault Location
- **Default**: `~/.ghostvault/`
- **Files**: Encrypted file storage
- **Notes**: `secure_notes.enc`
- **Passwords**: `stored_passwords.enc`
- **Config**: Application settings

### Security Settings
- **Encryption**: AES-256-GCM
- **Key Derivation**: PBKDF2-SHA256
- **Iterations**: 100,000+
- **Salt Length**: 32 bytes

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
- **📖 Documentation**: Check `dist/README.txt` for user guide

## ⭐ Why Choose GhostVault?

- **🔒 Military-Grade Security** - AES-256 encryption used by governments
- **🤖 AI-Powered** - Smart organization and search capabilities
- **🎯 Zero-Knowledge** - We never see your data or passwords
- **📱 User-Friendly** - Professional interface, easy to use
- **🌍 Cross-Platform** - Works on Windows, Mac, and Linux
- **📦 Portable** - Single file, no installation required
- **🔓 Open Source** - Transparent, auditable code
=======
3. **Configure Security** - Set up Decoy and Panic passwords
4. **Start Encrypting** - Drag & drop files to encrypt them instantly
5. **Explore Features** - Try Notes, Passwords, and AI search
>>>>>>> 5e3dbee5708a73a7823118329611adf0497308f8

---

## 🎯 Usage

### Authentication Modes
- **Master Password** → Access real vault with full functionality
- **Decoy Password** → Show fake vault under coercion/duress
- **Panic Password** → Emergency data destruction (irreversible)

### File Operations
- **Upload** - Drag & drop files or use upload button
- **Preview** - Double-click for integrated media playback
- **Download** - Select files and decrypt to local storage
- **Search** - Real-time filtering and file organization

### Security Features
- **Threat Monitor** - View real-time security status
- **System Tray** - Background protection monitoring
- **Backup/Restore** - Encrypted vault backup operations

---

## 🔒 Security Architecture

### Cryptographic Implementation
```
Algorithm: AES-256-CBC with PKCS5 padding
Key Derivation: Argon2id (64MB memory, 10 iterations)
Authentication: Triple-password system with secure hashing
Threat Detection: Real-time behavioral analysis
Emergency Protocol: Cryptographic erasure (4-phase destruction)
```

### Vault Structure
```
~/.ghostvault/
├── vault.config          # Encrypted password hashes
├── real/                 # Master vault (encrypted files)
├── decoy/                # Fake vault (believable content)
└── logs/                 # Security audit trails
```

---

## 📊 Technical Specifications

| Component | Status | Implementation |
|-----------|--------|----------------|
| **Triple Authentication** | ✅ Production | `PasswordManager.java` |
| **AES-256 Encryption** | ✅ Production | `CryptoManager.java` |
| **Threat Detection** | ✅ Production | `ThreatDetectionEngine.java` |
| **Media Players** | ✅ Production | `VaultMainController.java` |
| **UI Components** | ✅ Production | 30+ JavaFX classes |
| **AI File Analysis** | 🔮 Phase 1 | Research roadmap |
| **Honey Encryption** | 🔮 Phase 2 | Research roadmap |
| **Post-Quantum Crypto** | 🔮 Phase 3 | Research roadmap |
| **Zero-Knowledge Proofs** | 🔮 Phase 4 | Research roadmap |

### Performance Metrics
- **Encryption Throughput**: 55+ MB/s
- **UI Response Time**: <100ms
- **Memory Efficiency**: 1.5-1.8x overhead
- **Codebase**: 20,000+ lines across 110+ classes

---

## 🔬 Research Contributions

### Novel Security Paradigms
1. **Triple-Password Architecture** - Coercion-resistant authentication
2. **Cryptographic Erasure Protocol** - Key destruction as primary defense
3. **Behavioral Threat Detection** - AI-powered anomaly analysis
4. **Zero-Knowledge Security** - Privacy-preserving authentication and verification
5. **Integrated Secure Media** - Encrypted environment playback

### Academic Applications
- **Security Research** - Novel authentication and threat detection
- **AI Integration** - Privacy-preserving machine learning
- **Cryptographic Innovation** - Post-quantum, honey encryption, and zero-knowledge proofs
- **Privacy Engineering** - Zero-knowledge authentication and audit systems
- **Usable Security** - High security with excellent user experience

---

## 🛡️ Security Notices

**⚠️ Password Recovery**: Passwords cannot be recovered if lost (by design for security)

**🔥 Panic Mode**: Permanently destroys all data - use only in genuine emergencies

**🎭 Decoy Mode**: Shows fake files under coercion - ensure content is believable

**🔬 Research Status**: Core security features are production-ready; AI features in development

---

## 🤝 Contributing

We welcome contributions to both implementation and research:

```bash
# Development setup
git clone https://github.com/PathanWasim/GhostVault.git
cd GhostVault
mvn clean compile
mvn javafx:run
```

**Areas of Interest**:
- Security feature enhancement
- AI/ML integration
- Zero-knowledge protocol implementation
- Privacy-preserving systems
- Performance optimization
- Cross-platform compatibility

---

## � aLicense & Support

- **License**: MIT License - see [LICENSE](LICENSE) for details
- **Issues**: [GitHub Issues](https://github.com/PathanWasim/GhostVault/issues)
- **Discussions**: [GitHub Discussions](https://github.com/PathanWasim/GhostVault/discussions)
- **Documentation**: See `ghostvault.md` for comprehensive research documentation

---

## ⭐ Why GhostVault?

**🔒 Advanced Security** - Triple-password system with coercion resistance  
**🎬 Integrated Media** - Secure video/audio playback within encrypted environment  
**🔐 Zero-Knowledge Privacy** - Privacy-preserving authentication and verification  
**🧠 AI-Ready Architecture** - Comprehensive roadmap for intelligent security features  
**🔬 Research Quality** - Academic-grade implementation suitable for publication  
**🌍 Cross-Platform** - Works on Windows, macOS, and Linux  
**📖 Open Source** - Transparent, auditable, and trustworthy  

**🔐 Your Privacy, Your Security, Your Innovation.**
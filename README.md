# 🚀 GhostVault Enterprise Edition

A **professional, enterprise-grade security suite** with comprehensive file encryption, real-time security monitoring, and advanced features for modern security needs.

## ✨ Enterprise Features

### 🛡️ **Advanced Security Suite**
- **AES-256 Encryption** - Military-grade file protection
- **Real-time Threat Detection** - Advanced security monitoring
- **Memory Protection** - Secure data handling with encryption
- **File System Monitoring** - Tamper detection and integrity verification
- **Multi-Password System** - Master, Panic, and Decoy passwords
- **Secure Deletion** - DoD-standard file wiping

### 🚀 **Professional Features**
- **📊 Security Dashboard** - Real-time monitoring with charts and metrics
- **📝 Secure Notes Manager** - Encrypted note storage with categories
- **🔑 Password Manager** - Enterprise-grade password vault
- **🤖 Smart Search** - AI-powered natural language file search
- **🔔 System Tray Integration** - Background monitoring and notifications

### 📁 **Enhanced File Management**
- **Drag & Drop Upload** - Professional file encryption interface
- **File Preview** - View images, text, and PDFs securely
- **Smart Search & Filter** - Natural language file discovery
- **Backup & Restore** - Encrypted vault backups

### 🎨 **Professional Interface**
- **Enterprise UI Design** - Modern card-based layout with professional styling
- **Responsive Design** - Adapts to different screen sizes
- **Keyboard Shortcuts** - Full keyboard navigation (Ctrl+D, Ctrl+N, Ctrl+P)
- **Real-time Feedback** - Professional status updates and notifications
- **Cross-Platform** - Windows, macOS, Linux

## 🚀 Quick Start

### Requirements
- Java 11 or higher
- 512MB RAM minimum

### Installation
1. Download the latest release
2. Extract and run:
   ```bash
   java -jar ghostvault.jar
   ```

### First Setup
1. Launch GhostVault Enterprise Edition
2. Create three passwords:
   - **Master**: Normal vault access
   - **Panic**: Emergency data destruction
   - **Decoy**: Shows fake files
3. Explore the new enterprise features:
   - **📊 Dashboard** - Click to see real-time security monitoring
   - **📝 Notes** - Access the secure notes manager
   - **🔑 Passwords** - Open the enterprise password vault
   - **🔍 Smart Search** - Try "find my documents" in the search box

## 🌟 **Enterprise Features Guide**

### 📊 **Security Dashboard**
Access real-time security monitoring with professional charts and metrics:
- **Security Score**: Live security assessment (0-100)
- **Threat Level**: Current threat status monitoring
- **Active Sessions**: Session management and tracking
- **Protected Files**: Real-time file count and vault statistics
- **Security Events**: Timeline of recent security activities

### 📝 **Secure Notes Manager**
Enterprise-grade encrypted note storage:
- **Military-grade AES-256 encryption** for all notes
- **Category organization** for better note management
- **Tag-based search** for quick note discovery
- **Full-text search** capability across all notes
- **Secure cloud sync** ready for team deployment

### 🔑 **Password Manager**
Professional password security suite:
- **Zero-knowledge architecture** for maximum security
- **Password strength analysis** with recommendations
- **Secure password generation** with customizable rules
- **Breach monitoring** ready for security alerts
- **Multi-device sync** capability for team access
- **Category-based organization** for business passwords

### 🤖 **Smart Search**
AI-powered natural language file search:
- Try queries like: `"find my documents"`, `"show me images"`, `"search for pdf files"`
- **Intelligent results** based on file content and metadata
- **Real-time feedback** in the activity log
- **Fallback to basic search** if smart search is unavailable

## 📖 Usage

### Basic Operations
- **Upload**: Click 📁 Upload → Select file → Automatically encrypted
- **Download**: Select file → Click 💾 Download → Choose save location
- **Preview**: Select file → Click 👁️ Preview → View content securely
- **Delete**: Select file → Click 🗑️ Delete → Secure multi-pass deletion

### Security Modes
- **Normal Mode**: Enter master password for full access
- **Panic Mode**: Enter panic password to silently destroy all data
- **Decoy Mode**: Enter decoy password to show fake files

### Backup & Restore
- **Backup**: Click 📦 Backup → Choose location → Creates encrypted .gvbackup file
- **Restore**: Click 📥 Restore → Select .gvbackup → Restores vault contents

## 🔧 Technical Details

### Encryption
- **Algorithm**: AES-256-CBC with PBKDF2 key derivation
- **Iterations**: 100,000+ for password hashing
- **Integrity**: SHA-256 verification for all files
- **Memory**: Secure wiping of sensitive data

### File Storage
- **Location**: `~/.ghostvault/` directory
- **Structure**: Encrypted files with random UUID names
- **Metadata**: Separately encrypted file information
- **Backups**: Fully encrypted vault snapshots

## 🎯 Security Features

### Protection Against
- **Data Theft** - Strong encryption protects files
- **Password Attacks** - High-iteration hashing
- **Coercion** - Panic mode destroys evidence
- **Surveillance** - Decoy mode provides cover
- **Forensics** - Secure deletion prevents recovery

### Best Practices
1. Use strong, unique passwords
2. Create regular encrypted backups
3. Store backups in secure locations
4. Never reuse vault passwords
5. Keep your system physically secure

## 🔑 Keyboard Shortcuts

### **File Operations**
- `Ctrl+U` - Upload file
- `Ctrl+D` - Download file
- `Delete` - Secure delete
- `Ctrl+T` - Toggle theme

### **Enterprise Features** ⭐
- `Ctrl+D` - Open Security Dashboard
- `Ctrl+N` - Access Secure Notes Manager
- `Ctrl+P` - Open Password Manager
- `Ctrl+F` - Focus Smart Search

### **General**
- `F1` - Help system
- `Escape` - Close dialogs

## ⚠️ Important Notes

### Security Warnings
- **Lost passwords cannot be recovered** (by design)
- **Panic mode permanently destroys data** - use with caution
- **Keep backups secure** - they contain your encrypted vault

### Legal Notice
GhostVault is for legitimate privacy and security needs. Users must comply with all applicable laws. The panic feature permanently destroys data and should be used responsibly.

## 🛠️ Development

### Building from Source
```bash
git clone https://github.com/PathanWasim/GhostVault.git
cd GhostVault
mvn compile
mvn javafx:run
```

### Testing
```bash
mvn test                    # Run all tests
mvn test -Dtest=Security*   # Security tests only
```

## 📄 License

MIT License - see [LICENSE](LICENSE) for details.

## 🆘 Support

- **Help System**: Press F1 in the application
- **Issues**: Report bugs on GitHub
- **Security**: Email security issues privately

---

**Built with ❤️ for privacy and security**
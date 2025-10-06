# 🔐 GhostVault - Secure File Encryption

A modern, secure file vault application with military-grade encryption and advanced security features.

## ✨ Features

### �️ ySecurity
- **AES-256 Encryption** - Military-grade file protection
- **Multi-Password System** - Master, Panic, and Decoy passwords
- **Secure Deletion** - DoD-standard file wiping
- **Session Management** - Auto-timeout and activity monitoring

### 📁 File Management
- **Drag & Drop Upload** - Easy file encryption
- **File Preview** - View images, text, and PDFs securely
- **Search & Filter** - Find files quickly
- **Backup & Restore** - Encrypted vault backups

### 🎨 Interface
- **Modern UI** - Clean JavaFX interface
- **Dark/Light Themes** - Professional theme support
- **Accessibility** - Screen reader and keyboard navigation
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
1. Launch GhostVault
2. Create three passwords:
   - **Master**: Normal vault access
   - **Panic**: Emergency data destruction
   - **Decoy**: Shows fake files
3. Start encrypting your files!

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

- `Ctrl+U` - Upload file
- `Ctrl+D` - Download file
- `Delete` - Secure delete
- `Ctrl+T` - Toggle theme
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
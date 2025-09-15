# GhostVault - Secure File Vault

![GhostVault Logo](docs/logo.png)

GhostVault is a comprehensive secure file storage application built with JavaFX that provides military-grade encryption, advanced security features, and multiple protection modes including panic and decoy functionality.

## 🔐 Key Features

### Core Security
- **AES-256-CBC Encryption** - Military-grade encryption for all files
- **PBKDF2 Password Hashing** - 100,000+ iterations with secure salt
- **SHA-256 Integrity Verification** - Ensures file integrity and detects corruption
- **Secure Memory Management** - Automatic memory wiping and protection

### Multi-Password System
- **Master Password** - Full access to your secure vault
- **Panic Password** - Emergency data destruction (silent operation)
- **Decoy Password** - Shows fake files to mislead attackers

### Advanced Security Features
- **Session Management** - Automatic timeout and activity monitoring
- **Threat Detection** - Real-time monitoring and anomaly detection
- **Anti-Debugging** - Protection against reverse engineering attempts
- **Secure Deletion** - DoD 5220.22-M standard multi-pass overwriting

### User Interface
- **Modern JavaFX UI** - Clean, intuitive interface
- **Dark/Light Themes** - Professional theme support with smooth transitions
- **Accessibility Features** - Screen reader support, high contrast mode, keyboard navigation
- **Smooth Animations** - Professional animations and transitions
- **Progress Feedback** - Real-time progress for all operations

### Backup & Recovery
- **Encrypted Backups** - Secure vault backup with integrity verification
- **Flexible Restore** - Merge or replace options with conflict resolution
- **Cross-Platform** - Backups work across different operating systems

## 🚀 Quick Start

### Prerequisites
- Java 11 or higher
- JavaFX 11 or higher (included in most Java distributions)

### Installation
1. Download the latest release from the releases page
2. Extract the archive to your desired location
3. Run the application:
   ```bash
   java -jar ghostvault.jar
   ```

### First Run Setup
1. Launch GhostVault
2. Create three strong passwords:
   - **Master Password**: Your main vault access
   - **Panic Password**: Emergency destruction trigger
   - **Decoy Password**: Fake vault access
3. All passwords must be different and meet strength requirements
4. Your vault is now ready to use!

## 📖 Usage Guide

### Basic Operations

#### Uploading Files
1. Click the "📁 Upload" button
2. Select the file you want to encrypt
3. File is automatically encrypted and stored securely

#### Downloading Files
1. Select a file from the vault list
2. Click the "💾 Download" button
3. Choose where to save the decrypted file
4. File integrity is automatically verified

#### Secure Deletion
1. Select a file from the vault list
2. Click the "🗑️ Delete" button
3. Confirm deletion in the dialog
4. File is securely overwritten multiple times

#### Searching Files
- Use the search box to find files by name
- Results update in real-time as you type
- Search works on original file names

### Advanced Features

#### Creating Backups
1. Click the "📦 Backup" button
2. Choose a secure location for the backup file
3. Enter your master password to confirm
4. Backup file (.gvbackup) is created with full encryption

#### Restoring from Backup
1. Click the "📥 Restore" button
2. Select your .gvbackup file
3. Enter your master password
4. Choose merge or replace option

#### Security Modes

**Panic Mode**
- Enter your panic password at login
- Silently destroys all vault data
- Appears as normal failed login
- Cannot be undone - use with extreme caution

**Decoy Mode**
- Enter your decoy password at login
- Shows fake files to mislead attackers
- Completely separate from real vault
- Maintains plausible deniability

## 🛡️ Security Architecture

### Encryption Details
- **Algorithm**: AES-256-CBC with PKCS5 padding
- **Key Derivation**: PBKDF2WithHmacSHA256 (100,000+ iterations)
- **IV Generation**: Cryptographically secure random
- **File Integrity**: SHA-256 hash verification
- **Metadata Protection**: Encrypted separately from file data

### Password Security
- Passwords stored as salted hashes only
- Memory wiping after password operations
- Strong password requirements enforced
- Protection against timing attacks

### File Storage
- Files stored with random UUID names
- Original names encrypted in metadata
- Flat storage structure (no directory preservation)
- Secure deletion with multiple overwrites

### Session Security
- Configurable session timeout
- Activity monitoring (mouse/keyboard)
- Failed login attempt tracking
- Automatic lockout after max attempts

## 🎨 Themes and Accessibility

### Theme Support
- **Dark Theme** - Professional dark interface (default)
- **Light Theme** - Clean light interface
- **High Contrast** - Enhanced visibility for accessibility
- **Smooth Transitions** - Professional animations between themes

### Accessibility Features
- Full keyboard navigation support
- Screen reader compatibility
- High contrast mode for visual impairments
- Tooltips and descriptive labels
- Configurable font sizes

### Keyboard Shortcuts
- `Tab/Shift+Tab` - Navigate controls
- `Ctrl+U` - Upload file
- `Ctrl+D` - Download file
- `Delete` - Secure delete file
- `Ctrl+T` - Toggle theme
- `Ctrl+H` - Toggle high contrast
- `F1` - Show help
- `Escape` - Close dialogs

## 🔧 Technical Details

### System Requirements
- **Operating System**: Windows, macOS, Linux
- **Java Version**: 11 or higher
- **Memory**: 512MB RAM minimum, 1GB recommended
- **Storage**: 50MB for application, additional space for vault files

### File Locations
- **Vault Directory**: `~/.ghostvault/`
- **Configuration**: `~/.ghostvault/config.enc`
- **Encrypted Files**: `~/.ghostvault/files/`
- **Decoy Files**: `~/.ghostvault/decoys/`
- **Audit Logs**: `~/.ghostvault/audit.log.enc`

### Performance
- **Encryption Speed**: ~50-100 MB/s (depends on hardware)
- **File Size Limits**: Limited only by available disk space
- **Concurrent Operations**: Background processing for large files
- **Memory Usage**: Efficient streaming for large files

## 🧪 Testing

The application includes comprehensive test coverage:

### Test Categories
- **Unit Tests** - Individual component testing
- **Integration Tests** - Component interaction testing
- **Security Tests** - Cryptographic and security validation
- **Performance Tests** - Load and stress testing
- **Penetration Tests** - Security vulnerability assessment

### Running Tests
```bash
# Run all tests
mvn test

# Run specific test categories
mvn test -Dtest=SecurityValidationTest
mvn test -Dtest=PerformanceValidationTest
mvn test -Dtest=PenetrationTest
```

## 🔒 Security Considerations

### Best Practices
1. **Use Strong Passwords** - Follow the strength requirements
2. **Regular Backups** - Create encrypted backups frequently
3. **Secure Storage** - Store backups in multiple secure locations
4. **Password Management** - Never reuse vault passwords elsewhere
5. **Physical Security** - Secure your computer and backup media

### Threat Model
GhostVault protects against:
- **Data Theft** - Files encrypted with strong algorithms
- **Password Attacks** - Strong hashing with high iteration counts
- **Coercion** - Panic mode for emergency data destruction
- **Surveillance** - Decoy mode for plausible deniability
- **Forensic Analysis** - Secure deletion and encrypted metadata

### Limitations
- **Password Recovery** - Lost passwords cannot be recovered (by design)
- **Quantum Resistance** - AES-256 is currently quantum-resistant
- **Side-Channel Attacks** - Mitigated but not completely eliminated
- **Physical Access** - Cannot protect against hardware-level attacks

## 📚 Documentation

### Help System
- Built-in comprehensive help system (F1 key)
- Getting started guide
- Security features explanation
- Troubleshooting guide
- Keyboard shortcuts reference

### API Documentation
- JavaDoc documentation included
- Component architecture diagrams
- Security implementation details
- Extension points for customization

## 🤝 Contributing

We welcome contributions to GhostVault! Please see our [Contributing Guide](CONTRIBUTING.md) for details.

### Development Setup
1. Clone the repository
2. Install Java 11+ and Maven
3. Run `mvn compile` to build
4. Run `mvn test` to execute tests
5. Run `mvn javafx:run` to start the application

### Code Style
- Follow Java naming conventions
- Include comprehensive JavaDoc comments
- Write unit tests for new features
- Follow security best practices

## 📄 License

GhostVault is released under the MIT License. See [LICENSE](LICENSE) for details.

## 🆘 Support

### Getting Help
1. Check the built-in help system (F1 key)
2. Review this README and documentation
3. Search existing issues on GitHub
4. Create a new issue with detailed information

### Reporting Security Issues
Please report security vulnerabilities privately to [security@ghostvault.com](mailto:security@ghostvault.com).

## 🏆 Acknowledgments

- JavaFX team for the excellent UI framework
- Bouncy Castle for cryptographic implementations
- The security community for best practices guidance
- All contributors and testers

---

**⚠️ Important Security Notice**: GhostVault is designed for legitimate privacy and security needs. Users are responsible for complying with all applicable laws and regulations in their jurisdiction. The panic mode feature permanently destroys data and should be used with extreme caution.
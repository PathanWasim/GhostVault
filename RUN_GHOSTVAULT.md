# How to Run GhostVault

## Prerequisites
- Java 17 or higher installed
- The application has been built successfully

## Running the Application

### Method 1: Using the JAR file (Easiest)
```bash
java -jar target/ghostvault-1.0.0.jar
```

### Method 2: Using Maven
```bash
mvn javafx:run
```

### Method 3: Using Maven exec plugin
```bash
mvn exec:java -Dexec.mainClass="com.ghostvault.GhostVault"
```

## First Run Setup

When you run GhostVault for the first time, you'll see the **Initial Setup** screen where you need to configure three passwords:

1. **Master Password** - Your main password to access the real vault
2. **Panic Password** - Emergency password that destroys all data
3. **Decoy Password** - Shows fake files to mislead attackers

### Password Requirements:
- Minimum 12 characters
- At least one uppercase letter
- At least one lowercase letter
- At least one number
- At least one special character
- All three passwords must be different

## Using GhostVault

### Master Mode (Real Vault)
- Enter your **Master Password** to access your real encrypted files
- Upload, download, and manage your secure files
- Create encrypted backups
- All files are encrypted with AES-256

### Decoy Mode (Fake Vault)
- Enter your **Decoy Password** to show fake files
- Appears identical to the real vault
- Contains realistic-looking documents
- Protects your real data from coercion

### Panic Mode (Emergency Destruction)
- Enter your **Panic Password** to destroy all data
- Silently wipes all files, metadata, and logs
- Appears as a normal failed login
- Cannot be undone - use with extreme caution!

## Troubleshooting

### "Module not found" error
If you see module-related errors, try:
```bash
java --module-path target/ghostvault-1.0.0.jar --module com.ghostvault/com.ghostvault.GhostVault
```

### "JavaFX runtime components are missing"
The shaded JAR should include all JavaFX dependencies. If you still see this error:
```bash
mvn clean install "-Dmaven.test.skip=true"
```
Then run again.

### Application won't start
1. Check Java version: `java -version` (should be 17+)
2. Rebuild the application: `mvn clean install "-Dmaven.test.skip=true"`
3. Check for error messages in the console

## Security Notes

⚠️ **Important Security Information:**

1. **Remember Your Passwords** - There is NO password recovery. If you forget your master password, your data is permanently lost.

2. **Panic Password** - Using the panic password will PERMANENTLY DELETE all your data. This cannot be undone.

3. **Backup Regularly** - Use the built-in backup feature to create encrypted backups of your vault.

4. **Secure Your System** - GhostVault encrypts your files, but your system should also be secure (antivirus, firewall, etc.).

5. **Physical Security** - Ensure your computer is physically secure. Encryption doesn't protect against physical access to your device.

## Features

✅ **Military-Grade Encryption** - AES-256-CBC with PBKDF2 key derivation
✅ **Triple Password System** - Master, Panic, and Decoy passwords
✅ **Secure File Storage** - All files encrypted at rest
✅ **Encrypted Backups** - Create and restore encrypted vault backups
✅ **Audit Logging** - Track all vault operations
✅ **Session Management** - Auto-logout after inactivity
✅ **Threat Detection** - Monitor for suspicious activity
✅ **Secure Deletion** - DoD 5220.22-M standard file wiping

## Keyboard Shortcuts

- **Ctrl+O** - Upload file
- **Ctrl+S** - Download selected file
- **Delete** - Delete selected file
- **Ctrl+F** - Focus search box
- **Ctrl+B** - Create backup
- **Ctrl+R** - Restore from backup
- **Ctrl+L** - Logout
- **F1** - Show help

## Support

For issues or questions:
1. Check the console output for error messages
2. Review the audit logs in the vault directory
3. Ensure all prerequisites are met
4. Try rebuilding the application

## Vault Directory Structure

```
ghostvault-data/
├── vault/           # Encrypted files
├── metadata/        # File metadata (encrypted)
├── config/          # Password hashes and configuration
├── audit/           # Audit logs (encrypted)
├── decoys/          # Decoy files (for decoy mode)
└── backups/         # Encrypted backups
```

## Quick Start Guide

1. **Build the application:**
   ```bash
   mvn clean install "-Dmaven.test.skip=true"
   ```

2. **Run GhostVault:**
   ```bash
   java -jar target/ghostvault-1.0.0.jar
   ```

3. **Set up your passwords** (first run only)

4. **Start using your secure vault!**

---

**GhostVault** - Your files, your secrets, your security. 🔐

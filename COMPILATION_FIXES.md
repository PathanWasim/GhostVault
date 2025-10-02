# GhostVault Compilation Fixes

## Summary
The project has API mismatches between old and new implementations. The new PasswordManager and CryptoManager APIs have changed, but some files still use the old API.

## Files with Errors (19 total)

### Category 1: Unused Legacy Files (Can be deleted)
1. **VaultManager.java** - Not used (ApplicationIntegrator is the main coordinator)
2. **VaultInitializer.java** - Not used
3. **BackupManager.java** (core package) - Not used (VaultBackupManager is used instead)
4. **ScreenLockManager.java** - Not used

### Category 2: Used Files Needing Fixes

#### A. EncryptedData Type Mismatches (byte[] vs EncryptedData)
5. **FileManager.java** - Line 66: encrypt() returns byte[], needs EncryptedData
6. **MetadataManager.java** - Line 190: encrypt() returns byte[], needs EncryptedData
7. **AuditManager.java** - Line 320: encrypt() returns byte[], needs EncryptedData
8. **VaultBackupManager.java** - Line 324: encrypt() returns byte[], needs EncryptedData

#### B. CryptoManager API Changes
9. **VaultBackupManager.java** - Lines 337, 400: secureWipe() → zeroize()

#### C. KDF API Issues
10. **KDF.java** - Lines 167, 268, 278: Argon2 hash() method signature mismatch

## Fix Strategy

### Step 1: Delete Unused Files
- VaultManager.java
- VaultInitializer.java  
- core/BackupManager.java
- ScreenLockManager.java

### Step 2: Fix EncryptedData Issues
The new CryptoManager.encrypt() with SecretKey returns byte[] (combined IV+ciphertext).
Need to wrap in EncryptedData.fromCombinedData() or use the EncryptedData-returning overload.

### Step 3: Fix CryptoManager Method Names
- secureWipe() → zeroize()

### Step 4: Fix KDF Argon2 Issues
The Argon2 library hash() method doesn't accept the hashLength parameter we're passing.
Need to remove that parameter.

## Status
- ApplicationIntegrator: ✅ FIXED
- Remaining: 19 errors to fix

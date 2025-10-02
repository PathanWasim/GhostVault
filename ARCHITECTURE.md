# GhostVault Architecture Documentation

## System Architecture Overview

This document provides detailed architectural diagrams and explanations for the GhostVault secure file vault application.

---

## 1. High-Level System Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         USER INTERFACE                           │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │    Login     │  │  Vault Main  │  │ File Manager │          │
│  │  Controller  │  │  Controller  │  │  Controller  │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │ Initial Setup│  │   Backup     │  │  Audit Log   │          │
│  │  Controller  │  │  Controller  │  │  Controller  │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                    APPLICATION INTEGRATION                       │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │           ApplicationIntegrator (Facade)                  │   │
│  │  - Coordinates all components                             │   │
│  │  - Manages application lifecycle                          │   │
│  │  - Handles component communication                        │   │
│  └──────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                      BUSINESS LOGIC LAYER                        │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │    Vault     │  │     File     │  │   Metadata   │          │
│  │   Manager    │  │   Manager    │  │   Manager    │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │    Backup    │  │    Decoy     │  │    Audit     │          │
│  │   Manager    │  │   Manager    │  │   Manager    │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                       SECURITY LAYER                             │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │    Crypto    │  │   Password   │  │   Session    │          │
│  │   Manager    │  │   Manager    │  │   Manager    │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │ Panic Mode   │  │   Security   │  │    Threat    │          │
│  │  Executor    │  │   Monitor    │  │   Detection  │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                        STORAGE LAYER                             │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │  Encrypted   │  │  Encrypted   │  │  Encrypted   │          │
│  │    Files     │  │   Metadata   │  │  Audit Logs  │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
│  ┌──────────────┐  ┌──────────────┐                             │
│  │    Decoy     │  │    Config    │                             │
│  │    Files     │  │    Files     │                             │
│  └──────────────┘  └──────────────┘                             │
└─────────────────────────────────────────────────────────────────┘
```

---

## 2. Component Interaction Diagram

### File Upload Flow

```
User → LoginController → PasswordManager → CryptoManager
                              ↓
                    VaultMainController
                              ↓
                    FileManagementController
                              ↓
                         FileManager
                              ↓
                    ┌─────────┴─────────┐
                    ↓                   ↓
              CryptoManager      MetadataManager
                    ↓                   ↓
              Encrypt File        Store Metadata
                    ↓                   ↓
              File System         Encrypted DB
```

### Authentication Flow

```
User Input (Password)
        ↓
  LoginController
        ↓
  PasswordManager.validatePassword()
        ↓
  ┌─────┴─────┬─────────┬─────────┐
  ↓           ↓         ↓         ↓
MASTER     PANIC     DECOY    INVALID
  ↓           ↓         ↓         ↓
Normal    Silent    Fake      Error
Vault     Wipe      Vault    Message
```

### Panic Mode Execution Flow

```
Panic Password Entered
        ↓
  PasswordManager detects PANIC
        ↓
  PanicModeExecutor.execute()
        ↓
  ┌─────┴─────┬─────────┬─────────┬─────────┐
  ↓           ↓         ↓         ↓         ↓
Wipe      Wipe      Wipe      Wipe      Wipe
Files   Metadata   Logs    Config   Decoys
  ↓           ↓         ↓         ↓         ↓
  └─────┬─────┴─────────┴─────────┴─────────┘
        ↓
  System.exit(0)
  (Silent termination)
```

---

## 3. Security Architecture

### Cryptographic Flow

```
┌─────────────────────────────────────────────────────────┐
│                    Password Input                        │
└─────────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────┐
│              PBKDF2WithHmacSHA256                        │
│              100,000+ iterations                         │
│              + Secure Salt                               │
└─────────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────┐
│              256-bit Encryption Key                      │
└─────────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────┐
│              AES-256-CBC Encryption                      │
│              + Random IV per file                        │
│              + HMAC-SHA256 authentication                │
└─────────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────┐
│              Encrypted File Storage                      │
│              + SHA-256 integrity hash                    │
└─────────────────────────────────────────────────────────┘
```

### Security Layers

```
┌─────────────────────────────────────────────────────────┐
│  Layer 1: Authentication                                 │
│  - Triple password system                                │
│  - PBKDF2 hashing                                        │
│  - Constant-time comparison                              │
└─────────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────┐
│  Layer 2: Encryption                                     │
│  - AES-256-CBC                                           │
│  - Unique IV per file                                    │
│  - HMAC authentication                                   │
└─────────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────┐
│  Layer 3: Integrity                                      │
│  - SHA-256 hashing                                       │
│  - Metadata verification                                 │
│  - Tamper detection                                      │
└─────────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────┐
│  Layer 4: Session Security                               │
│  - Automatic timeout                                     │
│  - Activity monitoring                                   │
│  - Memory wiping                                         │
└─────────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────┐
│  Layer 5: Threat Detection                               │
│  - Failed login tracking                                 │
│  - Suspicious pattern detection                          │
│  - Duress indicators                                     │
└─────────────────────────────────────────────────────────┘
```

---

## 4. Data Flow Diagrams

### File Encryption Data Flow

```
┌──────────────┐
│ Original File│
└──────┬───────┘
       ↓
┌──────────────┐
│  Read File   │
│   Content    │
└──────┬───────┘
       ↓
┌──────────────┐
│ Generate IV  │
│ (Random 16B) │
└──────┬───────┘
       ↓
┌──────────────┐
│  AES-256-CBC │
│  Encryption  │
└──────┬───────┘
       ↓
┌──────────────┐
│ Calculate    │
│ HMAC-SHA256  │
└──────┬───────┘
       ↓
┌──────────────┐
│ Calculate    │
│  SHA-256     │
│  (Original)  │
└──────┬───────┘
       ↓
┌──────────────┐
│ Store:       │
│ - Ciphertext │
│ - IV         │
│ - HMAC       │
│ - Hash       │
└──────────────┘
```

### Metadata Management Flow

```
┌──────────────┐
│  File Info   │
│ - Name       │
│ - Size       │
│ - Type       │
│ - Timestamp  │
└──────┬───────┘
       ↓
┌──────────────┐
│  Serialize   │
│  to JSON     │
└──────┬───────┘
       ↓
┌──────────────┐
│  Encrypt     │
│  Metadata    │
└──────┬───────┘
       ↓
┌──────────────┐
│  Store in    │
│ metadata.enc │
└──────────────┘
```

---

## 5. Class Relationships

### Core Security Classes

```
┌─────────────────────┐
│  SecurityManager    │
│  (Coordinator)      │
└──────────┬──────────┘
           │
    ┌──────┴──────┬──────────┬──────────┐
    ↓             ↓          ↓          ↓
┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐
│ Crypto  │  │Password │  │ Session │  │  Panic  │
│ Manager │  │ Manager │  │ Manager │  │  Mode   │
└─────────┘  └─────────┘  └─────────┘  └─────────┘
     │            │            │            │
     ↓            ↓            ↓            ↓
┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐
│  JCE    │  │ PBKDF2  │  │ Timeout │  │ Secure  │
│  API    │  │  Hash   │  │ Monitor │  │ Delete  │
└─────────┘  └─────────┘  └─────────┘  └─────────┘
```

### File Management Classes

```
┌─────────────────────┐
│   VaultManager      │
│   (Facade)          │
└──────────┬──────────┘
           │
    ┌──────┴──────┬──────────┬──────────┐
    ↓             ↓          ↓          ↓
┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐
│  File   │  │Metadata │  │ Backup  │  │  Decoy  │
│ Manager │  │ Manager │  │ Manager │  │ Manager │
└─────────┘  └─────────┘  └─────────┘  └─────────┘
     │            │            │            │
     ↓            ↓            ↓            ↓
┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐
│  File   │  │Encrypted│  │ Backup  │  │  Fake   │
│ System  │  │   DB    │  │  File   │  │  Files  │
└─────────┘  └─────────┘  └─────────┘  └─────────┘
```

---

## 6. State Diagrams

### Application State Machine

```
┌─────────────┐
│   STARTUP   │
└──────┬──────┘
       ↓
┌─────────────┐
│ FIRST_RUN?  │
└──────┬──────┘
       │
   ┌───┴───┐
   ↓       ↓
  YES      NO
   ↓       ↓
┌──────┐  ┌──────┐
│SETUP │  │LOGIN │
└──┬───┘  └───┬──┘
   │          │
   └────┬─────┘
        ↓
┌───────────────┐
│ AUTHENTICATE  │
└───────┬───────┘
        │
   ┌────┴────┬────────┬────────┐
   ↓         ↓        ↓        ↓
┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐
│MASTER│ │PANIC │ │DECOY │ │ERROR │
└──┬───┘ └──┬───┘ └──┬───┘ └──┬───┘
   ↓        ↓        ↓        ↓
┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐
│VAULT │ │WIPE  │ │FAKE  │ │LOGIN │
│ VIEW │ │& EXIT│ │VAULT │ │RETRY │
└──┬───┘ └──────┘ └──┬───┘ └──────┘
   │                 │
   ↓                 ↓
┌──────┐          ┌──────┐
│ACTIVE│          │ACTIVE│
│SESSION│         │SESSION│
└──┬───┘          └──┬───┘
   │                 │
   ↓                 ↓
┌──────┐          ┌──────┐
│LOGOUT│          │LOGOUT│
└──────┘          └──────┘
```

### Session State Machine

```
┌─────────────┐
│   INACTIVE  │
└──────┬──────┘
       ↓
┌─────────────┐
│   START     │
│   SESSION   │
└──────┬──────┘
       ↓
┌─────────────┐
│   ACTIVE    │◄──────┐
└──────┬──────┘       │
       │              │
       ↓              │
┌─────────────┐       │
│  ACTIVITY   │───────┘
│  DETECTED   │ (Reset Timer)
└─────────────┘
       │
       ↓ (No Activity)
┌─────────────┐
│   WARNING   │
│  (1 minute) │
└──────┬──────┘
       │
   ┌───┴───┐
   ↓       ↓
Activity  Timeout
   │       │
   │       ↓
   │   ┌─────────────┐
   │   │   TIMEOUT   │
   │   └──────┬──────┘
   │          ↓
   │   ┌─────────────┐
   │   │   LOGOUT    │
   │   └─────────────┘
   │
   └──► (Back to ACTIVE)
```

---

## 7. Deployment Architecture

### Single-Machine Deployment

```
┌─────────────────────────────────────────────────────────┐
│                    User's Computer                       │
│                                                          │
│  ┌────────────────────────────────────────────────┐    │
│  │         GhostVault Application                  │    │
│  │         (ghostvault.jar)                        │    │
│  └────────────────────────────────────────────────┘    │
│                         ↓                                │
│  ┌────────────────────────────────────────────────┐    │
│  │         Java Runtime Environment (JRE 17+)      │    │
│  └────────────────────────────────────────────────┘    │
│                         ↓                                │
│  ┌────────────────────────────────────────────────┐    │
│  │         Operating System                        │    │
│  │         (Windows / macOS / Linux)               │    │
│  └────────────────────────────────────────────────┘    │
│                         ↓                                │
│  ┌────────────────────────────────────────────────┐    │
│  │         Local File System                       │    │
│  │         ~/.ghostvault/                          │    │
│  │         - files/                                │    │
│  │         - metadata.enc                          │    │
│  │         - config/                               │    │
│  │         - audit.log.enc                         │    │
│  └────────────────────────────────────────────────┘    │
│                                                          │
└─────────────────────────────────────────────────────────┘

Note: NO network connectivity required
      Completely offline operation
```

---

## 8. Error Handling Architecture

### Exception Hierarchy

```
┌─────────────────────────┐
│  GhostVaultException    │
│  (Base Exception)       │
└───────────┬─────────────┘
            │
    ┌───────┴───────┬──────────┬──────────┬──────────┐
    ↓               ↓          ↓          ↓          ↓
┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐
│Crypto   │  │Security │  │  File   │  │  Auth   │  │ Backup  │
│Exception│  │Exception│  │Operation│  │Exception│  │Exception│
└─────────┘  └─────────┘  │Exception│  └─────────┘  └─────────┘
                          └─────────┘
```

### Error Recovery Flow

```
┌─────────────┐
│   Error     │
│  Occurs     │
└──────┬──────┘
       ↓
┌─────────────┐
│  Catch      │
│  Exception  │
└──────┬──────┘
       ↓
┌─────────────┐
│  Log Error  │
│  (Encrypted)│
└──────┬──────┘
       ↓
┌─────────────┐
│  Determine  │
│  Severity   │
└──────┬──────┘
       │
   ┌───┴───┬────────┬────────┐
   ↓       ↓        ↓        ↓
┌──────┐┌──────┐┌──────┐┌──────┐
│MINOR ││MEDIUM││MAJOR ││CRITICAL│
└──┬───┘└──┬───┘└──┬───┘└──┬───┘
   ↓       ↓        ↓        ↓
┌──────┐┌──────┐┌──────┐┌──────┐
│Retry ││Show  ││Rollback││Exit │
│Auto  ││Error ││Changes││Safe │
└──────┘└──────┘└──────┘└──────┘
```

---

## 9. Performance Architecture

### Optimization Strategies

```
┌─────────────────────────────────────────────────────────┐
│  Strategy 1: Streaming Encryption                        │
│  - Process large files in chunks (1MB)                   │
│  - Avoid loading entire file into memory                 │
│  - Progress reporting per chunk                          │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│  Strategy 2: Background Processing                       │
│  - Separate threads for I/O operations                   │
│  - Non-blocking UI during encryption                     │
│  - Cancellable operations                                │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│  Strategy 3: Metadata Caching                            │
│  - Keep frequently accessed metadata in memory           │
│  - Lazy loading of file lists                            │
│  - Efficient search indexing                             │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│  Strategy 4: Memory Management                           │
│  - Secure wiping of sensitive data                       │
│  - Efficient buffer reuse                                │
│  - Garbage collection optimization                       │
└─────────────────────────────────────────────────────────┘
```

---

## 10. Testing Architecture

### Test Pyramid

```
                    ┌─────────┐
                    │  E2E    │
                    │  Tests  │
                    └─────────┘
                 ┌───────────────┐
                 │  Integration  │
                 │     Tests     │
                 └───────────────┘
            ┌─────────────────────────┐
            │      Unit Tests         │
            │   (Largest Coverage)    │
            └─────────────────────────┘
```

### Test Coverage by Layer

```
┌─────────────────────────────────────────────────────────┐
│  UI Layer                                                │
│  Coverage: 70%                                           │
│  - Controller tests                                      │
│  - UI component tests                                    │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│  Business Logic Layer                                    │
│  Coverage: 90%                                           │
│  - File operation tests                                  │
│  - Metadata management tests                             │
│  - Backup/restore tests                                  │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│  Security Layer                                          │
│  Coverage: 95%                                           │
│  - Cryptographic tests                                   │
│  - Password management tests                             │
│  - Session management tests                              │
│  - Panic mode tests                                      │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│  Integration Tests                                       │
│  Coverage: 80%                                           │
│  - End-to-end workflows                                  │
│  - Component interaction                                 │
│  - Security scenarios                                    │
└─────────────────────────────────────────────────────────┘
```

---

## Summary

This architecture provides:

✅ **Modularity** - Clear separation of concerns  
✅ **Security** - Multiple layers of protection  
✅ **Scalability** - Handles large files and many files  
✅ **Maintainability** - Well-organized, documented code  
✅ **Testability** - Comprehensive test coverage  
✅ **Performance** - Optimized for speed and memory  
✅ **Reliability** - Robust error handling and recovery  

The architecture supports all security requirements while maintaining excellent performance and user experience.

---

*Last Updated: 2025-10-02*

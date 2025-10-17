# Building GhostVault

## Prerequisites
- Java 17+ ([Download](https://adoptium.net/))
- Maven 3.6+ ([Download](https://maven.apache.org/download.cgi))

## Quick Build

### Windows
```bash
build-executable.bat
```

### Linux/Mac
```bash
mvn clean package -Dmaven.test.skip=true
mkdir -p dist
cp target/ghostvault-1.0.0.jar dist/GhostVault.jar
```

## Output

The build creates a `dist/` folder with:
- `GhostVault.jar` - Complete application (~50MB)
- `GhostVault.bat` - Windows launcher (created by build script)
- `README.txt` - User instructions (created by build script)

## Running

```bash
# From dist folder
java -jar GhostVault.jar

# Or on Windows
GhostVault.bat
```

## Development

```bash
# Run directly from source
mvn javafx:run

# Compile only
mvn compile
```

## Troubleshoot

**JavaFX errors**: Make sure you're using Java 17+ with JavaFX support.

**Build fails**: Try `mvn clean` first, then rebuild.

**Tests fail**: Use `-Dmaven.test.skip=true` to skip tests during build.
#!/bin/bash

echo "===================================================="
echo "           GhostVault Test Execution Script"
echo "===================================================="
echo

# Set up environment
export CLASSPATH=".:src/main/java:src/test/java"

echo "🔧 Compiling GhostVault..."
echo "----------------------------------------------------"

# Create output directories
mkdir -p build/classes/main
mkdir -p build/classes/test

# Compile main source files
echo "Compiling main source files..."
find src/main/java -name "*.java" | xargs javac -d build/classes/main -cp "$CLASSPATH"

if [ $? -ne 0 ]; then
    echo "❌ Main source compilation failed!"
    exit 1
fi

# Compile test source files
echo "Compiling test source files..."
find src/test/java -name "*.java" | xargs javac -d build/classes/test -cp "$CLASSPATH:build/classes/main"

if [ $? -ne 0 ]; then
    echo "❌ Test source compilation failed!"
    exit 1
fi

echo "✅ Compilation successful!"
echo

echo "🧪 Running Comprehensive Test Suite..."
echo "----------------------------------------------------"

# Run the comprehensive test runner
java -cp "build/classes/main:build/classes/test" com.ghostvault.ComprehensiveTestRunner

if [ $? -eq 0 ]; then
    echo
    echo "🎉 All tests completed successfully!"
    echo "GhostVault is ready for use."
else
    echo
    echo "❌ Some tests failed. Please review the output above."
    echo "Fix any issues before using GhostVault."
    exit 1
fi

echo
echo "Test execution completed."
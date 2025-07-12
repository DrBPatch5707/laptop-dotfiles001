#!/bin/bash

# Project Manager GUI Test Script
# This script compiles and runs the GUI test application

echo "=== PROJECT MANAGER GUI TEST SCRIPT ==="
echo ""

# Check if we're in the right directory
if [ ! -f "Project.java" ]; then
    echo "Error: Project.java not found. Please run this script from the gui/org/drbpatch directory."
    exit 1
fi

echo "Step 1: Compiling Java files..."
echo ""

# Compile all Java files
javac *.java

if [ $? -eq 0 ]; then
    echo "✓ Compilation successful!"
    echo ""
    echo "Step 2: Running test application..."
    echo ""
    
    # Run the test application
    java gui.org.drbpatch.TestProjectManagerApp
    
    if [ $? -eq 0 ]; then
        echo ""
        echo "✓ Test application completed successfully!"
    else
        echo ""
        echo "✗ Test application encountered an error."
        exit 1
    fi
else
    echo ""
    echo "✗ Compilation failed. Please check for errors above."
    exit 1
fi

echo ""
echo "=== TEST COMPLETE ===" 
#!/bin/bash
# GhostVault Unix/Linux/Mac Build Script
while true; do
    clear
    echo "============================="
    echo "  GhostVault Build Menu"
    echo "============================="
    echo "1. Clean"
    echo "2. Compile"
    echo "3. Run"
    echo "4. Test"
    echo "5. Package"
    echo "6. Quick Start"
    echo "7. Exit"
    read -p "Choose an option: " option
    case $option in
        1) mvn clean ;;
        2) mvn compile ;;
        3) mvn javafx:run ;;
        4) mvn test ;;
        5) mvn package ;;
        6) mvn clean compile && mvn javafx:run ;;
        7) exit ;;
        *) echo "Invalid option" ;;
    esac
    read -p "Press [Enter] to continue..."
done

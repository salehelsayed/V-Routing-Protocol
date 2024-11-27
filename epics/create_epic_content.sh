#!/bin/bash

# Function to extract content between sections
extract_section() {
    local start_pattern="## $1"
    local section_content
    
    # Read epics.md and extract content between this section and the next
    section_content=$(awk "/$start_pattern/,/^## /" epics/epics.md | sed '$d' | sed '1d')
    
    # If content is empty, try to match until the end of file
    if [ -z "$section_content" ]; then
        section_content=$(awk "/$start_pattern/,/^---/" epics/epics.md | sed '$d' | sed '1d')
    fi
    
    echo "$section_content"
}

# Create epic.md for Introduction
echo "# Introduction" > "1. Introduction/epic.md"
extract_section "Introduction" >> "1. Introduction/epic.md"

# Create epic.md for Epic 0
echo "# Epic 0: Project Initialization and Setup" > "2. Epic 0 - Project Initialization and Setup/epic.md"
extract_section "Epic 0: Project Initialization and Setup" >> "2. Epic 0 - Project Initialization and Setup/epic.md"

# Create epic.md for Epic 1
echo "# Epic 1: Core Peer-to-Peer Networking" > "3. Epic 1 - Core Peer-to-Peer Networking/epic.md"
extract_section "Epic 1: Core Peer-to-Peer Networking" >> "3. Epic 1 - Core Peer-to-Peer Networking/epic.md"

# Create epic.md for Epic 2
echo "# Epic 2: Data and State Management" > "4. Epic 2 - Data and State Management/epic.md"
extract_section "Epic 2: Data and State Management" >> "4. Epic 2 - Data and State Management/epic.md"

# Create epic.md for Epic 3
echo "# Epic 3: Social Network Features" > "5. Epic 3 - Social Network Features/epic.md"
extract_section "Epic 3: Social Network Features" >> "5. Epic 3 - Social Network Features/epic.md"

# Create epic.md for Epic 4
echo "# Epic 4: User Interface (UI/UX)" > "6. Epic 4 - User Interface (UIUX)/epic.md"
extract_section "Epic 4: User Interface (UI/UX)" >> "6. Epic 4 - User Interface (UIUX)/epic.md"

# Create epic.md for Epic 5
echo "# Epic 5: Security and Privacy" > "7. Epic 5 - Security and Privacy/epic.md"
extract_section "Epic 5: Security and Privacy" >> "7. Epic 5 - Security and Privacy/epic.md"

# Create epic.md for Epic 6
echo "# Epic 6: Notifications and Real-Time Updates" > "8. Epic 6 - Notifications and Real-Time Updates/epic.md"
extract_section "Epic 6: Notifications and Real-Time Updates" >> "8. Epic 6 - Notifications and Real-Time Updates/epic.md"

# Create epic.md for Epic 7
echo "# Epic 7: Testing and Optimization" > "9. Epic 7 - Testing and Optimization/epic.md"
extract_section "Epic 7: Testing and Optimization" >> "9. Epic 7 - Testing and Optimization/epic.md"

# Create epic.md for Epic 8
echo "# Epic 8: Deployment" > "10. Epic 8 - Deployment/epic.md"
extract_section "Epic 8: Deployment" >> "10. Epic 8 - Deployment/epic.md"

# Create epic.md for Task Classification
echo "# Task Classification" > "11. Task Classification/epic.md"
extract_section "Task Classification" >> "11. Task Classification/epic.md"

# Create epic.md for Conclusion
echo "# Conclusion" > "12. Conclusion/epic.md"
extract_section "Conclusion" >> "12. Conclusion/epic.md"

echo "Epic.md files created successfully in each directory!"

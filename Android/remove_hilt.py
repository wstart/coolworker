# Script to remove Hilt annotations from all ViewModel files

import os
import re

viewmodel_files = [
    "/Users/bbc/Desktop/MyApplication/app/src/main/java/com/easywork/ai/presentation/server/ServerListViewModel.kt",
    "/Users/bbc/Desktop/MyApplication/app/src/main/java/com/easywork/ai/presentation/server/ServerEditViewModel.kt",
    "/Users/bbc/Desktop/MyApplication/app/src/main/java/com/easywork/ai/presentation/session/SessionListViewModel.kt",
    "/Users/bbc/Desktop/MyApplication/app/src/main/java/com/easywork/ai/presentation/session/SessionViewModel.kt",
    "/Users/bbc/Desktop/MyApplication/app/src/main/java/com/easywork/ai/presentation/terminal/TerminalViewModel.kt",
]

for filepath in viewmodel_files:
    if not os.path.exists(filepath):
        print(f"File not found: {filepath}")
        continue

    with open(filepath, 'r') as f:
        content = f.read()

    # Remove Hilt imports
    content = re.sub(r'import dagger\.hilt\.android\.lifecycle\.HiltViewModel\n?', '', content)
    content = re.sub(r'import dagger\.hilt\.android\.navigation\.HiltViewModel\n?', '', content)
    content = re.sub(r'import javax\.inject\.Inject\n?', '', content)

    # Remove @HiltViewModel annotation
    content = re.sub(r'@HiltViewModel\nclass ', 'class ', content)

    # Remove @Inject constructor
    content = re.sub(r'@Inject\s+constructor', 'constructor', content)
    content = re.sub(r'@Inject\n', '', content)

    # Clean up empty imports
    content = re.sub(r'\nimport\s+\n', '\n', content)

    with open(filepath, 'w') as f:
        f.write(content)

    print(f"Processed: {os.path.basename(filepath)}")

print("\nAll ViewModel files have been updated to remove Hilt annotations")

# Script to update Screen files to use viewModel() instead of hiltViewModel()

import os
import re

screen_files = [
    "/Users/bbc/Desktop/MyApplication/app/src/main/java/com/easywork/ai/presentation/server/ServerListScreen.kt",
    "/Users/bbc/Desktop/MyApplication/app/src/main/java/com/easywork/ai/presentation/server/ServerEditScreen.kt",
    "/Users/bbc/Desktop/MyApplication/app/src/main/java/com/easywork/ai/presentation/session/SessionListScreen.kt",
    "/Users/bbc/Desktop/MyApplication/app/src/main/java/com/easywork/ai/presentation/terminal/TerminalScreen.kt",
]

for filepath in screen_files:
    if not os.path.exists(filepath):
        print(f"File not found: {filepath}")
        continue

    with open(filepath, 'r') as f:
        content = f.read()

    # Remove hilt.navigation.compose import if not needed
    content = re.sub(
        r'import androidx\.hilt\.navigation\.compose\.hiltViewModel\n?',
        '',
        content
    )

    # Add viewModel factory import if needed
    if 'androidx.lifecycle.viewmodel.compose.viewModel' not in content:
        # Find the import section
        import_match = re.search(r'import androidx\.compose\.runtime\.\*[\s\S]*?(?=\nimport|@Composable|\n\n)', content)
        if import_match:
            insert_pos = import_match.end()
            content = content[:insert_pos] + 'import androidx.lifecycle.viewmodel.compose.viewModel\n' + content[insert_pos:]

    # Replace hiltViewModel() with viewModel()
    content = re.sub(r'hiltViewModel\(', 'viewModel(', content)

    with open(filepath, 'w') as f:
        f.write(content)

    print(f"Processed: {os.path.basename(filepath)}")

print("\nAll Screen files have been updated to use viewModel() factory")

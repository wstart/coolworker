# Remove all Hilt annotations
import os
import re
import glob

# Find all Kotlin files
files = glob.glob("/Users/bbc/Desktop/MyApplication/app/src/main/java/com/easywork/ai/**/*.kt")

for filepath in files:
    with open(filepath, 'r') as f:
        content = f.read()

    original = content

    # Remove Hilt imports
    content = re.sub(r'import dagger\.hilt[^\n]*\n?', '', content)
    content = re.sub(r'import javax\.inject\.Inject[^\n]*\n?', '', content)
    content = re.sub(r'import javax\.inject\.Singleton[^\n]*\n?', '', content)

    # Remove @Inject annotations
    content = re.sub(r'@Inject\s+constructor', 'constructor', content)

    # Remove @Singleton
    content = re.sub(r'@Singleton', '', content)

    # Remove @AndroidEntryPoint
    content = re.sub(r'@AndroidEntryPoint', '', content)

    # Clean up empty lines
    content = re.sub(r'\n\s*\n', '\n\n', content)

    if content != original:
        with open(filepath, 'w') as f:
            f.write(content)
        print(f"Updated: {os.path.relpath(filepath, '/Users/bbc/Desktop/MyApplication/app/src/main/java/com/easywork/ai')}")

print("\n✅ Removed all Hilt annotations")

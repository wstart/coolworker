# Script to create ViewModel factory functions that use AppContainer

import os
import re

# 定义每个Screen需要的ViewModel工厂函数
factory_functions = {
    "ServerListScreen.kt": """fun serverListViewModel(): ServerListViewModel {
        return ServerListViewModel(AppContainer.provideServerRepository())
    }""",

    "ServerEditScreen.kt": """fun serverEditViewModel(serverId: String): ServerEditViewModel {
        return ServerEditViewModel(
            AppContainer.provideServerRepository(),
            android.os.SavedStateHandle().apply {
                set("serverId", serverId)
            }
        )
    }""",

    "SessionListScreen.kt": """fun sessionListViewModel(serverId: String): SessionListViewModel {
        return SessionListViewModel(
            AppContainer.provideServerRepository(),
            AppContainer.provideConnectServerUseCase(),
            AppContainer.provideListTmuxSessionsUseCase(),
            AppContainer.provideCreateTmuxSessionUseCase(),
            AppContainer.provideSendCommandUseCase()
        )
    }""",

    "TerminalScreen.kt": """fun terminalViewModel(): TerminalViewModel {
        return TerminalViewModel(
            AppContainer.provideServerRepository(),
            AppContainer.provideSendCommandUseCase(),
            AppContainer.provideAutoCompleteUseCase()
        )
    }""",
}

for filename, factory_code in factory_functions.items():
    filepath = f"/Users/bbc/Desktop/MyApplication/app/src/main/java/com/easywork/ai/presentation/{filename.replace('Screen.kt', '')}/{filename}"

    if not os.path.exists(filepath):
        print(f"File not found: {filepath}")
        continue

    with open(filepath, 'r') as f:
        content = f.read()

    # Find the Screen function definition
    screen_match = re.search(r'@Composable\s*\nfun (\w+)\(', content)
    if not screen_match:
        print(f"Could not find Screen function in {filename}")
        continue

    screen_name = screen_match.group(1)

    # Insert factory function before the Screen function
    # Find the last import statement
    last_import = content.rfind('import ')
    if last_import != -1:
        end_import = content.find('\n', last_import)
        if end_import != -1:
            # Add factory function after imports
            content = content[:end_import+1] + f'\n{factory_code}\n' + content[end_import+1:]

    # Update viewModel() call to use our factory function
    if filename == "ServerListScreen.kt":
        content = re.sub(r'viewModel\(\)', 'serverListViewModel()', content)
    elif filename == "ServerEditScreen.kt":
        content = re.sub(r'viewModel\(\)', 'serverEditViewModel(serverId)', content)
    elif filename == "SessionListScreen.kt":
        content = re.sub(r'viewModel\(\)', 'sessionListViewModel(serverId)', content)
    elif filename == "TerminalScreen.kt":
        content = re.sub(r'viewModel\(\)', 'terminalViewModel()', content)

    with open(filepath, 'w') as f:
        f.write(content)

    print(f"Processed: {filename}")

print("\nAll Screen files have been updated with custom ViewModel factory functions")

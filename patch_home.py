import re

with open('app/src/main/java/com/spinel/zicola/zicola/ui/screens/HomeScreen.kt', 'r', encoding='utf-8') as f:
    code = f.read()

# Add imports for Icons
if "import androidx.compose.material.icons.Icons" not in code:
    code = code.replace("import androidx.compose.material3.*", "import androidx.compose.material3.*\nimport androidx.compose.material.icons.Icons\nimport androidx.compose.material.icons.filled.Settings")

# Update signature
if "onSettingsClick: () -> Unit" not in code:
    code = code.replace(
        "    onBookClick: (String) -> Unit\n) {",
        "    onBookClick: (String) -> Unit,\n    onSettingsClick: () -> Unit\n) {"
    )

# Add TopAppBar inside Scaffold
top_app_bar = """        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("أرض زيكولا") },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "الإعدادات"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }"""
if "topBar = {" not in code:
    code = code.replace("        containerColor = MaterialTheme.colorScheme.background\n    ) {", "        containerColor = MaterialTheme.colorScheme.background,\n" + top_app_bar + "\n    ) {")

with open('app/src/main/java/com/spinel/zicola/zicola/ui/screens/HomeScreen.kt', 'w', encoding='utf-8') as f:
    f.write(code)
print("HomeScreen patched")

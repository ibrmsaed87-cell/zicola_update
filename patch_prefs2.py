import re

with open('app/src/main/java/com/spinel/zicola/zicola/data/PreferencesManager.kt', 'r', encoding='utf-8') as f:
    code = f.read()

# Add APP_THEME
if "APP_THEME" not in code:
    code = code.replace("private val THEME = stringPreferencesKey(\"theme\")", "private val THEME = stringPreferencesKey(\"theme\")\n        private val APP_THEME = stringPreferencesKey(\"app_theme\")")
    
    # Add flow and save function
    app_theme_flow = """    val appThemeFlow: Flow<String> = context.dataStore.data.map { it[APP_THEME] ?: "SYSTEM" }

    suspend fun saveAppTheme(theme: String) {
        context.dataStore.edit { it[APP_THEME] = theme }
    }"""
    code = code.replace("    val themeFlow: Flow<String> = context.dataStore.data.map { it[THEME] ?: \"SEPIA\" }", "    val themeFlow: Flow<String> = context.dataStore.data.map { it[THEME] ?: \"SEPIA\" }\n" + app_theme_flow)

with open('app/src/main/java/com/spinel/zicola/zicola/data/PreferencesManager.kt', 'w', encoding='utf-8') as f:
    f.write(code)
print("Preferences patched")

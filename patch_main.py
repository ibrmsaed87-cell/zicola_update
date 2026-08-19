import re

with open('app/src/main/java/com/spinel/zicola/zicola/MainActivity.kt', 'r', encoding='utf-8') as f:
    code = f.read()

# Imports
imports = """
import androidx.compose.foundation.isSystemInDarkTheme
import com.spinel.zicola.zicola.data.PreferencesManager
import com.spinel.zicola.zicola.ui.screens.SettingsScreen
"""
if "import com.spinel.zicola.zicola.data.PreferencesManager" not in code:
    code = code.replace("import com.spinel.zicola.zicola.navigation.Route", imports.strip() + "\nimport com.spinel.zicola.zicola.navigation.Route")

# onCreate
on_create_old = """    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ZicolaTheme {
                ZicolaApp()
            }
        }
    }"""
on_create_new = """    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val prefs = PreferencesManager(this)
        setContent {
            val appTheme by prefs.appThemeFlow.collectAsState(initial = "SYSTEM")
            val darkTheme = when (appTheme) {
                "DARK" -> true
                "LIGHT" -> false
                else -> isSystemInDarkTheme()
            }
            ZicolaTheme(darkTheme = darkTheme) {
                ZicolaApp(prefs)
            }
        }
    }"""
code = code.replace(on_create_old, on_create_new)

# ZicolaApp definition
code = code.replace("fun ZicolaApp() {", "fun ZicolaApp(preferencesManager: PreferencesManager) {")

# Add Settings Route to NavHost
settings_route = """
        composable(Route.Settings.route) {
            SettingsScreen(
                preferencesManager = preferencesManager,
                onBackClick = { navController.popBackStack() }
            )
        }
"""
if "Route.Settings.route" not in code:
    code = code.replace("        composable(Route.Home.route) {", settings_route.strip() + "\n\n        composable(Route.Home.route) {")

# Also pass onSettingsClick to HomeScreen
if "HomeScreen(" in code and "onSettingsClick" not in code:
    code = code.replace(
        "onBookClick = { bookId ->",
        "onSettingsClick = { navController.navigate(Route.Settings.route) },\n                onBookClick = { bookId ->"
    )

with open('app/src/main/java/com/spinel/zicola/zicola/MainActivity.kt', 'w', encoding='utf-8') as f:
    f.write(code)
print("MainActivity patched")

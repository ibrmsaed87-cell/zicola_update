import re

with open('app/src/main/java/com/spinel/zicola/zicola/data/PreferencesManager.kt', 'r', encoding='utf-8') as f:
    code = f.read()

# Add import if missing
if "import kotlinx.coroutines.flow.first" not in code:
    code = code.replace("import kotlinx.coroutines.flow.map", "import kotlinx.coroutines.flow.map\nimport kotlinx.coroutines.flow.first\nimport java.util.UUID")

# Add DEVICE_ID to companion object
if "private val DEVICE_ID" not in code:
    code = code.replace("private val THEME = stringPreferencesKey(\"theme\")", "private val THEME = stringPreferencesKey(\"theme\")\n        private val DEVICE_ID = stringPreferencesKey(\"device_id\")")

# Add getOrCreateDeviceId function
device_id_func = """
    suspend fun getOrCreateDeviceId(): String {
        val preferences = context.dataStore.data.first()
        val existingId = preferences[DEVICE_ID]
        if (existingId != null) {
            return existingId
        }
        val newId = UUID.randomUUID().toString()
        context.dataStore.edit { it[DEVICE_ID] = newId }
        return newId
    }
"""
if "fun getOrCreateDeviceId" not in code:
    code = code.replace("companion object {", device_id_func + "\n    companion object {")

with open('app/src/main/java/com/spinel/zicola/zicola/data/PreferencesManager.kt', 'w', encoding='utf-8') as f:
    f.write(code)

print("PreferencesManager patched.")

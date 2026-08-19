import re

with open('app/src/main/java/com/spinel/zicola/zicola/MainActivity.kt', 'r', encoding='utf-8') as f:
    code = f.read()

imports = """
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
"""
code = code.replace("import android.os.Bundle", imports.strip() + "\nimport android.os.Bundle")

request_permission_code = """
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission is granted.
        } else {
            // Permission denied.
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // Permission already granted
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
"""

class_def = "class MainActivity : ComponentActivity() {"
new_class_def = class_def + "\n" + request_permission_code

code = code.replace(class_def, new_class_def)

on_create = """    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)"""
on_create_new = """    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        askNotificationPermission()"""

code = code.replace(on_create, on_create_new)

with open('app/src/main/java/com/spinel/zicola/zicola/MainActivity.kt', 'w', encoding='utf-8') as f:
    f.write(code)
print("MainActivity patched for notification permission")

with open('app/build.gradle.kts', 'r', encoding='utf-8') as f:
    code = f.read()

if "libs.firebase.messaging" not in code:
    code = code.replace("  implementation(libs.firebase.ai)", "  implementation(libs.firebase.ai)\n  implementation(libs.firebase.messaging)")

with open('app/build.gradle.kts', 'w', encoding='utf-8') as f:
    f.write(code)
print("App Gradle patched")

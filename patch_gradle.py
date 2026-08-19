with open('build.gradle.kts', 'r', encoding='utf-8') as f:
    code = f.read()

# Make sure google-services plugin is applied
if "alias(libs.plugins.google.services) apply false" not in code:
    print("google.services plugin not in project build.gradle.kts, adding...")
    # But it WAS there based on earlier cat, so skip this logic unless it wasn't
else:
    print("google.services plugin is already in project build.gradle.kts")

with open('app/build.gradle.kts', 'r', encoding='utf-8') as f:
    code = f.read()

# Enable google.services plugin
if "alias(libs.plugins.google.services)" not in code:
    print("google.services plugin not in app build.gradle.kts, adding...")
else:
    print("google.services plugin is already in app build.gradle.kts")


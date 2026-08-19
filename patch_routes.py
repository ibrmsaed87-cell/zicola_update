with open('app/src/main/java/com/spinel/zicola/zicola/navigation/Routes.kt', 'r', encoding='utf-8') as f:
    code = f.read()

if "object Settings" not in code:
    code = code.replace("object Home : Route(\"home\")", "object Home : Route(\"home\")\n    object Settings : Route(\"settings\")")

with open('app/src/main/java/com/spinel/zicola/zicola/navigation/Routes.kt', 'w', encoding='utf-8') as f:
    f.write(code)
print("Routes patched")

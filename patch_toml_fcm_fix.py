with open('gradle/libs.versions.toml', 'r', encoding='utf-8') as f:
    code = f.read()

# Remove the faulty firebase-messaging definition that lacked a version ref but didn't rely on BOM properly if we used it as ktx, but wait, BOM should handle it, BUT we didn't specify version. Wait, BOM handles versions but maybe `firebase-messaging-ktx` needs to be `firebase-messaging`?
# Actually, the BOM handles both. But we wrote `name = "firebase-messaging-ktx"`. Maybe it didn't find it. Let's change to `firebase-messaging`.

code = code.replace("name = \"firebase-messaging-ktx\"", "name = \"firebase-messaging\"")

with open('gradle/libs.versions.toml', 'w', encoding='utf-8') as f:
    f.write(code)
print("Toml patched")

with open('gradle/libs.versions.toml', 'r', encoding='utf-8') as f:
    code = f.read()

if "firebase-messaging =" not in code:
    code = code.replace("firebase-auth = { group = \"com.google.firebase\", name = \"firebase-auth\" }", "firebase-auth = { group = \"com.google.firebase\", name = \"firebase-auth\" }\nfirebase-messaging = { group = \"com.google.firebase\", name = \"firebase-messaging-ktx\" }")

with open('gradle/libs.versions.toml', 'w', encoding='utf-8') as f:
    f.write(code)
print("Toml patched")

import zipfile
import re

try:
    with zipfile.ZipFile("zicola-fcm-test.apk", "r") as z:
        manifest = z.read("AndroidManifest.xml")
        # AndroidManifest is binary XML, but string literals might be visible if we just extract strings.
        # But SDK versions are usually integers.
        # Let's see if we can find 'minSdkVersion' strings (they are in the string pool).
        # Actually, let's just use androguard if it's installed.
except Exception as e:
    pass

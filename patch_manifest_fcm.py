with open('app/src/main/AndroidManifest.xml', 'r', encoding='utf-8') as f:
    code = f.read()

# Add permissions
if "android.permission.POST_NOTIFICATIONS" not in code:
    code = code.replace("<application", "<uses-permission android:name=\"android.permission.POST_NOTIFICATIONS\" />\n    <application")

# Add service
service_decl = """
        <service
            android:name=".ZicolaFirebaseMessagingService"
            android:exported="false">
            <intent-filter>
                <action android:name="com.google.firebase.MESSAGING_EVENT" />
            </intent-filter>
        </service>
"""
if "ZicolaFirebaseMessagingService" not in code:
    code = code.replace("</application>", service_decl + "    </application>")

with open('app/src/main/AndroidManifest.xml', 'w', encoding='utf-8') as f:
    f.write(code)
print("Manifest patched")

# CallSpace Android wrapper

This Android Studio project packages the CallSpace HTML app in a native Android WebView. It keeps WebView local storage/IndexedDB within the installed app's private app data, opens Android's native file/photo chooser for image inputs, and supports exporting contacts through Android's Save As dialog.

## Build an APK with GitHub Actions (no Android SDK required on your computer)
1. Create a new GitHub repository and upload the contents of this folder to the repository root (including the `.github` folder).
2. Open the repository's **Actions** tab and choose **Build CallSpace APK**.
3. Click **Run workflow** and wait for the workflow to finish successfully.
4. Open the completed workflow run and download the **CallSpace-debug-apk** artifact.
5. Extract the downloaded ZIP; install `app-debug.apk` on your Android phone. Android may ask you to allow installation from that source.

## Build locally
Open this folder in Android Studio and let it sync, then choose **Build > Build APK(s)**. The APK is usually at `app/build/outputs/apk/debug/app-debug.apk`.

## Notes
- The HEIC converter currently uses the `heic2any` library from a CDN, so the first HEIC conversion requires an internet connection. Other image handling and app screens are bundled locally.
- Contacts are stored in the installed app's own WebView storage. They should persist across normal app restarts; uninstalling/clearing app data removes them. Use Export for backups.
- This is a debug APK intended for personal testing, not a Play Store release.

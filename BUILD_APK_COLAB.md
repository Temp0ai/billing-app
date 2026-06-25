# 📱 Build APK in Google Colab (5 Minutes, Browser Only)

## What You Need
- A Google account
- The `billing-app` folder (already created)

---

## Step 1: Zip Your Project

On your computer:
- **Windows:** Right-click `billing-app` folder → Send to → Compressed (zipped) folder
- **Mac:** Right-click `billing-app` folder → Compress
- **Linux:** `zip -r billing-app.zip billing-app/`

---

## Step 2: Open Google Colab

Go to: **https://colab.research.google.com**

Click: **File → New Notebook**

---

## Step 3: Run These Cells (Copy & Paste)

### Cell 1 — Install Java & Android SDK
```python
!apt-get update -qq && apt-get install -y -qq openjdk-17-jdk wget unzip
!wget -q https://dl.google.com/android/repository/commandlinetools-linux-10406996_latest.zip
!unzip -q commandlinetools-linux-10406996_latest.zip
!mkdir -p /opt/android-sdk/cmdline-tools && mv cmdline-tools /opt/android-sdk/cmdline-tools/latest
import os
os.environ['ANDROID_HOME'] = '/opt/android-sdk'
os.environ['PATH'] += ':/opt/android-sdk/cmdline-tools/latest/bin'
os.environ['JAVA_HOME'] = '/usr/lib/jvm/java-17-openjdk-amd64'
!java -version
```

### Cell 2 — Install SDK Components
```python
!yes | sdkmanager --licenses 2>/dev/null
!sdkmanager "platforms;android-34" "build-tools;34.0.0" "platform-tools"
print("✅ SDK Ready!")
```

### Cell 3 — Upload & Extract Your Project
```python
from google.colab import files
import shutil, os
uploaded = files.upload()  # ← Upload billing-app.zip here
for f in uploaded:
    if f.endswith('.zip'):
        shutil.unpack_archive(f, '/content/')
        print(f"✅ Extracted {f}")
```

### Cell 4 — Build the APK
```python
os.chdir('/content/billing-app')  # Adjust if folder name differs
!chmod +x gradlew
!./gradlew assembleDebug --no-daemon
print("✅ Build Complete!")
```

### Cell 5 — Download Your APK
```python
files.download('/content/billing-app/app/build/outputs/apk/debug/app-debug.apk')
```

---

## Step 4: Install on Your Phone

1. Transfer the `app-debug.apk` to your phone (USB, email, or cloud)
2. On your phone: **Settings → Security → Allow unknown sources**
3. Open the APK file and install

---

## Troubleshooting

| Error | Fix |
|-------|-----|
| `sdkmanager not found` | Re-run Cell 1 |
| `JAVA_HOME not set` | Re-run Cell 1 |
| `build.gradle.kts not found` | Make sure you zip the correct folder |
| `Could not find com.android.tools.build:gradle` | Check internet connection |
| Build fails | Read the error message, usually a missing dependency |

---

## 🎯 Quick Summary

```
Zip folder → Open Colab → Run 5 cells → Download APK → Install on phone
```

Total time: **~10 minutes** (mostly waiting for build)

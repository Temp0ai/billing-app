"""
========================================
  BUILD ANDROID APK IN GOOGLE COLAB
========================================

INSTRUCTIONS:
1. Go to https://colab.research.google.com
2. Click File → New Notebook
3. Copy each cell below and run them one by one
4. Download the APK at the end!

"""

# ===== CELL 1: Install Java & Android SDK =====
print("📦 Installing Java 17 and Android SDK...")
!apt-get update -qq
!apt-get install -y -qq openjdk-17-jdk wget unzip zip
!wget -q https://dl.google.com/android/repository/commandlinetools-linux-10406996_latest.zip
!unzip -q commandlinetools-linux-10406996_latest.zip
!mkdir -p /opt/android-sdk/cmdline-tools
!mv cmdline-tools /opt/android-sdk/cmdline-tools/latest

import os
os.environ['ANDROID_HOME'] = '/opt/android-sdk'
os.environ['ANDROID_SDK_ROOT'] = '/opt/android-sdk'
os.environ['PATH'] += ':/opt/android-sdk/cmdline-tools/latest/bin:/opt/android-sdk/platform-tools'
os.environ['JAVA_HOME'] = '/usr/lib/jvm/java-17-openjdk-amd64'

print("✅ Java and Android SDK installed!")
!java -version


# ===== CELL 2: Accept SDK Licenses & Install Build Tools =====
print("📋 Accepting SDK licenses...")
!yes | sdkmanager --licenses 2>/dev/null
!sdkmanager "platforms;android-34" "build-tools;34.0.0" "platform-tools"
print("✅ SDK ready!")


# ===== CELL 3: Upload Your Project =====
print("📁 Upload your billing-app project as a ZIP file")
print("   (Zip the billing-app folder on your computer first)")

from google.colab import files
import shutil

uploaded = files.upload()

for filename in uploaded.keys():
    if filename.endswith('.zip'):
        print(f"📂 Extracting {filename}...")
        shutil.unpack_archive(filename, '/content/')
        print("✅ Extracted!")

# Find the project directory
project_dir = None
for d in os.listdir('/content/'):
    if os.path.isdir(f'/content/{d}') and os.path.exists(f'/content/{d}/build.gradle.kts'):
        project_dir = f'/content/{d}'
        break
    # Check one level deep
    if os.path.isdir(f'/content/{d}'):
        for sub in os.listdir(f'/content/{d}'):
            if os.path.exists(f'/content/{d}/{sub}/build.gradle.kts'):
                project_dir = f'/content/{d}/{sub}'
                break

if project_dir:
    print(f"✅ Project found at: {project_dir}")
else:
    print("⚠️ Could not find build.gradle.kts. Make sure you zip the billing-app folder.")


# ===== CELL 4: Build the APK =====
if project_dir:
    print("🔨 Building Debug APK...")
    os.chdir(project_dir)
    !chmod +x gradlew
    !./gradlew assembleDebug --no-daemon --stacktrace 2>&1 | tail -20
    print("\n✅ Build complete!")
else:
    print("❌ Project not found. Please re-upload.")


# ===== CELL 5: Download the APK =====
if project_dir:
    apk_path = f"{project_dir}/app/build/outputs/apk/debug/app-debug.apk"
    if os.path.exists(apk_path):
        print(f"📱 APK found: {apk_path}")
        print("⬇️ Downloading...")
        files.download(apk_path)
    else:
        print("❌ APK not found. Check build errors above.")
        # List what's actually there
        !find {project_dir}/app/build -name "*.apk" 2>/dev/null
else:
    print("❌ No project to build from.")

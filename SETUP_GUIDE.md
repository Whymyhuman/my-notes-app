# 📋 Panduan Setup Lengkap - My Notes App

Panduan ini akan membantu Anda setup aplikasi Notes dari awal hingga deployment di GitHub Actions.

## 🎯 Langkah 1: Persiapan Environment

### A. Setup GitHub Repository

1. **Buat Repository Baru di GitHub**
   ```
   - Login ke GitHub
   - Klik "New Repository"
   - Nama: "my-notes-app" (atau sesuai keinginan)
   - Pilih "Public" untuk unlimited GitHub Actions
   - Jangan centang "Initialize with README" (karena kita sudah punya)
   ```

2. **Clone Repository Kosong**
   ```bash
   git clone https://github.com/username/my-notes-app.git
   cd my-notes-app
   ```

### B. Setup di Termux (Opsional)

Jika menggunakan Termux untuk development:

```bash
# Update packages
pkg update && pkg upgrade

# Install essential tools
pkg install git nodejs python build-essential

# Install Java (untuk Gradle)
pkg install openjdk-17

# Install text editor (pilih salah satu)
pkg install nano        # Editor sederhana
pkg install vim         # Editor advanced
pkg install neovim      # Vim modern

# Setup storage access
termux-setup-storage
```

## 🎯 Langkah 2: Copy Project Files

### A. Struktur Direktori yang Harus Dibuat

```
my-notes-app/
├── .github/workflows/
│   └── android-build.yml
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/example/notes/
│       │   ├── adapter/NotesAdapter.java
│       │   ├── data/Note.java
│       │   └── utils/TimeUtils.java
│       ├── kotlin/com/example/notes/
│       │   ├── data/
│       │   │   ├── NoteDao.kt
│       │   │   └── NoteDatabase.kt
│       │   ├── repository/NoteRepository.kt
│       │   ├── viewmodel/NoteViewModel.kt
│       │   ├── MainActivity.kt
│       │   └── AddEditNoteActivity.kt
│       └── res/
│           ├── layout/
│           ├── values/
│           ├── drawable/
│           ├── menu/
│           ├── xml/
│           └── mipmap-*/
├── gradle/wrapper/
│   ├── gradle-wrapper.jar
│   └── gradle-wrapper.properties
├── build.gradle.kts
├── gradle.properties
├── settings.gradle.kts
├── gradlew
├── .gitignore
└── README.md
```

### B. Copy Files dari Template

Salin semua file dari template project yang telah dibuat ke direktori repository Anda.

## 🎯 Langkah 3: Setup Git dan Push Pertama

```bash
# Masuk ke direktori project
cd my-notes-app

# Initialize git (jika belum)
git init

# Add remote origin
git remote add origin https://github.com/username/my-notes-app.git

# Add semua files
git add .

# Commit pertama
git commit -m "Initial commit: Android Notes App with Kotlin & Java"

# Push ke GitHub
git push -u origin main
```

## 🎯 Langkah 4: Verifikasi GitHub Actions

### A. Cek Build Status

1. **Buka Repository di GitHub**
2. **Klik tab "Actions"**
3. **Lihat workflow "Android Build CI"**
4. **Tunggu hingga build selesai (✅ hijau)**

### B. Troubleshooting Build Issues

Jika build gagal (❌ merah):

1. **Klik pada workflow yang gagal**
2. **Lihat logs untuk mencari error**
3. **Common issues:**
   ```
   - Permission denied: ./gradlew
     Fix: chmod +x gradlew
   
   - Gradle wrapper not found
     Fix: Pastikan gradle/wrapper/ ada dan lengkap
   
   - Compilation error
     Fix: Cek syntax error di kode Kotlin/Java
   ```

## 🎯 Langkah 5: Download dan Test APK

### A. Download APK dari GitHub Actions

1. **Buka tab "Actions" di repository**
2. **Klik workflow run yang berhasil (✅)**
3. **Scroll ke bawah ke bagian "Artifacts"**
4. **Download "app-debug"**
5. **Extract file ZIP untuk mendapatkan APK**

### B. Install APK di Android

1. **Transfer APK ke device Android**
2. **Enable "Install from Unknown Sources"**
   ```
   Settings > Security > Unknown Sources (ON)
   atau
   Settings > Apps > Special Access > Install Unknown Apps
   ```
3. **Tap APK file untuk install**
4. **Test semua fitur aplikasi**

## 🎯 Langkah 6: Development Workflow

### A. Workflow Harian

```bash
# 1. Pull latest changes
git pull origin main

# 2. Edit code (contoh)
nano app/src/main/kotlin/com/example/notes/MainActivity.kt

# 3. Test changes locally (opsional)
./gradlew assembleDebug

# 4. Commit changes
git add .
git commit -m "Add new feature: dark theme support"

# 5. Push to trigger build
git push origin main

# 6. Check GitHub Actions
# 7. Download new APK when ready
```

### B. Best Practices

1. **Commit Messages yang Jelas**
   ```bash
   git commit -m "Fix: Search functionality not working"
   git commit -m "Add: Pin/unpin notes feature"
   git commit -m "Update: UI improvements for note cards"
   ```

2. **Test Before Push**
   ```bash
   # Compile check (tanpa build APK penuh)
   ./gradlew compileDebugKotlin compileDebugJavaWithJavac
   ```

3. **Branch Strategy (Advanced)**
   ```bash
   # Untuk fitur besar, gunakan branch terpisah
   git checkout -b feature/dark-theme
   # ... edit code ...
   git commit -m "Implement dark theme"
   git push origin feature/dark-theme
   # Buat Pull Request di GitHub
   ```

## 🎯 Langkah 7: Customization

### A. Mengubah Package Name

1. **Rename package di semua file Java/Kotlin**
   ```
   Dari: com.example.notes
   Ke:   com.yourname.mynotes
   ```

2. **Update build.gradle.kts**
   ```kotlin
   android {
       namespace = "com.yourname.mynotes"
       defaultConfig {
           applicationId = "com.yourname.mynotes"
       }
   }
   ```

3. **Update AndroidManifest.xml**

### B. Mengubah App Name dan Icon

1. **Edit strings.xml**
   ```xml
   <string name="app_name">My Personal Notes</string>
   ```

2. **Replace app icons di mipmap-* folders**

### C. Menambah Fitur Baru

Contoh menambah fitur kategori:

1. **Update Entity (Note.java)**
   ```java
   @ColumnInfo(name = "category")
   public String category;
   ```

2. **Update Database version**
   ```kotlin
   @Database(version = 2) // increment version
   ```

3. **Add migration**
   ```kotlin
   val MIGRATION_1_2 = object : Migration(1, 2) {
       override fun migrate(database: SupportSQLiteDatabase) {
           database.execSQL("ALTER TABLE notes ADD COLUMN category TEXT DEFAULT ''")
       }
   }
   ```

## 🎯 Langkah 8: Release Production

### A. Generate Signed APK

1. **Buat Keystore**
   ```bash
   keytool -genkey -v -keystore my-release-key.keystore -alias my-key-alias -keyalg RSA -keysize 2048 -validity 10000
   ```

2. **Add ke GitHub Secrets**
   ```
   Repository Settings > Secrets and Variables > Actions
   
   Secrets:
   - KEYSTORE_FILE (base64 encoded keystore)
   - KEYSTORE_PASSWORD
   - KEY_ALIAS
   - KEY_PASSWORD
   ```

3. **Update GitHub Actions workflow untuk signed release**

### B. Publish ke Play Store (Opsional)

1. **Generate AAB (Android App Bundle)**
   ```bash
   ./gradlew bundleRelease
   ```

2. **Upload ke Google Play Console**

## 🔧 Troubleshooting Common Issues

### Build Issues

| Error | Solution |
|-------|----------|
| `Permission denied: ./gradlew` | `chmod +x gradlew` |
| `Gradle wrapper not found` | Re-download gradle-wrapper.jar |
| `Java version mismatch` | Pastikan JDK 17 di GitHub Actions |
| `Room schema export` | Add `exportSchema = false` di @Database |

### Runtime Issues

| Issue | Solution |
|-------|----------|
| App crashes on start | Check AndroidManifest.xml dan dependencies |
| Database not created | Verify Room setup dan entity annotations |
| Search not working | Check LiveData observers di ViewModel |
| UI not responsive | Verify RecyclerView adapter setup |

## 📞 Support

Jika mengalami masalah:

1. **Check GitHub Issues** di repository
2. **Review logs** di GitHub Actions
3. **Test locally** dengan Android Studio
4. **Compare** dengan working template

---

**Selamat coding! 🚀**


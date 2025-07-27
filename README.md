# My Notes - Aplikasi Catatan Modern v1.2

Aplikasi catatan Android modern yang terinspirasi dari Google Keep, dibangun menggunakan kombinasi **Kotlin** dan **Java** dengan arsitektur MVVM dan Room database.

## 🚀 Fitur Utama

### Core Features
- **UI Modern**: Desain yang bersih dan elegan dengan Material Design 3
- **Pencarian Real-time**: Cari catatan berdasarkan judul atau konten
- **Pin Notes**: Sematkan catatan penting di bagian atas
- **Auto-save**: Catatan tersimpan otomatis saat keluar dari editor
- **Timestamp**: Menampilkan waktu pembuatan/modifikasi catatan
- **Staggered Grid Layout**: Tampilan grid yang responsif
- **Dark/Light Theme**: Mendukung tema gelap dan terang
- **Categories System**: Organisasi catatan dengan kategori berwarna

### 🆕 New Features (v1.2)
- **🗑️ Trash/Recycle Bin**: Soft delete dengan restore dalam 30 hari
- **✨ Rich Text Formatting**: Bold, italic, underline dengan toolbar
- **📤 Export & Share**: Export ke PDF dan share ke aplikasi lain
- **🖼️ Image Attachments**: Framework siap untuk attachment gambar
- **⏰ Reminder System**: Framework siap untuk notifikasi reminder

## 🛠️ Teknologi yang Digunakan

### Bahasa Pemrograman
- **Kotlin**: Untuk ViewModel, Repository, dan Activity utama
- **Java**: Untuk Entity, Adapter, dan utility classes

### Arsitektur & Libraries
- **MVVM Architecture**: Memisahkan logika bisnis dari UI
- **Room Database**: Penyimpanan data lokal yang robust
- **LiveData**: Observasi data secara reaktif
- **ViewBinding**: Binding view yang type-safe
- **Coroutines**: Operasi asynchronous yang efisien
- **Material Design 3**: Komponen UI modern

## 📱 Screenshot

*Screenshot akan ditambahkan setelah aplikasi di-build*

## 🔧 Setup dan Instalasi

### Prasyarat
- Android Studio Arctic Fox atau lebih baru
- JDK 17
- Android SDK API 24 atau lebih tinggi
- Git

### Clone Repository
```bash
git clone https://github.com/username/notes-app.git
cd notes-app
```

### Build Lokal
```bash
# Memberikan izin eksekusi pada gradlew
chmod +x gradlew

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease
```

## 🚀 GitHub Actions CI/CD

Proyek ini dilengkapi dengan GitHub Actions untuk build otomatis. Setiap kali Anda push ke branch `main` atau `master`, workflow akan:

1. **Setup Environment**: Menginstall JDK 17 dan cache Gradle
2. **Build APK**: Membuat debug dan release APK
3. **Run Tests**: Menjalankan unit tests
4. **Upload Artifacts**: Menyimpan APK hasil build

### Cara Menggunakan GitHub Actions

1. **Fork/Clone** repository ini ke GitHub Anda
2. **Push** kode ke branch `main` atau `master`
3. **Lihat** tab "Actions" di repository GitHub Anda
4. **Download** APK dari artifacts setelah build selesai

### Mengunduh APK dari GitHub Actions

1. Buka repository di GitHub
2. Klik tab **"Actions"**
3. Pilih workflow run yang berhasil (✅)
4. Scroll ke bawah ke bagian **"Artifacts"**
5. Download `app-debug` atau `app-release`

## 📂 Struktur Proyek

```
notes_app/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/notes/
│   │   │   ├── adapter/          # RecyclerView Adapters (Java)
│   │   │   ├── data/             # Room Entity (Java)
│   │   │   └── utils/            # Utility classes (Java)
│   │   ├── kotlin/com/example/notes/
│   │   │   ├── data/             # DAO & Database (Kotlin)
│   │   │   ├── repository/       # Repository pattern (Kotlin)
│   │   │   ├── viewmodel/        # ViewModels (Kotlin)
│   │   │   ├── utils/            # New utilities (Kotlin)
│   │   │   │   ├── RichTextEditor.kt    # Rich text formatting
│   │   │   │   ├── ImageUtils.kt        # Image management
│   │   │   │   ├── NotificationHelper.kt # Reminder notifications
│   │   │   │   ├── ExportUtils.kt       # PDF export & sharing
│   │   │   │   └── ReminderReceiver.kt  # Notification receiver
│   │   │   ├── MainActivity.kt          # Main Activity (Kotlin)
│   │   │   ├── AddEditNoteActivity.kt   # Enhanced with formatting
│   │   │   ├── TrashActivity.kt         # New trash management
│   │   │   ├── SearchActivity.kt        # Search functionality
│   │   │   ├── CategoriesActivity.kt    # Categories management
│   │   │   └── SettingsActivity.kt      # App settings
│   │   └── res/
│   │       ├── layout/           # XML layouts (enhanced)
│   │       ├── values/           # Colors, strings, themes
│   │       ├── drawable/         # Drawable resources (new icons)
│   │       ├── menu/             # Menu resources (enhanced)
│   │       └── xml/              # File provider paths
│   └── build.gradle.kts
├── .github/workflows/
│   └── android-build.yml         # GitHub Actions workflow
├── gradle/wrapper/
├── build.gradle.kts
├── README.md
└── FITUR_BARU.md                 # Documentation for new features
```

## 🎯 Cara Menggunakan Aplikasi

### Membuat Catatan Baru
1. Tap tombol **"+"** (FAB) di pojok kanan bawah
2. Masukkan judul dan konten catatan
3. Gunakan **formatting toolbar** untuk bold, italic, underline
4. Tap ikon **save** atau tekan tombol back (auto-save)

### Mencari Catatan
1. Gunakan search bar di bagian atas
2. Ketik kata kunci untuk mencari judul atau konten
3. Hasil akan muncul secara real-time
4. Atau gunakan **Search Activity** dari menu

### Pin/Unpin Catatan
1. Tap ikon **"⋮"** (more options) pada catatan
2. Pilih **"Pin"** atau **"Unpin"**
3. Catatan yang di-pin akan muncul di bagian atas

### Edit Catatan
1. Tap pada catatan yang ingin diedit
2. Ubah judul atau konten dengan **rich text formatting**
3. Tap ikon **save** atau tekan back untuk menyimpan

### Hapus & Restore Catatan
1. Tap ikon **"⋮"** (more options) pada catatan
2. Pilih **"Delete"** → catatan masuk ke **Trash**
3. Akses **Trash** dari menu utama
4. **Restore** catatan atau **Delete Permanently**
5. Catatan di trash otomatis dihapus setelah 30 hari

### Export & Share Catatan
1. Buka catatan → Menu → **"Share"** atau **"Export as PDF"**
2. Share langsung ke WhatsApp, Email, dll.
3. PDF tersimpan di folder eksternal aplikasi

### Menggunakan Categories
1. Menu → **"Categories"**
2. Tambah kategori baru dengan warna
3. Assign catatan ke kategori
4. Filter catatan berdasarkan kategori

## 🔄 Workflow Development

### Menggunakan Termux (Opsional)
Jika Anda menggunakan Termux untuk development:

```bash
# Install dependencies
pkg update && pkg upgrade
pkg install git nodejs python build-essential openjdk-17

# Clone dan edit
git clone https://github.com/username/notes-app.git
cd notes-app

# Edit dengan editor pilihan (nano/vim/neovim)
nano app/src/main/kotlin/com/example/notes/MainActivity.kt

# Commit dan push
git add .
git commit -m "Update fitur baru"
git push origin main
```

### Build di GitHub Actions
Setelah push, GitHub Actions akan otomatis:
- Compile kode Kotlin dan Java
- Generate APK debug dan release
- Menjalankan tests
- Upload artifacts untuk download

## 🐛 Troubleshooting

### Build Gagal di GitHub Actions
- Pastikan semua file `gradlew` memiliki permission yang benar
- Cek apakah ada syntax error di kode Kotlin/Java
- Lihat logs di tab Actions untuk detail error

### APK Tidak Bisa Diinstall
- Pastikan "Install from Unknown Sources" diaktifkan
- Download APK debug untuk testing
- Untuk release APK, perlu signing key

### Database Error
- Hapus data aplikasi di Settings > Apps > My Notes > Storage
- Atau uninstall dan install ulang aplikasi

## 📈 Changelog

### Version 1.2 (Latest)
- ✅ **Trash/Recycle Bin System** - Soft delete dengan restore capability
- ✅ **Rich Text Formatting** - Bold, italic, underline dengan toolbar
- ✅ **Export & Share** - PDF export dan share functionality
- ✅ **Image Attachments Framework** - Siap untuk implementasi UI
- ✅ **Reminder System Framework** - Siap untuk implementasi UI
- ✅ **Enhanced Database** - Migration ke version 3
- ✅ **New UI Components** - Trash activity, formatting toolbar
- ✅ **Performance Improvements** - Optimized queries dan storage

### Version 1.1
- ✅ Search functionality
- ✅ Categories system
- ✅ Dark mode support
- ✅ Improved UI/UX
- ✅ Better performance
- ✅ Bug fixes and improvements

### Version 1.0
- ✅ Basic CRUD operations
- ✅ Pin/unpin notes
- ✅ Material Design UI
- ✅ MVVM Architecture
- ✅ Room Database

## 🔮 Roadmap

### Phase 2 (Coming Soon)
- 🔄 **Image Attachment UI** - Interface untuk menambah/hapus gambar
- 🔄 **Reminder UI** - Date/time picker untuk set reminder
- 🔄 **Voice Notes** - Rekam dan putar audio notes
- 🔄 **Note Templates** - Template siap pakai
- 🔄 **Advanced Search** - Filter berdasarkan kategori, tanggal

### Phase 3 (Future)
- 🔄 **Collaborative Notes** - Share dan edit bersama
- 🔄 **Cloud Sync** - Sinkronisasi dengan cloud storage
- 🔄 **Note Encryption** - Enkripsi catatan sensitif
- 🔄 **Widget Support** - Widget untuk home screen
- 🔄 **Markdown Support** - Support syntax markdown

## 🤝 Kontribusi

1. Fork repository ini
2. Buat branch fitur baru (`git checkout -b fitur-baru`)
3. Commit perubahan (`git commit -m 'Tambah fitur baru'`)
4. Push ke branch (`git push origin fitur-baru`)
5. Buat Pull Request

## 📄 Lisensi

Proyek ini menggunakan lisensi MIT. Lihat file `LICENSE` untuk detail.

## 👨‍💻 Developer

Dibuat dengan ❤️ menggunakan kombinasi Kotlin dan Java untuk pembelajaran dan demonstrasi pengembangan Android modern.

**My Notes App** - Dari aplikasi sederhana menjadi notes app yang powerful! 🌟

---

**Happy Coding! 🚀**
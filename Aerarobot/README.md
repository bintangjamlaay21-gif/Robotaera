# Aera Robot — AI Companion Android App

Aera adalah robot AI companion yang tinggal di dalam smartphone kamu.  
Dibangun dengan **Jetpack Compose**, **Gemini 1.5 Flash**, Speech-to-Text, dan Text-to-Speech.

## Fitur

- 🤖 Wajah robot animasi (mata neon cyan yang berkedip & bereaksi)
- 💬 Chat teks + suara (STT & TTS Bahasa Indonesia)
- 🧠 Integrasi Google Gemini 1.5 Flash
- 💾 Memory percakapan (bisa di-clear)
- ⚙️ Settings: Auto Speak, Memory, API Key
- 🎨 Dark theme futuristik

---

## Cara Build APK Tanpa Laptop (pakai GitHub)

### Langkah 1 — Buat repository di GitHub
1. Buka [github.com](https://github.com) → login / daftar
2. Klik tombol **+** → **New repository**
3. Nama: `AeraRobot` (boleh apa saja)
4. Pilih **Public**
5. Klik **Create repository**

### Langkah 2 — Upload project
**Cara termudah (dari HP):**
1. Download file `AeraRobot_Complete.zip`
2. Extract di HP (pakai ZArchiver / RAR)
3. Buka GitHub (browser atau app) → masuk ke repository yang baru dibuat
4. Klik **Add file** → **Upload files**
5. Upload **semua isi** folder `AeraRobot` (bukan foldernya, tapi isinya: `.github`, `app`, `build.gradle.kts`, dll)
6. Tulis commit message: `Initial commit`
7. Klik **Commit changes**

### Langkah 3 — Jalankan Build
1. Di repository GitHub, klik tab **Actions**
2. Pilih workflow **Build AeraRobot APK**
3. Klik tombol **Run workflow** → **Run workflow**
4. Tunggu 3–8 menit sampai status hijau ✓

### Langkah 4 — Download APK
1. Setelah selesai, klik run yang sukses
2. Scroll ke bawah → bagian **Artifacts**
3. Download **AeraRobot-APK**
4. Extract file zip → dapat `app-debug.apk`
5. Kirim ke HP → install (aktifkan “Install unknown apps”)

---

## Cara Setup di dalam App

1. Install APK
2. Buka aplikasi Aera
3. Masuk ke **Settings** (ikon gear)
4. Paste **Gemini API Key** (ambil gratis di [aistudio.google.com/apikey](https://aistudio.google.com/apikey))
5. Klik **Simpan API Key**
6. Mulai ngobrol!

---

## Permission yang dibutuhkan

- `RECORD_AUDIO` — untuk voice input
- `INTERNET` — untuk memanggil Gemini API

## Catatan

- Wake Word masih placeholder
- Model yang digunakan: `gemini-1.5-flash`
- APK yang dihasilkan adalah **debug** (boleh untuk pemakaian pribadi)

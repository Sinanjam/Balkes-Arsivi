# Balkes Arşivi – GitHub'suz Final Proje

Paket adı: `com.sinanjam.arsiv`

Bu klasör Android Studio ile doğrudan açılıp APK alınacak şekilde hazırlanmıştır. GitHub Actions kullanmak zorunda değilsin.

## Hazır gelenler

- Splash screen videosu: `app/src/main/res/raw/splash.mp4`
- Ana ekran arka planı: taraftar/stadyum fotoğrafı
- Ana ekran üst görseli: 1966 logolu uzun görsel
- Sol üst küçük imza: Sinanjam
- Ana ekran kartları: sadece **Balkes Arşivi** ve **Uygulama Hakkında**
- Tek tuşla aydınlık / karanlık tema geçişi ve SharedPreferences kaydı
- Ana ekranda geri tuşu: “Çıkmak istiyor musunuz?” Evet/Hayır
- İç ekranlarda geri tuşu: ana ekrana dönüş
- Metin detayında A− / A+ yazı boyutu ve SharedPreferences kaydı
- Özbalkesler hazırlık zip'indeki 58 kayıt: `app/src/main/assets/archive/archive_items.json`
- Orijinal hazırlık dosyaları: `app/src/main/assets/archive/original/`
- Örnek metin + örnek fotoğraf
- Fotoğrafa uzun basınca kaydetme onayı, galeriye kaydetme, Toast ve bildirim
- Sabit imza anahtarı: `keystore/balkes-arsivi-stable.jks`

## APK alma

### Android Studio ile

1. Android Studio'yu aç.
2. `BalkesArsivi_FINAL_GITHUBSUZ` klasörünü proje olarak aç.
3. Gradle sync bitince: **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
4. APK konumu: `app/build/outputs/apk/debug/app-debug.apk`

### Terminal ile

Linux/macOS:

```bash
chmod +x BUILD_LOCAL_LINUX_MAC.sh
./BUILD_LOCAL_LINUX_MAC.sh
```

Windows:

```bat
BUILD_LOCAL_WINDOWS.bat
```

## Güncelleme / silmeden yükleme

Aynı paket adı (`com.sinanjam.arsiv`) ve aynı sabit keystore kullanıldığı sürece yeni APK eski APK'nın üstüne kurulabilir. Keystore dosyasını değiştirme/silme.

## Not

Bu paket kaynak proje zip'idir. APK derlemesi Android SDK/Android Studio bulunan bilgisayarda yapılır.

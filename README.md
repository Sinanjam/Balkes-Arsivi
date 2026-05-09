# Balkes Arşivi Android

**Sürüm:** 1.7 — **Kırmızı Beyaz Hafıza**

Balıkesirspor arşiv metinleri, fotoğrafları ve tabloları için yerel + GitHub hibrit çalışan Android uygulaması.

## 1.7 özeti

- GitHub üzerinden otomatik veri güncelleme kontrolü.
- Ana ekranda Favoriler, Fotoğraf Albümü ve arşiv girişleri sadeleştirildi.
- Son Okunan ana ekrandan kaldırıldı.
- Arama artık yazdıkça çalışır; Ara/Temizle butonları kaldırıldı.
- Arama sonuçlarında eşleşen kelime vurgulanır.
- Yazı favorileri ve fotoğraf favorileri ayrıldı.
- Fotoğraf paylaşma özelliği eklendi.
- Fotoğraflarda çift parmakla yakınlaştırma eklendi.
- Fotoğraf altındaki uzun, kullanıcıyı ilgilendirmeyen duyuru metni kaldırıldı.
- Tablolar daha okunaklı hale getirildi.
- Sade okuma / tam görünüm seçeneği eklendi.
- Boş durum ekranları ve yükleniyor iskeletleri eklendi.
- Splash ekranı yenilendi: “Kırmızı Beyaz tarih...”
- GitHub Actions artık release APK üretir; debug APK yayınlamaz.

## Lokal release build

```bash
./gradlew --no-daemon assembleRelease
```

APK:

```text
app/build/outputs/apk/release/app-release.apk
```

## GitHub Actions

Push sonrası `.github/workflows/android-build.yml` sadece release APK üretir.

## Paket

- Paket adı: `com.sinanjam.arsiv`
- versionCode: `8`
- versionName: `1.7-kirmizi-beyaz-hafiza`

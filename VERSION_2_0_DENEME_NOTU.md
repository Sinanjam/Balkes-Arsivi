# Balkes Arşivi 2.0 PDF Arşiv Deneme

Bu paket 1.9 tabanının üstüne hazırlanmış 2.0 deneme sürümüdür.

## Ana kararlar

- Ana ekranda **Kayıp Sayfalar** ayrı bölüm olarak gösterilmez.
- Tüm içerikler tek ve düz biçimde **Balkes Arşivi** içinde listelenir.
- Kullanıcıya kaynak ayrımı / Wayback ayrımı / eski site ayrımı gösterilmez.
- Sayfa içi görseller artık metnin üstüne yığılmaz; metin akışının arasına yerleşir.
- Uzun metinler PDF/dergi sayfası gibi paragraf bloklarına ayrılır.
- Puan tabloları logosuz, temiz tablo kartları olarak gösterilir.
- Tablo tam ekran açma ve tabloyu görsel olarak paylaşma akışı korunur.
- Fotoğrafa uzun basınca kaydetme/paylaşma/favori işlemleri korunur.
- Temsili görsel sadece gerçek fotoğraf olmayan sayfalarda, açık etiketle kullanılır.

## Build

GitHub Actions bu zip'i doğrudan açıp release APK üretebilir. Yerelde:

```bash
chmod +x ./gradlew
./gradlew --no-daemon assembleRelease
```

APK yolu:

```text
app/build/outputs/apk/release/app-release.apk
```

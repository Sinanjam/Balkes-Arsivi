# Balkes Arşivi - Final Verili Sürüm

Bu paket, önceki final zip bozulmadan ayrı klasörde oluşturuldu.

Kaynak uygulama zip: BalkesArsivi_FINAL_TEMIZ_METIN_FOTO_HAKKINDA.zip
Yeni veri zip: balkes_arsiv_only_20260508_182859.zip

## Entegrasyon
- Normal ÖzBalkesler haberleri dahil edilmedi.
- Kabul edilen Balıkesirspor arşivi sayfası: 12
- Uygulamaya eklenen görsel: 480
- Uygulamaya eklenen tablo: 24
- Wayback çekimi bu veri paketinde kapalıydı.

## Uygulamada
- Ana ekran, tema değiştirme, hakkında ekranı ve geri tuşu akışı korundu.
- Metin boyutu arttır/azalt tercihi SharedPreferences ile korunur.
- Her arşiv sayfasında fotoğraf galerisi vardır.
- Fotoğrafa uzun basınca galeriye kaydetme onayı çıkar.
- Tablolar ayrıca yatay kaydırılabilir metin bloğu olarak gösterilir.

## Dosyalar
- app/src/main/assets/archive/archive_items.json: uygulamanın kullandığı ana veri.
- app/src/main/assets/archive_data/: uygulama içine gömülü medya/manifest/table dosyaları.
- VERI_KAYNAGI_YENI_CEKIM/: kullanıcıdan gelen veri paketinin proje içindeki yedeği.

Not: Proje kökündeki VERI_KAYNAGI_YENI_CEKIM klasöründe metin/manifest/tablo yedeği tutuldu; görseller APK içine app/src/main/assets/archive_data/media/images altında gömüldü, kökte tekrar kopyalanmadı.

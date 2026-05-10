# Balkes Arşivi 2.1.3 - Yerel Arşiv Kesin Düzeltme

- Bazı cihazlarda görülen “Arşiv kaydı bulunamadı” hatası için uzaktan/bozuk cache ana listeden tamamen çıkarıldı.
- Balkes Arşivi ana listesi artık her zaman APK içindeki yerel arşiv verisini zorunlu kaynak olarak kullanır.
- Yerel veri okunamazsa ikinci bir minimal yerel kurtarma indeksi denenir.
- Liste ekranına girerken veri boşsa uygulama otomatik yeniden yüklemeyi dener; kullanıcıdan ADB/önbellek temizleme beklenmez.
- Build sırasında APK içinde archive_items.json ve archive_items_min.json varlığı doğrulanır.

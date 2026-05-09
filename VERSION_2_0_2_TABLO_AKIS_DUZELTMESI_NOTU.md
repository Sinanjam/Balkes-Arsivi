# Balkes Arşivi 2.0.2 - Tablo ve Akış Düzeltmesi

Bu sürüm, 2.0.1 testinde görülen tablo/metin akışı hatalarını düzeltmek için hazırlandı.

## Düzeltmeler

- Ziyaretçi defteri ve kullanıcıya görünen kaynak ayrımı kaldırılmış halde tutuldu.
- Başlık ve içeriklerdeki "Kayıp Sayfalar" ifadesi temizlendi.
- Arama ekranındaki filtre bölümü kaldırılmış halde tutuldu.
- İçerik JSON'undaki ham `## Tablolar`, `### Tablo` ve Markdown tablo satırları okunur metinden temizlendi.
- Puan tabloları düz metin yerine kartlı tablo bileşeniyle gösterilir.
- Maç listeleri tarih + skor kartları olarak gösterilir.
- Boş tablo başlığı/dev boşluk oluşmaması için boş tablo blokları gösterilmez.
- Kapaksız içerikler için sezon/tablo/gazete/genel türlerine göre farklı temsili arşiv görselleri eklendi.
- Üst ve alt sistem çubuğu boşlukları artırıldı.
- NixOS build dosyası 34.0.0 ve 35.0.0 build-tools ile çalışacak şekilde korunur.

## APK çıkışı

`~/BalkesArsivi-v2.0.2-tablo-akis-duzeltmesi-release.apk`

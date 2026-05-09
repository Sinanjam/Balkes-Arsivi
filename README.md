# Balkes Arşivi 2.0 Final

Bu paket, Balkes Arşivi Android uygulamasının halka açılacak final zip hazırlığıdır.

## Ana yapı

- Ana ekran: **Balkes Arşivi**, **Favoriler**, **Uygulama Hakkında**.
- Kayıp Sayfalar ayrı bölüm değildir; tüm içerikler düz **Balkes Arşivi** içinde bulunur.
- Metin ekranları PDF/dergi tarzı akar; görseller metinlerin arasında gösterilir.
- Puan tabloları ve maç skorları mobil okunabilir kart/tablo yapısındadır.
- Eksik fotoğraflar için temsili görseller kullanılır ve Temsilidir notu gösterilir.
- Güncelleme kontrolü splash screen sırasında GitHub latest release üzerinden yapılır.

## NixOS build

```fish
cd ~/Downloads/BalkesArsivi_2_0_Final
chmod +x BUILD_NIXOS_2_0.fish BUILD_NIXOS_2_0.sh BUILD_AND_PUSH_2_0_FIXED.sh gradlew
./BUILD_NIXOS_2_0.fish
```

APK çıkışı:

```text
~/BalkesArsivi-v2.0-final-release.apk
```

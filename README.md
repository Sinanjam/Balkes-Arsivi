# Balkes Arşivi Android Taslak

Paket adı: `com.sinanjam.arsiv`

Bu taslakta hazır olanlar:

- `18024.mp4` splash screen videosu olarak kullanılır.
- Ana ekranda taraftar/stadyum fotoğrafı arka plandır.
- Sol üstte küçük `Sinanjam` görseli görünür.
- Üst bölümde 1966 logolu uzun görsel vardır.
- Ana ekranda yalnızca `Balkes Arşivi` ve `Uygulama Hakkında` kutucukları vardır.
- Ana ekranda tek tuşla karanlık/aydınlık tema değişir ve ayar kaydedilir.
- Ana ekrandan geri tuşuna basılınca `Çıkmak istiyor musunuz?` evet/hayır diyaloğu çıkar.
- Kutucuk içindeyken geri tuşu ana ekrana döndürür.
- Metin ekranında yazı boyutu A-/A+ ile değişir ve SharedPreferences içinde saklanır.
- Örnek metin ve örnek fotoğraf vardır.
- Fotoğrafa uzun basınca kaydetme onayı sorulur; kaydetme sonucu Toast ve bildirim olarak gösterilir.
- Güncellemelerde uygulamayı silmeden kurabilmek için proje içinde sabit debug signing key bulunur.

GitHub Actions için verilen `build-latest-uploaded-zip.yml` dosyasıyla uyumludur. ZIP içinden `settings.gradle` bulunur ve `./gradlew clean assembleDebug` çalıştırılır.

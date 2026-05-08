# Balkes Arşivi 1.6 - GitHub hibrit sürüm

Bu paket önceki verili sürümü bozmadan hazırlanmıştır.

Eklenenler:
- Uygulama açılışında GitHub'daki `app/src/main/assets/archive/archive_items.json` kontrol edilir.
- İnternet yoksa APK içindeki yerel arşiv çalışmaya devam eder.
- GitHub'dan alınan yeni veri cihazda önbelleğe yazılır.
- Arşiv ekranına arama eklendi.
- Favoriler eklendi.
- Fotoğraf albümü eklendi.
- Tablolar monospaced düz metin yerine yatay kaydırılabilir hücreli görünümle gösterilir.
- Okuma ekranında yazı boyutu, son okunan kayıt ve kaldığın yerden devam desteği var.
- Hakkında ekranındaki fazladan açıklamalar kaldırıldı; önceki sade metin bırakıldı.
- Kullanıcı odaksız açıklamalar arayüzden temizlendi.

GitHub veri kaynağı:
`https://github.com/Sinanjam/Balkes-Arsivi`

Yeni yazı ekleme mantığı:
- Repo içindeki `app/src/main/assets/archive/archive_items.json` güncellenirse uygulama bunu kontrol eder.
- Yeni fotoğraflar repo içindeki `app/src/main/assets/...` yollarında durursa uygulama gerektiğinde raw GitHub üzerinden indirip önbelleğe alır.
- Büyük tasarım/özellik değişikliği olmadıkça APK güncellemesi gerekmez.

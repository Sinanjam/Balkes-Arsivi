# Balkes Arşivi GitHub Push Notu

Bu paket GitHub'a push edilecek temiz Android proje köküdür.

İçerik:
- Android uygulama kaynakları
- Yerel arşiv metinleri
- Yerel fotoğraflar
- Tablolar ve manifest dosyaları
- `.github/workflows/android-build.yml`
- `PUSH_TO_GITHUB.sh`

Repo hedefi:
`https://github.com/Sinanjam/Balkes-Arsivi.git`

Token için gereken yetkiler:
- repo
- workflow

Token'ı buraya veya herhangi bir dosyaya yazma. Push sırasında terminalde parola/token alanına yapıştır.

## Kullanım

```bash
cd ~/Downloads
unzip Balkes-Arsivi_PUSH_READY.zip -d BalkesPush
cd BalkesPush
bash PUSH_TO_GITHUB.sh
```

Repo daha önce doluysa ve normal push reddedilirse, sadece gerçekten bu paketi repo içeriği yapmak istediğinde:

```bash
FORCE_PUSH=1 bash PUSH_TO_GITHUB.sh
```

## Beklenen çıktı

GitHub Actions şu çıktıları artifact olarak üretir:
- BalkesArsivi-debug-apk
- BalkesArsivi-release-apk

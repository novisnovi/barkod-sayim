# Barkod Sayım

EAN13 lazer okuyucusu olan Android terminaller (iData vb.) ve responsive web tarayıcısı ile çalışan basit ama sağlam bir sayım uygulaması. Tek HTML dosyası hem web sitesi olarak yayınlanabilir, hem de bu repodaki Android projesi ile APK olarak derlenebilir. Ürün veritabanı Firebase Firestore üzerinden senkronize olur.

## Hızlı Bakış

| Ne istiyorum? | Yapılması gereken |
| --- | --- |
| Hızlıca denemek istiyorum | `index.html` dosyasını tarayıcıda aç (Firebase'siz, yerel modda çalışır) |
| Web sitesi olarak yayınlamak istiyorum | Bu repoyu GitHub'a yükle → Pages workflow'u otomatik yayınlar |
| APK indirmek istiyorum (zero-setup) | Bu repoyu GitHub'a yükle → `Build APK` workflow'u her push'ta APK üretir |
| Yerel olarak APK derlemek istiyorum | `android/` klasörünü Android Studio'da aç → `Build > Build APK` |

## Dosya Yapısı

```
.
├── index.html                  # Tüm web uygulaması (responsive, tek dosya)
├── manifest.json               # PWA tanımı
├── sw.js                       # Service worker (çevrimdışı destek)
├── firebase-config.js          # Firebase ayar şablonu (siz dolduracaksınız)
├── icon-192.png, icon-512.png  # Uygulama ikonları
├── ornek-urunler.csv           # Test verisi
├── KURULUM-KILAVUZU.docx       # Tam kurulum rehberi (Türkçe Word doc)
├── android/                    # Native Android WebView projesi
│   ├── app/
│   ├── build.gradle
│   └── settings.gradle
└── .github/workflows/
    ├── build-apk.yml           # Her push'ta APK derler
    └── pages.yml               # Her push'ta GitHub Pages'e yayınlar
```

## 1. GitHub Üzerinden Yayınlama (Web)

1. GitHub'da yeni bir repo aç (örn: `barkod-sayim`).
2. Bu klasördeki tüm dosyaları o repoya push'la:
   ```bash
   git init
   git add .
   git commit -m "ilk yükleme"
   git branch -M main
   git remote add origin https://github.com/KULLANICI/barkod-sayim.git
   git push -u origin main
   ```
3. GitHub'da reponun **Settings → Pages** sekmesine git, Source olarak **GitHub Actions** seç.
4. Bir kez push attığında `Deploy to GitHub Pages` workflow'u otomatik çalışır. Birkaç dakika sonra siteniz şuradadır:
   ```
   https://KULLANICI.github.io/barkod-sayim/
   ```
   APK içinde yerel `index.html` çalıştığı için bu site sadece tarayıcıdan kullanım için.

## 2. APK'yı Otomatik Derleme (GitHub Actions ile)

Repo GitHub'da olduğunda **hiçbir şey yapmanıza gerek yok**:

1. `main` dalına her push'tan sonra Actions sekmesinde `Build Android APK` çalışır.
2. Yaklaşık 3 dakika sonra:
   - **Actions → workflow → Artifacts** bölümünden `.apk` dosyasını indirebilirsiniz.
   - **Releases** sekmesinde otomatik olarak yeni bir sürüm yayınlanır.
3. APK'yı iData terminale yükleyin (bilinmeyen kaynaklara izin vermeyi unutmayın).

> APK debug-imzalı olarak çıkar — kişisel/şirket içi kullanım için sorun yok. Play Store'a yüklemek istiyorsanız release-imzalı bir keystore ekleyin (aşağıdaki "İmzalı Release" bölümüne bakın).

## 3. Yerel Olarak APK Derleme (Android Studio)

Android Studio yüklüyse:

1. Android Studio'yu açıp **File → Open** ile bu reponun `android/` klasörünü seçin.
2. İlk açılışta gradle dependencyleri internetten iner (yaklaşık 5 dakika).
3. Üst menüden **Build → Generate Signed App Bundle / APK → APK** seçin, debug variant'ını seçip Finish'e basın.
4. APK şu konumdadır: `android/app/build/outputs/apk/debug/app-debug.apk`.

Alternatif komut satırı:
```bash
cd android
./gradlew assembleDebug   # debug APK
./gradlew assembleRelease # release APK (signing config gerek)
```

## 4. Firebase Ayarı (APK ve site aynı veriyi görsün)

`firebase-config.js` içindeki `window.FIREBASE_CONFIG` nesnesinin değerlerini kendi Firebase projenize göre doldurun. Tam rehber için `KURULUM-KILAVUZU.docx` dosyasının 2. bölümüne bakın.

Doldurmazsanız uygulama yine çalışır ama her cihaz veriyi sadece kendi hafızasında tutar (yerel mod).

## 5. iData Lazer Ayarı (Çok Önemli)

iData terminal Scanner Wedge ayarında:
- **Output Mode** → Keyboard Wedge / Simulate Keystroke
- **Append Enter / Terminator** → Enter (CR)
- **Symbology** → EAN-13 aktif

Detaylı adımlar için `KURULUM-KILAVUZU.docx` dosyasının 5. bölümüne bakın.

## 6. İmzalı Release APK

Release APK üretmek için:

1. Lokal makinenizde keystore üretin:
   ```bash
   keytool -genkey -v -keystore release.keystore -keyalg RSA -keysize 2048 \
           -validity 10000 -alias barkodsayim
   ```
2. `android/app/build.gradle` içine `signingConfigs.release { ... }` bloğu ekleyin.
3. CI'da imzalamak için keystore'u base64'leyip GitHub Secrets'a `RELEASE_KEYSTORE` olarak ekleyin, workflow'u uyarlayın.

(Bu adım çoğu iç kullanıcı için gerekmez; debug APK her yerde çalışır.)

## Lisans

Bu proje MIT lisansı altında verilmiştir. Kişisel veya ticari kullanım için serbestçe kullanabilir, değiştirebilir, dağıtabilirsiniz.

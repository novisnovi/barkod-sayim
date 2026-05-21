/* ============================================================
 *  FIREBASE YAPILANDIRMASI
 *
 *  1. https://console.firebase.google.com adresinden yeni proje
 *     oluşturun (örn: "barkod-sayim").
 *  2. Sol menüden Build > Firestore Database > Create database
 *     -> Production veya Test modunda başlatın (Avrupa bölgesi).
 *  3. Proje ayarları (dişli ikon) > General > Your apps bölümünden
 *     "Web (</>)" simgesine tıklayıp uygulamayı kaydedin.
 *  4. Aşağıdaki firebaseConfig nesnesini, Firebase'in size verdiği
 *     değerlerle DEĞİŞTİRİN ve dosyayı kaydedin.
 *
 *  Eğer bu dosyayı doldurmazsanız uygulama sorunsuz çalışmaya
 *  devam eder ama veriler sadece o cihazın hafızasında kalır
 *  (yerel mod). Site ve APK arasında veri paylaşımı için
 *  Firebase'i mutlaka yapılandırın.
 * ============================================================ */

window.FIREBASE_CONFIG = {
  // ÖRNEK — kendi değerlerinizle değiştirin
  apiKey:            "",
  authDomain:        "",
  projectId:         "",
  storageBucket:     "",
  messagingSenderId: "",
  appId:             ""
};

/*
 *  Önerilen Firestore güvenlik kuralları (Rules sekmesi):
 *
 *  rules_version = '2';
 *  service cloud.firestore {
 *    match /databases/{database}/documents {
 *      // Geliştirme için tüm okuma/yazmaya açık
 *      match /{document=**} {
 *        allow read, write: if true;
 *      }
 *    }
 *  }
 *
 *  ⚠️  Yayına almadan önce mutlaka Firebase Authentication ekleyip
 *      kuralları "request.auth != null" şartına bağlayın.
 */

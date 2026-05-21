/* Barkod Sayım — basit service worker (PWA / offline) */
const CACHE = "barkod-sayim-v1";
const ASSETS = [
  "./",
  "./index.html",
  "./manifest.json",
  "./firebase-config.js",
  "./icon-192.png",
  "./icon-512.png",
  "https://cdn.tailwindcss.com",
  "https://cdn.jsdelivr.net/npm/xlsx@0.18.5/dist/xlsx.full.min.js"
];

self.addEventListener("install", (e) => {
  e.waitUntil(caches.open(CACHE).then(c => c.addAll(ASSETS).catch(() => {})));
  self.skipWaiting();
});

self.addEventListener("activate", (e) => {
  e.waitUntil(
    caches.keys().then(keys =>
      Promise.all(keys.filter(k => k !== CACHE).map(k => caches.delete(k)))
    )
  );
  self.clients.claim();
});

self.addEventListener("fetch", (e) => {
  // Firestore çağrılarını cache'leme
  if (e.request.url.includes("firestore.googleapis.com")) return;
  e.respondWith(
    caches.match(e.request).then(cached => {
      const network = fetch(e.request).then(res => {
        // Yalnız GET ve same-origin / CDN olanları cache'le
        if (e.request.method === "GET" && res.ok) {
          const copy = res.clone();
          caches.open(CACHE).then(c => c.put(e.request, copy)).catch(() => {});
        }
        return res;
      }).catch(() => cached);
      return cached || network;
    })
  );
});

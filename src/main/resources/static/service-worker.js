const CACHE_NAME = 'discipliner-v1';

// Ajout des ressources essentielles avec les bons chemins Thymeleaf
const ASSETS_TO_CACHE = [
  '/',
  '/manifest.json',
  '/css/styles.css',
  '/webjars/htmx.org/dist/htmx.min.js',
  '/webjars/alpinejs/dist/cdn.min.js'
];

self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open(CACHE_NAME)
      .then((cache) => {
        // On met en cache les ressources une par une pour éviter l'échec total si une ressource échoue
        return Promise.allSettled(
          ASSETS_TO_CACHE.map(url => 
            cache.add(url).catch(error => {
              console.error(`Failed to cache ${url}:`, error);
              // On continue même si une ressource échoue
              return Promise.resolve();
            })
          )
        );
      })
  );
});

self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches.keys().then((cacheNames) => {
      return Promise.all(
        cacheNames.map((cacheName) => {
          if (cacheName !== CACHE_NAME) {
            return caches.delete(cacheName);
          }
        })
      );
    })
  );
});

self.addEventListener('fetch', (event) => {
  event.respondWith(
    caches.match(event.request)
      .then((response) => {
        if (response) {
          return response;
        }
        return fetch(event.request)
          .then((response) => {
            // On ne met en cache que les réponses valides
            if (!response || response.status !== 200 || response.type !== 'basic') {
              return response;
            }

            // On clone la réponse car elle ne peut être utilisée qu'une fois
            const responseToCache = response.clone();

            caches.open(CACHE_NAME)
              .then((cache) => {
                cache.put(event.request, responseToCache);
              });

            return response;
          })
          .catch(() => {
            // Si la requête échoue et que c'est une requête d'image,
            // on peut retourner une image par défaut
            if (event.request.url.match(/\.(jpg|jpeg|png|gif|svg)$/)) {
              return caches.match('/icons/icon-192.png');
            }
            // Sinon on laisse l'erreur se propager
            throw new Error('Network error');
          });
      })
  );
}); 
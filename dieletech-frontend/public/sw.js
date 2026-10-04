/**
 * Service worker de Dieletech (HU-12).
 *
 * Estrategia deliberadamente conservadora:
 *  - El shell de la aplicacion se precachea para que la app abra sin red.
 *  - Los recursos estaticos van por stale-while-revalidate.
 *  - Las peticiones a la API NUNCA se cachean: mostrar un catalogo o un
 *    progreso desactualizado seria peor que mostrar un error honesto.
 */
const VERSION = 'dieletech-v1';
const SHELL = `${VERSION}-shell`;
const ASSETS = `${VERSION}-assets`;

const SHELL_FILES = [
  '/',
  '/index.html',
  '/manifest.webmanifest',
  '/icon-192.png',
  '/icon-512.png',
];

self.addEventListener('install', (event) => {
  event.waitUntil(
    caches
      .open(SHELL)
      .then((cache) => cache.addAll(SHELL_FILES))
      .catch(() => undefined)
      .then(() => self.skipWaiting())
  );
});

self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches
      .keys()
      .then((keys) =>
        Promise.all(keys.filter((k) => !k.startsWith(VERSION)).map((k) => caches.delete(k)))
      )
      .then(() => self.clients.claim())
  );
});

self.addEventListener('message', (event) => {
  if (event.data === 'SKIP_WAITING') self.skipWaiting();
});

self.addEventListener('fetch', (event) => {
  const { request } = event;

  if (request.method !== 'GET') return;

  const url = new URL(request.url);

  // Solo se gestiona el propio origen.
  if (url.origin !== self.location.origin) return;

  // La API queda fuera del cache: los datos deben ser siempre frescos.
  if (url.pathname.startsWith('/api/')) return;

  // Navegacion: red primero, con el shell como respaldo sin conexion.
  if (request.mode === 'navigate') {
    event.respondWith(
      fetch(request)
        .then((response) => {
          const copy = response.clone();
          caches.open(SHELL).then((c) => c.put('/index.html', copy)).catch(() => {});
          return response;
        })
        .catch(() =>
          caches.match('/index.html').then((r) => r || caches.match('/'))
        )
    );
    return;
  }

  // Estaticos: responde del cache y refresca en segundo plano.
  event.respondWith(
    caches.match(request).then((cached) => {
      const network = fetch(request)
        .then((response) => {
          if (response && response.status === 200 && response.type === 'basic') {
            const copy = response.clone();
            caches.open(ASSETS).then((c) => c.put(request, copy)).catch(() => {});
          }
          return response;
        })
        .catch(() => cached);
      return cached || network;
    })
  );
});

/**
 * Registro del service worker y gestion del prompt de instalacion (HU-12).
 *
 * El navegador solo dispara beforeinstallprompt cuando la app cumple los
 * criterios de instalabilidad, asi que el boton se muestra unicamente si
 * el evento llega: nunca se ofrece una accion que no funcionaria.
 */

let deferredPrompt = null;
const listeners = new Set();

const notify = () => listeners.forEach((fn) => fn(Boolean(deferredPrompt)));

export function onInstallAvailable(fn) {
  listeners.add(fn);
  fn(Boolean(deferredPrompt));
  return () => listeners.delete(fn);
}

export async function promptInstall() {
  if (!deferredPrompt) return 'unavailable';
  const prompt = deferredPrompt;
  deferredPrompt = null;
  notify();
  prompt.prompt();
  const { outcome } = await prompt.userChoice;
  return outcome; // 'accepted' | 'dismissed'
}

export function isStandalone() {
  return (
    window.matchMedia?.('(display-mode: standalone)').matches ||
    window.navigator.standalone === true
  );
}

export function registerPwa() {
  window.addEventListener('beforeinstallprompt', (e) => {
    e.preventDefault();
    deferredPrompt = e;
    notify();
  });

  window.addEventListener('appinstalled', () => {
    deferredPrompt = null;
    notify();
  });

  // El service worker solo se registra en producción: en desarrollo
  // interferiría con el hot reload de Vite.
  if ('serviceWorker' in navigator && import.meta.env.PROD) {
    window.addEventListener('load', () => {
      navigator.serviceWorker.register('/sw.js').catch(() => {
        // Sin service worker la app sigue funcionando: solo pierde el modo sin conexión.
      });
    });
  }
}

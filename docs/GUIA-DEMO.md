# Guía de demostración — DIELTECH

Guion corto para mostrar los 3 tiers funcionando en una presentación (~10 min).

## Antes de empezar (dejar corriendo)
1. **MySQL** activo (`localhost:3306`, base `dieletech_db`).
2. **Backend**: `cd dieletech-backend && .\gradlew bootRun` → espera `Started BackendApplicationKt`.
3. **Frontend**: `cd dieletech-frontend && npm run dev` → `http://localhost:5173`.
4. **Android**: emulador Pixel 7 con la app instalada (Play ▶ en Android Studio).

## 1. Backend (30 s)
- Abre `http://localhost:8080/api/courses` en el navegador → muestra los 6 cursos en JSON.
- Mensaje: *"Esta es la API REST en Spring Boot + Kotlin, leyendo de MySQL."*

## 2. Frontend Web (3–4 min)
- `http://localhost:5173` → **Catálogo** con estilos, filtros por tecnología.
- **Registrarse** (correo nuevo) → la cuenta queda activa (auto-verificación en dev).
- **Login** → entra a **Mis Cursos** (vacío).
- Abre un curso → **Comprar** → llena datos → **¡Compra exitosa!**
- Vuelve a **Mis Cursos** → aparece el curso con barra de progreso.
- Abre el curso (**/learn**) → marca lecciones → el progreso se guarda.
- Muestra **"Ver contraseña"** y **"¿Olvidaste tu contraseña?"**.

## 3. App Android (3–4 min)
- Abre la app → **Login / Registro** (mismos endpoints del backend).
- Barra inferior: **Inicio · Catálogo · Mis Cursos · Perfil**.
- Catálogo (datos reales del backend) → curso → **Comprar**.

## 4. Integración total (cierre, 1 min)
- **Compra un curso en el celular** con un correo, luego entra a la **web** con ese mismo correo → aparece en **Mis Cursos**.
- Mensaje: *"Los tres clientes escriben y leen de la misma base MySQL a través de la misma API."*

## Puntos a destacar
- Arquitectura cliente–servidor de 3 tiers conectados.
- Seguridad: contraseñas con BCrypt, sesión con JWT, verificación por correo.
- Persistencia real en MySQL (usuarios y compras sobreviven reinicios).
- App móvil con fallback offline (la demo no se cae aunque falle la red).

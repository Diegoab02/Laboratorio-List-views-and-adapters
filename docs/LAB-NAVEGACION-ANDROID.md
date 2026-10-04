# Laboratorio: List views and adapters — Diseño de navegabilidad DIELTECH (Android)

Autor: Diego Alejandro Betancur Herrera · Universidad Piloto de Colombia · 8° semestre
Materia: Desarrollo de Aplicaciones para Dispositivos Convergentes
Laboratorio Moodle: **List views and adapters**
Fecha: 2026-10-04 · Sprint 7 · HU-44

Repositorio: https://github.com/Diegoab02/DIELTECH

## 1. Objetivo

Comprender los mecanismos disponibles para diseñar la navegabilidad de una
aplicación Android y aplicarlos a DIELTECH — la plataforma de aprendizaje
virtual que vengo construyendo durante el semestre — organizando sus pantallas
en **actividades top-level**, **actividades categoría** y **actividades
detalle/edición**, tal como propone la presentación de apoyo (slides 1–5).

## 2. Preguntas de la diapositiva 1 — respuesta aplicada a DIELTECH

**¿En qué app estoy pensando?** DIELTECH: plataforma de cursos virtuales con
tres perfiles reales (STUDENT, INSTRUCTOR, ADMIN), ya desplegada en backend
Spring Boot + Kotlin, frontend React y app Android en Jetpack Compose.

**¿Qué actividades debería incluir?** De las HU completadas (HU-01 a HU-09) y
las que se construyen en los sprints 10–12 (HU-37 a HU-43) salen 24
actividades: autenticación, catálogo, detalle de curso, checkout, lecciones,
progreso, panel de instructor, panel de estudiante unificado, bandeja de
calificación, rúbricas, métricas y perfil.

**¿Cómo las organizo?** En tres niveles, siguiendo las slides 3, 4 y 5. El
nivel categoría es el que puentea las dos puntas: el usuario llega al
top-level, elige una categoría y desde ahí entra a la acción concreta.

## 3. Comparación con el ejemplo SeCoCo (slide 2)

El ejemplo SeCoCo arranca con "Según Perfiles" porque un sistema de
seguimiento epidemiológico cambia de pantallas según quién entra. DIELTECH
tiene exactamente el mismo problema: lo que ve un estudiante no es lo mismo
que ve un instructor, ni lo que ve un admin. Por eso adopto la misma raíz:
`Según Perfiles` como top-level único, con una rama por rol.

| Elemento SeCoCo | Equivalente DIELTECH |
|-----------------|-----------------------|
| Según Perfiles (top-level) | LoginRouter → StudentHome / InstructorHome / AdminHome |
| Diagnóstico, Seguimiento, Zonas (categorías) | Catálogo, Mis Cursos, Mi Panel, Perfil (STUDENT); Mis Cursos, Por Calificar, Métricas, Perfil (INSTRUCTOR); Cursos, Usuarios, Métricas, Config (ADMIN) |
| Síntomas, Exámenes, Reportes, Recorridos (detalle) | CourseDetail, Checkout, VideoPlayer, AssignmentDetail, EditCourse, GradeSubmission, EditRubric, EditUser, etc. |

## 4. Jerarquía propuesta — DIELTECH Android

### 4.1 Diagrama (estilo SeCoCo, slide 2)

```
                              ┌──────────────────────┐
                              │ Según Perfiles       │  ← TOP-LEVEL (raíz)
                              │  (LoginRouter)       │
                              └──────────┬───────────┘
                 ┌────────────────────────┼────────────────────────┐
                 ▼                        ▼                        ▼
         ┌──────────────┐         ┌──────────────┐         ┌──────────────┐
         │ StudentHome  │         │ InstructorHm │         │ AdminHome    │  ← TOP-LEVEL por rol
         └──────┬───────┘         └──────┬───────┘         └──────┬───────┘
   ┌─────┬─────┼─────┬─────┐      ┌─────┼─────┬─────┐      ┌─────┼─────┬─────┐
   ▼     ▼     ▼     ▼     ▼      ▼     ▼     ▼     ▼      ▼     ▼     ▼     ▼
Inicio Catá- Mis   Mi    Perfil  Mis   Por   Métri Perfil Cursos Usua- Métri Config
       logo  Curs  Panel         Curs  Cali- cas                 rios  cas         ← CATEGORÍAS
                                       ficar
   │     │     │     │     │      │     │     │     │      │     │     │     │
   │     ▼     ▼     ▼     ▼      ▼     ▼     ▼     ▼      ▼     ▼     ▼     ▼
   │  CrsDet  Learn Pend  Edit   EdCrs GradeS Metr  Edit  AdmCrs AdmUsr Metr  EdCfg
   │  Chkout  VidPl AsgnD Prof   EdLsn   ub   Det   Prof  AdmLsn EdRol  Det         ← DETALLE/EDICIÓN
   │  Succes       Rubr                                                   
   │                                                                    
   └── Logout (acción global desde cualquier Perfil)
```

### 4.2 Tabla formal

#### STUDENT (perfil por defecto)

| Nivel | Pantalla | Ruta Compose | HU origen |
|-------|----------|--------------|-----------|
| TOP-LEVEL | StudentHome (`MainScreen`) | `main` | HU-03 |
| CATEGORÍA | Inicio (tab Home) | `main?tab=0` | HU-03 |
| CATEGORÍA | Catálogo (tab Search) | `main?tab=1` | HU-04 |
| CATEGORÍA | Mis Cursos (tab Star) | `main?tab=2` | HU-05 |
| CATEGORÍA | Mi Panel pendientes (tab Panel) | `main?tab=3` | HU-43 |
| CATEGORÍA | Perfil (tab Person) | `main?tab=4` | HU-02 |
| DETALLE/EDIT | Detalle de Curso | `course/{id}` | HU-05 |
| DETALLE/EDIT | Checkout | `checkout/{id}` | HU-06 |
| DETALLE/EDIT | Confirmación compra | `success/{name}` | HU-06 |
| DETALLE/EDIT | Reproductor de lección | `video/{courseId}/{lessonId}` | HU-07 |
| DETALLE/EDIT | Entregar tarea | `assignment/{id}` | HU-38 |
| DETALLE/EDIT | Editar perfil | `profile/edit` | HU-02 |
| GLOBAL | Mapa de navegación (este laboratorio) | `nav_map` | HU-44 |

#### INSTRUCTOR

| Nivel | Pantalla | Ruta Compose | HU origen |
|-------|----------|--------------|-----------|
| TOP-LEVEL | InstructorHome | `instructor_main` | HU-09 |
| CATEGORÍA | Mis Cursos (tab) | `instructor_main?tab=0` | HU-09 |
| CATEGORÍA | Por Calificar (tab) | `instructor_main?tab=1` | HU-39 |
| CATEGORÍA | Métricas (tab) | `instructor_main?tab=2` | HU-41 |
| CATEGORÍA | Perfil (tab) | `instructor_main?tab=3` | HU-02 |
| DETALLE/EDIT | Editar curso | `admin/course/{id}/edit` | HU-09 |
| DETALLE/EDIT | Editar lección | `admin/lesson/{id}/edit` | HU-09 |
| DETALLE/EDIT | Calificar entrega | `grade/{submissionId}` | HU-39 |
| DETALLE/EDIT | Editar rúbrica | `rubric/{assignmentId}` | HU-42 |
| DETALLE/EDIT | Detalle de métrica | `metric/{id}` | HU-41 |

#### ADMIN

| Nivel | Pantalla | Ruta Compose | HU origen |
|-------|----------|--------------|-----------|
| TOP-LEVEL | AdminHome | `admin_main` | HU-09 |
| CATEGORÍA | Cursos (tab) | `admin_main?tab=0` | HU-09 |
| CATEGORÍA | Usuarios (tab) | `admin_main?tab=1` | HU-09 |
| CATEGORÍA | Métricas globales (tab) | `admin_main?tab=2` | HU-41 |
| CATEGORÍA | Configuración (tab) | `admin_main?tab=3` | — |
| DETALLE/EDIT | Editar curso | `admin/course/{id}/edit` | HU-09 |
| DETALLE/EDIT | Editar usuario | `admin/user/{id}/edit` | HU-09 |
| DETALLE/EDIT | Editar rol | `admin/user/{id}/role` | HU-09 |

## 5. Mecanismos de navegación usados

Siguiendo la slide 3 ("navegar de top-level a detalle/edit vía categorías"),
la app usa cuatro mecanismos de los disponibles en Jetpack Compose + Navigation:

1. **Navigation Component (NavHost + NavController).** Único grafo declarado
   en `MainActivity.DielTechApp()`. Cada ruta es una `composable(...)` con
   argumentos tipados por path. Es la columna vertebral.
2. **Bottom Navigation Bar.** Dentro de cada top-level por rol
   (`MainScreen`, `InstructorMainScreen`, `AdminMainScreen`) hay un
   `NavigationBar` que conmuta entre las pantallas categoría del mismo rol.
   El cambio es local (variable `tab`), no empuja al back stack.
3. **Up navigation / back stack.** Al entrar a un detalle (`course/{id}`,
   `video/...`, `assignment/{id}`) se hace `navController.navigate(ruta)` y
   el botón atrás del dispositivo o la flecha del `TopAppBar` regresan a la
   categoría de origen, no al top-level.
4. **Deep routing por rol en el arranque.** `MainActivity` decide el
   `startDestination` leyendo `SessionManager.role(context)`. Un instructor
   nunca aterriza en pantallas de estudiante y viceversa — es la raíz "Según
   Perfiles" del diagrama.

## 6. Reglas de navegación acordadas

Reglas aplicadas de forma consistente en todo el grafo:

1. **Un solo top-level visible por sesión.** El rol del JWT decide cuál. No
   hay switcher a mitad de sesión; para cambiar de rol hay que `logout`.
2. **Las categorías NO se apilan.** Al tocar un ítem de la bottom bar se
   cambia la pestaña actual, no se empuja una nueva pantalla. Esto evita el
   back stack enorme que critica la guía Material.
3. **Los detalles SÍ se apilan.** `course/{id}` → `checkout/{id}` →
   `success/{name}` queda registrado en el back stack para que el botón
   atrás tenga semántica.
4. **Checkout limpia su propia rama al confirmar.** Al navegar a
   `success/{name}` se hace `popUpTo("course/{id}") { inclusive = false }`
   para que al salir el usuario vuelva al detalle del curso, no al checkout.
5. **Logout siempre poppea al login.**
   `navigate("login") { popUpTo(0) }` — vacía el back stack para que no
   quede ninguna pantalla autenticada accesible con atrás.
6. **El mapa de navegación es público dentro de la app.** La pantalla
   `nav_map` (entregable de este laboratorio) es accesible desde la tab
   Perfil de los tres roles para dejar documentada la arquitectura dentro
   del propio producto.

## 7. Cumplimiento del entregable

- [x] Revisada la presentación de apoyo (slides 1–5).
- [x] Respondidas las preguntas de la slide 1.
- [x] Replicado el esquema del ejemplo SeCoCo (slides 2–3) sobre DIELTECH.
- [x] Elaborada propuesta propia con los elementos top-level / categoría /
      detalle-edición (slides 3–5).
- [x] Implementada en la app Android como `NavigationMapScreen.kt` para que
      quede visible dentro del producto, no solo en un documento.
- [x] Registrada como HU-44 en el backlog del proyecto para mantener la
      trazabilidad.
- [x] Reflejada en `claude/ESTADO ACTUAL - SPRINT ACTIVO`.
- [x] URL del repositorio para entregar en Moodle:
      **https://github.com/Diegoab02/DIELTECH** (commit del laboratorio en
      la rama `main`, feature `feature/hu-44-navigation-design`).

## 8. Archivos modificados / creados por este laboratorio

| Archivo | Qué cambia |
|---------|------------|
| `docs/LAB-NAVEGACION-ANDROID.md` | Este documento. |
| `dieletech-android/app/src/main/java/com/dieletech/mobile/ui/screens/NavigationMapScreen.kt` | Pantalla Compose nueva que renderiza el árbol de navegación por rol. |
| `dieletech-android/app/src/main/java/com/dieletech/mobile/MainActivity.kt` | Ruta `nav_map` añadida al `NavHost`. |
| `dieletech-android/app/src/main/java/com/dieletech/mobile/ui/screens/MainScreen.kt` | Entrada "Mapa de navegación" en la tab Perfil del estudiante. |
| `dieletech-android/app/src/main/java/com/dieletech/mobile/ui/screens/InstructorMainScreen.kt` | Misma entrada en la tab Perfil del instructor. |
| `dieletech-android/app/src/main/java/com/dieletech/mobile/ui/screens/AdminMainScreen.kt` | Misma entrada en la tab Perfil del admin. |

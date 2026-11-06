# Guía de Firebase — "Mis Cursos" (Auth + Firestore)

> Proyecto: `MisCursosSuarez` · Package: `com.tecsup.suarez`.
> El package de Android Studio, el `applicationId`
> y el package registrado en Firebase **deben ser idénticos**.

---

## PARTE 1 — Crear el proyecto en Firebase

1. Entra a https://console.firebase.google.com e inicia sesión con tu cuenta Google.
2. **Crear un proyecto** → nombre: `MisCursosSuarez` → Continuar.
3. Desactiva Google Analytics (simplifica) → **Crear proyecto** → esperar → **Continuar**.

## PARTE 2 — Registrar la app Android

1. En la pantalla principal del proyecto pulsa el ícono de **Android**.
2. **Nombre del paquete de Android**: `com.tecsup.suarez` (igual al `applicationId`).
3. Apodo (opcional): `Mis Cursos`. SHA-1: no es necesario para Email/Password.
4. **Registrar app** → **Descargar google-services.json**.
5. Copia el archivo en **`app/google-services.json`** (dentro de la carpeta `app`, al lado de `app/build.gradle.kts`).
   Si queda fuera de `app/`, Gradle no lo lee y el build falla.
6. Los pasos de Gradle que muestra la consola ya están hechos en este proyecto (plugins + BoM).

## PARTE 3 — Activar Authentication (Email/Password)

1. Menú izquierdo: **Compilación → Authentication → Comenzar**.
2. Pestaña **Método de acceso** → **Correo electrónico/contraseña**.
3. Activa **Correo electrónico/contraseña** (deja desactivado "Vínculo por correo") → **Guardar**.

Verás los usuarios registrados en la pestaña **Usuarios**.

## PARTE 4 — Crear Cloud Firestore

1. **Compilación → Firestore Database → Crear base de datos**.
2. Edición **Standard**. Ubicación: elige la más cercana (ej. `southamerica-east1`, São Paulo). No se puede cambiar después.
3. Modo: **Producción** (las reglas las pegamos en el siguiente paso) → **Crear**.

## PARTE 5 — Reglas de seguridad (cada usuario ve solo sus cursos)

1. Firestore → pestaña **Reglas**.
2. Borra todo y pega el contenido de `firestore.rules` del proyecto.
3. Pulsa **Publicar**.

Qué hacen: solo usuarios autenticados; se puede leer/editar/eliminar un documento
únicamente si `resource.data.userId == request.auth.uid`; al crear, el `userId` debe ser el tuyo.

> La app además filtra con `whereEqualTo("userId", uid)`. **Es obligatorio**: Firestore rechaza
> una consulta cuyos resultados posibles no cumplen las reglas. Las reglas protegen; el filtro hace la consulta válida.

## PARTE 6 — Estructura de datos

Colección **`cursos`** (se crea sola al guardar el primer curso):

| Campo        | Tipo   | Descripción                   |
|--------------|--------|-------------------------------|
| titulo       | string | Nombre del curso              |
| descripcion  | string | Descripción                   |
| docente      | string | Docente                       |
| creditos     | number | Créditos                      |
| userId       | string | UID del dueño (Auth)          |
| createdAt    | number | Timestamp en milisegundos     |

El ID del documento lo genera Firestore y se guarda en `Course.id` al leer.

## PARTE 7 — Ejecutar y probar

1. Android Studio → **File → Open** → carpeta del proyecto. Espera el **Gradle Sync**.
2. Coloca `google-services.json` en `app/`.
3. Ejecuta en emulador (API 26+) **con internet**.
4. Pruebas sugeridas:
   - Registrar usuario A → crear 2 cursos → editar uno → eliminar otro.
   - Cerrar sesión → registrar usuario B → debe verse la lista **vacía**.
   - Volver con usuario A → siguen sus cursos. (Esto demuestra el filtrado por usuario.)
5. En la consola verifica: **Authentication → Usuarios** y **Firestore → Datos → cursos**.

## Archivos clave y su función

| Archivo | Función |
|---|---|
| `google-services.json` | Credenciales/IDs del proyecto Firebase para esta app |
| `build.gradle.kts` (proyecto) | Declara el plugin `google-services` |
| `app/build.gradle.kts` | Aplica el plugin, Compose/M3, Firebase BoM, Auth y Firestore |
| `AndroidManifest.xml` | Permiso `INTERNET`, actividad principal |
| `MainActivity.kt` | Punto de entrada; carga el tema y `AuthApp` |
| `AuthApp.kt` | Navegación: login ↔ registro ↔ cursos según la sesión |
| `AuthRepository.kt` / `AuthViewModel.kt` | Login, registro, logout con Firebase Auth |
| `CourseRepository.kt` / `CourseViewModel.kt` | CRUD en Firestore filtrado por `userId` |
| `LoginScreen / RegisterScreen / CoursesScreen` | UI Material 3 |
| `Theme.kt` | `MaterialExpressiveTheme` + color dinámico |
| `firestore.rules` | Reglas de seguridad |

## Material 3 usado (incluye APIs experimentales)

- `MaterialExpressiveTheme` + `MotionScheme.expressive()` — `@ExperimentalMaterial3ExpressiveApi`
- `LoadingIndicator` — `@ExperimentalMaterial3ExpressiveApi`
- `LargeTopAppBar` con scroll colapsable, `ModalBottomSheet` — `@ExperimentalMaterial3Api`
- `ExtendedFloatingActionButton`, `ElevatedCard`, `FilledTonalIconButton`, `AlertDialog`,
  `AssistChip`, `SuggestionChip`, `Snackbar`, `OutlinedTextField`.

Las APIs Expressive requieren `androidx.compose.material3` **1.4.0 o superior** (el BoM `2025.09.00` ya lo trae).

## Problemas frecuentes

| Síntoma | Causa / solución |
|---|---|
| `File google-services.json is missing` | No está en `app/` |
| `No matching client found for package name` | El package de Firebase ≠ `applicationId` |
| `PERMISSION_DENIED` al leer | Reglas no publicadas, o consulta sin `whereEqualTo("userId", …)` |
| `CONFIGURATION_NOT_FOUND` al registrarse | No activaste Email/Password en Authentication |
| La lista no carga y no hay error | Emulador sin internet |
| `Unresolved reference LoadingIndicator` | Material3 < 1.4.0: sube el BoM de Compose |

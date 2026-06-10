# SCCA Móvil — App Android

> Sistema de Control de Calidad de Agua — Aplicación Android Nativa

[![Android CI](https://github.com/tu-org/SccaMovil/actions/workflows/android-ci.yml/badge.svg)](https://github.com/tu-org/SccaMovil/actions)

## 🌊 Descripción

SCCA Móvil es la aplicación Android nativa del **Sistema de Control de Calidad del Agua (SCCA)**. Permite monitorear en tiempo real los parámetros de calidad del agua (pH, temperatura, turbidez, TDS) a través de sensores IoT conectados al backend SccaServer.

### Funcionalidades principales

| Pantalla | Roles | Descripción |
|---|---|---|
| Dashboard | Todos | Última lectura + estado de calidad por sensor con actualización SSE en tiempo real |
| Historial | Todos | Lecturas paginadas con filtro de fechas |
| Análisis IA | Todos | Diagnóstico de calidad generado por Gemini IA |
| Nodos | Admin / Soporte / Gestionador | Gestión de dispositivos IoT |
| Usuarios | Administrador | CRUD de usuarios del sistema |
| Logs | Admin / Soporte | Logs del sistema con filtros |
| Preferencias | Todos | Tema, datos de cuenta, cerrar sesión |

---

## 🛠 Stack Tecnológico

| Capa | Tecnología |
|---|---|
| UI | Jetpack Compose + Material 3 |
| Arquitectura | MVVM + Clean Architecture |
| DI | Hilt |
| Red | Retrofit 2 + OkHttp 4 + SSE |
| Serialización | Kotlinx Serialization |
| Almacenamiento | EncryptedSharedPreferences + DataStore |
| Gráficos | Vico |
| Imágenes | Coil |
| Fechas | kotlinx-datetime |
| Tests | JUnit5 + MockK + Turbine |

---

## ⚙️ Configuración

### Requisitos

- Android Studio Hedgehog o superior
- JDK 17
- Android SDK 35

### Configurar la URL del backend

Crea o edita el archivo `local.properties` en la raíz del proyecto:

```properties
# URL del backend (emulador Android → máquina host)
API_BASE_URL=http://10.0.2.2:8080

# Para release (HTTPS requerido)
API_BASE_URL_RELEASE=https://api.scca.com
```

> **Nota:** `10.0.2.2` es la IP del host desde el emulador Android. Si corres en dispositivo físico, usa la IP local de tu máquina (ej. `192.168.1.100`).

### Correr la app localmente

1. Asegúrate de que `SccaServer` (Spring Boot) esté corriendo en el puerto 8080.
2. Abre el proyecto en Android Studio.
3. Selecciona el emulador o dispositivo.
4. Presiona **Run** o ejecuta:

```bash
./gradlew installDebug
```

---

## 🏗 Arquitectura

```
com.proyecto.scca
├── domain/           ← Kotlin puro. Sin dependencias Android/Retrofit/Compose
│   ├── model/        ← Modelos de dominio (Lectura, Nodo, Usuario...)
│   ├── calidad/      ← Reglas de calidad de agua (ParametrosCalidad)
│   ├── repository/   ← Interfaces de repositorio (contratos)
│   └── usecase/      ← Casos de uso (ObtenerNodosUseCase...)
├── data/
│   ├── remote/
│   │   ├── api/      ← Interfaces Retrofit
│   │   └── dto/      ← DTOs con @Serializable
│   ├── mapper/       ← Extensiones Dto.toDomain()
│   └── repository/   ← Implementaciones de repositorio
├── core/
│   ├── network/      ← ApiResult, SafeApiCall, AuthInterceptor, NetworkModule
│   ├── session/      ← SessionManager, PreferenciasStore
│   └── sse/          ← SseClient (OkHttp SSE con reconexión)
├── di/               ← Módulos Hilt (ApiModule, RepositoryModule)
└── presentation/
    ├── theme/        ← Colors, Typography, SccaTheme
    ├── navigation/   ← NavHost, AppScaffold, RolGuard
    ├── components/   ← SensorCard, StatusChip, StateContent...
    └── feature/
        ├── login/
        ├── dashboard/
        ├── historial/
        ├── analisis/
        ├── nodos/
        ├── usuarios/
        ├── logs/
        └── preferencias/
```

### Flujo de dependencias (Clean Architecture)

```
presentation → domain ← data
                ↑
              core
```

`domain` **no** importa nada de Android, Retrofit ni Compose. Esta restricción permite una futura migración a Kotlin Multiplatform (KMP).

---

## 🔐 Seguridad

- El token JWT se almacena en **EncryptedSharedPreferences** (Android Keystore / AES-256-GCM). Nunca se guarda en texto plano.
- La URL del API se lee de `BuildConfig` (generada desde `local.properties`). **No se hardcodea en el código.**
- En release: solo HTTPS (`network_security_config.xml`).
- HTTP cleartext solo permitido en debug para `10.0.2.2` (emulador) y `localhost`.

---

## 🔄 Tiempo Real (SSE)

El dashboard usa **Server-Sent Events** para recibir lecturas en tiempo real:

```
GET /api/v1/sse/lecturas?token=<JWT>
```

- El token va por **query param** (no header), según el backend `SseAuthFilter`.
- Si SSE no conecta en 30s, hay fallback a **polling cada 30s**.
- Reconexión automática con **backoff exponencial** (hasta 30s).

---

## 👥 Roles y Permisos

| Ruta | ADMINISTRADOR | CLIENTE | SOPORTE | GESTIONADOR |
|---|---|---|---|---|
| Dashboard | ✅ | ✅ | ✅ | ✅ |
| Historial | ✅ | ✅ | ✅ | ✅ |
| Análisis IA | ✅ | ✅ | ✅ | ✅ |
| Preferencias | ✅ | ✅ | ✅ | ✅ |
| Nodos | ✅ | ❌ | ✅ | ✅ |
| Usuarios | ✅ | ❌ | ❌ | ❌ |
| Logs | ✅ | ❌ | ✅ | ❌ |

---

## 🧪 Tests

```bash
# Correr todos los tests unitarios
./gradlew test

# Ktlint
./gradlew ktlintCheck

# Detekt
./gradlew detekt

# Tests de UI (requiere emulador)
./gradlew connectedAndroidTest
```

---

## 📦 Build de Release

```bash
# Configurar firma en local.properties (opcional)
KEYSTORE_PATH=path/to/keystore.jks
KEYSTORE_PASSWORD=...
KEY_ALIAS=...
KEY_PASSWORD=...

# Generar APK release
./gradlew assembleRelease
```

---

## 🍎 Migración futura a iOS (Kotlin Multiplatform)

La arquitectura está preparada para KMP. Solo se reescribe la UI:

1. Mover `domain/` a módulo `shared` de KMP.
2. Reemplazar Retrofit/OkHttp por **Ktor Client** en `data/` (mismo contrato de interfaces).
3. `ParametrosCalidad`, `FechaBackend`, `Validaciones`, mappers y modelos se reutilizan **sin cambios**.
4. UI: Compose Multiplatform o SwiftUI consumiendo el `shared`.

> Ver `docs/ARQUITECTURA.md` para más detalles.

---

## 📄 Licencia

Proyecto SCCA — Uso interno. Todos los derechos reservados.

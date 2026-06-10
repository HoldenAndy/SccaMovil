# Plan de implementación para corregir CI: Ktlint, Detekt y advertencias de GitHub Actions

## 1. Objetivo

Corregir los fallos actuales del pipeline de CI del proyecto Android/Kotlin `SccaMovil`, dejando el repositorio en un estado donde pasen al menos estos comandos:

```bash
./gradlew :app:ktlintCheck --no-daemon
./gradlew :app:detekt --no-daemon
```

Además, preparar una segunda fase para actualizar advertencias de mantenimiento relacionadas con GitHub Actions y Node.js 20, sin mezclar esas advertencias con los fallos reales de calidad de código.

---

## 2. Contexto del problema

El pipeline falla por dos grupos principales de problemas:

1. **Ktlint**
   - Falla por violaciones de formato Kotlin.
   - El task bloqueante es:

   ```text
   :app:ktlintMainSourceSetCheck
   ```

   - Hay errores de:
     - imports desordenados;
     - espacios finales;
     - comas finales faltantes;
     - parámetros multilínea mal formateados;
     - indentación incorrecta;
     - expresiones multilínea que deben empezar en nueva línea;
     - `if` sin llaves cuando otra rama sí usa llaves;
     - líneas con espacios innecesarios.

2. **Detekt**
   - Falla por 11 issues de calidad de código.
   - El task bloqueante es:

   ```text
   :app:detekt
   ```

   - Issues detectados:
     - `SccaChart.kt`: `LongParameterList`.
     - `SccaChart.kt`: `CyclomaticComplexMethod`.
     - `SessionManager.kt`: `TooGenericExceptionCaught`.
     - `SessionManager.kt`: `SwallowedException`.
     - `AnalisisIAScreen.kt`: `MaxLineLength`.
     - `DashboardScreen.kt`: `MaxLineLength`.
     - `MarkdownText.kt`: `LoopWithTooManyJumpStatements`.

Las advertencias de Node.js 20 y `gradle/actions/wrapper-validation` son importantes, pero **no son la causa inmediata del exit code 1**. Deben tratarse como fase posterior.

---

## 3. Principios de trabajo para el agente

El agente debe seguir estas reglas:

1. **No cambiar comportamiento funcional salvo que sea estrictamente necesario.**
2. **No usar `@Suppress` como primera solución.**
3. **No subir thresholds de Detekt para hacer pasar CI.**
4. **No mezclar refactor funcional con cambios de formato masivo en el mismo commit si se puede evitar.**
5. **Ejecutar validaciones después de cada bloque de cambios.**
6. **Priorizar soluciones simples, explícitas y mantenibles.**
7. **Mantener compatibilidad con el stack actual del proyecto.**
8. **No actualizar versiones de Gradle, Kotlin, Compose o plugins salvo que el plan lo indique expresamente.**

---

## 4. Rama de trabajo sugerida

Crear una rama dedicada:

```bash
git checkout -b fix/ci-ktlint-detekt
```

---

## 5. Fase 1: corrección automática de formato Ktlint

### 5.1 Ejecutar autofix

```bash
./gradlew :app:ktlintFormat --no-daemon
```

Luego verificar:

```bash
./gradlew :app:ktlintCheck --no-daemon
```

### 5.2 Si Ktlint sigue fallando

Corregir manualmente los archivos reportados. Priorizar los archivos con más errores.

Archivos mencionados en el log:

```text
app/src/main/java/com/proyecto/scca/core/network/NetworkModule.kt
app/src/main/java/com/proyecto/scca/core/session/SessionManager.kt
app/src/main/java/com/proyecto/scca/core/sse/SseClient.kt
app/src/main/java/com/proyecto/scca/data/repository/LecturaRepositoryImpl.kt
app/src/main/java/com/proyecto/scca/data/repository/NodoRepositoryImpl.kt
app/src/main/java/com/proyecto/scca/di/ApiModule.kt
app/src/main/java/com/proyecto/scca/presentation/components/MarkdownText.kt
app/src/main/java/com/proyecto/scca/presentation/components/SccaChart.kt
app/src/main/java/com/proyecto/scca/presentation/feature/analisis/AnalisisIAScreen.kt
app/src/main/java/com/proyecto/scca/presentation/feature/analisis/AnalisisViewModel.kt
app/src/main/java/com/proyecto/scca/presentation/feature/dashboard/DashboardScreen.kt
app/src/main/java/com/proyecto/scca/presentation/feature/dashboard/DashboardViewModel.kt
app/src/main/java/com/proyecto/scca/presentation/feature/historial/HistorialScreen.kt
app/src/main/java/com/proyecto/scca/presentation/feature/historial/HistorialViewModel.kt
app/src/main/java/com/proyecto/scca/presentation/feature/nodos/NodosScreen.kt
app/src/main/java/com/proyecto/scca/presentation/feature/nodos/NodosViewModel.kt
app/src/main/java/com/proyecto/scca/presentation/feature/usuarios/UsuariosScreen.kt
app/src/main/java/com/proyecto/scca/presentation/feature/usuarios/UsuariosViewModel.kt
app/src/main/java/com/proyecto/scca/presentation/navigation/AppScaffold.kt
app/src/main/java/com/proyecto/scca/presentation/theme/Theme.kt
```

### 5.3 Patrones Ktlint a corregir

#### Espacios indebidos dentro de paréntesis

Mal:

```kotlin
provideApi( client )
```

Bien:

```kotlin
provideApi(client)
```

#### Parámetros multilínea

Mal:

```kotlin
fun example(param1: String,
    param2: Int)
```

Bien:

```kotlin
fun example(
    param1: String,
    param2: Int,
)
```

#### Llamadas largas

Mal:

```kotlin
SomeComposable(title = title, subtitle = subtitle, modifier = modifier, onClick = onClick)
```

Bien:

```kotlin
SomeComposable(
    title = title,
    subtitle = subtitle,
    modifier = modifier,
    onClick = onClick,
)
```

#### If con ramas inconsistentes

Mal:

```kotlin
if (condition) {
    doSomething()
} else doOtherThing()
```

Bien:

```kotlin
if (condition) {
    doSomething()
} else {
    doOtherThing()
}
```

#### Imports

Ordenar imports lexicográficamente y eliminar imports no usados.

---

## 6. Fase 2: corregir Detekt

Ejecutar:

```bash
./gradlew :app:detekt --no-daemon
```

Corregir los issues en el siguiente orden.

---

## 7. Tarea Detekt 1: refactor de `SccaChart.kt`

### 7.1 Problemas actuales

Detekt reporta:

```text
LongParameterList
CyclomaticComplexMethod
```

La función afectada:

```kotlin
SparklineChart(
    data: List<Float>,
    color: Color,
    modifier: Modifier,
    minVal: Float?,
    maxVal: Float?,
    dates: List<String>,
    axisTimeOnly: Boolean,
    unidad: String,
    label: String,
    refLine: Float?,
    refLabel: String?,
    isAlert: ((Float) -> Boolean)?,
    alertColor: Color,
)
```

Problemas:

- tiene más parámetros que el threshold permitido;
- concentra demasiada lógica;
- mezcla cálculo de rango, renderizado, labels, alertas y línea de referencia;
- será difícil de testear y mantener.

### 7.2 Implementación recomendada

Crear un objeto de configuración:

```kotlin
data class SparklineChartConfig(
    val minVal: Float? = null,
    val maxVal: Float? = null,
    val dates: List<String> = emptyList(),
    val axisTimeOnly: Boolean = false,
    val unidad: String = "",
    val label: String = "",
    val refLine: Float? = null,
    val refLabel: String? = null,
    val isAlert: ((Float) -> Boolean)? = null,
    val alertColor: Color,
)
```

Reducir la firma del composable:

```kotlin
@Composable
fun SparklineChart(
    data: List<Float>,
    color: Color,
    modifier: Modifier = Modifier,
    config: SparklineChartConfig,
) {
    // Implementación delegada
}
```

### 7.3 Extraer helpers

Extraer funciones privadas para reducir complejidad:

```kotlin
private fun calculateChartRange(
    data: List<Float>,
    minVal: Float?,
    maxVal: Float?,
): ClosedFloatingPointRange<Float>
```

```kotlin
private fun normalizePoint(
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    height: Float,
): Float
```

```kotlin
private fun buildChartPoints(
    data: List<Float>,
    width: Float,
    height: Float,
    range: ClosedFloatingPointRange<Float>,
): List<Offset>
```

Si la función usa `Canvas`, evaluar helpers con `DrawScope`:

```kotlin
private fun DrawScope.drawSparkline(...)
private fun DrawScope.drawReferenceLine(...)
private fun DrawScope.drawAlertPoints(...)
private fun DrawScope.drawAxisLabels(...)
```

### 7.4 Criterios de aceptación

- `SparklineChart` tiene 10 parámetros o menos.
- La complejidad ciclomática baja por debajo del threshold configurado.
- No se introduce `@Suppress("LongParameterList")`.
- No se introduce `@Suppress("CyclomaticComplexMethod")`.
- El gráfico se sigue mostrando igual visualmente.
- Los call sites quedan actualizados para usar `SparklineChartConfig`.

---

## 8. Tarea Detekt 2: corregir `SessionManager.kt`

### 8.1 Problemas actuales

Detekt reporta:

```text
TooGenericExceptionCaught
SwallowedException
```

Esto indica que el código probablemente captura `Exception` de forma genérica y luego ignora el error.

Ejemplo problemático:

```kotlin
try {
    // operación
} catch (e: Exception) {
    null
}
```

### 8.2 Implementación recomendada

Identificar qué operación se está ejecutando:

- lectura/escritura de preferencias;
- DataStore;
- parsing JSON;
- IO;
- seguridad/permisos;
- token/session decoding.

Capturar excepciones específicas.

Ejemplo:

```kotlin
try {
    // operación de sesión
} catch (e: IOException) {
    Log.e(TAG, "Error reading session data", e)
    null
} catch (e: SecurityException) {
    Log.e(TAG, "Security error reading session data", e)
    null
}
```

Si se usa JSON:

```kotlin
try {
    // parsing
} catch (e: SerializationException) {
    Log.e(TAG, "Invalid session payload", e)
    null
}
```

### 8.3 Criterios de aceptación

- No capturar `Exception` si puede evitarse.
- No ignorar excepciones silenciosamente.
- Registrar el error con contexto.
- Mantener el fallback actual si existe, por ejemplo devolver `null` o limpiar sesión.
- No exponer tokens ni datos sensibles en logs.

---

## 9. Tarea Detekt 3: corregir líneas largas

### 9.1 Archivos afectados

```text
AnalisisIAScreen.kt:44
DashboardScreen.kt:195
DashboardScreen.kt:196
DashboardScreen.kt:213
```

### 9.2 Implementación recomendada

Romper llamadas largas:

```kotlin
Text(
    text = "Contenido largo...",
    modifier = modifier,
)
```

Romper strings largos:

```kotlin
val message = "Primera parte del mensaje " +
    "segunda parte del mensaje"
```

Para textos visibles de UI, preferir recursos string si el proyecto ya usa `strings.xml`.

### 9.3 Criterios de aceptación

- Ninguna línea supera el máximo configurado por Detekt.
- No se usa `@Suppress("MaxLineLength")` salvo para URLs inevitables.
- El texto visible no cambia semánticamente.

---

## 10. Tarea Detekt 4: refactor de `MarkdownText.kt`

### 10.1 Problema actual

Detekt reporta:

```text
LoopWithTooManyJumpStatements
```

Esto suele aparecer cuando un loop contiene varios `continue` o `break`.

### 10.2 Implementación recomendada

Extraer condiciones a funciones pequeñas.

Antes:

```kotlin
for (line in lines) {
    if (line.isBlank()) continue
    if (line.startsWith("#")) continue
    if (line == "---") break

    processLine(line)
}
```

Después:

```kotlin
private fun shouldSkipMarkdownLine(line: String): Boolean {
    return line.isBlank() || line.startsWith("#")
}
```

```kotlin
for (line in lines) {
    if (shouldSkipMarkdownLine(line)) {
        continue
    }

    if (shouldStopMarkdownParsing(line)) {
        break
    }

    processLine(line)
}
```

O, si aplica:

```kotlin
lines
    .asSequence()
    .takeWhile { line -> !shouldStopMarkdownParsing(line) }
    .filterNot(::shouldSkipMarkdownLine)
    .forEach(::processLine)
```

### 10.3 Criterios de aceptación

- Reducir la cantidad de `break`/`continue` dentro del loop.
- Mantener el mismo resultado visual del Markdown.
- No convertir el código en una cadena funcional difícil de leer.
- Si la lógica es compleja, preferir helpers con nombres claros.

---

## 11. Fase 3: validación incremental

Después de cada grupo de cambios ejecutar:

```bash
./gradlew :app:ktlintCheck --no-daemon
```

```bash
./gradlew :app:detekt --no-daemon
```

Al final ejecutar:

```bash
./gradlew clean :app:ktlintCheck :app:detekt --no-daemon
```

Si el tiempo lo permite:

```bash
./gradlew :app:testDebugUnitTest --no-daemon
```

Si existe build Android disponible en CI:

```bash
./gradlew :app:assembleDebug --no-daemon
```

---

## 12. Fase 4: actualizar advertencias de GitHub Actions

Esta fase debe hacerse después de corregir Ktlint y Detekt.

### 12.1 Problema

El log advierte que varias actions usan Node.js 20:

```text
actions/cache@v4
actions/checkout@v4
actions/setup-java@v4
actions/upload-artifact@v4
gradle/actions/wrapper-validation@v3.5.0
```

GitHub indica que Node.js 20 será reemplazado por Node.js 24 en actions.

### 12.2 Implementación sugerida

Actualizar el workflow a versiones recientes compatibles con Node.js 24 cuando estén disponibles en el repositorio.

Ejemplo objetivo:

```yaml
- uses: actions/checkout@v5
- uses: actions/setup-java@v5
- uses: actions/cache@v5
- uses: actions/upload-artifact@v6
```

Para Gradle wrapper validation:

- Revisar si el workflow ya usa `gradle/actions/setup-gradle`.
- Si usa `setup-gradle` moderno y este ya valida el wrapper, eliminar el step separado de `wrapper-validation`.
- Si se mantiene el step separado, actualizarlo a una versión moderna compatible.

### 12.3 Validación

Ejecutar nuevamente el pipeline completo en GitHub Actions.

Criterio:

- no debe fallar por Ktlint;
- no debe fallar por Detekt;
- no deben quedar advertencias críticas de actions obsoletas si hay versión compatible disponible.

---

## 13. Commits sugeridos

Dividir el trabajo en commits pequeños:

### Commit 1

```text
style: apply ktlint formatting
```

Contenido:

- cambios generados por `ktlintFormat`;
- imports ordenados;
- espacios finales eliminados;
- formato multilínea corregido.

### Commit 2

```text
refactor: simplify sparkline chart configuration
```

Contenido:

- `SparklineChartConfig`;
- helpers privados;
- call sites actualizados;
- reducción de complejidad.

### Commit 3

```text
fix: handle session exceptions explicitly
```

Contenido:

- excepciones específicas;
- logging seguro;
- eliminación de excepciones tragadas.

### Commit 4

```text
style: fix detekt line length and markdown loop
```

Contenido:

- líneas largas;
- loop de `MarkdownText`.

### Commit 5

```text
ci: update GitHub Actions runtime versions
```

Contenido:

- updates de actions;
- ajuste o eliminación de wrapper validation si corresponde.

---

## 14. Checklist para el agente

- [ ] Crear rama `fix/ci-ktlint-detekt`.
- [ ] Ejecutar `./gradlew :app:ktlintFormat --no-daemon`.
- [ ] Ejecutar `./gradlew :app:ktlintCheck --no-daemon`.
- [ ] Corregir manualmente errores restantes de Ktlint.
- [ ] Ejecutar `./gradlew :app:detekt --no-daemon`.
- [ ] Refactorizar `SccaChart.kt`.
- [ ] Actualizar call sites de `SparklineChart`.
- [ ] Corregir excepciones en `SessionManager.kt`.
- [ ] Corregir líneas largas en `AnalisisIAScreen.kt`.
- [ ] Corregir líneas largas en `DashboardScreen.kt`.
- [ ] Refactorizar loop en `MarkdownText.kt`.
- [ ] Ejecutar `./gradlew clean :app:ktlintCheck :app:detekt --no-daemon`.
- [ ] Ejecutar pruebas unitarias si existen.
- [ ] Actualizar GitHub Actions en una fase separada.
- [ ] Abrir PR con explicación de los cambios.

---

## 15. Prompt recomendado para ejecutar con un agente

Usar este prompt como instrucción principal:

```text
Necesito corregir el CI del proyecto Android/Kotlin SccaMovil. No hagas cambios funcionales innecesarios. Primero corrige Ktlint con ktlintFormat y ajustes manuales. Luego corrige Detekt sin usar @Suppress como primera opción ni subir thresholds.

Fallas conocidas:
- Ktlint falla en :app:ktlintMainSourceSetCheck por formato Kotlin.
- Detekt falla con 11 issues:
  - SccaChart.kt: LongParameterList y CyclomaticComplexMethod en SparklineChart.
  - SessionManager.kt: TooGenericExceptionCaught y SwallowedException.
  - AnalisisIAScreen.kt: MaxLineLength.
  - DashboardScreen.kt: MaxLineLength.
  - MarkdownText.kt: LoopWithTooManyJumpStatements.

Prioridades:
1. Ejecuta ./gradlew :app:ktlintFormat --no-daemon.
2. Ejecuta ./gradlew :app:ktlintCheck --no-daemon y corrige lo restante.
3. Ejecuta ./gradlew :app:detekt --no-daemon.
4. Refactoriza SparklineChart creando un objeto SparklineChartConfig y extrayendo helpers para reducir complejidad.
5. Corrige SessionManager capturando excepciones específicas y registrando errores sin exponer datos sensibles.
6. Corrige líneas largas y el loop de MarkdownText.
7. Ejecuta ./gradlew clean :app:ktlintCheck :app:detekt --no-daemon.
8. Solo al final, actualiza GitHub Actions para resolver advertencias de Node.js 20.

Entrega final:
- lista de archivos modificados;
- explicación breve de cada cambio;
- comandos ejecutados;
- resultado de validación;
- riesgos o follow-ups pendientes.
```

---

## 16. Definition of Done

El trabajo se considera terminado cuando:

- [ ] `./gradlew :app:ktlintCheck --no-daemon` pasa.
- [ ] `./gradlew :app:detekt --no-daemon` pasa.
- [ ] No se agregaron suppressions injustificadas.
- [ ] No se subieron thresholds para ocultar problemas.
- [ ] La UI relacionada con `SccaChart` se mantiene visualmente equivalente.
- [ ] `SessionManager` registra errores sin exponer datos sensibles.
- [ ] El PR explica claramente qué se corrigió y por qué.
- [ ] Las advertencias de GitHub Actions quedan documentadas o corregidas en una fase separada.

---

## 17. Riesgos

### Riesgo 1: cambio visual en `SccaChart`

Mitigación:

- hacer refactor pequeño;
- mantener cálculos existentes;
- probar pantallas que usan `SparklineChart`.

### Riesgo 2: romper call sites del composable

Mitigación:

- buscar todas las referencias:

```bash
grep -R "SparklineChart(" app/src/main/java
```

- actualizar todos los llamados para usar `SparklineChartConfig`.

### Riesgo 3: logging de información sensible

Mitigación:

- no loggear tokens;
- no loggear payloads completos de sesión;
- loggear solo contexto técnico.

### Riesgo 4: mezclar cambios de CI con refactor

Mitigación:

- separar commits;
- hacer primero calidad de código;
- actualizar GitHub Actions al final.

---

## 18. Notas finales

El objetivo no es únicamente hacer pasar CI. El objetivo es dejar el código más mantenible:

- Ktlint debe resolver formato.
- Detekt debe resolver problemas reales de complejidad y manejo de errores.
- GitHub Actions debe actualizarse después, porque sus advertencias no son la causa inmediata del fallo actual.

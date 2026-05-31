## 📋 Descripción
<!-- Describe detalladamente qué cambios introduce este Pull Request y por qué son necesarios. -->

## 🔗 Tarea / Issue Relacionado
<!-- Enlaza el ID de la tarea o issue correspondiente del tablero (ej. #Dev1-Task3 o issue #12). -->
*   **Tarea:** 

## 🛠️ Tipo de Cambio
- [ ] 🚀 **Feature** (Nueva funcionalidad)
- [ ] 🐛 **Fix** (Corrección de error)
- [ ] ⚙️ **Chore** (Configuración, Gradle, dependencias, etc.)
- [ ] ♻️ **Refactor** (Reorganización de código sin cambio de comportamiento)
- [ ] 📝 **Docs** (Cambios en documentación)
- [ ] 🧪 **Test** (Añadir o modificar pruebas unitarias)

## 📸 Demostración Visual (Opcional)
<!-- Si este PR cambia o agrega elementos de UI (Compose, Canvas, Mapas), adjunta capturas de pantalla o un video corto aquí. -->
| Antes | Después |
| :--- | :--- |
| <!-- Captura antigua --> | <!-- Captura nueva --> |

---

## 🧪 Checklist de Calidad
Por favor, asegúrate de cumplir con los siguientes puntos antes de marcar este PR como listo para revisión:

### 1. Pruebas y Cobertura (Kover)
- [ ] He ejecutado las pruebas unitarias locales (`./gradlew test`).
- [ ] La cobertura de líneas de este PR (archivos modificados/nuevos) es de al menos **60%** (`./gradlew :app:koverXmlReportRelease`).
- [ ] Se han añadido pruebas unitarias para cubrir nuevos casos de uso o flujos de lógica.

### 2. Estilo de Código y Linting
- [ ] He formateado el código con ktlint ejecutando `./gradlew :app:ktlintFormat`.
- [ ] He verificado que no haya advertencias graves de Android Lint con `./gradlew lint`.

### 3. Arquitectura y Convención de Commits
- [ ] La estructura de clases respeta la **Clean Architecture** (UI separada de Datos y Dominio).
- [ ] Los mensajes de mis commits siguen la convención de **Conventional Commits** (ej. `feat(scope): ...`, `fix(scope): ...`).
- [ ] La navegación y la inyección de dependencias se realizan utilizando **NavHost** y **Dagger Hilt** respectivamente.

# Guía de Agentes y Skills de IA en el Workspace

Este directorio `.agents/` contiene las **Skills** (habilidades) optimizadas para modelos de lenguaje (LLMs) y asistentes de inteligencia artificial. Estas skills sirven para "contextualizar" y "guiar" a los agentes que utilizamos en nuestro flujo de desarrollo diario (como **Android Studio Gemini**, **Claude Code** y **Antigravity**), asegurando que sigan las mejores prácticas oficiales de Android.

---

## 💡 ¿Qué son las Skills y cómo funcionan?

Las **Skills** son archivos Markdown estructurados (`SKILL.md`) que contienen metadatos, instrucciones detalladas, snippets de código optimizados y reglas de diseño/arquitectura sobre temas específicos de Android.

Cuando interactúas con un agente de IA en este workspace, el agente analiza automáticamente el contenido del directorio `.agents/skills/` para:
- Conocer las APIs y herramientas específicas que debe emplear.
- Seguir los estándares de arquitectura modular, rendimiento y optimización adecuados.
- Evitar patrones obsoletos o deprecados (por ejemplo, usar CameraX en lugar de Camera1).

---

## 🛠️ Skills Disponibles y Casos de Uso

Actualmente disponemos de **19 skills** instaladas en el directorio `.agents/skills/`. A continuación se describe cuándo y para qué debe usarse cada una:

### Desarrollo de UI y Jetpack Compose
*   **`adaptive`**: Úsala cuando necesites adaptar la interfaz para múltiples pantallas (tablets, plegables, desktop).
*   **`edge-to-edge`**: Úsala para configurar la visualización inmersiva (Edge-to-Edge) y solucionar solapamientos con la barra de estado/navegación.
*   **`styles`**: Úsala para implementar el Styles API experimental en Compose, definir temas de componentes personalizados y evitar parámetros harcodeados.
*   **`migrate-xml-views-to-jetpack-compose`**: Úsala si vas a refactorizar layouts XML antiguos al paradigma moderno de Compose.
*   **`navigation-3`**: Úsala para estructurar flujos de navegación complejos utilizando Jetpack Navigation 3.

### Dispositivos Especializados
*   **`jetpack-compose-m3`**: Específica para wearables (Wear OS) usando Material Design 3.
*   **`display-glasses-with-jetpack-compose-glimmer`**: Úsala para el desarrollo XR con smart glasses utilizando el toolkit Glimmer.

### Rendimiento y Construcción
*   **`r8-analyzer`**: Para optimización del tamaño del APK, análisis de reglas ProGuard y optimización de código mediante R8.
*   **`agp-9-upgrade`**: Úsala como guía paso a paso si vas a migrar la versión del Gradle Plugin a AGP 9.
*   **`perfetto-sql`** y **`perfetto-trace-analysis`**: Úsalas para analizar trazas de rendimiento Perfetto y solucionar cuellos de botella (jank, consumo de memoria o latencia).

### Hardware y APIs de Google Play
*   **`camera1-to-camerax`**: Úsala para migrar integraciones de cámara heredadas a la suite de CameraX.
*   **`engage-sdk-integration`**: Integración con el SDK de Google Play Engage.
*   **`play-billing-library-version-upgrade`**: Migración y mantenimiento de las APIs de compras in-app (Billing Library).

### Identidad y Utilidades de Sistema
*   **`verified-email`**: Implementación de flujos de autenticación e inicio de sesión seguros mediante Credential Manager.
*   **`appfunctions`**: Configuración de App Functions para permitir que Gemini interactúe con el dispositivo.
*   **`android-cli`**: Habilidad para orquestar y operar la línea de comandos `android`.
*   **`skill-creator`**: Permite crear nuevas skills personalizadas basadas en markdown para nuestro equipo.

---

## ⚙️ Configuración del Entorno de Trabajo

Para que los agentes puedan leer estas skills, es necesario configurar tu entorno según la herramienta que utilices:

### 1. Android Studio (Gemini)
El asistente Gemini integrado en Android Studio escanea de forma nativa la carpeta `.agents/` en la raíz del proyecto.
- **Acción:** No necesitas configuración adicional. Al abrir el proyecto, Gemini detectará y usará las skills automáticamente cuando hagas preguntas sobre esos temas.

### 2. Claude Code (Consola / CLI)
Claude Code busca las herramientas y configuraciones locales dentro del directorio `.claude/` o `.agents/` del proyecto.
- **Acción sugerida:** Puedes enlazar simbólicamente o copiar el directorio `.agents/skills` en tu directorio local de Claude si quieres que las detecte sin problemas:
  ```bash
  mkdir -p .claude
  ln -s ../.agents/skills .claude/skills
  ```

### 3. Antigravity / Asistentes basados en Gemini
Antigravity lee directamente la carpeta `.agents/skills/` del proyecto actual y también las carpetas globales en el sistema.
- Si deseas que las habilidades estén disponibles de forma global en tu máquina para cualquier proyecto que abras con Antigravity o Gemini, puedes ejecutar la herramienta de comandos CLI.

---

## 🚀 Uso del CLI de Android (`android`)

Para facilitar el mantenimiento y sincronización de estas skills, disponemos de la herramienta de línea de comandos oficial de Android.

### Instalación de Android CLI:
Si no dispones del binario `android` en tu sistema, puedes instalarlo ejecutando en tu terminal:
```bash
curl -fsSL https://dl.google.com/android/cli/latest/linux_x86_64/install.sh | bash
```

### Comandos Comunes:
- **Listar las habilidades del ecosistema:**
  ```bash
  android skills list
  ```
- **Sincronizar o añadir una habilidad al proyecto:**
  ```bash
  android skills add --skill=<nombre-de-la-skill> --project=.
  ```
- **Instalar todo el set de habilidades para todos tus agentes locales:**
  ```bash
  android skills add --all
  ```
- **Realizar búsquedas rápidas en la base de conocimiento oficial de Android:**
  ```bash
  android docs search "Jetpack Compose Navigation"
  ```

---

> [!TIP]
> **Recomendación para el equipo:** Cuando le pidas ayuda a un agente de IA para codificar una feature (por ejemplo, migrar a Edge-to-Edge), puedes mencionarle explícitamente en el prompt: *"Usa la skill edge-to-edge que tenemos instalada en el workspace"* para asegurar que el código generado use exactamente las directrices locales.

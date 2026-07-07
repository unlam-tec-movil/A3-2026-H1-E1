# GambApp — Rehabilitación Inteligente

> Aplicación Android de rehabilitación física que combina visión por computadora (ML Kit Pose
> Detection), sensores del dispositivo, mapas y sincronización con Health Connect para guiar y
> monitorear sesiones de ejercicio terapéutico.

## Índice

- [Configuración](#configuración)
- [Arquitectura Hexagonal](#arquitectura-hexagonal)
  - [Domain](#domain)
  - [Application](#application)
  - [UI](#ui)
  - [Data](#data)
  - [Reglas de dependencia](#reglas-de-dependencia)
- [Estructura de carpetas](#estructura-de-carpetas)
- [Navegación](#navegación)
- [Inyección de Dependencias](#inyección-de-dependencias)
- [Tests](#tests)

---

## Configuración

El proyecto ejecuta dos workflows de GitHub Actions en cada PR:

1. Análisis estático con `ktlint` y `./gradlew lint`.
2. Tests unitarios con cobertura mínima del 60% (Kover).

El archivo `google-services.json` **no se versiona**. Está provisto en CI/CD como secret
`GOOGLE_SERVICES_JSON` codificado en Base64. Para desarrollo local, descargarlo desde
[Firebase Console](https://console.firebase.google.com) y colocarlo en `app/`.

Para configurar Android Studio, instalar el plugin de
[ktlint](https://plugins.jetbrains.com/plugin/15057-ktlint).

---

## Arquitectura Hexagonal

El proyecto implementa la **Arquitectura Hexagonal (Ports & Adapters)** en un único módulo Android.
El objetivo es aislar la lógica de negocio del framework, haciendo el core testeable sin emulador.

```
ar.edu.unlam.mobile.scaffolding/
│
├── domain/          # Núcleo de negocio — sin Android, sin frameworks
├── application/     # Orquestación y definición de puertos (use cases)
├── ui/              # Adaptador de entrada — Compose, ViewModels, Navigation
└── data/            # Adaptador de salida — Room, Retrofit, Firebase, sensores
```

---

### Domain

La capa más interna. Sin dependencias externas: sólo Kotlin puro.

**`model/`** — Modelos de dominio como `data class`:
`Achievement`, `Clinic`, `Exercise`, `PoseResult`, `SensorReading`, `Session`, `User`

**`repository/`** — Interfaces de repositorio que la capa `application` usa y `data` implementa:
`AchievementRepository`, `ClinicRepository`, `RehabRepository`, `UserRepository`

**`ports/camera/`** — Puerto de la sesión de cámara: `CameraSessionPort`

**`usecase/`** — Servicios de dominio puros: `CalculateJointAngleUseCase`, `SyncMotorUseCase`

---

### Application

Capa de orquestación. Define **qué** hace el sistema (puertos) y **cómo** se coordina
(use cases / interactors). Sin dependencias de Android ni de data.

**`port/out/local/`** — Interfaces de salida hacia almacenamiento local: DB, location, prefs, sensor

**`port/out/remote/`** — Interfaces de salida hacia servicios remotos: map API, routing API

**`service/`** — Interactors y servicios de aplicación:
- `ThemeSensorManager` — auto dark-mode via sensor de luz ambiental
- `HasStoredClinicsInteractor`, `NearClinicsHelper`
- `GetRouteInteractor`

**`usecases/`** — Casos de uso concretos agrupados por dominio:

| Carpeta | Casos de uso |
|---|---|
| `user/` | `LoginUseCase`, `CreateUserUseCase`, `UpdateUserUseCase`, `SignOutUseCase` |
| `location/` | `GetClinicsStoredUseCase`, `SaveClinicUseCase`, `DeleteClinicUseCase`, `PopulateClinicsDbUseCase`, `ObserverLocationUseCase`, `GetClinicsFromAssetsUseCase`, `UpdateClinicUseCase` |
| `mapprefs/` | `GetLastDestinationClinicIdUseCase`, `SaveLastDestinationClinicIdUseCase` |

---

### UI

Adaptador de entrada. Jetpack Compose con patrón contenedor/componente.

- **ViewModels** inyectan use cases o repositorios; exponen estado vía `StateFlow`.
- **Screens (contenedor)**: conectan ViewModel con composables, manejan navegación.
- **Components**: reciben datos y lambdas, sin estado propio.

---

### Data

Adaptador de salida. Implementa los puertos de `application/port/out/`.

**`datasources/local/`** — Room: DAOs, Entities, AppDatabase, SessionPreferences (DataStore), MapScreenPreferences

**`datasources/sensor/`** — Sensores Android: `LightSensorDataSource`, `AccelerometerDataSource`, `StepCounterDataSource`, `StepCounterService`, `MeasurableSensorImpl`, `LightSensor`

**`datasources/camera/`** — CameraX: `CameraXSessionAdapter`

**`datasources/device/`** — ML Kit (`PoseDetectionDataSource`), Health Connect (`HealthConnectDataSource`)

**`datasources/location/`** — Localización: `LocationServicePortImpl`, `BuildConfigApiKeyProviderImpl`

**`datasources/network/`** — Retrofit: modelos GraphHopper, routing API key provider

**`mappers/`** — Conversión entity/DTO ↔ domain: `UserMappers`, `SessionMappers`, `ClinicMappers`, `ExerciseMappers`, `AchievementMappers`

**`repositories/`** — `UserRepositoryImpl`, `RehabRepositoryImpl`, `ClinicsRepositoryImpl`, `AchievementRepositoryImpl`, `MapPrefsRepositoryImpl`, `RoutingRepositoryImpl`

**`di/`** — `AppModule` (Hilt singleton), `ScopeQualifiers`

---

### Reglas de dependencia

| Capa          | Puede importar      | NO puede importar |
|---------------|---------------------|-------------------|
| `domain`      | nada (Kotlin puro)  | application, ui, data |
| `application` | domain únicamente   | ui, data |
| `ui`          | application, domain | data |
| `data`        | application, domain | ui |

---

## Estructura de carpetas

```
app/src/main/java/ar/edu/unlam/mobile/scaffolding/
│
├── MainActivity.kt
├── ScaffoldingApplication.kt
│
├── domain/
│   ├── model/
│   │   ├── Achievement.kt
│   │   ├── Clinic.kt
│   │   ├── Exercise.kt
│   │   ├── PoseResult.kt
│   │   ├── SensorReading.kt
│   │   ├── Session.kt
│   │   └── User.kt
│   ├── ports/
│   │   └── camera/CameraSessionPort.kt
│   ├── repository/
│   │   ├── AchievementRepository.kt
│   │   ├── ClinicRepository.kt
│   │   ├── RehabRepository.kt
│   │   └── UserRepository.kt
│   └── usecase/
│       ├── CalculateJointAngleUseCase.kt
│       └── SyncMotorUseCase.kt
│
├── application/
│   ├── port/out/
│   │   ├── local/  (db, location, prefs, sensor)
│   │   └── remote/ (map, routing)
│   ├── service/
│   │   ├── local/  (ThemeSensorManager, HasStoredClinicsInteractor, NearClinicsHelper)
│   │   └── remote/routing/ (GetRouteInteractor)
│   └── usecases/
│       ├── user/   (Login, CreateUser, UpdateUser, SignOut)
│       ├── location/
│       └── mapprefs/
│
├── data/
│   ├── datasources/
│   │   ├── camera/       CameraXSessionAdapter
│   │   ├── device/
│   │   │   ├── health/   HealthConnectDataSource
│   │   │   └── mlkit/    PoseDetectionDataSource
│   │   ├── local/
│   │   │   ├── dao/      (UserDao, SessionDao, ExerciseDao, AchievementDao, ClinicsDao)
│   │   │   ├── database/ AppDatabase
│   │   │   ├── entities/ (AppEntities, AchievementEntity)
│   │   │   └── preferences/ (SessionPreferences, MapScreenPreferences)
│   │   ├── location/     (LocationServicePortImpl, BuildConfigApiKeyProviderImpl)
│   │   ├── network/      (GraphHopperModels, Constants, BuildConfigRoutingApiKeyProviderImpl)
│   │   └── sensor/       (LightSensorDataSource, AccelerometerDataSource, StepCounterDataSource,
│   │                       StepCounterService, LightSensor, MeasurableSensorImpl)
│   ├── mappers/          (User, Session, Clinic, Exercise, Achievement)
│   ├── repositories/     (User, Rehab, Clinics, Achievement, MapPrefs, Routing)
│   └── di/               (AppModule, ScopeQualifiers)
│
└── ui/
    ├── navigation/
    │   ├── Navigation.kt  ← definición de rutas (Screen sealed class)
    │   └── MainScreen.kt  ← NavHost + Scaffold raíz
    ├── screens/
    │   ├── SplashScreen.kt
    │   ├── OnboardingScreen.kt
    │   ├── LoginScreen.kt
    │   ├── RegisterScreen.kt
    │   ├── DashboardScreen.kt
    │   ├── ProfileScreen.kt
    │   ├── MapScreen.kt
    │   ├── dashboard/
    │   │   └── AchievementsView.kt
    │   ├── progress/
    │   │   ├── ProgressScreen.kt
    │   │   └── ProgressViewModel.kt
    │   └── rehab/
    │       ├── EnvironmentCheckScreen.kt
    │       ├── RehabSessionScreen.kt
    │       ├── RoutineListScreen.kt
    │       ├── PostSessionScreen.kt
    │       └── CameraPreviewComponent.kt
    ├── components/
    │   ├── BottomBar.kt
    │   ├── FABShortCut.kt
    │   ├── LottieAnimation.kt
    │   ├── BottomSheetCard.kt
    │   └── dashboard/
    │       ├── RomProgressCard.kt   ← RomProgressRing incluida
    │       ├── StepsCard.kt         ← StatInfoItem incluida
    │       ├── LastSessionCard.kt   ← DashboardHorizontalDivider incluida
    │       ├── AchievementsCard.kt  ← MiniMedalBadge incluida
    │       └── ActiveRoutineBanner.kt
    ├── viewmodels/
    │   ├── SplashViewModel.kt
    │   ├── OnboardingViewModel.kt
    │   ├── LoginViewModel.kt
    │   ├── RegisterViewModel.kt
    │   ├── DashboardViewModel.kt
    │   ├── ProfileViewModel.kt
    │   ├── RehabSessionViewModel.kt
    │   ├── RoutineListViewModel.kt
    │   ├── EnvironmentCheckViewModel.kt
    │   ├── PostSessionViewModel.kt
    │   ├── AchievementsViewModel.kt
    │   └── MapScreenViewModel.kt
    └── theme/
        ├── Color.kt
        ├── Theme.kt
        └── Type.kt
```

---

## Navegación

Single-activity con `NavHost` definido en `ui/navigation/MainScreen.kt`. Las rutas se declaran
como objetos de la sealed class `Screen` en `Navigation.kt`.

### Flujo de inicio de sesión

```
Splash
  ├── onboarding_completed = false  →  Onboarding  →  Login
  └── onboarding_completed = true   →  Login
                                           ├── éxito        →  Dashboard
                                           └── sin cuenta   →  Register  →  Login
```

### Rutas registradas en el NavHost

| Ruta | Screen | Descripción |
|---|---|---|
| `splash` | `SplashScreen` | Lee preferencia de onboarding; decide redirigir |
| `onboarding` | `OnboardingScreen` | 3 slides explicativos; guarda flag en DataStore |
| `login` | `LoginScreen` | Firebase Auth; guarda token en DataStore + Room |
| `register` | `RegisterScreen` | Firebase createUser; idem login |
| `dashboard` | `DashboardScreen` | Inicio principal con métricas y accesos rápidos |
| `routine_list` | `RoutineListScreen` | Lista de ejercicios de la sesión del día |
| `environment_check/{exerciseId}` | `EnvironmentCheckScreen` | Verificación lumínica previa a la sesión |
| `rehab_session/{exerciseId}` | `RehabSessionScreen` | Sesión con cámara, pose detection y acelerómetro |
| `post_session` | `PostSessionScreen` | Resumen tras completar la sesión |
| `progress` | `ProgressScreen` | Gráficos ROM y FC históricos; sincronización Health Connect |
| `profile` | `ProfileScreen` | Perfil de usuario, historial de sesiones, logros, dark mode |
| `MapScreen` | `MapScreen` | Mapa de clínicas con routing GraphHopper |

### Bottom Navigation (rutas con chrome)

Las siguientes rutas muestran `BottomBar` y el FAB de acceso rápido al mapa:

```
Dashboard · Rutinas · Progreso · Mapa · Perfil
```

Las rutas de Splash, Onboarding, Login y Register **no muestran** chrome (sin bottom bar).

### Flujo de sesión de rehabilitación

```
RoutineList
  └──  [Iniciar ejercicio]
         └── EnvironmentCheck/{exerciseId}
               ├── luz >= 100 lux  →  RehabSession/{exerciseId}  →  PostSession  →  Dashboard
               └── luz < 100 lux   →  [Continuar igual] también lleva a RehabSession
```

### Back stack y popUpTo

- Al completar Login/Register el back stack se limpia hasta Login (inclusive) para evitar
  retroceder al formulario.
- Al hacer Sign Out se limpia todo el back stack (`popUpTo(0)`) y se navega a Login.
- La pantalla de Mapa usa `launchSingleTop = true` para evitar instancias duplicadas.

---

## Inyección de Dependencias

Dagger Hilt en toda la aplicación:

- `ScaffoldingApplication` → `@HiltAndroidApp`
- `MainActivity` → `@AndroidEntryPoint` (inyecta `SessionPreferences` para dark mode reactivo)
- ViewModels → `@HiltViewModel` + `hiltViewModel()` en composables
- Módulo principal: `data/di/AppModule` — registra todos los singletons

### Principales bindings en AppModule

| Interfaz | Implementación |
|---|---|
| `FirebaseAuth` | `FirebaseAuth.getInstance()` |
| `LightSensorDataSource` | `LightSensorDataSource(context)` |
| `AccelerometerDataSource` | `AccelerometerDataSource(context)` |
| `StepCounterDataSource` | `StepCounterDataSource(context)` |
| `SessionPreferences` | `SessionPreferences(context)` |
| `CameraSessionPort` | `CameraXSessionAdapter(context)` |
| `UserRepository` | `UserRepositoryImpl(userDao)` |
| `RehabRepository` | `RehabRepositoryImpl(sessionDao, exerciseDao)` |
| `AchievementRepository` | `AchievementRepositoryImpl(achievementDao)` |
| `LocationServicePort` | `LocationServicePortImpl(context)` |

---

## Tests

### Cobertura mínima requerida: 60% (Kover)

Los tests unitarios se ubican en `app/src/test/` espejando la estructura de producción.

| Archivo de test | Qué cubre |
|---|---|
| `LoginUseCaseTest` | Firebase Auth → DataStore → Room; errores de credenciales |
| `CreateUserUseCaseTest` | Firebase createUser; unicidad de email; trim de datos |
| `UpdateUserUseCaseTest` | Validación de nombre; no hay usuario logueado |
| `SignOutUseCaseTest` | Orden clearSession → clearUser → signOut; idempotencia |
| `LoginViewModelTest` | Validación inline de email/password; estados Loading/Success/Error |
| `RegisterViewModelTest` | Coincidencia de contraseñas; mín. 8 chars; email único |
| `ProfileViewModelTest` | Edit name; toggle dark mode; sign out; sesiones recientes |
| `DashboardViewModelTest` | Carga de usuario, sesiones y pasos; cálculo de ROM máximo |
| `RehabSessionViewModelTest` | Detección de caídas; `setTargetAngle`; `dismissFallAlert` |
| `EnvironmentCheckViewModelTest` | Clasificación de lux en GOOD/FAIR/POOR; sensor no disponible |
| `AccelerometerDataSourceTest` | Umbral de caída 2.5 G; función `isFallDetected` pura |

### Ejecutar tests

```bash
./gradlew testDebugUnitTest
```

### Ejecutar tests con reporte de cobertura

```bash
./gradlew koverXmlReportDebug
```

---

## Referencias

- [Hexagonal Architecture — Alistair Cockburn](https://alistair.cockburn.us/hexagonal-architecture/)
- [Domain-Driven Design — Martin Fowler](https://martinfowler.com/bliki/DomainDrivenDesign.html)
- [Dagger Hilt — Android Docs](https://developer.android.com/training/dependency-injection/hilt-android)
- [Jetpack Compose Navigation](https://developer.android.com/jetpack/compose/navigation)
- [ML Kit Pose Detection](https://developers.google.com/ml-kit/vision/pose-detection/android)
- [Health Connect](https://developer.android.com/health-and-fitness/guides/health-connect)

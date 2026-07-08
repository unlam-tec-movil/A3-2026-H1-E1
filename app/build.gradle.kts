import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.ksp)
    alias(libs.plugins.daggerHiltAndroid)
    alias(libs.plugins.kover)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.kotlin.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
    // id("com.google.gms.google-services")
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
        languageVersion.set(KotlinVersion.KOTLIN_2_1)
        apiVersion.set(KotlinVersion.KOTLIN_2_1)
    }
}

// Load API_KEY from local.properties
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { localProperties.load(it) }
}
val mapApiKey: String = (localProperties.getProperty("API_KEY") ?: "").trim { it == '"' }
val graphHopperApiKey: String = (localProperties.getProperty("GRAPH_HOPPER_API_KEY") ?: "").trim { it == '"' }
android {

    packaging {
        resources {
            excludes += "META-INF/native-image/**"
        }
    }
    namespace = "ar.edu.unlam.mobile.scaffolding"
    compileSdk = 36

    defaultConfig {
        buildConfigField("String", "API_KEY", "\"${mapApiKey}\"")
        buildConfigField("String", "GRAPH_HOPPER_API_KEY", "\"${graphHopperApiKey}\"")
        applicationId = "ar.edu.unlam.mobile.scaffolding"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    testOptions {
        managedDevices {
            localDevices {
                create("pixel6api34") {
                    device = "Pixel 6"
                    apiLevel = 34
                    systemImageSource = "google_apis"
                }
            }
        }
        unitTests {
            isReturnDefaultValues = true
            // Configure JUnit 5 support for JVM-based integration tests
            all { task ->
                task.useJUnitPlatform {
                    includeEngines("junit-vintage", "junit-jupiter", "kotest")
                }
            }
        }
        packaging {
            resources {
                excludes += "/META-INF/{AL2.0,LGPL2.1}"
            }
        }
    }
    kover {
        reports {
            filters {
                excludes {
                    annotatedBy("androidx.compose.runtime.Composable")
                }
            }
        }
    }

    configurations.all {
        exclude(group = "com.intellij", module = "annotations")
    }

    dependencies {

        // Firebase Authentication
        implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
        implementation("com.google.firebase:firebase-auth-ktx")

        // Splash Screen
        implementation("androidx.core:core-splashscreen:1.0.1")

        // Base
        implementation(libs.androidx.core.ktx)
        implementation(libs.androidx.lifecycle.runtime.ktx)
        implementation(libs.androidx.activity.compose)
        implementation(platform(libs.androidx.compose.bom))
        implementation(libs.androidx.ui)
        implementation(libs.androidx.ui.graphics)
        implementation(libs.androidx.ui.tooling.preview)
        implementation(libs.androidx.material3)
        implementation(libs.androidx.material.icon)
        implementation(libs.firebase.analytics)
        testImplementation(libs.junit)
        androidTestImplementation(libs.androidx.junit)
        androidTestImplementation(libs.androidx.espresso.core)
        androidTestImplementation("androidx.test.espresso:espresso-intents:3.5.1")
        androidTestImplementation("androidx.test:runner:1.5.2")
        androidTestImplementation("androidx.test.ext:junit:1.1.5")
        androidTestImplementation(platform(libs.androidx.compose.bom))
        androidTestImplementation(libs.androidx.ui.test.junit4)
        debugImplementation(libs.androidx.ui.tooling)
        debugImplementation(libs.androidx.ui.test.manifest)

        // Dagger + Hilt
        implementation(libs.google.dagger.hilt.android)
        ksp(libs.google.dagger.hilt.android.compiler)
        implementation(libs.google.dagger.hilt.android.testing)
        implementation(libs.androidx.hilt.navigation.compose)
        androidTestImplementation(libs.google.dagger.hilt.android.testing)
        testImplementation(libs.google.dagger.hilt.android.testing)

        // Navigation Compose
        implementation(libs.navigation.compose)

        // Room (Persistencia Local)

        implementation(libs.room.runtime)
        implementation(libs.room.ktx)
        ksp(libs.room.compiler)

        // Maptiler
        implementation(libs.maptiler.sdk.kotlin)

        // DataStore (Configuración / Cache de Sesión)
        implementation("androidx.datastore:datastore-preferences:1.0.0")

        // CameraX & ML Kit (Dev 2 - Visión Artificial)
        implementation("androidx.camera:camera-camera2:1.3.1")
        implementation("androidx.camera:camera-lifecycle:1.3.1")
        implementation("androidx.camera:camera-view:1.3.1")
        implementation("com.google.mlkit:pose-detection:18.0.0-beta3")

        // Play Services Location (Dev 3)
        implementation(libs.play.services.location)

        // Health Connect Client (Dev 4)
        implementation("androidx.health.connect:connect-client:1.1.0-alpha07")

        // Retrofit (Network / API Directions)
        implementation("com.squareup.retrofit2:retrofit:2.9.0")
        implementation("com.squareup.retrofit2:converter-gson:2.9.0")

        // Guava (Required for ListenableFuture in CameraX)
        implementation(libs.guava)

        // Testing (Unitarios y Flow testing)
        testImplementation("io.mockk:mockk:1.13.9")
        testImplementation(libs.kotlinx.coroutines.test)
        testImplementation("app.cash.turbine:turbine:1.0.0")

        // Room Testing Library - for JVM-based integration tests

        testImplementation(libs.room.testing)
        testImplementation(libs.sqlite.bundled)
        testRuntimeOnly("org.junit.vintage:junit-vintage-engine")
        testImplementation(libs.sqlite.jdbc)

        // Robolectric - for Android framework simulation in JVM tests
        testImplementation(libs.robolectric)

        // Kotest - Property-based testing framework
        testImplementation(libs.kotest.runner.junit5)
        testImplementation(libs.kotest.property)
        testImplementation(libs.kotest.assertions.core)

        // JUnit 5 - for running integration tests alongside JUnit 4
        testImplementation(libs.junit)
        testImplementation(libs.junit5.api)
        testRuntimeOnly("org.junit.vintage:junit-vintage-engine:5.10.0")
        testRuntimeOnly(libs.junit5.engine)

        // Coroutine test support
        testImplementation(libs.kotlinx.coroutines.test)
        // KotlinSerialization
        implementation(libs.kotlinx.serialization.json)
        implementation(libs.androidx.compose.animation)

        // Lootie
        implementation("com.airbnb.android:lottie-compose:6.7.1")
    }
}
dependencies {
    implementation(libs.play.services.maps)
}

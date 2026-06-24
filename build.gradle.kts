// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
    alias(libs.plugins.daggerHiltAndroid) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kover) apply false
    alias(libs.plugins.ktlint)
    id("com.google.gms.google-services") version "4.4.2" apply false
}

evaluationDependsOn(":app")

tasks.register("prCheck") {
    group = "verification"
    description = "Ejecuta verificaciones locales de PR (formateo, tests, cobertura y lint)."

    dependsOn("ktlintFormat")
    dependsOn(":app:test")
    dependsOn(":app:koverXmlReportRelease")
    dependsOn(":app:lint")
}

// Configurar el orden de ejecución
val ktlintFormatTask = project(":app").tasks.named("ktlintFormat")
val testTask = project(":app").tasks.named("test")
val lintTask = project(":app").tasks.named("lint")
val koverTask = project(":app").tasks.named("koverXmlReportRelease")

testTask.configure { mustRunAfter(ktlintFormatTask) }
lintTask.configure { mustRunAfter(ktlintFormatTask) }
koverTask.configure { mustRunAfter(testTask) }

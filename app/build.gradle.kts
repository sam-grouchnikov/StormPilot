plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
    id("com.google.gms.google-services")
}

val legacyStormPilotApiBaseUrl = providers.gradleProperty("stormpilotApiBaseUrl")
val stormPilotEmulatorApiBaseUrl = providers.gradleProperty("stormpilotEmulatorApiBaseUrl")
    .orElse(legacyStormPilotApiBaseUrl)
    .orElse("http://10.0.2.2:8080/api/v1")
val stormPilotPhysicalDeviceApiBaseUrl = providers.gradleProperty("stormpilotPhysicalDeviceApiBaseUrl")
    .orElse(legacyStormPilotApiBaseUrl)
    .orElse("http://127.0.0.1:8080/api/v1")
val legacyRadarTileBaseUrl = providers.gradleProperty("radarTileBaseUrl")
val radarTileEmulatorBaseUrl = providers.gradleProperty("radarTileEmulatorBaseUrl")
    .orElse(legacyRadarTileBaseUrl)
    .orElse("http://10.0.2.2:8090")
val radarTilePhysicalDeviceBaseUrl = providers.gradleProperty("radarTilePhysicalDeviceBaseUrl")
    .orElse(legacyRadarTileBaseUrl)
    .orElse("http://127.0.0.1:8090")

android {
    namespace = "com.example.stormpilot"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.stormpilot"
        minSdk = 28
        //noinspection OldTargetApi
        targetSdk = 36
        compileSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    flavorDimensions += "backendTarget"
    productFlavors {
        create("emulator") {
            dimension = "backendTarget"
            buildConfigField(
                "String",
                "STORMPILOT_API_BASE_URL",
                "\"${stormPilotEmulatorApiBaseUrl.get()}\"",
            )
            buildConfigField(
                "String",
                "RADAR_TILE_BASE_URL",
                "\"${radarTileEmulatorBaseUrl.get()}\"",
            )
        }
        create("physicalDevice") {
            dimension = "backendTarget"
            buildConfigField(
                "String",
                "STORMPILOT_API_BASE_URL",
                "\"${stormPilotPhysicalDeviceApiBaseUrl.get()}\"",
            )
            buildConfigField(
                "String",
                "RADAR_TILE_BASE_URL",
                "\"${radarTilePhysicalDeviceBaseUrl.get()}\"",
            )
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.play.services.location)
    implementation(libs.androidx.compose.animation.core)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    testImplementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    testImplementation("io.mockk:mockk:1.13.10")
    testImplementation("io.mockk:mockk-android:1.13.10")
    testImplementation("app.cash.turbine:turbine:1.1.0")
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation("androidx.compose.material:material-icons-extended:1.7.8")
    implementation("androidx.navigation:navigation-compose:2.9.7")
    implementation("androidx.compose.ui:ui-text-google-fonts:1.10.4")
    implementation(libs.maplibre.compose)
    implementation("com.google.android.material:material:1.11.0")

    implementation("androidx.compose.material3:material3")

    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.10.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    implementation("com.google.dagger:hilt-android:2.59.2")
    ksp("com.google.dagger:hilt-compiler:2.59.2")

    implementation("androidx.hilt:hilt-navigation-compose:1.3.0")

    implementation(platform("com.google.firebase:firebase-bom:34.13.0"))
    implementation("com.google.firebase:firebase-analytics")
    dependencies {
        implementation("com.patrykandpatrick.vico:compose:3.2.3")
    }

}

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}
// AGP 9 has built-in Kotlin support: the org.jetbrains.kotlin.android plugin must NOT be applied.
// Release versioning/signing are driven by environment (set by .github/workflows/release.yml).
// Local builds fall back to the defaults below and the debug key.
val releaseVersionName: String = System.getenv("VERSION_NAME") ?: "0.1.2"
val releaseVersionCode: Int = System.getenv("VERSION_CODE")?.toIntOrNull() ?: 3
val releaseKeystore: String? = System.getenv("KEYSTORE_FILE")

android {
    namespace = "com.tokidiscover.app"
    compileSdk = 37
    defaultConfig {
        applicationId = "com.tokidiscover.app"
        // TokiUI Android target is Android 16+ (API 36). 0.1.0 shipped minSdk 26.
        minSdk = 36
        targetSdk = 36
        versionCode = releaseVersionCode
        versionName = releaseVersionName
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    signingConfigs {
        if (releaseKeystore != null) {
            create("release") {
                storeFile = file(releaseKeystore)
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
            }
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.findByName("release") ?: signingConfigs.getByName("debug")
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
    sourceSets.named("main") {
        // Official TokiUI parser + token JSON, snapshotted under android/tokiui
        // because this public repo cannot see the private TokiUI checkout in CI.
        kotlin.directories.add("../tokiui/src/main/kotlin")
        assets.directories.add("../tokiui/tokens")
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.material.color.utilities)
    implementation(libs.haze)
    implementation(libs.haze.blur)
    implementation(libs.kotlinx.coroutines.android)
    testImplementation(libs.junit)
    testImplementation(libs.org.json)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

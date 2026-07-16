plugins {
    alias(libs.plugins.android.application)
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "io.github.anishfyi.aperture"
    compileSdk = 37

    defaultConfig {
        applicationId = "io.github.anishfyi.aperture"
        minSdk = 24
        targetSdk = 35
        versionCode = 5
        versionName = "1.3.1"
    }

    flavorDimensions += listOf("implementation", "ovpnimpl")
    productFlavors {
        create("skeleton") {
            dimension = "implementation"
        }
        create("ovpn23") {
            dimension = "ovpnimpl"
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
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
        jniLibs {
            // ics-openvpn ships the openvpn binary as libovpnexec.so and
            // exec()s it at runtime, which requires the native libs to be
            // extracted to disk (extractNativeLibs=true), not loaded from the
            // compressed APK. Without this, connecting fails with
            // "Cannot run program .../libovpnexec.so: No such file".
            useLegacyPackaging = true
        }
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.10.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation(project(":openvpn"))

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    implementation(libs.square.okhttp)

    debugImplementation("androidx.compose.ui:ui-tooling")
}

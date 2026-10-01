plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.kapt")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}

fun quotedBuildConfigString(value: String): String =
    "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\""

android {
    namespace = "com.focusforge.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.focusforge.app"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
    }

    signingConfigs {
        create("release") {
            val storeFilePath = System.getenv("FOCUSFORGE_RELEASE_KEYSTORE")
            val storePasswordValue = System.getenv("FOCUSFORGE_RELEASE_STORE_PASSWORD")
            val keyAliasValue = System.getenv("FOCUSFORGE_RELEASE_KEY_ALIAS")
            val keyPasswordValue = System.getenv("FOCUSFORGE_RELEASE_KEY_PASSWORD")
            if (!storeFilePath.isNullOrBlank() &&
                !storePasswordValue.isNullOrBlank() &&
                !keyAliasValue.isNullOrBlank() &&
                !keyPasswordValue.isNullOrBlank()
            ) {
                storeFile = file(storeFilePath)
                storePassword = storePasswordValue
                keyAlias = keyAliasValue
                keyPassword = keyPasswordValue
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            val endpoint = providers.gradleProperty("focusforgeApiBaseUrlDebug")
                .orElse("http://10.0.2.2:8080")
                .get()
                .trim()
            buildConfigField("String", "API_BASE_URL", quotedBuildConfigString(endpoint))
            buildConfigField("Boolean", "API_REQUIRES_HTTPS", "false")
        }
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
            val endpoint = providers.gradleProperty("focusforgeApiBaseUrlRelease").orNull?.trim()
                ?: throw GradleException(
                    "Release builds require -PfocusforgeApiBaseUrlRelease=https://<production-host>. " +
                        "Refusing to ship a placeholder or localhost endpoint."
                )
            require(endpoint.startsWith("https://")) { "Release API endpoint must use HTTPS." }
            require(!endpoint.contains("localhost.invalid")) { "Release API endpoint cannot be the placeholder." }
            buildConfigField("String", "API_BASE_URL", quotedBuildConfigString(endpoint))
            buildConfigField("Boolean", "API_REQUIRES_HTTPS", "true")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions { jvmTarget = "17" }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    sourceSets {
        getByName("androidTest").assets.srcDir("${project.projectDir}/schemas")
    }
}

ksp {
    arg("room.schemaLocation", "${project.projectDir}/schemas")
    arg("room.generateKotlin", "true")
}

dependencies {
    val bom = platform("androidx.compose:compose-bom:2025.09.00")
    implementation(bom)
    androidTestImplementation(bom)
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.activity:activity-compose:1.11.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.4")
    implementation("androidx.navigation:navigation-compose:2.9.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    implementation("com.google.dagger:hilt-android:2.57.1")
    kapt("com.google.dagger:hilt-compiler:2.57.1")
    implementation("androidx.room:room-runtime:2.8.3")
    implementation("androidx.room:room-ktx:2.8.3")
    ksp("androidx.room:room-compiler:2.8.3")
    testImplementation("androidx.room:room-testing:2.8.3")
    implementation("androidx.datastore:datastore-preferences:1.2.1")
    implementation("androidx.work:work-runtime-ktx:2.10.5")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
    debugImplementation("androidx.compose.ui:ui-tooling")
}

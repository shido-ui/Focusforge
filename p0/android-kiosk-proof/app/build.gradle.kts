plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "com.focusforge.p0.kiosk"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.focusforge.p0.kiosk"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "0.1-p0"
    }
}
dependencies {
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.activity:activity-ktx:1.11.0")
}
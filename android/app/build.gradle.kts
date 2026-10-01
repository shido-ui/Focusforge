plugins {
 id("com.android.application")
 id("org.jetbrains.kotlin.android")
 id("org.jetbrains.kotlin.plugin.compose")
 id("org.jetbrains.kotlin.kapt")
 id("com.google.dagger.hilt.android")
}
android {
 namespace="com.focusforge.app"
 compileSdk=36
 defaultConfig { applicationId="com.focusforge.app"; minSdk=28; targetSdk=36; versionCode=1; versionName="0.1.0" }
 buildTypes { release { isMinifyEnabled=false }; debug { applicationIdSuffix=".debug"; versionNameSuffix="-debug" } }
 compileOptions { sourceCompatibility=JavaVersion.VERSION_17; targetCompatibility=JavaVersion.VERSION_17 }
 kotlinOptions { jvmTarget="17" }
 buildFeatures { compose=true; buildConfig=true }
}
dependencies {
 val bom=platform("androidx.compose:compose-bom:2025.09.00")
 implementation(bom); androidTestImplementation(bom)
 implementation("androidx.core:core-ktx:1.17.0")
 implementation("androidx.activity:activity-compose:1.11.0")
 implementation("androidx.compose.ui:ui")
 implementation("androidx.compose.material3:material3")
 implementation("androidx.compose.ui:ui-tooling-preview")
 implementation("com.google.dagger:hilt-android:2.57.1")
 kapt("com.google.dagger:hilt-compiler:2.57.1")
 testImplementation("junit:junit:4.13.2")
 debugImplementation("androidx.compose.ui:ui-tooling")
}

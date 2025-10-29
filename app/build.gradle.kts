plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.kotlin.kapt")
    alias(libs.plugins.navigation.safeargs)
}

android {
    namespace = "com.example.myspecial.moodtunes"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.myspecial.moodtunes"
        minSdk = 36
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

        // Room Database
        implementation(libs.androidx.room.runtime)
        implementation(libs.androidx.room.ktx)
        kapt(libs.androidx.room.compiler)

        // Retrofit for API
        implementation(libs.retrofit.core)
        implementation(libs.retrofit.converter.gson)

        // Navigation Component
        implementation(libs.androidx.navigation.fragment.ktx)
        implementation(libs.androidx.navigation.ui.ktx)

        // ViewModel & LiveData
        implementation(libs.androidx.lifecycle.viewmodel.ktx)
        implementation(libs.androidx.lifecycle.livedata.ktx)

        // Coroutines
        implementation(libs.kotlinx.coroutines.android)

        // Optional: For better RecyclerView
        implementation(libs.androidx.recyclerview)

    // Spotify Auth (for authentication)
    implementation(libs.spotify.auth)

    // For handling redirects
    implementation(libs.browser)

    // For secure storage of tokens
    implementation(libs.security.crypto)

    // For HTTP requests
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)

}
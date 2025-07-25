@file:Suppress("DEPRECATION")

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
    id("com.google.devtools.ksp")

}

android {
    namespace = "com.example.androidproject1"
    compileSdk = 35

    buildFeatures {
        viewBinding =true
    }

    defaultConfig {
        applicationId = "com.example.androidproject1"
        minSdk = 27
        targetSdk = 35
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
    implementation(libs.firebase.auth.ktx.v2230)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Firebase
    implementation(libs.firebase.database)
    implementation(libs.firebase.storage)

    // Firebase UI
    implementation(libs.firebaseui.database)
    implementation(libs.firebaseui.storage)

    // Glide
    implementation(libs.github.glide)
    ksp(libs.glide.ksp)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebaseui.firebase.ui.database)

    implementation(libs.firebaseui.firebase.ui.storage)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

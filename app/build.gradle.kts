plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.kotlin.kapt") version "1.9.24"
}

android {
    namespace = "com.soordinary.todo"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.soordinary.todo"
        minSdk = 24
        targetSdk = 34
        versionCode = 3
        versionName = "2.0.2"

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
        debug {
            applicationIdSuffix =".debug"
            isDebuggable = true
        }
    }
    buildFeatures {
        viewBinding = true
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
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.circleimageview)
    implementation(libs.navigation.fragment.ktx)
    implementation(libs.navigation.ui.ktx)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation("androidx.work:work-runtime:2.8.0")
    implementation("com.github.bumptech.glide:glide:4.15.1")
    implementation("io.noties.markwon:core:4.6.2")
    implementation("androidx.hilt:hilt-common:1.0.0")
    implementation("androidx.lifecycle:lifecycle-service:2.6.2")
    kapt("com.github.bumptech.glide:compiler:4.15.1")
    kapt("androidx.room:room-compiler:2.5.1")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
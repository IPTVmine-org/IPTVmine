plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.samyak2403.iptvmine"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.samyak2403.iptvmine"
        minSdk = 21
        targetSdk = 34
        versionCode = 10
        versionName = "1.0.9"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures{
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    //for exoplayer
    implementation(libs.exoplayerCore)
    implementation(libs.exoplayerUi)

    //for playing online content
    implementation(libs.exoplayerDash)
    implementation(libs.androidx.fragment)
    implementation(libs.cronet.embedded)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation (libs.smoothbottombar)
    // Fragment library
    implementation("androidx.fragment:fragment-ktx:1.6.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")

    implementation("com.google.code.gson:gson:2.8.8")

    implementation("com.squareup.okhttp3:okhttp:4.11.0")

    implementation ("com.airbnb.android:lottie:4.2.2")

    // SpeedTest Library
    implementation("fr.bmartel:jspeedtest:1.32.1")
    // Gauge Library
    implementation("com.github.Gruzer:simple-gauge-android:0.3.1")

    //Toast
    implementation ("com.github.samyak2403:TastyToasts:1.0.2")

    implementation ("de.hdodenhof:circleimageview:3.1.0")

//    implementation ("com.github.halilozercan:BetterVideoPlayer:2.0.0-alpha01")
//    implementation ("com.github.halilozercan:BetterVideoPlayer:v1.1.0")
//    implementation ("com.github.halilozercan:BetterVideoPlayer:1.1.0")

//    implementation(project(mapOf("path" to ":bettervideoplayer")))

    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0") // For older versions of LiveData
    // or
//    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.7.1" )// For the latest versions with Kotlin extensions
//    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.1") // For ViewModel
    // Add other dependencies here
}
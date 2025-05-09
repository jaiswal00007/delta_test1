plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.delta"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.delta"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {

        debug {
            buildConfigField(
                "String",
                "GEMINI_API_KEY",
                "\"${project.findProperty("GEMINI_API_KEY") ?: "API_KEY_NOT_FOUND"}\""
            )
            buildConfigField(
                "String",
                "OPENWEATHER_API_KEY",
                "\"${project.findProperty("OPENWEATHER_API_KEY") ?: "API_KEY_NOT_FOUND"}\""
            )
            buildConfigField(
                "String",
                "STABILITY_API_KEY",
                "\"${project.findProperty("STABILITY_API_KEY") ?: "API_KEY_NOT_FOUND"}\""
            )
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            buildConfigField(
                "String",
                "GEMINI_API_KEY",
                "\"${project.findProperty("GEMINI_API_KEY") ?: "API_KEY_NOT_FOUND"}\""
            )
            buildConfigField(
                "String",
                "OPENWEATHER_API_KEY",
                "\"${project.findProperty("OPENWEATHER_API_KEY") ?: "API_KEY_NOT_FOUND"}\""
            )
            buildConfigField(
                "String",
                "STABILITY_API_KEY",
                "${project.findProperty("STABILITY_API_KEY") ?: "API_KEY_NOT_FOUND"}\""
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
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation ("com.squareup.okhttp3:okhttp:4.11.0")  // For API requests
    implementation ("org.json:json:20210307") // For handling JSON responses
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.google.android.gms:play-services-location:21.0.1")
    implementation("io.coil-kt:coil-compose:2.4.0")

}

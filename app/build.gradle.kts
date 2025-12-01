plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.ksp)
}

android {
    namespace = "com.example.moodfood"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.moodfood"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        // Load credentials from .env file
        var apiKey = ((project.findProperty("OPENROUTER_API_KEY") as String?) ?: "").trim()
        var model = ((project.findProperty("OPENROUTER_MODEL") as String?) ?: "deepseek/deepseek-chat-v3.1").trim()
        var referer = ((project.findProperty("OPENROUTER_REFERER") as String?) ?: "https://moodfood.app").trim()
        
        // If not found in gradle.properties, try .env file
        if (apiKey.isEmpty()) {
            val envFile = rootProject.file(".env")
            if (envFile.exists()) {
                val lines = envFile.readLines()
                apiKey = (lines.firstOrNull { it.startsWith("OPENROUTER_API_KEY=") }?.substringAfter('=') ?: "").trim()
                model = (lines.firstOrNull { it.startsWith("OPENROUTER_MODEL=") }?.substringAfter('=') ?: model).trim()
                referer = (lines.firstOrNull { it.startsWith("OPENROUTER_REFERER=") }?.substringAfter('=') ?: referer).trim()
            }
        }
        
        // Validate API key is present
        if (apiKey.isEmpty()) {
            logger.warn("WARNING: OPENROUTER_API_KEY is not set! Please create a .env file with your API key.")
            logger.warn("Copy .env.example to .env and add your API key.")
        }
        
        debug {
            // Use credentials from .env file (no hardcoded keys)
            buildConfigField("String", "OPENROUTER_API_KEY", "\"${'$'}apiKey\"")
            buildConfigField("String", "OPENROUTER_MODEL", "\"${'$'}model\"")
            buildConfigField("String", "OPENROUTER_REFERER", "\"${'$'}referer\"")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "OPENROUTER_API_KEY", "\"${'$'}apiKey\"")
            buildConfigField("String", "OPENROUTER_MODEL", "\"${'$'}model\"")
            buildConfigField("String", "OPENROUTER_REFERER", "\"${'$'}referer\"")
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
    // Navigation + DataStore + Icons
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.retrofit2)
    implementation(libs.retrofit2.converter.scalars)
    implementation(libs.okhttp3)
    implementation(libs.okhttp3.logging)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
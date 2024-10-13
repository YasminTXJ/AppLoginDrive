import com.android.build.api.dsl.Packaging

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.example.applogindrive"
    compileSdk = 34

    packaging {
        resources {
            resources.excludes.add("META-INF/DEPENDENCIES")
            resources.excludes.add("META-INF/NOTICE")
            resources.excludes.add("META-INF/LICENSE")

        }
    }
    defaultConfig {
        applicationId = "com.example.applogindrive"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation ("com.google.android.gms:play-services-auth:20.7.0")
    implementation("com.google.api-client:google-api-client:1.35.1")
    implementation("com.google.apis:google-api-services-drive:v3-rev136-1.25.0")
    //Drive
    implementation("com.google.api-client:google-api-client-android:1.23.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
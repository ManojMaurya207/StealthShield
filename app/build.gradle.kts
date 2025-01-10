plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("com.google.gms.google-services")

    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}
buildscript {
    dependencies {
        classpath("com.google.android.libraries.mapsplatform.secrets-gradle-plugin:secrets-gradle-plugin:2.0.1")
    }
}
android {
//    //FOR SMS
//    packagingOptions {
//        exclude("META-INF/DEPENDENCIES")
//    }

    namespace = "com.example.stealthshield"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.stealthshield"
        minSdk = 25
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

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
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
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
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.auth)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.places)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.play.services.auth)
    implementation(libs.firebase.firestore.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.timber)
    implementation(libs.material3) // Material3 components
    implementation(libs.ui)
    implementation(libs.ui.tooling.preview)
    implementation(libs.places.v250)
    implementation("com.google.accompanist:accompanist-permissions:0.32.0")
    implementation("com.google.firebase:firebase-database:21.0.0")
    implementation("me.saket.swipe:swipe:1.3.0")
    implementation("com.google.maps.android:android-maps-utils:2.3.0")
    implementation("com.google.maps.android:maps-compose:2.11.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("com.google.android.gms:play-services-maps:19.0.0")
    implementation("com.google.accompanist:accompanist-navigation-animation:0.36.0")
    implementation("com.google.android.gms:play-services-auth:21.0.1")
    implementation("com.google.code.gson:gson:2.8.8")
    implementation("io.coil-kt:coil-compose:2.2.2")

    implementation(libs.accompanist.systemuicontroller)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.hilt.android)
    implementation(libs.timber)
    ksp(libs.hilt.android.compiler)
    ksp(libs.androidx.hilt.compiler)


    // For CameraX
    implementation("androidx.camera:camera-core:1.3.4")
    implementation("androidx.camera:camera-camera2:1.3.4")
    implementation("androidx.camera:camera-lifecycle:1.3.4")
    implementation("androidx.camera:camera-view:1.0.0-alpha32")
    implementation("androidx.camera:camera-extensions:1.0.0-alpha32")

    // Firebase Storage
    implementation("com.google.firebase:firebase-storage-ktx:20.1.0")
    implementation(platform("com.google.firebase:firebase-bom:32.0.0"))


    //For animation
    implementation("com.airbnb.android:lottie-compose:6.0.0")

    implementation("com.google.accompanist:accompanist-pager:0.28.0") // or the latest version
    implementation("com.google.accompanist:accompanist-pager-indicators:0.28.0")
}

//    implementation("androidx.compose.material:material-icons-extended:1.9.1")
//    implementation("com.canopas.compose-animated-navigationbar:bottombar:1.0.1")

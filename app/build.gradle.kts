plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.namah.feedwithlove"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.namah.feedwithlove"
        minSdk = 24
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

    sourceSets {
        getByName("main") {
            res.srcDirs(
                "src/main/res/layouts/donor",
                "src/main/res/layouts/volunteer",
                "src/main/res/layouts/receiver",
                "src/main/res/layouts",
                "src/main/res"
            )
        }
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.database)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
    implementation("com.amazonaws:aws-android-sdk-core:2.81.1")
    implementation("com.amazonaws:aws-android-sdk-cognitoidentityprovider:2.81.1")
    implementation("com.amazonaws:aws-android-sdk-mobile-client:2.74.0")
    implementation("com.amazonaws:aws-android-sdk-s3:2.81.1")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("androidx.viewpager2:viewpager2:1.1.0")
    implementation("com.ncorti:slidetoact:0.11.0")
    implementation("com.github.ibrahimsn98:SmoothBottomBar:1.7.9")
    implementation("androidx.biometric:biometric:1.2.0-alpha05")
    implementation("com.squareup.picasso:picasso:2.8")
}
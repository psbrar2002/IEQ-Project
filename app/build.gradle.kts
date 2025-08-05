plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("kotlin-parcelize")

}

android {
    namespace = "com.tst.ieqproject"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.tst.ieqproject"
        minSdk = 35
        targetSdk = 35
        versionCode = 12
        versionName = "2.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    signingConfigs {
        create("release") {
            storeFile = file("C:/Users/PSB/Documents/mykeystore/keystore1.jks")
            storePassword = "Brar1234"
            keyAlias = "key0"
            keyPassword = "Brar1234"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
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
    implementation ("com.squareup.okhttp3:okhttp:4.12.0")
    implementation ("com.google.code.gson:gson:2.12.1")
    implementation ("org.apache.commons:commons-csv:1.13.0")
    implementation ("org.osmdroid:osmdroid-android:6.1.20")
    implementation ("com.google.android.material:material:1.13.0-alpha10")
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")
    implementation ("androidx.cardview:cardview:1.0.0")
    implementation ("com.google.android.material:material:1.9.0")
    implementation("com.google.firebase:firebase-database:21.0.0")
    implementation("com.google.firebase:firebase-firestore-ktx:25.1.2")
    implementation ("com.google.android.material:material:1.12.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias (libs.plugins.kotlinSerialization)
}

android {
    namespace = "com.chukwuma.MOFI"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.chukwuma.MOFI"
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
    implementation ("com.google.mlkit:barcode-scanning:17.0.2")
    implementation(libs.androidx.constraintlayout)
//    implementation(libs.qr.scanner)
    implementation ("com.journeyapps:zxing-android-embedded:4.3.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation ("io.ktor:ktor-client-core:2.3.12")
    implementation ("io.ktor:ktor-client-android:2.3.12")
    implementation ("io.ktor:ktor-client-serialization:2.3.12")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.12")
    implementation ("io.ktor:ktor-client-logging:2.3.12")
    implementation ("ch.qos.logback:logback-classic:1.2.3")
    implementation ("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.12")
}
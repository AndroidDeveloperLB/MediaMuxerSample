@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.lb.mediamuxersample"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.lb.mediamuxersample"
        minSdk = 26
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
//    kotlin {
//        jvmToolchain(17)
//    }
}

dependencies {

    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.androidx.media3.transformer)

    //    https://github.com/bumptech/glide https://github.com/zjupure/GlideWebpDecoder
    val GLIDE_VERSION = "4.16.0"
    implementation ("com.github.zjupure:webpdecoder:2.6.${GLIDE_VERSION}")
    implementation ("com.github.bumptech.glide:glide:${GLIDE_VERSION}")
    ksp ("com.github.bumptech.glide:ksp:${GLIDE_VERSION}")
//    https://github.com/airbnb/lottie-android
    implementation("com.airbnb.android:lottie:6.4.0")
//    implementation ("com.github.israel-fl:bitmap2video:2.0.0")


}

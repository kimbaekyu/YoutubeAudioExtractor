plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.youtubeaudioextractor"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.youtubeaudioextractor"
        minSdk = 24
        targetSdk = 33 // Scoped Storage 정책 완화를 위해 34에서 33으로 조정하거나 29 유지 고려
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters += listOf("x86", "x86_64", "armeabi-v7a", "arm64-v8a")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            optimization {
                enable = false
            }
        }
        debug {
            applicationIdSuffix = ".debug"
        }
    }

    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    // YoutubeDL 라이브러리 및 FFmpeg(음원 변환용) 추가
    implementation("io.github.junkfood02.youtubedl-android:library:0.18.1")
    implementation("io.github.junkfood02.youtubedl-android:ffmpeg:0.18.1")
    // 백그라운드 작업을 위한 코루틴
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}
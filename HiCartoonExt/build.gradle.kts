plugins {
    id("com.android.library")
    kotlin("android")
    id("com.lagradost.cloudstream3.gradle")
}

cloudstream {
    // This allows the compiled plugin to specify its repository source
    setRepo("https://raw.githubusercontent.com/OWNER/REPO/builds")
}

android {
    namespace = "com.hicartoon"
    compileSdk = 33
    defaultConfig {
        minSdk = 21
        targetSdk = 33
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
    implementation(kotlin("stdlib"))
    implementation("com.github.recloudstream:cloudstream:-SNAPSHOT")
    implementation("org.jsoup:jsoup:1.15.3")
}

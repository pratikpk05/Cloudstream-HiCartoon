plugins {
    id("com.android.library")
    kotlin("android")
    id("com.lagradost.cloudstream3.gradle")
}

cloudstream {
    setRepo("pratikpk05", "Cloudstream-HiCartoon", "github")
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
    implementation("com.github.recloudstream:cloudstream:pre-release")
    implementation("org.jsoup:jsoup:1.15.3")
}

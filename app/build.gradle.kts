import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

fun runGitCommand(vararg args: String): String {
    return try {
        val process = ProcessBuilder("git", *args)
            .redirectErrorStream(true)
            .start()
        process.inputStream.bufferedReader().use { it.readText().trim() }
    } catch (e: Exception) {
        ""
    }
}

fun gitCommitHash(): String =
    runGitCommand("rev-parse", "--short=8", "HEAD")
        .ifBlank { "nogit" }

fun gitCommitEpoch(): String =
    runGitCommand("log", "-1", "--format=%ct")
        .ifBlank { "0" }

val gitVersionName = "${gitCommitEpoch()}-${gitCommitHash()}"



val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}


android {
    namespace = "com.pl.myweightapp"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.pl.myweightapp"
        minSdk = 26
        targetSdk = 36
        //versionCode = 1
        //versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        versionName = gitCommitHash()
        versionCode = gitCommitEpoch()
            .toLongOrNull()
            ?.coerceAtMost(Int.MAX_VALUE.toLong())
            ?.toInt()
            ?: 1
        buildConfigField("String", "APP_VERSION_FULL", "\"$gitVersionName\"")
    }

    buildTypes {
        debug {
            buildConfigField("String", "BASE_URL", "\"https://rest.my-api.io/v3/\"")
            buildConfigField("String", "API_KEY", "\"${localProperties.getProperty("API_KEY", "")}\"")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "BASE_URL", "\"https://rest.my-api.io/v3/\"")
            buildConfigField("String", "API_KEY", "\"${localProperties.getProperty("API_KEY", "")}\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.bundles.ktor)
    implementation(libs.mpandroidchart)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.datastore.preferences)
    // Hilt core
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
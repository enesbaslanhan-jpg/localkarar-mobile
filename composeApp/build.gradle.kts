import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }
    
    sourceSets {
        
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.ktor.client.android)
            implementation(libs.androidx.security.crypto)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.viewmodel.compose)
            implementation(libs.androidx.lifecycle.runtime.compose)

            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.auth)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.multiplatform.markdown.renderer)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}

android {
    namespace = "com.localkarar.app"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        applicationId = "com.localkarar.app"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = libs.versions.app.versionCode.get().toInt()
        versionName = libs.versions.app.versionName.get()
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    /*
     * IMZALAMA.
     *
     * Onceki halinde signingConfigs blogu HIC YOKTU: `assembleRelease` imzasiz
     * cikti uretiyordu ve magazaya yuklenemezdi.
     *
     * Keystore ve parolalar DEPOYA GIRMIYOR, ortam degiskeninden okunuyor.
     * Degiskenler yoksa imzalama yapilandirmasi hic kurulmuyor: yerelde
     * `assembleRelease` (imzasiz) calismaya devam ediyor, CI'da degiskenler
     * verilerek imzali cikti aliniyor. Boylece "keystore yok" hatasi
     * gelistirici makinesinde derlemeyi durdurmuyor.
     */
    val keystoreYolu = System.getenv("LK_KEYSTORE_PATH")
    val keystoreParolasi = System.getenv("LK_KEYSTORE_PASSWORD")
    val anahtarAdi = System.getenv("LK_KEY_ALIAS")
    val anahtarParolasi = System.getenv("LK_KEY_PASSWORD")
    val imzalanabilir = !keystoreYolu.isNullOrBlank() &&
        !keystoreParolasi.isNullOrBlank() &&
        !anahtarAdi.isNullOrBlank() &&
        !anahtarParolasi.isNullOrBlank()

    if (imzalanabilir) {
        signingConfigs {
            create("release") {
                storeFile = file(keystoreYolu!!)
                storePassword = keystoreParolasi
                keyAlias = anahtarAdi
                keyPassword = anahtarParolasi
            }
        }
    }

    buildTypes {
        getByName("debug") {
            buildConfigField("Boolean", "IS_RELEASE", "false")
        }
        getByName("release") {
            /*
             * R8 ACILDI.
             *
             * Kapaliyken release APK'si tamamen okunabilir kaliyordu: sinif ve
             * yontem adlari, ic string'ler, sozlesme yapisinin tamami acikta.
             *
             * ⚠️ RISK: kotlinx.serialization'in @Serializable sinif ve alan
             * adlari R8 tarafindan degistirilirse DTO'lar SESSIZCE bozulur --
             * debug'da calisip release'de bozulan tipik hata budur. Bunu
             * onleyen kurallar proguard-rules.pro icinde; oradaki -keep
             * satirlari SILINMEMELI.
             */
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (imzalanabilir) {
                signingConfig = signingConfigs.getByName("release")
            }
            buildConfigField("Boolean", "IS_RELEASE", "true")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    dependencies {
        debugImplementation(compose.uiTooling)
    }
}

plugins {
    id("com.android.application")
    id("kotlin-android")
}

val versionMajor = 1
val versionMinor = 1
val versionPatch = 0

android {
    compileSdk = 30
    buildToolsVersion = "30.0.3"

    defaultConfig {
        applicationId = "com.wndenis.snipsnap"
        minSdk = 23
        targetSdk = 30
        versionCode = versionMajor * 10000 + versionMinor * 100 + versionPatch
        versionName = "$versionMajor.$versionMinor.$versionPatch"
        the<BasePluginConvention>().archivesBaseName = "${rootProject.name}.v$versionName"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
        useIR = true
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = rootProject.extra["compose_version"] as String
        kotlinCompilerVersion = "1.4.32"
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.5.0")
    implementation("androidx.appcompat:appcompat:1.3.0")
    implementation("com.google.android.material:material:1.3.0")
    implementation("androidx.compose.ui:ui:${rootProject.extra["compose_version"]}")
    implementation("androidx.compose.material:material:${rootProject.extra["compose_version"]}")
    implementation("androidx.compose.ui:ui-tooling:${rootProject.extra["compose_version"]}")
    implementation(
        "androidx.compose.material:material-icons-extended:${rootProject.extra["compose_version"]}"
    )
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.3.1")
    implementation("androidx.activity:activity-compose:1.3.0-alpha08")

    implementation("com.google.accompanist:accompanist-pager:0.10.0")
    implementation("com.google.firebase:protolite-well-known-types:18.0.0")
    implementation("com.google.accompanist:accompanist-swiperefresh:0.10.0")
    implementation("com.google.accompanist:accompanist-glide:0.10.0")

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:1.1.5")
    implementation("io.github.vanpra.compose-material-dialogs:core:0.4.1")
    implementation("io.github.vanpra.compose-material-dialogs:datetime:0.4.1")
    implementation("io.github.vanpra.compose-material-dialogs:color:0.4.1")
    implementation("com.google.code.gson:gson:2.8.7")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.3.0")
    androidTestImplementation(
        "androidx.compose.ui:ui-test-junit4:${rootProject.extra["compose_version"]}"
    )
}

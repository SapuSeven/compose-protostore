import com.vanniktech.maven.publish.AndroidSingleVariantLibrary
import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.maven.publish)
}

mavenPublishing {
    coordinates("com.sapuseven.compose", "protostore", "0.1.2")

    configure(AndroidSingleVariantLibrary(
        variant = "release",
        sourcesJar = true,
        publishJavadocJar = true,
    ))

    pom {
        name.set("Compose ProtoStore")
        description.set("A compose library for storing typed objects with Proto DataStore and building a matching UI.")
        inceptionYear.set("2025")
        url.set("https://github.com/SapuSeven/compose-protostore")
        licenses {
            license {
                name.set("The MIT License")
                url.set("https://opensource.org/licenses/MIT")
            }
        }
        developers {
            developer {
                id.set("sapuseven")
                name.set("SapuSeven")
                url.set("https://sapuseven.com")
            }
        }
        scm {
            url.set("https://github.com/SapuSeven/compose-protostore")
            connection.set("scm:git:git://github.com/SapuSeven/compose-protostore.git")
            developerConnection.set("scm:git:ssh://git@github.com/SapuSeven/compose-protostore.git")
        }
    }

    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

    if (!gradle.startParameter.taskNames.any { it.contains("publishToMavenLocal") }) {
        signAllPublications()
    }
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/SapuSeven/compose-protostore")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
            }
        }
    }
}

android {
    namespace = "com.sapuseven.compose.protostore"
    compileSdk = 35

    defaultConfig {
        minSdk = 21

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)

    // Compose and Material3
    implementation(libs.androidx.ui)
    implementation(libs.androidx.material3)

    // DataStore and Protobuf
    implementation(libs.androidx.datastore)
    implementation(libs.protobuf.javalite)

    // Utils
    implementation(libs.colormath)
    implementation(libs.androidx.lifecycle.viewmodel.android)
    implementation(libs.androidx.ui.tooling.preview.android)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

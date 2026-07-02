/*
 * Copyright 2026 WuJia(Linxu_Link)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import java.util.Properties

plugins {
    alias(libs.plugins.advance.android.application)
    alias(libs.plugins.advance.android.application.compose)
    alias(libs.plugins.advance.android.application.flavors)
    alias(libs.plugins.advance.android.application.jacoco)
    alias(libs.plugins.advance.android.application.firebase)
    alias(libs.plugins.advance.hilt)
    alias(libs.plugins.baselineprofile)
    alias(libs.plugins.roborazzi)
    alias(libs.plugins.kotlin.serialization)
}

val localProperties = Properties().apply {
    rootProject.file("local.properties")
        .takeIf { it.exists() }
        ?.inputStream()
        ?.use(::load)
}

fun signingProperty(name: String): String? {
    return providers.gradleProperty(name).orNull ?: localProperties.getProperty(name)
}

fun adProperty(name: String, fallback: String): String {
    return providers.gradleProperty(name).orNull
        ?: localProperties.getProperty(name)
        ?: fallback
}

fun requiredAdProperty(name: String): String {
    return providers.gradleProperty(name).orNull
        ?: localProperties.getProperty(name)
        ?: error("Missing required release ad property: $name")
}

val googleMobileAdsTestAppId = "ca-app-pub-3940256099942544~3347511713"
val googleAppOpenTestAdUnitId = "ca-app-pub-3940256099942544/9257395921"
val releaseGoogleMobileAdsAppId = requiredAdProperty("velaris.ads.release.appId")
val releaseAppOpenAdUnitId = requiredAdProperty("velaris.ads.release.appOpenAdUnitId")

android {
    defaultConfig {
        applicationId = "com.wujia.velaris"
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["googleMobileAdsApplicationId"] = googleMobileAdsTestAppId
        manifestPlaceholders["appOpenAdUnitId"] = googleAppOpenTestAdUnitId
    }

    productFlavors {
        named("demo") {
            applicationIdSuffix = ""
        }
        named("prod") {
            applicationIdSuffix = ""
        }
    }

    signingConfigs {
        create("release") {
            val storeFilePath = signingProperty("velaris.release.storeFile")
            if (!storeFilePath.isNullOrBlank()) {
                storeFile = rootProject.file(storeFilePath)
                storePassword = signingProperty("velaris.release.storePassword")
                keyAlias = signingProperty("velaris.release.keyAlias")
                keyPassword = signingProperty("velaris.release.keyPassword")
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            manifestPlaceholders["googleMobileAdsApplicationId"] = googleMobileAdsTestAppId
            manifestPlaceholders["appOpenAdUnitId"] = googleAppOpenTestAdUnitId
        }
        release {
            isMinifyEnabled = providers.gradleProperty("minifyWithR8")
                .map(String::toBooleanStrict).getOrElse(true)
            applicationIdSuffix = ".release"
            manifestPlaceholders["googleMobileAdsApplicationId"] = releaseGoogleMobileAdsAppId
            manifestPlaceholders["appOpenAdUnitId"] = releaseAppOpenAdUnitId
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.named("release").get()
        }
    }

    buildFeatures {
        buildConfig = true
    }

    packaging {
        resources {
            excludes.add("/META-INF/{AL2.0,LGPL2.1}")
        }
    }

    lint {
        disable += "ResourceName"
    }

    testOptions.unitTests.isIncludeAndroidResources = true
    namespace = "com.wujia.velaris"
}

dependencies {
    implementation(projects.feature.scene.impl)

    implementation(projects.foundation.ads)
    implementation(projects.foundation.data)
    implementation(projects.foundation.designsystem)
    implementation(projects.foundation.domain)
    implementation(projects.foundation.model)
    implementation(projects.foundation.navigation)
    implementation(projects.foundation.toolkit)
    implementation(projects.sync.work)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.compose.material.iconsExtended)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtimeCompose)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.protobuf.javalite)
    implementation(libs.timber)
}

baselineProfile {
    automaticGenerationDuringBuild = false
}

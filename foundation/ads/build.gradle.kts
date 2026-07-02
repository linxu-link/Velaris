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
    alias(libs.plugins.advance.android.library)
    alias(libs.plugins.advance.android.library.jacoco)
    alias(libs.plugins.advance.hilt)
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use(::load)
    }
}

fun localBooleanProperty(name: String, default: Boolean): Boolean =
    localProperties.getProperty(name)?.toBooleanStrictOrNull() ?: default

android {
    namespace = "com.wujia.foundation.ads"

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        buildConfigField("boolean", "ADS_ENABLED", "true")
        buildConfigField("boolean", "ADS_DEBUG", "false")
        buildConfigField("boolean", "ADS_SKIP_CONSENT_FLOW_IN_DEBUG", "false")
        buildConfigField("String", "ADS_TEST_DEVICE_HASHED_IDS", "\"\"")
        buildConfigField("String", "ADS_DEBUG_GEOGRAPHY", "\"DISABLED\"")
        buildConfigField(
            "String",
            "ADS_APP_OPEN_AD_UNIT_ID",
            "\"ca-app-pub-3940256099942544/9257395921\"",
        )
        buildConfigField(
            "long",
            "ADS_APP_OPEN_SHOW_TIMEOUT_MILLIS",
            providers.gradleProperty("velaris.appOpenShowTimeoutMillis").orElse("3000").get(),
        )
        buildConfigField(
            "long",
            "ADS_APP_OPEN_PRELOAD_TIMEOUT_MILLIS",
            providers.gradleProperty("velaris.appOpenPreloadTimeoutMillis").orElse("5000").get(),
        )
    }

    buildTypes {
        debug {
            buildConfigField(
                "boolean",
                "ADS_DEBUG",
                providers.gradleProperty("velaris.adsDebug").orElse("true").get(),
            )
            buildConfigField(
                "boolean",
                "ADS_SKIP_CONSENT_FLOW_IN_DEBUG",
                localBooleanProperty("velaris.adsSkipConsentFlowInDebug", true).toString(),
            )
            buildConfigField(
                "String",
                "ADS_TEST_DEVICE_HASHED_IDS",
                "\"${localProperties.getProperty("velaris.adsTestDeviceHashedIds", "")}\"",
            )
            buildConfigField(
                "String",
                "ADS_DEBUG_GEOGRAPHY",
                "\"${localProperties.getProperty("velaris.adsDebugGeography", "DISABLED")}\"",
            )
        }
        release {
            buildConfigField("boolean", "ADS_DEBUG", "false")
            buildConfigField("boolean", "ADS_SKIP_CONSENT_FLOW_IN_DEBUG", "false")
            buildConfigField("String", "ADS_TEST_DEVICE_HASHED_IDS", "\"\"")
            buildConfigField("String", "ADS_DEBUG_GEOGRAPHY", "\"DISABLED\"")
        }
    }
}

dependencies {
    implementation(projects.foundation.toolkit)

    implementation(libs.androidx.core.ktx)
    implementation(libs.google.play.services.ads)
    implementation(libs.google.user.messaging.platform)
    implementation(libs.javax.inject)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.timber)

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.robolectric)
}

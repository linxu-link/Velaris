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
plugins {
    alias(libs.plugins.advance.android.feature.impl)
    alias(libs.plugins.advance.android.library.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.wujia.feature.scene.impl"
}

dependencies {
    api(projects.feature.scene.api)
    implementation(projects.feature.sceneControl.impl)
    implementation(projects.feature.sceneEdit.impl)
    implementation(projects.feature.sceneList.impl)
    implementation(projects.feature.settings.impl)

    implementation(projects.foundation.navigation)
    implementation(projects.foundation.ui)
    implementation(projects.foundation.toolkit)
    implementation(projects.foundation.model)
    implementation(projects.foundation.domain)
    implementation(projects.foundation.player)
    implementation(projects.foundation.alarm)
    implementation(projects.foundation.particle)

    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.compose.material3.adaptive)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.iconsExtended)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.timber)
    testImplementation(projects.foundation.testing)
}

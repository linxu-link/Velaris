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
    alias(libs.plugins.advance.android.library)
    alias(libs.plugins.advance.android.library.jacoco)
    alias(libs.plugins.advance.android.room)
    alias(libs.plugins.advance.hilt)
}

android {
    namespace = "com.wujia.foundation.data"
}

dependencies {
    api(projects.foundation.model)
    implementation(projects.foundation.database)
    implementation(projects.foundation.player)
    implementation(projects.foundation.toolkit)
    implementation(projects.foundation.ui)
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.timber)
    testImplementation(libs.kotlinx.coroutines.test)
}

plugins {
    alias(libs.plugins.advance.android.feature.impl)
    alias(libs.plugins.advance.android.library.compose)
}
android {
    namespace = "com.wujia.feature.lock.impl"
}

dependencies {
    api(projects.feature.lock.api)
    implementation(projects.foundation.designsystem)
    implementation(projects.foundation.model)
    implementation(projects.foundation.domain)
    implementation(projects.foundation.player)
    implementation(projects.foundation.toolkit)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.compose.material.iconsExtended)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.lifecycle.runtimeCompose)
    implementation(libs.kotlinx.datetime)
}

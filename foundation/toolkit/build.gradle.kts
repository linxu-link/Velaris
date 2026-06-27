plugins {
    alias(libs.plugins.advance.android.library.compose)
    alias(libs.plugins.advance.hilt)
}

android {
    namespace = "com.wujia.foundation.toolkit"

}

dependencies {
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.timber)
}

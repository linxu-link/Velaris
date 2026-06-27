package com.wujia.foundation.toolkit.device

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings
import com.wujia.foundation.toolkit.app.AppContext

/**
 * 设备信息工具类
 *
 * 提供设备硬件信息获取，包括 AndroidId、SDK 版本、品牌型号、
 * 模拟器检测、平板判断、CPU 架构等。
 * 默认使用 [AppContext.app] 作为 Context。
 */
object DeviceUtils {

    /**
     * 获取设备 AndroidId
     *
     * 注意：AndroidId 在恢复出厂设置后可能改变，且某些厂商设备可能返回相同值
     *
     * @param context 上下文，默认使用 AppContext
     * @return AndroidId 字符串
     */
    fun getAndroidId(context: Context = AppContext.app): String {
        return Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        ) ?: ""
    }

    /**
     * 获取设备 SDK 版本号
     *
     * @return SDK 版本号，如 33 (Android 13)
     */
    fun getSdkVersion(): Int {
        return Build.VERSION.SDK_INT
    }

    /**
     * 获取设备 SDK 版本名称
     *
     * @return SDK 版本名称，如 "13"、"14"
     */
    fun getSdkVersionName(): String {
        return Build.VERSION.RELEASE
    }

    /**
     * 获取设备品牌
     *
     * @return 品牌名称，如 "Xiaomi"、"HUAWEI"、"samsung"
     */
    fun getDeviceBrand(): String {
        return Build.BRAND ?: ""
    }

    /**
     * 获取设备型号
     *
     * @return 设备型号，如 "MI 10"、"SM-G9910"
     */
    fun getDeviceModel(): String {
        return Build.MODEL ?: ""
    }

    /**
     * 获取设备制造商
     *
     * @return 制造商名称，如 "Xiaomi"、"HUAWEI"
     */
    fun getDeviceManufacturer(): String {
        return Build.MANUFACTURER ?: ""
    }

    /**
     * 获取设备主板型号
     *
     * @return 主板型号字符串
     */
    fun getDeviceBoard(): String {
        return Build.BOARD ?: ""
    }

    /**
     * 获取设备硬件信息
     *
     * @return 硬件信息字符串，如 "qcom"、"mt6789"
     */
    fun getDeviceHardware(): String {
        return Build.HARDWARE ?: ""
    }

    /**
     * 获取设备指纹信息
     *
     * @return 指纹字符串，包含品牌、型号、构建类型等综合信息
     */
    fun getDeviceFingerprint(): String {
        return Build.FINGERPRINT ?: ""
    }

    /**
     * 获取设备 Display 构建信息
     *
     * @return Display 构建字符串
     */
    fun getDeviceDisplay(): String {
        return Build.DISPLAY ?: ""
    }

    /**
     * 获取构建 ID
     *
     * @return 构建 ID 字符串
     */
    fun getBuildId(): String {
        return Build.ID ?: ""
    }

    /**
     * 获取设备支持的 CPU 架构列表
     *
     * @return CPU 架构数组，按优先级排序，如 ["arm64-v8a", "armeabi-v7a", "armeabi"]
     */
    fun getAbis(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Build.SUPPORTED_ABIS
        } else {
            @Suppress("DEPRECATION")
            arrayOf(Build.CPU_ABI, Build.CPU_ABI2)
        }
    }

    /**
     * 判断当前设备是否为模拟器
     *
     * 通过检测 Build 中的多个特征值来判断，匹配超过 3 个特征则判定为模拟器
     *
     * @return 模拟器返回 true，否则返回 false
     */
    fun isEmulator(): Boolean {
        val flags = listOf(
            Build.FINGERPRINT.startsWith("generic"),
            Build.FINGERPRINT.startsWith("unknown"),
            Build.MODEL.contains("google_sdk"),
            Build.MODEL.contains("Emulator"),
            Build.MODEL.contains("Android SDK built for x86"),
            Build.MANUFACTURER.contains("Genymotion"),
            Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"),
            "google_sdk" == Build.PRODUCT,
            Build.HARDWARE.contains("goldfish"),
            Build.HARDWARE.contains("ranchu"),
            Build.PRODUCT.contains("sdk"),
            Build.PRODUCT.contains("sdk_google"),
            Build.PRODUCT.contains("sdk_x86"),
            Build.PRODUCT.contains("vbox86p"),
            Build.PRODUCT.contains("emulator"),
            Build.PRODUCT.contains("simulator")
        )
        return flags.count { it } > 3
    }

    /**
     * 判断当前设备是否为平板
     *
     * 通过屏幕尺寸判断，SCREENLAYOUT_SIZE_LARGE 及以上视为平板
     *
     * @param context 上下文，默认使用 AppContext
     * @return 平板返回 true，否则返回 false
     */
    fun isTablet(context: Context = AppContext.app): Boolean {
        return (context.resources.configuration.screenLayout
                and android.content.res.Configuration.SCREENLAYOUT_SIZE_MASK) >=
                android.content.res.Configuration.SCREENLAYOUT_SIZE_LARGE
    }

    /**
     * 获取设备序列号
     *
     * 注意：Android O 及以上需要 READ_PHONE_STATE 或 READ_PRIVILEGED_PHONE_STATE 权限
     *
     * @return 序列号字符串
     */
    @SuppressLint("HardwareIds")
    fun getSerialNumber(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                Build.getSerial()
            } catch (e: Exception) {
                Build.SERIAL ?: ""
            }
        } else {
            @Suppress("DEPRECATION")
            Build.SERIAL ?: ""
        }
    }
}
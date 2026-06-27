package com.wujia.foundation.toolkit.intent

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import com.wujia.foundation.toolkit.app.AppContext

/**
 * Intent 工具类
 *
 * 提供常用系统 Intent 的快捷构建，包括应用安装卸载、
 * 拨号短信、浏览器、邮件、分享、拍照、系统设置页等跳转。
 * 默认使用 [AppContext.app] 作为 Context。
 */
object IntentUtils {

    /**
     * 获取启动指定应用的主界面 Intent
     *
     * @param packageName 目标应用包名
     * @param context     上下文，默认使用 AppContext
     * @return 启动 Intent，应用不存在时返回 null
     */
    fun getLaunchAppIntent(packageName: String, context: Context = AppContext.app): Intent? {
        return context.packageManager.getLaunchIntentForPackage(packageName)
    }

    /**
     * 获取安装应用的 Intent
     *
     * @param filePath APK 文件路径
     * @param context  上下文，默认使用 AppContext
     * @return 安装 Intent，Android N 及以上使用 FileProvider
     */
    fun getInstallAppIntent(filePath: String, context: Context = AppContext.app): Intent {
        val file = java.io.File(filePath)
        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } else {
            Uri.fromFile(file)
        }
        return Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    /**
     * 获取卸载应用的 Intent
     *
     * @param packageName 要卸载的应用包名
     * @return 卸载 Intent
     */
    fun getUninstallAppIntent(packageName: String): Intent {
        return Intent(Intent.ACTION_DELETE, Uri.parse("package:$packageName"))
    }

    /**
     * 获取拨号 Intent（只打开拨号界面，不直接拨打）
     *
     * @param phoneNumber 电话号码
     * @return 拨号 Intent
     */
    fun getDialIntent(phoneNumber: String): Intent {
        return Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
    }

    /**
     * 获取直接拨打电话的 Intent
     *
     * 注意：需要 android.permission.CALL_PHONE 权限
     *
     * @param phoneNumber 电话号码
     * @return 拨打 Intent
     */
    fun getCallIntent(phoneNumber: String): Intent {
        return Intent(Intent.ACTION_CALL, Uri.parse("tel:$phoneNumber"))
    }

    /**
     * 获取发送短信的 Intent
     *
     * @param phoneNumber 收信人号码
     * @param content     短信内容，默认为空
     * @return 发送短信 Intent
     */
    fun getSendSmsIntent(phoneNumber: String, content: String = ""): Intent {
        return Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$phoneNumber")).apply {
            putExtra("sms_body", content)
        }
    }

    /**
     * 获取打开浏览器的 Intent，自动补全 https 前缀
     *
     * @param url 网址
     * @return 浏览器 Intent
     */
    fun getBrowserIntent(url: String): Intent {
        var finalUrl = url
        if (!finalUrl.startsWith("http://") && !finalUrl.startsWith("https://")) {
            finalUrl = "https://$finalUrl"
        }
        return Intent(Intent.ACTION_VIEW, Uri.parse(finalUrl))
    }

    /**
     * 获取发送邮件的 Intent
     *
     * @param email   收件人邮箱
     * @param subject 邮件主题，默认为空
     * @param text    邮件正文，默认为空
     * @return 发送邮件 Intent
     */
    fun getEmailIntent(email: String, subject: String = "", text: String = ""): Intent {
        return Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email")).apply {
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, text)
        }
    }

    /**
     * 获取分享文本的 Intent
     *
     * @param text 要分享的文本内容
     * @return 分享 Intent
     */
    fun getShareTextIntent(text: String): Intent {
        return Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
    }

    /**
     * 获取分享图片的 Intent
     *
     * @param imageUri 图片 URI
     * @return 分享 Intent
     */
    fun getShareImageIntent(imageUri: Uri): Intent {
        return Intent(Intent.ACTION_SEND).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, imageUri)
        }
    }

    /**
     * 获取拍照 Intent
     *
     * @param outputUri 拍照后图片保存的 URI
     * @return 拍照 Intent
     */
    fun getCaptureIntent(outputUri: Uri): Intent {
        return Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, outputUri)
        }
    }

    /**
     * 获取从相册选取图片的 Intent
     *
     * @return 选取图片 Intent
     */
    fun getPickImageIntent(): Intent {
        return Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
    }

    /**
     * 获取应用详情设置页 Intent
     *
     * @param packageName 应用包名
     * @return 应用设置页 Intent
     */
    fun getAppSettingsIntent(packageName: String): Intent {
        return Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName"))
    }

    /**
     * 获取 WiFi 设置页 Intent
     *
     * @return WiFi 设置页 Intent
     */
    fun getWifiSettingsIntent(): Intent {
        return Intent(Settings.ACTION_WIFI_SETTINGS)
    }

    /**
     * 获取蓝牙设置页 Intent
     *
     * @return 蓝牙设置页 Intent
     */
    fun getBluetoothSettingsIntent(): Intent {
        return Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
    }

    /**
     * 获取位置设置页 Intent
     *
     * @return 位置设置页 Intent
     */
    fun getLocationSettingsIntent(): Intent {
        return Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
    }

    /**
     * 获取显示设置页 Intent
     *
     * @return 显示设置页 Intent
     */
    fun getDisplaySettingsIntent(): Intent {
        return Intent(Settings.ACTION_DISPLAY_SETTINGS)
    }

    /**
     * 获取声音设置页 Intent
     *
     * @return 声音设置页 Intent
     */
    fun getSoundSettingsIntent(): Intent {
        return Intent(Settings.ACTION_SOUND_SETTINGS)
    }

    /**
     * 获取系统主设置页 Intent
     *
     * @return 系统设置页 Intent
     */
    fun getSystemSettingsIntent(): Intent {
        return Intent(Settings.ACTION_SETTINGS)
    }

    /**
     * 获取打开地图定位的 Intent
     *
     * @param lat   纬度
     * @param lng   经度
     * @param label 地点标签，默认为空
     * @return 地图 Intent
     */
    fun getMapIntent(lat: Double, lng: Double, label: String = ""): Intent {
        val uri = Uri.parse("geo:$lat,$lng?q=$lat,$lng($label)")
        return Intent(Intent.ACTION_VIEW, uri)
    }
}
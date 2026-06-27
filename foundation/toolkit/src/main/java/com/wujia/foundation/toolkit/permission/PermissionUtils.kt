package com.wujia.foundation.toolkit.permission

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

enum class PermissionGrantPolicy {
    All,
    Any,
}

data class PermissionRequest(
    val permissions: List<String>,
    val grantPolicy: PermissionGrantPolicy = PermissionGrantPolicy.All,
)

enum class VisualMediaPermissionType {
    Image,
    Video,
}

object PermissionUtils {
    fun request(
        vararg permissions: String,
        grantPolicy: PermissionGrantPolicy = PermissionGrantPolicy.All,
    ): PermissionRequest =
        PermissionRequest(
            permissions = permissions.toList(),
            grantPolicy = grantPolicy,
        )

    fun visualMediaRequest(type: VisualMediaPermissionType): PermissionRequest =
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> request(
                type.readMediaPermission,
                Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED,
                grantPolicy = PermissionGrantPolicy.Any,
            )

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> request(type.readMediaPermission)

            else -> request(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

    fun audioMediaRequest(): PermissionRequest =
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> request(Manifest.permission.READ_MEDIA_AUDIO)
            else -> request(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

    fun hasFullAudioMediaAccess(context: Context): Boolean =
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> hasPermission(
                context,
                Manifest.permission.READ_MEDIA_AUDIO,
            )

            else -> hasPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
        }

    fun hasAccess(
        context: Context,
        request: PermissionRequest,
    ): Boolean =
        when (request.grantPolicy) {
            PermissionGrantPolicy.All -> hasAllPermissions(context, request.permissions)
            PermissionGrantPolicy.Any -> hasAnyPermission(context, request.permissions)
        }

    fun hasPermission(
        context: Context,
        permission: String,
    ): Boolean =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    fun hasAllPermissions(
        context: Context,
        permissions: Collection<String>,
    ): Boolean =
        permissions.all { hasPermission(context, it) }

    fun hasAnyPermission(
        context: Context,
        permissions: Collection<String>,
    ): Boolean =
        permissions.any { hasPermission(context, it) }

    fun hasFullVisualMediaAccess(
        context: Context,
        type: VisualMediaPermissionType,
    ): Boolean =
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> hasPermission(context, type.readMediaPermission)
            else -> hasPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
        }

    private val VisualMediaPermissionType.readMediaPermission: String
        get() = when (this) {
            VisualMediaPermissionType.Image -> Manifest.permission.READ_MEDIA_IMAGES
            VisualMediaPermissionType.Video -> Manifest.permission.READ_MEDIA_VIDEO
        }
}

package org.b3log.siyuan

import android.Manifest
import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.PowerManager
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest
import kotlin.math.pow
import kotlin.math.sqrt


object Us {
    fun isMIUI(applicationContext : Context): Boolean {
        val packageManager = applicationContext.packageManager
        val miuiPackageName = "com.miui.gallery"
        return try {
            packageManager.getPackageInfo(miuiPackageName, PackageManager.GET_META_DATA)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun isLargeScreenMachine(context: Context): Boolean {
        // 获取屏幕的方向
        val screenLayout = context.resources.configuration.screenLayout
        // 获取屏幕尺寸的掩码
        val sizeMask = Configuration.SCREENLAYOUT_SIZE_MASK
        // 获取屏幕尺寸的值
        val screenSize = screenLayout and sizeMask

        // 如果屏幕尺寸是超大屏或者巨屏，则可能是平板电脑
        return screenSize == Configuration.SCREENLAYOUT_SIZE_XLARGE ||
                screenSize == Configuration.SCREENLAYOUT_SIZE_LARGE
    }
    fun isPad(context: Context): Boolean { // Converted from Utils.java
        val metrics = context.resources.displayMetrics
        val widthInches = metrics.widthPixels / metrics.xdpi
        val heightInches = metrics.heightPixels / metrics.ydpi
        val diagonalInches = sqrt(widthInches.toDouble().pow(2.0) + heightInches.toDouble()
            .pow(2.0)
        )
        return diagonalInches >= 7
    }

    fun isValidPermission(id: String?): Boolean { // Converted from Utils.java
        if (id.isNullOrEmpty()) {
            return false
        }
        try {
            // 使用反射获取 Manifest.permission 类中的所有静态字段
            val fields = Manifest.permission::class.java.getFields()
            for (field in fields) {
                // 检查是否存在与id匹配的静态字段
                if (field.type == String::class.java && field[null] == id) {
                    return false
                }
            }
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
            return false
        }
        return true
    }


    fun requestExternalStoragePermission(activity: Activity) {
        if (!canManageAllFiles(activity)) {
            val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
            ActivityCompat.startActivityForResult(
                activity,
                intent,
                S.REQUEST_CODE_MANAGE_STORAGE,
                null
            )
        }
    }

    fun canManageAllFiles(context: Context): Boolean { // 管理所有文件
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED &&
                context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED
        // On older versions, we assume that the READ_EXTERNAL_STORAGE and WRITE_EXTERNAL_STORAGE
        // permissions are sufficient to manage all files.
    }

    fun canAccessDeviceState(context: Context): Boolean { // 访问设备状态信息
        return context.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) ==
                PackageManager.PERMISSION_GRANTED
    }

    fun isIgnoringBatteryOptimizations(context: Context): Boolean { // 忽略电池优化
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager?.isIgnoringBatteryOptimizations(context.packageName) ?: false
    }

    fun isShowingOnLockScreen(context: Context): Boolean { // 锁屏显示
        val keyguardManager = context.getSystemService(
            KeyguardManager::class.java
        )
        return keyguardManager?.isDeviceLocked ?: false
    }


    fun canShowOnTop(context: Context?): Boolean { // 悬浮窗
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else true
        // Assuming it's allowed on older versions
    }

    fun canPopInBackground(context: Context?): Boolean { // 后台弹出界面
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Settings.canDrawOverlays(context)
        } else true
        // Assuming it's allowed on older versions
    }

    fun canRequestPackageInstalls(context: Context): Boolean { // 安装未知应用
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else true
        // Assuming it's allowed on older versions
    }

    fun getDirectoriesInPath(path: String): List<String> {
        val directories = mutableListOf<String>()
        val file = File(path)
        if (file.exists() && file.isDirectory) {
            val files = file.listFiles()
            if (files != null) {
                for (currentFile in files) {
                    if (currentFile.isDirectory) {
                        directories.add(currentFile.name)
                    }
                }
            }
        }
        return directories
    }

    fun filesHaveSameHash(file1: File, file2: File): Boolean {
        val digest1 = getFileHash(file1)
        val digest2 = getFileHash(file2)
        return digest1.contentEquals(digest2)
    }

    fun getFileHash(file: File): ByteArray {
        val digest = MessageDigest.getInstance("SHA-256")
        FileInputStream(file).use { inputStream ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        return digest.digest()
    }


    fun getFileMIMEType(mimeType: String): String {
        return when {
            mimeType.startsWith("video/") -> {
                when (mimeType) {
                    "video/mp4" -> "MP4 视频"
                    "video/mpeg" -> "MPEG 视频"
                    "video/quicktime" -> "QuickTime 视频"
                    "video/x-msvideo" -> "AVI 视频"
                    "video/x-flv" -> "FLV 视频"
                    "video/x-matroska" -> "Matroska 视频"
                    "video/webm" -> "WebM 视频"
                    else -> "其他视频"
                }
            }
            mimeType.startsWith("audio/") -> {
                when (mimeType) {
                    "audio/mpeg" -> "MP3 音频"
                    "audio/x-wav" -> "WAV 音频"
                    "audio/ogg" -> "OGG 音频"
                    "audio/aac" -> "AAC 音频"
                    "audio/flac" -> "FLAC 音频"
                    "audio/amr" -> "AMR 音频"
                    "audio/midi" -> "MIDI 音频"
                    "audio/x-ms-wma" -> "WMA 音频"
                    "audio/x-aiff" -> "AIFF 音频"
                    "audio/x-ms-wmv" -> "WMV 音频"
                    "audio/mp4" -> "M4A 音频"
                    else -> "其他音频"
                }
            }
            mimeType.startsWith("text/") -> {
                when (mimeType) {
                    "text/plain" -> "文本"
                    "text/html" -> "HTML"
                    "text/css" -> "CSS"
                    "text/javascript" -> "JavaScript"
                    else -> "其他文本"
                }
            }
            mimeType.startsWith("image/") -> {
                when (mimeType) {
                    "image/jpeg" -> "JPEG 图像"
                    "image/png" -> "PNG 图像"
                    "image/gif" -> "GIF 图像"
                    "image/bmp" -> "BMP 图像"
                    "image/webp" -> "WebP 图像"
                    "image/tiff" -> "TIFF 图像"
                    "image/tiff-fx" -> "TIFF-FX 图像"
                    else -> "其他图像"
                }
            }
            mimeType.startsWith("application/") -> {
                when (mimeType) {
                    "application/vnd.android.package-archive" -> "程序"
                    "application/pdf" -> "PDF"
                    "application/zip" -> "压缩文件"
                    "application/epub+zip" -> "EPUB"
                    "application/msword" -> "Word文档"
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> "Word文档"
                    "application/vnd.ms-excel" -> "Excel表格"
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" -> "Excel表格"
                    "application/vnd.ms-powerpoint" -> "PowerPoint演示文稿"
                    "application/vnd.openxmlformats-officedocument.presentationml.presentation" -> "PowerPoint演示文稿"

                    // 更多应用程序类型的判断
                    else -> "其他程序"
                }
            }
            // 其他类型
            else -> "其他"
        }
    }

    fun installApk(activity: Activity, apkFile: File) {
        val installIntent: Intent
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            // Android N及以上版本需要使用FileProvider安装APK
            val apkUri = FileProvider.getUriForFile(
                activity,
                "${activity.packageName}.fileprovider",
                apkFile
            )
            installIntent = Intent(Intent.ACTION_INSTALL_PACKAGE)
            installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            installIntent.setData(apkUri)
        } else {
            // Android N以下版本直接使用文件路径
            val apkUri = Uri.fromFile(apkFile)
            installIntent = Intent(Intent.ACTION_VIEW)
            installIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            installIntent.setDataAndType(apkUri, "application/vnd.android.package-archive")
        }

        activity.startActivity(installIntent)
    }
    fun installApk(activity: Activity, apkUri: Uri) {
        val installIntent: Intent

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            // Android N及以上版本需要额外权限
            installIntent = Intent(Intent.ACTION_INSTALL_PACKAGE)
            installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            installIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        } else {
            // Android N以下版本
            installIntent = Intent(Intent.ACTION_VIEW)
            installIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        installIntent.setDataAndType(apkUri, "application/vnd.android.package-archive")
        activity.startActivity(installIntent)
    }
}
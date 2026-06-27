package com.wujia.foundation.toolkit.file

import android.content.Context
import android.os.Environment
import com.wujia.foundation.toolkit.app.AppContext
import java.io.BufferedReader
import java.io.Closeable
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader

/**
 * 文件工具类
 *
 * 提供文件和目录的创建、删除、读写、复制、移动，
 * 文件大小计算与格式化，内外存储路径获取等功能。
 * 默认使用 [AppContext.app] 作为 Context。
 */
object FileUtils {

    /**
     * 根据路径获取 File 对象
     *
     * @param filePath 文件路径
     * @return File 对象，文件不存在时返回 null
     */
    fun getFileByPath(filePath: String): File? {
        val file = File(filePath)
        return if (file.exists()) file else null
    }

    /**
     * 判断文件是否存在
     *
     * @param filePath 文件路径
     * @return 存在返回 true，否则返回 false
     */
    fun isFileExists(filePath: String): Boolean {
        return File(filePath).exists()
    }

    /**
     * 判断路径是否为目录
     *
     * @param path 路径
     * @return 是目录返回 true，否则返回 false
     */
    fun isDir(path: String): Boolean {
        val file = File(path)
        return file.exists() && file.isDirectory
    }

    /**
     * 判断路径是否为文件
     *
     * @param path 路径
     * @return 是文件返回 true，否则返回 false
     */
    fun isFile(path: String): Boolean {
        val file = File(path)
        return file.exists() && file.isFile
    }

    /**
     * 创建目录，如果父目录不存在会一并创建
     *
     * @param dirPath 目录路径
     * @return 创建成功或目录已存在返回 true，否则返回 false
     */
    fun createDir(dirPath: String): Boolean {
        return File(dirPath).mkdirs()
    }

    /**
     * 创建文件，如果父目录不存在会一并创建
     *
     * @param filePath 文件路径
     * @return 创建成功或文件已存在返回 true，否则返回 false
     */
    fun createFile(filePath: String): Boolean {
        val file = File(filePath)
        if (file.exists()) return true
        val parentFile = file.parentFile ?: return false
        if (!parentFile.exists() && !parentFile.mkdirs()) return false
        return try {
            file.createNewFile()
        } catch (e: IOException) {
            false
        }
    }

    /**
     * 删除文件或目录，如果是目录会递归删除其中所有内容
     *
     * @param filePath 文件或目录路径
     * @return 删除成功返回 true，否则返回 false
     */
    fun deleteFile(filePath: String): Boolean {
        val file = File(filePath)
        if (!file.exists()) return false
        if (file.isDirectory) return deleteDir(filePath)
        return file.delete()
    }

    /**
     * 递归删除目录及其所有子内容
     *
     * @param dirPath 目录路径
     * @return 删除成功返回 true，否则返回 false
     */
    fun deleteDir(dirPath: String): Boolean {
        val dir = File(dirPath)
        if (!dir.exists() || !dir.isDirectory) return false
        val files = dir.listFiles() ?: return false
        for (file in files) {
            if (file.isFile) {
                if (!file.delete()) return false
            } else if (file.isDirectory) {
                if (!deleteDir(file.absolutePath)) return false
            }
        }
        return dir.delete()
    }

    /**
     * 获取文件大小
     *
     * @param filePath 文件路径
     * @return 文件大小（字节），文件不存在返回 0
     */
    fun getFileSize(filePath: String): Long {
        val file = File(filePath)
        return if (file.exists() && file.isFile) file.length() else 0L
    }

    /**
     * 递归获取目录大小
     *
     * @param dirPath 目录路径
     * @return 目录总大小（字节），目录不存在返回 0
     */
    fun getDirSize(dirPath: String): Long {
        val dir = File(dirPath)
        if (!dir.exists() || !dir.isDirectory) return 0L
        var size = 0L
        val files = dir.listFiles() ?: return 0L
        for (file in files) {
            size += if (file.isFile) {
                file.length()
            } else {
                getDirSize(file.absolutePath)
            }
        }
        return size
    }

    /**
     * 将文件大小格式化为可读字符串
     *
     * 例如：1536 -> "1.50 KB"，1048576 -> "1.00 MB"
     *
     * @param size 文件大小（字节）
     * @return 格式化后的字符串，如 "1.50 MB"
     */
    fun formatFileSize(size: Long): String {
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        var fileSize = size.toDouble()
        var unitIndex = 0
        while (fileSize >= 1024 && unitIndex < units.size - 1) {
            fileSize /= 1024
            unitIndex++
        }
        return String.format("%.2f %s", fileSize, units[unitIndex])
    }

    /**
     * 读取文件内容为字符串
     *
     * @param filePath 文件路径
     * @return 文件内容字符串，读取失败返回空字符串
     */
    fun readFileToString(filePath: String): String {
        val file = File(filePath)
        if (!file.exists() || !file.isFile) return ""
        val sb = StringBuilder()
        var reader: BufferedReader? = null
        try {
            reader = BufferedReader(InputStreamReader(FileInputStream(file), "UTF-8"))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                sb.append(line).append("\n")
            }
        } catch (e: IOException) {
            return ""
        } finally {
            closeIO(reader)
        }
        return sb.toString()
    }

    /**
     * 将字符串写入文件
     *
     * @param filePath 文件路径
     * @param content  要写入的内容
     * @param append   是否追加写入，默认 false（覆盖写入）
     * @return 写入成功返回 true，否则返回 false
     */
    fun writeStringToFile(filePath: String, content: String, append: Boolean = false): Boolean {
        val file = File(filePath)
        val parentFile = file.parentFile
        if (parentFile != null && !parentFile.exists() && !parentFile.mkdirs()) return false
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(file, append)
            fos.write(content.toByteArray(Charsets.UTF_8))
            return true
        } catch (e: IOException) {
            return false
        } finally {
            closeIO(fos)
        }
    }

    /**
     * 复制文件
     *
     * @param srcPath  源文件路径
     * @param destPath 目标文件路径
     * @return 复制成功返回 true，否则返回 false
     */
    fun copyFile(srcPath: String, destPath: String): Boolean {
        val srcFile = File(srcPath)
        val destFile = File(destPath)
        if (!srcFile.exists() || !srcFile.isFile) return false
        val destParent = destFile.parentFile
        if (destParent != null && !destParent.exists() && !destParent.mkdirs()) return false
        var fis: FileInputStream? = null
        var fos: FileOutputStream? = null
        try {
            fis = FileInputStream(srcFile)
            fos = FileOutputStream(destFile)
            val buffer = ByteArray(8192)
            var len: Int
            while (fis.read(buffer).also { len = it } != -1) {
                fos.write(buffer, 0, len)
            }
            return true
        } catch (e: IOException) {
            return false
        } finally {
            closeIO(fis, fos)
        }
    }

    /**
     * 移动文件，先复制后删除源文件
     *
     * @param srcPath  源文件路径
     * @param destPath 目标文件路径
     * @return 移动成功返回 true，否则返回 false
     */
    fun moveFile(srcPath: String, destPath: String): Boolean {
        return if (copyFile(srcPath, destPath)) {
            deleteFile(srcPath)
        } else {
            false
        }
    }

    /**
     * 获取内部存储缓存目录
     *
     * @param context 上下文，默认使用 AppContext
     * @return 内部缓存目录 File
     */
    fun getInternalCacheDir(context: Context = AppContext.app): File {
        return context.cacheDir
    }

    /**
     * 获取内部存储文件目录
     *
     * @param context 上下文，默认使用 AppContext
     * @return 内部文件目录 File
     */
    fun getInternalFilesDir(context: Context = AppContext.app): File {
        return context.filesDir
    }

    /**
     * 获取外部存储缓存目录
     *
     * @param context 上下文，默认使用 AppContext
     * @return 外部缓存目录 File，不可用时返回 null
     */
    fun getExternalCacheDir(context: Context = AppContext.app): File? {
        return context.externalCacheDir
    }

    /**
     * 获取外部存储文件目录
     *
     * @param type    子目录类型，如 Environment.DIRECTORY_PICTURES，null 为根目录
     * @param context 上下文，默认使用 AppContext
     * @return 外部文件目录 File，不可用时返回 null
     */
    fun getExternalFilesDir(type: String? = null, context: Context = AppContext.app): File? {
        return context.getExternalFilesDir(type)
    }

    /**
     * 判断外部存储是否可写
     *
     * @return 可写返回 true，否则返回 false
     */
    fun isExternalStorageWritable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    /**
     * 判断外部存储是否可读
     *
     * @return 可读返回 true，否则返回 false
     */
    fun isExternalStorageReadable(): Boolean {
        val state = Environment.getExternalStorageState()
        return state == Environment.MEDIA_MOUNTED || state == Environment.MEDIA_MOUNTED_READ_ONLY
    }

    /**
     * 安全关闭 Closeable 对象
     *
     * @param closeables 可变参数，传入需要关闭的 Closeable 对象
     */
    fun closeIO(vararg closeables: Closeable?) {
        for (closeable in closeables) {
            try {
                closeable?.close()
            } catch (_: IOException) {
            }
        }
    }
}
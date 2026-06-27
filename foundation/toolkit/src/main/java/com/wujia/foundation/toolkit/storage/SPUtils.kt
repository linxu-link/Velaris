package com.wujia.foundation.toolkit.storage

import android.content.Context
import android.content.SharedPreferences
import com.wujia.foundation.toolkit.app.AppContext

/**
 * SharedPreferences 工具类
 *
 * 提供 SharedPreferences 的便捷读写操作，支持多种数据类型，
 * 支持多 SP 文件管理。默认使用 [AppContext.app] 作为 Context，
 * 也可通过参数传入自定义 Context。
 */
object SPUtils {

    private const val DEFAULT_SP_NAME = "default_sp"

    private lateinit var appContext: Context

    /**
     * 初始化 SPUtils，由 HiToolKit.init 自动调用
     *
     * @param context 应用上下文，内部会取 applicationContext
     */
    fun init(context: Context) {
        appContext = context.applicationContext
    }

    /**
     * 获取 SharedPreferences 实例
     *
     * @param name SP 文件名，默认为 "default_sp"
     * @return SharedPreferences 实例
     */
    fun getSp(name: String = DEFAULT_SP_NAME): SharedPreferences {
        return appContext.getSharedPreferences(name, Context.MODE_PRIVATE)
    }

    /**
     * 向 SP 中写入数据，根据 value 类型自动选择对应的 put 方法，
     * 支持 String、Int、Long、Float、Boolean，value 为 null 时删除该 key
     *
     * @param key    键名
     * @param value  值，支持 String/Int/Long/Float/Boolean，null 时删除
     * @param spName SP 文件名，默认为 "default_sp"
     */
    fun put(key: String, value: Any?, spName: String = DEFAULT_SP_NAME) {
        val sp = getSp(spName)
        sp.edit().apply {
            when (value) {
                null -> remove(key)
                is String -> putString(key, value)
                is Int -> putInt(key, value)
                is Long -> putLong(key, value)
                is Float -> putFloat(key, value)
                is Boolean -> putBoolean(key, value)
                else -> putString(key, value.toString())
            }
            apply()
        }
    }

    /**
     * 读取 String 值
     *
     * @param key          键名
     * @param defaultValue 默认值，默认为空字符串
     * @param spName       SP 文件名，默认为 "default_sp"
     * @return 对应的 String 值，不存在则返回默认值
     */
    fun getString(key: String, defaultValue: String = "", spName: String = DEFAULT_SP_NAME): String {
        return getSp(spName).getString(key, defaultValue) ?: defaultValue
    }

    /**
     * 读取 Int 值
     *
     * @param key          键名
     * @param defaultValue 默认值，默认为 0
     * @param spName       SP 文件名，默认为 "default_sp"
     * @return 对应的 Int 值，不存在则返回默认值
     */
    fun getInt(key: String, defaultValue: Int = 0, spName: String = DEFAULT_SP_NAME): Int {
        return getSp(spName).getInt(key, defaultValue)
    }

    /**
     * 读取 Long 值
     *
     * @param key          键名
     * @param defaultValue 默认值，默认为 0L
     * @param spName       SP 文件名，默认为 "default_sp"
     * @return 对应的 Long 值，不存在则返回默认值
     */
    fun getLong(key: String, defaultValue: Long = 0L, spName: String = DEFAULT_SP_NAME): Long {
        return getSp(spName).getLong(key, defaultValue)
    }

    /**
     * 读取 Float 值
     *
     * @param key          键名
     * @param defaultValue 默认值，默认为 0f
     * @param spName       SP 文件名，默认为 "default_sp"
     * @return 对应的 Float 值，不存在则返回默认值
     */
    fun getFloat(key: String, defaultValue: Float = 0f, spName: String = DEFAULT_SP_NAME): Float {
        return getSp(spName).getFloat(key, defaultValue)
    }

    /**
     * 读取 Boolean 值
     *
     * @param key          键名
     * @param defaultValue 默认值，默认为 false
     * @param spName       SP 文件名，默认为 "default_sp"
     * @return 对应的 Boolean 值，不存在则返回默认值
     */
    fun getBoolean(key: String, defaultValue: Boolean = false, spName: String = DEFAULT_SP_NAME): Boolean {
        return getSp(spName).getBoolean(key, defaultValue)
    }

    /**
     * 移除指定 key
     *
     * @param key    要移除的键名
     * @param spName SP 文件名，默认为 "default_sp"
     */
    fun remove(key: String, spName: String = DEFAULT_SP_NAME) {
        getSp(spName).edit().remove(key).apply()
    }

    /**
     * 清空 SP 文件中的所有数据
     *
     * @param spName SP 文件名，默认为 "default_sp"
     */
    fun clear(spName: String = DEFAULT_SP_NAME) {
        getSp(spName).edit().clear().apply()
    }

    /**
     * 判断 SP 中是否包含指定 key
     *
     * @param key    键名
     * @param spName SP 文件名，默认为 "default_sp"
     * @return 包含返回 true，否则返回 false
     */
    fun contains(key: String, spName: String = DEFAULT_SP_NAME): Boolean {
        return getSp(spName).contains(key)
    }

    /**
     * 获取 SP 文件中的所有键值对
     *
     * @param spName SP 文件名，默认为 "default_sp"
     * @return 所有键值对的 Map
     */
    fun getAll(spName: String = DEFAULT_SP_NAME): Map<String, *> {
        return getSp(spName).all
    }
}
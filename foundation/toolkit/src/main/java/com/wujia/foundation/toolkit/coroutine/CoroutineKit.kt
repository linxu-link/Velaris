package com.wujia.foundation.toolkit.coroutine

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * 通用协程启动工具，类似线程池工具类。
 *
 * - Application 级别默认 scope（[launchIO]、[launchDefault]、[launchMain]）
 * - 自定义 scope 支持（[launchIn]）
 * - 自动错误捕获与日志
 * - 任务取消管理（返回 [Job]、[cancelAll]）
 */
object CoroutineKit {

    /** 错误处理器，可通过赋值替换为 Timber 等 */
    var errorHandler: (Throwable) -> Unit = {
        Timber.e(it, "CoroutineKit error")
    }

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        if (throwable is CancellationException) return@CoroutineExceptionHandler
        errorHandler(throwable)
    }

    private val applicationJob = SupervisorJob()
    private val applicationScope = CoroutineScope(applicationJob + exceptionHandler)

    // ── Application scope properties ──

    val ioScope: CoroutineScope
        get() = CoroutineScope(applicationJob + Dispatchers.IO + exceptionHandler)

    val defaultScope: CoroutineScope
        get() = CoroutineScope(applicationJob + Dispatchers.Default + exceptionHandler)

    val mainScope: CoroutineScope
        get() = CoroutineScope(applicationJob + Dispatchers.Main + exceptionHandler)

    // ── Launch on Application scope ──

    fun launchIO(block: suspend CoroutineScope.() -> Unit): Job =
        applicationScope.launch(Dispatchers.IO) { block() }

    fun launchDefault(block: suspend CoroutineScope.() -> Unit): Job =
        applicationScope.launch(Dispatchers.Default) { block() }

    fun launchMain(block: suspend CoroutineScope.() -> Unit): Job =
        applicationScope.launch(Dispatchers.Main) { block() }

    fun launch(
        dispatcher: CoroutineDispatcher = Dispatchers.Default,
        block: suspend CoroutineScope.() -> Unit,
    ): Job = applicationScope.launch(dispatcher) { block() }

    // ── Launch in custom scope ──

    fun launchIn(
        scope: CoroutineScope,
        dispatcher: CoroutineDispatcher = Dispatchers.Default,
        block: suspend CoroutineScope.() -> Unit,
    ): Job = scope.launch(dispatcher + exceptionHandler) { block() }

    // ── Task management ──

    /** 取消所有 Application scope 任务，scope 将不可用 */
    fun cancelAll() {
        applicationJob.cancel()
    }
}

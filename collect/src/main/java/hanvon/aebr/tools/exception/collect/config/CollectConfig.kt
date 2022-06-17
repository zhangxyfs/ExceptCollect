package hanvon.aebr.tools.exception.collect.config

import android.app.Application
import hanvon.aebr.tools.exception.collect.ExceptCollectManager

class CollectConfig {
    /**
     * 使用 java 监听
     */
    var usedJavaLeaker: Boolean = true
        private set

    /**
     * 使用 native 监听
     */
    var usedNativeLeaker: Boolean = true
        private set

    /**
     * 使用 线程监听
     */
    var usedThreadLeaker: Boolean = true
        private set

    /**
     * 延迟多少毫秒后开始监控
     */
    var javaLoopTime: Long = 5_000L

    /**
     * native监听 单位毫秒
     */
    var nativeLoopInterval: Long = 50_000L
        private set

    /**
     * 最小监听内存值
     */
    var minMemoryMonitor: Int = 16
        private set

    /**
     * 监控阈值
     */
    var thresholdMonitor: Int = 0
        private set

    /**
     * 监听的so列表， 不设置是监控所有， 设置是监听特定的so
     */
    var soMonitor: Array<String> = arrayOf()
        private set

    /**
     * 设置需要忽略监控的so
     */
    var ignoredSoMonitor: Array<String> = arrayOf()
        private set

    /**
     * 线程泄露监听 设置轮询间隔为30s
     */
    var threadLoopInterval: Long = 30_000L
        private set

    fun usedJavaLeaker(b: Boolean = usedJavaLeaker) = kotlin.run {
        usedJavaLeaker = b
        this
    }

    fun usedNativeLeaker(b: Boolean = usedNativeLeaker) = kotlin.run {
        usedNativeLeaker = b
        this
    }

    fun usedThreadLeaker(b: Boolean = usedThreadLeaker) = kotlin.run {
        usedThreadLeaker = b
        this
    }

    fun javaLoopTime(i: Long = javaLoopTime) = kotlin.run {
        javaLoopTime = i
        this
    }

    fun nativeLoopInterval(i: Long = nativeLoopInterval) = kotlin.run {
        nativeLoopInterval = i
        this
    }

    fun minMemoryMonitor(i: Int = minMemoryMonitor) = kotlin.run {
        minMemoryMonitor = i
        this
    }

    fun thresholdMonitor(i: Int = thresholdMonitor) = kotlin.run {
        thresholdMonitor = i
        this
    }

    fun soMonitor(array: Array<String> = soMonitor) = kotlin.run {
        soMonitor = array
        this
    }

    fun ignoredSoMonitor(array: Array<String> = ignoredSoMonitor) = kotlin.run {
        ignoredSoMonitor = array
        this
    }

    fun threadLoopInterval(i: Long = threadLoopInterval) = kotlin.run {
        threadLoopInterval = i
        this
    }

    fun init(application: Application) {
        ExceptCollectManager.instance.init(application, this)
    }
}
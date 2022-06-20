package hanvon.aebr.tools.exception.collect.startup

import android.app.Application
import android.content.Context
import com.kwai.koom.base.DefaultInitTask
import com.kwai.koom.base.MonitorLog
import com.kwai.koom.base.MonitorManager
import com.kwai.koom.javaoom.monitor.OOMHprofUploader
import com.kwai.koom.javaoom.monitor.OOMMonitor
import com.kwai.koom.javaoom.monitor.OOMMonitorConfig
import com.kwai.koom.javaoom.monitor.OOMReportUploader
import com.kwai.koom.nativeoom.leakmonitor.LeakListener
import com.kwai.koom.nativeoom.leakmonitor.LeakMonitor
import com.kwai.koom.nativeoom.leakmonitor.LeakMonitorConfig
import com.kwai.koom.nativeoom.leakmonitor.LeakRecord
import com.kwai.performance.overhead.thread.monitor.ThreadLeakListener
import com.kwai.performance.overhead.thread.monitor.ThreadLeakRecord
import com.kwai.performance.overhead.thread.monitor.ThreadMonitor
import com.kwai.performance.overhead.thread.monitor.ThreadMonitorConfig
import com.rousetime.android_startup.AndroidStartup
import hanvon.aebr.tools.exception.collect.config.CollectConfig
import hanvon.aebr.tools.exception.collect.utils.FileUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import kotlin.coroutines.CoroutineContext

/**
 * 快手 oom 捕获
 */
class KoomStartup(private val conf: CollectConfig, private val call: () -> Application) : AndroidStartup<String>(),
    CoroutineScope {
    private var mJob = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + mJob

    override fun callCreateOnMainThread() = true
    override fun waitOnMainThread() = true

    override fun create(context: Context): String? {
        DefaultInitTask.init(call.invoke())
        javaLeaker(context)
        nativeLeaker()
        threadLeaker()
        return KoomStartup::class.java.name
    }

    /**
     * Java Heap 泄漏监控
     */
    private fun javaLeaker(context: Context) {
        if (!conf.usedJavaLeaker) {
            return
        }

        val config = if (conf.isDebug) {
            javaTestConfig()
        } else {
            javaConfig()
        }.setEnableHprofDumpAnalysis(true)
            .setHprofUploader(object : OOMHprofUploader {
                override fun upload(file: File, type: OOMHprofUploader.HprofType) {
                    MonitorLog.e("OOMMonitor", "todo, upload hprof ${file.name} if necessary")
                    launch(Dispatchers.IO) {
                        FileUtils.copyToSdcard(file, context)
                    }
                }
            })
            .setReportUploader(object : OOMReportUploader {
                override fun upload(file: File, content: String) {
                    MonitorLog.i("OOMMonitor", content)
                    MonitorLog.e("OOMMonitor", "todo, upload report ${file.name} if necessary")
                    launch(Dispatchers.IO) {
                        FileUtils.copyToSdcard(file, context)
                    }
                }
            })
            .build()
        MonitorManager.addMonitorConfig(config)
        OOMMonitor.startLoop(delayMillis = conf.javaLoopTime)
    }

    private fun javaTestConfig() = OOMMonitorConfig.Builder()
        .setThreadThreshold(50)
        .setFdThreshold(300)
        .setHeapThreshold(0.9f)
        .setVssSizeThreshold(1_000_000)
        .setMaxOverThresholdCount(1)
        .setAnalysisMaxTimesPerVersion(3)
        .setAnalysisPeriodPerVersion(15 * 24 * 60 * 60 * 1000)
        .setLoopInterval(5_000)

    private fun javaConfig() = OOMMonitorConfig.Builder()


    /**
     * Native Heap 泄漏监控
     */
    private fun nativeLeaker() {
        if (!conf.usedNativeLeaker) {
            return
        }

        val config: LeakMonitorConfig.Builder = LeakMonitorConfig.Builder()
            // 设置轮训的间隔，单位：毫秒
            .setLoopInterval(conf.nativeLoopInterval)
            // 设置监听的最小内存值，单位：字节
            .setMonitorThreshold(conf.minMemoryMonitor)
            // 设置native heap分配的内存达到多少阈值开始监控，单位：字节
            .setNativeHeapAllocatedThreshold(conf.thresholdMonitor)
        // 不设置是监控所有， 设置是监听特定的so,  比如监控libcore.so 填写 libcore 不带.so
        if (conf.soMonitor.isNotEmpty()) {
            config.setSelectedSoList(conf.soMonitor)
        }
        // 设置需要忽略监控的so
        if (conf.ignoredSoMonitor.isNotEmpty()) {
            config.setIgnoredSoList(conf.ignoredSoMonitor)
        }
        // 设置使能本地符号化，仅在 debuggable apk 下有用，release 请关闭
        config.setEnableLocalSymbolic(false)
            .setLeakListener(object : LeakListener {
                override fun onLeak(leaks: MutableCollection<LeakRecord>) {
                    leaks.forEach {
                        MonitorLog.i("nativeLeaker", it.toString())
                    }
                }
            }) // 设置泄漏监听器
        MonitorManager.addMonitorConfig(config.build())
        LeakMonitor.start()
    }

    /**
     * Thread 泄漏监控
     */
    private fun threadLeaker() {
        if (!conf.usedThreadLeaker) {
            return
        }
        val config = ThreadMonitorConfig.Builder()
            .enableThreadLeakCheck(conf.threadLoopInterval, 60 * 1000L) // 设置轮询间隔为30s，线程泄露延迟期限为1min
            .setListener(object : ThreadLeakListener {
                override fun onError(msg: String) {
                    MonitorLog.e("ThreadLeaker", msg)
                }

                override fun onReport(leaks: MutableList<ThreadLeakRecord>) {
                    leaks.forEach {
                        MonitorLog.i("ThreadLeaker", it.toString())
                    }
                }
            }).build()
        MonitorManager.addMonitorConfig(config)
        ThreadMonitor.startTrackAsync()
    }
}
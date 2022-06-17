package hanvon.aebr.tools.exception.collect

import android.app.Application
import androidx.multidex.BuildConfig
import com.rousetime.android_startup.StartupManager
import com.rousetime.android_startup.model.LoggerLevel
import com.rousetime.android_startup.model.StartupConfig
import hanvon.aebr.tools.exception.collect.config.CollectConfig
import hanvon.aebr.tools.exception.collect.startup.KoomStartup

/**
 * 异常收集
 */
class ExceptCollectManager private constructor() {
    /**
     * 设置参数
     */
    fun build() = CollectConfig()

    /**
     * 初始化
     * 请放入[Application]中
     */
    fun init(application: Application) {
        init(application, CollectConfig())
    }

    /**
     * 初始化
     * 请放入[Application]中
     */
    fun init(application: Application, config: CollectConfig = CollectConfig()) {
        StartupManager.Builder()
            .setConfig(
                StartupConfig.Builder()
                    .setLoggerLevel(
                        if (BuildConfig.DEBUG) {
                            LoggerLevel.DEBUG
                        } else {
                            LoggerLevel.NONE
                        }
                    ).build()
            )
            .addStartup(KoomStartup(config) {
                return@KoomStartup application
            })
            .build(application).start()
    }


    companion object {
        val instance by
        lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            ExceptCollectManager()
        }
    }
}
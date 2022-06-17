package hanvon.aebr.tools.exception.collect

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ExceptCollectManager.instance.build()
            .usedJavaLeaker(true)
            .usedNativeLeaker(true)
            .usedThreadLeaker(true)
            .nativeLoopInterval(5000L)
            .threadLoopInterval(3000L)
            .minMemoryMonitor(16)
            .thresholdMonitor(0)
            .soMonitor()
            .ignoredSoMonitor()
            .init(application)
    }
}
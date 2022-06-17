package hanvon.aebr.tools.exception.collect.utils

import android.content.Context
import android.os.Environment
import com.kwai.koom.base.MonitorLog
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.channels.FileChannel

object FileUtils {
    /**
     * 文件拷贝到sdcard下
     */
    suspend fun copyToSdcard(file: File, context: Context) {
        context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.apply {
            val newFile = File(this, file.name)
            nioTransferCopy(file, newFile)
            MonitorLog.e("OOMMonitor", "todo, copy ${newFile.path} success")
        }
    }

    private fun nioTransferCopy(source: File, target: File) {
        var `in`: FileChannel? = null
        var out: FileChannel? = null
        var inStream: FileInputStream? = null
        var outStream: FileOutputStream? = null
        try {
            inStream = FileInputStream(source)
            outStream = FileOutputStream(target)
            `in` = inStream.getChannel()
            out = outStream.getChannel()
            `in`.transferTo(0, `in`.size(), out)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            inStream?.close()
            `in`?.close()
            outStream?.close()
            out?.close()
        }
    }
}
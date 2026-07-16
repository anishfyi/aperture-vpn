package io.github.anishfyi.aperture

import android.content.Context
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

/*
 * Lightweight crash capture. We cannot pull logcat from every device, so on an
 * uncaught exception we persist the stack trace and surface it on next launch,
 * where the user can read or screenshot it. The previous handler still runs so
 * the process dies normally.
 */
object CrashLog {
    private const val FILE = "last_crash.txt"

    fun install(context: Context) {
        val appContext = context.applicationContext
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            runCatching {
                val sw = StringWriter()
                throwable.printStackTrace(PrintWriter(sw))
                val header = "thread=${thread.name}\n\n"
                File(appContext.filesDir, FILE).writeText(header + sw.toString())
            }
            previous?.uncaughtException(thread, throwable)
        }
    }

    fun read(context: Context): String? {
        val file = File(context.applicationContext.filesDir, FILE)
        return if (file.exists()) file.readText().takeIf { it.isNotBlank() } else null
    }

    fun clear(context: Context) {
        File(context.applicationContext.filesDir, FILE).delete()
    }
}

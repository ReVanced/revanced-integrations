package app.revanced.integrations.utils

import android.content.Context
import android.content.res.Resources
import android.os.Handler
import java.lang.ref.WeakReference

private val CTX_NULL = IllegalArgumentException("Context cannot be null")
private val CTX_NOT_INIT = IllegalArgumentException("Context is not initialized")

object ReVancedUtils {
    @JvmStatic
    var isNewVideo = false

    private var context: WeakReference<Context>? = null
        set(value) {
            field = value ?: throw CTX_NULL
            handler = Handler(value.get()!!.mainLooper)
        }

    lateinit var handler: Handler
        private set

    val resources: Resources get() = context().resources

    @JvmStatic
    fun context(): Context {
        return context?.get() ?: throw CTX_NOT_INIT
    }

    @JvmStatic
    fun setContext(ctx: Context) {
        context = WeakReference(ctx)
    }

    @JvmStatic
    fun runOnMainThread(runnable: Runnable) {
        handler.post(runnable)
    }

    @JvmStatic
    fun stringContains(string: String, vararg substrings: String): Boolean {
        return substrings.any { string.contains(it) }
    }
}
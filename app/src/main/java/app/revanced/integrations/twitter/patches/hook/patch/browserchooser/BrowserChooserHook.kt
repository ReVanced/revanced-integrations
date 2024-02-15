package app.revanced.integrations.twitter.patches.hook.patch.browserchooser

import android.content.Context
import android.content.Intent
import app.revanced.integrations.twitter.patches.hook.twifucker.TwiFucker


object BrowserChooserHook {
    fun open(context: Context, intent: Intent) = TwiFucker.openWithChooser(context, intent)
}
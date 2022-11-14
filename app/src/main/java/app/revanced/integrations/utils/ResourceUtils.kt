package app.revanced.integrations.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.View
import android.view.animation.AnimationUtils
import app.revanced.integrations.utils.ReVancedUtils.getContext
import app.revanced.integrations.utils.ReVancedUtils.resources

@SuppressLint("DiscouragedApi")
object ResourceUtils {
    @JvmStatic
    fun identifier(name: String, type: ResourceType) =
        identifier(name, type, getContext())

    @JvmStatic
    fun identifier(name: String, type: ResourceType, context: Context): Int {
        return resources.getIdentifier(name, type.value, context.packageName)
    }

    @JvmStatic
    fun string(name: String) = identifier(name, ResourceType.STRING).let {
        if (it == 0) name else resources.getString(it)
    }

    @JvmStatic
    fun integer(name: String) = resources.getInteger(identifier(name, ResourceType.INTEGER))

    @JvmStatic
    fun anim(name: String) =
        AnimationUtils.loadAnimation(getContext(), identifier(name, ResourceType.ANIM))

    @JvmStatic
    fun <T : Class<*>, R : View> findView(clazz: T, view: View, name: String): R {
        return view.findViewById(identifier(name, ResourceType.ID)) ?: run {
            val ex = IllegalArgumentException("View with name $name not found")
            LogHelper.printException(clazz, "View not found", ex)
            throw ex
        }
    }

    @JvmStatic
    fun <T : Class<*>, R : View> findView(clazz: T, activity: Activity, name: String): R {
        return findView(clazz, activity.window.decorView, name)
    }
}

@Suppress("unused")
enum class ResourceType(internal val value: String) {
    STRING("string"),
    LAYOUT("layout"),
    DRAWABLE("drawable"),
    COLOR("color"),
    STYLE("style"),
    ANIM("anim"),
    MENU("menu"),
    RAW("raw"),
    XML("xml"),
    FONT("font"),
    MIPMAP("mipmap"),
    ID("id"),
    INTEGER("integer");
}
package com.kuelye.vkcup20ii.core.utils

import android.app.Activity.INPUT_METHOD_SERVICE
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.Bitmap.Config.ARGB_8888
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build.VERSION.SDK_INT
import android.util.SparseArray
import android.util.TypedValue
import android.util.TypedValue.COMPLEX_UNIT_SP
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.*
import com.kuelye.vkcup20ii.core.R
import com.kuelye.vkcup20ii.core.model.VKGroup
import com.vk.api.sdk.utils.VKUtils.density
import com.vk.api.sdk.utils.VKUtils.getDisplayMetrics
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.GregorianCalendar.YEAR
import kotlin.math.*


// # RESOURCES

fun dimen(context: Context, @DimenRes dimen: Int): Int =
    context.resources.getDimension(dimen).toInt()

fun View.dimen(@DimenRes dimen: Int): Int = dimen(context, dimen)

@ColorInt
fun color(context: Context, @ColorRes color: Int): Int =
    if (SDK_INT >= 23) context.getColor(color) else context.resources.getColor(color)

@ColorInt
fun View.color(@ColorRes color: Int): Int = color(context, color)

@ColorInt
fun themeColor(context: Context, @AttrRes attr: Int): Int {
    val typedValue = TypedValue()
    context.theme.resolveAttribute(attr, typedValue, true)
    return color(context, typedValue.resourceId)
}

@ColorInt
fun View.themeColor(@AttrRes attr: Int): Int = themeColor(context, attr)

@Dimension
fun View.themeDimen(@AttrRes res: Int): Int {
    val typedValue = TypedValue()
    context.theme.resolveAttribute(res, typedValue, true)
    return typedValue.getDimension(context.resources.displayMetrics).toInt()
}

fun View.themeDrawable(@AttrRes attr : Int): Drawable {
    val typedValue = TypedValue()
    context.theme.resolveAttribute(attr, typedValue, true)
    return context.resources.getDrawable(typedValue.resourceId, context.theme)
}

// # DRAWABLE

fun toBitmap(
    drawable: Drawable?
): Bitmap? {
    return when (drawable) {
        null -> null
        is BitmapDrawable -> drawable.bitmap
        else -> return try {
            val width = if (drawable is ColorDrawable) 1 else drawable.intrinsicWidth
            val height = if (drawable is ColorDrawable) 1 else drawable.intrinsicHeight
            val bitmap: Bitmap = Bitmap.createBitmap(width, height, ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

fun toBitmap(
    context: Context,
    @DrawableRes drawableRes: Int,
    width: Int, height: Int
): Bitmap {
    val drawable = context.resources.getDrawable(drawableRes)
    val bitmap = Bitmap.createBitmap(width, height, ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, width, height)
    drawable.isFilterBitmap = true
    drawable.draw(canvas)
    return bitmap
}

// # MATH

fun interpolate(state: Float, from: Float, to: Float): Float {
    return when (state) {
        0.0f -> from
        1.0f -> to
        else -> from + (to - from) * state
    }
}

fun interpolate(state: Float, from: Int, to: Int): Int {
    return when (state) {
        0.0f -> from
        1.0f -> to
        else -> (from + (to - from) * state).toInt()
    }
}

fun ceil(dividend: Int, divisor: Int) =
    dividend.sign * divisor.sign * (dividend.absoluteValue + divisor.absoluteValue - 1) /
            divisor.absoluteValue

fun formatShort(value: Float, decimals: Int = 1, floor: Boolean = false): String {
    return if (decimals == 0) {
        (if (floor) floor(value).toInt() else round(value).toInt()).toString()
    } else {
        val tenValues = if (floor) floor(value * 10) else round(value * 10)
        "%.1f".format(tenValues / 10).replace(".0", "")
    }
}

// # COLOR

@ColorInt
fun Int.modifyAlpha(factor: Float): Int = Color.argb(
    (Color.alpha(this) * factor).toInt(), Color.red(this),
    Color.green(this), Color.blue(this)
)

@ColorInt
fun interpolateColor(state: Float, @ColorInt from: Int, @ColorInt to: Int): Int {
    return when (state) {
        0.0f -> from
        1.0f -> to
        else -> Color.argb(
            interpolate(state, Color.alpha(from), Color.alpha(to)),
            interpolate(state, Color.red(from), Color.red(to)),
            interpolate(state, Color.green(from), Color.green(to)),
            interpolate(state, Color.blue(from), Color.blue(to))
        )
    }
}

// # DATE

private val LOCALE =
    Locale.getAvailableLocales().firstOrNull { it.language == "ru" } ?: Locale.ENGLISH

private val ONLY_DATE_FORMAT = SimpleDateFormat("d MMMM", LOCALE)
private val DATE_WITH_YEAR_FORMAT = SimpleDateFormat("d MMMM yyyy", LOCALE)

fun formatTime(context: Context, timestamp: Long): String {
    return when {
        isToday(timestamp) -> context.getString(R.string.a_today)
        isSameYear(timestamp) -> ONLY_DATE_FORMAT.format(Date(timestamp))
        else -> DATE_WITH_YEAR_FORMAT.format(Date(timestamp))
    }
}

private fun isToday(timestamp: Long): Boolean {
    val calendar = GregorianCalendar()
    calendar.timeInMillis = timestamp
    val thenYear = calendar.get(YEAR)
    val thenMonth = calendar.get(Calendar.MONTH)
    val thenDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
    calendar.timeInMillis = System.currentTimeMillis()
    return (thenYear == calendar.get(YEAR)
            && thenMonth == calendar.get(Calendar.MONTH)
            && thenDayOfMonth == calendar.get(Calendar.DAY_OF_MONTH))
}

private fun isSameYear(timestamp: Long): Boolean {
    val calendar = GregorianCalendar()
    calendar.timeInMillis = timestamp
    val thenYear = calendar.get(YEAR)
    calendar.timeInMillis = System.currentTimeMillis()
    return thenYear == calendar.get(YEAR)
}

// # JSON

inline fun <R> JSONArray.map(transform: (JSONObject) -> R): List<R> {
    val destination = mutableListOf<R>()
    for (i in 0 until length()) {
        destination.add(transform.invoke(getJSONObject(i)))
    }
    return destination
}

fun JSONArray.toStringList(): List<String> {
    val destination = mutableListOf<String>()
    for (i in 0 until length()) {
        destination.add(getString(i))
    }
    return destination
}

// # MISC

fun px(px: Int) = ceil((px.toDouble() / density())).toInt()

fun sp(sp: Int) = TypedValue.applyDimension(COMPLEX_UNIT_SP, sp.toFloat(), getDisplayMetrics())

fun getStatusBarHeight(context: Context): Int {
    val resourceId = context.resources.getIdentifier(
        "status_bar_height", "dimen", "android")
    return if (resourceId > 0) {
        context.resources.getDimensionPixelSize(resourceId)
    } else {
        0
    }
}

fun hideKeyboard(context: Context, view: View) {
    val imm = context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view.windowToken, 0)
    view.clearFocus()
}

fun getImagePath(context: Context, imageUri: Uri): Uri? {
    var input: FileInputStream? = null
    var output: FileOutputStream? = null
    try {
        val pfd = context.contentResolver.openFileDescriptor(imageUri, "r") ?: return null
        input = FileInputStream(pfd.fileDescriptor)

        val outputFile = File.createTempFile("image", null, context.cacheDir)
        output = FileOutputStream(outputFile.absolutePath)

        input.copyTo(output)

        return Uri.parse("file://" + outputFile.absolutePath)
    } finally {
        try {
            input?.close()
            output?.close()
        } catch (e: Exception) {
            // ignore
        }
    }
}

fun <T> SparseArray<T>.toList(): List<T> {
    val result = mutableListOf<T>()
    for (i in 0 until size()) {
        result.add(get(keyAt(i)))
    }
    return result
}

inline fun <T> SparseArray<T>.filter(predicate: (T) -> Boolean): List<T> {
    val result = mutableListOf<T>()
    for (i in 0 until size()) {
        val element = get(keyAt(i))
        if (predicate(element)) result.add(element)
    }
    return result
}

fun getHeight(paint: Paint, text: String?): Int {
    return if (text == null) {
        0
    } else {
        val bounds = Rect()
        paint.getTextBounds(text, 0, text.length, bounds)
        return bounds.height()
    }
}

// # VK

fun open(context: Context, group: VKGroup) {
    val uri: Uri = Uri.parse("http://vk.com/${group.screenName}")
    val intent = Intent(Intent.ACTION_VIEW, uri)
    context.startActivity(intent)
}
package com.kuelye.vkcup20ii.core.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.Config.ARGB_8888
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.View
import androidx.annotation.*
import com.vk.api.sdk.utils.VKUtils
import kotlin.math.ceil


fun dimen(context: Context, @DimenRes res: Int): Int = context.resources.getDimension(res).toInt()

fun View.dimen(@DimenRes res: Int): Int = dimen(context, res)

@Dimension
fun View.themeDimen(@AttrRes res: Int): Int {
    val typedValue = TypedValue()
    context.theme.resolveAttribute(res, typedValue, true)
    return typedValue.getDimension(context.resources.displayMetrics).toInt()
}

fun px(px: Int) = ceil((px.toDouble() / VKUtils.density())).toInt()

fun toBitmap(
    drawable: Drawable?
): Bitmap? {
    return when {
        drawable == null -> null
        drawable is BitmapDrawable -> drawable.bitmap
        else -> return try {
            val bitmap: Bitmap = if (drawable is ColorDrawable) {
                Bitmap.createBitmap(2, 2, ARGB_8888)
            } else {
                Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, ARGB_8888)
            }
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


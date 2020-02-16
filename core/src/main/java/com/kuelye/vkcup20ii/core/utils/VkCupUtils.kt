package com.kuelye.vkcup20ii.core.utils

import android.content.Context
import android.util.TypedValue
import android.view.View
import androidx.annotation.AttrRes
import androidx.annotation.DimenRes
import androidx.annotation.Dimension

fun View.dimen(@DimenRes res: Int): Int = context.resources.getDimension(res).toInt()

@Dimension
fun View.themeDimen(@AttrRes res: Int): Int {
    val typedValue = TypedValue()
    context.theme.resolveAttribute(res, typedValue, true)
    return typedValue.getDimension(context.resources.displayMetrics).toInt()
}
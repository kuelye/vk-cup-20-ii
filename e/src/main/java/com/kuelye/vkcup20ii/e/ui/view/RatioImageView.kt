package com.kuelye.vkcup20ii.e.ui.view

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.ImageView.ScaleType.FIT_XY


class RatioImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ImageView(context, attrs, defStyleAttr) {

    init {
        scaleType = FIT_XY
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (drawable == null || drawable.intrinsicWidth == 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        } else {
            val width = MeasureSpec.getSize(widthMeasureSpec)
            val height = width * drawable.intrinsicHeight / drawable.intrinsicWidth
            setMeasuredDimension(width, height)
        }
    }

}
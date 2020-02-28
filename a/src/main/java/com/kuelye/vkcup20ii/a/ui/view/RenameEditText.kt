package com.kuelye.vkcup20ii.a.ui.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.Paint.Style.FILL
import android.util.AttributeSet
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.EditText
import androidx.appcompat.widget.AppCompatEditText
import com.kuelye.vkcup20ii.a.R
import com.kuelye.vkcup20ii.core.utils.color
import com.kuelye.vkcup20ii.core.utils.modifyAlpha

class RenameEditText @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatEditText(context, attrs, defStyleAttr) {

    private var backgroundAlpha: Float = 0f
    private var backgroundAnimator: ValueAnimator? = null
    private var animatorEnabledTarget: Boolean? = null

    private val backgroundColor = color(R.color.filled_box_edit_text_fill_color)

    private val paint = Paint().apply {
        style = FILL
        color = backgroundColor
        flags = ANTI_ALIAS_FLAG
    }

    override fun onDraw(canvas: Canvas) {
        if (backgroundAlpha > 0) {
            paint.color = backgroundColor.modifyAlpha(backgroundAlpha)
            canvas.drawRoundRect(0f, 0f, width.toFloat(), height.toFloat(), 10f, 10f, paint)
        }
        super.onDraw(canvas)
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        animateBackground(enabled)
    }

    private fun animateBackground(enabled: Boolean) {
        if (animatorEnabledTarget != enabled) {
            animatorEnabledTarget = enabled
            if (backgroundAnimator == null) {
                backgroundAnimator = ValueAnimator().apply {
                    interpolator = AccelerateDecelerateInterpolator()
                    addUpdateListener {
                        backgroundAlpha = it.animatedValue as Float
                        invalidate()
                    }
                }
            } else {
                backgroundAnimator!!.cancel()
            }
            backgroundAnimator!!.apply {
                setFloatValues(backgroundAlpha, if (enabled) 1f else 0f)
                start()
            }
        }
    }

}
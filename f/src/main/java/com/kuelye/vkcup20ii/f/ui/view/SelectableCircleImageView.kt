package com.kuelye.vkcup20ii.f.ui.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.animation.DecelerateInterpolator
import com.kuelye.vkcup20ii.core.utils.interpolate
import com.kuelye.vkcup20ii.core.utils.interpolateColor
import com.kuelye.vkcup20ii.core.utils.toBitmap
import com.kuelye.vkcup20ii.f.R
import com.vk.api.sdk.utils.VKUtils.dp
import kotlin.math.sqrt

class SelectableCircleImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : CircleImageView(context, attrs, defStyleAttr) {

    companion object {
        private val TAG = SelectableCircleImageView::class.java.simpleName

        private val SQUARE_OF_TWO = sqrt(2f)

        private val UNSELECTED_BORDER_WIDTH = dp(1).toFloat()
        private val SELECTED_BORDER_WIDTH = dp(2).toFloat()
        private const val UNSELECTED_BORDER_COLOR = 0xFFF6F6F6.toInt()
        private const val SELECTED_BORDER_COLOR = 0xFF5499E5.toInt()
        private const val UNSELECTED_SCALE = 1f
        private const val SELECTED_SCALE = 0.95f
    }

    private var animator: ValueAnimator? = null

    private val iconSize = dp(28)
    private val checkIcon: Bitmap = toBitmap(context, R.drawable.ic_check_circle_28, iconSize, iconSize)
    private val iconMatrix = Matrix()
    private val iconPaint = Paint()

    init {
        updateBySelected()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateBySelected()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(checkIcon, iconMatrix, iconPaint)
    }

    override fun setSelected(selected: Boolean) {
        super.setSelected(selected)
        updateBySelected()
    }

    fun animateSelected(selected: Boolean) {
        if (isSelected != selected) {
            super.setSelected(selected)
            animateSelected(getState(selected))
        }
    }

    private fun getState(selected: Boolean) = if (selected) 1f else 0f

    private fun animateSelected(valueTo: Float) {
        if (animator == null) {
            animator = ValueAnimator().apply {
                interpolator = DecelerateInterpolator()
                addUpdateListener { updateBySelected(animator!!.animatedValue as Float) }
            }
        } else {
            animator!!.cancel()
        }

        animator!!.setFloatValues(1 - valueTo, valueTo)
        animator!!.start()
    }

    private fun updateBySelected(state: Float = getState(isSelected)) {
        borderWidth = interpolate(state, UNSELECTED_BORDER_WIDTH, SELECTED_BORDER_WIDTH)
        borderColor = interpolateColor(state, UNSELECTED_BORDER_COLOR, SELECTED_BORDER_COLOR)
        scale = interpolate(state, UNSELECTED_SCALE, SELECTED_SCALE)

        val halfWidth = width.toFloat() / 2
        val r = halfWidth - borderWidth / 2
        val x = halfWidth + SQUARE_OF_TWO * r / 2 - state * iconSize / 2
        iconMatrix.reset()
        iconMatrix.setScale(state, state)
        iconMatrix.postTranslate(x, x)

        invalidate()
    }

}
package com.kuelye.vkcup20ii.core.ui.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.util.AttributeSet
import android.view.animation.DecelerateInterpolator
import androidx.annotation.DrawableRes
import com.kuelye.vkcup20ii.core.ui.view.BadgeBorderImageView.Position.BOTTOM_RIGHT
import com.kuelye.vkcup20ii.core.ui.view.BorderImageView.BorderType.CIRCLE
import com.kuelye.vkcup20ii.core.utils.interpolate
import com.kuelye.vkcup20ii.core.utils.interpolateColor
import com.kuelye.vkcup20ii.core.utils.toBitmap
import com.vk.api.sdk.utils.VKUtils
import kotlin.math.sqrt

class BadgeBorderImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BorderImageView(context, attrs, defStyleAttr) {

    companion object {
        private val TAG = BadgeBorderImageView::class.java.simpleName
        private val SQUARE_OF_TWO = sqrt(2f)

        val UNSELECTED_BORDER_WIDTH = VKUtils.dp(1).toFloat()
        private val SELECTED_BORDER_WIDTH_DEFAULT = VKUtils.dp(2).toFloat()
        const val UNSELECTED_BORDER_COLOR = 0xFFF6F6F6.toInt()
        private const val SELECTED_BORDER_COLOR_DEFAULT = 0xFF5499E5.toInt()
        private const val UNSELECTED_SCALE = 1f
        private const val SELECTED_SCALE_DEFAULT = 0.93f
    }

    var selectedScale = SELECTED_SCALE_DEFAULT
        set(value) {
            if (field != value) {
                field = value
                updateBadge()
            }
        }
    var selectedBorderWidth = SELECTED_BORDER_WIDTH_DEFAULT
        set(value) {
            if (field != value) {
                field = value
                updateBadge()
            }
        }
    var selectedBorderColor = SELECTED_BORDER_COLOR_DEFAULT
        set(value) {
            if (field != value) {
                field = value
                updateBadge()
            }
        }

    private var animator: ValueAnimator? = null

    private var badgeSize: Int? = null
    private var badgeIcon: Bitmap? = null
    private var badgePosition: Position? = null
    private val badgeMatrix: Matrix by lazy { Matrix() }
    private val badgePaint: Paint by lazy { Paint() }

    init {
        updateBadge()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateBadge()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (badgeIcon != null) canvas.drawBitmap(badgeIcon!!, badgeMatrix, badgePaint)
    }

    override fun setSelected(selected: Boolean) {
        super.setSelected(selected)
        updateBadge()
    }

    fun setBadge(
        @DrawableRes icon: Int, size: Int, position: Position
    ) {
        badgeIcon = toBitmap(context, icon, size, size)
        badgeSize = size
        badgePosition = position
    }

    fun animateSelected(selected: Boolean) {
        if (isSelected != selected) {
            super.setSelected(selected)
            animateSelected(getState(selected))
        }
    }

    private fun getState(selected: Boolean) = if (selected) 1f else 0f

    private fun animateSelected(valueTo: Float) {
        if (animator == null && badgeIcon != null) {
            animator = ValueAnimator().apply {
                interpolator = DecelerateInterpolator()
                addUpdateListener { updateBadge(animator!!.animatedValue as Float) }
            }
        } else {
            animator!!.cancel()
        }

        animator!!.setFloatValues(1 - valueTo, valueTo)
        animator!!.start()
    }

    private fun updateBadge(state: Float = getState(isSelected)) {
        if (badgeIcon != null && badgeSize != null && badgePosition != null) {
            borderWidth = interpolate(state, UNSELECTED_BORDER_WIDTH, selectedBorderWidth)
            borderColor = interpolateColor(state, UNSELECTED_BORDER_COLOR, selectedBorderColor)
            scale = interpolate(state, UNSELECTED_SCALE, selectedScale)

            val halfWidth = width.toFloat() / 2
            val d0 = halfWidth - state * badgeSize!! / 2
            val d = -badgeSize!! / 2 + if (borderType == CIRCLE) {
                val r = halfWidth - borderWidth / 2
                SQUARE_OF_TWO * r / 2
            } else {
                halfWidth
            }
            badgeMatrix.reset()
            badgeMatrix.setScale(state, state)
            badgeMatrix.postTranslate(d0 + d, d0 + if (badgePosition == BOTTOM_RIGHT) d else -d)

            invalidate()
        }
    }

    enum class Position {
        BOTTOM_RIGHT, TOP_RIGHT
    }

}
package com.kuelye.vkcup20ii.core.ui.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_UP
import android.view.animation.DecelerateInterpolator
import androidx.annotation.DrawableRes
import com.kuelye.vkcup20ii.core.ui.view.BadgeBorderImageView.Position.BOTTOM_RIGHT
import com.kuelye.vkcup20ii.core.ui.view.BorderImageView.BorderType.CIRCLE
import com.kuelye.vkcup20ii.core.utils.interpolate
import com.kuelye.vkcup20ii.core.utils.interpolateColor
import com.kuelye.vkcup20ii.core.utils.toBitmap
import com.vk.api.sdk.utils.VKUtils
import com.vk.api.sdk.utils.VKUtils.dp
import java.lang.Math.pow
import kotlin.math.sqrt

class BadgeBorderImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BorderImageView(context, attrs, defStyleAttr) {

    companion object {
        private val TAG = BadgeBorderImageView::class.java.simpleName
        private val SQUARE_OF_TWO = sqrt(2f)

        val UNSELECTED_BORDER_WIDTH = dp(1).toFloat()
        private val SELECTED_BORDER_WIDTH_DEFAULT = VKUtils.dp(2).toFloat()
        const val UNSELECTED_BORDER_COLOR = 0xFFF6F6F6.toInt()
        private const val SELECTED_BORDER_COLOR_DEFAULT = 0xFF5499E5.toInt()
        private const val UNSELECTED_SCALE = 1f
        private const val SELECTED_SCALE_DEFAULT = 0.93f
        private val CLICK_DISTANCE_HALF = dp(24)
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

    var onBadgeClickListener: (() -> Unit)? = null

    private var selectedAnimator: ValueAnimator? = null

    private var badgeSize: Int? = null
    private var badgeIcon: Bitmap? = null
    private var badgePosition: Position? = null
    private var badgeCenter: PointF? = null
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

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (badgeCenter != null && isSelected) {
            if (event.action == ACTION_UP && (event.x - badgeCenter!!.x) * (event.x - badgeCenter!!.x)
                    + (event.y - badgeCenter!!.y) * (event.y - badgeCenter!!.y)
                    < CLICK_DISTANCE_HALF * CLICK_DISTANCE_HALF) {
                onBadgeClickListener?.invoke()
            }
            return true
        }
        return super.onTouchEvent(event)
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

    private fun animateSelected(targetValue: Float) {
        if (selectedAnimator == null && badgeIcon != null) {
            selectedAnimator = ValueAnimator().apply {
                interpolator = DecelerateInterpolator()
                addUpdateListener { updateBadge(selectedAnimator!!.animatedValue as Float) }
            }
        }
        selectedAnimator!!.apply {
            cancel()
            setFloatValues(1 - targetValue, targetValue)
            start()
        }
    }

    private fun updateBadge(state: Float = getState(isSelected)) {
        if (badgeIcon != null && badgeSize != null && badgePosition != null) {
            borderWidth = interpolate(state, UNSELECTED_BORDER_WIDTH, selectedBorderWidth)
            borderColor = interpolateColor(state, UNSELECTED_BORDER_COLOR, selectedBorderColor)
            scale = interpolate(state, UNSELECTED_SCALE, selectedScale)

            val hW = width.toFloat() / 2
            val d0 = -state * badgeSize!! / 2
            val d = -badgeSize!! / 2 + if (borderType == CIRCLE) {
                val r = hW - borderWidth / 2
                SQUARE_OF_TWO * r / 2
            } else {
                hW
            }
            badgeCenter = PointF(hW + d, hW + if (badgePosition == BOTTOM_RIGHT) d else -d)
            badgeMatrix.reset()
            badgeMatrix.setScale(state, state)
            badgeMatrix.postTranslate(badgeCenter!!.x + d0, badgeCenter!!.y + d0)

            invalidate()
        }
    }

    enum class Position {
        BOTTOM_RIGHT, TOP_RIGHT
    }

}
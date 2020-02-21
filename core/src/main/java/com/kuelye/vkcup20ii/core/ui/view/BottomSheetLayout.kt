package com.kuelye.vkcup20ii.core.ui.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color.BLACK
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.Gravity.BOTTOM
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import com.kuelye.vkcup20ii.core.R
import com.kuelye.vkcup20ii.core.utils.modifyAlpha

class BottomSheetLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    companion object {
        private val TAG = BottomSheetLayout::class.java.simpleName

        private const val EXPANDED_DEFAULT = false
        private const val SCRIM_COLOR = BLACK
        private const val SCRIM_ALPHA_MAX = 0.4f

        private const val COLLAPSED_STATE = 0f
        private const val EXPANDED_STATE = 1f
    }

    private var bottomSheet: View? = null

    private var animator: ValueAnimator? = null
    private var expanded: Boolean = EXPANDED_DEFAULT
    private var scrimAlpha: Float = 0f

    private val state: Float
        get() = (animator?.animatedValue ?: if (expanded) EXPANDED_STATE else COLLAPSED_STATE) as Float
    private val scrimBottom: Int
        get() = bottomSheet?.top ?: 0

    init {
        setWillNotDraw(false)
    }

    override fun generateLayoutParams(attrs: AttributeSet): FrameLayout.LayoutParams {
        return LayoutParams(context, attrs)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        bottomSheet = getChildAt(1)
        if (bottomSheet != null) {
            bottomSheet!!.layoutParams = (bottomSheet!!.layoutParams as LayoutParams).apply {
                gravity = BOTTOM
                setExpanded(expanded)
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateBottomSheet()
        updateScrim()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(SCRIM_COLOR.modifyAlpha(scrimAlpha))
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        return if (expanded && ev.y < scrimBottom) {
            animateExpanded(false)
            true
        } else {
            super.dispatchTouchEvent(ev)
        }
    }

    fun switch() {
        animateExpanded(!expanded)
    }

    fun dismiss() {
        animateExpanded(false)
    }

    fun animateExpanded(expanded: Boolean) {
        Log.v(TAG, "animateExpanded: expanded=$expanded")
        if (this.expanded != expanded) {
            val fromState = state
            val toState = if (expanded) EXPANDED_STATE else COLLAPSED_STATE
            this.expanded = expanded
            if (animator == null) {
                animator = ValueAnimator().apply {
                    duration = 2000
                    addUpdateListener {
                        updateBottomSheet()
                        updateScrim()
                        requestLayout()
                    }
                }
            } else {
                animator!!.cancel()
            }
            animator!!.interpolator = if (expanded) DecelerateInterpolator() else AccelerateInterpolator()
            animator!!.setFloatValues(fromState, toState)
            animator!!.start()
        }
    }

    private fun setExpanded(expanded: Boolean) {
        Log.v(TAG, "setExpanded: expanded=$expanded")
        animator?.cancel()
        animator = null
        this.expanded = expanded
        updateBottomSheet()
        updateScrim()
    }

    private fun updateScrim() {
        scrimAlpha = state * SCRIM_ALPHA_MAX
        requestLayout()
    }

    private fun updateBottomSheet() {
        if (bottomSheet != null) {
            bottomSheet!!.translationY = bottomSheet!!.measuredHeight * (1 - state)
            Log.v(TAG, "updateBottomSheet: $state, ${bottomSheet!!.translationY}")
            invalidate()
        }
    }

    private class LayoutParams(c: Context, attrs: AttributeSet) : FrameLayout.LayoutParams(c, attrs) {

        var expanded: Boolean = EXPANDED_DEFAULT

        init {
            val a = c.obtainStyledAttributes(attrs, R.styleable.BottomSheetLayout_Layout)
            for (i in 0 until a.indexCount) {
                when (val attr = a.getIndex(i)) {
                    R.styleable.BottomSheetLayout_Layout_layout_expanded -> {
                        expanded = a.getBoolean(attr, EXPANDED_DEFAULT)
                    }
                }
            }
            a.recycle()
        }

    }

}
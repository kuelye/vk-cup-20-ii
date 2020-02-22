package com.kuelye.vkcup20ii.core.ui.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color.BLACK
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.AbsSavedState.EMPTY_STATE
import android.view.GestureDetector
import android.view.Gravity.BOTTOM
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_UP
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import androidx.core.math.MathUtils.clamp
import androidx.core.view.*
import com.kuelye.vkcup20ii.core.R
import com.kuelye.vkcup20ii.core.utils.modifyAlpha
import kotlinx.android.parcel.Parcelize
import kotlin.math.absoluteValue
import kotlin.math.exp

class BottomSheetLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), NestedScrollingParent2 {

    companion object {
        private val TAG = BottomSheetLayout::class.java.simpleName

        private const val EXPANDED_DEFAULT = false
        private const val SCRIM_COLOR = BLACK
        private const val SCRIM_ALPHA_MAX = 0.4f

        private const val COLLAPSED_STATE = 0f
        private const val EXPANDED_STATE = 1f
        private const val SEPARATOR_STATE = 0.8f
    }

    var onCollapsedListener: (() -> Unit)? = null

    val expanded: Boolean
        get() = state == EXPANDED_STATE

    private var bottomSheet: View? = null
    private var gestureDetector: GestureDetectorCompat? = null
    private val scrollingParentHelper = NestedScrollingParentHelper(this)
    private var ignoreNestedScroll: Boolean = false
    private var nestedScrollStarted: Boolean = false
    private var animator: ValueAnimator? = null
    private var animatorToState: Float? = null
    private var scrimAlpha: Float = 0f
    private var state: Float = getStateByExpanded(EXPANDED_DEFAULT)
        set(value) {
            if (field != value) {
                field = value
                if (value == COLLAPSED_STATE) onCollapsedListener?.invoke()
            }
        }

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
        initializeBottomSheet()
        initializeGestures()
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
        return if (state != COLLAPSED_STATE && ev.y < scrimBottom && ev.action == ACTION_DOWN) {
            animateExpanded(false)
            true
        } else {
            if (ev.action == ACTION_UP) {
                ignoreNestedScroll = true
                if (animator?.isRunning == false) animateExpanded(getNearestExpanded(state))
            }
            gestureDetector?.onTouchEvent(ev)
            super.dispatchTouchEvent(ev)
        }
    }

    override fun onStartNestedScroll(child: View, target: View, axes: Int, type: Int): Boolean {
        nestedScrollStarted = axes and SCROLL_AXIS_VERTICAL != 0
        return nestedScrollStarted
    }

    override fun onNestedScrollAccepted(child: View, target: View, axes: Int, type: Int) {
        scrollingParentHelper.onNestedScrollAccepted(child, target, axes, type)
    }

    override fun onStopNestedScroll(target: View, type: Int) {
        scrollingParentHelper.onStopNestedScroll(target, type)
        nestedScrollStarted = false
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        //Log.v(TAG, "onNestedPreScroll: $dy, $state, $target")
        if (bottomSheet == null || ignoreNestedScroll) return
        if (dy > 0 && state != EXPANDED_STATE) {
            setScrollY(clampScrollY(bottomSheet!!.translationY - dy))
            consumed[1] = dy
        }
    }

    override fun onNestedScroll(
        target: View, dxConsumed: Int, dyConsumed: Int,
        dxUnconsumed: Int, dyUnconsumed: Int, type: Int
    ) {
        if (bottomSheet == null || ignoreNestedScroll) return
        setScrollY(clampScrollY(bottomSheet!!.translationY - dyUnconsumed))
    }

    fun switch() {
        animateExpanded(state == COLLAPSED_STATE)
    }

    fun dismiss() {
        animateExpanded(false)
    }

    fun animateExpanded(expanded: Boolean) {
        val toState = getStateByExpanded(expanded)
        //Log.v(TAG, "animateExpanded: $expanded, $state, $toState, $animatorToState")
        if (state != toState && animatorToState != toState) {
            val fromState = state
            if (animator == null) {
                animator = ValueAnimator().apply {
                    addUpdateListener {
                        state = animator!!.animatedValue as Float
                        updateBottomSheet()
                        updateScrim()
                        requestLayout()
                    }
                }
            } else {
                animator!!.cancel()
            }
            animator!!.interpolator =
                if (expanded) DecelerateInterpolator() else AccelerateInterpolator()
            animator!!.setFloatValues(fromState, toState)
            animatorToState = toState
            animator!!.start()
        }
    }

    private fun initializeBottomSheet() {
        bottomSheet = getChildAt(1)
        if (bottomSheet != null) {
            bottomSheet!!.layoutParams = (bottomSheet!!.layoutParams as LayoutParams).apply {
                gravity = BOTTOM
                setExpanded(expanded)
            }
        }
    }

    private fun initializeGestures() {
        gestureDetector = GestureDetectorCompat(context, object : GestureDetector.SimpleOnGestureListener() {
                override fun onDown(e: MotionEvent?): Boolean {
                    if (bottomSheet == null) return false
                    ignoreNestedScroll = false
                    return true
                }

                override fun onScroll(
                    e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float
                ): Boolean {
                    //Log.v(TAG, "onScroll: $distanceY, $nestedScrollStarted")
                    if (!nestedScrollStarted) setScrollY(clampScrollY(bottomSheet!!.translationY - distanceY))
                    return true
                }

                override fun onFling(
                    e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float
                ): Boolean {
                    ignoreNestedScroll = true
                    animateExpanded(velocityY < 0)
                    return true
                }
            })
    }

    private fun setExpanded(expanded: Boolean) {
        animator?.cancel()
        animatorToState = null
        state = getStateByExpanded(expanded)
        if (state == COLLAPSED_STATE) onCollapsedListener?.invoke()
        updateBottomSheet()
        updateScrim()
    }

    private fun setScrollY(scrollY: Float) {
        //Log.v(TAG, "setScrollY: $scrollY")
        if (bottomSheet == null) return
        animator?.cancel()
        animatorToState = null
        state = (bottomSheet!!.measuredHeight - scrollY.absoluteValue) / bottomSheet!!.measuredHeight
        updateBottomSheet()
        updateScrim()
    }

    private fun updateScrim() {
        scrimAlpha = state * SCRIM_ALPHA_MAX
        requestLayout()
    }

    private fun updateBottomSheet() {
        if (bottomSheet == null) return
        bottomSheet!!.translationY = bottomSheet!!.measuredHeight * (1 - state)
        invalidate()
    }

    private fun getStateByExpanded(expanded: Boolean) =
        if (expanded) EXPANDED_STATE else COLLAPSED_STATE

    private fun clampScrollY(scrollY: Float) =
        clamp(scrollY, 0f, bottomSheet!!.measuredHeight.toFloat())

    private fun getNearestExpanded(state: Float) = state > SEPARATOR_STATE

    private class LayoutParams(c: Context, attrs: AttributeSet) :
        FrameLayout.LayoutParams(c, attrs) {

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
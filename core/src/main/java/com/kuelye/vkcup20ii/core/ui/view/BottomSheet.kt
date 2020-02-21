package com.kuelye.vkcup20ii.core.ui.view

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import androidx.core.view.*
import com.kuelye.vkcup20ii.core.R

class BottomSheet @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), NestedScrollingChild2, NestedScrollingParent2 {

    companion object {
        private val TAG = BottomSheet::class.java.simpleName
    }

    private val scrollingParentHelper = NestedScrollingParentHelper(this)
    private val scrollingChildHelper = NestedScrollingChildHelper(this)

    private var toolbar: BottomSheetToolbar? = null
    private var animator: ValueAnimator? = null
    private var animatorToElevation: Float? = null

    init {
        orientation = VERTICAL
        setBackgroundResource(R.drawable.bg_bottom_sheet)
        scrollingChildHelper.isNestedScrollingEnabled = true
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        if (childCount > 0 && getChildAt(0) is BottomSheetToolbar) {
            toolbar = getChildAt(0) as BottomSheetToolbar?
        }
    }

    override fun onStartNestedScroll(child: View, target: View, axes: Int, type: Int): Boolean {
        Log.v(TAG, "onStartNestedScroll")
        return axes and ViewCompat.SCROLL_AXIS_VERTICAL != 0
    }

    override fun onNestedScrollAccepted(child: View, target: View, axes: Int, type: Int) {
        Log.v(TAG, "onNestedScrollAccepted")
        scrollingParentHelper.onNestedScrollAccepted(child, target, axes, type)
        startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL, type)
    }

    override fun onStopNestedScroll(target: View, type: Int) {
        Log.v(TAG, "onStopNestedScroll")
        scrollingParentHelper.onStopNestedScroll(target, type)
        stopNestedScroll(type)
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        Log.v(TAG, "onNestedPreScroll")
        dispatchNestedPreScroll(dx, dy, consumed, null, type)
    }

    override fun onNestedScroll(
        target: View, dxConsumed: Int, dyConsumed: Int,
        dxUnconsumed: Int, dyUnconsumed: Int, type: Int
    ) {
        Log.v(TAG, "onNestedScroll")
        scrollingChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed,
            dxUnconsumed, dyUnconsumed, null, type, null)
    }

    override fun startNestedScroll(axes: Int, type: Int): Boolean {
        Log.v(TAG, "startNestedScroll")
        return scrollingChildHelper.startNestedScroll(axes, type)
    }

    override fun stopNestedScroll(type: Int) {
        Log.v(TAG, "stopNestedScroll")
        scrollingChildHelper.stopNestedScroll(type)
    }

    override fun hasNestedScrollingParent(type: Int): Boolean {
        Log.v(TAG, "hasNestedScrollingParent")
        return scrollingChildHelper.hasNestedScrollingParent(type)
    }

    override fun dispatchNestedPreScroll(
        dx: Int, dy: Int, consumed: IntArray?, offsetInWindow: IntArray?, type: Int
    ): Boolean {
        Log.v(TAG, "dispatchNestedPreScroll: $dy, ${consumed?.contentToString()}")
        return scrollingChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow, type)
    }

    override fun dispatchNestedScroll(
        dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int,
        offsetInWindow: IntArray?, type: Int
    ): Boolean {
        Log.v(TAG, "dispatchNestedScroll: $dyConsumed, $dyUnconsumed")
        return scrollingChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed,
            dxUnconsumed, dyUnconsumed, offsetInWindow, type)
//        animateToolbarElevation(
//            if (target.canScrollVertically(-1))
//                dimen(R.dimen.padding_standard_quarter).toFloat() else 0f
//        )
        return false
    }

    private fun animateToolbarElevation(elevation: Float) {
        Log.v(TAG, "animateToolbarElevation: elevation=$elevation")
        if (toolbar == null) return
        if (animatorToElevation != elevation) {
            animatorToElevation = elevation
            if (animator == null) {
                animator = ValueAnimator().apply {
                    interpolator = DecelerateInterpolator()
                    addUpdateListener { toolbar!!.elevation = animator!!.animatedValue as Float }
                }
            } else {
                animator!!.cancel()
            }
            animator!!.setFloatValues(toolbar!!.elevation, elevation)
            animator!!.start()
        }
    }

}
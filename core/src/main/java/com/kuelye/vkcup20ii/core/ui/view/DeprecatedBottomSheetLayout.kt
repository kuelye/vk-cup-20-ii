package com.kuelye.vkcup20ii.core.ui.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color.BLACK
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.annotation.IdRes
import com.kuelye.vkcup20ii.core.R
import com.kuelye.vkcup20ii.core.utils.modifyAlpha

class DeprecatedBottomSheetLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {

    companion object {
        private val TAG = DeprecatedBottomSheetLayout::class.java.simpleName
        private const val SCRIM_COLOR = BLACK
        private const val SCRIM_ALPHA_MAX = 0.4f
    }

    private var scrimAlpha: Float = 0f

    init {
        setWillNotDraw(false)
    }

    override fun generateLayoutParams(attrs: AttributeSet): ViewGroup.LayoutParams {
        return LayoutParams(context, attrs)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.visibility != GONE) {
                val lp = child.layoutParams as LayoutParams
                child.measure(
                    getChildMeasureSpec(widthMeasureSpec, 0, lp.width),
                    getChildMeasureSpec(heightMeasureSpec, 0, lp.height)
                )
            }
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.visibility != GONE) {
                val childTop = (height - child.measuredHeight * getState(child)).toInt()
                child.layout(0, childTop, child.measuredWidth, height)
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(SCRIM_COLOR.modifyAlpha(scrimAlpha))
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        return if (isScrimVisible() && ev.y < getScrimBottom()) {
            forEachScrimChild { view -> animateVisible(view, false) }
            true
        } else {
            super.dispatchTouchEvent(ev)
        }
    }

    fun switch(@IdRes id: Int) {
        val view = findViewById<View>(id)
        if (view != null) {
            animateVisible(view, !isOpened(view))
        }
    }

    fun animateVisible(@IdRes id: Int, visible: Boolean) {
        val view = findViewById<View>(id)
        if (view != null) {
            animateVisible(view, visible)
        }
    }

    private fun animateVisible(view: View, toVisible: Boolean) {
        if (toVisible != isOpened(view)) {
            view.setTag(R.id.tag_opened, toVisible)
            var animator = getAnimator(view)
            if (animator == null) {
                animator = ValueAnimator().apply {
                    addUpdateListener {
                        requestLayout()
                        updateScrim()
                    }
                }
                view.setTag(R.id.tag_animator, animator)
            } else {
                animator.cancel()
            }
            val toState = if (toVisible) 1f else 0f
            animator.interpolator =
                if (toVisible) DecelerateInterpolator() else AccelerateInterpolator()
            animator.setFloatValues(getState(view), toState)
            animator.start()
        }
    }

    private fun updateScrim() {
        var minState: Float? = null
        forEachScrimChild { view ->
            val state = getState(view)
            if (minState == null || minState!! < getState(view)) minState = state
        }
        scrimAlpha = (minState ?: 0f) * SCRIM_ALPHA_MAX
    }

    private fun getAnimator(view: View): ValueAnimator? =
        view.getTag(R.id.tag_animator) as ValueAnimator?

    private fun isOpened(view: View): Boolean =
        (view.getTag(R.id.tag_opened) ?: false) as Boolean

    private fun getState(view: View): Float =
        (getAnimator(view)?.animatedValue ?: 0f) as Float

    private fun isScrimVisible(): Boolean {
        for (i in 0 until childCount) {
            val view = getChildAt(i)
            if ((view.layoutParams as LayoutParams).scrimEnabled && isOpened(view)) {
                return true
            }
        }
        return false
    }

    private fun getScrimBottom(): Int {
        var maxChildTop = 0
        forEachScrimChild { view -> if (view.top > maxChildTop) maxChildTop = view.top }
        return maxChildTop
    }

    private fun forEachScrimChild(block: (View) -> Unit) {
        for (i in 0 until childCount) {
            val view = getChildAt(i)
            if ((view.layoutParams as LayoutParams).scrimEnabled) {
                block.invoke(view)
            }
        }
    }

    private class LayoutParams(c: Context, attrs: AttributeSet) : MarginLayoutParams(c, attrs) {

        companion object {
            private const val SCRIM_ENABLED_DEFAULT = true
        }

        var scrimEnabled: Boolean = SCRIM_ENABLED_DEFAULT

        init {
//            val a = c.obtainStyledAttributes(attrs, R.styleable.DeprecatedBottomSheetLayout_Layout)
//            for (i in 0 until a.indexCount) {
//                when (val attr = a.getIndex(i)) {
//                    R.styleable.DeprecatedBottomSheetLayout_Layout_layout_scrimEnabled -> {
//                        scrimEnabled = a.getBoolean(attr, SCRIM_ENABLED_DEFAULT)
//                    }
//                }
//            }
//            a.recycle()
        }

    }

}
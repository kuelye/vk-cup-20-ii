package com.kuelye.vkcup20ii.core.ui.view

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.annotation.IdRes
import com.kuelye.vkcup20ii.core.R

class BottomSheetLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {

    companion object {
        private val TAG = BottomSheetLayout::class.java.simpleName
    }

    override fun generateLayoutParams(attrs: AttributeSet): ViewGroup.LayoutParams {
        return LayoutParams(context, attrs)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.visibility != View.GONE) {
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
            if (child.visibility != View.GONE) {
                val state = (getAnimator(child)?.animatedValue ?: 0f) as Float
                child.layout(
                    0, (height - child.measuredHeight * state).toInt(),
                    child.measuredWidth, height
                )
            }
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

    private fun animateVisible(view: View, visible: Boolean) {
        if (visible != isOpened(view)) {
            view.setTag(R.id.tag_opened, visible)
            var animator = getAnimator(view)
            if (animator == null) {
                animator = ValueAnimator().apply {
                    interpolator = DecelerateInterpolator()
                    duration = 2000
                    addUpdateListener { requestLayout() }
                }
                view.setTag(R.id.tag_animator, animator)
            } else {
                animator.cancel()
            }
            val stateTo = if (visible) 1f else 0f
            animator.setFloatValues(1 - stateTo, stateTo)
            animator.start()
        }
    }

    private fun getAnimator(view: View): ValueAnimator? =
        view.getTag(R.id.tag_animator) as ValueAnimator?

    private fun isOpened(view: View): Boolean =
        (view.getTag(R.id.tag_opened) ?: false) as Boolean

    private class LayoutParams : MarginLayoutParams {

        companion object {
            private const val SCRIM_ENABLED_DEFAULT = true
        }

        var scrimEnabled: Boolean = SCRIM_ENABLED_DEFAULT

        constructor(c: Context, attrs: AttributeSet) : super(c, attrs) {
            val a = c.obtainStyledAttributes(attrs, R.styleable.BottomSheetLayout_Layout)
            for (i in 0..a.indexCount) {
                when (val attr = a.getIndex(i)) {
                    R.styleable.BottomSheetLayout_Layout_layout_scrimEnabled -> {
                        scrimEnabled = a.getBoolean(attr, SCRIM_ENABLED_DEFAULT)
                    }
                }
            }
            a.recycle()
        }

    }

}
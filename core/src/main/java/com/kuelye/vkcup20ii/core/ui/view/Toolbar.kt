package com.kuelye.vkcup20ii.core.ui.view

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.MeasureSpec.EXACTLY
import android.view.View.MeasureSpec.makeMeasureSpec
import android.view.animation.DecelerateInterpolator
import android.widget.RelativeLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.math.MathUtils.clamp
import androidx.core.view.ViewCompat
import androidx.core.view.ViewCompat.TYPE_NON_TOUCH
import androidx.core.view.ViewCompat.TYPE_TOUCH
import com.kuelye.vkcup20ii.core.R
import com.kuelye.vkcup20ii.core.utils.dimen
import com.kuelye.vkcup20ii.core.utils.themeDimen
import kotlinx.android.synthetic.main.layout_toolbar.view.*

class Toolbar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr), CoordinatorLayout.AttachedBehavior {

    companion object {
        private val TAG = Toolbar::class.java.simpleName
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_toolbar, this, true)
    }

    var title: String? = null
        set(value) {
            field = value
            titleTextView.text = value
        }

    var subtitle: String? = null
        set(value) {
            field = value
            subtitleTextView.text = value
        }

    val scrollingOffset: Int
        get() = if (actualHeight == null) height else actualHeight!!

    private val collapsedHeight: Int = themeDimen(android.R.attr.actionBarSize)
    private var expandedHeight: Int = collapsedHeight

    private val paddingStandard: Int = dimen(R.dimen.padding_standard)

    private var actualHeight: Int? = null
        set(value) {
            field = value
            update()
            requestLayout()
        }

    private var animator: ValueAnimator? = null
//    private val expandedHeightState: Float
//        get() = if (expandedHeight == collapsedHeight) 0f else (actualHeight - collapsedHeight).toFloat() / (expandedHeight - collapsedHeight)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(
            widthMeasureSpec,
            if (actualHeight == null) heightMeasureSpec else makeMeasureSpec(
                actualHeight!!,
                EXACTLY
            )
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (oldh != h && actualHeight == null) {
            expandedHeight = h
        }
    }

    private fun scroll(dy: Int): Int {
        return if (dy == 0) {
            0
        } else {
            if (actualHeight == null) actualHeight = height
            val actualHeight = clamp(actualHeight!! - dy, collapsedHeight, expandedHeight)
            val consumed = this.actualHeight!! - actualHeight
            this.actualHeight = actualHeight
            consumed
        }
    }

    private fun animate(targetHeight: Int) {
        if (actualHeight != null) {
            if (animator == null) {
                animator = ValueAnimator().apply {
                    interpolator = DecelerateInterpolator()
                    addUpdateListener { actualHeight = animator!!.animatedValue as Int? }
                }
            } else {
                animator!!.cancel()
            }

            animator!!.setIntValues(actualHeight!!, targetHeight)
            animator!!.start()
        }
    }

    private fun update() {
        val state = when {
            expandedHeight == collapsedHeight -> 0f
            actualHeight == null -> 1f
            else -> (actualHeight!! - collapsedHeight).toFloat() / (expandedHeight - collapsedHeight)
        }
        titleTextView.setPadding(0, (state * paddingStandard + paddingStandard).toInt(), 0, 0)
        subtitleTextView.alpha = state
        subtitleTextView.setPadding(
            paddingStandard * 2, (state * paddingStandard / 2).toInt(),
            paddingStandard * 2, 0
        )
        titleTextView.elevation = (1 - state) * paddingStandard / 4
    }

    private fun getTargetHeightByVelocityY(velocityY: Float) =
        if (velocityY < 0) expandedHeight else collapsedHeight

    private fun getNearestTargetHeight() =
        if (actualHeight == null || expandedHeight - actualHeight!! > actualHeight!! - collapsedHeight) collapsedHeight else expandedHeight

    override fun getBehavior(): CoordinatorLayout.Behavior<*> = Behavior()

    class ScrollingViewBehavior(
        context: Context, attrs: AttributeSet
    ) : CoordinatorLayout.Behavior<View>(context, attrs) {

        private var layoutTop: Int = 0
        private var offsetTop: Int = 0

        override fun onLayoutChild(
            parent: CoordinatorLayout, child: View, layoutDirection: Int
        ): Boolean {
            parent.onLayoutChild(child, layoutDirection)
            layoutTop = child.top
            return false
        }

        override fun layoutDependsOn(
            parent: CoordinatorLayout, child: View, dependency: View
        ): Boolean {
            return dependency is Toolbar
        }

        override fun onDependentViewChanged(
            parent: CoordinatorLayout, child: View, dependency: View
        ): Boolean {
            offsetTop = (dependency as Toolbar).scrollingOffset
            updateOffsetTop(child)
            return false
        }

        private fun updateOffsetTop(child: View) {
            child.translationY = (offsetTop - child.top - layoutTop).toFloat()
        }

    }

    private class Behavior : CoordinatorLayout.Behavior<Toolbar>() {

        private var flingVelocityY: Float? = null

        override fun onStartNestedScroll(
            coordinatorLayout: CoordinatorLayout, child: Toolbar, directTargetChild: View,
            target: View, axes: Int, type: Int
        ): Boolean {
            val started = axes and SCROLL_AXIS_VERTICAL != 0
            if (started && type == TYPE_TOUCH) child.animator?.cancel()
            return started
        }

        override fun onNestedPreScroll(
            coordinatorLayout: CoordinatorLayout, child: Toolbar, target: View,
            dx: Int, dy: Int, consumed: IntArray, type: Int
        ) {
            if (dy > 0) consumed[1] = child.scroll(dy)
        }

        override fun onNestedScroll(
            coordinatorLayout: CoordinatorLayout, child: Toolbar, target: View,
            dxConsumed: Int, dyConsumed: Int,
            dxUnconsumed: Int, dyUnconsumed: Int, type: Int
        ) {
            if (dyUnconsumed < 0) child.scroll(dyUnconsumed)
        }

        override fun onNestedFling(
            coordinatorLayout: CoordinatorLayout,
            child: Toolbar,
            target: View,
            velocityX: Float,
            velocityY: Float,
            consumed: Boolean
        ): Boolean {
            if (consumed) flingVelocityY = velocityY
            return false
        }

        override fun onStopNestedScroll(
            coordinatorLayout: CoordinatorLayout,
            child: Toolbar,
            target: View,
            type: Int
        ) {
            if (flingVelocityY == null && type == TYPE_TOUCH) child.animate(child.getNearestTargetHeight())
            if (flingVelocityY != null && type == TYPE_NON_TOUCH) {
                child.animate(child.getTargetHeightByVelocityY(flingVelocityY!!))
                flingVelocityY = null
            }
        }

    }


}
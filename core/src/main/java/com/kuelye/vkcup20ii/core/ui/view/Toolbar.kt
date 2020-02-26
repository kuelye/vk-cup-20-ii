package com.kuelye.vkcup20ii.core.ui.view

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Outline
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.MeasureSpec.EXACTLY
import android.view.View.MeasureSpec.makeMeasureSpec
import android.view.ViewOutlineProvider
import android.view.animation.DecelerateInterpolator
import android.widget.RelativeLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.math.MathUtils.clamp
import androidx.core.view.ViewCompat.TYPE_NON_TOUCH
import androidx.core.view.ViewCompat.TYPE_TOUCH
import com.kuelye.vkcup20ii.core.R
import com.kuelye.vkcup20ii.core.utils.dimen
import com.kuelye.vkcup20ii.core.utils.themeDimen
import com.kuelye.vkcup20ii.core.utils.themeDrawable
import kotlinx.android.synthetic.main.view_toolbar.view.*

class Toolbar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr), CoordinatorLayout.AttachedBehavior {

    companion object {
        private val TAG = Toolbar::class.java.simpleName
        const val COLLAPSED_STATE = 0f
        const val EXPANDED_STATE = 1f
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

    var onExpandedStateChangedListener: ((Float) -> Unit)? = null

    private val collapsedHeight: Int = themeDimen(android.R.attr.actionBarSize)
    private var expandedHeight: Int = collapsedHeight

    private val paddingStandard: Int = dimen(R.dimen.padding_standard)

    private var actualHeight: Int? = null
        set(value) {
            field = value
            update()
        }

    private var stateAnimator: ValueAnimator? = null

    private var targetElevation: Int = 0
    private var elevationAnimator: ValueAnimator? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.view_toolbar, this, true)
        background = themeDrawable(android.R.attr.windowBackground)
        initializeAttrs(attrs)
        if (subtitle == null) actualHeight = collapsedHeight
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(
            widthMeasureSpec,
            if (actualHeight == null) heightMeasureSpec else makeMeasureSpec(actualHeight!!, EXACTLY)
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        outlineProvider = OutlineProvider(w, h)
        if (oldh != h && actualHeight == null) expandedHeight = h
        update()
    }

    @SuppressLint("PrivateResource")
    private fun initializeAttrs(attrs: AttributeSet?) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.Toolbar)
        for (i in 0 until a.indexCount) {
            when (val attr = a.getIndex(i)) {
                R.styleable.Toolbar_title -> title = a.getString(attr)
                R.styleable.Toolbar_subtitle -> subtitle = a.getString(attr)
            }
        }
        a.recycle()
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
        //Log.v(TAG, "animate: targetHeight=$targetHeight")
        if (actualHeight != null) {
            if (stateAnimator == null) {
                stateAnimator = ValueAnimator().apply {
                    interpolator = DecelerateInterpolator()
                    addUpdateListener { actualHeight = stateAnimator!!.animatedValue as Int }
                }
            } else {
                stateAnimator!!.cancel()
            }

            stateAnimator!!.setIntValues(actualHeight!!, targetHeight)
            stateAnimator!!.start()
        }
    }

    private fun animateElevation(elevated: Boolean) =
        animateElevation(if (elevated) paddingStandard / 4 else 0)

    private fun animateElevation(targetElevation: Int) {
        if (this.targetElevation != targetElevation) {
            this.targetElevation = targetElevation
            if (elevationAnimator == null) {
                elevationAnimator = ValueAnimator().apply {
                    interpolator = DecelerateInterpolator()
                    addUpdateListener { elevation = elevationAnimator!!.animatedValue as Float }
                }
            } else {
                elevationAnimator!!.cancel()
            }
            elevationAnimator!!.apply {
                setFloatValues(elevation, targetElevation.toFloat())
                start()
            }
        }
    }

    private fun update() {
        val s = when {
            expandedHeight == collapsedHeight -> COLLAPSED_STATE
            actualHeight == null -> EXPANDED_STATE
            else -> (actualHeight!! - collapsedHeight).toFloat() / (expandedHeight - collapsedHeight)
        }

        if (subtitle != null) animateElevation(s != EXPANDED_STATE)

        val tX = ((measuredWidth - titleTextView.measuredWidth) / 2 - paddingStandard) * s + paddingStandard
        val tH = com.kuelye.vkcup20ii.core.utils.getHeight(titleTextView.paint, title)
        val pT = ((collapsedHeight - tH) / 2 - paddingStandard) * s + paddingStandard
        titleTextView.setPadding(0, pT.toInt(), 0, 0)
        titleTextView.translationX = tX

        subtitleTextView.alpha = s
        subtitleTextView.setPadding(
            paddingStandard * 2, (s * paddingStandard / 2).toInt(),
            paddingStandard * 2, 0
        )

        onExpandedStateChangedListener?.invoke(s)
        requestLayout()
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
        private var ignoreScroll: Boolean = false

        override fun onStartNestedScroll(
            coordinatorLayout: CoordinatorLayout, child: Toolbar, directTargetChild: View,
            target: View, axes: Int, type: Int
        ): Boolean {
            val started = axes and SCROLL_AXIS_VERTICAL != 0
            if (started && type == TYPE_TOUCH) {
                child.stateAnimator?.cancel()
                ignoreScroll = false
                flingVelocityY = null
            }
            return started
        }

        override fun onNestedPreScroll(
            coordinatorLayout: CoordinatorLayout, child: Toolbar, target: View,
            dx: Int, dy: Int, consumed: IntArray, type: Int
        ) {
            if (!ignoreScroll && dy > 0) {
                consumed[1] = child.scroll(dy)
            }
        }

        override fun onNestedScroll(
            coordinatorLayout: CoordinatorLayout, child: Toolbar, target: View,
            dxConsumed: Int, dyConsumed: Int,
            dxUnconsumed: Int, dyUnconsumed: Int, type: Int
        ) {
            if (child.subtitle == null) {
                child.animateElevation(dyConsumed != 0 || dyUnconsumed > 0)
            } else if (!ignoreScroll && dyUnconsumed < 0) {
                child.scroll(dyUnconsumed)
            }
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
            return true
        }

        override fun onStopNestedScroll(
            coordinatorLayout: CoordinatorLayout,
            child: Toolbar,
            target: View,
            type: Int
        ) {
            if (!ignoreScroll) {
                if (flingVelocityY == null && type == TYPE_TOUCH) {
                    child.animate(child.getNearestTargetHeight())
                } else if (flingVelocityY != null && type == TYPE_NON_TOUCH) {
                    if (!target.canScrollVertically(-1)) {
                        child.animate(child.getTargetHeightByVelocityY(flingVelocityY!!))
                    }
                }
            }
        }

    }

    private class OutlineProvider(val width: Int, val height: Int) : ViewOutlineProvider() {

        override fun getOutline(view: View, outline: Outline) {
            outline.setRect(0, 0, width, height)
        }

    }


}
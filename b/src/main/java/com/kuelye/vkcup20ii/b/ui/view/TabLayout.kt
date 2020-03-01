package com.kuelye.vkcup20ii.b.ui.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.Paint.Style.FILL
import android.graphics.Typeface
import android.graphics.Typeface.NORMAL
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity.CENTER
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout
import android.widget.TextView
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.kuelye.vkcup20ii.b.R
import com.kuelye.vkcup20ii.core.utils.color
import com.kuelye.vkcup20ii.core.utils.dimen
import com.kuelye.vkcup20ii.core.utils.themeDrawable
import com.vk.api.sdk.utils.VKUtils.dp

@ViewPager.DecorView
class TabLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    companion object {
        private val TAG = TabLayout::class.java.simpleName
        private val UNDERLINE_HEIGHT = dp(2)
    }

    var onTabSelectedListener: ((Int) -> Unit)? = null
    var adapter: PagerAdapter? = null

    private val paddingStandard = dimen(R.dimen.padding_standard)
    private var animator: ValueAnimator? = null
    private var animatorTargetState: Float? = null

    private val underlinePaint = Paint().apply {
        style = FILL
        flags = ANTI_ALIAS_FLAG
        color = color(R.color.map_tab_underline_color)
    }

    private var underlineState = 0f
        set(value) {
            if (field != value) {
                field = value
                invalidate()
            }
        }

    init {
        setWillNotDraw(false)
    }

    fun setupWithViewPager(viewPager: ViewPager) {
        if (viewPager.adapter == null) return
        adapter = viewPager.adapter
        val count = adapter!!.count
        for (i in 0 until count) {
            TextView(context).apply {
                layoutParams = LayoutParams(0, MATCH_PARENT).apply { weight = 1f }
                text = adapter!!.getPageTitle(i)
                textSize = 16f
                setTextColor(color(R.color.title_color))
                typeface = Typeface.create("sans-serif-medium", NORMAL)
                gravity = CENTER
                background = themeDrawable(android.R.attr.selectableItemBackgroundBorderless)
                setOnClickListener { select(i.toFloat()) }
                addView(this)
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (adapter == null) return
        val tabWidth = width / adapter!!.count
        val underlineSize = tabWidth - paddingStandard * 2
        val underlineCenter = underlineState * tabWidth + tabWidth / 2
        val left = underlineCenter - underlineSize / 2
        val bottom = (height - paddingStandard / 2).toFloat()
        canvas.drawRect(left, bottom - UNDERLINE_HEIGHT,
            left + underlineSize, bottom, underlinePaint)
    }

    fun select(page: Float, animate: Boolean = true) {
        Log.v(TAG, "select: $page, $animate")
        if (adapter == null) return
        if (animate) {
            if (animatorTargetState != page) {
                onTabSelectedListener?.invoke(page.toInt())
                animatorTargetState = page
                if (animator == null) {
                    animator = ValueAnimator().apply {
                        interpolator = FastOutSlowInInterpolator()
                        addUpdateListener {
                            underlineState = animator!!.animatedValue as Float
                        }
                    }
                } else {
                    animator!!.cancel()
                }
                animator!!.apply {
                    setFloatValues(underlineState, page)
                    start()
                }
            }
        } else {
            animator?.cancel()
            underlineState = page
        }
    }

}
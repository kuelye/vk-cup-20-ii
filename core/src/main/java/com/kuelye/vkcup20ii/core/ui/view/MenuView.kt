package com.kuelye.vkcup20ii.core.ui.view

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.RippleDrawable
import android.os.Build.VERSION.SDK_INT
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity.CENTER
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.annotation.DrawableRes
import androidx.core.view.forEach
import com.kuelye.vkcup20ii.core.R
import com.kuelye.vkcup20ii.core.utils.dimen
import com.kuelye.vkcup20ii.core.utils.themeColor
import com.kuelye.vkcup20ii.core.utils.themeDimen

class MenuView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    companion object {
        private val TAG = MenuView::class.java.simpleName
    }

    var onItemClickListener: ((Int) -> Unit)? = null

    private val itemSize = themeDimen(R.attr.actionBarSize)
    private val highlightColor = themeColor(R.attr.colorControlHighlight)

    val realWidth: Int
        get() {
            var width = 0f
            forEach { view -> width += view.width * view.scaleX }
            return width.toInt()
        }

    init {
        gravity = CENTER
        setPadding(dimen(R.dimen.padding_standard_half), 0, dimen(R.dimen.padding_standard_half), 0)
    }

    fun setMenu(vararg items: Item) {
        val itemsSize = items.size
        Log.v(TAG, "setMenu: $childCount, $itemsSize")
        for (i in childCount downTo (itemsSize + 1)) removeItem(childCount - i)
        for (i in childCount until itemsSize) addItem()
        for (i in 0 until itemsSize) updateItem(getNotRemovedItem(i) as ImageView, items[i])
    }

    private fun addItem() {
        Log.v(TAG, "addItem")
        ImageView(context).apply {
            layoutParams = LayoutParams(itemSize, itemSize)
            scaleType = ImageView.ScaleType.CENTER
            val selectableBackground = RippleDrawable(ColorStateList.valueOf(highlightColor), null, null)
            if (SDK_INT >= 23) selectableBackground.radius = itemSize / 2
            background = selectableBackground
            imageTintList = ColorStateList.valueOf(themeColor(R.attr.colorAccent))
            alpha = 0f
            scaleX = 0f
            scaleY = 0f
            addView(this, 0)

            animate()
                .alpha(1f).scaleX(1f).scaleY(1f)
                .start()
        }
    }

    private fun removeItem(i: Int) {
        val view = getChildAt(i)
        view.animate()
            .alpha(0f).scaleX(0f).scaleY(0f)
            .withEndAction { removeView(view) }
            .start()
        view.tag = true
        Log.v(TAG, "removeItem: $i")
    }

    private fun getNotRemovedItem(i: Int): View? {
        var k = 0
        forEach { view ->
            if (view.tag != true && k++ == i) {
                return view
            }
        }
        return null
    }

    private fun updateItem(view: ImageView, item: Item) {
        view.setImageResource(item.icon)
        view.setOnClickListener {
            onItemClickListener?.invoke(item.id)
        }
    }

    class Item(
        @DrawableRes val icon: Int,
        val id: Int,
        val navigation: Boolean = false
    )

}
package com.kuelye.vkcup20ii.core.ui.view

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.RippleDrawable
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.util.AttributeSet
import android.view.Gravity.CENTER
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.annotation.DrawableRes
import com.kuelye.vkcup20ii.core.R
import com.kuelye.vkcup20ii.core.utils.dimen
import com.kuelye.vkcup20ii.core.utils.themeColor
import com.kuelye.vkcup20ii.core.utils.themeDimen

class MenuView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    var onItemClickListener: ((Int) -> Unit)? = null

    private val itemSize = themeDimen(R.attr.actionBarSize)
    private val highlightColor = themeColor(R.attr.colorControlHighlight)

    init {
        gravity = CENTER
        setPadding(0, 0, dimen(R.dimen.padding_standard), 0)
    }

    fun setMenu(vararg items: Item) {
        val itemsSize = items.size
        for (i in itemsSize until childCount) removeViewAt(i)
        for (i in childCount until itemsSize) addItem()
        for (i in 0 until itemsSize) updateItem(getChildAt(i) as ImageView, items!![i])
    }

    private fun addItem() {
        ImageView(context).apply {
            layoutParams = LayoutParams(itemSize, itemSize)
            scaleType = ImageView.ScaleType.CENTER
            val selectableBackground = RippleDrawable(ColorStateList.valueOf(highlightColor), null, null)
            if (SDK_INT >= 23) selectableBackground.radius = itemSize / 2
            background = selectableBackground
            imageTintList = ColorStateList.valueOf(themeColor(R.attr.colorAccent))
            addView(this)
        }
    }

    private fun updateItem(view: ImageView, item: Item) {
        view.setImageResource(item.icon)
        view.setOnClickListener {
            onItemClickListener?.invoke(item.id)
        }
    }

    class Item(
        @DrawableRes val icon: Int,
        val id: Int
    )

}
package com.kuelye.vkcup20ii.core.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import com.kuelye.vkcup20ii.core.R
import com.kuelye.vkcup20ii.core.utils.dimen
import kotlinx.android.synthetic.main.view_bottom_sheet_toolbar.view.*

class BottomSheetToolbar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    companion object {
        private val TAG = BottomSheetToolbar::class.java.simpleName
    }

    var title: String? = null
        set(value) {
            field = value
            titleTextView.text = value
        }
    val dismissImageView: ImageView

    init {
        View.inflate(context, R.layout.view_bottom_sheet_toolbar, this)
        minimumHeight = dimen(R.dimen.bottom_sheet_toolbar_height)
        setBackgroundResource(R.drawable.bg_bottom_sheet)
        isClickable = true
        dismissImageView = findViewById(R.id.dismissImageView)
    }

}
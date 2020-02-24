package com.kuelye.vkcup20ii.b.ui.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import com.kuelye.vkcup20ii.b.R
import com.kuelye.vkcup20ii.core.model.VKAddress
import com.kuelye.vkcup20ii.core.model.VKGroup
import com.kuelye.vkcup20ii.core.ui.view.BottomSheet
import com.kuelye.vkcup20ii.core.utils.open
import kotlinx.android.synthetic.main.layout_map_group_address_info.view.*

class MapGroupAddressInfoView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BottomSheet(context, attrs, defStyleAttr) {

    companion object {
        private val TAG = MapGroupAddressInfoView::class.java.simpleName
    }

    private var group: VKGroup? = null
    private var address: VKAddress? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_map_group_address_info, this, true)
        background = context.resources.getDrawable(R.drawable.bg_bottom_sheet, context.theme)
        isClickable = true
        isFocusable = true
    }

    fun setGroupAddress(group: VKGroup, address: VKAddress) {
        this.group = group
        this.address = address
        update()
    }

    private fun update() {
        if (group == null || address == null) {
            // TODO
        } else {
            bottomSheetToolbar.title = group!!.name
            addressTextView.text = address!!.formattedAddress
            updateDescription(group!!)
            openButton.setOnClickListener { open(context, group!!) }
        }
    }

    private fun updateDescription(group: VKGroup) {
        if (group.description.isNullOrBlank()) {
            descriptionLayout.visibility = GONE
        } else {
            descriptionLayout.visibility = VISIBLE
            descriptionTextView.text = group.description
        }
    }

}
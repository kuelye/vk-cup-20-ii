package com.kuelye.vkcup20ii.f.ui.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.kuelye.vkcup20ii.core.utils.dimen
import com.kuelye.vkcup20ii.core.utils.themeColor
import com.kuelye.vkcup20ii.f.R
import com.kuelye.vkcup20ii.f.model.VKGroup
import com.vk.api.sdk.VK
import kotlinx.android.synthetic.main.layout_group_info.view.*
import java.lang.Math.round
import kotlin.math.floor

class GroupInfoView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    companion object {
        private val TAG = GroupInfoView::class.java.simpleName
    }

    var group: VKGroup? = null
        set(value) {
            field = value
            updateByGroup()
        }

    private val paddingStandard = dimen(R.dimen.padding_standard)

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_group_info, this, true)
        background = context.resources.getDrawable(R.drawable.bg_bottom_sheet, context.theme)
        setPadding(paddingStandard, 0, paddingStandard, 0)
        orientation = VERTICAL
        isClickable = true
        isFocusable = true
    }

    private fun updateByGroup() {
        if (group == null) {
            // TODO
        } else {
            nameTextView.text = group!!.name
            membersTextView.text = formatMembers(group!!)
            descriptionTextView.text = group!!.description
        }
    }

    private fun formatMembers(group: VKGroup): String {
        Log.v(TAG, "formatMembers: ${group.membersCount}")
        val membersCount = if (group.membersCount < 1000) {
            "${group.membersCount}"
        } else if (group.membersCount < 1000000) {
            "${formatShort(group.membersCount.toFloat() / 1000)}K"
        } else {
            "${group.membersCount / 1000000}M"
        }
        return String.format(context.getString(R.string.info_members_template), membersCount, "0")
    }

    private fun formatShort(value: Float): String =
        "%.1f".format(floor(value * 10) / 10).replace(".0", "")

}
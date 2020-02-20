package com.kuelye.vkcup20ii.f.ui.view

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.kuelye.vkcup20ii.core.utils.dimen
import com.kuelye.vkcup20ii.core.utils.formatTime
import com.kuelye.vkcup20ii.f.R
import com.kuelye.vkcup20ii.f.model.VKGroup
import com.kuelye.vkcup20ii.f.model.VKGroup.Companion.NO_POSTS_DATE
import kotlinx.android.synthetic.main.layout_group_info.view.*
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
            updateDescription(group!!)
            updateLastPost(group!!)
            openButton.setOnClickListener {
                val uri: Uri = Uri.parse("http://vk.com/${group!!.screenName}")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                context.startActivity(intent)
            }
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

    private fun updateLastPost(group: VKGroup) {
        if (group.lastPostDate == null) {
            lastPostTextView.visibility = GONE
        } else {
            lastPostTextView.visibility = VISIBLE
            if (group.lastPostDate == NO_POSTS_DATE) {
                lastPostTextView.text = context.getString(R.string.info_no_posts)
            } else {
                lastPostTextView.text = String.format(
                    context.getString(
                        R.string.info_last_post_template,
                        formatTime(context, group.lastPostDate!!)
                    )
                )
            }
        }
    }

    private fun formatMembers(group: VKGroup): String {
        val membersCount = when {
            group.membersCount < 1000 -> "${group.membersCount}"
            group.membersCount < 1000000 -> "${formatShort(group.membersCount.toFloat() / 1000)}K"
            else -> "${group.membersCount / 1000000}M"
        }
        return String.format(
            context.getString(R.string.info_members_template),
            membersCount,
            group.friendsCount
        )
    }

    private fun formatShort(value: Float): String =
        "%.1f".format(floor(value * 10) / 10).replace(".0", "")

}
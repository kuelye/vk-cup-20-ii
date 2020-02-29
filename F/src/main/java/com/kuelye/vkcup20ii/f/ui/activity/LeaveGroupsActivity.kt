package com.kuelye.vkcup20ii.f.ui.activity

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kuelye.vkcup20ii.core.Config
import com.kuelye.vkcup20ii.core.data.GroupRepository
import com.kuelye.vkcup20ii.core.model.groups.VKGroup
import com.kuelye.vkcup20ii.core.model.groups.VKGroup.Field.DESCRIPTION
import com.kuelye.vkcup20ii.core.model.groups.VKGroup.Field.MEMBERS_COUNT
import com.kuelye.vkcup20ii.core.ui.activity.BaseActivity
import com.kuelye.vkcup20ii.core.ui.misc.SpaceItemDecoration
import com.kuelye.vkcup20ii.core.ui.view.Toolbar.Companion.COLLAPSED_STATE
import com.kuelye.vkcup20ii.core.ui.view.Toolbar.Companion.EXPANDED_STATE
import com.kuelye.vkcup20ii.core.utils.dimen
import com.kuelye.vkcup20ii.f.R
import com.kuelye.vkcup20ii.f.ui.view.SelectableBorderImageView
import com.squareup.picasso.Picasso
import com.vk.api.sdk.VKApiCallback
import com.vk.api.sdk.auth.VKScope.GROUPS
import com.vk.api.sdk.utils.VKUtils
import com.vk.api.sdk.utils.VKUtils.dp
import kotlinx.android.synthetic.main.activity_leave_group.*
import kotlin.math.floor

class LeaveGroupsActivity : BaseActivity() {

    companion object {
        private val TAG = LeaveGroupsActivity::class.java.simpleName
        private const val EXTRA_SELECTED_GROUPS_IDS = "SELECTED_GROUPS_IDS"
        private val GROUP_EXTENDED_FIELDS = arrayOf(DESCRIPTION, MEMBERS_COUNT)
    }

    private lateinit var adapter: Adapter
    private val selectedGroupsIds = mutableSetOf<Int>()

    init {
        Config.scopes = listOf(GROUPS)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leave_group)

        if (savedInstanceState != null) {
            val ids = savedInstanceState.getIntArray(EXTRA_SELECTED_GROUPS_IDS)
            if (ids != null) selectedGroupsIds.addAll(ids.toTypedArray())
        }

        initializeLayout()
        updateLeaveLayout()
    }

    override fun onLogin() {
        super.onLogin()
        requestGroups()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putIntArray(EXTRA_SELECTED_GROUPS_IDS, selectedGroupsIds.toIntArray())
    }

    private fun requestGroups() {
        GroupRepository.getGroups(
            GROUP_EXTENDED_FIELDS,
            null,
            object : VKApiCallback<List<VKGroup>> {
                override fun success(result: List<VKGroup>) {
                    adapter.groups = result
                }

                override fun fail(error: Exception) {
                    Log.e(TAG, "requestGroups>fail", error) // TODO
                }
            })
    }

    private fun requestGroup(groupId: Int) {
        GroupRepository.getGroup(groupId, object : VKApiCallback<VKGroup?> {
            override fun success(result: VKGroup?) {
                updateGroupInfoLayout(result)
            }

            override fun fail(error: Exception) {
                Log.e(TAG, "requestGroup>fail", error) // TODO
            }
        })
    }

    private fun initializeLayout() {
        updateToolbarTitle(EXPANDED_STATE)
        toolbar.onExpandedStateChangedListener = { state -> updateToolbarTitle(state) }

        val paddingStandard = dimen(this, R.dimen.padding_standard)
        val totalWidth = VKUtils.width(this) - paddingStandard * 2
        val itemWidth = dimen(this, R.dimen.group_item_width)
        val spanCount = floor((totalWidth + paddingStandard * .5) / (itemWidth + paddingStandard * .5)).toInt()
        val horizontalSpace = (totalWidth - spanCount * itemWidth) / (spanCount - 1)

        adapter = Adapter(this)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = GridLayoutManager(this, spanCount)
        recyclerView.addItemDecoration(SpaceItemDecoration(horizontalSpace, dp(12), spanCount))
    }

    private fun updateLeaveLayout() {
        bottomSheetLayout.animateExpanded(selectedGroupsIds.isNotEmpty())
    }

    private fun updateToolbarTitle(expandedState: Float) {
        toolbar.title = getString(if (expandedState == COLLAPSED_STATE) R.string.leave_title_collapsed else R.string.leave_title_expanded)
    }

    private fun updateGroupInfoLayout(group: VKGroup?) {
        //Log.v(TAG, "GUB updateGroupInfoLayout: group=$group")
        groupInfoLayout.group = group
    }

    private inner class Adapter(context: Context) : RecyclerView.Adapter<Adapter.ViewHolder>() {

        private val layoutInflater: LayoutInflater = LayoutInflater.from(context)

        var groups: List<VKGroup>? = null
            set(value) {
                field = value
                notifyDataSetChanged()
            }

        override fun getItemCount(): Int = groups?.size ?: 0

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(layoutInflater.inflate(R.layout.layout_group, parent, false))

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val group = groups!![position]
            holder.fill(group)
            updateBySelected(holder, group, false)

            holder.itemView.setOnClickListener {
                if (selectedGroupsIds.contains(group.id)) {
                    selectedGroupsIds.remove(group.id)
                } else {
                    selectedGroupsIds.add(group.id)
                }
                updateLeaveLayout()
                updateBySelected(holder, group, true)
            }

            holder.itemView.setOnLongClickListener {
                bottomSheetLayout.animateExpanded(true)
                requestGroup(group.id)
                true
            }
        }

        private fun updateBySelected(holder: ViewHolder, group: VKGroup, animate: Boolean) {
            holder.setSelected(selectedGroupsIds.contains(group.id), animate)
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            private val nameTextView =
                itemView.findViewById<TextView>(R.id.nameTextView)
            private val photoImageView =
                itemView.findViewById<SelectableBorderImageView>(R.id.photoImageView)

            fun setSelected(selected: Boolean, animate: Boolean) {
                if (animate) {
                    photoImageView.animateSelected(selected)
                } else {
                    photoImageView.isSelected = selected
                }
            }

            fun fill(group: VKGroup) {
                Picasso.get().load(group.photo200)
                    .placeholder(ColorDrawable(PLACEHOLDER_COLOR))
                    .error(ColorDrawable(PLACEHOLDER_COLOR))
                    .into(photoImageView)
                nameTextView.text = group.name
            }

        }

    }

}

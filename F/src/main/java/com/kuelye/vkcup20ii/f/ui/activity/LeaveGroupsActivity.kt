package com.kuelye.vkcup20ii.f.ui.activity

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kuelye.vkcup20ii.core.Config
import com.kuelye.vkcup20ii.core.data.BaseRepository
import com.kuelye.vkcup20ii.core.data.BaseRepository.Source.ANY
import com.kuelye.vkcup20ii.core.data.BaseRepository.Source.CACHE
import com.kuelye.vkcup20ii.core.data.GroupRepository
import com.kuelye.vkcup20ii.core.model.groups.VKGroup
import com.kuelye.vkcup20ii.core.model.groups.VKGroup.Field.DESCRIPTION
import com.kuelye.vkcup20ii.core.model.groups.VKGroup.Field.MEMBERS_COUNT
import com.kuelye.vkcup20ii.core.ui.activity.BaseRecyclerActivity
import com.kuelye.vkcup20ii.core.ui.view.Toolbar.Companion.COLLAPSED_STATE
import com.kuelye.vkcup20ii.core.utils.PLACEHOLDER_COLOR
import com.kuelye.vkcup20ii.f.R
import com.kuelye.vkcup20ii.f.ui.view.SelectableBorderImageView
import com.squareup.picasso.Picasso
import com.vk.api.sdk.VKApiCallback
import com.vk.api.sdk.auth.VKScope.GROUPS
import kotlinx.android.synthetic.main.activity_leave_group.*

class LeaveGroupsActivity : BaseRecyclerActivity<VKGroup, LeaveGroupsActivity.Adapter>() {

    companion object {
        private val TAG = LeaveGroupsActivity::class.java.simpleName
        private const val EXTRA_SELECTED_GROUPS_IDS = "SELECTED_GROUPS_IDS"
        private val GROUP_EXTENDED_FIELDS = listOf(DESCRIPTION, MEMBERS_COUNT)
    }

    private val selectedGroupsIds = mutableSetOf<Int>()

    init {
        Config.scopes = listOf(GROUPS)
    }

    private var groupsListener: BaseRepository.Listener<VKGroup>? = null

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

    override fun onResume() {
        super.onResume()
        subscribeGroups()
    }

    override fun onPause() {
        super.onPause()
        unsubscribeGroups()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putIntArray(EXTRA_SELECTED_GROUPS_IDS, selectedGroupsIds.toIntArray())
    }

    override fun initializeLayout() {
        adapter = Adapter(this)
        layoutManager = LinearLayoutManager(this)
        super.initializeLayout()
    }

    override fun requestData(onlyCache: Boolean) {
        super.requestData(onlyCache)
        GroupRepository.requestGroups(GroupRepository.RequestGroupsArguments(
            (pagesCount - 1) * countPerPage, countPerPage,
            GROUP_EXTENDED_FIELDS), if (onlyCache) CACHE else ANY)
    }

    private fun requestGroup(groupId: Int) {
        GroupRepository.requestGroup(groupId, object : VKApiCallback<VKGroup?> {
            override fun success(result: VKGroup?) {
                updateGroupInfoLayout(result)
            }

            override fun fail(error: Exception) {
                Log.e(TAG, "requestGroup>fail", error) // TODO
            }
        })
    }

//    private fun initializeLayout() {
//        updateToolbarTitle(EXPANDED_STATE)
//        toolbar.onExpandedStateChangedListener = { state -> updateToolbarTitle(state) }
//
//        val paddingStandard = dimen(this, R.dimen.padding_standard)
//        val totalWidth = VKUtils.width(this) - paddingStandard * 2
//        val itemWidth = dimen(this, R.dimen.group_item_width)
//        val spanCount = floor((totalWidth + paddingStandard * .5) / (itemWidth + paddingStandard * .5)).toInt()
//        val horizontalSpace = (totalWidth - spanCount * itemWidth) / (spanCount - 1)
//
//        adapter = Adapter(this)
//        recyclerView.adapter = adapter
//        recyclerView.layoutManager = GridLayoutManager(this, spanCount)
//        recyclerView.addItemDecoration(SpaceItemDecoration(horizontalSpace, dp(12), spanCount))
//    }

    private fun updateLeaveLayout() {
        bottomSheetLayout.animateExpanded(selectedGroupsIds.isNotEmpty())
    }

    private fun updateToolbarTitle(expandedState: Float) {
        toolbar?.title = getString(if (expandedState == COLLAPSED_STATE) R.string.leave_title_collapsed else R.string.leave_title_expanded)
    }

    private fun updateGroupInfoLayout(group: VKGroup?) {
        groupInfoLayout.group = group
    }

    private fun subscribeGroups() {
        if (groupsListener == null) {
            groupsListener = object : BaseRepository.Listener<VKGroup> {
                override fun onNextItems(result: BaseRepository.ItemsResult<VKGroup>) {
                    showData(result.items, result.items?.size != result.totalCount)
                }

                override fun onFail(error: java.lang.Exception) {
                    Log.e(TAG, "subscribeGroups>fail", error) // TODO
                }

                override fun getFilter(): Int? = null
            }
        }
        GroupRepository.groupCache.listeners.add(groupsListener!!)
    }

    private fun unsubscribeGroups() {
        if (groupsListener != null) {
            GroupRepository.groupCache.listeners.remove(groupsListener!!)
            groupsListener = null
        }
    }

    inner class Adapter(
        context: Context
    ) : BaseAdapter<VKGroup>(context) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return when (viewType) {
                ITEM_VIEW_VALUE -> ItemViewHolder(
                    layoutInflater.inflate(
                        R.layout.layout_group_item, parent, false
                    )
                )
                else -> super.onCreateViewHolder(parent, viewType)
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val group = items[position]
            if (group != null) {
                (holder as ItemViewHolder).update(group)
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
        }

        private fun updateBySelected(holder: ItemViewHolder, group: VKGroup, animate: Boolean) {
            holder.setSelected(selectedGroupsIds.contains(group.id), animate)
        }

        inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

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

            fun update(group: VKGroup) {
                Picasso.get().load(group.photo200)
                    .placeholder(ColorDrawable(PLACEHOLDER_COLOR))
                    .error(ColorDrawable(PLACEHOLDER_COLOR))
                    .into(photoImageView)
                nameTextView.text = group.name
            }

        }

    }

}

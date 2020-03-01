package com.kuelye.vkcup20ii.b.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.Gravity.START
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kuelye.vkcup20ii.b.R
import com.kuelye.vkcup20ii.b.ui.misc.GroupMarkerHolder
import com.kuelye.vkcup20ii.core.data.BaseRepository
import com.kuelye.vkcup20ii.core.data.BaseRepository.ItemsResult
import com.kuelye.vkcup20ii.core.data.BaseRepository.Source
import com.kuelye.vkcup20ii.core.data.GroupRepository
import com.kuelye.vkcup20ii.core.data.GroupRepository.RequestGroupsArguments
import com.kuelye.vkcup20ii.core.model.groups.VKGroup
import com.kuelye.vkcup20ii.core.model.groups.VKGroup.Field.ADDRESSES
import com.kuelye.vkcup20ii.core.model.groups.VKGroup.Field.DESCRIPTION
import com.kuelye.vkcup20ii.core.ui.view.BottomSheet
import com.kuelye.vkcup20ii.core.ui.view.BottomSheetLayout
import kotlinx.android.synthetic.main.fragment_group_map.*
import kotlinx.android.synthetic.main.layout_map_group_address_info.view.*

class GroupMapFragment : BaseMapFragment<GroupMarkerHolder>() {

    companion object {
        private val TAG = GroupMapFragment::class.java.simpleName
        private val GROUP_EXTENDED_FIELDS = listOf(DESCRIPTION, ADDRESSES)
        private const val EXTRA_GROUP_TYPE = "FILTER"

        fun newInstance(groupType: VKGroup.Type): GroupMapFragment {
            val fragment = GroupMapFragment()
            fragment.arguments = Bundle().apply { putSerializable(EXTRA_GROUP_TYPE, groupType) }
            return fragment
        }
    }

    private var selectedMarker: GroupMarkerHolder? = null
    private var groupsListener: BaseRepository.Listener<VKGroup>? = null

    private val filter
        get() = arguments?.getSerializable(EXTRA_GROUP_TYPE) as VKGroup.Type

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_group_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (view as BottomSheetLayout).apply {
            outsideScrollEnabled = true
            onTargetStateChangeListener = { state ->
                map!!.setPadding(0, 0, 0, (infoView.measuredWidth * state).toInt())
            }
            onCollapsedListener = {
                select(null)
            }
            if (bottomSheet is BottomSheet) {
                (bottomSheet as BottomSheet).toolbar?.titleGravity = START
            }
        }
    }

    override fun onResume() {
        subscribeGroups()
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        unsubscribeGroups()
    }

    override fun requestData(source: Source) {
        Log.v(TAG, "requestData: source=$source")
        GroupRepository.requestGroups(RequestGroupsArguments(
            (pagesCount - 1) * countPerPage, countPerPage,
            GROUP_EXTENDED_FIELDS, filter),
            source)
    }

    override fun onClusterItemClick(marker: GroupMarkerHolder): Boolean {
        super.onClusterItemClick(marker)
        select(marker)
        return true
    }

    private fun subscribeGroups() {
        if (groupsListener == null) {
            groupsListener = object : BaseRepository.Listener<VKGroup> {
                override fun onNextItems(result: ItemsResult<VKGroup>) {
                    Log.v(TAG, "subscribeGroups>success: filter=${filter.value.hashCode()}, result=$result")
                    updateMarkers(result.items)
                    if (result.totalCount != null && result.items?.size ?: 0 < result.totalCount!!
                        && !result.fromCache) {
                        pagesCount++
                        requestData()
                    }
                }

                override fun onFail(error: java.lang.Exception) {
                    Log.e(TAG, "subscribeGroups>fail", error) // TODO
                }

                override fun getFilter(): Int? = filter.value.hashCode()
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

    private fun updateMarkers(groups: List<VKGroup>?) {
        if (map == null || clusterManager == null || clusterRenderer == null) return
        if (groups == null) return
        Log.v(TAG, "updateMarkers: filter=$filter, groups.size=${groups.size}")
        for (group in groups) {
            if (group.addresses.isNullOrEmpty()) continue
            for (address in group.addresses!!) {
                var marker = markers.get(address.id)
                if (marker == null) {
                    marker = GroupMarkerHolder(group, address, clusterRenderer!!)
                    markers.put(address.id, marker)
                    clusterManager!!.addItem(marker)
                } else {
                    marker.setGroupAddress(group, address)
                }
            }
        }
        clusterManager!!.cluster()
        initializeCamera()
    }

    private fun select(marker: GroupMarkerHolder? = null) {
        if (marker != selectedMarker) {
            selectedMarker?.selected = false
            selectedMarker = marker
            selectedMarker?.apply {
                selected = true
                (view as BottomSheetLayout).apply {
                    animateExpanded(false) {
                        infoView.setGroupAddress(selectedMarker!!.group, selectedMarker!!.address)
                        animateExpanded(true)
                    }
                }
            }
        }
    }

}
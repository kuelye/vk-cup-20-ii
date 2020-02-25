package com.kuelye.vkcup20ii.b.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kuelye.vkcup20ii.b.R
import com.kuelye.vkcup20ii.b.ui.misc.GroupMarkerHolder
import com.kuelye.vkcup20ii.core.data.GroupRepository
import com.kuelye.vkcup20ii.core.model.VKGroup
import com.kuelye.vkcup20ii.core.model.VKGroup.Field.ADDRESSES
import com.kuelye.vkcup20ii.core.model.VKGroup.Field.DESCRIPTION
import com.kuelye.vkcup20ii.core.ui.view.BottomSheetLayout
import com.vk.api.sdk.VKApiCallback
import kotlinx.android.synthetic.main.fragment_group_map.*

class GroupMapFragment : BaseMapFragment<GroupMarkerHolder>() {

    companion object {
        private val TAG = GroupMapFragment::class.java.simpleName
        private val GROUP_EXTENDED_FIELDS = arrayOf(DESCRIPTION, ADDRESSES)
    }

    private var selectedMarker: GroupMarkerHolder? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_group_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (view as BottomSheetLayout).outsideScrollEnabled = true
    }

    override fun requestData() {
        GroupRepository.getGroups(GROUP_EXTENDED_FIELDS, "groups",
            object : VKApiCallback<List<VKGroup>> {
                override fun success(result: List<VKGroup>) {
                    updateMarkers(result)
                }

                override fun fail(error: Exception) {
                    Log.e(TAG, "requestData>fail", error) // TODO
                }
            })
    }

    override fun onClusterItemClick(marker: GroupMarkerHolder): Boolean {
        select(marker)
        return true
    }

    private fun updateMarkers(groups: List<VKGroup>) {
        if (map == null || clusterManager == null || clusterRenderer == null) return
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
            clusterManager!!.cluster()
        }
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
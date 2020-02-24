package com.kuelye.vkcup20ii.b.ui.misc

import android.content.Context
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer

class GroupMarkerRenderer(
    context: Context,
    map: GoogleMap,
    clusterManager: ClusterManager<MarkerHolder>
) : DefaultClusterRenderer<MarkerHolder>(context, map, clusterManager) {

    companion object {
        private val TAG = GroupMarkerRenderer::class.java.simpleName
    }

    init {
        minClusterSize = 1
    }

    override fun onBeforeClusterItemRendered(marker: MarkerHolder, markerOptions: MarkerOptions) {
        markerOptions.anchor(0.5f, 0.5f)
    }

    override fun onClusterItemRendered(clusterItem: MarkerHolder, marker: Marker) {
        clusterItem.updateIcon()
    }

}
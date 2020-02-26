package com.kuelye.vkcup20ii.b.ui.misc

import android.content.Context
import android.util.Log
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager

class MarkerRenderer<T : BaseMarkerHolder>(
    context: Context,
    map: GoogleMap,
    clusterManager: ClusterManager<T>
) : DefaultClusterRenderer<T>(context, map, clusterManager) {

    companion object {
        private val TAG = MarkerRenderer::class.java.simpleName
    }

    init {
        minClusterSize = 1
    }

    override fun onBeforeClusterItemRendered(marker: T, markerOptions: MarkerOptions) {
        markerOptions.anchor(0.5f, 0.5f)
    }

    override fun onClusterItemRendered(clusterItem: T, marker: Marker) {
        clusterItem.onClusterItemRendered()
    }

    override fun onBeforeClusterRendered(cluster: Cluster<T>, markerOptions: MarkerOptions) {
        markerOptions.anchor(0.5f, 0.5f)
        val clusterBitmap = cluster.items.first().getClusterBitmap(cluster.size)
        clusterBitmap?.let { markerOptions.icon(BitmapDescriptorFactory.fromBitmap(clusterBitmap)) }
    }

    override fun onClusterRendered(cluster: Cluster<T>, marker: Marker) {
        Log.v(TAG, "GUB onClusterRendered: $marker")
        cluster.items.first().onClusterRendered(cluster.size)
    }

}
package com.kuelye.vkcup20ii.b.ui.misc

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.util.SparseArray
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

    private val clusterMarkers: SparseArray<Marker> by lazy { SparseArray<Marker>() }

    override fun onBeforeClusterItemRendered(clusterItem: T, markerOptions: MarkerOptions) {
        clusterItem.onBeforeClusterItemRendered(markerOptions)
        clusterMarkers.remove(clusterItem.id)
    }

    override fun onClusterItemRendered(clusterItem: T, marker: Marker) {
        clusterItem.onClusterItemRendered()
    }

    override fun onBeforeClusterRendered(cluster: Cluster<T>, markerOptions: MarkerOptions) {
        cluster.items.first().onBeforeClusterRendered(cluster.size, markerOptions)
    }

    override fun onClusterRendered(cluster: Cluster<T>, marker: Marker) {
        super.onClusterRendered(cluster, marker)
        val clusterItem = cluster.items.first()
        clusterMarkers.put(clusterItem.id, marker)
        clusterItem.onClusterRendered(cluster.size)
    }

    fun onIconLoaded(clusterItem: T, bitmap: Bitmap?, cluster: Boolean) {
        val icon = BitmapDescriptorFactory.fromBitmap(bitmap)
        if (cluster) {
            clusterMarkers.get(clusterItem.id)?.setIcon(icon)
        } else {
            getMarker(clusterItem)?.setIcon(icon)
        }
    }

}
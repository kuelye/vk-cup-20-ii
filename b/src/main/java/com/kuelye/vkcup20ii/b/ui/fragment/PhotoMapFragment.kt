package com.kuelye.vkcup20ii.b.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kuelye.vkcup20ii.b.R
import com.kuelye.vkcup20ii.b.ui.misc.PhotoMarkerHolder
import com.kuelye.vkcup20ii.core.data.PhotoRepository
import com.kuelye.vkcup20ii.core.model.photos.VKPhoto
import com.vk.api.sdk.VKApiCallback

class PhotoMapFragment : BaseMapFragment<PhotoMarkerHolder>() {

    companion object {
        private val TAG = PhotoMapFragment::class.java.simpleName
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_photo_map, container, false)
    }

    override fun requestData() {
        PhotoRepository.getPhotos(object : VKApiCallback<List<VKPhoto>> {
            override fun success(result: List<VKPhoto>) {
                updateMarkers(result)
            }

            override fun fail(error: Exception) {
                Log.e(TAG, "requestData>fail", error) // TODO
            }
        })
    }

    override fun onClusterItemClick(marker: PhotoMarkerHolder): Boolean {
        // TODO
        return true
    }

    private fun updateMarkers(photos: List<VKPhoto>) {
        if (map == null || clusterManager == null || clusterRenderer == null) return
        Log.v(TAG, "updateMarkers: ${photos.size}")
        for (photo in photos) {
            if (photo.lat == null || photo.lng == null) continue
            var marker = markers.get(photo.id)
            if (marker == null) {
                marker = PhotoMarkerHolder(photo, clusterRenderer!!)
                markers.put(photo.id, marker)
                clusterManager!!.addItem(marker)
            } else {
                marker.photo = photo
            }
            Log.v(TAG, "updateMarkers: ${markers.size()}")
            clusterManager!!.cluster()
        }
    }

}
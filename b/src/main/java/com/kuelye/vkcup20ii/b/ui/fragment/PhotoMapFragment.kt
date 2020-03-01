package com.kuelye.vkcup20ii.b.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kuelye.vkcup20ii.b.R
import com.kuelye.vkcup20ii.b.ui.misc.PhotoMarkerHolder
import com.kuelye.vkcup20ii.core.data.BaseRepository
import com.kuelye.vkcup20ii.core.data.BaseRepository.ItemsResult
import com.kuelye.vkcup20ii.core.data.PhotoRepository
import com.kuelye.vkcup20ii.core.data.PhotoRepository.RequestPhotosArguments
import com.kuelye.vkcup20ii.core.model.photos.VKPhoto

class PhotoMapFragment : BaseMapFragment<PhotoMarkerHolder>() {

    companion object {
        private val TAG = PhotoMapFragment::class.java.simpleName
    }

    private var photosListener: BaseRepository.Listener<VKPhoto>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_photo_map, container, false)
    }

    override fun onResume() {
        super.onResume()
        subscribePhotos()
    }

    override fun onPause() {
        super.onPause()
        unsubscribePhotos()
    }

    override fun requestData(source: BaseRepository.Source) {
        Log.v(TAG, "requestData: v=$source")
        PhotoRepository.requestPhotos(RequestPhotosArguments(
            (pagesCount - 1) * countPerPage, countPerPage),
            source)
    }

    override fun onClusterItemClick(marker: PhotoMarkerHolder): Boolean {
        super.onClusterItemClick(marker)
        return true
    }

    private fun subscribePhotos() {
        if (photosListener == null) {
            photosListener = object : BaseRepository.Listener<VKPhoto> {
                override fun onNextItems(result: ItemsResult<VKPhoto>) {
                    Log.v(TAG, "subscribePhotos>success: result=$result")
                    updateMarkers(result.items)
                    if (result.totalCount != null && result.items?.size ?: 0 < result.totalCount!!
                            && !result.fromCache) {
                        pagesCount++
                        requestData()
                    }
                }

                override fun onFail(error: java.lang.Exception) {
                    Log.e(TAG, "subscribePhotos>fail", error) // TODO
                }

                override fun getFilter(): Int? = null
            }
        }
        PhotoRepository.photoCache.listeners.add(photosListener!!)
    }

    private fun unsubscribePhotos() {
        if (photosListener != null) {
            PhotoRepository.photoCache.listeners.remove(photosListener!!)
            photosListener = null
        }
    }

    private fun updateMarkers(photos: List<VKPhoto>?) {
        if (map == null || clusterManager == null || clusterRenderer == null ) return
        if (photos == null) return
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
        }
        clusterManager!!.cluster()
        initializeCamera()
    }

}
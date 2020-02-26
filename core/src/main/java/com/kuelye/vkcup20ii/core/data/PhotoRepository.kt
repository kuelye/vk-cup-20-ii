package com.kuelye.vkcup20ii.core.data

import android.util.SparseArray
import com.kuelye.vkcup20ii.core.api.VKPhotosGetAllRequest
import com.kuelye.vkcup20ii.core.model.VKPhoto
import com.kuelye.vkcup20ii.core.model.VKPhotosGetAllResponse
import com.kuelye.vkcup20ii.core.utils.toList
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKApiCallback

object PhotoRepository {

    private val TAG = PhotoRepository::class.java.simpleName

    private var photosCache: SparseArray<VKPhoto>? = null

    fun getPhotos(
        callback: VKApiCallback<List<VKPhoto>>
    ) {
        if (photosCache != null) callback.success(photosCache!!.toList())
        if (!VK.isLoggedIn()) return
        VK.execute(VKPhotosGetAllRequest(0, 20), object : VKApiCallback<VKPhotosGetAllResponse> {
            override fun success(result: VKPhotosGetAllResponse) {
                ensureCache()
                photosCache!!.clear()
                for (photo in result.photos) {
                    photosCache!!.put(photo.id, photo)
                }
                callback.success(result.photos)
            }

            override fun fail(error: Exception) {
                callback.fail(error)
            }
        })
    }

    private fun ensureCache() {
        if (photosCache == null) {
            photosCache = SparseArray()
        }
    }

}
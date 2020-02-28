package com.kuelye.vkcup20ii.core.data

import android.util.SparseArray
import com.kuelye.vkcup20ii.core.api.photos.VKPhotoAlbumsGetRequest
import com.kuelye.vkcup20ii.core.api.photos.VKPhotosGetAllRequest
import com.kuelye.vkcup20ii.core.model.photos.VKPhoto
import com.kuelye.vkcup20ii.core.model.photos.VKPhotoAlbum
import com.kuelye.vkcup20ii.core.model.photos.VKPhotosGetAllResponse
import com.kuelye.vkcup20ii.core.utils.toList
import com.kuelye.vkcup20ii.core.utils.toMutableList
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKApiCallback
import java.util.*

object PhotoRepository : BaseRepository() {

    private val TAG = PhotoRepository::class.java.simpleName

    private var photoAlbums: SparseArray<VKPhotoAlbum>? = null
    private var sortedPhotoAlbums: MutableList<VKPhotoAlbum>? = null
    private var photoAlbumsTotalCount: Int? = null
    private var photos: SparseArray<VKPhoto>? = null

    fun getPhotoAlbums(
        offset: Int, count: Int, onlyCache: Boolean,
        callback: VKApiCallback<GetItemsResult<VKPhotoAlbum>>
    ) {
        if (sortedPhotoAlbums != null) {
            callback.success(GetItemsResult(sortedPhotoAlbums!!, photoAlbumsTotalCount))
            if (onlyCache) return
        }
        if (!VK.isLoggedIn()) return
        val request = VKPhotoAlbumsGetRequest(offset, count)
        VK.execute(request, object : VKApiCallback<VKPhotoAlbumsGetRequest.Response> {
            override fun success(result: VKPhotoAlbumsGetRequest.Response) {
                ensurePhotoAlbumsCache()
                photoAlbums!!.clear()
                for (document in result.items) photoAlbums!!.put(document.id, document)
                photoAlbumsTotalCount = result.count
                sortedPhotoAlbums = photoAlbums!!.toMutableList()
                Collections.sort(sortedPhotoAlbums!!, VKPhotoAlbum.DefaultComparator())
                callback.success(GetItemsResult(sortedPhotoAlbums!!, result.count))
            }

            override fun fail(error: Exception) {
                callback.fail(error)
            }
        })
    }

    fun getPhotos(
        callback: VKApiCallback<List<VKPhoto>>
    ) {
        if (photos != null) callback.success(photos!!.toList())
        if (!VK.isLoggedIn()) return
        val request = VKPhotosGetAllRequest(0, 20)
        VK.execute(request, object : VKApiCallback<VKPhotosGetAllResponse> {
            override fun success(result: VKPhotosGetAllResponse) {
                ensurePhotosCache()
                photos!!.clear()
                for (photo in result.photos) {
                    photos!!.put(photo.id, photo)
                }
                callback.success(result.photos)
            }

            override fun fail(error: Exception) {
                callback.fail(error)
            }
        })
    }

    private fun ensurePhotoAlbumsCache() {
        if (photoAlbums == null) {
            photoAlbums = SparseArray()
        }
    }

    private fun ensurePhotosCache() {
        if (photos == null) {
            photos = SparseArray()
        }
    }

}
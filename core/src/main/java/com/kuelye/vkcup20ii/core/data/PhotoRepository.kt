package com.kuelye.vkcup20ii.core.data

import android.content.Context
import android.net.Uri
import android.util.Log
import com.kuelye.vkcup20ii.core.api.photos.*
import com.kuelye.vkcup20ii.core.api.wall.VKWallPostCommand
import com.kuelye.vkcup20ii.core.model.photos.VKPhoto
import com.kuelye.vkcup20ii.core.model.photos.VKPhotoAlbum
import com.kuelye.vkcup20ii.core.model.photos.VKPhotosDeleteAlbumRequest
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKApiCallback

object PhotoRepository : BaseRepository() {

    private val TAG = PhotoRepository::class.java.simpleName

    private var photoAlbumCache: Cache<VKPhotoAlbum> =
        Cache(defaultComparator = VKPhotoAlbum.DefaultComparator())
    private var photoCache: Cache<VKPhoto> =
        Cache({ photo, albumId -> photo.albumId == albumId }, VKPhoto.DefaultComparator())

    fun getPhotoAlbums(
        offset: Int, count: Int, onlyCache: Boolean,
        callback: VKApiCallback<GetItemsResult<VKPhotoAlbum>>
    ) {
        if (photoAlbumCache.sortedItems != null) {
            callback.success(GetItemsResult.from(photoAlbumCache))
            if (onlyCache) return
        }
        if (!VK.isLoggedIn()) return
        val request = VKPhotoAlbumsGetRequest(offset, count)
        VK.execute(request, object : VKApiCallback<VKPhotoAlbumsGetRequest.Response> {
            override fun success(result: VKPhotoAlbumsGetRequest.Response) {
                photoAlbumCache.set(result.items, result.count, clear = offset == 0)
                callback.success(GetItemsResult.from(photoAlbumCache))
            }

            override fun fail(error: Exception) {
                callback.fail(error)
            }
        })
    }

    fun removePhotoAlbum(
        album: VKPhotoAlbum,
        callback: VKApiCallback<Int>
    ) {
        if (!VK.isLoggedIn()) return
        val request = VKPhotosDeleteAlbumRequest(album.id)
        VK.execute(request, object : VKApiCallback<Int> {
            override fun success(result: Int) {
                if (result == 1) photoAlbumCache.remove(album)
                callback.success(result)
            }

            override fun fail(error: Exception) {
                callback.fail(error)
            }
        })
    }

    fun getPhotos(
        photoAlbumId: Int?, offset: Int, count: Int, onlyCache: Boolean,
        callback: VKApiCallback<GetItemsResult<VKPhoto>>
    ) {
        if (photoCache.sortedItems != null) {
            callback.success(GetItemsResult.from(photoCache, photoAlbumId))
            if (onlyCache && photoCache.totalCounts.containsKey(photoAlbumId)) return
        }
        if (!VK.isLoggedIn()) return
        val request = if (photoAlbumId == null) VKPhotosGetAllRequest(offset, count) else
            VKPhotosGetRequest(photoAlbumId, offset, count)
        VK.execute(request, object : VKApiCallback<BaseVKPhotosGetRequest.Response> {
            override fun success(result: BaseVKPhotosGetRequest.Response) {
                photoCache.set(result.items, result.count, photoAlbumId, offset == 0)
                callback.success(GetItemsResult.from(photoCache, photoAlbumId))
            }

            override fun fail(error: Exception) {
                callback.fail(error)
            }
        })
    }

    fun savePhoto(
        context: Context, photo: Uri, albumId: Int,
        callback: VKApiCallback<VKPhoto>
    ) {
        if (!VK.isLoggedIn()) return
        VK.execute(VKSavePhotoCommand(context, photo, albumId), object : VKApiCallback<VKPhoto> {
            override fun success(result: VKPhoto) {
                photoCache.add(result, result.albumId)
                callback.success(result)
            }

            override fun fail(error: Exception) {
                callback.fail(error)
            }
        })
    }

}
package com.kuelye.vkcup20ii.core.data

import android.content.Context
import android.net.Uri
import android.util.Log
import com.kuelye.vkcup20ii.core.api.photos.*
import com.kuelye.vkcup20ii.core.data.BaseRepository.Source.CACHE
import com.kuelye.vkcup20ii.core.data.BaseRepository.Source.FRESH
import com.kuelye.vkcup20ii.core.model.photos.VKPhoto
import com.kuelye.vkcup20ii.core.model.photos.VKPhotoAlbum
import com.kuelye.vkcup20ii.core.model.photos.VKPhotosDeleteAlbumRequest

import com.vk.api.sdk.VK
import com.vk.api.sdk.VKApiCallback

object PhotoRepository : BaseRepository() {

    private val TAG = PhotoRepository::class.java.simpleName

    val photoAlbumCache: Cache<VKPhotoAlbum> by lazy {
        Cache(defaultComparator = VKPhotoAlbum.DefaultComparator()) }
    private val photoAlbumsRequestManager: RequestManager by lazy { RequestManager() }
    val photoCache: Cache<VKPhoto> by lazy {
        Cache({ photo, albumId -> photo.albumId == albumId }, VKPhoto.DefaultComparator()) }
    private val photosRequestManager: RequestManager by lazy { RequestManager() }

    fun requestPhotoAlbums(
        arguments: RequestPhotoAlbumsArguments, source: Source
    ) {
        //Log.v(TAG, "requestPhotoAlbums: arguments=$arguments, source=$source")
        if (source != CACHE && !photoAlbumsRequestManager.request(arguments)) return
        if (source != FRESH) if (photoAlbumCache.emit(null, true)) if (source == CACHE) return
        if (!VK.isLoggedIn()) return
        val request = VKPhotoAlbumsGetRequest()
        VK.execute(request, object : VKApiCallback<VKPhotoAlbumsGetRequest.Response> {
            override fun success(result: VKPhotoAlbumsGetRequest.Response) {
                //Log.v(TAG, "requestPhotoAlbums>success: result=$result")
                photoAlbumsRequestManager.finish(arguments)
                photoAlbumCache.set(result.items, result.count, null, true)
            }

            override fun fail(error: Exception) {
                photoAlbumsRequestManager.finish(arguments)
                photoAlbumCache.fail(error)
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

    fun requestPhotos(
        arguments: RequestPhotosArguments, source: Source
    ) {
        //Log.v(TAG, "getPhotos: arguments=$arguments, source=$source")
        if (source != CACHE && !photosRequestManager.request(arguments)) return
        if (source != FRESH && photoCache.emit(arguments.filter, true)) if (source == CACHE) return
        if (!VK.isLoggedIn()) return
        val request = if (arguments.albumId == null) VKPhotosGetAllRequest(arguments.offset, arguments.count) else
            VKPhotosGetRequest(arguments.albumId, arguments.offset, arguments.count)
        VK.execute(request, object : VKApiCallback<BaseVKPhotosGetRequest.Response> {
            override fun success(result: BaseVKPhotosGetRequest.Response) {
                //Log.v(TAG, "requestGroups>success: result=$result")
                photosRequestManager.finish(arguments)
                photoCache.set(result.items, result.count, arguments.filter, arguments.offset == 0)
            }

            override fun fail(error: Exception) {
                photosRequestManager.finish(arguments)
                photoCache.fail(error)
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
                photoCache.update(result, result.albumId)
                callback.success(result)
            }

            override fun fail(error: Exception) {
                callback.fail(error)
            }
        })
    }

    data class RequestPhotoAlbumsArguments(
        val count: Int? = null
    ) : RequestManager.Arguments

    data class RequestPhotosArguments(
        val offset: Int,
        val count: Int,
        val albumId: Int? = null
    ) : RequestManager.Arguments {
        val filter = albumId
    }

}
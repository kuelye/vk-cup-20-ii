package com.kuelye.vkcup20ii.core.api.photos

import android.content.Context
import android.net.Uri
import com.kuelye.vkcup20ii.core.api.BasePhotoUploadCommand
import com.kuelye.vkcup20ii.core.api.RESPONSE_FIELD_KEY
import com.kuelye.vkcup20ii.core.api.VKPhotoColumns.Companion.ALBUM_ID_FIELD_KEY
import com.kuelye.vkcup20ii.core.model.misc.FileUploadInfo
import com.kuelye.vkcup20ii.core.model.photos.VKPhoto
import com.kuelye.vkcup20ii.core.utils.getImagePath
import com.vk.api.sdk.VKApiManager
import com.vk.api.sdk.VKApiResponseParser
import com.vk.api.sdk.VKMethodCall
import com.vk.api.sdk.exceptions.VKApiIllegalResponseException
import org.json.JSONException
import org.json.JSONObject
import java.lang.ref.WeakReference

class VKSavePhotoCommand(
    context: Context,
    private val photo: Uri,
    private val albumId: Int
) : BasePhotoUploadCommand<VKPhoto>(context) {

    companion object {
        private val TAG = VKSavePhotoCommand::class.java.simpleName
    }

    override fun onExecute(manager: VKApiManager): VKPhoto {
        val context = context.get() ?: throw IllegalArgumentException()
        val photoPath = getImagePath(context, photo) ?: throw IllegalArgumentException()

        val uploadServerUrl = getUploadServerUrl(manager, "photos.getUploadServer",
            mapOf(ALBUM_ID_FIELD_KEY to albumId.toString()))
        val photoUploadInfo = uploadPhoto(manager, photoPath, uploadServerUrl,
            "file1", "photos_list")
        return savePhoto(manager, photoUploadInfo, "photos.save", "photos_list",
            mapOf(ALBUM_ID_FIELD_KEY to albumId.toString()))[0]
    }

}
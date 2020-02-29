package com.kuelye.vkcup20ii.core.api.wall

import android.content.Context
import android.net.Uri
import com.kuelye.vkcup20ii.core.api.BasePhotoUploadCommand
import com.kuelye.vkcup20ii.core.api.RESPONSE_FIELD_KEY
import com.kuelye.vkcup20ii.core.model.misc.FileUploadInfo
import com.kuelye.vkcup20ii.core.utils.getImagePath
import com.vk.api.sdk.VKApiManager
import com.vk.api.sdk.VKApiResponseParser
import com.vk.api.sdk.VKMethodCall
import com.vk.api.sdk.exceptions.VKApiIllegalResponseException
import org.json.JSONException
import org.json.JSONObject
import java.lang.ref.WeakReference

class VKWallPostCommand(
    context: Context,
    private val message: String,
    private val photo: Uri
) : BasePhotoUploadCommand<Int>(context) {

    companion object {
        private val TAG = VKWallPostCommand::class.java.simpleName
    }

    override fun onExecute(manager: VKApiManager): Int {
        val context = context.get() ?: throw IllegalArgumentException()
        val photoPath = getImagePath(context, photo) ?: throw IllegalArgumentException()

        val uploadServerUrl = getUploadServerUrl(manager, "photos.getWallUploadServer")
        val photoUploadInfo = uploadPhoto(manager, photoPath, uploadServerUrl, "photo", "photo")
        val photo = savePhoto(manager, photoUploadInfo, "photos.saveWallPhoto", "photo")

        val call = VKMethodCall.Builder()
            .method("wall.post")
            .args("message", message)
            .args("attachments", photo[0].attachment)
            .version(manager.config.version)
            .build()
        return manager.execute(call, ResponseApiParser())
    }

    private class ResponseApiParser : VKApiResponseParser<Int> {
        override fun parse(response: String): Int {
            try {
                return JSONObject(response).getJSONObject(RESPONSE_FIELD_KEY).getInt("post_id")
            } catch (e: JSONException) {
                throw VKApiIllegalResponseException(e)
            }
        }
    }

}
package com.kuelye.vkcup20ii.e.api

import android.content.Context
import android.net.Uri
import android.util.Log
import com.kuelye.vkcup20ii.core.utils.getImagePath
import com.kuelye.vkcup20ii.e.model.FileUploadInfo
import com.vk.api.sdk.*
import com.vk.api.sdk.exceptions.VKApiIllegalResponseException
import com.vk.api.sdk.internal.ApiCommand
import com.vk.api.sdk.utils.VKUtils
import org.json.JSONException
import org.json.JSONObject
import java.lang.IllegalArgumentException
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit.MINUTES

class VKWallPostCommand(
    context: Context,
    private val message: String,
    private val photo: Uri
) : ApiCommand<Int>() {

    companion object {
        private val TAG = VKWallPostCommand::class.java.simpleName
        private val UPLOAD_PHOTO_TIMEOUT = MINUTES.toMillis(5)
        private const val UPLOAD_PHOTO_RETRY_COUNT = 3
        private const val PHOTO_ATTACHMENT_TEMPLATE = "photo%s_%s"
    }

    private val context: WeakReference<Context> = WeakReference(context)

    override fun onExecute(manager: VKApiManager): Int {
        val context = context.get() ?: throw IllegalArgumentException()
        val photoPath = getImagePath(context, photo) ?: throw IllegalArgumentException()

        val uploadServerUrl = getPhotosWallUploadServerUrl(manager)
        val attachment = uploadPhoto(manager, photoPath, uploadServerUrl)

        val call = VKMethodCall.Builder()
            .method("wall.post")
            .args("message", message)
            .args("attachments", attachment)
            .version(manager.config.version)
            .build()
        return manager.execute(call, ResponseApiParser())
    }

    private fun getPhotosWallUploadServerUrl(manager: VKApiManager): String {
        val call = VKMethodCall.Builder()
            .method("photos.getWallUploadServer")
            .version(manager.config.version)
            .build()
        return manager.execute(call, WallUploadServerUrlParser())
    }

    private fun uploadPhoto(manager: VKApiManager, photoPath: Uri, uploadServerUrl: String): String {
        val fileUploadCall = VKHttpPostCall.Builder()
            .url(uploadServerUrl)
            .args("photo", photoPath, "image.jpg")
            .timeout(UPLOAD_PHOTO_TIMEOUT)
            .retryCount(UPLOAD_PHOTO_RETRY_COUNT)
            .build()
        val fileUploadInfo = manager.execute(fileUploadCall, null, FileUploadInfoParser())

        val saveWallPhotoCall = VKMethodCall.Builder()
            .method("photos.saveWallPhoto")
            .args("server", fileUploadInfo.server)
            .args("photo", fileUploadInfo.photo)
            .args("hash", fileUploadInfo.hash)
            .version(manager.config.version)
            .build()
        return manager.execute(saveWallPhotoCall, SaveWallPhotoParser())
    }

    private class ResponseApiParser : VKApiResponseParser<Int> {
        override fun parse(response: String): Int {
            try {
                return JSONObject(response).getJSONObject("response").getInt("post_id")
            } catch (e: JSONException) {
                throw VKApiIllegalResponseException(e)
            }
        }
    }

    private class WallUploadServerUrlParser : VKApiResponseParser<String> {
        override fun parse(response: String): String {
            try {
                return JSONObject(response).getJSONObject("response").getString("upload_url")
            } catch (e: JSONException) {
                throw VKApiIllegalResponseException(e)
            }
        }
    }

    private class FileUploadInfoParser : VKApiResponseParser<FileUploadInfo> {
        override fun parse(response: String): FileUploadInfo {
            try {
                return FileUploadInfo.parse(JSONObject(response))
            } catch (e: JSONException) {
                throw VKApiIllegalResponseException(e)
            }
        }
    }

    private class SaveWallPhotoParser : VKApiResponseParser<String> {
        override fun parse(response: String): String {
            try {
                val jo = JSONObject(response).getJSONArray("response").getJSONObject(0)
                return String.format(
                    PHOTO_ATTACHMENT_TEMPLATE,
                    jo.getInt("owner_id"),
                    jo.getInt("id")
                )
            } catch (ex: JSONException) {
                throw VKApiIllegalResponseException(ex)
            }
        }
    }
}
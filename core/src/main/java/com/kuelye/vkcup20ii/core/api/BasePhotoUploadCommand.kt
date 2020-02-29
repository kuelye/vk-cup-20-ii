package com.kuelye.vkcup20ii.core.api

import android.content.Context
import android.net.Uri
import com.kuelye.vkcup20ii.core.model.misc.FileUploadInfo
import com.kuelye.vkcup20ii.core.model.photos.VKPhoto
import com.kuelye.vkcup20ii.core.utils.map
import com.vk.api.sdk.VKApiManager
import com.vk.api.sdk.VKApiResponseParser
import com.vk.api.sdk.VKHttpPostCall
import com.vk.api.sdk.VKMethodCall
import com.vk.api.sdk.exceptions.VKApiIllegalResponseException
import com.vk.api.sdk.internal.ApiCommand
import org.json.JSONException
import org.json.JSONObject
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit

abstract class BasePhotoUploadCommand<R>(
    context: Context
) : ApiCommand<R>() {

    companion object {
        private val UPLOAD_PHOTO_TIMEOUT = TimeUnit.MINUTES.toMillis(5)
        private const val UPLOAD_PHOTO_RETRY_COUNT = 3
    }

    protected val context: WeakReference<Context> = WeakReference(context)

    protected fun getUploadServerUrl(
        manager: VKApiManager, method: String,
        args: Map<String, String>? = null
    ): String {
        val callBuilder = VKMethodCall.Builder()
            .method(method)
            .version(manager.config.version)
        if (args != null) callBuilder.args(args)
        return manager.execute(callBuilder.build(), WallUploadServerUrlParser())
    }

    protected fun uploadPhoto(
        manager: VKApiManager, photoPath: Uri, uploadServerUrl: String,
        requestFileKey: String, responseFileKey: String
    ): FileUploadInfo {
        val call = VKHttpPostCall.Builder()
            .url(uploadServerUrl)
            .args(requestFileKey, photoPath, "image.jpg")
            .timeout(UPLOAD_PHOTO_TIMEOUT)
            .retryCount(UPLOAD_PHOTO_RETRY_COUNT)
            .build()
        return manager.execute(call, null, FileUploadInfoParser(responseFileKey))
    }

    protected fun savePhoto(
        manager: VKApiManager, fileUploadInfo: FileUploadInfo,
        method: String, requestPhotoKey: String,
        args: Map<String, String>? = null
    ): List<VKPhoto> {
        val callBuilder = VKMethodCall.Builder()
            .method(method)
            .args("server", fileUploadInfo.server)
            .args(requestPhotoKey, fileUploadInfo.file)
            .args("hash", fileUploadInfo.hash)
            .version(manager.config.version)
        if (args != null) callBuilder.args(args)
        return manager.execute(callBuilder.build(), PhotosSavePhotoParser())
    }

    private class WallUploadServerUrlParser : VKApiResponseParser<String> {
        override fun parse(response: String): String {
            try {
                return JSONObject(response).getJSONObject(RESPONSE_FIELD_KEY).getString("upload_url")
            } catch (e: JSONException) {
                throw VKApiIllegalResponseException(e)
            }
        }
    }

    private class FileUploadInfoParser(
        private val fileKey: String
    ) : VKApiResponseParser<FileUploadInfo> {
        override fun parse(response: String): FileUploadInfo {
            try {
                return FileUploadInfo.parse(JSONObject(response), fileKey)
            } catch (e: JSONException) {
                throw VKApiIllegalResponseException(e)
            }
        }
    }

    private class PhotosSavePhotoParser : VKApiResponseParser<List<VKPhoto>> {
        override fun parse(response: String): List<VKPhoto> {
            try {
                return JSONObject(response).getJSONArray(RESPONSE_FIELD_KEY)
                    .map { VKPhoto.parse(it) }
            } catch (e: JSONException) {
                throw VKApiIllegalResponseException(e)
            }
        }
    }

}
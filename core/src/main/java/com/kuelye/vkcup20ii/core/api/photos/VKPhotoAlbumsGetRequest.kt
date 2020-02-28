package com.kuelye.vkcup20ii.core.api.photos

import com.kuelye.vkcup20ii.core.api.COUNT_FIELD_KEY
import com.kuelye.vkcup20ii.core.api.ITEMS_FIELD_KEY
import com.kuelye.vkcup20ii.core.api.RESPONSE_FIELD_KEY
import com.kuelye.vkcup20ii.core.model.photos.VKPhotoAlbum
import com.kuelye.vkcup20ii.core.utils.map
import com.vk.api.sdk.requests.VKRequest
import org.json.JSONObject

class VKPhotoAlbumsGetRequest(
    offset: Int, count: Int
) : VKRequest<VKPhotoAlbumsGetRequest.Response>("photos.getAlbums") {

    companion object {
        private val TAG = VKPhotoAlbumsGetRequest::class.java.simpleName
    }

    init {
        addParam("offset", offset)
        addParam("count", count)
        addParam("need_system", 1)
        addParam("need_covers", 1)
        addParam("photo_sizes", 1)
    }

    override fun parse(r: JSONObject): Response {
        return Response.parse(r.getJSONObject(RESPONSE_FIELD_KEY))
    }

    data class Response(
        val count: Int,
        val items: List<VKPhotoAlbum>
    ) {
        companion object {
            fun parse(jo: JSONObject): Response {
                return Response(
                    count = jo.getInt(COUNT_FIELD_KEY),
                    items = jo.getJSONArray(ITEMS_FIELD_KEY).map { VKPhotoAlbum.parse(it) }
                )
            }
        }
    }

}
package com.kuelye.vkcup20ii.core.api.photos

import com.kuelye.vkcup20ii.core.api.COUNT_FIELD_KEY
import com.kuelye.vkcup20ii.core.api.ITEMS_FIELD_KEY
import com.kuelye.vkcup20ii.core.api.RESPONSE_FIELD_KEY
import com.kuelye.vkcup20ii.core.model.photos.VKPhoto
import com.kuelye.vkcup20ii.core.utils.map
import com.vk.api.sdk.requests.VKRequest
import org.json.JSONObject

open class BaseVKPhotosGetRequest(
    method: String, offset: Int, count: Int
) : VKRequest<BaseVKPhotosGetRequest.Response>(method) {

    init {
        addParam("offset", offset)
        addParam("count", count)
        addParam("photo_sizes", 1)
    }

    override fun parse(r: JSONObject): Response {
        return Response.parse(r.getJSONObject(RESPONSE_FIELD_KEY))
    }

    class Response(
        val count: Int,
        val items: List<VKPhoto>
    ) {
        companion object {
            fun parse(jo: JSONObject): Response {
                return Response(
                    count = jo.getInt(COUNT_FIELD_KEY),
                    items = jo.getJSONArray(ITEMS_FIELD_KEY).map { VKPhoto.parse(it) }
                )
            }
        }
    }

}
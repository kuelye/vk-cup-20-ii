package com.kuelye.vkcup20ii.core.api.photos

import com.kuelye.vkcup20ii.core.api.RESPONSE_FIELD_KEY
import com.kuelye.vkcup20ii.core.model.photos.VKPhotosGetAllResponse
import com.vk.api.sdk.requests.VKRequest
import org.json.JSONObject

class VKPhotosGetAllRequest(
    offset: Int,
    count: Int
) : VKRequest<VKPhotosGetAllResponse>("photos.getAll") {

    init {
        addParam("offset", offset)
        addParam("count", count)
        addParam("photo_sizes", 1)
        addParam("skip_hidden", 1)
    }

    override fun parse(r: JSONObject): VKPhotosGetAllResponse {
        return VKPhotosGetAllResponse.parse(r.getJSONObject(RESPONSE_FIELD_KEY))
    }

}

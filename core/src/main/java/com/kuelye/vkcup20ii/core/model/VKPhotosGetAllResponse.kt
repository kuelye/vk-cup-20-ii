package com.kuelye.vkcup20ii.core.model

import com.kuelye.vkcup20ii.core.api.COUNT_FIELD_KEY
import com.kuelye.vkcup20ii.core.api.ITEMS_FIELD_KEY
import com.kuelye.vkcup20ii.core.utils.map
import org.json.JSONObject

data class VKPhotosGetAllResponse(
    val count: Int,
    val photos: List<VKPhoto>
) {
    companion object {
        fun parse(jo: JSONObject): VKPhotosGetAllResponse {
            return VKPhotosGetAllResponse(
                count = jo.getInt(COUNT_FIELD_KEY),
                photos = jo.getJSONArray(ITEMS_FIELD_KEY).map { VKPhoto.parse(it) }
            )
        }
    }
}
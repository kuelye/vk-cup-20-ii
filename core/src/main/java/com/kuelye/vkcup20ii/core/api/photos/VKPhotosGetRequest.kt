package com.kuelye.vkcup20ii.core.api.photos

import com.kuelye.vkcup20ii.core.api.COUNT_FIELD_KEY
import com.kuelye.vkcup20ii.core.api.ITEMS_FIELD_KEY
import com.kuelye.vkcup20ii.core.api.RESPONSE_FIELD_KEY
import com.kuelye.vkcup20ii.core.model.photos.VKPhoto
import com.kuelye.vkcup20ii.core.utils.map
import com.vk.api.sdk.requests.VKRequest
import org.json.JSONObject

class VKPhotosGetRequest(
    albumId: Int?, offset: Int, count: Int
) : BaseVKPhotosGetRequest("photos.get", offset, count) {

    init {
        if (albumId != null) addParam("album_id", albumId)
        addParam("rev", 1)
    }

}

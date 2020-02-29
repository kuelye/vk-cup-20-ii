package com.kuelye.vkcup20ii.core.model.photos

import com.kuelye.vkcup20ii.core.api.OWNER_ID_FIELD_KEY
import com.kuelye.vkcup20ii.core.api.RESPONSE_FIELD_KEY
import com.kuelye.vkcup20ii.core.api.VKPhotoColumns.Companion.ALBUM_ID_FIELD_KEY
import com.vk.api.sdk.requests.VKRequest
import org.json.JSONObject

class VKPhotosDeleteAlbumRequest(
    albumId: Int
) : VKRequest<Int>("photos.deleteAlbum") {

    init {
        addParam(ALBUM_ID_FIELD_KEY, albumId)
    }

    override fun parse(r: JSONObject): Int {
        return r.getInt(RESPONSE_FIELD_KEY)
    }

}

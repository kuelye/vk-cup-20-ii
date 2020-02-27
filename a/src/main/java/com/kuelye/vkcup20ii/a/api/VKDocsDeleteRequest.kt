package com.kuelye.vkcup20ii.a.api

import com.kuelye.vkcup20ii.core.api.OWNER_ID_FIELD_KEY
import com.kuelye.vkcup20ii.core.api.RESPONSE_FIELD_KEY
import com.vk.api.sdk.requests.VKRequest
import org.json.JSONObject

class VKDocsDeleteRequest(
    documentId: Int,
    ownerId: Int
) : VKRequest<Int>("docs.delete") {

    init {
        addParam(OWNER_ID_FIELD_KEY, ownerId)
        addParam("doc_id", documentId)
    }

    override fun parse(r: JSONObject): Int {
        return r.getInt(RESPONSE_FIELD_KEY)
    }

}

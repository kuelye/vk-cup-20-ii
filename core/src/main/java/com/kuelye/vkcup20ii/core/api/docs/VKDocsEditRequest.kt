package com.kuelye.vkcup20ii.core.api.docs

import com.kuelye.vkcup20ii.core.api.OWNER_ID_FIELD_KEY
import com.kuelye.vkcup20ii.core.api.RESPONSE_FIELD_KEY
import com.kuelye.vkcup20ii.core.api.TITLE_FIELD_KEY
import com.vk.api.sdk.requests.VKRequest
import org.json.JSONObject

class VKDocsEditRequest(
    documentId: Int,
    ownerId: Int,
    title: String
) : VKRequest<Int>("docs.edit") {

    init {
        addParam(OWNER_ID_FIELD_KEY, ownerId)
        addParam("doc_id", documentId)
        addParam(TITLE_FIELD_KEY, title)
    }

    override fun parse(r: JSONObject): Int {
        return r.getInt(RESPONSE_FIELD_KEY)
    }

}

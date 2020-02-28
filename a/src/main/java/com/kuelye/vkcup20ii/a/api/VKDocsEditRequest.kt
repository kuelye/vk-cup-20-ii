package com.kuelye.vkcup20ii.a.api

import android.util.Log
import com.kuelye.vkcup20ii.a.model.VKDocument
import com.kuelye.vkcup20ii.core.api.*
import com.kuelye.vkcup20ii.core.utils.map
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

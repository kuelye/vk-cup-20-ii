package com.kuelye.vkcup20ii.a.api

import com.kuelye.vkcup20ii.a.model.VKDocsGetResponse
import com.kuelye.vkcup20ii.core.api.RESPONSE_FIELD_KEY
import com.vk.api.sdk.requests.VKRequest
import org.json.JSONObject

class VKDocsGetRequest(
    offset: Int,
    count: Int
) : VKRequest<VKDocsGetResponse>("docs.get") {

    init {
        addParam("offset", offset)
        addParam("count", count)
        addParam("return_tags", 1)
    }

    override fun parse(r: JSONObject): VKDocsGetResponse {
        return VKDocsGetResponse.parse(r.getJSONObject(RESPONSE_FIELD_KEY))
    }

}

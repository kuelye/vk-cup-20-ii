package com.kuelye.vkcup20ii.a.api

import android.util.Log
import com.kuelye.vkcup20ii.a.model.VKDocument
import com.kuelye.vkcup20ii.core.api.COUNT_FIELD_KEY
import com.kuelye.vkcup20ii.core.api.ITEMS_FIELD_KEY
import com.kuelye.vkcup20ii.core.api.RESPONSE_FIELD_KEY
import com.kuelye.vkcup20ii.core.utils.map
import com.vk.api.sdk.requests.VKRequest
import org.json.JSONObject

class VKDocsGetRequest(
    offset: Int,
    count: Int
) : VKRequest<VKDocsGetRequest.Response>("docs.get") {

    companion object {
        private val TAG = VKDocsGetRequest::class.java.simpleName
    }

    init {
        Log.v(TAG, "GUB $offset, $count")
        addParam("offset", offset)
        addParam("count", count)
        addParam("return_tags", 1)
    }

    override fun parse(r: JSONObject): Response {
        return Response.parse(r.getJSONObject(RESPONSE_FIELD_KEY))
    }

    data class Response(
        val count: Int,
        val items: List<VKDocument>
    ) {
        companion object {
            fun parse(jo: JSONObject): Response {
                return Response(
                    count = jo.getInt(COUNT_FIELD_KEY),
                    items = jo.getJSONArray(ITEMS_FIELD_KEY).map { VKDocument.parse(it) }
                )
            }
        }
    }

}

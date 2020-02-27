package com.kuelye.vkcup20ii.a.model

import com.kuelye.vkcup20ii.core.api.COUNT_FIELD_KEY
import com.kuelye.vkcup20ii.core.api.ITEMS_FIELD_KEY
import com.kuelye.vkcup20ii.core.utils.map
import org.json.JSONObject

data class VKDocsGetResponse(
    val count: Int,
    val items: List<VKDocument>
) {
    companion object {
        fun parse(jo: JSONObject): VKDocsGetResponse {
            return VKDocsGetResponse(
                count = jo.getInt(COUNT_FIELD_KEY),
                items = jo.getJSONArray(ITEMS_FIELD_KEY).map { VKDocument.parse(it) }
            )
        }
    }
}
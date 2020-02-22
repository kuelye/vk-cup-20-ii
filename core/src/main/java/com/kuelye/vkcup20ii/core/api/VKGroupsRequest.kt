package com.kuelye.vkcup20ii.core.api

import com.kuelye.vkcup20ii.core.model.VKGroup
import com.vk.api.sdk.requests.VKRequest
import org.json.JSONObject

class VKGroupsRequest(
    extendedFields: Array<String>? = null
) : VKRequest<List<VKGroup>>("groups.get") {

    init {
        addParam("extended", 1)
        extendedFields?.let { addParam("fields", extendedFields.joinToString(",")) }
    }

    override fun parse(r: JSONObject): List<VKGroup> {
        val groups = r.getJSONObject("response").getJSONArray("items")
        val result = ArrayList<VKGroup>()
        for (i in 0 until groups.length()) {
            result.add(VKGroup.parse(groups.getJSONObject(i)))
        }
        return result
    }
}

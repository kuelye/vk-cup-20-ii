package com.kuelye.vkcup20ii.core.api

import android.util.Log
import com.kuelye.vkcup20ii.core.model.VKGroup
import com.vk.api.sdk.requests.VKRequest
import org.json.JSONObject

class VKGroupsRequest(
    extendedFields: Array<String>? = null,
    filter: String? = null
) : VKRequest<List<VKGroup>>("groups.get") {

    companion object {
        private val TAG = VKGroupsRequest::class.java.simpleName
    }

    init {
        Log.v(TAG, "{C}: ${extendedFields?.contentToString()}, $filter")
        addParam("extended", 1)
        extendedFields?.let { addParam("fields", extendedFields.joinToString(",")) }
        filter?.let { addParam("filter", filter) }
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

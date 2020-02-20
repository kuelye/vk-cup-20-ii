package com.kuelye.vkcup20ii.f.api

import com.kuelye.vkcup20ii.f.model.VKGroup
import com.kuelye.vkcup20ii.f.model.VKGroup.Companion.DESCRIPTION_FIELD_KEY
import com.kuelye.vkcup20ii.f.model.VKGroup.Companion.MEMBERS_COUNT_FIELD_KEY
import com.vk.api.sdk.requests.VKRequest
import org.json.JSONObject

class VKGroupsRequest : VKRequest<List<VKGroup>>("groups.get") {

    init {
        addParam("extended", 1)
        addParam("fields", "$DESCRIPTION_FIELD_KEY,$MEMBERS_COUNT_FIELD_KEY")
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

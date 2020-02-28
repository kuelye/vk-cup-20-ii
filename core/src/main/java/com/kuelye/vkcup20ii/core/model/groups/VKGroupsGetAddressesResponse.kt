package com.kuelye.vkcup20ii.core.model.groups

import com.kuelye.vkcup20ii.core.api.COUNT_FIELD_KEY
import com.kuelye.vkcup20ii.core.api.ITEMS_FIELD_KEY
import com.kuelye.vkcup20ii.core.utils.map
import org.json.JSONObject

data class VKGroupsGetAddressesResponse(
    val count: Int,
    val addresses: List<VKAddress>
) {
    companion object {
        fun parse(jo: JSONObject): VKGroupsGetAddressesResponse {
            return VKGroupsGetAddressesResponse(
                count = jo.getInt(COUNT_FIELD_KEY),
                addresses = jo.getJSONArray(ITEMS_FIELD_KEY).map {
                    VKAddress.parse(
                        it
                    )
                }
            )
        }
    }
}
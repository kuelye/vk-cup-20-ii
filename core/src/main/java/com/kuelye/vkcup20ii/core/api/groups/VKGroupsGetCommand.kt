package com.kuelye.vkcup20ii.core.api.groups

import com.kuelye.vkcup20ii.core.api.*
import com.kuelye.vkcup20ii.core.api.groups.VKGroupsGetCommand.Response
import com.kuelye.vkcup20ii.core.model.groups.VKAddress
import com.kuelye.vkcup20ii.core.model.database.VKCity
import com.kuelye.vkcup20ii.core.model.groups.VKGroup
import com.kuelye.vkcup20ii.core.model.groups.VKGroup.Field.ADDRESSES
import com.kuelye.vkcup20ii.core.model.groups.VKGroupsGetAddressesResponse
import com.kuelye.vkcup20ii.core.utils.ceil
import com.kuelye.vkcup20ii.core.utils.map
import com.vk.api.sdk.VKApiManager
import com.vk.api.sdk.VKApiResponseParser
import com.vk.api.sdk.VKMethodCall
import com.vk.api.sdk.exceptions.VKApiIllegalResponseException
import com.vk.api.sdk.internal.ApiCommand
import org.json.JSONException
import org.json.JSONObject

class VKGroupsGetCommand(
    private val offset: Int,
    private val count: Int,
    private val extendedFields: List<VKGroup.Field>? = null,
    private val filter: String? = null
) : ApiCommand<Response>() {

    companion object {
        private val TAG = VKGroupsGetCommand::class.java.simpleName
        private const val ADDRESSES_COUNT_PER_REQUEST = 10
    }

    override fun onExecute(manager: VKApiManager): Response {
        val callBuilder = VKMethodCall.Builder()
            .method("groups.get")
            .args("offset", offset)
            .args("count", count)
            .args("extended", 1)
            .version(manager.config.version)
        extendedFields?.let { callBuilder.args("fields",
            extendedFields.mapNotNull { it.key }.joinToString(",")) }
        filter?.let { callBuilder.args("filter", filter) }
        val groupsGetResult = manager.execute(callBuilder.build(), GroupsGetParser())

        if (extendedFields?.contains(ADDRESSES) == true) {
            val cityIds = mutableSetOf<Int>()
            for (group in groupsGetResult.items) {
                if (group.addressesEnabled == true) {
                    group.addresses = getAddresses(manager, group.id)
                    cityIds.addAll(group.addresses!!.map { it.cityId })
                }
            }
            if (cityIds.isNotEmpty()) {
                val cities = getCities(manager, cityIds)
                for (group in groupsGetResult.items) {
                    if (group.addresses.isNullOrEmpty()) continue
                    for (address in group.addresses!!) {
                        address.cityTitle = cities.firstOrNull { it.id == address.cityId }?.title
                    }
                }
            }
        }

        return groupsGetResult
    }

    private fun getAddresses(manager: VKApiManager, groupId: Int): List<VKAddress> {
        var response = getAddresses(manager, groupId, 0)
        return if (response.addresses.size < response.count) {
            val addresses = response.addresses.toMutableList()
            for (i in 1..(ceil(response.count, ADDRESSES_COUNT_PER_REQUEST))) {
                response = getAddresses(manager, groupId, ADDRESSES_COUNT_PER_REQUEST * i)
                addresses.addAll(response.addresses)
            }
            addresses
        } else {
            response.addresses
        }
    }

    private fun getAddresses(
        manager: VKApiManager, groupId: Int, offset: Int
    ): VKGroupsGetAddressesResponse {
        val call = VKMethodCall.Builder()
            .method("groups.getAddresses")
            .args("group_id", groupId)
            .args("fields", "$ADDRESS_FIELD_KEY,$LATITUDE_FIELD_KEY,$LONGITUDE_FIELD_KEY,$CITY_ID_FIELD_KEY")
            .args("offset", offset)
            .args("count", ADDRESSES_COUNT_PER_REQUEST)
            .version(manager.config.version)
            .build()
        return manager.execute(call, GroupsGetAddressesParser())
    }

    private fun getCities(
        manager: VKApiManager, cityIds: Set<Int>
    ): List<VKCity> {
        val call = VKMethodCall.Builder()
            .method("database.getCitiesById")
            .args("city_ids", cityIds.joinToString(","))
            .version(manager.config.version)
            .build()
        return manager.execute(call, DatabaseGetCitiesByIdParser())
    }

    class Response(
        val count: Int,
        val items: List<VKGroup>
    ) {
        companion object {
            fun parse(jo: JSONObject): Response {
                return Response(
                    count = jo.getInt(COUNT_FIELD_KEY),
                    items = jo.getJSONArray(ITEMS_FIELD_KEY).map { VKGroup.parse(it) }
                )
            }
        }

        override fun toString(): String {
            return "Response(count=$count, items.size=${items.size})"
        }
    }

    private class GroupsGetParser : VKApiResponseParser<Response> {
        override fun parse(response: String): Response {
            try {
                return Response.parse(JSONObject(response).getJSONObject(RESPONSE_FIELD_KEY))
            } catch (e: JSONException) {
                throw VKApiIllegalResponseException(e)
            }
        }
    }

    private class GroupsGetAddressesParser : VKApiResponseParser<VKGroupsGetAddressesResponse> {
        override fun parse(response: String): VKGroupsGetAddressesResponse {
            try {
                return VKGroupsGetAddressesResponse.parse(JSONObject(response)
                    .getJSONObject(RESPONSE_FIELD_KEY))
            } catch (e: JSONException) {
                throw VKApiIllegalResponseException(e)
            }
        }
    }

    private class DatabaseGetCitiesByIdParser : VKApiResponseParser<List<VKCity>> {
        override fun parse(response: String): List<VKCity> {
            try {
                return JSONObject(response)
                    .getJSONArray(RESPONSE_FIELD_KEY)
                    .map { VKCity.parse(it) }
            } catch (e: JSONException) {
                throw VKApiIllegalResponseException(e)
            }
        }
    }

}

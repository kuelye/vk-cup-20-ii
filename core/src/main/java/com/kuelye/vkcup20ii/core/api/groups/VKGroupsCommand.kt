package com.kuelye.vkcup20ii.core.api.groups

import android.util.Log
import com.kuelye.vkcup20ii.core.api.*
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

class VKGroupsCommand(
    private val extendedFields: Array<VKGroup.Field>? = null,
    private val filter: String? = null
) : ApiCommand<List<VKGroup>>() {

    companion object {
        private val TAG = VKGroupsCommand::class.java.simpleName
        private const val ADDRESSES_COUNT_PER_REQUEST = 10
    }

    override fun onExecute(manager: VKApiManager): List<VKGroup> {
        val callBuilder = VKMethodCall.Builder()
            .method("groups.get")
            .args("extended", 1)
            .version(manager.config.version)
        extendedFields?.let { callBuilder.args("fields",
            extendedFields.mapNotNull { it.key }.joinToString(",")) }
        filter?.let { callBuilder.args("filter", filter) }
        val groups = manager.execute(callBuilder.build(),
            GroupsGetParser()
        )

        if (extendedFields?.contains(ADDRESSES) == true) {
            val cityIds = mutableSetOf<Int>()
            for (group in groups) {
                if (group.addressesEnabled == true) {
                    group.addresses = getAddresses(manager, group.id)
                    cityIds.addAll(group.addresses!!.map { it.cityId })
                }
            }
            Log.v(TAG, "$cityIds")
            if (cityIds.isNotEmpty()) {
                val cities = getCities(manager, cityIds)
                Log.v(TAG, "$cities")
                for (group in groups) {
                    if (group.addresses.isNullOrEmpty()) continue
                    for (address in group.addresses!!) {
                        address.cityTitle = cities.firstOrNull { it.id == address.cityId }?.title
                    }
                }
            }
        }

        return groups
    }

    private fun getAddresses(manager: VKApiManager, groupId: Int): List<VKAddress> {
        var response = getAddresses(manager, groupId, 0)
        return if (response.addresses.size < response.count) {
            val addresses = response.addresses.toMutableList()
            for (i in 1..(ceil(response.count,
                ADDRESSES_COUNT_PER_REQUEST
            ))) {
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
            .args("count",
                ADDRESSES_COUNT_PER_REQUEST
            )
            .version(manager.config.version)
            .build()
        return manager.execute(call,
            GroupsGetAddressesParser()
        )
    }

    private fun getCities(
        manager: VKApiManager, cityIds: Set<Int>
    ): List<VKCity> {
        val call = VKMethodCall.Builder()
            .method("database.getCitiesById")
            .args("city_ids", cityIds.joinToString(","))
            .version(manager.config.version)
            .build()
        return manager.execute(call,
            DatabaseGetCitiesByIdParser()
        )
    }

    private class GroupsGetParser : VKApiResponseParser<List<VKGroup>> {
        override fun parse(response: String): List<VKGroup> {
            try {
                return JSONObject(response)
                    .getJSONObject(RESPONSE_FIELD_KEY)
                    .getJSONArray(ITEMS_FIELD_KEY)
                    .map { VKGroup.parse(it) }
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

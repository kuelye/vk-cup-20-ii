package com.kuelye.vkcup20ii.core.model

import org.json.JSONObject

data class VKPlace(
    val latitude: Double,
    val longitude: Double,
    val address: String
) {

    companion object {
        private const val LATITUDE_FIELD_KEY = "id"
        private const val LONGITUDE_FIELD_KEY = "name"
        private const val ADDRESS_FIELD_KEY = "screen_name"

        fun parse(jo: JSONObject): VKPlace {
            return VKPlace(
                latitude = jo.getDouble(LATITUDE_FIELD_KEY),
                longitude = jo.getDouble(LONGITUDE_FIELD_KEY),
                address = jo.getString(ADDRESS_FIELD_KEY)
            )
        }
    }

}
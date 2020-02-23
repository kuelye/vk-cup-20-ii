package com.kuelye.vkcup20ii.core.model

import com.google.android.gms.maps.model.LatLng
import com.kuelye.vkcup20ii.core.api.*
import org.json.JSONObject

data class VKAddress(
    val id: Int,
    val latitude: Double,
    val longitude: Double,
    val address: String
) {

    companion object {
        fun parse(jo: JSONObject): VKAddress {
            return VKAddress(
                id = jo.getInt(ID_FIELD_KEY),
                latitude = jo.getDouble(LATITUDE_FIELD_KEY),
                longitude = jo.getDouble(LONGITUDE_FIELD_KEY),
                address = jo.getString(ADDRESS_FIELD_KEY)
            )
        }
    }

    val position: LatLng
        get() = LatLng(latitude, longitude)

}
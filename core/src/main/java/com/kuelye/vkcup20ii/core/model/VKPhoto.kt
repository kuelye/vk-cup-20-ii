package com.kuelye.vkcup20ii.core.model

import com.google.android.gms.maps.model.LatLng
import com.kuelye.vkcup20ii.core.api.DATE_FIELD_KEY
import com.kuelye.vkcup20ii.core.api.ID_FIELD_KEY
import com.kuelye.vkcup20ii.core.api.VKPhotoColumns.Companion.LAT_FIELD_KEY
import com.kuelye.vkcup20ii.core.api.VKPhotoColumns.Companion.LONG_FIELD_KEY
import com.kuelye.vkcup20ii.core.api.VKPhotoColumns.Companion.SIZES_FIELD_KEY
import com.kuelye.vkcup20ii.core.api.VKPhotoColumns.Companion.SIZE_HEIGHT_FIELD_KEY
import com.kuelye.vkcup20ii.core.api.VKPhotoColumns.Companion.SIZE_TYPE_FIELD_KEY
import com.kuelye.vkcup20ii.core.api.VKPhotoColumns.Companion.SIZE_URL_FIELD_KEY
import com.kuelye.vkcup20ii.core.api.VKPhotoColumns.Companion.SIZE_WIDTH_FIELD_KEY
import com.kuelye.vkcup20ii.core.utils.map
import org.json.JSONObject

data class VKPhoto(
    val id: Int,
    val date: Int,
    val lat: Double?,
    val lng: Double?,
    val sizes: List<Size>
) {
    companion object {
        private val TAG = VKPhoto::class.java.simpleName

        fun parse(jo: JSONObject): VKPhoto {
            return VKPhoto(
                id = jo.getInt(ID_FIELD_KEY),
                date = jo.getInt(DATE_FIELD_KEY),
                lat = if (jo.has(LAT_FIELD_KEY)) jo.getDouble(LAT_FIELD_KEY) else null,
                lng = if (jo.has(LONG_FIELD_KEY)) jo.getDouble(LONG_FIELD_KEY) else null,
                sizes = jo.getJSONArray(SIZES_FIELD_KEY).map { Size.parse(it) }
            )
        }
    }

    val position: LatLng?
        get() = if (lat == null || lng == null) null else LatLng(lat, lng)

    val iconPhoto: String
        get() = (sizes.firstOrNull { it.type == "p" } ?: sizes.first()).url

    val photo: String
        get() = sizes.last().url

    class Size(
        val type: String,
        val url: String,
        val width: Int,
        val height: Int
    ) {
        companion object {
            fun parse(jo: JSONObject): Size {
                return Size(
                    type = jo.getString(SIZE_TYPE_FIELD_KEY),
                    url = jo.getString(SIZE_URL_FIELD_KEY),
                    width = jo.getInt(SIZE_WIDTH_FIELD_KEY),
                    height = jo.getInt(SIZE_HEIGHT_FIELD_KEY)
                )
            }
        }
    }

}
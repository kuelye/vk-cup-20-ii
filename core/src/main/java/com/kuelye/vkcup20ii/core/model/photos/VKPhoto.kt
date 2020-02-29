package com.kuelye.vkcup20ii.core.model.photos

import com.google.android.gms.maps.model.LatLng
import com.kuelye.vkcup20ii.core.api.DATE_FIELD_KEY
import com.kuelye.vkcup20ii.core.api.ID_FIELD_KEY
import com.kuelye.vkcup20ii.core.api.OWNER_ID_FIELD_KEY
import com.kuelye.vkcup20ii.core.api.VKPhotoColumns.Companion.ALBUM_ID_FIELD_KEY
import com.kuelye.vkcup20ii.core.api.VKPhotoColumns.Companion.LAT_FIELD_KEY
import com.kuelye.vkcup20ii.core.api.VKPhotoColumns.Companion.LONG_FIELD_KEY
import com.kuelye.vkcup20ii.core.api.VKPhotoColumns.Companion.SIZES_FIELD_KEY
import com.kuelye.vkcup20ii.core.api.VKPhotoSizeColumns.Companion.URL_FIELD_KEY
import com.kuelye.vkcup20ii.core.model.Identifiable
import com.kuelye.vkcup20ii.core.model.misc.VKPhotoSize
import com.kuelye.vkcup20ii.core.utils.map
import org.json.JSONObject

data class VKPhoto(
    override val id: Int,
    val ownerId: Int,
    val albumId: Int,
    val date: Int,
    val lat: Double?,
    val lng: Double?,
    val sizes: List<VKPhotoSize>
) : Identifiable {
    companion object {
        private val TAG = VKPhoto::class.java.simpleName
        private const val PHOTO_ATTACHMENT_TEMPLATE = "photo%s_%s"

        fun parse(jo: JSONObject): VKPhoto {
            return VKPhoto(
                id = jo.getInt(ID_FIELD_KEY),
                ownerId = jo.getInt(OWNER_ID_FIELD_KEY),
                albumId = jo.getInt(ALBUM_ID_FIELD_KEY),
                date = jo.getInt(DATE_FIELD_KEY),
                lat = if (jo.has(LAT_FIELD_KEY)) jo.getDouble(LAT_FIELD_KEY) else null,
                lng = if (jo.has(LONG_FIELD_KEY)) jo.getDouble(LONG_FIELD_KEY) else null,
                sizes = jo.getJSONArray(SIZES_FIELD_KEY).
                    map { VKPhotoSize.parse(it, URL_FIELD_KEY) }
            )
        }
    }

    val position: LatLng?
        get() = if (lat == null || lng == null) null else LatLng(lat, lng)

    val iconPhoto: String
        get() = (sizes.firstOrNull { it.type == "x" } ?: sizes.first()).url

    val photo: String
        get() = sizes.last().url

    val attachment: String
        get() = String.format(PHOTO_ATTACHMENT_TEMPLATE, ownerId, id)

    class DefaultComparator : Comparator<VKPhoto> {
        override fun compare(o1: VKPhoto, o2: VKPhoto): Int = o2.date.compareTo(o1.date)
    }

}
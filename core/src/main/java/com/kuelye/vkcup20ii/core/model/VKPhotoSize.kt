package com.kuelye.vkcup20ii.core.model

import com.kuelye.vkcup20ii.core.api.VKPhotoSizeColumns.Companion.HEIGHT_FIELD_KEY
import com.kuelye.vkcup20ii.core.api.VKPhotoSizeColumns.Companion.SRC_FIELD_KEY
import com.kuelye.vkcup20ii.core.api.VKPhotoSizeColumns.Companion.TYPE_FIELD_KEY
import com.kuelye.vkcup20ii.core.api.VKPhotoSizeColumns.Companion.WIDTH_FIELD_KEY
import org.json.JSONObject

class VKPhotoSize(
    val type: String,
    val url: String,
    val width: Int,
    val height: Int
) {
    companion object {
        private val ICON_TYPES = arrayOf("s", "m", "x")

        fun parse(jo: JSONObject, urlFieldKey: String = SRC_FIELD_KEY): VKPhotoSize {
            return VKPhotoSize(
                type = jo.getString(TYPE_FIELD_KEY),
                url = jo.getString(urlFieldKey),
                width = jo.getInt(WIDTH_FIELD_KEY),
                height = jo.getInt(HEIGHT_FIELD_KEY)
            )
        }

        fun getIconUrl(sizes: List<VKPhotoSize>): String? {
            var iconI: Int? = null
            var iconUrl: String? = null
            for (size in sizes) {
                val i = ICON_TYPES.indexOf(size.type)
                if (iconI == null || i > iconI) {
                    iconI = i
                    iconUrl = size.url
                }
            }
            return iconUrl
        }
    }
}
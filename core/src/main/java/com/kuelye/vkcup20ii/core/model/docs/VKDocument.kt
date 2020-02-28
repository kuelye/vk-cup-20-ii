package com.kuelye.vkcup20ii.core.model.docs

import com.kuelye.vkcup20ii.core.api.*
import com.kuelye.vkcup20ii.core.api.VKDocumentColumns.Companion.EXT_FIELD_KEY
import com.kuelye.vkcup20ii.core.api.VKDocumentColumns.Companion.PHOTO_FIELD_KEY
import com.kuelye.vkcup20ii.core.api.VKDocumentColumns.Companion.PREVIEW_FIELD_KEY
import com.kuelye.vkcup20ii.core.api.VKDocumentColumns.Companion.SIZES_FIELD_KEY
import com.kuelye.vkcup20ii.core.api.VKDocumentColumns.Companion.SIZE_FIELD_KEY
import com.kuelye.vkcup20ii.core.api.VKDocumentColumns.Companion.TAGS_FIELD_KEY
import com.kuelye.vkcup20ii.core.api.VKDocumentColumns.Companion.URL_FIELD_KEY
import com.kuelye.vkcup20ii.core.model.Identifiable
import com.kuelye.vkcup20ii.core.model.misc.VKPhotoSize
import com.kuelye.vkcup20ii.core.utils.map
import com.kuelye.vkcup20ii.core.utils.toStringList
import org.json.JSONObject

data class VKDocument(
    override val id: Int,
    val ownerId: Int,
    var title: String,
    val size: Int,
    val ext: String,
    val url: String,
    val date: Long,
    val type: Int,
    val tags: List<String>?,
    val iconUrl: String?
) : Identifiable {

    companion object {
        private val TAG = VKDocument::class.java.simpleName

        fun parse(jo: JSONObject): VKDocument {
            return VKDocument(
                id = jo.getInt(ID_FIELD_KEY),
                ownerId = jo.getInt(OWNER_ID_FIELD_KEY),
                title = jo.getString(TITLE_FIELD_KEY),
                size = jo.getInt(SIZE_FIELD_KEY),
                ext = jo.getString(EXT_FIELD_KEY),
                url = jo.getString(URL_FIELD_KEY),
                date = jo.getLong(DATE_FIELD_KEY) * 1000,
                type = jo.getInt(TYPE_FIELD_KEY),
                tags = if (jo.has(TAGS_FIELD_KEY)) jo.getJSONArray(TAGS_FIELD_KEY).toStringList() else null,
                iconUrl = getIconPhoto(jo)
            )
        }

        private fun getIconPhoto(jo: JSONObject): String? {
            if (!jo.has(PREVIEW_FIELD_KEY)) return null
            val preview = jo.getJSONObject(PREVIEW_FIELD_KEY)
            if (!preview.has(PHOTO_FIELD_KEY)) return null
            val sizes = preview.getJSONObject(PHOTO_FIELD_KEY).getJSONArray(SIZES_FIELD_KEY)
                .map { VKPhotoSize.parse(it) }
            return VKPhotoSize.getIconUrl(sizes)
        }
    }

    class DefaultComparator : Comparator<VKDocument> {
        override fun compare(o1: VKDocument, o2: VKDocument): Int = o2.date.compareTo(o1.date)
    }

}
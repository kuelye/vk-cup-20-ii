package com.kuelye.vkcup20ii.core.model.photos

import com.kuelye.vkcup20ii.core.api.ID_FIELD_KEY
import com.kuelye.vkcup20ii.core.api.TITLE_FIELD_KEY
import com.kuelye.vkcup20ii.core.api.VKPhotoAlbumColumns.Companion.SIZES_FIELD_KEY
import com.kuelye.vkcup20ii.core.api.VKPhotoAlbumColumns.Companion.SIZE_FIELD_KEY
import com.kuelye.vkcup20ii.core.api.VKPhotoAlbumColumns.Companion.CREATED_FIELD_KEY
import com.kuelye.vkcup20ii.core.api.VKPhotoSizeColumns.Companion.SRC_FIELD_KEY
import com.kuelye.vkcup20ii.core.model.Identifiable
import com.kuelye.vkcup20ii.core.model.misc.VKPhotoSize
import com.kuelye.vkcup20ii.core.utils.map
import org.json.JSONObject

class VKPhotoAlbum(
    override val id: Int,
    val title: String,
    val created: Int?,
    val size: Int,
    val sizes: List<VKPhotoSize>
) : Identifiable {

    companion object {
        private val TAG = VKPhotoAlbum::class.java.simpleName

        fun parse(jo: JSONObject): VKPhotoAlbum {
            return VKPhotoAlbum(
                id = jo.getInt(ID_FIELD_KEY),
                title = jo.getString(TITLE_FIELD_KEY),
                created = if (jo.has(CREATED_FIELD_KEY)) jo.getInt(CREATED_FIELD_KEY) else null,
                size = jo.getInt(SIZE_FIELD_KEY),
                sizes = jo.getJSONArray(SIZES_FIELD_KEY).
                    map { VKPhotoSize.parse(it, SRC_FIELD_KEY) }
            )
        }
    }

    val photo: String
        get() = sizes.last().url

    class DefaultComparator : Comparator<VKPhotoAlbum> {
        override fun compare(o1: VKPhotoAlbum, o2: VKPhotoAlbum): Int {
            return if (o1.created == null) {
                if (o2.created == null) o2.id.compareTo(o1.id) else -1
            } else {
                if (o2.created == null) 1 else o2.created.compareTo(o1.created)
            }
        }
    }

}
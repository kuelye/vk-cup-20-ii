package com.kuelye.vkcup20ii.a.model

import android.content.Context
import androidx.annotation.DrawableRes
import com.kuelye.vkcup20ii.a.R
import com.kuelye.vkcup20ii.core.api.*
import com.kuelye.vkcup20ii.core.api.VKDocumentColumns.Companion.EXT_FIELD_KEY
import com.kuelye.vkcup20ii.core.api.VKDocumentColumns.Companion.PHOTO_FIELD_KEY
import com.kuelye.vkcup20ii.core.api.VKDocumentColumns.Companion.PREVIEW_FIELD_KEY
import com.kuelye.vkcup20ii.core.api.VKDocumentColumns.Companion.SIZES_FIELD_KEY
import com.kuelye.vkcup20ii.core.api.VKDocumentColumns.Companion.SIZE_FIELD_KEY
import com.kuelye.vkcup20ii.core.api.VKDocumentColumns.Companion.TAGS_FIELD_KEY
import com.kuelye.vkcup20ii.core.api.VKDocumentColumns.Companion.URL_FIELD_KEY
import com.kuelye.vkcup20ii.core.model.VKPhotoSize
import com.kuelye.vkcup20ii.core.utils.formatShort
import com.kuelye.vkcup20ii.core.utils.formatTime
import com.kuelye.vkcup20ii.core.utils.map
import com.kuelye.vkcup20ii.core.utils.toStringList
import org.json.JSONObject
import java.util.*
import java.util.Locale.ENGLISH

data class VKDocument(
    val id: Int,
    val ownerId: Int,
    val title: String,
    val size: Int,
    val ext: String,
    val url: String,
    val date: Long,
    val type: Type,
    val tags: List<String>?,
    val iconUrl: String?
) {
    companion object {
        private val TAG = VKDocument::class.java.simpleName
        private const val INFO_TEMPLATE = "%s • %s • %s"
        private const val SIZE_TEMPLATE = "%s %s"
        private const val BYTES_IN_KB = 1024
        private const val BYTES_IN_MB = BYTES_IN_KB * 1024
        private const val BYTES_IN_GB = BYTES_IN_MB * 1024

        fun parse(jo: JSONObject): VKDocument {
            return VKDocument(
                id = jo.getInt(ID_FIELD_KEY),
                ownerId = jo.getInt(OWNER_ID_FIELD_KEY),
                title = jo.getString(TITLE_FIELD_KEY),
                size = jo.getInt(SIZE_FIELD_KEY),
                ext = jo.getString(EXT_FIELD_KEY),
                url = jo.getString(URL_FIELD_KEY),
                date = jo.getLong(DATE_FIELD_KEY) * 1000,
                type = Type.forValue(jo.getInt(TYPE_FIELD_KEY)),
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

    fun getFormattedInfo(context: Context): String =
        String.format(INFO_TEMPLATE, ext.toUpperCase(ENGLISH),
            getFormattedSize(context), formatTime(context, date))

    private fun getFormattedSize(context: Context): String {
        val (value, unit) = when {
            size < BYTES_IN_KB -> Pair(size.toString(), R.string.document_size_b)
            size < BYTES_IN_MB -> Pair(formatShort(size.toFloat() / BYTES_IN_KB, 0), R.string.document_size_kb)
            size < BYTES_IN_GB -> Pair(formatShort(size.toFloat() / BYTES_IN_MB), R.string.document_size_mb)
            else -> Pair(formatShort(size.toFloat() / BYTES_IN_GB), R.string.document_size_gb)
        }
        return String.format(SIZE_TEMPLATE, value, context.getString(unit))
    }

    enum class Type(
        val value: Int,
        @DrawableRes val drawable: Int
    ) {
        TEXT(1, R.drawable.ic_placeholder_document_text_72),
        ARCHIVE(2, R.drawable.ic_placeholder_document_archive_72),
        GIF(3, R.drawable.ic_placeholder_document_image_72),
        IMAGE(4, R.drawable.ic_placeholder_document_image_72),
        AUDIO(5, R.drawable.ic_placeholder_document_music_72),
        VIDEO(6, R.drawable.ic_placeholder_document_video_72),
        EBOOK(7, R.drawable.ic_placeholder_document_book_72),
        UNKNOWN(8, R.drawable.ic_placeholder_document_other_72);

        companion object {
            fun forValue(value: Int): Type = values().firstOrNull { it.value == value } ?: UNKNOWN
        }
    }

    class DateComparator : Comparator<VKDocument> {
        override fun compare(o1: VKDocument, o2: VKDocument): Int = o2.date.compareTo(o1.date)
    }

}
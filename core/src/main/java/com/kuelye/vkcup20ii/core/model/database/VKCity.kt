package com.kuelye.vkcup20ii.core.model.database

import com.kuelye.vkcup20ii.core.api.ID_FIELD_KEY
import com.kuelye.vkcup20ii.core.api.TITLE_FIELD_KEY
import org.json.JSONObject

data class VKCity(
    val id: Int,
    val title: String
) {

    companion object {
        fun parse(jo: JSONObject): VKCity {
            return VKCity(
                id = jo.getInt(ID_FIELD_KEY),
                title = jo.getString(TITLE_FIELD_KEY)
            )
        }
    }

}
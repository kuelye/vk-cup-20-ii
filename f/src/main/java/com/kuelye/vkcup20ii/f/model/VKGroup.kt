package com.kuelye.vkcup20ii.f.model

import org.json.JSONObject

class VKGroup(
    val id: Long,
    val name: String,
    val photo200: String
) {

    companion object {
        fun parse(r: JSONObject): VKGroup {
            return VKGroup(
                id = r.getLong("id"),
                name = r.getString("name"),
                photo200 = r.getString("photo_200")
            )
        }
    }

}
package com.kuelye.vkcup20ii.f.model

import org.json.JSONObject

class VKGroup(
    val id: Long,
    val name: String,
    val member: Boolean,
    val photo200: String
) {

    companion object {
        fun parse(r: JSONObject): VKGroup {
            return VKGroup(
                id = r.getLong("id"),
                name = r.getString("name"),
                member = r.getInt("is_member") == 1,
                photo200 = r.getString("photo_200")
            )
        }
    }

}
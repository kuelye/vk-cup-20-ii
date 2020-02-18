package com.kuelye.vkcup20ii.f.model

import org.json.JSONObject

class VKGroup(
    val name: String,
    val photo200: String
) {

    companion object {
        fun parse(r: JSONObject): VKGroup {
            return VKGroup(r.getString("name"), r.getString("photo_200"))
        }
    }

}
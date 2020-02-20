package com.kuelye.vkcup20ii.f.model

import org.json.JSONObject

data class VKGroup(
    val id: Int,
    val name: String,
    val description: String,
    val isMember: Boolean,
    val photo200: String,
    val membersCount: Int,
    var friendsCount: Int? = null
) {

    @Suppress("MemberVisibilityCanBePrivate")
    companion object {
        const val ID_FIELD_KEY = "id"
        const val NAME_FIELD_KEY = "name"
        const val DESCRIPTION_FIELD_KEY = "description"
        const val IS_MEMBER_FIELD_KEY = "is_member"
        const val PHOTO_200_FIELD_KEY = "photo_200"
        const val MEMBERS_COUNT_FIELD_KEY = "members_count"

        fun parse(r: JSONObject): VKGroup {
            return VKGroup(
                id = r.getInt(ID_FIELD_KEY),
                name = r.getString(NAME_FIELD_KEY),
                description = r.getString(DESCRIPTION_FIELD_KEY),
                isMember = r.getInt(IS_MEMBER_FIELD_KEY) == 1,
                photo200 = r.getString(PHOTO_200_FIELD_KEY),
                membersCount = r.getInt(MEMBERS_COUNT_FIELD_KEY)
            )
        }
    }

}
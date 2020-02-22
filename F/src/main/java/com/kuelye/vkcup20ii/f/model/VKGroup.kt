package com.kuelye.vkcup20ii.f.model

import org.json.JSONObject

data class VKGroup(
    val id: Int,
    val name: String,
    val screenName: String,
    val description: String,
    val isMember: Boolean,
    val photo200: String,
    val membersCount: Int,
    var friendsCount: Int? = null,
    var lastPostDate: Long? = null
) {

    @Suppress("MemberVisibilityCanBePrivate")
    companion object {
        const val ID_FIELD_KEY = "id"
        const val NAME_FIELD_KEY = "name"
        const val SCREEN_NAME_FIELD_KEY = "screen_name"
        const val DESCRIPTION_FIELD_KEY = "description"
        const val IS_MEMBER_FIELD_KEY = "is_member"
        const val PHOTO_200_FIELD_KEY = "photo_200"
        const val MEMBERS_COUNT_FIELD_KEY = "members_count"

        const val NO_POSTS_DATE = -1L

        fun parse(jo: JSONObject): VKGroup {
            return VKGroup(
                id = jo.getInt(ID_FIELD_KEY),
                name = jo.getString(NAME_FIELD_KEY),
                screenName = jo.getString(SCREEN_NAME_FIELD_KEY),
                description = jo.getString(DESCRIPTION_FIELD_KEY),
                isMember = jo.getInt(IS_MEMBER_FIELD_KEY) == 1,
                photo200 = jo.getString(PHOTO_200_FIELD_KEY),
                membersCount = jo.getInt(MEMBERS_COUNT_FIELD_KEY)
            )
        }
    }

}
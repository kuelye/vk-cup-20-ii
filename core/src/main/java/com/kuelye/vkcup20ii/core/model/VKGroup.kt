package com.kuelye.vkcup20ii.core.model

import android.util.Log
import org.json.JSONObject

data class VKGroup(
    val id: Int,
    val name: String,
    val screenName: String,
    val isMember: Boolean,
    val photo200: String,
    val description: String? = null,
    val membersCount: Int? = null,
    val place: VKPlace? = null,
    var friendsCount: Int? = null,
    var lastPostDate: Long? = null
) {

    companion object {
        private val TAG = VKGroup::class.java.simpleName

        const val DESCRIPTION_FIELD_KEY = "description"
        const val MEMBERS_COUNT_FIELD_KEY = "members_count"
        const val PLACE_KEY = "place"

        private const val ID_FIELD_KEY = "id"
        private const val NAME_FIELD_KEY = "name"
        private const val SCREEN_NAME_FIELD_KEY = "screen_name"
        private const val IS_MEMBER_FIELD_KEY = "is_member"
        private const val PHOTO_200_FIELD_KEY = "photo_200"

        const val NO_POSTS_DATE = -1L

        fun parse(jo: JSONObject): VKGroup {
            return VKGroup(
                id = jo.getInt(ID_FIELD_KEY),
                name = jo.getString(NAME_FIELD_KEY),
                screenName = jo.getString(SCREEN_NAME_FIELD_KEY),
                isMember = jo.getInt(IS_MEMBER_FIELD_KEY) == 1,
                photo200 = jo.getString(PHOTO_200_FIELD_KEY),
                description = if (jo.has(DESCRIPTION_FIELD_KEY)) jo.getString(DESCRIPTION_FIELD_KEY) else null,
                membersCount = if (jo.has(MEMBERS_COUNT_FIELD_KEY)) jo.getInt(MEMBERS_COUNT_FIELD_KEY) else null,
                place = if (jo.has(PLACE_KEY)) VKPlace.parse(jo.getJSONObject(PLACE_KEY)) else null
            )
        }
    }

}
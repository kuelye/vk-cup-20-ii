package com.kuelye.vkcup20ii.core.model

import com.kuelye.vkcup20ii.core.api.*
import org.json.JSONObject

data class VKGroup(
    val id: Int,
    val name: String,
    val screenName: String,
    val isMember: Boolean,
    val photo200: String,

    val description: String? = null,
    val membersCount: Int? = null,
    val addressesEnabled: Boolean? = null,

    var friendsCount: Int? = null,
    var lastPostDate: Long? = null,
    var addresses: List<VKAddress>? = null
) {

    companion object {
        private val TAG = VKGroup::class.java.simpleName
        const val NO_POSTS_DATE = -1L

        fun parse(jo: JSONObject): VKGroup {
            return VKGroup(
                id = jo.getInt(ID_FIELD_KEY),
                name = jo.getString(NAME_FIELD_KEY),
                screenName = jo.getString(SCREEN_NAME_FIELD_KEY),
                isMember = jo.getInt(IS_MEMBER_FIELD_KEY) == 1,
                photo200 = jo.getString(PHOTO_200_FIELD_KEY),
                description = if (jo.has(DESCRIPTION_FIELD_KEY))
                    jo.getString(DESCRIPTION_FIELD_KEY) else null,
                membersCount = if (jo.has(MEMBERS_COUNT_FIELD_KEY))
                    jo.getInt(MEMBERS_COUNT_FIELD_KEY) else null,
                addressesEnabled = if (jo.has(ADDRESSES_FIELD_KEY))
                    jo.getJSONObject(ADDRESSES_FIELD_KEY).getBoolean("is_enabled") else null
            )
        }
    }

    enum class Field(
        val key: String? = null
    ) {
        DESCRIPTION(DESCRIPTION_FIELD_KEY),
        MEMBERS_COUNT(MEMBERS_COUNT_FIELD_KEY),
        ADDRESSES(ADDRESSES_FIELD_KEY)
    }

}
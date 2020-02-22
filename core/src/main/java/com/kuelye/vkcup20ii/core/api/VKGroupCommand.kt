package com.kuelye.vkcup20ii.core.api

import com.kuelye.vkcup20ii.core.model.VKGroup
import com.kuelye.vkcup20ii.core.model.VKGroup.Companion.DESCRIPTION_FIELD_KEY
import com.kuelye.vkcup20ii.core.model.VKGroup.Companion.MEMBERS_COUNT_FIELD_KEY
import com.kuelye.vkcup20ii.core.model.VKGroup.Companion.NO_POSTS_DATE
import com.vk.api.sdk.VKApiManager
import com.vk.api.sdk.VKApiResponseParser
import com.vk.api.sdk.VKMethodCall
import com.vk.api.sdk.exceptions.VKApiIllegalResponseException
import com.vk.api.sdk.internal.ApiCommand
import org.json.JSONException
import org.json.JSONObject

class VKGroupCommand(
    private val groupId: Int
) : ApiCommand<VKGroup?>() {

    companion object {
        private val TAG = VKGroupCommand::class.java.simpleName
    }

    override fun onExecute(manager: VKApiManager): VKGroup? {
        //Log.v(TAG, "onExecute: groupId=$groupId")
        var call = VKMethodCall.Builder()
            .method("groups.getById")
            .args("group_id", groupId)
            .args("fields", "$DESCRIPTION_FIELD_KEY,$MEMBERS_COUNT_FIELD_KEY")
            .version(manager.config.version)
            .build()
        val group = manager.execute(call, VKApiResponseParser { r ->
            try {
                val groups = JSONObject(r).getJSONArray("response")
                if (groups.length() == 0) null else VKGroup.parse(groups.getJSONObject(0))
            } catch (e: JSONException) {
                throw VKApiIllegalResponseException(e)
            }
        }) ?: return null

        call = VKMethodCall.Builder()
            .method("groups.getMembers")
            .args("group_id", groupId)
            .args("filter", "friends")
            .version(manager.config.version)
            .build()
        group.friendsCount = manager.execute(call, VKApiResponseParser { r ->
            try {
                JSONObject(r).getJSONObject("response").getInt("count")
            } catch (e: JSONException) {
                throw VKApiIllegalResponseException(e)
            }
        })

        call = VKMethodCall.Builder()
            .method("wall.get")
            .args("owner_id", -groupId)
            .args("count", 2)
            .version(manager.config.version)
            .build()
        group.lastPostDate = manager.execute(call, VKApiResponseParser { r ->
            try {
                val posts = JSONObject(r).getJSONObject("response").getJSONArray("items")
                var lastPostDate = NO_POSTS_DATE
                for (i in 0 until posts.length()) {
                    val date = posts.getJSONObject(i).getLong("date")
                    if (date > lastPostDate) lastPostDate = date
                }
                if (lastPostDate < 0) lastPostDate else lastPostDate * 1000
            } catch (e: JSONException) {
                throw VKApiIllegalResponseException(e)
            }
        })

        return group
    }

}
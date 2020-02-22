package com.kuelye.vkcup20ii.core.data

import android.util.SparseArray
import com.kuelye.vkcup20ii.core.api.VKGroupCommand
import com.kuelye.vkcup20ii.core.api.VKGroupsRequest
import com.kuelye.vkcup20ii.core.model.VKGroup
import com.kuelye.vkcup20ii.core.model.VKGroup.Companion.DESCRIPTION_FIELD_KEY
import com.kuelye.vkcup20ii.core.model.VKGroup.Companion.MEMBERS_COUNT_FIELD_KEY
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKApiCallback
import com.vk.api.sdk.exceptions.VKApiExecutionException

object GroupRepository {

    private val TAG = GroupRepository::class.java.simpleName

    private var groupsCache: SparseArray<VKGroup>? = null

    fun getGroups(callback: VKApiCallback<List<VKGroup>>) {
        if (groupsCache != null) {
            callback.success(groupsCache!!.toList())
        }
        val additionalFields = arrayOf(DESCRIPTION_FIELD_KEY, MEMBERS_COUNT_FIELD_KEY)
        VK.execute(VKGroupsRequest(additionalFields), object : VKApiCallback<List<VKGroup>> {
            override fun success(result: List<VKGroup>) {
                ensureCache()
                groupsCache!!.clear()
                for (group in result) {
                    groupsCache!!.put(group.id, group)
                }
                callback.success(result)
            }

            override fun fail(error: VKApiExecutionException) {
                callback.fail(error)
            }
        })
    }

    fun getGroup(groupId: Int, callback: VKApiCallback<VKGroup?>) {
        val group = groupsCache?.get(groupId)
        if (group != null) {
            callback.success(group)
        }
        VK.execute(VKGroupCommand(groupId), object : VKApiCallback<VKGroup?> {
            override fun success(result: VKGroup?) {
                ensureCache()
                if (result == null) {
                    groupsCache!!.remove(groupId)
                } else {
                    groupsCache!!.put(result.id, result)
                }
                callback.success(result)
            }

            override fun fail(error: VKApiExecutionException) {
                callback.fail(error)
            }
        })
    }

    private fun ensureCache() {
        if (groupsCache == null) {
            groupsCache = SparseArray()
        }
    }

    private fun SparseArray<VKGroup>.toList(): List<VKGroup> {
        val l = mutableListOf<VKGroup>()
        for (i in 0 until size()) {
            l.add(get(keyAt(i)))
        }
        return l
    }

}
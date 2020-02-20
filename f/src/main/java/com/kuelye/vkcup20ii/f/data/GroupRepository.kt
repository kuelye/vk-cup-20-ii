package com.kuelye.vkcup20ii.f.data

import android.util.Log
import android.util.LongSparseArray
import com.kuelye.vkcup20ii.f.api.VKGroupsRequest
import com.kuelye.vkcup20ii.f.model.VKGroup
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKApiCallback
import com.vk.api.sdk.exceptions.VKApiExecutionException

object GroupRepository {

    private val TAG = GroupRepository::class.java.simpleName

    private var groupsCache: LongSparseArray<VKGroup>? = null

    fun getGroups(callback: VKApiCallback<List<VKGroup>>) {
        if (groupsCache != null) {
            callback.success(groupsCache!!.toList())
        }
        VK.execute(VKGroupsRequest(), object : VKApiCallback<List<VKGroup>> {
            override fun success(result: List<VKGroup>) {
                ensureCache()
                groupsCache?.clear()
                for (group in result) {
                    groupsCache?.put(group.id, group)
                }
                callback.success(result)
            }

            override fun fail(error: VKApiExecutionException) {
                callback.fail(error)
            }
        })
    }

    fun getGroup(groupId: Long, callback: VKApiCallback<VKGroup>) {
        Log.v(TAG, "getGroup: groupId=$groupId")
        if (groupsCache != null) {
            val group = groupsCache!!.get(groupId)
            Log.v(TAG, "getGroup: group=$group")
            if (group != null) {
                callback.success(group)
            }
        }
    }

    private fun ensureCache() {
        if (groupsCache == null) {
            groupsCache = LongSparseArray()
        }
    }

    private fun LongSparseArray<VKGroup>.toList(): List<VKGroup> {
        val l = mutableListOf<VKGroup>()
        for (i in 0 until size()) {
            l.add(get(keyAt(i)))
        }
        return l
    }

}
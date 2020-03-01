package com.kuelye.vkcup20ii.core.data

import android.util.Log
import com.kuelye.vkcup20ii.core.api.groups.VKGroupCommand
import com.kuelye.vkcup20ii.core.api.groups.VKGroupsGetCommand
import com.kuelye.vkcup20ii.core.data.BaseRepository.Source.CACHE
import com.kuelye.vkcup20ii.core.data.BaseRepository.Source.FRESH
import com.kuelye.vkcup20ii.core.model.groups.VKGroup
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKApiCallback

object GroupRepository : BaseRepository() {

    private val TAG = GroupRepository::class.java.simpleName

    val groupCache: Cache<VKGroup> by lazy {
        Cache<VKGroup>({ group, type -> group.type.hashCode() == type }) }
    private val groupsRequestManager: RequestManager by lazy { RequestManager() }

    fun requestGroups(arguments: RequestGroupsArguments, source: Source) {
        //Log.v(TAG, "requestGroups: arguments=$arguments, source=$source")
        if (source != CACHE && !groupsRequestManager.request(arguments)) return
        if (source != FRESH) if (groupCache.emit(arguments.filter, true)) if (source == CACHE) return
        if (!VK.isLoggedIn()) return
        val request = VKGroupsGetCommand(arguments.offset, arguments.count,
            arguments.extendedFields, arguments.type?.filter)
        VK.execute(request, object : VKApiCallback<VKGroupsGetCommand.Response> {
            override fun success(result: VKGroupsGetCommand.Response) {
                //Log.v(TAG, "requestGroups>success: result=$result")
                groupsRequestManager.finish(arguments)
                groupCache.set(result.items, result.count, arguments.filter, arguments.offset == 0)
            }

            override fun fail(error: Exception) {
                groupsRequestManager.finish(arguments)
                groupCache.fail(error)
            }
        })
    }

    fun requestGroup(groupId: Int, callback: VKApiCallback<VKGroup?>) {
        val group = groupCache.items?.get(groupId)
        if (group != null) {
            callback.success(group)
        }
        if (!VK.isLoggedIn()) return
        VK.execute(VKGroupCommand(groupId), object : VKApiCallback<VKGroup?> {
            override fun success(result: VKGroup?) {
                if (result != null) groupCache.update(result, null)
                callback.success(result)
            }

            override fun fail(error: Exception) {
                callback.fail(error)
            }
        })
    }

    data class RequestGroupsArguments(
        val offset: Int,
        val count: Int,
        val extendedFields: List<VKGroup.Field>,
        val type: VKGroup.Type? = null
    ) : RequestManager.Arguments {
        val filter = type?.value?.hashCode()
    }

}
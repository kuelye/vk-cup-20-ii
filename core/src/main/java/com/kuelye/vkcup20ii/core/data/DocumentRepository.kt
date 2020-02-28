package com.kuelye.vkcup20ii.core.data

import android.util.Log
import android.util.SparseArray
import com.kuelye.vkcup20ii.core.api.docs.VKDocsDeleteRequest
import com.kuelye.vkcup20ii.core.api.docs.VKDocsEditRequest
import com.kuelye.vkcup20ii.core.api.docs.VKDocsGetRequest
import com.kuelye.vkcup20ii.core.model.docs.VKDocument
import com.kuelye.vkcup20ii.core.utils.toMutableList
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKApiCallback
import java.util.*

object DocumentRepository : BaseRepository() {

    private val TAG = DocumentRepository::class.java.simpleName

    private var documents: SparseArray<VKDocument>? = null
    private var totalCount: Int? = null
    private var sortedDocuments: MutableList<VKDocument>? = null

    fun getDocuments(
        offset: Int, count: Int, onlyCache: Boolean,
        callback: VKApiCallback<GetItemsResult<VKDocument>>
    ) {
        Log.v(TAG, "getDocuments: $offset, $count")
        if (sortedDocuments != null) {
            callback.success(GetItemsResult(sortedDocuments!!, totalCount))
            if (onlyCache) return
        }
        if (!VK.isLoggedIn()) return
        val request = VKDocsGetRequest(offset, count)
        VK.execute(request, object : VKApiCallback<VKDocsGetRequest.Response> {
            override fun success(result: VKDocsGetRequest.Response) {
                ensureCache()
                if (offset == 0) documents!!.clear()
                for (document in result.items) documents!!.put(document.id, document)
                totalCount = result.count
                sortedDocuments = documents!!.toMutableList()
                Collections.sort(sortedDocuments!!, VKDocument.DefaultComparator())
                callback.success(GetItemsResult(sortedDocuments!!, result.count))
            }

            override fun fail(error: Exception) {
                callback.fail(error)
            }
        })
    }

    fun removeDocument(
        document: VKDocument,
        callback: VKApiCallback<Int>
    ) {
        if (!VK.isLoggedIn()) return
        val request = VKDocsDeleteRequest(document.id, document.ownerId)
        VK.execute(request, object : VKApiCallback<Int> {
            override fun success(result: Int) {
                ensureCache()
                if (result == 1) {
                    documents!!.remove(document.id)
                    if (totalCount != null) totalCount = totalCount!! - 1
                    if (sortedDocuments != null) sortedDocuments!!.remove(document)
                }
                callback.success(result)
            }

            override fun fail(error: Exception) {
                callback.fail(error)
            }
        })
    }

    fun renameDocument(
        document: VKDocument,
        title: String,
        callback: VKApiCallback<Int>
    ) {
        if (!VK.isLoggedIn()) return
        val request = VKDocsEditRequest(document.id, document.ownerId, title)
        VK.execute(request, object : VKApiCallback<Int> {
            override fun success(result: Int) {
                ensureCache()
                if (result == 1) {
                    @Suppress("NAME_SHADOWING") val document = documents!!.get(document.id)
                    if (document != null) document.title = title
                }
                callback.success(result)
            }

            override fun fail(error: Exception) {
                callback.fail(error)
            }
        })
    }

    private fun ensureCache() {
        if (documents == null) {
            documents = SparseArray()
        }
    }

}
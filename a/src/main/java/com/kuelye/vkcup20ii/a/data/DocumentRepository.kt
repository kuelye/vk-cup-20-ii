package com.kuelye.vkcup20ii.a.data

import android.util.SparseArray
import com.kuelye.vkcup20ii.a.api.VKDocsDeleteRequest
import com.kuelye.vkcup20ii.a.api.VKDocsGetRequest
import com.kuelye.vkcup20ii.a.model.VKDocument
import com.kuelye.vkcup20ii.core.utils.toMutableList
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKApiCallback
import java.util.*

object DocumentRepository {

    private val TAG = DocumentRepository::class.java.simpleName
    const val COUNT_PER_PAGE = 10

    private var documents: SparseArray<VKDocument>? = null
    private var sortedDocuments: MutableList<VKDocument>? = null

    fun getDocuments(
        pagesCount: Int,
        callback: VKApiCallback<GetDocumentsResult>
    ) {
        if (sortedDocuments != null) callback.success(GetDocumentsResult(sortedDocuments!!))
        if (!VK.isLoggedIn()) return
        VK.execute(VKDocsGetRequest(0, pagesCount * COUNT_PER_PAGE),
            object : VKApiCallback<VKDocsGetRequest.Response> {
                override fun success(result: VKDocsGetRequest.Response) {
                    ensureCache()
                    documents!!.clear()
                    for (document in result.items) documents!!.put(document.id, document)
                    sortedDocuments = documents!!.toMutableList()
                    Collections.sort(sortedDocuments!!, VKDocument.DateComparator())
                    callback.success(GetDocumentsResult(sortedDocuments!!, result.count))
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
        VK.execute(VKDocsDeleteRequest(document.id, document.ownerId), object : VKApiCallback<Int> {
            override fun success(result: Int) {
                ensureCache()
                if (result == 1) {
                    documents!!.remove(document.id)
                    if (sortedDocuments != null) sortedDocuments!!.remove(document)
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

    data class GetDocumentsResult(
        val documents: List<VKDocument>,
        val totalCount: Int? = null
    )

}
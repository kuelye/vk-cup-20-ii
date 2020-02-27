package com.kuelye.vkcup20ii.a.data

import android.util.SparseArray
import com.kuelye.vkcup20ii.a.api.VKDocsGetRequest
import com.kuelye.vkcup20ii.a.model.VKDocsGetResponse
import com.kuelye.vkcup20ii.a.model.VKDocument
import com.kuelye.vkcup20ii.core.utils.toList
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKApiCallback

object DocumentRepository {

    private val TAG = DocumentRepository::class.java.simpleName

    private var documentCache: SparseArray<VKDocument>? = null

    fun getDocuments(
        callback: VKApiCallback<List<VKDocument>>
    ) {
        if (documentCache != null) callback.success(documentCache!!.toList())
        if (!VK.isLoggedIn()) return
        VK.execute(VKDocsGetRequest(0, 20), object : VKApiCallback<VKDocsGetResponse> {
            override fun success(result: VKDocsGetResponse) {
                ensureCache()
                documentCache!!.clear()
                for (document in result.items) {
                    documentCache!!.put(document.id, document)
                }
                callback.success(result.items)
            }

            override fun fail(error: Exception) {
                callback.fail(error)
            }
        })
    }

    private fun ensureCache() {
        if (documentCache == null) {
            documentCache = SparseArray()
        }
    }

}
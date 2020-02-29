package com.kuelye.vkcup20ii.core.data

import android.annotation.SuppressLint
import android.util.Log
import android.util.SparseArray
import androidx.core.util.forEach
import com.kuelye.vkcup20ii.core.model.Identifiable
import com.kuelye.vkcup20ii.core.utils.toMutableList
import java.util.*
import kotlin.Comparator
import kotlin.collections.HashMap

open class BaseRepository {

    @SuppressLint("UseSparseArrays")
    class Cache<I : Identifiable>(
        val filterBlock: ((I, Int?) -> Boolean)? = null,
        val defaultComparator: Comparator<I>? = null
    ) {

        companion object {
            private val TAG = Cache::class.java.simpleName
        }

        var items: SparseArray<I>? = null
        var sortedItems: MutableList<I>? = null
        val totalCounts: MutableMap<Int?, Int?> by lazy { HashMap<Int?, Int?>() }

        fun set(
            items: List<I>?, totalCount: Int?,
            filter: Int? = null, clear: Boolean
        ) {
            ensure()
            //Log.v(TAG, "set: items.size=${items?.size}, totalCount=$totalCount, filter=$filter, clear=$clear")
            if (clear) clear(filter)
            totalCounts[filter] = totalCount
            if (items != null) {
                for (document in items) this.items!!.put(document.id, document)
                sortedItems = this.items!!.toMutableList()
                if (defaultComparator != null) Collections.sort(sortedItems!!, defaultComparator)
            } else {
                sortedItems = null
            }
            //Log.v(TAG, "set: totalCounts=$totalCounts, this.items.size=${this.items?.size()}")
        }

        private fun clear(filter: Int? = null) {
            if (filterBlock == null || filter == null) {
                items?.clear()
            } else {
                val removeItems = mutableListOf<I>()
                items!!.forEach { _, item ->
                    if (filterBlock.invoke(item, filter)) removeItems.add(item)
                }
                for (item in removeItems) {
                    items!!.remove(item.id)
                    sortedItems?.remove(item)
                }
                //Log.v(TAG, "clear: filter=$filter items.size=${items?.size()}")
            }
        }

        private fun ensure() {
            if (items == null) items = SparseArray()
        }

    }

    data class GetItemsResult<I : Identifiable>(
        val items: List<I>?,
        val totalCount: Int? = null
    ) {

        companion object {
            fun <I : Identifiable> from(
                cache: Cache<I>, filter: Int? = null
            ) : GetItemsResult<I> {
                val items = if (cache.filterBlock == null) cache.sortedItems else
                    cache.sortedItems?.filter { cache.filterBlock.invoke(it, filter) }
                return GetItemsResult(items, cache.totalCounts[filter])
            }
        }

    }

}
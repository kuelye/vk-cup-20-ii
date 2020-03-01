package com.kuelye.vkcup20ii.core.data

import android.annotation.SuppressLint
import android.util.Log
import android.util.SparseArray
import androidx.core.util.contains
import androidx.core.util.forEach
import com.kuelye.vkcup20ii.core.model.Identifiable
import com.kuelye.vkcup20ii.core.utils.toMutableList
import java.lang.Exception
import java.util.*
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

        val listeners: MutableSet<Listener<I>> by lazy { mutableSetOf<Listener<I>>() }

        fun set(
            items: List<I>?, totalCount: Int?,
            filter: Int? = null, clear: Boolean
        ) {
            Log.v(TAG, "set: items.size=${items?.size}, totalCount=$totalCount, filter=$filter, clear=$clear")
            ensure()
            if (clear) clear(filter)
            totalCounts[filter] = totalCount
            if (items != null) {
                for (document in items) this.items!!.put(document.id, document)
                sort()
            } else {
                sortedItems = null
            }
            emit(filter)
        }

        fun remove(item: I, filter: Int? = null) {
            Log.v(TAG, "remove: item=$item, filter=$filter")
            if (items != null) {
                items!!.remove(item.id)
                sortedItems?.remove(item)
                var totalCount = totalCounts[null]
                if (totalCount != null) totalCounts[null] = totalCount - 1
                if (filter != null) {
                    totalCount = totalCounts[filter]
                    if (totalCount != null) totalCounts[filter] = totalCount - 1
                }
            }
        }

        fun update(item: I, filter: Int? = null) {
            Log.v(TAG, "update: item=$item, filter=$filter")
            if (items != null) {
                val update = items!!.contains(item.id)
                items!!.put(item.id, item)
                sort()
                if (!update) {
                    var totalCount = totalCounts[null]
                    if (totalCount != null) totalCounts[null] = totalCount + 1
                    if (filter != null) {
                        totalCount = totalCounts[filter]
                        if (totalCount != null) totalCounts[filter] = totalCount + 1
                    }
                }
            }
        }

        fun emit(
            filter: Int? = null,
            fromCache: Boolean = false
        ): Boolean {
            Log.v(TAG, "emit: filter=$filter, fromCache=$fromCache")
            if (sortedItems == null || !totalCounts.containsKey(filter)) return false
            listeners.forEach { listener ->
                if (listener.getFilter() == null || listener.getFilter() == filter)
                    listener.onNextItems(ItemsResult.from(this, listener.getFilter(), fromCache))
            }
            return true
        }

        fun fail(
            error: Exception
        ) {
            Log.v(TAG, "fail: error=$error")
            listeners.forEach { it.onFail(error) }
        }

        private fun clear(filter: Int? = null) {
            Log.v(TAG, "clear: filter=$filter")
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
            }
        }

        private fun sort() {
            if (this.items != null) {
                sortedItems = this.items!!.toMutableList()
                if (defaultComparator != null) Collections.sort(sortedItems!!, defaultComparator)
            }
        }

        private fun ensure() {
            if (items == null) items = SparseArray()
        }

    }

    class RequestManager {

        private val activeArguments: MutableSet<Arguments> by lazy { mutableSetOf<Arguments>() }

        fun request(arguments: Arguments): Boolean {
            return if (activeArguments.contains(arguments)) {
                false
            } else {
                activeArguments.add(arguments)
                true
            }
        }

        fun finish(arguments: Arguments) {
            activeArguments.remove(arguments)
        }

        interface Arguments

    }

    class ItemsResult<I : Identifiable>(
        val items: List<I>?,
        val totalCount: Int? = null,
        val fromCache: Boolean = false
    ) {

        companion object {
            fun <I : Identifiable> from(
                cache: Cache<I>,
                filter: Int? = null,
                fromCache: Boolean = false
            ) : ItemsResult<I> {
                val items = if (cache.filterBlock == null || filter == null) cache.sortedItems else
                    cache.sortedItems?.filter { cache.filterBlock.invoke(it, filter) }
                return ItemsResult(items, cache.totalCounts[filter], fromCache)
            }
        }

        override fun toString(): String {
            return "ItemsResult(items.size=${items?.size}, totalCount=$totalCount, fromCache=$fromCache)"
        }

    }

    interface Listener<I : Identifiable> {
        fun onNextItems(result: ItemsResult<I>)
        fun onFail(error: Exception)
        fun getFilter(): Int?
    }

    enum class Source {
        ANY, CACHE, FRESH
    }

}
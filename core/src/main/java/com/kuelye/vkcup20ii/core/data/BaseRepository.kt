package com.kuelye.vkcup20ii.core.data

open class BaseRepository {

    data class GetItemsResult<I>(
        val items: List<I>,
        val totalCount: Int? = null
    )

}
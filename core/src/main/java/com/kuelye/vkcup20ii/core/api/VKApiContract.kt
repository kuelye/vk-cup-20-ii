package com.kuelye.vkcup20ii.core.api

// Common
const val RESPONSE_FIELD_KEY = "response"
const val COUNT_FIELD_KEY = "count"
const val ITEMS_FIELD_KEY = "items"
const val ID_FIELD_KEY = "id"
const val OWNER_ID_FIELD_KEY = "owner_id"
const val DATE_FIELD_KEY = "date"
const val TITLE_FIELD_KEY = "title"
const val TYPE_FIELD_KEY = "type"
const val ERROR_FIELD_KEY = "error"

// VKGroup
const val NAME_FIELD_KEY = "name"
const val SCREEN_NAME_FIELD_KEY = "screen_name"
const val IS_MEMBER_FIELD_KEY = "is_member"
const val PHOTO_200_FIELD_KEY = "photo_200"
const val DESCRIPTION_FIELD_KEY = "description"
const val MEMBERS_COUNT_FIELD_KEY = "members_count"
const val ADDRESSES_FIELD_KEY = "addresses"

const val GROUP_FILTER_PAGE = "publics"
const val GROUP_FILTER_GROUP = "groups"
const val GROUP_FILTER_EVENT = "events"

const val GROUP_TYPE_PAGE = "page"
const val GROUP_TYPE_GROUP = "group"
const val GROUP_TYPE_EVENT = "event"

// VKAddress
const val LATITUDE_FIELD_KEY = "latitude"
const val LONGITUDE_FIELD_KEY = "longitude"
const val ADDRESS_FIELD_KEY = "address"
const val CITY_ID_FIELD_KEY = "city_id"

class VKPhotoColumns {
    companion object {
        const val LAT_FIELD_KEY = "lat"
        const val LONG_FIELD_KEY = "long"
        const val SIZES_FIELD_KEY = "sizes"
        const val SIZE_TYPE_FIELD_KEY = "type"
        const val SIZE_URL_FIELD_KEY = "url"
        const val SIZE_WIDTH_FIELD_KEY = "width"
        const val SIZE_HEIGHT_FIELD_KEY = "height"
    }
}

class VKDocumentColumns {
    companion object {
        const val SIZE_FIELD_KEY = "size"
        const val EXT_FIELD_KEY = "ext"
        const val TAGS_FIELD_KEY = "tags"
    }
}

class VKErrorColumns {
    companion object {
        const val CODE_FIELD_KEY = "error_code"
        const val MESSAGE_FIELD_KEY = "error_msg"
    }
}
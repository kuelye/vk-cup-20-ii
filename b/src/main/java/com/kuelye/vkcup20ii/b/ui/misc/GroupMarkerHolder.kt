package com.kuelye.vkcup20ii.b.ui.misc

import com.google.android.gms.maps.model.LatLng
import com.kuelye.vkcup20ii.core.model.VKAddress
import com.kuelye.vkcup20ii.core.model.VKGroup
import com.squareup.picasso.Picasso
import com.vk.api.sdk.utils.VKUtils.dp

@Suppress("UNCHECKED_CAST")
class GroupMarkerHolder(
    var group: VKGroup,
    var address: VKAddress,
    clusterRenderer: MarkerRenderer<GroupMarkerHolder>
) : BaseMarkerHolder(clusterRenderer as MarkerRenderer<in BaseMarkerHolder>) {

    companion object {
        private val ICON_SIZE_DEFAULT = dp(22)
        private val ICON_SIZE_SELECTED = dp(32)
    }

    var selected: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                updateIcon()
            }
        }

    override fun getPosition(): LatLng = address.position

    override fun updateIcon() {
        val size = if (selected) ICON_SIZE_SELECTED else ICON_SIZE_DEFAULT
        Picasso.get().load(group.photo200)
            .resize(size, size)
            //            .placeholder(ColorDrawable(PLACEHOLDER_COLOR)) // TODO
            //            .error(ColorDrawable(PLACEHOLDER_COLOR))
            .transform(BorderTransformation(selected))
            .into(this)
    }

    fun setGroupAddress(group: VKGroup, address: VKAddress) {
        this.group = group
        this.address = address
        updateIcon()
    }

}
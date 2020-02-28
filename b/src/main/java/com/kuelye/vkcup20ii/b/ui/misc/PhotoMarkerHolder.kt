package com.kuelye.vkcup20ii.b.ui.misc

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.kuelye.vkcup20ii.b.ui.misc.BaseMarkerHolder.IconTransformation.BorderType.SQUARE
import com.kuelye.vkcup20ii.core.model.photos.VKPhoto
import com.vk.api.sdk.utils.VKUtils.dp

@Suppress("UNCHECKED_CAST")
class PhotoMarkerHolder(
    photo: VKPhoto,
    clusterRenderer: MarkerRenderer<PhotoMarkerHolder>
) : BaseMarkerHolder(clusterRenderer as MarkerRenderer<in BaseMarkerHolder>) {

    companion object {
        private val TAG = PhotoMarkerHolder::class.java.simpleName
        private val CENTER_ICON_SIZE_DEFAULT = dp(48)
    }

    var photo: VKPhoto = photo
        set(value) {
            field = value
            onClusterItemRendered()
        }

    override fun getPosition(): LatLng = photo.position!!

    override fun onClusterItemRendered() {
        updateIcon(photo.iconPhoto, CENTER_ICON_SIZE_DEFAULT,
            IconTransformation(false, SQUARE))
    }

    override fun onClusterRendered(clusterSize: Int) {
        Log.v(TAG, "GUB onClusterRendered: $clusterSize, ${photo.iconPhoto}")
        updateIcon(photo.iconPhoto, CENTER_ICON_SIZE_DEFAULT,
            IconTransformation(false, SQUARE, clusterSize), true)
    }

}
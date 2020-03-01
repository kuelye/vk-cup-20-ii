package com.kuelye.vkcup20ii.b.ui.misc

import android.graphics.Bitmap
import android.util.Log
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.kuelye.vkcup20ii.b.ui.misc.BaseMarkerHolder.IconTransformation.BorderType.SQUARE
import com.kuelye.vkcup20ii.core.model.photos.VKPhoto
import com.squareup.picasso.Picasso
import com.vk.api.sdk.utils.VKUtils.dp

@Suppress("UNCHECKED_CAST")
class PhotoMarkerHolder(
    photo: VKPhoto,
    clusterRenderer: MarkerRenderer<PhotoMarkerHolder>
) : BaseMarkerHolder(clusterRenderer as MarkerRenderer<in BaseMarkerHolder>) {

    companion object {
        private val TAG = PhotoMarkerHolder::class.java.simpleName
        private val CENTER_ICON_SIZE_DEFAULT = dp(48)

        private var clusterItemStubBitmap: Bitmap? = null
    }

    var photo: VKPhoto = photo
        set(value) {
            field = value
            onClusterItemRendered()
        }

    override val id: Int
        get() = photo.id

    override fun getPosition(): LatLng = photo.position!!

    override fun onBeforeClusterItemRendered(markerOptions: MarkerOptions) {
        super.onBeforeClusterItemRendered(markerOptions)
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(getClusterItemStubBitmap()))
    }

    override fun onClusterItemRendered() {
        super.onClusterItemRendered()
        updateIcon(
            Picasso.get().load(photo.iconPhoto), CENTER_ICON_SIZE_DEFAULT,
            IconTransformation(false, SQUARE))
    }

    override fun onBeforeClusterRendered(clusterSize: Int, markerOptions: MarkerOptions) {
        super.onBeforeClusterRendered(clusterSize, markerOptions)
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(getClusterStubBitmap(clusterSize)))
    }

    override fun onClusterRendered(clusterSize: Int) {
        super.onClusterRendered(clusterSize)
        updateIcon(
            Picasso.get().load(photo.iconPhoto), CENTER_ICON_SIZE_DEFAULT,
            IconTransformation(false, SQUARE, clusterSize), true)
    }

    private fun getClusterItemStubBitmap() : Bitmap? {
        if (clusterItemStubBitmap == null) {
            clusterItemStubBitmap = getClusterStubBitmap(null)
        }
        return clusterItemStubBitmap
    }

    private fun getClusterStubBitmap(clusterSize: Int?): Bitmap? {
        return IconTransformation(false, SQUARE, clusterSize,
            CENTER_ICON_SIZE_DEFAULT).transform(null)
    }


//    override fun onBeforeClusterItemRendered(markerOptions: MarkerOptions) {
//        cluster
//        return IconTransformation(false, SQUARE, null, CENTER_ICON_SIZE_DEFAULT).transform(null)
//    }
//
//    override fun onClusterItemRendered() {
//        updateIcon(photo.iconPhoto, CENTER_ICON_SIZE_DEFAULT,
//            IconTransformation(false, SQUARE))
//    }
//
//    override fun getClusterBitmap(clusterSize: Int): Bitmap? {
//        return IconTransformation(false, SQUARE, null, CENTER_ICON_SIZE_DEFAULT).transform(null)
//    }
//
//    override fun onClusterRendered(clusterSize: Int) {
//        Log.v(TAG, "GUB onClusterRendered: $clusterSize, ${photo.iconPhoto}")
//        updateIcon(photo.iconPhoto, CENTER_ICON_SIZE_DEFAULT,
//            IconTransformation(false, SQUARE, clusterSize), true)
//    }

}
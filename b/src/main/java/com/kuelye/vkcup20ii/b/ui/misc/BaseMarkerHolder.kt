package com.kuelye.vkcup20ii.b.ui.misc

import android.graphics.*
import android.graphics.BlurMaskFilter.Blur.OUTER
import android.graphics.Color.WHITE
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.Paint.Style.FILL
import android.graphics.Paint.Style.STROKE
import android.graphics.Shader.TileMode.CLAMP
import android.graphics.drawable.Drawable
import androidx.core.graphics.scaleMatrix
import androidx.core.graphics.translationMatrix
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.clustering.ClusterItem
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import com.squareup.picasso.Transformation
import com.vk.api.sdk.utils.VKUtils.dp

abstract class BaseMarkerHolder(
    private val clusterRenderer: MarkerRenderer<in BaseMarkerHolder>
) : ClusterItem, Target {

    companion object {
        private val TAG = Marker::class.java.simpleName
    }

    override fun getTitle(): String? = null

    override fun getSnippet(): String? = null

    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}

    override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
        // TODO
    }

    override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
        val icon = BitmapDescriptorFactory.fromBitmap(bitmap)
        clusterRenderer.getMarker(this)?.setIcon(icon)
    }

    abstract fun updateIcon()

    open fun getClusterBitmap(clusterSize: Int): Bitmap? = null

}
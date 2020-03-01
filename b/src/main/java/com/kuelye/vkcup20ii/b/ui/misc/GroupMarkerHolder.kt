package com.kuelye.vkcup20ii.b.ui.misc

import android.graphics.*
import android.graphics.Bitmap.Config.ARGB_8888
import android.graphics.BlurMaskFilter.Blur.OUTER
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.Paint.Style.FILL
import android.graphics.Paint.Style.STROKE
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.kuelye.vkcup20ii.b.ui.misc.BaseMarkerHolder.IconTransformation.BorderType.CIRCLE
import com.kuelye.vkcup20ii.core.model.groups.VKAddress
import com.kuelye.vkcup20ii.core.model.groups.VKGroup
import com.squareup.picasso.Picasso
import com.vk.api.sdk.utils.VKUtils.dp
import kotlin.math.max

@Suppress("UNCHECKED_CAST")
class GroupMarkerHolder(
    var group: VKGroup,
    var address: VKAddress,
    clusterRenderer: MarkerRenderer<GroupMarkerHolder>
) : BaseMarkerHolder(clusterRenderer as MarkerRenderer<in BaseMarkerHolder>) {

    companion object {
        private val CENTER_ICON_SIZE_DEFAULT = dp(22)
        private val CENTER_ICON_SIZE_SELECTED = dp(32)

        private var clusterItemStubBitmap: Bitmap? = null
    }

    override val id: Int
        get() = address.id

    var selected: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                onClusterItemRendered()
            }
        }

    override fun getPosition(): LatLng = address.position

    override fun onBeforeClusterItemRendered(markerOptions: MarkerOptions) {
        super.onBeforeClusterItemRendered(markerOptions)
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(getClusterItemStubBitmap()))
    }

    override fun onClusterItemRendered() {
        super.onClusterItemRendered()
        updateIcon(
            Picasso.get().load(group.photo200),
            if (selected) CENTER_ICON_SIZE_SELECTED else CENTER_ICON_SIZE_DEFAULT,
            IconTransformation(selected, CIRCLE))
    }

    override fun onBeforeClusterRendered(clusterSize: Int, markerOptions: MarkerOptions) {
        super.onBeforeClusterRendered(clusterSize, markerOptions)
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(getClusterBitmap(clusterSize)))
    }

    private fun getClusterItemStubBitmap() : Bitmap? {
        if (clusterItemStubBitmap == null) {
            clusterItemStubBitmap = IconTransformation(false, CIRCLE, null,
                CENTER_ICON_SIZE_DEFAULT).transform(null)
        }
        return clusterItemStubBitmap
    }

    private fun getClusterBitmap(clusterSize: Int): Bitmap? {
        // calculate sizes
        val fillPaint = Paint().apply {
            style = FILL
            color = CLUSTER_PLACEHOLDER_COLOR
            flags = ANTI_ALIAS_FLAG
            textSize = CLUSTER_ICON_TEXT_SIZE
            isAntiAlias = true
            color = CLUSTER_ICON_TEXT_COLOR
        }
        val text = clusterSize.toString()
        val bounds = Rect()
        fillPaint.getTextBounds(text, 0, text.length, bounds)
        val textWidth = fillPaint.measureText(text)
        val centerIconSize = max(CENTER_ICON_SIZE_DEFAULT,
            textWidth.toInt() + 2 * CLUSTER_ICON_TEXT_PADDING)
        val shadowSize = ICON_SHADOW_SIZE_DEFAULT
        val size = centerIconSize + ICON_BORDER_WIDTH * 2 + shadowSize * 2

        // create canvas
        val result = Bitmap.createBitmap(size, size, ARGB_8888)
        val canvas = Canvas(result)

        // draw shadow
        val shadowPaint = Paint().apply {
            style = FILL
            color = ICON_SHADOW_COLOR
            flags = ANTI_ALIAS_FLAG
            maskFilter = BlurMaskFilter(shadowSize.toFloat(), OUTER)
        }
        val center = size.toFloat() / 2
        val centerRadius = centerIconSize.toFloat() / 2
        val borderRadius = centerRadius + ICON_BORDER_WIDTH / 2
        canvas.drawCircle(center, center, borderRadius, shadowPaint)

        // draw border
        val borderPaint = Paint().apply {
            style = STROKE
            strokeWidth = ICON_BORDER_WIDTH.toFloat()
            color = ICON_BORDER_COLOR
            isAntiAlias = true
        }
        canvas.drawCircle(center, center, borderRadius, borderPaint)

        // draw center bg
        fillPaint.color = CLUSTER_PLACEHOLDER_COLOR
        canvas.drawCircle(center, center, centerRadius, fillPaint)

        // draw cluster size
        fillPaint.color = CLUSTER_ICON_TEXT_COLOR
        canvas.drawText(clusterSize.toString(), center - textWidth / 2,
            center + bounds.height() / 2, fillPaint)

        return result
    }

    fun setGroupAddress(group: VKGroup, address: VKAddress) {
        this.group = group
        this.address = address
        onClusterItemRendered()
    }

}
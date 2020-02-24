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
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.clustering.ClusterItem
import com.kuelye.vkcup20ii.core.model.VKAddress
import com.kuelye.vkcup20ii.core.model.VKGroup
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import com.squareup.picasso.Transformation
import com.vk.api.sdk.utils.VKUtils.dp


class MarkerHolder(
    var group: VKGroup,
    var address: VKAddress,
    private val clusterRenderer: GroupMarkerRenderer
) : ClusterItem, Target {

    companion object {
        private val TAG = Marker::class.java.simpleName
        private val ICON_SIZE_DEFAULT = dp(22)
        private val ICON_SIZE_SELECTED = dp(32)
        private val ICON_BORDER_WIDTH = dp(3)
        private const val ICON_BORDER_COLOR = WHITE
        private val ICON_SHADOW_SIZE_DEFAULT = dp(4)
        private val ICON_SHADOW_SIZE_SELECTED = dp(8)
        private const val ICON_SHADOW_COLOR = 0x40000000
    }

    var selected: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                updateIcon()
            }
        }

    init {
        updateIcon()
    }

    override fun getTitle(): String? = null

    override fun getPosition(): LatLng = address.position

    override fun getSnippet(): String? = null

    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}

    override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
        // TODO
    }

    override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
        clusterRenderer.getMarker(this)
            ?.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap))
    }

    fun setGroupAddress(group: VKGroup, address: VKAddress) {
        this.group = group
        this.address = address
        updateIcon()
    }

    fun updateIcon() {
        val size = if (selected) ICON_SIZE_SELECTED else ICON_SIZE_DEFAULT
        Picasso.get().load(group.photo200)
            .resize(size, size)
            //            .placeholder(ColorDrawable(PLACEHOLDER_COLOR)) // TODO
            //            .error(ColorDrawable(PLACEHOLDER_COLOR))
            .transform(BorderTransformation(selected))
            .into(this)
    }

    class BorderTransformation(
        private val selected: Boolean
    ) : Transformation {
        override fun transform(source: Bitmap): Bitmap {
            val shadowSize = if (selected) ICON_SHADOW_SIZE_SELECTED else ICON_SHADOW_SIZE_DEFAULT
            val size = source.width + ICON_BORDER_WIDTH * 2 + shadowSize * 2
            val result = Bitmap.createBitmap(size, size, source.config)
            val canvas = Canvas(result)

            val shadowPaint = Paint().apply {
                style = FILL
                color = ICON_SHADOW_COLOR
                flags = ANTI_ALIAS_FLAG
                maskFilter = BlurMaskFilter(shadowSize.toFloat(), OUTER)
            }
            val center = size.toFloat() / 2
            val bitmapRadius = source.width.toFloat() / 2
            val borderRadius = bitmapRadius + ICON_BORDER_WIDTH / 2
            canvas.drawCircle(center, center, borderRadius, shadowPaint)

            val borderPaint = Paint().apply {
                style = STROKE
                strokeWidth = ICON_BORDER_WIDTH.toFloat()
                color = ICON_BORDER_COLOR
                isAntiAlias = true
            }
            canvas.drawCircle(center, center, borderRadius, borderPaint)

            val bitmapPaint = Paint().apply {
                isAntiAlias = true
                shader = BitmapShader(source, CLAMP, CLAMP)
                scaleMatrix()
                val d = (shadowSize + ICON_BORDER_WIDTH).toFloat()
                shader.setLocalMatrix(translationMatrix(d, d))
            }
            canvas.drawCircle(center, center, bitmapRadius, bitmapPaint)
            source.recycle()

            return result
        }

        override fun key() = "border-${if (selected) "1" else "0"}"
    }

}
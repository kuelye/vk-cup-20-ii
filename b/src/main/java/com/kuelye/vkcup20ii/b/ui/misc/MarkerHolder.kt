package com.kuelye.vkcup20ii.b.ui.misc

import android.graphics.*
import android.graphics.BlurMaskFilter.Blur.OUTER
import android.graphics.Color.BLACK
import android.graphics.Color.WHITE
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.Paint.Style.FILL
import android.graphics.Paint.Style.STROKE
import android.graphics.Shader.TileMode.CLAMP
import android.graphics.drawable.Drawable
import android.util.Log
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.kuelye.vkcup20ii.core.model.VKGroup
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import com.squareup.picasso.Transformation
import com.vk.api.sdk.utils.VKUtils.dp


class MarkerHolder(
    val marker: Marker,
    group: VKGroup
) : Target {

    companion object {
        private val TAG = MarkerHolder::class.java.simpleName
        private val ICON_SIZE_DEFAULT = dp(22)
        private val ICON_SIZE_SELECTED = dp(32)
        private val ICON_BORDER_WIDTH = dp(3)
        private val ICON_BORDER_COLOR = WHITE
        private val ICON_SHADOW_SIZE_DEFAULT = dp(4)
        private val ICON_SHADOW_SIZE_SELECTED = dp(8)
        private val ICON_SHADOW_COLOR = 0x40000000
    }

    private var group: VKGroup = group
        set(value) {
            if (field != value) {
                field = value
                updateIcon()
            }
        }

    init {
        updateIcon()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MarkerHolder

        if (marker != other.marker) return false

        return true
    }

    override fun hashCode(): Int {
        return marker.hashCode()
    }

    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
        // stub
    }

    override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
        // TODO
    }

    override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
        marker.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap))
    }

    private fun updateIcon() {
        Log.v(TAG, "updateIcon: ${group.photo200}")
        Picasso.get().load(group.photo200)
            .resize(ICON_SIZE_SELECTED, ICON_SIZE_SELECTED)
            //            .placeholder(ColorDrawable(PLACEHOLDER_COLOR)) // TODO
            //            .error(ColorDrawable(PLACEHOLDER_COLOR))
            .transform(BorderTransformation())
            .into(this)
    }

    class BorderTransformation : Transformation {
        override fun transform(source: Bitmap): Bitmap {
            val size = source.width + ICON_BORDER_WIDTH * 2 + ICON_SHADOW_SIZE_DEFAULT * 2
            val result = Bitmap.createBitmap(size, size, source.config)
            val canvas = Canvas(result)

            val shadowPaint = Paint().apply {
                style = FILL
                color = ICON_SHADOW_COLOR
                flags = ANTI_ALIAS_FLAG
                maskFilter = BlurMaskFilter(ICON_SHADOW_SIZE_DEFAULT.toFloat(), OUTER)
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
            }
            canvas.drawCircle(center, center, bitmapRadius, bitmapPaint)
            source.recycle()

            return result
        }

        override fun key() = "border"
    }

}
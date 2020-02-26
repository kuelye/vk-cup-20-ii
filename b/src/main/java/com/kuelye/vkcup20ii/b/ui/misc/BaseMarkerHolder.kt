package com.kuelye.vkcup20ii.b.ui.misc

import android.graphics.*
import android.graphics.BlurMaskFilter.Blur.OUTER
import android.graphics.Color.WHITE
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.Paint.Style.FILL
import android.graphics.Paint.Style.STROKE
import android.graphics.Shader.TileMode.CLAMP
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.core.graphics.scaleMatrix
import androidx.core.graphics.translationMatrix
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.clustering.ClusterItem
import com.kuelye.vkcup20ii.b.ui.misc.BaseMarkerHolder.IconTransformation.BorderType.CIRCLE
import com.kuelye.vkcup20ii.core.utils.sp
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import com.squareup.picasso.Transformation
import com.vk.api.sdk.utils.VKUtils.dp

abstract class BaseMarkerHolder(
    private val clusterRenderer: MarkerRenderer<in BaseMarkerHolder>
): ClusterItem {

    companion object {
        private val TAG = Marker::class.java.simpleName
        val ICON_BORDER_WIDTH = dp(3)
        val ICON_SHADOW_SIZE_DEFAULT = dp(4)
        val ICON_SHADOW_SIZE_SELECTED = dp(8)
        val CLUSTER_ICON_TEXT_PADDING = dp(6)
        val CLUSTER_ICON_TEXT_SIZE = sp(15)
        private val SQUARE_ICON_CORNER_RADIUS = dp(6)
        const val PLACEHOLDER_COLOR = 0xFF99A2AD.toInt()
        const val ICON_BORDER_COLOR = WHITE
        const val ICON_SHADOW_COLOR = 0x40000000
        const val BADGE_COLOR = 0xFF3F8AE0.toInt()
        const val CLUSTER_ICON_TEXT_COLOR = WHITE
    }

    private var clusterItemTarget: Target = object : Target {
        override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}

        override fun onBitmapFailed(e: java.lang.Exception?, errorDrawable: Drawable?) {}

        override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
            val icon = BitmapDescriptorFactory.fromBitmap(bitmap)
            clusterRenderer.getMarker(this@BaseMarkerHolder)?.setIcon(icon)
        }
    }

    private val clusterTarget = object : Target {
        override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}

        override fun onBitmapFailed(e: java.lang.Exception?, errorDrawable: Drawable?) {}

        override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
            if (this@BaseMarkerHolder is PhotoMarkerHolder) {
                Log.v(TAG, "GUB onBitmapLoaded: ${this@BaseMarkerHolder.photo.iconPhoto} ${clusterRenderer.getClusterMarker(this@BaseMarkerHolder)}")
            }
            val icon = BitmapDescriptorFactory.fromBitmap(bitmap)
            clusterRenderer.getClusterMarker(this@BaseMarkerHolder)?.setIcon(icon)
        }
    }

    override fun getTitle(): String? = null

    override fun getSnippet(): String? = null

    open fun onClusterItemRendered() {}

    open fun onClusterRendered(clusterSize: Int) {}

    open fun getClusterBitmap(clusterSize: Int): Bitmap? = null

    protected fun updateIcon(
        photo: String, size: Int, transformation: Transformation, intoCluster: Boolean = false
    ) {
        Picasso.get().load(photo)
            .resize(size, size)
            .centerCrop()
            .placeholder(ColorDrawable(PLACEHOLDER_COLOR))
            .error(ColorDrawable(PLACEHOLDER_COLOR))
            .transform(transformation)
            .into(if (intoCluster) clusterTarget else clusterItemTarget)
    }

    protected class IconTransformation(
        private val selected: Boolean,
        private val borderType: BorderType,
        private val clusterSize: Int? = null
    ) : Transformation {
        override fun transform(source: Bitmap): Bitmap {
            // calculate and create canvas
            val shadowSize = if (selected) ICON_SHADOW_SIZE_SELECTED else ICON_SHADOW_SIZE_DEFAULT
            val size = source.width + ICON_BORDER_WIDTH * 2 + shadowSize * 2
            val result = Bitmap.createBitmap(size, size, source.config)
            val canvas = Canvas(result)

            // shadow
            val shadowPaint = Paint().apply {
                style = FILL
                color = ICON_SHADOW_COLOR
                flags = ANTI_ALIAS_FLAG
                maskFilter = BlurMaskFilter(shadowSize.toFloat(), OUTER)
            }
            val c = size.toFloat() / 2
            val bitmapR = source.width.toFloat() / 2
            val borderR = bitmapR + ICON_BORDER_WIDTH / 2

            // border
            val borderPaint = Paint().apply {
                style = STROKE
                strokeWidth = ICON_BORDER_WIDTH.toFloat()
                color = ICON_BORDER_COLOR
                isAntiAlias = true
            }

            // source
            val bitmapPaint = Paint().apply {
                isAntiAlias = true
                shader = BitmapShader(source, CLAMP, CLAMP)
                val matrix = scaleMatrix()
                val d = (shadowSize + ICON_BORDER_WIDTH).toFloat()
                shader.setLocalMatrix(translationMatrix(d, d))
            }

            // draw
            if (borderType == CIRCLE) {
                canvas.drawCircle(c, c, borderR, shadowPaint)
                canvas.drawCircle(c, c, borderR, borderPaint)
                canvas.drawCircle(c, c, bitmapR, bitmapPaint)
            } else {
                val cornerR = SQUARE_ICON_CORNER_RADIUS.toFloat()
                var lT = c - borderR
                var rB = c + borderR
                canvas.drawRoundRect(lT, lT, rB, rB, cornerR, cornerR, shadowPaint)
                canvas.drawRoundRect(lT, lT, rB, rB, cornerR, cornerR, borderPaint)
                lT = c - bitmapR
                rB = c + bitmapR
                canvas.drawRoundRect(lT, lT, rB, rB, cornerR, cornerR, bitmapPaint)

                // badge
                if (clusterSize != null) {
                    val fillPaint = Paint().apply {
                        style = FILL
                        color = PLACEHOLDER_COLOR
                        flags = ANTI_ALIAS_FLAG
                        textSize = CLUSTER_ICON_TEXT_SIZE
                        isAntiAlias = true
                    }
                    val text = clusterSize.toString()
                    val bounds = Rect()
                    fillPaint.getTextBounds(text, 0, text.length, bounds)
                    val textWidth = fillPaint.measureText(text)
                    val tX = canvas.width - CLUSTER_ICON_TEXT_PADDING - textWidth
                    val tY = (CLUSTER_ICON_TEXT_PADDING + bounds.height()).toFloat()

                    fillPaint.color = BADGE_COLOR
                    canvas.drawOval(tX - CLUSTER_ICON_TEXT_PADDING, 0f,
                        canvas.width.toFloat(), tY + CLUSTER_ICON_TEXT_PADDING, fillPaint)

                    fillPaint.color = CLUSTER_ICON_TEXT_COLOR
                    canvas.drawText(text, tX, tY, fillPaint)
                }
            }

            source.recycle()

            return result
        }

        override fun key() = "border-${if (selected) "1" else "0"}-$clusterSize"

        enum class BorderType {
            CIRCLE, SQUARE
        }

    }

}
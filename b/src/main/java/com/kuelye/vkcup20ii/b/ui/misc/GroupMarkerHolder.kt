package com.kuelye.vkcup20ii.b.ui.misc

import android.graphics.*
import android.graphics.Bitmap.Config.ARGB_8888
import android.graphics.Color.WHITE
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.Paint.Style.FILL
import android.graphics.Paint.Style.STROKE
import android.graphics.Shader.TileMode.CLAMP
import androidx.core.graphics.scaleMatrix
import androidx.core.graphics.translationMatrix
import com.google.android.gms.maps.model.LatLng
import com.kuelye.vkcup20ii.core.model.VKAddress
import com.kuelye.vkcup20ii.core.model.VKGroup
import com.kuelye.vkcup20ii.core.utils.sp
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation
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
        private val ICON_BORDER_WIDTH = dp(3)
        private const val ICON_BORDER_COLOR = WHITE
        private val ICON_SHADOW_SIZE_DEFAULT = dp(4)
        private val ICON_SHADOW_SIZE_SELECTED = dp(8)
        private const val ICON_SHADOW_COLOR = 0x40000000
        private const val CLUSTER_ICON_BACKGROUND_COLOR = 0xFF99A2AD.toInt()
        private const val CLUSTER_ICON_TEXT_COLOR = WHITE
        private val CLUSTER_ICON_TEXT_SIZE = sp(15)
        private val CLUSTER_ICON_TEXT_PADDING = dp(6)
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
        val size = if (selected) CENTER_ICON_SIZE_SELECTED else CENTER_ICON_SIZE_DEFAULT
        Picasso.get().load(group.photo200)
            .resize(size, size)
            //            .placeholder(ColorDrawable(PLACEHOLDER_COLOR)) // TODO
            //            .error(ColorDrawable(PLACEHOLDER_COLOR))
            .transform(BorderTransformation(selected))
            .into(this)
    }

    override fun getClusterBitmap(clusterSize: Int): Bitmap? {
        // calculate sizes
        val fillPaint = Paint().apply {
            style = FILL
            color = CLUSTER_ICON_BACKGROUND_COLOR
            flags = ANTI_ALIAS_FLAG
            textSize = CLUSTER_ICON_TEXT_SIZE
            isAntiAlias = true
        }
        fillPaint.color = CLUSTER_ICON_TEXT_COLOR
        fillPaint.textSize = CLUSTER_ICON_TEXT_SIZE
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
            maskFilter = BlurMaskFilter(shadowSize.toFloat(), BlurMaskFilter.Blur.OUTER)
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
        fillPaint.color = CLUSTER_ICON_BACKGROUND_COLOR
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
        updateIcon()
    }

    class BorderTransformation(
        private val selected: Boolean
    ) : Transformation {
        override fun transform(source: Bitmap): Bitmap {
            // calculate and create canvas
            val shadowSize = if (selected) ICON_SHADOW_SIZE_SELECTED else ICON_SHADOW_SIZE_DEFAULT
            val size = source.width + ICON_BORDER_WIDTH * 2 + shadowSize * 2
            val result = Bitmap.createBitmap(size, size, source.config)
            val canvas = Canvas(result)

            // draw shadow
            val shadowPaint = Paint().apply {
                style = FILL
                color = ICON_SHADOW_COLOR
                flags = ANTI_ALIAS_FLAG
                maskFilter = BlurMaskFilter(shadowSize.toFloat(), BlurMaskFilter.Blur.OUTER)
            }
            val center = size.toFloat() / 2
            val bitmapRadius = source.width.toFloat() / 2
            val borderRadius = bitmapRadius + ICON_BORDER_WIDTH / 2
            canvas.drawCircle(center, center, borderRadius, shadowPaint)

            // draw border
            val borderPaint = Paint().apply {
                style = STROKE
                strokeWidth = ICON_BORDER_WIDTH.toFloat()
                color = ICON_BORDER_COLOR
                isAntiAlias = true
            }
            canvas.drawCircle(center, center, borderRadius, borderPaint)

            // draw source
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
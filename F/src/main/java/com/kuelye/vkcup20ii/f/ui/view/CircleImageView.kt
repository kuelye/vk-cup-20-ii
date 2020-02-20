package com.kuelye.vkcup20ii.f.ui.view

import android.content.Context
import android.graphics.*
import android.graphics.Paint.Style.STROKE
import android.graphics.Shader.TileMode.CLAMP
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.ImageView
import androidx.annotation.ColorInt
import com.kuelye.vkcup20ii.core.utils.toBitmap
import com.vk.api.sdk.utils.VKUtils.dp

open class CircleImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ImageView(context, attrs, defStyleAttr) {

    companion object {
        private const val BORDER_COLOR_DEFAULT = Color.RED
        private val BORDER_WIDTH_DEFAULT = dp(1)
    }

    @ColorInt
    var borderColor: Int = BORDER_COLOR_DEFAULT
        set(value) {
            field = value
            borderPaint.color = borderColor
            invalidate()
        }
    var borderWidth: Float = BORDER_WIDTH_DEFAULT.toFloat()
        set(value) {
            field = value
            borderPaint.strokeWidth = borderWidth
            invalidate()
        }
    var scale: Float = 1f
        set(value) {
            field = value
            updateShaderMatrix()
        }

    private var bitmap: Bitmap? = null
    private val shaderMatrix = Matrix()
    private val paint = Paint().apply {
        isAntiAlias = true
        isFilterBitmap = true
        isDither = true
    }

    private val borderPaint = Paint().apply {
        style = STROKE
        strokeWidth = borderWidth
        color = borderColor
        isAntiAlias = true
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateByBitmap()
    }

    override fun onDraw(canvas: Canvas) {
        if (bitmap != null) {
            val halfWidth = width.toFloat() / 2
            val halfHeight = height.toFloat() / 2
            val r = halfWidth - borderWidth / 2
            canvas.drawCircle(halfWidth, halfHeight, r, borderPaint)
            canvas.drawCircle(halfWidth, halfHeight, r * scale, paint)
        }
    }

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        updateByBitmap()
    }

    private fun updateByBitmap() {
        bitmap = toBitmap(drawable)
        if (bitmap == null) {
            paint.shader = null
        } else {
            paint.shader = BitmapShader(bitmap!!, CLAMP, CLAMP)
            updateShaderMatrix()
        }
    }

    private fun updateShaderMatrix() {
        if (paint.shader != null) {
            shaderMatrix.apply {
                reset()
                val drawingRect = Rect().apply { getDrawingRect(this) }
                val d = drawingRect.width().toFloat() * (1f - scale) / 2
                val scale = drawingRect.width().toFloat() * scale / bitmap!!.width
                setScale(scale, scale)
                postTranslate(d, d)
            }
            paint.shader!!.setLocalMatrix(shaderMatrix)
        }
    }

}
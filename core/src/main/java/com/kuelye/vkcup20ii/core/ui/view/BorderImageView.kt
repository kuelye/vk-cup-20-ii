package com.kuelye.vkcup20ii.core.ui.view

import android.content.Context
import android.graphics.*
import android.graphics.Paint.Style.STROKE
import android.graphics.Shader.TileMode.CLAMP
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.ImageView
import androidx.annotation.ColorInt
import com.kuelye.vkcup20ii.core.R
import com.kuelye.vkcup20ii.core.utils.toBitmap
import com.vk.api.sdk.utils.VKUtils.dp

open class BorderImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ImageView(context, attrs, defStyleAttr) {

    companion object {
        private val TAG = BorderImageView::class.java.simpleName
        private const val BORDER_COLOR_DEFAULT = Color.RED
        private val BORDER_WIDTH_DEFAULT = dp(1)
        private val BORDER_TYPE_DEFAULT = BorderType.CIRCLE
        private val BORDER_CORNER_RADIUS = dp(6)
    }

    @ColorInt
    var borderColor: Int =
        BORDER_COLOR_DEFAULT
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
    var borderType: BorderType = BORDER_TYPE_DEFAULT
        set(value) {
            field = value
            invalidate()
        }
    var borderCornerRadius: Float = BORDER_CORNER_RADIUS.toFloat()
        set(value) {
            field = value
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

    init {
        initializeAttrs(attrs)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateByDrawable()
    }

    override fun onDraw(canvas: Canvas) {
        if (bitmap != null) {
            val w = width.toFloat() - paddingStart - paddingEnd
            val h = height.toFloat() - paddingTop - paddingBottom
            val hBW = borderWidth / 2
            if (borderType == BorderType.CIRCLE) {
                val hW = w / 2
                val hH = h / 2
                val cX = hW + paddingStart
                val cY = hH + paddingTop
                val r = hW - hBW
                if (borderWidth > 0) canvas.drawCircle(cX, cY, r, borderPaint)
                canvas.drawCircle(cX, cY, (hW - borderWidth) * scale, paint)
            } else {
                if (borderWidth > 0) canvas.drawRoundRect(paddingStart + hBW, paddingTop + hBW,
                    width - paddingEnd - hBW, height - paddingBottom - hBW,
                    borderCornerRadius, borderCornerRadius, borderPaint)
                // TODO support scale in square mode
                canvas.drawRoundRect(paddingStart + borderWidth, paddingTop + borderWidth,
                    width - paddingEnd - borderWidth, height - paddingBottom - borderWidth,
                    borderCornerRadius, borderCornerRadius, paint)
            }
        }
    }

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        updateByDrawable()
    }

    override fun setImageResource(resId: Int) {
        super.setImageResource(resId)
        updateByDrawable()
    }

    private fun initializeAttrs(attrs: AttributeSet?) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.BorderImageView)
        for (i in 0 until a.indexCount) {
            when (val attr = a.getIndex(i)) {
                R.styleable.BorderImageView_borderColor -> borderColor =
                    a.getColor(attr, BORDER_COLOR_DEFAULT)
                R.styleable.BorderImageView_borderWidth -> borderWidth =
                    a.getDimension(attr, BORDER_WIDTH_DEFAULT.toFloat())
                R.styleable.BorderImageView_borderType -> borderType =
                    BorderType.forValue(a.getInt(attr, BORDER_TYPE_DEFAULT.value))
                R.styleable.BorderImageView_borderCornerRadius -> borderCornerRadius =
                    a.getDimension(attr, BORDER_CORNER_RADIUS.toFloat())
            }
        }
        a.recycle()
    }

    private fun updateByDrawable() {
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
                val w = drawingRect.width().toFloat() - paddingLeft - paddingRight
                val h = drawingRect.height().toFloat() - paddingTop - paddingBottom
                setScale(w * scale / bitmap!!.width, h * scale / bitmap!!.height)
                postTranslate(d, d)
            }
            paint.shader!!.setLocalMatrix(shaderMatrix)
        }
    }

    enum class BorderType(val value: Int) {
        CIRCLE(0), SQUARE(1);
        companion object {
            fun forValue(value: Int): BorderType =
                values().firstOrNull { it.value == value } ?: BORDER_TYPE_DEFAULT
        }
    }

}
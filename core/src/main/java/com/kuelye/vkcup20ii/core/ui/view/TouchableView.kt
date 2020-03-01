package com.kuelye.vkcup20ii.core.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_MOVE
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup

class TouchableView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var onTouchListener: ((event : MotionEvent) -> Unit)? = null
    var onScrollListener: ((event : MotionEvent) -> Unit)? = null

    private var touchSlopSquare = 0
    private var touchDownX = 0f
    private var touchDownY = 0f

    init {
        val configuration = ViewConfiguration.get(context)
        touchSlopSquare = configuration.scaledTouchSlop * configuration.scaledTouchSlop

    }

//    override fun onTouchEvent(event: MotionEvent): Boolean {
//        onTouchListener?.invoke(event)
//        when (event.action) {
//            ACTION_DOWN -> {
//                touchDownX = event.x
//                touchDownY = event.y
//                return true
//            }
//            ACTION_MOVE -> {
//                val dx = event.x - touchDownX
//                val dy = event.y - touchDownY
//                if (dx * dx + dy * dy > touchSlopSquare) {
//                    onScrollListener?.invoke(event)
//                    return false
//                }
//            }
//        }
//        return super.onTouchEvent(event)
//    }
//
//    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
//        onTouchListener?.invoke(event)
//        when (event.action) {
//            ACTION_DOWN -> {
//                touchDownX = event.x
//                touchDownY = event.y
//                return true
//            }
//            ACTION_MOVE -> {
//                val dx = event.x - touchDownX
//                val dy = event.y - touchDownY
//                if (dx * dx + dy * dy > touchSlopSquare) {
//                    onScrollListener?.invoke(event)
//                    return false
//                }
//            }
//        }
//        return super.dispatchTouchEvent(event)
//    }

}
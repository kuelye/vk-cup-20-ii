package com.kuelye.vkcup20ii.core.ui.misc

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class SpaceItemDecoration(
    private val horizontalSpace: Int,
    private val verticalSpace: Int,
    private val spanCount: Int
) : RecyclerView.ItemDecoration() {

    private val horizontalSpacePerSpan = horizontalSpace / spanCount

    override fun getItemOffsets(
        outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
    ) {
        val position = parent.getChildLayoutPosition(view)
        val column = parent.getChildLayoutPosition(view) % spanCount
        val row = position / spanCount
        outRect.left = column * horizontalSpacePerSpan
        outRect.right = horizontalSpace - (column + 1) * horizontalSpacePerSpan
        if (row != 0) outRect.top = verticalSpace
    }

}

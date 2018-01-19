package com.condecosoftware.core.recyclerview

import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.View


/**
 * Used to supply vertical cell spacing for a recycle view
 */
class VerticalSpaceItemDecoration(private val mVerticalSpaceHeight: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView,
                                state: RecyclerView.State) {
        outRect.bottom = mVerticalSpaceHeight
    }
}
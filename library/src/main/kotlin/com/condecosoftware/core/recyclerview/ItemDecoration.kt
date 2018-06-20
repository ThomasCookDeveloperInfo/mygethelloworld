package com.condecosoftware.core.recyclerview

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator


/**
 * Used to supply vertical cell spacing for a recycle view
 */
class VerticalSpaceItemDecoration(private val mVerticalSpaceHeight: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView,
                                state: RecyclerView.State) {
        outRect.bottom = mVerticalSpaceHeight
    }
}


/**
 * Used to draw list item separators in a [RecyclerView]
 */

class VerticalItemDecoration(context: Context) : RecyclerView.ItemDecoration() {

    private val mDivider: Drawable?

    private val mBounds = Rect()

    private var mDrawLast = true

    init {
        val a = context.obtainStyledAttributes(ATTRS)
        mDivider = a.getDrawable(0)
        a.recycle()
    }

    fun setDrawLast(mDrawLast: Boolean) {
        this.mDrawLast = mDrawLast
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State?) {
        if (parent.layoutManager == null) {
            return
        }
        drawVertical(c, parent)
    }

    @SuppressLint("NewApi")
    private fun drawVertical(canvas: Canvas, parent: RecyclerView) {
        canvas.save()
        val left: Int
        val right: Int
        if (parent.clipToPadding) {
            left = parent.paddingLeft
            right = parent.width - parent.paddingRight
            canvas.clipRect(left, parent.paddingTop, right,
                    parent.height - parent.paddingBottom)
        } else {
            left = 0
            right = parent.width
        }

        var childCount = parent.childCount
        if (!mDrawLast)
            --childCount

        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            parent.getDecoratedBoundsWithMargins(child, mBounds)
            val bottom = mBounds.bottom + Math.round(child.translationY)
            val top = bottom - mDivider!!.intrinsicHeight
            mDivider.setBounds(left, top, right, bottom)
            mDivider.draw(canvas)
        }
        canvas.restore()
    }

    companion object {
        private val ATTRS = intArrayOf(android.R.attr.listDivider)
    }
}

/**
 * Shamelessly pintched this code from https://stackoverflow.com/questions/33841363/how-to-make-a-page-indicator-for-horizontal-recyclerview
 * Another good resource for item decorators is here: https://github.com/bleeding182/recyclerviewItemDecorations
 */
class CirclePagerIndicatorDecoration(private val colorActive: Int,
                                     private val colorInactive: Int) : RecyclerView.ItemDecoration() {
//    private val colorActive: Int = -1
//    private val colorInactive: Int = 0x66FFFFFF

    private val DP = Resources.getSystem().displayMetrics.density

    /**
     * Height of the space the indicator takes up at the bottom of the view.
     */
    private val mIndicatorHeight = (DP * 16).toInt()

    /**
     * Indicator stroke width.
     */
    private val mIndicatorStrokeWidth = DP * 4

    /**
     * Indicator width.
     */
    private val mIndicatorItemLength = DP * 4
    /**
     * Padding between indicators.
     */
    private val mIndicatorItemPadding = DP * 8

    /**
     * Some more natural animation interpolation
     */
    private val mInterpolator = AccelerateDecelerateInterpolator()

    private val mPaint = Paint()

    init {
        mPaint.strokeWidth = mIndicatorStrokeWidth
        mPaint.style = Paint.Style.STROKE
        mPaint.isAntiAlias = true
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State?) {
        super.onDrawOver(c, parent, state)

        val itemCount = parent.adapter.itemCount

        // center horizontally, calculate width and subtract half from center
        val totalLength = mIndicatorItemLength * itemCount
        val paddingBetweenItems = Math.max(0, itemCount - 1) * mIndicatorItemPadding
        val indicatorTotalWidth = totalLength + paddingBetweenItems
        val indicatorStartX = (parent.width - indicatorTotalWidth) / 2f

        // center vertically in the allotted space
        val indicatorPosY = parent.height - mIndicatorHeight / 2f

        drawInactiveIndicators(c, indicatorStartX, indicatorPosY, itemCount)

        // find active page (which should be highlighted)
        val layoutManager = parent.layoutManager as LinearLayoutManager
        val activePosition = layoutManager.findFirstVisibleItemPosition()
        if (activePosition == RecyclerView.NO_POSITION) {
            return
        }

        // find offset of active page (if the user is scrolling)
        val activeChild = layoutManager.findViewByPosition(activePosition)
        val left = activeChild.left
        val width = activeChild.width

        // on swipe the active item will be positioned from [-width, 0]
        // interpolate offset for smooth animation
        val progress = mInterpolator.getInterpolation(left * -1 / width.toFloat())

        drawHighlights(c, indicatorStartX, indicatorPosY, activePosition, progress)
    }

    private fun drawInactiveIndicators(c: Canvas, indicatorStartX: Float, indicatorPosY: Float, itemCount: Int) {
        mPaint.color = colorInactive

        // width of item indicator including padding
        val itemWidth = mIndicatorItemLength + mIndicatorItemPadding

        var start = indicatorStartX
        for (i in 0 until itemCount) {

            c.drawCircle(start, indicatorPosY, mIndicatorItemLength / 2f, mPaint)

            start += itemWidth
        }
    }

    private fun drawHighlights(c: Canvas, indicatorStartX: Float, indicatorPosY: Float,
                               highlightPosition: Int, progress: Float) {
        mPaint.color = colorActive

        // width of item indicator including padding
        val itemWidth = mIndicatorItemLength + mIndicatorItemPadding

        if (progress == 0f) {
            // no swipe, draw a normal indicator
            val highlightStart = indicatorStartX + itemWidth * highlightPosition

            c.drawCircle(highlightStart, indicatorPosY, mIndicatorItemLength / 2f, mPaint)

        } else {
            val highlightStart = indicatorStartX + itemWidth * highlightPosition
            // calculate partial highlight
            val partialLength = mIndicatorItemLength * progress + mIndicatorItemPadding * progress

            c.drawCircle(highlightStart + partialLength, indicatorPosY, mIndicatorItemLength / 2f, mPaint)
        }
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State?) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.bottom = mIndicatorHeight
    }
}
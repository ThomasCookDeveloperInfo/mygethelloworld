@file:Suppress("unused")

package com.condecosoftware.core.recyclerview

import android.support.v7.util.DiffUtil
import android.support.v7.util.ListUpdateCallback
import android.support.v7.widget.RecyclerView

/**
 * Helper utility functions for sending notifications to a [RecyclerView]
 */
object RecyclerViewUtils {
    fun notifyItemRangeInserted(
            recyclerView: RecyclerView,
            adapter: RecyclerView.Adapter<*>, startPos: Int, itemCount: Int) {

        if (recyclerView.isComputingLayout) {
            recyclerView.post { notifyItemRangeInserted(recyclerView, adapter, startPos, itemCount) }
        } else {
            adapter.notifyItemRangeInserted(startPos, itemCount)
        }
    }

    fun notifyItemInserted(
            recyclerView: RecyclerView,
            adapter: RecyclerView.Adapter<*>, startPos: Int) {
        notifyItemRangeInserted(recyclerView, adapter, startPos, 1)
    }

    fun notifyItemRangeRemoved(
            recyclerView: RecyclerView,
            adapter: RecyclerView.Adapter<*>, startPos: Int, itemCount: Int) {

        if (recyclerView.isComputingLayout) {
            recyclerView.post { notifyItemRangeRemoved(recyclerView, adapter, startPos, itemCount) }
        } else {
            adapter.notifyItemRangeRemoved(startPos, itemCount)
        }
    }

    fun notifyItemRemoved(
            recyclerView: RecyclerView,
            adapter: RecyclerView.Adapter<*>, startPos: Int) {
        notifyItemRangeRemoved(recyclerView, adapter, startPos, 1)
    }

    fun notifyItemRangeChanged(
            recyclerView: RecyclerView,
            adapter: RecyclerView.Adapter<*>, startPos: Int, itemCount: Int) {

        if (recyclerView.isComputingLayout) {
            recyclerView.post { notifyItemRangeChanged(recyclerView, adapter, startPos, itemCount) }
        } else {
            adapter.notifyItemRangeChanged(startPos, itemCount)
        }
    }

    fun notifyItemMoved(recyclerView: RecyclerView,
                        adapter: RecyclerView.Adapter<*>,
                        fromPost: Int, toPos: Int) {
        if (recyclerView.isComputingLayout) {
            recyclerView.post { notifyItemMoved(recyclerView, adapter, fromPost, toPos) }
        } else {
            adapter.notifyItemMoved(fromPost, toPos)
        }

    }

    fun notifyItemChanged(
            recyclerView: RecyclerView,
            adapter: RecyclerView.Adapter<*>, startPos: Int) {
        notifyItemRangeChanged(recyclerView, adapter, startPos, 1)
    }

    fun clearRecyclerView(recyclerView: RecyclerView,
                          adapter: RecyclerViewBase.ViewAdapter<*>,
                          startPos: Int, length: Int) {
        for (index in startPos + length downTo startPos) {
            adapter.removeData(index)
        }
        notifyItemRangeRemoved(recyclerView, adapter, startPos, length)
    }
}

/**
 * Used to create a [DiffUtil.Callback] to be used with [android.support.v7.widget.RecyclerView]
 * in order to issue list changes notifications.
 */
class RecyclerDiffCallback(private val oldList: List<RecyclerViewBase.ViewModelBase>,
                           private val newList: List<RecyclerViewBase.ViewModelBase>) : DiffUtil.Callback() {

    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldValue = oldList.getOrNull(oldItemPosition)
        val newValue = newList.getOrNull(newItemPosition)
        return oldValue == newValue
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldValue = oldList.getOrNull(oldItemPosition)
        val newValue = newList.getOrNull(newItemPosition)
        return oldValue == newValue
    }
}

/**
 * Callback class for safely posting updates to the recycler view
 */
class RecycleUpdateCallback(private val recyclerView: RecyclerView,
                            private val adapter: RecyclerView.Adapter<*>) : ListUpdateCallback {
    override fun onChanged(position: Int, count: Int, payload: Any?) {
        RecyclerViewUtils.notifyItemRangeChanged(
                recyclerView, adapter, position, count)
    }

    override fun onMoved(fromPosition: Int, toPosition: Int) {
        RecyclerViewUtils.notifyItemMoved(
                recyclerView, adapter, fromPosition, toPosition)
    }

    override fun onInserted(position: Int, count: Int) {
        RecyclerViewUtils.notifyItemRangeInserted(
                recyclerView, adapter, position, count)
    }

    override fun onRemoved(position: Int, count: Int) {
        RecyclerViewUtils.notifyItemRangeRemoved(
                recyclerView, adapter, position, count)
    }
}


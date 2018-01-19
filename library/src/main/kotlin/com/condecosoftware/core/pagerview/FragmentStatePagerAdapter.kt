package com.condecosoftware.core.pagerview

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Parcelable
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.support.v4.view.PagerAdapter
import android.util.Log
import android.view.View
import android.view.ViewGroup
import java.util.*

private const val TAG = "FragStatePagerAdapter"

abstract class FragmentStatePagerAdapter(private val mFragmentManager: FragmentManager) : PagerAdapter() {

    private var mCurTransaction: FragmentTransaction? = null
    private val mSavedState = ArrayList<Fragment.SavedState?>()
    protected val mFragments = ArrayList<Fragment?>()

    var currentPrimaryItem: Fragment? = null

    /**
     * Return the Fragment associated with a specified position.
     */
    abstract fun getItem(position: Int): Fragment

    override fun startUpdate(container: ViewGroup) {
        if (container.id == View.NO_ID) {
            throw IllegalStateException("ViewPager with adapter " + this
                    + " requires a view id")
        }
    }

    @SuppressLint("CommitTransaction")
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        // If we already have this item instantiated, there is nothing
        // to do.  This can happen when we are restoring the entire pager
        // from its saved state, where the fragment manager has already
        // taken care of restoring the fragments we previously had instantiated.
        if (mFragments.size > position) {
            val f = mFragments[position]
            if (f !== null) {
                return f
            }
        }

        val currentTransaction = mCurTransaction ?: mFragmentManager.beginTransaction()
        mCurTransaction = currentTransaction

        val fragment = getItem(position)
        if (mSavedState.size > position) {
            val fss = mSavedState[position]
            fss?.let {
                fragment.setInitialSavedState(it)
            }
        }
        while (mFragments.size <= position) {
            mFragments.add(null)
        }
        fragment.setMenuVisibility(false)
        fragment.userVisibleHint = false
        mFragments[position] = fragment
        currentTransaction.add(container.id, fragment)

        return fragment
    }

    @SuppressLint("CommitTransaction")
    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        val fragment = `object` as Fragment

        val currentTransaction = mCurTransaction ?: mFragmentManager.beginTransaction()
        mCurTransaction = currentTransaction

        while (mSavedState.size <= position) {
            mSavedState.add(null)
        }
        mSavedState[position] = if (fragment.isAdded)
            mFragmentManager.saveFragmentInstanceState(fragment)
        else
            null
        mFragments[position] = null

        currentTransaction.remove(fragment)
    }

    override fun setPrimaryItem(container: ViewGroup, position: Int, item: Any) {
        val fragment = item as? Fragment ?: return

        if (fragment !== currentPrimaryItem) {
            currentPrimaryItem?.let {
                it.setMenuVisibility(false)
                it.userVisibleHint = false
            }
            fragment.setMenuVisibility(true)
            fragment.userVisibleHint = true
            currentPrimaryItem = fragment
        }
    }

    override fun finishUpdate(container: ViewGroup) {
        val currentTransaction = mCurTransaction ?: return
        mCurTransaction = null
        currentTransaction.commitNowAllowingStateLoss()
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return (`object` as Fragment?)?.view === view
    }

    override fun saveState(): Parcelable? {
        var state: Bundle? = null
        if (mSavedState.size > 0) {
            state = Bundle()
            val fss = arrayOfNulls<Fragment.SavedState>(mSavedState.size)
            mSavedState.toArray()
            state.putParcelableArray("states", fss)
        }
        for (i in mFragments.indices) {
            val f = mFragments[i]
            f?.let {
                if (f.isAdded) {
                    if (state === null) {
                        state = Bundle()
                    }
                    val key = "f" + i
                    mFragmentManager.putFragment(state, key, f)
                }
            }
        }
        return state
    }

    override fun restoreState(state: Parcelable?, loader: ClassLoader?) {
        state?.let {
            val bundle = it as Bundle?
            bundle?.classLoader = loader
            val fss = bundle?.getParcelableArray("states")
            mSavedState.clear()
            mFragments.clear()
            fss?.let {
                fss.indices.mapTo(mSavedState) { fss[it] as Fragment.SavedState? }
            }
            val keys = bundle?.keySet()
            keys?.let {
                for (key in it) {
                    if (key.startsWith("f")) {
                        val index = Integer.parseInt(key.substring(1))
                        val f = mFragmentManager.getFragment(bundle, key)
                        if (f !== null) {
                            while (mFragments.size <= index) {
                                mFragments.add(null)
                            }
                            f.setMenuVisibility(false)
                            mFragments[index] = f
                        } else {
                            Log.w(TAG, "Bad fragment at key " + key)
                        }
                    }
                }
            }
        }
    }
}

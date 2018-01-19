package com.condecosoftware.core.utils

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import com.condecosoftware.core.pagerview.FragmentStatePagerAdapter
import java.util.*

/**
 * Fragment view pager adapter with extra functions.
 */
private const val TAG = "FragPageAdapter"

open class FragmentViewPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

    private val fragmentClasses = ArrayList<Class<out Fragment>>(10)
    private val fragmentParameters = ArrayList<Bundle?>(10)
    private val fragmentTitles = ArrayList<CharSequence?>(10)

    /**
     * Add fragment to the pager
     */
    fun addFragment(fragmentClass: Class<out Fragment>, args: Bundle?, pageTitle: CharSequence?) {
        fragmentClasses.add(fragmentClass)
        fragmentParameters.add(args)
        fragmentTitles.add(pageTitle)
    }

    /**
     * Update the parameters for a particular position
     */
    protected fun updateParameters(position: Int, args: Bundle?) {
        if (fragmentParameters.size <= position || position < 0)
            return

        fragmentParameters[position] = args
    }

    override fun getItem(position: Int): Fragment {
        val fragment = fragmentClasses[position].newInstance()
        fragment.arguments = fragmentParameters[position]
        return fragment
    }

    override fun getCount(): Int = fragmentClasses.size

    override fun getPageTitle(position: Int): CharSequence? = fragmentTitles[position]
}
package com.condecosoftware.core.utils

import android.content.Context
import android.net.ConnectivityManager

/**
 * Collection of helper function for networking
 */
object NetworkUtils {

    /**
     * Check if we have network connectivity
     */
    fun isNetworkAvailable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        return cm?.activeNetworkInfo?.isAvailable == true
    }
}
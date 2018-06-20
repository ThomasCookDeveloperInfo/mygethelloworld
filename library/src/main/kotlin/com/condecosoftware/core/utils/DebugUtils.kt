package com.condecosoftware.core.utils

import android.content.Intent
import android.os.Bundle
import android.util.Log

/**
 * Collection of common debug utilities
 */

object DebugUtils {
    /**
     * Used to dump the content of the specified [Intent]
     *
     * @param tag    Tag to be used for logging
     * @param intent Intent to dump
     */
    fun dumpIntent(tag: String, intent: Intent) {
        Log.d(tag, "Intent action: " + intent.action)
        if (intent.extras != null) {
            for (key in intent.extras.keySet()) {
                Log.d(tag, "KEY: " + key + ", VALUE: " + intent.extras.get(key))
            }
        }
    }

    /**
     * Used to dump the content of the specified [Intent]
     *
     * @param tag    Tag to be used for logging
     * @param bundle [Bundle] to dump
     */
    fun dumpBundle(tag: String, bundle: Bundle?) {
        if (bundle == null)
            return

        for (key in bundle.keySet()) {
            Log.d(tag, "KEY: " + key + ", VALUE: " + bundle.get(key))
        }
    }

    /**
     * Used to dumb content of collection.
     *
     * @param tag        Tag to be used for debugging
     * @param collection Collection to dump
     */
    fun dumpCollection(tag: String, collection: Collection<*>) {
        for (`object` in collection) {
            Log.d(tag, "Collection value: " + `object`.toString() + ", hash: " + `object`?.hashCode())
        }
    }
}

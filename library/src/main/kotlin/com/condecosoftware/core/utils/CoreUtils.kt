package com.condecosoftware.core.utils

import android.content.Context
import android.os.IBinder
import android.text.InputFilter
import android.text.Spanned
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import com.condecosoftware.core.recyclerview.RecyclerViewBase
import kotlin.reflect.KClass
import kotlin.reflect.full.cast


//Create type alias for view holder creators
typealias RecyclerViewCreator = RecyclerViewBase.ViewHolderBaseCreator<RecyclerViewBase.ViewHolderBase>

/**
 * Collection of basic utilities
 */
object CoreUtils {
    /**
     * Interface that should be implemented by a class that wants to expose internal listeners
     * to the caller instead of implementing required interfaces themselves.
     */
    interface ListenerProvider {
        fun <T : Any> getListenerForType(forType: KClass<T>): T?
    }

    /**
     * Find first instance of the specified type
     */
    @JvmStatic
    fun <T : Any> getType(forType: KClass<T>, objects: Iterable<Any?>): T? {
        objects.forEach { listener ->
            if (forType.isInstance(listener))
                return forType.cast(listener)
        }
        return null
    }

    /**
     * Function that iterates through the view group and hides keyboard for any view that has it.
     * Also ot calls clearFocus function on the found view.
     */
    @JvmStatic
    fun dismissKeyboard(view: View): Boolean {
        val context = view.context
        //Hide the keyboard
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                ?: return false
        var foundActiveView: View? = null
        var result = false
        if (view is ViewGroup) {
            for (child in ViewsCollection(view)) {
                if (child is ViewGroup) {
                    if (dismissKeyboard(child))
                        return true
                } else {
                    if (imm.isActive(child)) {
                        foundActiveView = child
                        break
                    }
                }
            }
        } else {
            if (imm.isActive(view)) {
                foundActiveView = view
            }
        }

        if (foundActiveView != null) {
            result = imm.hideSoftInputFromWindow(foundActiveView.windowToken, 0)
            foundActiveView.clearFocus()
        }
        return result
    }

    @JvmStatic
    fun dismissKeyboard(context: Context, token: IBinder) {
        //Hide the keyboard
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                ?: return
        imm.hideSoftInputFromWindow(token, 0)
    }
}

/**
 * Extension function that is used to compare content of two collections for equality
 */
fun <T> Collection<T>.contentEquals(other: Collection<T>): Boolean {
    if (other == this) return true
    if (this.size != other.size) return false

    val thisIterator = this.iterator()
    val otherIterator = other.iterator()
    while (thisIterator.hasNext() && otherIterator.hasNext()) {
        if (thisIterator.next() != otherIterator.next())
            return false
    }

    return !thisIterator.hasNext() && !otherIterator.hasNext()
}

/**
 * Filter to stop a user entering numbers starting from 0. It must only be used with number
 * type edit fields
 */
class WholeNoZeroNumberFilter : InputFilter {

    override fun filter(source: CharSequence?, start: Int, end: Int,
                        dest: Spanned?, dStart: Int, dEnd: Int): CharSequence? {

        val dst = dest ?: return null
        val src = source ?: return null

        if (dst.isEmpty() || (start == dStart && end == dEnd)) {
            //Validate that the source doesn't start with 0
            if ((src.toString().toIntOrNull() ?: 0) == 0) {
                return dst
            }
        }

        return null //keep the original
    }
}
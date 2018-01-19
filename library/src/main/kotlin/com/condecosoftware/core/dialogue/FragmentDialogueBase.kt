package com.condecosoftware.core.dialogue

import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction


private const val DIALOGUE_TAG = "dialogue_tag"

/**
 * Base class for fragments
 */
open class FragmentDialogueBase : DialogFragment() {

    protected val dialogueTag: String
        get() =
            arguments?.getString(DIALOGUE_TAG)
                    ?: throw IllegalStateException("Create instance of this dialogue using newInstance method.")


    override fun show(manager: FragmentManager, tag: String) {
        val args = arguments
                ?: throw IllegalStateException("Create instance of this dialogue using newInstance method.")

        args.putString(DIALOGUE_TAG, tag)
        return try {
            super.show(manager, tag)
        } catch (_: IllegalStateException) {
        }
    }

    override fun show(transaction: FragmentTransaction, tag: String): Int {
        val args = arguments
                ?: throw IllegalStateException("Create instance of this dialogue using newInstance method.")

        args.putString(DIALOGUE_TAG, tag)
        return try {
            super.show(transaction, tag)
        } catch (_: IllegalStateException) {
            -1
        }
    }
}

//Called to notify a listener that dialogue's positive button has been clicked
interface IPositiveButtonListener {
    fun onPositiveButtonClicked(dialogueId: String)
}

//Called to notify a listener that dialogue's negative button has been clicked
interface INegativeButtonListener {
    fun onNegativeButtonClicked(dialogueId: String)
}
package com.condecosoftware.core.dialogue

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.view.ContextThemeWrapper
import com.condecosoftware.core.utils.CoreUtils

private const val ARG_YEAR = "arg_year"
private const val ARG_MONTH = "arg_month"
private const val ARG_DAY = "arg_day"
private const val ARG_CALLER_BUNDLE = "arg_caller_bundle"

/**
 * Wrapper dialogue fragment that uses parent fragment or activity for callback
 */
class FragmentDatePickerDialogue : FragmentDialogueBase() {

    interface IListener {
        fun onFragmentDatePickerDateSet(
                dialogueId: String, year: Int, monthOfYear: Int, dayOfMonth: Int, callerArgs: Bundle?)
    }

    companion object {
        /**
         * Create instance of the fragment. Use show method to display it.
         */
        fun newInstance(year: Int, monthOfYear: Int, dayOfMonth: Int, callerArgs: Bundle? = null):
                FragmentDatePickerDialogue {

            val args = Bundle()
            args.putInt(ARG_YEAR, year)
            args.putInt(ARG_MONTH, monthOfYear)
            args.putInt(ARG_DAY, dayOfMonth)
            args.putParcelable(ARG_CALLER_BUNDLE, callerArgs)

            val fragment = FragmentDatePickerDialogue()
            fragment.arguments = args

            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val args = arguments
                ?: throw IllegalStateException("Create instance of this dialogue using newInstance method.")
        val baseContext = activity ?: context
        ?: throw IllegalStateException("Fragment not attached.")

        val context = if (theme > 0) ContextThemeWrapper(baseContext, theme) else baseContext

        val year = args.getInt(ARG_YEAR)
        val month = args.getInt(ARG_MONTH)
        val day = args.getInt(ARG_DAY)

        val dialogue = DatePickerDialog(context, DatePickerDialog.OnDateSetListener { v, y, m, d ->

            if (!v.isShown)
                return@OnDateSetListener

            CoreUtils.getType(CoreUtils.ListenerProvider::class, listOf(parentFragment, activity))
                    ?.getListenerForType(IListener::class)
                    ?.onFragmentDatePickerDateSet(dialogueTag, y, m, d, args.getParcelable(ARG_CALLER_BUNDLE))
        }, year, month, day)

        dialogue.setCancelable(isCancelable)
        dialogue.setCanceledOnTouchOutside(isCancelable)
        return dialogue
    }
}
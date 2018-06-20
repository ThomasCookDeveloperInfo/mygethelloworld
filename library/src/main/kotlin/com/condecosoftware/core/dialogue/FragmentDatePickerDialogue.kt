package com.condecosoftware.core.dialogue

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.view.ContextThemeWrapper
import com.condecosoftware.core.utils.CoreUtils
import java.util.*

private const val ARG_START_DATE = "arg_start_date"
private const val ARG_END_DATE = "arg_end_date"
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
        fun newInstance(startDate: Date, endDate: Date? = null, callerArgs: Bundle? = null):
                FragmentDatePickerDialogue {

            val args = Bundle()
            args.putLong(ARG_START_DATE, startDate.time)
            if (endDate !== null) {
                args.putLong(ARG_END_DATE, endDate.time)
            }
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

        val calendar = args.getLong(ARG_START_DATE).let { date ->
            Calendar.getInstance().also {
                if (date != 0L) {
                    it.time = Date(date)
                }
            }
        }

        val dialogue = DatePickerDialog(context, DatePickerDialog.OnDateSetListener { v, y, m, d ->
            if (v.isShown) {
                CoreUtils.getType(CoreUtils.ListenerProvider::class, listOf(parentFragment, activity))
                        ?.getListenerForType(IListener::class)
                        ?.onFragmentDatePickerDateSet(dialogueTag, y, m, d, args.getParcelable(ARG_CALLER_BUNDLE))
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
        dialogue.datePicker.minDate = calendar.time.time

        dialogue.setCancelable(isCancelable)
        dialogue.setCanceledOnTouchOutside(isCancelable)

        //Set the end date if it has been specified
        args.getLong(ARG_END_DATE).run {
            if (this != 0L)
                dialogue.datePicker.maxDate = this
        }

        return dialogue
    }
}
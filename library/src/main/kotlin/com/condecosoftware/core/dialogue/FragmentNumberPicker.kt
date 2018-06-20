package com.condecosoftware.core.dialogue

import android.app.Dialog
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.NumberPicker
import com.afollestad.materialdialogs.MaterialDialog
import com.condecosoftware.connect.R
import com.condecosoftware.core.utils.CoreUtils

private const val ARG_TITLE = "arg_title"
private const val ARG_DISPLAY_ITEMS = "arg_display_items"
private const val ARG_BUTTON_TEXT = "arg_button_text"
private const val ARG_CALLER_ARGS = "arg_caller_args"

/**
 * Dialogue fragment for displaying a picker.
 */
class FragmentNumberPicker : FragmentDialogueBase() {

    interface IListener {
        //Called to notify a listener which item has been selected
        fun onFragmentNumberPickerSelected(dialogueId: String, index: Int, callerArgs: Bundle?)
    }

    companion object {
        fun newInstance(title: CharSequence,
                        displayValues: Collection<String>,
                        buttonText: CharSequence,
                        callerArgs: Bundle? = null): FragmentNumberPicker {

            val fragment = FragmentNumberPicker()
            fragment.arguments = Bundle().apply {
                putCharSequence(ARG_TITLE, title)
                if (displayValues is ArrayList) {
                    putStringArrayList(ARG_DISPLAY_ITEMS, displayValues)
                } else {
                    putStringArrayList(ARG_DISPLAY_ITEMS, ArrayList(displayValues))
                }

                putCharSequence(ARG_BUTTON_TEXT, buttonText)
                putBundle(ARG_CALLER_ARGS, callerArgs)
            }

            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val args = arguments
                ?: throw IllegalStateException("Create fragment using newInstance function")

        val baseContext = activity ?: context
        ?: throw IllegalStateException("Fragment not attached.")

        val context = if (theme > 0) ContextThemeWrapper(baseContext, theme) else baseContext

        val builder = MaterialDialog.Builder(context)
        builder.title(args.getCharSequence(ARG_TITLE, ""))
        val items = args.getStringArrayList(ARG_DISPLAY_ITEMS)

        val frame = FrameLayout(context)

        val numberPicker = NumberPicker(context)
        val lp = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER)

        context.resources.getDimension(R.dimen.activity_horizontal_margin).toInt().also {
            lp.marginEnd = it
            lp.marginStart = it
        }

        numberPicker.layoutParams = lp
        frame.addView(numberPicker)

        numberPicker.minValue = 0
        numberPicker.maxValue = items.count() - 1
        numberPicker.displayedValues = items.toTypedArray()

        builder.customView(frame, false)

        //Set positive button text
        builder.positiveText(args.getString(ARG_BUTTON_TEXT, ""))
        builder.onPositive { _, _ ->
            CoreUtils.getType(CoreUtils.ListenerProvider::class, arrayListOf(parentFragment, activity))
                    ?.getListenerForType(IListener::class)?.onFragmentNumberPickerSelected(dialogueTag, numberPicker.value,
                            args.getBundle(ARG_CALLER_ARGS))
        }

        return builder.build()
    }
}
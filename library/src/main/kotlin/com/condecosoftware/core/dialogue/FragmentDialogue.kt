package com.condecosoftware.core.dialogue

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.condecosoftware.core.R
import com.condecosoftware.core.utils.CoreUtils

/**
 * Dialogue fragment for showing an alert with a title, message and up to two buttons.
 */

private const val ARG_LAYOUT_ID = "arg_layout_id"
private const val ARG_MESSAGE = "arg_message"
private const val ARG_TITLE = "arg_title"
private const val ARG_POSITIVE = "arg_positive"
private const val ARG_NEGATIVE = "arg_negative"

class FragmentDialogue : FragmentDialogueBase() {

    companion object {
        fun newInstance(layoutId: Int,
                        title: CharSequence,
                        message: CharSequence,
                        positiveButton: CharSequence,
                        negativeButton: CharSequence): FragmentDialogue {
            val args = Bundle()
            args.putInt(ARG_LAYOUT_ID, layoutId)
            args.putCharSequence(ARG_MESSAGE, message)
            args.putCharSequence(ARG_TITLE, title)
            args.putCharSequence(ARG_POSITIVE, positiveButton)
            args.putCharSequence(ARG_NEGATIVE, negativeButton)

            val fragment = FragmentDialogue()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val args = arguments
                ?: throw IllegalStateException("Create instance using one of the newInstance methods")

        val baseContext: Context = activity ?: context
        ?: throw IllegalStateException("Fragment not attached")

        val theme = theme
        val context: Context = if (theme > 0) ContextThemeWrapper(baseContext, theme) else baseContext

        val layoutId = args.getInt(ARG_LAYOUT_ID, 0)
        if (layoutId == 0)
            throw IllegalArgumentException("Specified layout is invalid")

        val layoutInflater = LayoutInflater.from(context)
        //Inflate the layout

        val viewBinding = android.databinding.DataBindingUtil.inflate<android.databinding.ViewDataBinding>(
                layoutInflater, layoutId, null, false)
                ?: throw IllegalArgumentException("Failed to inflate layout with id $layoutId")

        viewBinding.root.findViewById<TextView>(R.id.dlg_title)?.let {
            it.text = args.getCharSequence(ARG_TITLE)
        }

        viewBinding.root.findViewById<TextView>(R.id.dlg_message)?.let {
            it.text = args.getCharSequence(ARG_MESSAGE)
        }

        val dialogueTag = dialogueTag
        viewBinding.root.findViewById<TextView>(R.id.dlg_positive_button)?.let {
            it.text = args.getCharSequence(ARG_POSITIVE)
            it.setOnClickListener {
                dismiss()
                //Get listener
                CoreUtils.getType(
                        CoreUtils.ListenerProvider::class, listOf(parentFragment, activity))?.getListenerForType(IPositiveButtonListener::class)?.onPositiveButtonClicked(dialogueTag)
            }
        }

        viewBinding.root.findViewById<TextView>(R.id.dlg_negative_button)?.let {
            it.text = args.getCharSequence(ARG_NEGATIVE)
            it.setOnClickListener {
                dismiss()
                //Get listener
                CoreUtils.getType(
                        CoreUtils.ListenerProvider::class, listOf(parentFragment, activity))?.getListenerForType(INegativeButtonListener::class)?.onNegativeButtonClicked(dialogueTag)
            }
        }

        //Create dialogue
        return MaterialDialog.Builder(context)
                .customView(viewBinding.root, false)
                .canceledOnTouchOutside(isCancelable)
                .cancelable(isCancelable)
                .build()
    }
}

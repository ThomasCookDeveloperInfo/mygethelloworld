package com.condecosoftware.core.dialogue

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.condecosoftware.connect.R
import com.condecosoftware.core.utils.CoreUtils

/**
 * Dialogue fragment for showing an alert with a title, message and up to two buttons.
 */

private const val ARG_LAYOUT_ID = "arg_layout_id"
private const val ARG_MESSAGE = "arg_message"
private const val ARG_TITLE = "arg_title"
private const val ARG_POSITIVE = "arg_positive"
private const val ARG_NEGATIVE = "arg_negative"
private const val ARG_CALLER_ARGS = "arg_caller_args"

class FragmentDialogue : FragmentDialogueBase() {

    companion object {
        fun newInstance(layoutId: Int,
                        title: CharSequence,
                        message: CharSequence,
                        positiveButton: CharSequence,
                        negativeButton: CharSequence, callerArgs: Bundle? = null): FragmentDialogue {
            val args = Bundle()
            args.putInt(ARG_LAYOUT_ID, layoutId)
            args.putCharSequence(ARG_MESSAGE, message)
            args.putCharSequence(ARG_TITLE, title)
            args.putCharSequence(ARG_POSITIVE, positiveButton)
            args.putCharSequence(ARG_NEGATIVE, negativeButton)
            args.putBundle(ARG_CALLER_ARGS, callerArgs)
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

        viewBinding.root.findViewById<TextView>(R.id.core_dlg_title)?.let {
            args.getCharSequence(ARG_TITLE)?.also { text ->
                if (text.isNotEmpty())
                    it.text = text
                else it.visibility = View.GONE
            }
        }

        viewBinding.root.findViewById<TextView>(R.id.core_dlg_message)?.let {
            it.text = args.getCharSequence(ARG_MESSAGE)
        }

        val dialogueTag = dialogueTag
        viewBinding.root.findViewById<TextView>(R.id.core_dlg_positive_button)?.let {
            val text = args.getCharSequence(ARG_POSITIVE)
            it.text = text
            if (text.isNotEmpty()) {
                it.visibility = View.VISIBLE
                it.setOnClickListener {
                    dismiss()
                    //Get listener
                    CoreUtils.getType(
                            CoreUtils.ListenerProvider::class, listOf(parentFragment, activity))
                            ?.getListenerForType(IPositiveButtonListener::class)
                            ?.onPositiveButtonClicked(dialogueTag, args.getBundle(ARG_CALLER_ARGS))
                }
            } else {
                it.visibility = View.GONE
            }
        }

        viewBinding.root.findViewById<TextView>(R.id.core_dlg_negative_button)?.let {
            val text = args.getCharSequence(ARG_NEGATIVE)
            it.text = text
            if (text.isNotEmpty()) {
                it.visibility = View.VISIBLE
                it.setOnClickListener {
                    dismiss()

                    //Get listener
                    CoreUtils.getType(
                            CoreUtils.ListenerProvider::class, listOf(parentFragment, activity))
                            ?.getListenerForType(INegativeButtonListener::class)
                            ?.onNegativeButtonClicked(dialogueTag, args.getBundle(ARG_CALLER_ARGS))
                }
            } else {
                it.visibility = View.GONE
            }
        }

        val dlg = AlertDialog.Builder(context)
                .setView(viewBinding.root)
                .setCancelable(isCancelable)
                .setOnDismissListener {
                    CoreUtils.getType(
                            CoreUtils.ListenerProvider::class, listOf(parentFragment, activity))
                            ?.getListenerForType(ICancelListener::class)
                            ?.onDialogueCancelled(dialogueTag, args.getBundle(ARG_CALLER_ARGS))
                }.create()
        dlg.setCanceledOnTouchOutside(isCancelable)
        return dlg
    }
}

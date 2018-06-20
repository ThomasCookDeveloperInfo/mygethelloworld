package com.condecosoftware.core.dialogue

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.text.Editable
import android.text.TextWatcher
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import com.condecosoftware.core.R
import com.condecosoftware.core.utils.CoreUtils

private const val ARG_LAYOUT_ID = "arg_layout_id"
private const val ARG_HINT = "arg_hint"
private const val ARG_TITLE = "arg_title"
private const val ARG_TEXT = "arg_text"
private const val ARG_POSITIVE = "arg_positive"
private const val ARG_CALLER_ARGS = "arg_caller_args"

interface IInputListener {
    //Called when a user provided some data
    fun onInputProvided(dialogueId: String, input: CharSequence, callerArgs: Bundle?)
}

class FragmentInputDialogue : FragmentDialogueBase() {

    companion object {
        fun newInstance(layoutId: Int,
                        title: CharSequence,
                        hint: CharSequence,
                        text: CharSequence,
                        positiveButton: CharSequence,
                        callerArgs: Bundle? = null): FragmentInputDialogue {
            val args = Bundle()
            args.putInt(ARG_LAYOUT_ID, layoutId)
            args.putCharSequence(ARG_TITLE, title)
            args.putCharSequence(ARG_HINT, hint)
            args.putCharSequence(ARG_TEXT, text)
            args.putCharSequence(ARG_POSITIVE, positiveButton)
            args.putBundle(ARG_CALLER_ARGS, callerArgs)

            val fragment = FragmentInputDialogue()
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
            }
        }

        val dialogueTag = dialogueTag
        val positiveButton = viewBinding.root.findViewById<TextView>(R.id.core_dlg_positive_button)
                ?: throw IllegalStateException("Layout has to contain a button with id core_dlg_positive_button")

        val editor = viewBinding.root.findViewById<EditText>(R.id.core_dlg_input)
                ?: throw IllegalStateException("Layout has to contain an editor with id core_dlg_input")

        positiveButton.text = args.getCharSequence(ARG_POSITIVE)
        positiveButton.setOnClickListener {
            hideKeyboard()

            dismiss()

            //Get listener
            CoreUtils.getType(
                    CoreUtils.ListenerProvider::class, listOf(parentFragment, activity))
                    ?.getListenerForType(IInputListener::class)?.also { listener ->
                        listener.onInputProvided(dialogueTag, editor.editableText ?: "", args.getBundle(ARG_CALLER_ARGS))
                    }
        }

        editor.hint = args.getCharSequence(ARG_HINT)
        args.getCharSequence(ARG_TEXT).also {
            if (it.isNotEmpty()) {
                editor.setText(it)
                editor.setSelection(it.length)
                positiveButton.isEnabled = true
            }
        }

        editor.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE && v.editableText.isNotEmpty()) {
                //Simulate clicking button
                positiveButton.performClick()
                true
            } else {
                false
            }
        }

        editor.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                positiveButton.isEnabled = s.toString().isNotBlank()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //Not used
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                //Not used
            }
        })
        editor.requestFocus()

        //Create dialogue
        val dialog = AlertDialog.Builder(context)
                .setView(viewBinding.root)
                .setCancelable(isCancelable)
                .create()
        dialog.setCanceledOnTouchOutside(false)

        return dialog
    }

    override fun onCancel(dialog: DialogInterface?) {
        if (isCancelable) {
            CoreUtils.getType(
                    CoreUtils.ListenerProvider::class, listOf(parentFragment, activity))
                    ?.getListenerForType(ICancelListener::class)
                    ?.onDialogueCancelled(dialogueTag, arguments?.getBundle(ARG_CALLER_ARGS))
        }
    }

    override fun onStart() {
        super.onStart()
        (context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.also {
            it.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
        }
    }

    private fun hideKeyboard() {
        (context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.also {
            it.hideSoftInputFromWindow(dialog.window.currentFocus.windowToken, 0)
        }
    }
}
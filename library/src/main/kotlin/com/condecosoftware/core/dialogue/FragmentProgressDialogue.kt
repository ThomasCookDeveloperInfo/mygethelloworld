package com.condecosoftware.core.dialogue

import android.app.Dialog
import android.os.Bundle
import android.view.ContextThemeWrapper
import com.afollestad.materialdialogs.MaterialDialog


private const val ARG_TITLE = "arg_title"
private const val ARG_MESSAGE = "arg_message"

@Suppress("unused")
/**
 * Fragment for displaying progress dialogue
 */
class FragmentProgressDialogue : FragmentDialogueBase() {
    companion object {
        fun newInstance(title: CharSequence, message: CharSequence): FragmentProgressDialogue {
            val args = Bundle()
            args.putCharSequence(ARG_MESSAGE, message)
            args.putCharSequence(ARG_TITLE, title)

            val fragmentProgressDialogue = FragmentProgressDialogue()
            fragmentProgressDialogue.arguments = args
            return fragmentProgressDialogue
        }
    }

    var dialogue: MaterialDialog? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val args = arguments
                ?: throw IllegalStateException("Create instance using newInstance method")

        val baseContext = activity ?: context
        ?: throw IllegalStateException("Fragment not attached.")
        val theme = theme
        val context = if (theme > 0) ContextThemeWrapper(baseContext, theme) else baseContext

        val title = args.getCharSequence(ARG_TITLE, "")
        val message = args.getCharSequence(ARG_MESSAGE, "")

        val builder = MaterialDialog.Builder(context)

        if (title.isNotBlank())
            builder.title(title)

        builder.content(message)
                .canceledOnTouchOutside(isCancelable)
                .cancelable(isCancelable)
                .progress(true, 0)

        val dialogue = builder.build()
        this.dialogue = dialogue
        return dialogue
    }

    override fun onDestroy() {
        dialogue = null
        super.onDestroy()
    }

    /**
     * Call the function to update dialogue's message
     */
    fun updateMessage(message: CharSequence) {

        //Update stores message in the arguments bundle
        val args = arguments
                ?: throw IllegalStateException("Create instance using newInstance method")
        args.putCharSequence(ARG_MESSAGE, message)

        //Update the view if we have it
        val dialogue = this.dialogue ?: return
        dialogue.setContent(message)
    }
}
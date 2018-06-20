package com.condecosoftware.core.dialogue

import android.app.Dialog
import android.os.Bundle
import android.view.ContextThemeWrapper
import com.afollestad.materialdialogs.MaterialDialog
import com.condecosoftware.core.utils.CoreUtils


private const val ARG_TITLE = "arg_title"
private const val ARG_ITEMS = "arg_items"
private const val ARG_ITEMS_SELECTIONS = "arg_items_selection"
private const val ARG_BUTTON_TEXT = "arg_button_text"
private const val ARG_CALLER_BUNDLE = "arg_caller_bundle"

/**
 * Dialogue fragment for showing a multiple choice list of items
 */
class FragmentMultiChoiceDialogue : FragmentDialogueBase() {

    interface IListener {
        //Called every time a selection is made
        fun onMultiChoiceSelection(dialogueTag: String,
                                   which: Array<Int>,
                                   text: Array<CharSequence>,
                                   callerArgs: Bundle?): Boolean

        //Called once the button has been pressed
        fun onMultiChoiceSelected(dialogueTag: String,
                                  which: Array<Int>,
                                  listItems: List<CharSequence>,
                                  callerArgs: Bundle?)
    }

    companion object {

        fun newInstance(title: CharSequence,
                        listItems: Collection<CharSequence>,
                        selections: Array<Int>,
                        buttonText: CharSequence,
                        callerArgs: Bundle? = null): FragmentMultiChoiceDialogue {

            val args = Bundle()
            args.putCharSequence(ARG_TITLE, title)
            if (listItems is ArrayList) {
                args.putCharSequenceArrayList(ARG_ITEMS, listItems)
            } else {
                args.putCharSequenceArrayList(ARG_ITEMS, ArrayList(listItems))
            }

            args.putCharSequence(ARG_BUTTON_TEXT, buttonText)
            args.putSerializable(ARG_ITEMS_SELECTIONS, selections)
            args.putParcelable(ARG_CALLER_BUNDLE, callerArgs)

            val fragment = FragmentMultiChoiceDialogue()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val args = arguments

        if (args === null)
            throw IllegalStateException("Create instance using newInstance method")

        val baseContext = activity ?: context
        ?: throw IllegalStateException("Fragment not attached.")

        val context = if (theme > 0) ContextThemeWrapper(baseContext, theme) else baseContext

        val builder = MaterialDialog.Builder(context)
        builder.title(args.getCharSequence(ARG_TITLE, ""))
        val items = args.getCharSequenceArrayList(ARG_ITEMS)

        val callerArgs: Bundle = args.getParcelable(ARG_CALLER_BUNDLE)
        val dialogueTag = dialogueTag

        if (items.isNotEmpty()) {
            builder.items(items)

            @Suppress("UNCHECKED_CAST")
            val selections = args.getSerializable(ARG_ITEMS_SELECTIONS) as? Array<Int>
                    ?: emptyArray()
            builder.itemsCallbackMultiChoice(selections, { _, which, text ->
                CoreUtils.getType(CoreUtils.ListenerProvider::class, arrayListOf(parentFragment, activity))
                        ?.getListenerForType(IListener::class)
                        ?.onMultiChoiceSelection(dialogueTag, which, text, callerArgs) == true

            })
        }

        //Set positive button text
        builder.positiveText(args.getString(ARG_BUTTON_TEXT, ""))
        builder.onPositive { materialDialog, _ ->
            CoreUtils.getType(CoreUtils.ListenerProvider::class, arrayListOf(parentFragment, activity))
                    ?.getListenerForType(IListener::class)?.also { listener ->
                        val selection = materialDialog.selectedIndices ?: return@onPositive

                        listener.onMultiChoiceSelected(dialogueTag, selection,
                                (materialDialog.items ?: emptyList()), callerArgs)
                    }
        }

        return builder.build()
    }
}
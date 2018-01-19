package com.condecosoftware.core.dialogue

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.AttributeSet
import android.view.ContextThemeWrapper
import android.widget.NumberPicker
import android.widget.TimePicker
import com.condecosoftware.core.utils.CoreUtils
import java.lang.reflect.Field
import java.util.*

private const val MODE_SPINNER = 1

/**
 * Use to show a time picker as dialogue fragment
 */
private class CustomTimePickerDialogue(
        context: Context, callBack: OnTimeSetListener?,
        hourOfDay: Int, minute: Int,
        is24HourView: Boolean, private val interval: Int = 0)

    : TimePickerDialog(
        context,
        OnTimeSetListener { timePicker, hour1, minute1 ->
            if (callBack !== null) {
                timePicker.clearFocus()
                callBack.onTimeSet(timePicker, hour1, if (interval == 0) minute1 else minute1 * interval)
            }
        },
        hourOfDay,
        if (interval == 0) minute else minute / interval,
        is24HourView) {

    init {
        fixSpinner(context, hourOfDay, if (interval == 0) minute else minute / interval, is24HourView)
    }

    @SuppressLint("PrivateApi")
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        val interval = this.interval
        if (interval == 0)
            return

        try {
            val classForId = Class.forName("com.android.internal.R\$id")
            val timePickerField = classForId.getField("timePicker")
            val timePicker: TimePicker = findViewById(timePickerField.getInt(null))
            val field = classForId.getField("minute")

            val mMinuteSpinner = timePicker.findViewById<NumberPicker>(field.getInt(null))
            mMinuteSpinner.minValue = 0
            mMinuteSpinner.maxValue = 60 / interval - 1
            val displayedValues = mutableListOf<String>()
            var i = 0
            while (i < 60) {
                displayedValues.add(String.format(Locale.getDefault(), "%02d", i))
                i += interval
            }

            mMinuteSpinner.displayedValues = displayedValues.toTypedArray()

        } catch (e: Exception) {
        }
    }


    @SuppressLint("PrivateApi", "InlinedApi")
    private fun fixSpinner(context: Context, hourOfDay: Int, minute: Int, is24HourView: Boolean) {
        // android:timePickerMode spinner and clock began in Lollipop
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                // Get the theme's android:timePickerMode
                val styleableClass = Class.forName("com.android.internal.R\$styleable")
                val timePickerStyleableField = styleableClass.getField("TimePicker")
                val timePickerStyleable = timePickerStyleableField.get(null) as IntArray

                val a = context.obtainStyledAttributes(null, timePickerStyleable, android.R.attr.timePickerStyle, 0)
                val timePickerModeStyleableField = styleableClass.getField("TimePicker_timePickerMode")
                val timePickerModeStyleable = timePickerModeStyleableField.getInt(null)
                val mode = a.getInt(timePickerModeStyleable, MODE_SPINNER)
                a.recycle()
                if (mode == MODE_SPINNER) {
                    val timePicker = findField(TimePickerDialog::class.java, TimePicker::class.java, "mTimePicker")?.get(this) as TimePicker
                    val delegateClass = Class.forName("android.widget.TimePicker\$TimePickerDelegate")
                    val delegateField = findField(TimePicker::class.java, delegateClass, "mDelegate")
                            ?: return

                    var delegate = delegateField.get(timePicker)
                    val spinnerDelegateClass: Class<*> = if (Build.VERSION.SDK_INT != Build.VERSION_CODES.LOLLIPOP) {
                        Class.forName("android.widget.TimePickerSpinnerDelegate")
                    } else {
                        // TimePickerSpinnerDelegate was initially misnamed TimePickerClockDelegate in API 21!
                        Class.forName("android.widget.TimePickerClockDelegate")
                    }
                    // In 7.0 Nougat for some reason the timePickerMode is ignored and the delegate is TimePickerClockDelegate
                    if (delegate.javaClass != spinnerDelegateClass) {
                        delegateField.set(timePicker, null) // throw out the TimePickerClockDelegate!
                        timePicker.removeAllViews() // remove the TimePickerClockDelegate views
                        val spinnerDelegateConstructor = spinnerDelegateClass.getConstructor(
                                TimePicker::class.java, Context::class.java,
                                AttributeSet::class.java,
                                Int::class.javaPrimitiveType,
                                Int::class.javaPrimitiveType)
                        spinnerDelegateConstructor.isAccessible = true
                        // Instantiate a TimePickerSpinnerDelegate
                        delegate = spinnerDelegateConstructor.newInstance(timePicker, context, null, android.R.attr.timePickerStyle, 0)
                        delegateField.set(timePicker, delegate) // set the TimePicker.mDelegate to the spinner delegate
                        // Set up the TimePicker again, with the TimePickerSpinnerDelegate
                        timePicker.setIs24HourView(is24HourView)
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                            @Suppress("DEPRECATION")
                            timePicker.currentHour = hourOfDay
                            @Suppress("DEPRECATION")
                            timePicker.currentMinute = minute
                        } else {
                            timePicker.hour = hourOfDay
                            timePicker.minute = minute
                        }

                        timePicker.setOnTimeChangedListener(this)

                    }
                }
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }
    }
}

private fun findField(objectClass: Class<*>, fieldClass: Class<*>, expectedName: String): Field? {
    try {
        val field = objectClass.getDeclaredField(expectedName)
        field.isAccessible = true
        return field
    } catch (ignore: NoSuchFieldException) {
    }
    // ignore
    // search for it if it wasn't found under the expected ivar name
    for (searchField in objectClass.declaredFields) {
        if (searchField.type === fieldClass) {
            searchField.isAccessible = true
            return searchField
        }
    }
    return null
}

private const val ARG_TITLE = "arg_title"
private const val ARG_HOUR = "arg_hour"
private const val ARG_MIN = "arg_min"
private const val ARG_IS_24 = "arg_is_24"
private const val ARG_CALLER_BUNDLE = "arg_caller_bundle"
private const val ARG_MINS_INTERVAL = "arg_mins_interval"

class FragmentTimeDialogue : FragmentDialogueBase() {

    interface IListener {
        fun onFragmentTimePickerTimeSet(dialogueId: String, hour: Int, min: Int, callerBundle: Bundle?)
    }

    companion object {
        /**
         * Used to create a new instance of the dialogue fragment
         */
        fun newInstance(
                title: CharSequence,
                hour: Int,
                min: Int,
                is24: Boolean,
                minsInterval: Int = 0,
                callerBundle: Bundle? = null): FragmentTimeDialogue {

            val args = Bundle()
            args.putCharSequence(ARG_TITLE, title)
            args.putInt(ARG_HOUR, hour)
            args.putInt(ARG_MIN, min)
            args.putBoolean(ARG_IS_24, is24)
            args.putParcelable(ARG_CALLER_BUNDLE, callerBundle)

            args.putInt(ARG_MINS_INTERVAL,
                    if (minsInterval < 0 || minsInterval > 59)
                        0 else minsInterval)

            val dialogFragment = FragmentTimeDialogue()
            dialogFragment.arguments = args
            return dialogFragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val args = arguments
                ?: throw IllegalStateException("Create instance using newInstance method")

        val baseContext = activity ?: context
        ?: throw IllegalStateException("Fragment not attached.")

        val theme = theme
        val localContext = if (theme > 0) ContextThemeWrapper(baseContext, theme) else baseContext

        val title = args.getCharSequence(ARG_TITLE, "")
        val hour = args.getInt(ARG_HOUR, 0)
        val min = args.getInt(ARG_MIN, 0)
        val is24 = args.getBoolean(ARG_IS_24)
        val minsInterval = args.getInt(ARG_MINS_INTERVAL)
        val timeDialogue = CustomTimePickerDialogue(localContext, OnTimeSetListener { _, hourVal, minVal ->
            dismiss()
            //Get listener
            val provider = CoreUtils.getType(
                    CoreUtils.ListenerProvider::class, listOf(this.parentFragment, this.activity))
                    ?: return@OnTimeSetListener
            //Call a listener if we have one
            provider.getListenerForType(IListener::class)
                    ?.onFragmentTimePickerTimeSet(dialogueTag, hourVal, minVal, args.getParcelable(ARG_CALLER_BUNDLE))

        }, hour, min, is24, minsInterval)
        timeDialogue.setTitle(title)

        return timeDialogue
    }
}
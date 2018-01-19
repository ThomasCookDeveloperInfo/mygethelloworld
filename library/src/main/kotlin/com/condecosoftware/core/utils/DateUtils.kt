package com.condecosoftware.core.utils

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Converts a standard Java Date to dd/MM/yyyy HH:mm:ss Z format. Example: 31/12/2009 16:00:00 -0700
 */
fun Date.toGMTFormat(timeZone: TimeZone): String =
        SimpleDateFormat("dd/MM/yyyy HH:mm:ss Z", Locale.US).also {
            it.timeZone = timeZone
        }.format(this.time)

object DateUtils {

    /**
     * Check if two calendars are set to the same day (compares day of the year and the year)
     */
    fun areDaysSame(day1: Calendar, day2: Calendar) =
            day1.get(Calendar.DAY_OF_YEAR) == day2.get(Calendar.DAY_OF_YEAR) &&
                    day1.get(Calendar.YEAR) == day2.get(Calendar.YEAR)

    /**
     * Returns the difference between 2 times in minutes
     */
    fun differenceInMinutes(from: Date, to: Date): Long {
        val then = TimeUnit.MILLISECONDS.toMinutes(from.time)
        val now = TimeUnit.MILLISECONDS.toMinutes(to.time)
        return now - then
    }
}
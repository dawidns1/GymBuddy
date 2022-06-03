package com.example.gymbuddy.helpers

import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.text.DateFormat
import java.util.*

class LineChartXAxisValueFormatter(private val refTime: Long) : IndexAxisValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        val emissionsMilliSince1970Time = value.toLong() + refTime

        val timeMilliseconds = Date(emissionsMilliSince1970Time)
        val dateTimeFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault())
        return dateTimeFormat.format(timeMilliseconds)
    }
}
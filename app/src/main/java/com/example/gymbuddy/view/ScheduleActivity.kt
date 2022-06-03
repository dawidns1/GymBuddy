package com.example.gymbuddy.view

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentValues
import android.content.DialogInterface
import android.icu.util.Calendar
import android.icu.util.TimeZone
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.CalendarContract
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.gymbuddy.R
import com.example.gymbuddy.databinding.ActivityScheduleBinding
import com.example.gymbuddy.helpers.Helpers
import com.example.gymbuddy.helpers.Utils
import java.util.*
import kotlin.collections.ArrayList

class ScheduleActivity : AppCompatActivity(), CompoundButton.OnCheckedChangeListener {
    private val checkBoxes = ArrayList<CheckBox>()
    private val startingTime = ArrayList<TextView>()
    private val duration = ArrayList<TextView>()
    private var selectionDone = false
    private var weeks = 0
    private var reminderMins = 0
    private val startingHrs = intArrayOf(0, 0, 0, 0, 0, 0, 0)
    private val startingMins = intArrayOf(0, 0, 0, 0, 0, 0, 0)
    private val durationMillis = longArrayOf(0, 0, 0, 0, 0, 0, 0, 0)
    private var checkedNumber = 0
    private var calendarId = ArrayList<String>()
    private var calendarNames = ArrayList<String>()
    private lateinit var binding: ActivityScheduleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_GymBuddy)
        super.onCreate(savedInstanceState)
        binding = ActivityScheduleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Helpers.setupActionBar(getString(R.string.schedulingWorkouts), "", supportActionBar, this)
        initViews()

        val calendarNames=handleCalendarSelection()
        val reminderArrayAdapter = ArrayAdapter(this, R.layout.dropdown_item, resources.getStringArray(R.array.scheduleReminder))
        binding.edtReminder.setAdapter(reminderArrayAdapter)
        val scheduleArrayAdapter = ArrayAdapter(this, R.layout.dropdown_item, resources.getStringArray(R.array.scheduleDuration))
        binding.edtSchedule.setAdapter(scheduleArrayAdapter)
        val calendarArrayAdapter=ArrayAdapter(this,R.layout.dropdown_item,calendarNames)
        binding.edtCalendar.setAdapter(calendarArrayAdapter)

        binding.edtCalendar.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(s: CharSequence, p1: Int, p2: Int, p3: Int) {
            if(s.toString() == resources.getString(R.string.def)) { Utils.getInstance(this@ScheduleActivity).calendarID = ""}
                else{
                    for(i in calendarNames.indices){
                        if(calendarNames[i]==s.toString())Utils.getInstance(this@ScheduleActivity).calendarID = calendarId[i - 1]
                    }
                }
            }
            override fun afterTextChanged(s: Editable) {}

        })
//        AlertDialog.Builder(this, R.style.DefaultAlertDialogTheme)
//            .setTitle(R.string.selectCalendar)
//            .setIcon(R.drawable.ic_calendar)
//            .setSingleChoiceItems(
//                accountNames,
//                checkedItem
//            ) { _: DialogInterface?, which: Int -> selected = which }
//            .setPositiveButton(R.string.ok) { _: DialogInterface?, _: Int ->
//                if (selected == 0) {
//                    Utils.getInstance(this@ScheduleActivity).calendarID = ""
//                    binding.txtCalendar.text = resources.getString(R.string.def)
//                } else {
//                    Utils.getInstance(this@ScheduleActivity).calendarID = calendarId[selected - 1]
//                    binding.txtCalendar.text = calendarName[selected - 1]
//                }
//            }
//            .setNegativeButton(R.string.cancel, null)
//            .show()

        binding.edtSchedule.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(s: CharSequence, p1: Int, p2: Int, p3: Int) {
                weeks = when (s.toString()) {
                    resources.getString(R.string.w2) -> 2
                    resources.getString(R.string.w4) -> 4
                    resources.getString(R.string.w6) -> 6
                    resources.getString(R.string.w8) -> 8
                    resources.getString(R.string.w10) -> 10
                    resources.getString(R.string.w12) -> 12
                    else -> 1
                }
                Toast.makeText(this@ScheduleActivity, "$weeks", Toast.LENGTH_SHORT).show()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.edtReminder.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                reminderMins = when (s.toString()) {
                    resources.getString(R.string.m15) -> 15
                    resources.getString(R.string.m30) -> 30
                    resources.getString(R.string.h1) -> 60
                    resources.getString(R.string.h1m30) -> 90
                    resources.getString(R.string.h2) -> 120
                    else -> 0
                }
                Toast.makeText(this@ScheduleActivity, "$reminderMins", Toast.LENGTH_SHORT).show()
            }

            override fun afterTextChanged(s: Editable) {}
        })

        binding.btnSchedule.setOnClickListener {
            val title: String = if (binding.edtTitle.text.toString().isEmpty()) {
                resources.getString(R.string.workout)
            } else {
                binding.edtTitle.text.toString()
            }
            val message: String = if (binding.edtDescription.text.toString().isEmpty()) {
                ""
            } else {
                binding.edtDescription.text.toString()
            }
            for (k in 0 until weeks) {
                for (i in checkBoxes.indices) {
                    if (checkBoxes[i].isChecked) {
                        val now = Calendar.getInstance(TimeZone.getDefault())
                        val day = now[Calendar.DAY_OF_WEEK]
                        for (j in 0..6) {
                            if ((day - 1 + j) % 7 == i) {
                                handleAddCalendarEntry(j, i, title, message, k)
                            }
                        }
                    }
                }
            }
            Toast.makeText(this@ScheduleActivity, resources.getString(R.string.eventsAdded), Toast.LENGTH_SHORT).show()
            finish()
        }
//        binding.txtCalendar.setOnClickListener { handleCalendarSelection() }
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        for (i in checkBoxes.indices) {
            if (checkBoxes[i].id == buttonView.id) {
                if (isChecked) {
                    selectionDone = false
                    handleTimeSelection(i, startingTime, resources.getString(R.string.startMsg), 12, 0, 23)
                } else {
                    startingTime[i].visibility = View.GONE
                    duration[i].visibility = View.GONE
                    checkedNumber--
                    if (checkedNumber == 0) {
                        binding.scheduleViews.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun handleAddCalendarEntry(offset: Int, position: Int, title: String, description: String, weekMultiplier: Int) {
        var currentOffset = offset
        val startMillis: Long
        val now = Calendar.getInstance(TimeZone.getDefault())
        val beginTime = Calendar.getInstance()
        val year = now[Calendar.YEAR]
        val month = now[Calendar.MONTH]
        val day = now[Calendar.DAY_OF_MONTH]
        val hour = startingHrs[position]
        val minute = startingMins[position]
        if (currentOffset == 0 && startingHrs[position] < now[Calendar.HOUR_OF_DAY]) {
            currentOffset = 7
        }
        currentOffset += weekMultiplier * 7
        beginTime[year, month, day, hour] = minute
        startMillis = beginTime.timeInMillis
        val cr = contentResolver
        val values = ContentValues()
        values.apply {
            put(CalendarContract.Events.DTSTART, startMillis + daysToMillis(currentOffset))
            put(
                CalendarContract.Events.DTEND,
                startMillis + daysToMillis(currentOffset) + durationMillis[position]

            )
            put(CalendarContract.Events.TITLE, title)
            put(CalendarContract.Events.DESCRIPTION, description)
            put(
                CalendarContract.Events.CALENDAR_ID,
                when (Utils.getInstance(this@ScheduleActivity).calendarID) {
                    "" -> 1
                    else -> Utils.getInstance(this@ScheduleActivity).calendarID.toInt()
                }
            )
            put(CalendarContract.Events.HAS_ALARM, 1)
            put(CalendarContract.Events.EVENT_COLOR, ContextCompat.getColor(this@ScheduleActivity, R.color.orange_500))
            put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
        }
        val uri = cr.insert(CalendarContract.Events.CONTENT_URI, values)
        if (reminderMins != 0) {
            val id = uri!!.lastPathSegment!!.toInt()
            val reminders = ContentValues().apply {
                put(CalendarContract.Reminders.EVENT_ID, id)
                put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT)
                put(CalendarContract.Reminders.MINUTES, reminderMins)
            }
            cr.insert(CalendarContract.Reminders.CONTENT_URI, reminders)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleTimeSelection(position: Int, views: ArrayList<TextView>, title: String, initialTime: Int, min: Int, max: Int) {
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.time_dialog, null)
        val d = AlertDialog.Builder(this@ScheduleActivity, R.style.DefaultAlertDialogTheme).apply {
            setTitle(title)
            setView(dialogView)
            setCancelable(false)
            setIcon(R.drawable.ic_timer)
        }
        val numberPickerHrs = dialogView.findViewById<NumberPicker>(R.id.hoursNumberPicker).apply {
            maxValue = max
            minValue = min
            wrapSelectorWheel = false
            value = initialTime
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            numberPickerHrs.textSize = 80f
        }
        try {
            val field = NumberPicker::class.java.getDeclaredField("mInputText")
            field.isAccessible = true
            val inputText = field[numberPickerHrs] as EditText
            inputText.visibility = View.INVISIBLE
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val numberPickerMin = dialogView.findViewById<NumberPicker>(R.id.minutesNumberPicker).apply {
            maxValue = 11
            minValue = 0
            wrapSelectorWheel = false
            setFormatter { i: Int -> String.format("%02d", i * 5) }
            value = 0
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            numberPickerMin.textSize = 80f
        }
        try {
            val field = NumberPicker::class.java.getDeclaredField("mInputText")
            field.isAccessible = true
            val inputText = field[numberPickerMin] as EditText
            inputText.visibility = View.INVISIBLE
        } catch (e: Exception) {
            e.printStackTrace()
        }
        d.setPositiveButton(R.string.ok) { _: DialogInterface?, _: Int ->
            try {
                duration[position].text = "1:00"
                duration[position].visibility = View.VISIBLE
                views[position].visibility = View.VISIBLE
                views[position].text = "${numberPickerHrs.value}:${
                    String.format(
                        "%02d",
                        numberPickerMin.value * 5
                    )
                }"
                if (!selectionDone) {
                    handleTimeSelection(position, duration, resources.getString(R.string.durationMsg), 1, 1, 4)
                    startingHrs[position] = numberPickerHrs.value
                    startingMins[position] = numberPickerMin.value * 5
                    selectionDone = true
                } else {
                    checkedNumber++
                    if (!binding.scheduleViews.isShown) {
                        binding.scheduleViews.visibility = View.VISIBLE
                    }
                    durationMillis[position] =
                        ((numberPickerHrs.value * 60 + numberPickerMin.value * 5) * 60 * 1000).toLong()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        d.setNegativeButton(R.string.cancel) { _: DialogInterface?, _: Int ->
            startingTime[position].visibility = View.GONE
            duration[position].visibility = View.GONE
            checkBoxes[position].isChecked = false
        }
        val alertDialog = d.create()
        alertDialog.show()
        alertDialog.setCanceledOnTouchOutside(false)
    }

    private fun daysToMillis(days: Int): Long {
        return (days * 86400000).toLong()
    }

    private fun initViews() {
        getGmailCalendarIds()
        if (Utils.getInstance(this).calendarID != "") {
            if (calendarId.isNotEmpty()) {
                for (i in calendarId.indices) {
                    if (calendarId[i] == Utils.getInstance(this).calendarID) {
                        binding.edtCalendar.setText(calendarNames[i])
                    }
                }
            }
        }
        checkBoxes.apply {
            add(binding.cbSun)
            add(binding.cbMon)
            add(binding.cbTue)
            add(binding.cbWed)
            add(binding.cbThu)
            add(binding.cbFri)
            add(binding.cbSat)
            forEach { c -> c.setOnCheckedChangeListener(this@ScheduleActivity) }
        }

        startingTime.apply {
            add(binding.startSun)
            add(binding.startMon)
            add(binding.startTue)
            add(binding.startWed)
            add(binding.startThu)
            add(binding.startFri)
            add(binding.startSat)
        }
        duration.apply {
            add(binding.durationSun)
            add(binding.durationMon)
            add(binding.durationTue)
            add(binding.durationWed)
            add(binding.durationThu)
            add(binding.durationFri)
            add(binding.durationSat)
        }

        Helpers.handleNativeAds(binding.scheduleAdTemplate, this, Helpers.AD_ID_SCHEDULE_NATIVE, null)
    }

    private fun getGmailCalendarIds() {
        calendarId.clear()
        calendarNames.clear()
        val projection = arrayOf("_id", "calendar_displayName")
        val calendars: Uri = Uri.parse("content://com.android.calendar/calendars")
        val managedCursor = contentResolver.query(
            calendars,
            projection, null, null, null
        )
        managedCursor?.let { cursor ->
            if (cursor.moveToFirst()) {
                var calName: String
                var calID: String
                val nameCol = cursor.getColumnIndex(projection[1])
                val idCol = cursor.getColumnIndex(projection[0])
                do {
                    calName = cursor.getString(nameCol)
                    calID = cursor.getString(idCol)
                    if (calName.contains("@gmail")) {
                        calendarId.add(calID)
                        calendarNames.add(calName)
                    }
                } while (cursor.moveToNext())
            }
        }
        managedCursor?.close()
    }

    private fun handleCalendarSelection(): ArrayList<String> {
        getGmailCalendarIds()
        val accountNames = arrayListOf<String>()
        var checkedItem = 0
        accountNames.add(resources.getString(R.string.def))
        if (calendarNames.isNotEmpty()) accountNames.addAll(calendarNames)
        calendarNames.forEach { calendarName ->
            if(calendarName==Utils.getInstance(this@ScheduleActivity).calendarID)
                binding.edtCalendar.setText(calendarName)
        }
        return accountNames
    }
}
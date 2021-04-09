package com.example.gymbuddy;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.icu.util.Calendar;
import android.icu.util.TimeZone;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.lang.reflect.Field;
import java.util.ArrayList;

import static android.view.View.GONE;

public class ScheduleActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    private final ArrayList<CheckBox> checkBoxes = new ArrayList<>();
    private final ArrayList<TextView> startingTime = new ArrayList<>();
    private final ArrayList<TextView> duration = new ArrayList<>();
    private boolean selectionDone = false;
    private Button btnSchedule;
    private EditText edtTitle, edtDescription;
    private Spinner edtSchedule, edtReminder;
    private int weeks;
    private int reminderMins;
    private final int[] startingHrs = {0, 0, 0, 0, 0, 0, 0};
    private final int[] startingMins = {0, 0, 0, 0, 0, 0, 0};
    private final long[] durationMillis = {0, 0, 0, 0, 0, 0, 0, 0};
    private int checkedNumber = 0;
    private ConstraintLayout scheduleViews;
    private TextView txtCalendar;
    private ArrayList<String> calendarId, calendarName;
    private int selected = 0;
//    private AdView scheduleAd;
    private FrameLayout scheduleAdContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_GymBuddy);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        setupActionBar(getString(R.string.schedulingWorkouts), "");

        initViews();

        edtSchedule.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        weeks = 1;
                        break;
                    case 1:
                        weeks = 2;
                        break;
                    case 2:
                        weeks = 4;
                        break;
                    case 3:
                        weeks = 6;
                        break;
                    case 4:
                        weeks = 8;
                        break;
                    case 5:
                        weeks = 10;
                        break;
                    case 6:
                        weeks = 12;
                        break;
                    default:
                        weeks = 1;
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        edtReminder.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        reminderMins = 0;
                        break;
                    case 1:
                        reminderMins = 15;
                        break;
                    case 2:
                        reminderMins = 30;
                        break;
                    case 3:
                        reminderMins = 60;
                        break;
                    case 4:
                        reminderMins = 90;
                        break;
                    case 5:
                        reminderMins = 120;
                        break;
                    default:
                        reminderMins = 0;
                        break;
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btnSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title;
                String message;
                if (edtTitle.getText().toString().isEmpty()) {
                    title = getResources().getString(R.string.workout);
                } else {
                    title = edtTitle.getText().toString();
                }
                if (edtDescription.getText().toString().isEmpty()) {
                    message = "";
                } else {
                    message = edtDescription.getText().toString();
                }
                for (int k = 0; k < weeks; k++) {
                    for (int i = 0; i < checkBoxes.size(); i++) {
                        if (checkBoxes.get(i).isChecked()) {
                            Calendar now = Calendar.getInstance(TimeZone.getDefault());
                            int day = now.get(Calendar.DAY_OF_WEEK);
                            for (int j = 0; j < 7; j++) {
                                if ((day - 1 + j) % 7 == i) {
                                    handleAddCalendarEntry(j, i, title, message, k);
                                }
                            }
                        }
                    }
                }
                Toast.makeText(ScheduleActivity.this, getResources().getString(R.string.eventsAdded), Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        txtCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleCalendarSelection();
            }
        });
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        for (int i = 0; i < checkBoxes.size(); i++) {
            if (checkBoxes.get(i).getId() == buttonView.getId()) {
                if (isChecked) {
                    selectionDone = false;
                    handleTimeSelection(i, startingTime, getResources().getString(R.string.startMsg), 12, 0, 23);

                } else {
                    startingTime.get(i).setVisibility(GONE);
                    duration.get(i).setVisibility(GONE);
                    checkedNumber--;
                    if (checkedNumber == 0) {
                        scheduleViews.setVisibility(GONE);
                    }
                }
            }

        }
    }

    private void handleAddCalendarEntry(int offset, int position, String title, String description, int weekMultiplier) {
        long startMillis = 0;
        Calendar now = Calendar.getInstance(TimeZone.getDefault());
        Calendar beginTime = Calendar.getInstance();
        int year = now.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH);
        int day = now.get(Calendar.DAY_OF_MONTH);
        int hour = startingHrs[position];
        int minute = startingMins[position];
        if (offset == 0 && startingHrs[position] < now.get(Calendar.HOUR_OF_DAY)) {
            offset = 7;
        }
        offset += weekMultiplier * 7;
        beginTime.set(year, month, day, hour, minute);
        startMillis = beginTime.getTimeInMillis();
        ContentResolver cr = getContentResolver();
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.DTSTART, startMillis + daysToMillis(offset));
        values.put(CalendarContract.Events.DTEND, startMillis + daysToMillis(offset) + durationMillis[position]);
        values.put(CalendarContract.Events.TITLE, title);
        values.put(CalendarContract.Events.DESCRIPTION, description);
        if (Utils.getInstance(this).getCalendarID().equals("")) {
            values.put(CalendarContract.Events.CALENDAR_ID, 1);
        } else {
            values.put(CalendarContract.Events.CALENDAR_ID, Utils.getInstance(this).getCalendarID());
        }
        values.put(CalendarContract.Events.HAS_ALARM, 1);
        values.put(CalendarContract.Events.EVENT_COLOR, getResources().getColor(R.color.orange_500));
        values.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());
        Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);


        if (reminderMins != 0) {
            int id = Integer.parseInt(uri.getLastPathSegment());
            ContentValues reminders = new ContentValues();
            reminders.put(CalendarContract.Reminders.EVENT_ID, id);
            reminders.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
            reminders.put(CalendarContract.Reminders.MINUTES, reminderMins);

            Uri uri2 = cr.insert(CalendarContract.Reminders.CONTENT_URI, reminders);
        }
    }

    private void handleTimeSelection(int position, ArrayList<TextView> views, String title, int initialTime, int min, int max) {

        final AlertDialog.Builder d = new AlertDialog.Builder(ScheduleActivity.this, R.style.DefaultAlertDialogTheme);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.time_dialog, null);
        d.setTitle(title);
        d.setView(dialogView);
        d.setCancelable(false);
        d.setIcon(R.drawable.ic_timer);
        final NumberPicker numberPickerHrs = (NumberPicker) dialogView.findViewById(R.id.hoursNumberPicker);
        numberPickerHrs.setMaxValue(max);
        numberPickerHrs.setMinValue(min);
        numberPickerHrs.setWrapSelectorWheel(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            numberPickerHrs.setTextSize(80);
        }
        numberPickerHrs.setValue(initialTime);
        try {
            Field field = NumberPicker.class.getDeclaredField("mInputText");
            field.setAccessible(true);
            EditText inputText = (EditText) field.get(numberPickerHrs);
            inputText.setVisibility(View.INVISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        final NumberPicker numberPickerMin = (NumberPicker) dialogView.findViewById(R.id.minutesNumberPicker);
        numberPickerMin.setMaxValue(11);
        numberPickerMin.setMinValue(0);
        numberPickerMin.setWrapSelectorWheel(false);
        numberPickerMin.setFormatter(new NumberPicker.Formatter() {
            @Override
            public String format(int i) {
                return String.format("%02d", i * 5);
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            numberPickerMin.setTextSize(80);
        }
        numberPickerMin.setValue(0);
        try {
            Field field = NumberPicker.class.getDeclaredField("mInputText");
            field.setAccessible(true);
            EditText inputText = (EditText) field.get(numberPickerMin);
            inputText.setVisibility(View.INVISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        d.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                try {
                    duration.get(position).setText("1:00");
                    duration.get(position).setVisibility(View.VISIBLE);
                    views.get(position).setVisibility(View.VISIBLE);
                    views.get(position).setText(numberPickerHrs.getValue() + ":" + String.format("%02d", (numberPickerMin.getValue() * 5)));
                    if (!selectionDone) {
                        handleTimeSelection(position, duration, getResources().getString(R.string.durationMsg), 1, 1, 4);
                        startingHrs[position] = numberPickerHrs.getValue();
                        startingMins[position] = (numberPickerMin.getValue() * 5);
                        selectionDone = true;

                    } else {
                        checkedNumber++;
                        if (!scheduleViews.isShown()) {
                            scheduleViews.setVisibility(View.VISIBLE);
                        }
                        durationMillis[position] = ((numberPickerHrs.getValue() * 60) + (numberPickerMin.getValue() * 5)) * 60 * 1000;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        d.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startingTime.get(position).setVisibility(GONE);
                duration.get(position).setVisibility(GONE);
                checkBoxes.get(position).setChecked(false);
            }
        });
        AlertDialog alertDialog = d.create();
        alertDialog.show();
        alertDialog.setCanceledOnTouchOutside(false);
    }

    private long daysToMillis(int days) {
        return days * 86400000;
    }

    public void setupActionBar(String text1, String text2) {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.orange_500)));
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);

        ActionBar.LayoutParams params = new ActionBar.LayoutParams(android.app.ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
        View customActionBar = LayoutInflater.from(this).inflate(R.layout.action_bar, null);
        actionBar.setCustomView(customActionBar, params);
        TextView abText1 = findViewById(R.id.abText1);
        TextView abText2 = findViewById(R.id.abText2);
        abText1.setText(text1);
        abText2.setText(text2);
    }

    private void initViews() {
        btnSchedule = findViewById(R.id.btnSchedule);
        edtTitle = findViewById(R.id.edtTitle);
        edtDescription = findViewById(R.id.edtDescription);
        edtSchedule = findViewById(R.id.edtSchedule);
        scheduleViews = findViewById(R.id.scheduleViews);
        edtReminder = findViewById(R.id.edtReminder);
        txtCalendar = findViewById(R.id.txtCalendar);
        getGmailCalendarIds(this);
        if (!Utils.getInstance(this).getCalendarID().equals("")) {
            if (!calendarId.isEmpty()) {
                for (int i = 0; i < calendarId.size(); i++) {
                    if (calendarId.get(i).equals(Utils.getInstance(this).getCalendarID())) {
                        txtCalendar.setText(calendarName.get(i));
                    }
                }
            }
        } else
            txtCalendar.setText(getResources().getString(R.string.def));

        checkBoxes.add(findViewById(R.id.cbSun));
        checkBoxes.add(findViewById(R.id.cbMon));
        checkBoxes.add(findViewById(R.id.cbTue));
        checkBoxes.add(findViewById(R.id.cbWed));
        checkBoxes.add(findViewById(R.id.cbThu));
        checkBoxes.add(findViewById(R.id.cbFri));
        checkBoxes.add(findViewById(R.id.cbSat));

        for (CheckBox c : checkBoxes) {
            c.setOnCheckedChangeListener(this);
        }
        startingTime.add(findViewById(R.id.startSun));
        startingTime.add(findViewById(R.id.startMon));
        startingTime.add(findViewById(R.id.startTue));
        startingTime.add(findViewById(R.id.startWed));
        startingTime.add(findViewById(R.id.startThu));
        startingTime.add(findViewById(R.id.startFri));
        startingTime.add(findViewById(R.id.startSat));

        duration.add(findViewById(R.id.durationSun));
        duration.add(findViewById(R.id.durationMon));
        duration.add(findViewById(R.id.durationTue));
        duration.add(findViewById(R.id.durationWed));
        duration.add(findViewById(R.id.durationThu));
        duration.add(findViewById(R.id.durationFri));
        duration.add(findViewById(R.id.durationSat));
        scheduleAdContainer=findViewById(R.id.scheduleAdContainer);
        Helpers.handleAds(scheduleAdContainer,this);
    }

    private void getGmailCalendarIds(Context c) {
        calendarId = new ArrayList<>();
        calendarName = new ArrayList<>();
        String[] projection = {"_id", "calendar_displayName"};
        Uri calendars;
        calendars = Uri.parse("content://com.android.calendar/calendars");
        ContentResolver contentResolver = c.getContentResolver();
        Cursor managedCursor = contentResolver.query(calendars,
                projection, null, null, null);
        if (managedCursor.moveToFirst()) {
            String calName;
            String calID;
            int nameCol = managedCursor.getColumnIndex(projection[1]);
            int idCol = managedCursor.getColumnIndex(projection[0]);
            do {
                calName = managedCursor.getString(nameCol);
                calID = managedCursor.getString(idCol);
                if (calName.contains("@gmail")) {
                    calendarId.add(calID);
                    calendarName.add(calName);
                }
            } while (managedCursor.moveToNext());
            managedCursor.close();
        }
    }

    private void handleCalendarSelection() {
        getGmailCalendarIds(this);
        String[] accountNames;
        int checkedItem = 0;
        if (calendarName.isEmpty()) {
            accountNames = new String[]{getResources().getString(R.string.def)};
        } else {
            accountNames = new String[calendarName.size() + 1];
            accountNames[0] = getResources().getString(R.string.def);
            for (int i = 0; i < calendarName.size(); i++) {
                accountNames[i + 1] = calendarName.get(i);
                if (calendarId.get(i).equals(Utils.getInstance(ScheduleActivity.this).getCalendarID())) {
                    checkedItem = i + 1;
                }
            }
        }


        new AlertDialog.Builder(this, R.style.DefaultAlertDialogTheme)
                .setTitle(R.string.selectCalendar)
                .setIcon(R.drawable.ic_calendar)
                .setSingleChoiceItems(accountNames, checkedItem, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selected = which;
                    }
                })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (selected == 0) {
                            Utils.getInstance(ScheduleActivity.this).setCalendarID("");
                            txtCalendar.setText(getResources().getString(R.string.def));
                        } else {
                            Utils.getInstance(ScheduleActivity.this).setCalendarID(calendarId.get(selected - 1));
                            txtCalendar.setText(calendarName.get(selected - 1));
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();

    }

}

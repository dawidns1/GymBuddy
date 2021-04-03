package com.example.gymbuddy;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ShareCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;

import static android.view.View.GONE;

public class MainActivity extends AppCompatActivity {

    private RecyclerView workoutsRV, workoutsExercisesRV;
    ArrayList<Workout> workouts = new ArrayList<>();
    ArrayList<Workout> returnList = new ArrayList<>();
    public final static String NEW_WORKOUT_KEY = "new workout key";
    private ConstraintLayout parent;
    private ExtendedFloatingActionButton btnAddWorkout;
    public WorkoutsRVAdapter adapter = new WorkoutsRVAdapter(this);
    private boolean areExercisesShown;
    private boolean loggingEnabled;
    private boolean doubleBackToExitPressedOnce = false;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    public static final int REQUEST_CALENDAR = 2;
    private static final String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final String[] PERMISSIONS_CALENDAR = {
            Manifest.permission.READ_CALENDAR,
            Manifest.permission.WRITE_CALENDAR};
    private MenuItem selectedMenuItem;
    private ExtendedFloatingActionButton btnSchedule;
    private ArrayList<String> calendarId, calendarName;
    private int selected;
    private AdView mainAd;


    public boolean verifyStoragePermissions(Activity activity) {
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            new AlertDialog.Builder(this, R.style.DefaultAlertDialogTheme)
                    .setTitle(R.string.permissions)
                    .setIcon(R.drawable.ic_info)
                    .setMessage(R.string.permissionInfo)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(
                                    activity,
                                    PERMISSIONS_STORAGE,
                                    REQUEST_EXTERNAL_STORAGE);
                        }
                    })
                    .show();
            return false;
        } else return true;
    }

    public boolean verifyCalendarPermissions(Activity activity) {
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_CALENDAR);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            new AlertDialog.Builder(this, R.style.DefaultAlertDialogTheme)
                    .setTitle(R.string.permissions)
                    .setIcon(R.drawable.ic_info)
                    .setMessage(R.string.permissionInfoCalendar)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(
                                    activity,
                                    PERMISSIONS_CALENDAR,
                                    REQUEST_CALENDAR);
                        }
                    })
                    .show();
            return false;
        } else return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            onOptionsItemSelected(selectedMenuItem);
        }
//        } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && requestCode == 2) {
//            loggingEnabled = true;
//            Utils.getInstance(MainActivity.this).setLoggingEnabled(true);
//        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                Workout newWorkout = (Workout) data.getSerializableExtra(NEW_WORKOUT_KEY);
                Utils.getInstance(this).addToAllWorkouts(newWorkout);
                adapter.setWorkouts(Utils.getInstance(this).getAllWorkouts());
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;

        Toast.makeText(this, R.string.pressBackToExit, Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.setWorkouts(Utils.getInstance(this).getAllWorkouts());
        adapter.notifyDataSetChanged();
        doubleBackToExitPressedOnce = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        selectedMenuItem = item;
        switch (item.getItemId()) {
            case R.id.submenuHideShow:
                areExercisesShown = !areExercisesShown;
                Utils.getInstance(this).setAreExercisesShown(areExercisesShown);
                finish();
                startActivity(getIntent());
                return true;
            case R.id.logInCalendar:
                if (verifyCalendarPermissions(MainActivity.this)) {
                    if (loggingEnabled) {
                        new AlertDialog.Builder(this, R.style.DefaultAlertDialogTheme)
                                .setTitle(R.string.disableLogging)
                                .setIcon(R.drawable.ic_calendar)
                                .setMessage(R.string.disableLoggingMsg)
                                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        loggingEnabled = false;
                                        Utils.getInstance(MainActivity.this).setLoggingEnabled(loggingEnabled);
                                    }
                                })
                                .setNegativeButton(R.string.no, null)
                                .show();
                    } else {
                        new AlertDialog.Builder(this, R.style.DefaultAlertDialogTheme)
                                .setTitle(R.string.enableLogging)
                                .setIcon(R.drawable.ic_calendar)
                                .setMessage(R.string.enableLoggingMsg)
                                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                            handleCalendarSelection();
                                    }
                                })
                                .setNegativeButton(R.string.no, null)
                                .show();
                    }
                }
                return true;
            case R.id.submenuShare:
                if (verifyStoragePermissions(this))
                    handleShare();
                return true;
            case R.id.importFromArchive:
                handleImportFromArchive();
                return true;
            case R.id.importFromFile:
                if (verifyStoragePermissions(this))
                    handleImportFromFile();
                return true;
            case R.id.exportToArchive:
                handleExportToArchive();
                return true;
            case R.id.exportToFile:
                if (verifyStoragePermissions(this))
                    handleExportToFile();
                return true;

            default:
//                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
                return super.onOptionsItemSelected(item);
        }
    }

    private void handleShare() {
        boolean[] areChecked = new boolean[workouts.size()];
        String[] workoutNames = new String[workouts.size()];
        for (int i = 0; i < workouts.size(); i++) {
            workoutNames[i] = workouts.get(i).getName();
            areChecked[i] = false;
        }
        new AlertDialog.Builder(this, R.style.DefaultAlertDialogTheme)
                .setTitle(R.string.selectWorkoutsToShare)
                .setIcon(R.drawable.ic_share)
                .setMultiChoiceItems(workoutNames, areChecked, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        areChecked[which] = isChecked;
                    }
                })
                .setPositiveButton(R.string.share, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ArrayList<Workout> exportedWorkouts = new ArrayList<>();
                        int removed = 0;
                        int size = workouts.size();
                        for (int i = 0; i < size; i++) {
                            if (areChecked[i]) {
                                exportedWorkouts.add(workouts.get(i - removed));
                            }
                        }
                        if (exportedWorkouts.isEmpty()) {
                            Toast.makeText(MainActivity.this, R.string.noWorkoutsSelected, Toast.LENGTH_SHORT).show();
                        } else {
                            File directory = new File(String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)));
                            if (!directory.exists()) {
                                directory.mkdirs();
                            }

                            String filename = "gbExport";
                            ObjectOutput out = null;

                            try {
                                out = new ObjectOutputStream(new FileOutputStream(directory
                                        + File.separator + filename));
                                out.writeObject(exportedWorkouts);
                                out.close();
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                                Toast.makeText(MainActivity.this, e + "", Toast.LENGTH_SHORT).show();
                            } catch (IOException e) {
                                e.printStackTrace();
                                Toast.makeText(MainActivity.this, e + "", Toast.LENGTH_SHORT).show();
                            }
                            ShareCompat.IntentBuilder shareIntent = ShareCompat.IntentBuilder.from(MainActivity.this).setType("*/*");
                            File directoryFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) +
                                    File.separator + filename);
                            Uri uri = FileProvider.getUriForFile(MainActivity.this, "com.example.gymbuddy.fileprovider", directoryFile);
                            shareIntent.addStream(uri);
                            Intent intent = shareIntent.getIntent();
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            MainActivity.this.startActivity(Intent.createChooser(intent, getString(R.string.shareWorkoutsVia)));
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void handleImportFromFile() {
        new AlertDialog.Builder(MainActivity.this, R.style.DefaultAlertDialogTheme)
                .setTitle(R.string.fileLocation)
                .setIcon(R.drawable.ic_info)
                .setMessage(R.string.fileLocationInfo)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final EditText exportName = new EditText(MainActivity.this);
                        exportName.getBackground().setColorFilter(Color.parseColor("#FF5722"), PorterDuff.Mode.SRC_IN);
                        exportName.setTextColor(Color.WHITE);
                        new AlertDialog.Builder(MainActivity.this, R.style.DefaultAlertDialogTheme)
                                .setTitle(R.string.fileName)
                                .setIcon(R.drawable.ic_load)
                                .setView(exportName)
                                .setPositiveButton(R.string.next, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        File directory = new File(String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)));
                                        String filename = exportName.getText().toString();
                                        ObjectInput in = null;

                                        try {
                                            in = new ObjectInputStream(new FileInputStream(directory
                                                    + File.separator + filename));
                                            returnList = (ArrayList<Workout>) in.readObject();
                                            in.close();
                                            if (returnList == null || returnList.isEmpty()) {
                                                Toast.makeText(MainActivity.this, R.string.noWorkoutsToLoad, Toast.LENGTH_SHORT).show();
                                            } else {
                                                boolean[] areChecked = new boolean[returnList.size()];
                                                String[] workoutNames = new String[returnList.size()];
                                                for (int i = 0; i < returnList.size(); i++) {
                                                    workoutNames[i] = returnList.get(i).getName();
                                                    areChecked[i] = false;
                                                }
                                                new AlertDialog.Builder(MainActivity.this, R.style.DefaultAlertDialogTheme)
                                                        .setTitle(R.string.selectWorkoutsToLoad)
                                                        .setIcon(R.drawable.ic_load)
                                                        .setMultiChoiceItems(workoutNames, areChecked, new DialogInterface.OnMultiChoiceClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                                                areChecked[which] = isChecked;
                                                            }
                                                        })
                                                        .setPositiveButton(R.string.load, new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                int removed = 0;
                                                                int size = returnList.size();
                                                                workouts = Utils.getInstance(MainActivity.this).getAllWorkouts();
                                                                for (int i = 0; i < size; i++) {
                                                                    if (areChecked[i]) {
                                                                        workouts.add(returnList.get(i - removed));
                                                                        Utils.getInstance(MainActivity.this).addToAllWorkouts(returnList.get(i - removed));
                                                                        returnList.remove(i - removed);
                                                                        removed++;
                                                                    }
                                                                }
                                                                adapter.setWorkouts(workouts);
                                                            }
                                                        })
                                                        .setNegativeButton(R.string.cancel, null)
                                                        .show();
                                            }
                                        } catch (FileNotFoundException e) {
                                            e.printStackTrace();
//                                            Toast.makeText(MainActivity.this, e + "", Toast.LENGTH_LONG).show();
                                            new AlertDialog.Builder(MainActivity.this, R.style.DefaultAlertDialogTheme)
                                                    .setTitle(R.string.fileNotFound)
                                                    .setIcon(R.drawable.ic_warning)
                                                    .setMessage(R.string.checkLocationName)
                                                    .setPositiveButton(R.string.ok, null)
                                                    .show();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                            Toast.makeText(MainActivity.this, "" + e, Toast.LENGTH_SHORT).show();
                                        } catch (ClassNotFoundException e) {
                                            e.printStackTrace();
                                            Toast.makeText(MainActivity.this, "" + e, Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                })
                                .setNegativeButton(R.string.cancel, null)
                                .show();
                    }
                })
                .show();

    }

    private void handleExportToFile() {
        boolean[] areChecked = new boolean[workouts.size()];
        String[] workoutNames = new String[workouts.size()];
        workouts = Utils.getInstance(MainActivity.this).getAllWorkouts();
        for (int i = 0; i < workouts.size(); i++) {
            workoutNames[i] = workouts.get(i).getName();
            areChecked[i] = false;
        }
        new AlertDialog.Builder(this, R.style.DefaultAlertDialogTheme)
                .setTitle(R.string.selectWorkoutsToSave)
                .setIcon(R.drawable.ic_save)
                .setMultiChoiceItems(workoutNames, areChecked, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        areChecked[which] = isChecked;
                    }
                })
                .setPositiveButton(R.string.next, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ArrayList<Workout> exportedWorkouts = new ArrayList<>();
                        int removed = 0;
                        int size = workouts.size();
                        for (int i = 0; i < size; i++) {
                            if (areChecked[i]) {
                                exportedWorkouts.add(workouts.get(i - removed));
                            }
                        }
                        if (exportedWorkouts.isEmpty()) {
                            Toast.makeText(MainActivity.this, R.string.noWorkoutsSelected, Toast.LENGTH_SHORT).show();
                        } else {
                            File directory = new File(String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)));
                            if (!directory.exists()) {
                                directory.mkdirs();
                            }

                            final EditText exportName = new EditText(MainActivity.this);
                            exportName.getBackground().setColorFilter(Color.parseColor("#FF5722"), PorterDuff.Mode.SRC_IN);
                            exportName.setTextColor(Color.WHITE);
                            new AlertDialog.Builder(MainActivity.this, R.style.DefaultAlertDialogTheme)
                                    .setTitle(R.string.fileName)
                                    .setIcon(R.drawable.ic_save)
                                    .setView(exportName)
                                    .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (exportName.getText().toString().isEmpty()) {
                                                Toast.makeText(MainActivity.this, R.string.insertFileName, Toast.LENGTH_SHORT).show();
                                            } else {
                                                String filename = exportName.getText().toString();
                                                ObjectOutput out = null;

                                                try {
                                                    out = new ObjectOutputStream(new FileOutputStream(directory
                                                            + File.separator + filename));
                                                    out.writeObject(exportedWorkouts);
                                                    out.close();
                                                    Toast.makeText(MainActivity.this, getResources().getString(R.string.workoutsSavedIn) + " " + directory, Toast.LENGTH_LONG).show();
                                                } catch (FileNotFoundException e) {
                                                    e.printStackTrace();
                                                    Toast.makeText(MainActivity.this, e + "", Toast.LENGTH_SHORT).show();
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                    Toast.makeText(MainActivity.this, e + "", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        }
                                    })
                                    .setNegativeButton(R.string.cancel, null)
                                    .show();
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void handleExportToArchive() {
        boolean[] areChecked = new boolean[workouts.size()];
        String[] workoutNames = new String[workouts.size()];
        workouts = Utils.getInstance(MainActivity.this).getAllWorkouts();
        for (int i = 0; i < workouts.size(); i++) {
            workoutNames[i] = workouts.get(i).getName();
            areChecked[i] = false;
        }
        new AlertDialog.Builder(this, R.style.DefaultAlertDialogTheme)
                .setTitle(R.string.savingToAppMemory)
                .setMessage(R.string.savingToAppMemoryInfo)
                .setIcon(R.drawable.ic_info)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new AlertDialog.Builder(MainActivity.this, R.style.DefaultAlertDialogTheme)
                                .setTitle(R.string.selectWorkoutsToSave)
                                .setIcon(R.drawable.ic_save)
                                .setMultiChoiceItems(workoutNames, areChecked, new DialogInterface.OnMultiChoiceClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                        areChecked[which] = isChecked;
                                    }
                                })
                                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        ArrayList<Workout> exportedWorkouts = Utils.getInstance(MainActivity.this).getExportedWorkouts();
                                        if (exportedWorkouts == null) {
                                            exportedWorkouts = new ArrayList<>();
                                        }
                                        int removed = 0;
                                        int size = workouts.size();
                                        for (int i = 0; i < size; i++) {

                                            if (areChecked[i]) {
                                                exportedWorkouts.add(workouts.get(i - removed));
                                                workouts.remove(i - removed);
                                                removed++;
//
                                            }
                                        }
                                        adapter.setWorkouts(workouts);
                                        Utils.getInstance(MainActivity.this).updateWorkouts(workouts);
                                        Utils.getInstance(MainActivity.this).setExportedWorkouts(exportedWorkouts);
                                    }
                                })
                                .setNegativeButton(R.string.cancel, null)
                                .show();
                    }
                }).show();

    }

    private void handleImportFromArchive() {
        ArrayList<Workout> workoutsImport = Utils.getInstance(MainActivity.this).getExportedWorkouts();
        if (workoutsImport == null || workoutsImport.isEmpty()) {
            Toast.makeText(this, R.string.noWorkoutsToLoad, Toast.LENGTH_SHORT).show();
        } else {
            boolean[] areChecked = new boolean[workoutsImport.size()];
            String[] workoutNames = new String[workoutsImport.size()];
            for (int i = 0; i < workoutsImport.size(); i++) {
                workoutNames[i] = workoutsImport.get(i).getName();
                areChecked[i] = false;
            }
            new AlertDialog.Builder(this, R.style.DefaultAlertDialogTheme)
                    .setTitle(R.string.selectWorkoutsToLoad)
                    .setIcon(R.drawable.ic_load)
                    .setMultiChoiceItems(workoutNames, areChecked, new DialogInterface.OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                            areChecked[which] = isChecked;
                        }
                    })
                    .setPositiveButton(R.string.load, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            int removed = 0;
                            int size = workoutsImport.size();
                            workouts = Utils.getInstance(MainActivity.this).getAllWorkouts();
                            for (int i = 0; i < size; i++) {
                                if (areChecked[i]) {
                                    workouts.add(workoutsImport.get(i - removed));
                                    workoutsImport.remove(i - removed);
                                    removed++;
                                }
                            }
                            adapter.setWorkouts(workouts);
                            Utils.getInstance(MainActivity.this).updateWorkouts(workouts);
                            Utils.getInstance(MainActivity.this).setExportedWorkouts(workoutsImport);
                        }
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        }
    }

    private void handleCalendarSelection() {
        getGmailCalendarIds(this);
        String[] accountNames;
        int checkedItem=0;
        if (calendarName.isEmpty()) {
            accountNames = new String[]{getResources().getString(R.string.def)};
        } else {
            accountNames = new String[calendarName.size() + 1];
            accountNames[0] = getResources().getString(R.string.def);
            for (int i = 0; i < calendarName.size(); i++) {
                accountNames[i + 1] = calendarName.get(i);
                if(calendarId.get(i).equals(Utils.getInstance(MainActivity.this).getCalendarID())){
                    checkedItem=i+1;
                }
            }
        }

        new AlertDialog.Builder(this, R.style.DefaultAlertDialogTheme)
                .setTitle(R.string.selectCalendar)
                .setIcon(R.drawable.ic_calendar)
                .setSingleChoiceItems(accountNames, checkedItem, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selected=which;


                    }
                })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        loggingEnabled = true;
                        Utils.getInstance(MainActivity.this).setLoggingEnabled(true);
                        if(selected==0){
                            Utils.getInstance(MainActivity.this).setCalendarID("");
                        }else{
                            Utils.getInstance(MainActivity.this).setCalendarID(calendarId.get(selected-1));
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();

    }

    private void getGmailCalendarIds(Context c) {
        calendarId = new ArrayList<>();
        calendarName = new ArrayList<>();
        String projection[] = {"_id", "calendar_displayName"};
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        areExercisesShown = Utils.getInstance(this).isAreExercisesShown();
        setTheme(R.style.Theme_GymBuddy);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.orange_500)));

        Helpers.setupActionBar(getString(R.string.workouts),"",getSupportActionBar(),this);

//        Toast.makeText(this, areExercisesShown + "", Toast.LENGTH_SHORT).show();

        //workoutsRV=findViewById(R.id.workoutsRV);
        mainAd=findViewById(R.id.mainAd);
        parent = findViewById(R.id.parent);
        btnAddWorkout = findViewById(R.id.btnAddWorkout);
        workoutsRV = findViewById(R.id.workoutsRV);
        btnSchedule = findViewById(R.id.btnSchedule);

        MobileAds.initialize(this);
        AdRequest adRequest = new AdRequest.Builder().build();
        mainAd.loadAd(adRequest);

        workouts = Utils.getInstance(this).getAllWorkouts();
        loggingEnabled = Utils.getInstance(this).isLoggingEnabled();
//
//        for(Workout w:workouts){
//            for(Exercise e:w.getExercises()){
//                e.setSuperSet(0);
//            }
//        }
//
//        Utils.getInstance(this).updateWorkouts(workouts);
//        for(Workout w:workouts
//        ){
//            w.setState(new int[]{1,1,1});
//        }

//        for(Workout w:workouts) {
//            int state[] = w.getState();
//            Toast.makeText(this, w.getId()+ " " + state[0], Toast.LENGTH_LONG).show();
//        }

//        Utils.getInstance(this).updateWorkouts(workouts);

        adapter.setWorkouts(workouts);

        workoutsRV.setAdapter(adapter);

        workoutsRV.setLayoutManager(new LinearLayoutManager(this));

        workoutsRV.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    if (btnAddWorkout.isShown()) {
                        btnAddWorkout.setVisibility(GONE);
                    }
                } else if (dy < 0) {
                    if (!btnAddWorkout.isShown()) {
                        btnAddWorkout.show();
                    }
                }
            }
        });

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(workoutsRV);

        btnSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (verifyCalendarPermissions(MainActivity.this))
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        handleSchedule();
                    }
            }
        });

        btnAddWorkout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int LAUNCH_SECOND_ACTIVITY = 1;
                Intent i = new Intent(MainActivity.this, AddWorkoutActivity.class);
//                startActivity(i);
                startActivityForResult(i, LAUNCH_SECOND_ACTIVITY);
            }
        });
    }

    private void handleSchedule() {
        Intent intent = new Intent(this, ScheduleActivity.class);
        startActivity(intent);
    }


    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN |
            ItemTouchHelper.START | ItemTouchHelper.END, 0) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            int fromPosition = viewHolder.getAdapterPosition();
            int toPosition = target.getAdapterPosition();
            workouts = Utils.getInstance(MainActivity.this).getAllWorkouts();
            Collections.swap(workouts, fromPosition, toPosition);
            adapter.notifyItemMoved(fromPosition, toPosition);
            Utils.getInstance(MainActivity.this).updateWorkouts(workouts);

            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

        }
    };
}

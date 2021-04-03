package com.example.gymbuddy;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.icu.util.TimeZone;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.CalendarContract;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import static android.view.View.GONE;
import static com.example.gymbuddy.ExercisesActivity.NEW_SESSION_KEY;
import static com.example.gymbuddy.ExercisesActivity.RESUMED_KEY;

public class NewSessionActivity extends AppCompatActivity implements SessionsRVAdapter.OnItemClickListener {

    private TextView txtSetCount;
    private TextView txtTimer;
    private TextView tempoTxt, txtTempoSession, repsTxt;
    private EditText edtLoad;
    private EditText edtReps;
    private Button btnSkipTimer;
    private Button btnNextSet;
    private float[] load = {0, 0, 0, 0, 0, 0, 0, 0};
    private int[] reps = {0, 0, 0, 0, 0, 0, 0, 0};
    private int exerciseNo = 1;
    private int setNo = 1;
    private ArrayList<Exercise> exercises;
    private ArrayList<Session> sessions;
    private long timeLeftInMs;
    private boolean isSetFinished = false, isWorkoutFinished = false;
    private CountDownTimer countDownTimer;
    private RecyclerView sessionsRV, exercisesRV;
    SessionsRVAdapter adapter = new SessionsRVAdapter(this);
    private Context mContext = this;
    boolean doubleBackToExitPressedOnce = false;
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    private ProgressBar progressBar;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder builder;
    private NotificationManagerCompat notificationManagerCompat;
    public static final String CHANNEL_ID = "id";
    private boolean isInBackground = false;
    private ImageView imgReps, imgLoads;
    private boolean wasBackPressed = false, wasDoubleBackPressed = false;
    public WorkoutsExercisesRVAdapter adapterExercises = new WorkoutsExercisesRVAdapter(mContext);
    private int[] state = {1, 1, 1};
    private boolean stopwatchRequired = false, isRunning = false, isStopped = false;
    private Handler timerHandler;
    private Runnable timerRunnable;
    int startingTime = 0;
    private boolean breakRunning;
    private Workout workout;
    private AdView newSessionAd;

    @Override
    protected void onResume() {
        notificationManager.cancelAll();
        isInBackground = false;
        wasDoubleBackPressed = false;
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (!wasBackPressed) {
            isInBackground = true;
            wasBackPressed = false;
        }
        super.onPause();
    }


    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            wasBackPressed = true;
            wasDoubleBackPressed = true;
            super.onBackPressed();
            this.finish();
            return;
        }

        this.doubleBackToExitPressedOnce = true;

        Toast.makeText(this, R.string.pressBackToExitSession, Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_GymBuddy);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_session);

        final Intent notificationIntent = new Intent(this, NewSessionActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        timerHandler = new Handler();
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                timerHandler.postDelayed(timerRunnable, 1000);
                startingTime++;
                if (startingTime > 300) {
                    timerHandler.removeCallbacks(timerRunnable);
                }
                updateTimer((startingTime - 1) * 1000);
            }
        };

        Intent intent = getIntent();
        workout = (Workout) intent.getSerializableExtra(NEW_SESSION_KEY);

        exercises = workout.getExercises();
//        if (exercises.get(0).getSessions().isEmpty()) {
//            sessions = new ArrayList<Session>();
//        }

//        getSupportActionBar().setBackgroundDrawable(
//                new ColorDrawable(getResources().getColor(R.color.orange_500)));
//        setTitle(workout.getName());
        Helpers.setupActionBar(workout.getName(), "", getSupportActionBar(), this);

        CharSequence name = "Name";
        String description = "Description";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel = null;
        channel = new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setDescription(description);
        channel.enableVibration(true);
        channel.setVibrationPattern(new long[]{0, 50, 50, 50});
        channel.enableLights(true);
        notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);


        builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_icon)
                .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.mipmap.ic_launcher))
                .setContentTitle(getString(R.string.breakFinished))
                .setContentText(getString(R.string.clickToOpenAndDoTheSet))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setShowWhen(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

//        notificationManagerCompat = NotificationManagerCompat.from(this);

        initViews();

        if (intent.getBooleanExtra(RESUMED_KEY, false)) {
            state = workout.getState();
            exerciseNo = state[1];
            setNo = state[2];
            if (exercises.size() == exerciseNo && exercises.get(exerciseNo - 1).getSets() == setNo) {
                btnNextSet.setText(R.string.finishWorkout);
                isWorkoutFinished = true;
            }
            if (exercises.get(exerciseNo - 1).getSets() == setNo && !isWorkoutFinished) {
                btnNextSet.setText(R.string.nextExercise);
                isSetFinished = true;
            }
            if (Utils.getInstance(this).getBreakLeft() != 0) {
                if (System.currentTimeMillis() - Utils.getInstance(this).getSystemTime() < Utils.getInstance(this).getBreakLeft()) {

                    startTimer(Utils.getInstance(this).getBreakLeft() - System.currentTimeMillis() + Utils.getInstance(this).getSystemTime());
                }
            }

        }

        sessions = exercises.get(exerciseNo - 1).getSessions();

        if (!sessions.isEmpty()) {
            load = sessions.get(0).getLoad();
            reps = sessions.get(0).getReps();
        }

        txtSetCount.setText(String.valueOf(setNo));
        setTempo(exerciseNo);
        if (exercises.get(exerciseNo - 1).getTempo() == 9999) {
            btnSkipTimer.setText(R.string.start);
        }

        adapterExercises.setNewSession(true);
        adapterExercises.setExercises(exercises);
        adapterExercises.setExerciseNo(exerciseNo - 1);

        exercisesRV.setAdapter(adapterExercises);
        exercisesRV.setLayoutManager(new LinearLayoutManager(this));

        adapter.setSessions(sessions);
        adapter.setExercises(exercises);
        adapter.setExercise(exercises.get(exerciseNo - 1));
        adapter.setMenuShown(false);
        adapter.setOnItemClickListener(this);
        sessionsRV.setAdapter(adapter);
        sessionsRV.setLayoutManager(new LinearLayoutManager(this));


        edtLoad.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

//        vibrate();

        btnSkipTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (stopwatchRequired) {
                    if (isStopped) {
                        startingTime = 0;
                        txtTimer.setText("0:00");
                        isRunning = false;
                        isStopped = false;
                        btnSkipTimer.setText(R.string.start);
                    } else if (isRunning) {
                        timerHandler.removeCallbacks(timerRunnable);
                        edtReps.setText(String.valueOf(startingTime - 1));
                        edtReps.setSelection(edtReps.getText().length());
                        isRunning = false;
                        isStopped = true;
                        btnSkipTimer.setText(R.string.reset);
                        btnNextSet.setEnabled(true);
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    } else if (!isRunning & !isStopped) {

                        timerHandler.post(timerRunnable);
                        isRunning = true;
                        btnSkipTimer.setText(R.string.stop);
                        btnNextSet.setEnabled(false);
                        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    }


                } else {
                    breakRunning = false;
                    progressBar.setProgress(0, true);
                    txtTimer.setText("0:00");
                    countDownTimer.cancel();
                    btnNextSet.setEnabled(true);
                    btnSkipTimer.setEnabled(false);
                    vibrate();
                    if (exercises.get(exerciseNo - 1).getTempo() == 9999) {
                        stopwatchRequired = true;
                        btnSkipTimer.setText(R.string.start);
                        btnSkipTimer.setEnabled(true);
                    }
                }
            }
        });


        btnNextSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                isRunning = false;
                isStopped = false;
                startingTime = 0;

                if (edtLoad.getText().toString().isEmpty() || edtReps.getText().toString().isEmpty()) {
                    Toast.makeText(NewSessionActivity.this, R.string.insertRepsAndLoad, Toast.LENGTH_SHORT).show();
                    imgLoads.setImageResource(R.drawable.ic_hexagon_triple_red);
                    Helpers.shake(imgLoads);
                    imgReps.setImageResource(R.drawable.ic_hexagon_triple_red);
                    Helpers.shake(imgReps);
                } else if (isWorkoutFinished) {
                    getValuesAndClear(v, workout);
                    workout.setState(new int[]{1, 1, 1});
                    Utils.getInstance(NewSessionActivity.this).updateSingleWorkouts(workout);
                    InputMethodManager imm = null;
                    if (Utils.getInstance(NewSessionActivity.this).isLoggingEnabled()) {
                        try {
                            handleAddCalendarEntry(calendarEntryStringBuilder());
                        } catch (Exception e) {
                            Toast.makeText(NewSessionActivity.this, getResources().getString(R.string.grantPermission), Toast.LENGTH_LONG).show();
                            Utils.getInstance(NewSessionActivity.this).setLoggingEnabled(false);
                        }

                    }
                    imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
//                    Intent intent = new Intent(NewSessionActivity.this, MainActivity.class);
//                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                    startActivity(intent);
                    finish();
                } else if (isSetFinished) {
                    imgLoads.setImageResource(R.drawable.ic_hexagon_triple_empty);
                    imgReps.setImageResource(R.drawable.ic_hexagon_triple_empty);
                    getValuesAndClear(v, workout);
                    sessions = exercises.get(exerciseNo).getSessions();
                    adapter.setExercise(exercises.get(exerciseNo));
                    adapter.setSessions(sessions);
                    adapter.notifyDataSetChanged();
                    startTimer(Long.parseLong(String.valueOf(exercises.get(exerciseNo).getBreaks())) * 1000);
                    changeColor(exerciseNo);
                    exerciseNo++;
                    setTempo(exerciseNo);
                    setNo = 1;
                    workout.setState(new int[]{0, exerciseNo, setNo});
                    Utils.getInstance(NewSessionActivity.this).updateSingleWorkouts(workout);
                    txtSetCount.setText(String.valueOf(setNo));
                    btnNextSet.setText(R.string.nextSet);
                    stopwatchRequired = false;
                    isSetFinished = false;

                } else {
                    if (setNo == 1) {
                        sessions.add(0, new Session(0, new float[]{0, 0, 0, 0, 0, 0, 0, 0}, new int[]{0, 0, 0, 0, 0, 0, 0, 0}));
                        sessions.get(0).setDate(df.format(Calendar.getInstance().getTime()));
                        load = sessions.get(0).getLoad();
                        reps = sessions.get(0).getReps();
                        adapter.notifyItemInserted(0);
                        sessionsRV.smoothScrollToPosition(0);
                    }


//                    if (exercises.size() == exerciseNo && exercises.get(exerciseNo - 1).getSets() == setNo + 1) {
//                        btnNextSet.setText(R.string.finishWorkout);
//                        isWorkoutFinished = true;
//                    }
//                    if (exercises.get(exerciseNo - 1).getSets() == setNo + 1 && !isWorkoutFinished) {
//                        btnNextSet.setText(R.string.nextExercise);
//                        isSetFinished = true;
//                    }
                    btnSkipTimer.setText(R.string.skipTimer);
                    stopwatchRequired = false;
                    imgLoads.setImageResource(R.drawable.ic_hexagon_triple_empty);
                    imgReps.setImageResource(R.drawable.ic_hexagon_triple_empty);
                    getValuesAndClear(v, workout);
                    adapter.notifyItemChanged(0);
                    if (exercises.get(exerciseNo - 1).getSuperSet() == 0) {
                        setNo++;
                        startTimer(Long.parseLong(String.valueOf(exercises.get(exerciseNo - 1).getBreaks())) * 1000);
                    } else {
                        if (exercises.get(exerciseNo - 1).getSuperSet() == 1) {
                            changeSupersetColor(exerciseNo, 1);
                            exerciseNo++;
                        } else if (exercises.get(exerciseNo - 1).getSuperSet() == 2) {
                            changeSupersetColor(exerciseNo, 2);
                            exerciseNo--;
                            setNo++;
                            startTimer(Long.parseLong(String.valueOf(exercises.get(exerciseNo - 1).getBreaks())) * 1000);
                        }
                        sessions = exercises.get(exerciseNo - 1).getSessions();
                        load = sessions.get(0).getLoad();
                        reps = sessions.get(0).getReps();
                        adapter.setExercise(exercises.get(exerciseNo - 1));
                        adapter.setSessions(sessions);
                        adapter.notifyDataSetChanged();
                        setTempo(exerciseNo);
                    }
                    txtSetCount.setText(String.valueOf(setNo));
                    workout.setState(new int[]{0, exerciseNo, setNo});
                    Utils.getInstance(NewSessionActivity.this).updateSingleWorkouts(workout);
                    if (exercises.size() == exerciseNo && exercises.get(exerciseNo - 1).getSets() == setNo) {
                        btnNextSet.setText(R.string.finishWorkout);
                        isWorkoutFinished = true;
                    }
                    if (exercises.get(exerciseNo - 1).getSets() == setNo && !isWorkoutFinished && exercises.get(exerciseNo - 1).getSuperSet() != 1) {
                        btnNextSet.setText(R.string.nextExercise);
                        isSetFinished = true;
                    }
                }
            }
        });
    }

    private void initViews() {
        exercisesRV = findViewById(R.id.newSessionExercisesRV);
        exercisesRV.setFocusable(false);
        exercisesRV.setClickable(false);
        sessionsRV = findViewById(R.id.newSessionsRV);
//        txtExercise = findViewById(R.id.txtExercise);
//        txtExerciseCount = findViewById(R.id.txtExerciseCount);
        txtSetCount = findViewById(R.id.txtSetCount);
        txtTimer = findViewById(R.id.txtTimer);
        edtLoad = findViewById(R.id.edtLoads);
        edtReps = findViewById(R.id.edtReps);
        btnSkipTimer = findViewById(R.id.btnSkipTimer);
        btnSkipTimer.setEnabled(false);
        btnNextSet = findViewById(R.id.btnNextSet);
//        insertRepsAndLoadTxt = findViewById(R.id.insertRepsAndLoadTxt);
        progressBar = findViewById(R.id.progressBar);
        tempoTxt = findViewById(R.id.tempoTxt);
        txtTempoSession = findViewById(R.id.txtTempoSession);

        imgLoads = findViewById(R.id.imgLoads);
        imgReps = findViewById(R.id.imgReps);

        repsTxt = findViewById(R.id.repsTxt);
        newSessionAd = findViewById(R.id.newSessionAd);
        AdRequest adRequest = new AdRequest.Builder().build();
        newSessionAd.loadAd(adRequest);
    }

    private void setTempo(int exerciseNo) {
        if (exercises.get(exerciseNo - 1).getTempo() == 9999) {
            repsTxt.setText(R.string.timeSet);
            txtTempoSession.setVisibility(GONE);
            tempoTxt.setText(R.string.isometric);
            tempoTxt.setVisibility(View.VISIBLE);
            btnSkipTimer.setEnabled(true);
            stopwatchRequired = true;
        } else {
            repsTxt.setText(R.string.repsSet);
            btnSkipTimer.setText(R.string.skipTimer);
            stopwatchRequired = false;
            if (exercises.get(exerciseNo - 1).getTempo() == 0) {
                txtTempoSession.setVisibility(GONE);
                tempoTxt.setVisibility(GONE);
            } else {
                txtTempoSession.setVisibility(View.VISIBLE);
                tempoTxt.setText(R.string.tempo);
                tempoTxt.setVisibility(View.VISIBLE);
                txtTempoSession.setText(String.valueOf(exercises.get(exerciseNo - 1).getTempo()));
            }
        }
    }

    private void changeColor(int exerciseNo) {
        if (exercises.get(exerciseNo).getSuperSet() == 1) {
            RecyclerView.ViewHolder superSetView = exercisesRV.findViewHolderForAdapterPosition(exerciseNo + 1);
            TextView superSetTxtName = superSetView.itemView.findViewById(R.id.txtExerciseNameSimple);
            TextView superSetTxtSets = superSetView.itemView.findViewById(R.id.txtSetsNoSimple);
            CardView superSetParent = superSetView.itemView.findViewById(R.id.parentExerciseSimple);
            ImageView superSetImgView = superSetView.itemView.findViewById(R.id.imgExerciseSimple);

            superSetTxtName.setTextColor(ContextCompat.getColor(this,R.color.grey_700));
            superSetTxtSets.setTextColor(ContextCompat.getColor(this,R.color.grey_700));
            superSetParent.setCardBackgroundColor(ContextCompat.getColor(this,R.color.orange_200));
            superSetImgView.setImageResource(R.drawable.ic_hexagon_double_vertical_empty);
        } else if (exercises.get(exerciseNo - 1).getSuperSet() == 2) {
            RecyclerView.ViewHolder superSetView = exercisesRV.findViewHolderForAdapterPosition(exerciseNo + 2);
            TextView superSetTxtName = superSetView.itemView.findViewById(R.id.txtExerciseNameSimple);
            TextView superSetTxtSets = superSetView.itemView.findViewById(R.id.txtSetsNoSimple);
            CardView superSetParent = superSetView.itemView.findViewById(R.id.parentExerciseSimple);
            ImageView superSetImgView = superSetView.itemView.findViewById(R.id.imgExerciseSimple);

            superSetTxtName.setTextColor(ContextCompat.getColor(this,R.color.grey_700));
            superSetTxtSets.setTextColor(ContextCompat.getColor(this,R.color.grey_700));
            superSetParent.setCardBackgroundColor(ContextCompat.getColor(this,R.color.orange_200));
            superSetImgView.setImageResource(R.drawable.ic_hexagon_double_vertical_empty);
        }

        RecyclerView.ViewHolder oldView = exercisesRV.findViewHolderForAdapterPosition(exerciseNo - 1);
        TextView oldTxtName = oldView.itemView.findViewById(R.id.txtExerciseNameSimple);
        TextView oldTxtSets = oldView.itemView.findViewById(R.id.txtSetsNoSimple);
        CardView oldParent = oldView.itemView.findViewById(R.id.parentExerciseSimple);
        ImageView oldImgView = oldView.itemView.findViewById(R.id.imgExerciseSimple);

        oldTxtName.setTextColor(ContextCompat.getColor(this,R.color.grey_200));
        oldTxtSets.setTextColor(ContextCompat.getColor(this,R.color.grey_200));
        oldParent.setCardBackgroundColor(ContextCompat.getColor(this,R.color.grey_500));

        RecyclerView.ViewHolder newView = exercisesRV.findViewHolderForAdapterPosition(exerciseNo);
        TextView newTxtName = newView.itemView.findViewById(R.id.txtExerciseNameSimple);
        TextView newTxtSets = newView.itemView.findViewById(R.id.txtSetsNoSimple);
        CardView newParent = newView.itemView.findViewById(R.id.parentExerciseSimple);
        ImageView newImgView = newView.itemView.findViewById(R.id.imgExerciseSimple);

        newTxtName.setTextColor(ContextCompat.getColor(this,R.color.grey_700));
        newTxtSets.setTextColor(ContextCompat.getColor(this,R.color.grey_700));
        newParent.setCardBackgroundColor(ContextCompat.getColor(this,R.color.orange_500));
        newImgView.setImageResource(R.drawable.ic_hexagon_double_vertical_empty);
    }

    private void changeSupersetColor(int exerciseNo, int superset) {

        RecyclerView.ViewHolder oldView;
        RecyclerView.ViewHolder newView;

        if (superset == 1) {
            oldView = exercisesRV.findViewHolderForAdapterPosition(exerciseNo - 1);
            newView = exercisesRV.findViewHolderForAdapterPosition(exerciseNo);
        } else {
            oldView = exercisesRV.findViewHolderForAdapterPosition(exerciseNo - 1);
            newView = exercisesRV.findViewHolderForAdapterPosition(exerciseNo - 2);
        }

        CardView oldParent = oldView.itemView.findViewById(R.id.parentExerciseSimple);
        oldParent.setCardBackgroundColor(getResources().getColor(R.color.orange_200));
        CardView newParent = newView.itemView.findViewById(R.id.parentExerciseSimple);
        newParent.setCardBackgroundColor(getResources().getColor(R.color.orange_500));
    }

    private void vibrate() {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        long[] wave_time = {50, 50, 50};
        int[] wave_ampl = {25, 0, 25};
        VibrationEffect vibrationEffect = null;
        vibrationEffect = VibrationEffect.createWaveform(wave_time, wave_ampl, -1);
        v.vibrate(vibrationEffect);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (breakRunning) {
            Utils.getInstance(this).setSystemTime(System.currentTimeMillis());
            Utils.getInstance(this).setBreakLeft(timeLeftInMs);
        } else {
            Utils.getInstance(this).setBreakLeft(0);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (breakRunning) {
            Utils.getInstance(this).setSystemTime(System.currentTimeMillis());
            Utils.getInstance(this).setBreakLeft(timeLeftInMs);
        } else {
            Utils.getInstance(this).setBreakLeft(0);
        }
    }

    private String calendarEntryStringBuilder() {
        StringBuilder workoutLog = new StringBuilder();
        for (Exercise e : exercises) {
            workoutLog.append(e.getName());
            workoutLog.append(": ");
            for (int i = 0; i < e.getSets(); i++) {
                workoutLog.append(e.getSessions().get(0).getLoad()[i]);
                workoutLog.append("x");
                workoutLog.append(e.getSessions().get(0).getReps()[i]);
                workoutLog.append("  ");
            }
            workoutLog.append("\n");
        }
        return workoutLog.toString();
    }

    private void handleAddCalendarEntry(String entry) {
        long startMillis = 0;
        long endMillis = 0;
        Calendar now = Calendar.getInstance(TimeZone.getDefault());
        Calendar beginTime = Calendar.getInstance();
        int year = now.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH);
        int day = now.get(Calendar.DAY_OF_MONTH);
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int minute = now.get(Calendar.MINUTE);
        beginTime.set(year, month, day, hour, minute);
        startMillis = beginTime.getTimeInMillis();
        Calendar endTime = Calendar.getInstance();
        endTime.set(year, month, day, hour, minute);
        endMillis = endTime.getTimeInMillis();
        ContentResolver cr = getContentResolver();
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.DTSTART, startMillis);
        values.put(CalendarContract.Events.DTEND, endMillis);
        values.put(CalendarContract.Events.TITLE, getResources().getString(R.string.workout) + " " + workout.getName());
        values.put(CalendarContract.Events.DESCRIPTION, entry);
        if (Utils.getInstance(this).getCalendarID().equals("")) {
            values.put(CalendarContract.Events.CALENDAR_ID, 1);
        } else {
            values.put(CalendarContract.Events.CALENDAR_ID, Utils.getInstance(this).getCalendarID());
        }
        values.put(CalendarContract.Events.EVENT_COLOR, getResources().getColor(R.color.orange_500));
        values.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());
        Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);
    }

    private void startTimer(long time) {
        breakRunning = true;
        progressBar.setProgress(100);
        btnNextSet.setEnabled(false);
        btnSkipTimer.setEnabled(true);
        countDownTimer = new CountDownTimer(time + 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                progressBar.setMax(Integer.parseInt(String.valueOf(time)));
                progressBar.setProgress(Integer.parseInt(String.valueOf(millisUntilFinished - 1000)), true);
                timeLeftInMs = millisUntilFinished;
                updateTimer(timeLeftInMs);
            }
            @Override
            public void onFinish() {
                breakRunning = false;
                progressBar.setProgress(0);
                btnNextSet.setEnabled(true);
                btnSkipTimer.setEnabled(false);
                txtTimer.setText("0:00");
                if (isInBackground) {
                    builder.setWhen(System.currentTimeMillis());
                    notificationManager.notify(1, builder.build());
                } else if (!wasDoubleBackPressed) {
                    vibrate();
                }
                if (exercises.get(exerciseNo - 1).getTempo() == 9999) {
                    stopwatchRequired = true;
                    btnSkipTimer.setText(R.string.start);
                    btnSkipTimer.setEnabled(true);
                }
            }
        }.start();
    }

    private void updateTimer(long timeLeftInMs) {
        int minutes = (int) timeLeftInMs / 60000;
        int seconds = (int) timeLeftInMs % 60000 / 1000;
        String timeLeftTxt;
        timeLeftTxt = "" + minutes;
        timeLeftTxt += ":";
        if (seconds < 10) {
            timeLeftTxt += "0";
        }
        timeLeftTxt += seconds;
        txtTimer.setText(timeLeftTxt);
    }

    private void getValuesAndClear(View v, Workout workout) {
        load[setNo - 1] = Float.parseFloat(String.valueOf(edtLoad.getText()));
        reps[setNo - 1] = Integer.parseInt(String.valueOf(edtReps.getText()));
        edtReps.setText(null);
        edtLoad.setText(null);
        edtLoad.requestFocus();
        sessions.get(0).setLoad(load);
        sessions.get(0).setReps(reps);
        exercises.get(exerciseNo - 1).setSessions(sessions);
        Utils.getInstance(v.getContext()).updateWorkoutsExercises(workout, exercises);
    }

    @Override
    public void onItemClick(int positionRV) {
    }
}


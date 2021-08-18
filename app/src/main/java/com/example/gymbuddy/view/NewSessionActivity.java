package com.example.gymbuddy.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.icu.util.TimeZone;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.CalendarContract;
import android.text.InputFilter;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gymbuddy.R;
import com.example.gymbuddy.helpers.Helpers;
import com.example.gymbuddy.helpers.TemplateView;
import com.example.gymbuddy.helpers.Utils;
import com.example.gymbuddy.model.Exercise;
import com.example.gymbuddy.model.Session;
import com.example.gymbuddy.model.Workout;
import com.example.gymbuddy.recyclerViewAdapters.SessionsRVAdapter;
import com.example.gymbuddy.recyclerViewAdapters.WorkoutsExercisesRVAdapter;
import com.example.gymbuddy.services.NotificationService;
import com.google.android.gms.ads.AdLoader;
import com.google.android.material.textfield.TextInputLayout;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import static android.view.View.GONE;

public class NewSessionActivity extends AppCompatActivity implements SessionsRVAdapter.OnItemClickListener {

    private TextView txtSetCount;
    private TextView txtTimer;
    private TextView tempoTxt, txtTempoSession, workoutName;
    private EditText edtLoad;
    private EditText edtReps;
    private TextInputLayout tilReps;
    private Button btnSkipTimer;
    private ImageButton btnNextSet, btnPreviousSet;
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
    boolean doubleBackToExitPressedOnce = false;
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    private ProgressBar progressBar;
    //    private NotificationManager notificationManager;
//    private NotificationCompat.Builder builder1, builder2;
    private boolean isInBackground = false;
    private ImageView imgReps, imgLoads;
    private boolean wasBackPressed = false, wasDoubleBackPressed = false;
    public WorkoutsExercisesRVAdapter adapterExercises = new WorkoutsExercisesRVAdapter(this);
    private int[] state = {1, 1, 1};
    private boolean stopwatchRequired = false, isRunning = false, isStopped = false;
    private Handler timerHandler;
    private Runnable timerRunnable;
    int startingTime = 0;
    private boolean breakRunning;
    private Workout workout;
    private FrameLayout newSessionAdContainer;
    private int backed = 0;
    private TemplateView newSessionAdTemplate;
    private AdLoader adLoader;
    private ArrayList<Set> currentSession;
    private int currentSet = 0;
    private Exercise exercise;
    private boolean mBound;
    private NotificationService notificationService;
    private Intent serviceIntent;
    private ServiceConnection mConnection;

    @Override
    protected void onResume() {
        if (notificationService != null) {
            notificationService.cancelAll();
            stopService(serviceIntent);
//            Toast.makeText(this, "here", Toast.LENGTH_SHORT).show();
        }

        isInBackground = false;
        wasDoubleBackPressed = false;
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (!wasBackPressed) {
            isInBackground = true;
            if (breakRunning) {
                notificationService.setExercise(exercise);
                notificationService.setBreakEnded(false);
                notificationService.setTimeTxt(txtTimer.getText().toString());
                startService(serviceIntent);
//                builder2.setWhen(System.currentTimeMillis())
//                        .setContentTitle(txtTimer.getText().toString() + " - " + getResources().getString(R.string.breakNotification) + " - " + exercise.getName());
//                notificationManager.notify(2, builder2.build());
            }
        }
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
//            Toast.makeText(this, "back pressed twice", Toast.LENGTH_SHORT).show();
            wasBackPressed = true;
            wasDoubleBackPressed = true;
            exercises.get(currentSession.get(currentSet).exercise).setSessions(sessions);
            workout.setState(new int[]{0, currentSet, backed});
//            Utils.getInstance(NewSessionActivity.this).updateSingleWorkout(workout);
            Utils.getInstance(NewSessionActivity.this).updateWorkoutsExercises(workout, exercises);
            super.onBackPressed();
            this.finish();
            return;
        }

        this.doubleBackToExitPressedOnce = true;

        Toast.makeText(this, R.string.pressBackToExitSession, Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_GymBuddy);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_session);

        mConnection = new ServiceConnection() {
            @Override
            public void onServiceDisconnected(ComponentName name) {
                mBound = false;
                notificationService = null;
            }

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mBound = true;
                NotificationService.LocalBinder mLocalBinder = (NotificationService.LocalBinder) service;
                notificationService = mLocalBinder.getServerInstance();
            }
        };


        serviceIntent = new Intent(this, NotificationService.class);
        bindService(serviceIntent, mConnection, BIND_AUTO_CREATE);

        timerHandler = new Handler();
        timerRunnable = () -> {
            timerHandler.postDelayed(timerRunnable, 1000);
            startingTime++;
            if (startingTime > 300) {
                timerHandler.removeCallbacks(timerRunnable);
            }
            updateTimer((startingTime - 1) * 1000);
        };

        initViews();

        if (savedInstanceState==null) {
            Intent intent = getIntent();
            workout = (Workout) intent.getSerializableExtra(Helpers.NEW_SESSION_KEY);
            exercises = workout.getExercises();
            currentSession = new ArrayList<>();
            for (int i = 0; i < exercises.size(); i++) {
                for (int j = 0; j < exercises.get(i).getSets(); j++) {
                    if (exercises.get(i).getSuperSet() != 1 && exercises.get(i).getSuperSet() != 2) {   //jak nie jest supersetem
                        currentSession.add(new Set(i, j));
                        if (j + 1 == exercises.get(i).getSets()) {
//                        if (i + 1 == exercises.size()) {
//                            currentSession.get(currentSession.size() - 1).setState(3);
//                        } else
                            currentSession.get(currentSession.size() - 1).setState(2);
                        }
                    } else if (exercises.get(i).getSuperSet() == 1) {    //jak jest supersetem
//                    Toast.makeText(this, ""+i, Toast.LENGTH_SHORT).show();
                        currentSession.add(new Set(i, j));
                        currentSession.get(currentSession.size() - 1).setState(2);
                        currentSession.add(new Set(i + 1, j));
                        currentSession.get(currentSession.size() - 1).setState(2);
                    }
                }
            }
            currentSession.get(currentSession.size() - 1).setState(3);

            if (intent.getBooleanExtra(Helpers.RESUMED_KEY, false)) {
                state = workout.getState();
                currentSet = state[1];
                backed = state[2];

                if (Utils.getInstance(this).getBreakLeft() != 0) {
                    if (System.currentTimeMillis() - Utils.getInstance(this).getSystemTime() < Utils.getInstance(this).getBreakLeft()) {
                        startTimer(Utils.getInstance(this).getBreakLeft() - System.currentTimeMillis() + Utils.getInstance(this).getSystemTime());
                        breakRunning = true;
                    }
                }
            }
        } else {
//            Toast.makeText(this, "here", Toast.LENGTH_SHORT).show();
            workout = (Workout) savedInstanceState.getSerializable(Helpers.WORKOUT);
            exercises=workout.getExercises();
            currentSession = (ArrayList<Set>) savedInstanceState.getSerializable(Helpers.CURRENT_SESSION);
            currentSet = savedInstanceState.getInt(Helpers.CURRENT_SET);
            backed = savedInstanceState.getInt(Helpers.BACKED);
        }

        Helpers.setupActionBar(workout.getName(), "", getSupportActionBar(), this);
        getSupportActionBar().hide();
        workoutName.setText(workout.getName());


        exercise = exercises.get(currentSession.get(currentSet).getExercise());
        sessions = exercise.getSessions();


        if (currentSet == 0 && (sessions.isEmpty() || sessions.get(0).getTotal() != 0)) {
            sessions.add(0, new Session(0, new float[]{0, 0, 0, 0, 0, 0, 0, 0}, new int[]{0, 0, 0, 0, 0, 0, 0, 0}));
            sessions.get(0).setDate(df.format(Calendar.getInstance().getTime()));
        } else {
            if (!sessions.isEmpty()) {
                load = sessions.get(0).getLoad();
                reps = sessions.get(0).getReps();
            }
        }
        if (backed > 0) {
            edtLoad.setText(Helpers.stringFormat(load[currentSession.get(currentSet).exerciseSet]));
            edtLoad.setSelection(edtLoad.getText().length());
            edtReps.setText(String.valueOf(reps[currentSession.get(currentSet).exerciseSet]));
            edtReps.setSelection(edtReps.getText().length());
        }
        txtSetCount.setText(String.valueOf(currentSession.get(currentSet).getExerciseSet() + 1));
        setTempo(currentSession.get(currentSet).getExercise());
        if (exercises.get(currentSession.get(currentSet).exercise).getTempo() == 9999 && !breakRunning) {
            btnSkipTimer.setText(R.string.start);
        }

        setupRVsAndAdapters();

        edtLoad.requestFocus();
        Helpers.showKeyboard(this);
        btnSkipTimer.setOnClickListener(v -> handleSkip());
        btnNextSet.setOnClickListener(v -> handleNextSet());
        btnPreviousSet.setOnClickListener(v -> handlePreviousSet());
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putSerializable(Helpers.WORKOUT, workout);
        outState.putSerializable(Helpers.CURRENT_SESSION, currentSession);
        outState.putInt(Helpers.CURRENT_SET, currentSet);
        outState.putInt(Helpers.BACKED, backed);
        super.onSaveInstanceState(outState);
    }

    private void handleSkip() {
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
            } else {
                timerHandler.post(timerRunnable);
                isRunning = true;
                btnSkipTimer.setText(R.string.stop);
                btnNextSet.setEnabled(false);
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        } else {
            if (isRunning) {
                timerHandler.removeCallbacks(timerRunnable);
                isRunning = false;
                isStopped = true;
            }
            breakRunning = false;
            progressBar.setProgress(0, true);
            txtTimer.setText("0:00");
            countDownTimer.cancel();
            btnNextSet.setEnabled(true);
            btnSkipTimer.setEnabled(false);
            if (exercises.get(currentSession.get(currentSet).getExercise()).getTempo() == 9999) {
                stopwatchRequired = true;
                btnSkipTimer.setText(R.string.start);
                btnSkipTimer.setEnabled(true);
            }
        }
    }

    private void handlePreviousSet() {
        backed++;
        currentSet--;
        if (currentSession.get(currentSet).state == 2) {
            changeColorToHighlighted(currentSession.get(currentSet).exercise);
            changeColorToDone(currentSession.get(currentSet + 1).exercise);
            setTempo(currentSession.get(currentSet).exercise);
            exercise = exercises.get(currentSession.get(currentSet).exercise);
            sessions = exercise.getSessions();
            adapter.setExercise(exercise);
            adapter.setSessions(sessions);
            adapter.setHighlightPosition(currentSession.get(currentSet).exerciseSet);
            adapter.notifyDataSetChanged();
            load = sessions.get(0).getLoad();
            reps = sessions.get(0).getReps();
        } else {
            adapter.setHighlightPosition(currentSession.get(currentSet).exerciseSet);
            adapter.notifyItemChanged(0);
        }
        txtSetCount.setText(String.valueOf(currentSession.get(currentSet).exerciseSet));
        edtReps.setText(String.valueOf(reps[currentSession.get(currentSet).exerciseSet]));
        edtReps.setSelection(edtReps.getText().length());
        edtLoad.setText(Helpers.stringFormat(load[currentSession.get(currentSet).exerciseSet]));
        edtLoad.setSelection(edtLoad.getText().length());
        handleSkip();
        if (backed == 3 || currentSet == 0) btnPreviousSet.setEnabled(false);
        workout.setState(new int[]{0, currentSet, backed});
        Utils.getInstance(this).updateWorkoutsExercises(workout, exercises);
    }

    private void handleNextSet() {
//        Toast.makeText(this, "backed " + backed, Toast.LENGTH_SHORT).show();
        isRunning = false;
        isStopped = false;
        startingTime = 0;

        if (edtLoad.getText().toString().isEmpty() || edtReps.getText().toString().isEmpty()) {
            Toast.makeText(NewSessionActivity.this, R.string.insertRepsAndLoad, Toast.LENGTH_SHORT).show();
            imgLoads.setImageResource(R.drawable.ic_hexagon_triple_red);
            Helpers.shake(imgLoads);
            imgReps.setImageResource(R.drawable.ic_hexagon_triple_red);
            Helpers.shake(imgReps);
        } else {

            getValuesAndClear();

            if (currentSession.get(currentSet).state == 3) {
                Helpers.hideKeyboard(this);
                workout.setState(new int[]{1, 0, 0});
                if (Utils.getInstance(NewSessionActivity.this).isLoggingEnabled()) {
                    try {
                        handleAddCalendarEntry(calendarEntryStringBuilder());
                    } catch (Exception e) {
                        Toast.makeText(NewSessionActivity.this, getResources().getString(R.string.grantPermission), Toast.LENGTH_LONG).show();
                        Utils.getInstance(NewSessionActivity.this).setLoggingEnabled(false);
                    }
                }

                exercises.get(currentSession.get(currentSet).exercise).setSessions(sessions);
                Utils.getInstance(NewSessionActivity.this).updateWorkoutsExercises(workout, exercises);
                breakRunning = false;
                finish();
            } else {
                if (currentSession.get(currentSet).state == 2) {
//                Toast.makeText(this, "next exercise", Toast.LENGTH_SHORT).show();
                    Helpers.handleNativeAds(newSessionAdTemplate, this, Helpers.AD_ID_NEW_SESSION_NATIVE, adLoader);
                    exercises.get(currentSession.get(currentSet).exercise).setSessions(sessions);
                    exercise = exercises.get(currentSession.get(currentSet + 1).exercise);
                    sessions = exercise.getSessions();
                    if (backed < 1 && currentSession.get(currentSet + 1).exerciseSet == 0) {
                        sessions.add(0, new Session(0, new float[]{0, 0, 0, 0, 0, 0, 0, 0}, new int[]{0, 0, 0, 0, 0, 0, 0, 0}));
                        sessions.get(0).setDate(df.format(Calendar.getInstance().getTime()));
                    }
                    adapter.setExercise(exercise);
                    adapter.setSessions(sessions);
                    adapter.notifyDataSetChanged();
                    load = sessions.get(0).getLoad();
                    reps = sessions.get(0).getReps();
                    changeColorToDone(currentSession.get(currentSet).exercise);
                    changeColorToHighlighted(currentSession.get(currentSet + 1).exercise);
                    setTempo(currentSession.get(currentSet + 1).exercise);
                    Utils.getInstance(NewSessionActivity.this).updateSingleWorkout(workout);
                } else {
//                Toast.makeText(this, "next set", Toast.LENGTH_SHORT).show();
                    btnSkipTimer.setText(R.string.skipTimer);
                    adapter.notifyItemChanged(0);
                }
                stopwatchRequired = false;
                txtSetCount.setText(String.valueOf((currentSession.get(currentSet + 1).exerciseSet) + 1));
                adapter.setHighlightPosition(currentSession.get(currentSet + 1).exerciseSet);
                startTimer(exercise.getBreaks() * 1000);
                if (backed > 0) {
                    backed--;
                    if (backed > 0) {
                        edtLoad.setText(Helpers.stringFormat(load[currentSession.get(currentSet + 1).exerciseSet]));
                        edtLoad.setSelection(edtLoad.getText().length());
                        edtReps.setText(String.valueOf(reps[currentSession.get(currentSet + 1).exerciseSet]));
                        edtReps.setSelection(edtReps.getText().length());
                    }
                }
                currentSet++;
                workout.setState(new int[]{0, currentSet, backed});
                Utils.getInstance(this).updateWorkoutsExercises(workout, exercises);
//                Toast.makeText(this, "" + currentSet, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initViews() {
        newSessionAdTemplate = findViewById(R.id.newSessionAdTemplate);
        exercisesRV = findViewById(R.id.newSessionExercisesRV);
        workoutName = findViewById(R.id.workoutName);
        exercisesRV.setFocusable(false);
        exercisesRV.setClickable(false);
        sessionsRV = findViewById(R.id.newSessionsRV);
//        ((SimpleItemAnimator) Objects.requireNonNull(sessionsRV.getItemAnimator())).setSupportsChangeAnimations(false);
        txtSetCount = findViewById(R.id.txtSetCount);
        txtTimer = findViewById(R.id.txtTimer);
        edtLoad = findViewById(R.id.edtLoads);
        edtLoad.setFilters(new InputFilter[]{new Helpers.DecimalDigitsInputFilter()});
        edtReps = findViewById(R.id.edtReps);
        edtLoad.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == KeyEvent.KEYCODE_CALL) {
                if (edtReps.getText().toString().isEmpty()) {
                    edtReps.requestFocus();
                } else if (btnNextSet.isEnabled()) {
                    handleNextSet();
                }
            }
            return true;
        });
        edtReps.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == KeyEvent.KEYCODE_CALL) {
                if (edtLoad.getText().toString().isEmpty()) {
                    edtLoad.requestFocus();
                } else if (btnNextSet.isEnabled()) {
                    handleNextSet();
                }
            }
            return true;
        });

        btnSkipTimer = findViewById(R.id.btnSkipTimer);
        btnSkipTimer.setEnabled(false);
        btnNextSet = findViewById(R.id.btnNextSet);
        btnPreviousSet = findViewById(R.id.btnPreviousSet);
        if (backed == 3 || currentSet == 0) btnPreviousSet.setEnabled(false);
        progressBar = findViewById(R.id.progressBar);
        tempoTxt = findViewById(R.id.tempoTxt);
        txtTempoSession = findViewById(R.id.txtTempoSession);
        imgLoads = findViewById(R.id.imgLoads);
        imgReps = findViewById(R.id.imgReps);
        tilReps = findViewById(R.id.tilReps);
        newSessionAdContainer = findViewById(R.id.newSessionAdContainer);
        adLoader = Helpers.handleNativeAds(newSessionAdTemplate, this, Helpers.AD_ID_NEW_SESSION_NATIVE, null);
    }

    private void setTempo(int exerciseNo) {
        if (exercises.get(exerciseNo).getTempo() == 9999) {
            tilReps.setHint(R.string.timeSet);
            txtTempoSession.setVisibility(GONE);
            tempoTxt.setText(R.string.isometric);
            tempoTxt.setVisibility(View.VISIBLE);
            btnSkipTimer.setEnabled(true);
            if (!breakRunning) stopwatchRequired = true;
        } else {
            tilReps.setHint(R.string.repsSet);
            btnSkipTimer.setText(R.string.skipTimer);
            stopwatchRequired = false;
            if (exercises.get(exerciseNo).getTempo() == 0) {
                txtTempoSession.setVisibility(GONE);
                tempoTxt.setVisibility(GONE);
            } else {
                txtTempoSession.setVisibility(View.VISIBLE);
                tempoTxt.setText(R.string.tempo);
                tempoTxt.setVisibility(View.VISIBLE);
                txtTempoSession.setText(String.valueOf(exercises.get(exerciseNo).getTempo()));
            }
        }
    }

    private void changeColorToHighlighted(int position) {
        RecyclerView.ViewHolder viewHolder = exercisesRV.findViewHolderForAdapterPosition(position);
        if (viewHolder != null) {
            TextView txtName = viewHolder.itemView.findViewById(R.id.txtExerciseNameSimple);
            TextView txtSets = viewHolder.itemView.findViewById(R.id.txtSetsNoSimple);
            CardView parent = viewHolder.itemView.findViewById(R.id.parentExerciseSimple);
            ImageView imgView = viewHolder.itemView.findViewById(R.id.imgExerciseSimple);

            txtName.setTextColor(ContextCompat.getColor(this, R.color.grey_700));
            txtSets.setTextColor(ContextCompat.getColor(this, R.color.grey_700));
            parent.setCardBackgroundColor(ContextCompat.getColor(this, R.color.orange_500));
            imgView.setImageResource(R.drawable.ic_hexagon_double_vertical_empty);
        }
    }

    private void changeColorToDone(int position) {

        RecyclerView.ViewHolder viewHolder = exercisesRV.findViewHolderForAdapterPosition(position);
        if (viewHolder != null) {
            TextView txtName = viewHolder.itemView.findViewById(R.id.txtExerciseNameSimple);
            TextView txtSets = viewHolder.itemView.findViewById(R.id.txtSetsNoSimple);
            CardView parent = viewHolder.itemView.findViewById(R.id.parentExerciseSimple);

            txtName.setTextColor(ContextCompat.getColor(this, R.color.grey_200));
            txtSets.setTextColor(ContextCompat.getColor(this, R.color.grey_200));
            parent.setCardBackgroundColor(ContextCompat.getColor(this, R.color.grey_500));
        }

    }

    private void vibrate() {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        long[] wave_time = {50, 50, 50};
        int[] wave_ampl = {25, 0, 25};
        VibrationEffect vibrationEffect;
        vibrationEffect = VibrationEffect.createWaveform(wave_time, wave_ampl, -1);
        v.vibrate(vibrationEffect);
    }

    @Override
    protected void onDestroy() {

        if (breakRunning) {
            Utils.getInstance(this).setSystemTime(System.currentTimeMillis());
            Utils.getInstance(this).setBreakLeft(timeLeftInMs);
        } else {
            Utils.getInstance(this).setBreakLeft(0);
        }
        if (mBound) {
            unbindService(mConnection);
            stopService(serviceIntent);
            mBound = false;
        }
        super.onDestroy();
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
        long startMillis;
        long endMillis;
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
        values.put(CalendarContract.Events.EVENT_COLOR, ContextCompat.getColor(this, R.color.orange_500));
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
                if (isInBackground) {
                    notificationService.updateTime(txtTimer.getText().toString());
                }
            }

            @Override
            public void onFinish() {
                breakRunning = false;
                progressBar.setProgress(0);
                btnNextSet.setEnabled(true);
                btnSkipTimer.setEnabled(false);
                txtTimer.setText("0:00");
                if (isInBackground) {
                    notificationService.displayBreakFinishedNotification();

                } else if (!wasDoubleBackPressed) {
                    vibrate();
                }
                if (exercise.getTempo() == 9999) {
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

    private void getValuesAndClear() {
        if (!btnPreviousSet.isEnabled()) btnPreviousSet.setEnabled(true);
        imgLoads.setImageResource(R.drawable.ic_hexagon_triple_empty);
        imgReps.setImageResource(R.drawable.ic_hexagon_triple_empty);
        load[currentSession.get(currentSet).exerciseSet] = Float.parseFloat(String.valueOf(edtLoad.getText()));
        reps[currentSession.get(currentSet).exerciseSet] = Integer.parseInt(String.valueOf(edtReps.getText()));
        edtReps.setText(null);
        edtLoad.setText(null);
        edtLoad.requestFocus();
        sessions.get(0).setLoad(load);
        sessions.get(0).setReps(reps);
    }

    private void setupRVsAndAdapters() {
        adapterExercises.setNewSession(true);
        adapterExercises.setExercises(exercises);
        adapterExercises.setExerciseNo(currentSession.get(currentSet).exercise);
        exercisesRV.setAdapter(adapterExercises);
        exercisesRV.setLayoutManager(new LinearLayoutManager(this));
        adapter.setSessions(sessions);
        adapter.setExercises(exercises);
        adapter.setExercise(exercises.get(currentSession.get(currentSet).exercise));
        adapter.setHighlightPosition(currentSession.get(currentSet).exerciseSet);
        adapter.setMenuShown(false);
        adapter.setOnItemClickListener(this);
        sessionsRV.setAdapter(adapter);
        sessionsRV.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void onItemClick(int positionRV) {
    }

    private static class Set implements Serializable {

        private int exercise;
        private int exerciseSet;
        private int state;

        public Set(int exercise, int exerciseSet) {
            this.exercise = exercise;
            this.exerciseSet = exerciseSet;
            this.state = 0;
        }

        public int getExercise() {
            return exercise;
        }

        public void setExercise(int exercise) {
            this.exercise = exercise;
        }

        public int getExerciseSet() {
            return exerciseSet;
        }

        public void setExerciseSet(int exerciseSet) {
            this.exerciseSet = exerciseSet;
        }

        public void setState(int state) {
            this.state = state;
        }
    }
}


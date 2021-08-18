package com.example.gymbuddy.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gymbuddy.R;
import com.example.gymbuddy.helpers.Helpers;
import com.example.gymbuddy.helpers.TemplateView;
import com.example.gymbuddy.helpers.Utils;
import com.example.gymbuddy.model.Exercise;
import com.example.gymbuddy.model.Session;
import com.example.gymbuddy.model.Workout;
import com.example.gymbuddy.recyclerViewAdapters.SessionsRVAdapter;
import com.google.android.gms.ads.AdLoader;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class SessionsActivity extends AppCompatActivity implements SessionsRVAdapter.OnItemClickListener, SessionsRVAdapter.OnItemLongClickListener {

    private ExtendedFloatingActionButton btnAddSession, btnViewChart;
    private ExtendedFloatingActionButton btnNextExercise, btnPreviousExercise;
    private ArrayList<Exercise> exercises, exercisesImporting;
    private ArrayList<Session> sessions;
    private RecyclerView sessionsRV;
    private int position;
    //    private TextView txtLoad, txtRep;
    private EditText edtLoad, edtRep;
    private TextInputLayout tilLoad, tilRep;
    private ImageButton btnNext, btnPrevious;
    private int setNo = 1;
    private boolean isInputFinished = false;
    private boolean isInputOngoing = false;
    private boolean editing = false;
    private float[] load = {0, 0, 0, 0, 0, 0, 0, 0};
    private int[] reps = {0, 0, 0, 0, 0, 0, 0, 0};
    SessionsRVAdapter adapter = new SessionsRVAdapter(this);
    private final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    private ArrayList<Workout> workouts;
    private String[] workoutNames, exerciseNames;
    private int selectedWorkout = 0, selectedExercise = 0, selectedOptions = 0;
    private ImageView imgRep, imgLoad;
    private Workout displayedWorkout;
    private View viewDisableRV;
    //    private AdView sessionsAd;
    private FrameLayout sessionsAdContainer;
    private TemplateView sessionsAdTemplate;
    private int backed = 0;
    private int inputPosition = 0;
    private AdLoader adLoader;
    private ImageView imgSuperset;

    @Override
    public void onBackPressed() {
        if (tilLoad.isShown()) {
            hideViewsAndReset();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sessions_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.submenuDeleteAllSessions:
                new AlertDialog.Builder(this, R.style.DefaultAlertDialogTheme)
                        .setTitle(getResources().getString(R.string.deletingSessions) + " " + exercises.get(position).getName())
                        .setMessage(R.string.deletingSessionsInfo)
                        .setPositiveButton(R.string.yes, (dialog, which) -> {
                            if(!sessions.isEmpty()) {
                                sessions.clear();
//                                adapter.setSessions(sessions);
                                adapter.notifyDataSetChanged();
                                exercises.get(position).setSessions(sessions);
                                Utils.getInstance(SessionsActivity.this).updateWorkoutsExercisesWithoutWorkout(exercises);
                            }})
                        .setNegativeButton(R.string.no, null)
                        .show();
                return true;
            case R.id.menuItem0:
                return false;
            default:
//                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setTheme(R.style.Theme_GymBuddy);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sessions);

        Intent intent = getIntent();
        exercises = (ArrayList<Exercise>) intent.getSerializableExtra(Helpers.EXERCISES_KEY);
        position = intent.getIntExtra(Helpers.POSITION_KEY, 0);
        displayedWorkout = (Workout) intent.getSerializableExtra(Helpers.WORKOUT_KEY);

//        if (exercises.get(position).getSessions().isEmpty()) {
//            sessions = new ArrayList<Session>();
//        } else {
        sessions = exercises.get(position).getSessions();

//        }

//        getSupportActionBar().setBackgroundDrawable(
//                new ColorDrawable(getResources().getColor(R.color.orange_500)));
        imgRep = findViewById(R.id.imgRep);
        imgLoad = findViewById(R.id.imgLoad);

        edtLoad = findViewById(R.id.edtLoad);
        edtLoad.setFilters(new InputFilter[]{new Helpers.DecimalDigitsInputFilter()});
        edtRep = findViewById(R.id.edtRep);
        tilLoad = findViewById(R.id.tilLoad);
        tilRep = findViewById(R.id.tilRep);
        btnNext = findViewById(R.id.btnNext);
        btnPrevious = findViewById(R.id.btnPrevious);
        sessionsAdTemplate = findViewById(R.id.sessionsAdTemplate);

        setTitleAndTempo();

        sessionsAdContainer = findViewById(R.id.sessionsAdContainer);
//            Helpers.handleAds(sessionsAdContainer, this, Helpers.AD_ID_SESSIONS);
        adLoader = Helpers.handleNativeAds(sessionsAdTemplate, this, Helpers.AD_ID_SESSIONS_NATIVE, null);

//        txtLoad = findViewById(R.id.txtLoad);
//        txtRep = findViewById(R.id.txtRep);

//        parentConstraintLayout = findViewById(R.id.parentConstraintLayout);


        btnPrevious.setEnabled(false);

        edtLoad.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == KeyEvent.KEYCODE_CALL) {
                if (edtRep.getText().toString().isEmpty()) {
                    edtRep.requestFocus();
                } else {
                    handleNextInput();
                }
            }
            return true;
        });

        edtRep.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == KeyEvent.KEYCODE_CALL) {
                if (edtLoad.getText().toString().isEmpty()) {
                    edtLoad.requestFocus();
                } else {
                    handleNextInput();
                }
            }
            return true;
        });

        viewDisableRV = findViewById(R.id.viewDisableRV);

        viewDisableRV.setOnClickListener(v -> Toast.makeText(SessionsActivity.this, R.string.finishInputFirst, Toast.LENGTH_SHORT).show());

        btnViewChart = findViewById(R.id.btnViewChart);
        btnAddSession = findViewById(R.id.btnAddSession);
        btnNextExercise = findViewById(R.id.btnNextExercise);
        btnPreviousExercise = findViewById(R.id.btnPreviousExercise);

        if (sessions.size() < 2) btnViewChart.setVisibility(View.GONE);

        sessionsRV = findViewById(R.id.sessionsRV);

        adapter.setSessions(sessions);
        adapter.setExercises(exercises);
        adapter.setExercise(exercises.get(position));
        adapter.setOnItemClickListener(this);
        adapter.setOnItemLongClickListener(this);
        sessionsRV.setAdapter(adapter);
        sessionsRV.setLayoutManager(new LinearLayoutManager(this));

        btnViewChart.setOnClickListener(v -> {
            Intent intent1 = new Intent(SessionsActivity.this, ChartActivity.class);
            intent1.putExtra(Helpers.CHART_KEY, exercises.get(position));
            startActivity(intent1);
        });

        sessionsRV.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (!isInputOngoing) {
                    if (dy > 0) {
                        if (btnNextExercise.isShown()) {
                            btnNextExercise.setVisibility(GONE);
                            btnPreviousExercise.setVisibility(GONE);
                        }
                        if (btnAddSession.isShown()) {
                            btnAddSession.setVisibility(GONE);
                            btnViewChart.setVisibility(GONE);
                        }
                    } else if (dy < 0) {
                        if (!btnAddSession.isShown()) {
                            btnAddSession.show();
                            btnViewChart.show();
                        }
                        if (!btnNextExercise.isShown()) {
                            btnNextExercise.show();
                            btnPreviousExercise.show();
                        }
                    }
                }
            }
        });

        btnAddSession.setOnClickListener(v -> showViews());

        btnAddSession.setOnLongClickListener(v -> {
            handleImportSessions();
            return false;
        });

        btnNext.setOnClickListener(v -> handleNextInput());

        btnPrevious.setOnClickListener(v -> handlePreviousInput());

        btnNextExercise.setOnClickListener(v -> {
            position++;
            handleNextAndPrevious(position);
        });


        btnPreviousExercise.setOnClickListener(v -> {
            position--;
            handleNextAndPrevious(position);
        });

        if (exercises.size() == position + 1) {
            Helpers.disableEFABClickable(btnNextExercise, this);

        }

        if (position == 0) {
            Helpers.disableEFABClickable(btnPreviousExercise, this);
        }
    }

    private void handlePreviousInput() {
        if (isInputFinished) isInputFinished = false;
        backed++;
        setNo--;
        adapter.setHighlightPosition(setNo - 1);
        adapter.notifyItemChanged(inputPosition);
        if (setNo == 1) btnPrevious.setEnabled(false);
        edtRep.setText(String.valueOf(reps[setNo - 1]));
        edtRep.setSelection(edtRep.getText().length());
        edtLoad.setText(String.valueOf(load[setNo - 1]));
        edtLoad.setSelection(edtLoad.getText().length());
    }

    private void handleNextInput() {
        if (edtRep.getText().toString().isEmpty() || edtLoad.getText().toString().isEmpty()) {
            Toast.makeText(SessionsActivity.this, R.string.insertRepsAndLoad, Toast.LENGTH_SHORT).show();
            imgLoad.setImageResource(R.drawable.ic_hexagon_triple_red);
            Helpers.shake(imgLoad);
            imgRep.setImageResource(R.drawable.ic_hexagon_triple_red);
            Helpers.shake(imgRep);
        } else if (isInputFinished) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            getValuesAndClear();
            hideViewsAndReset();
            Helpers.handleNativeAds(sessionsAdTemplate, this, Helpers.AD_ID_SESSIONS_NATIVE, adLoader);
        } else {
            if (!btnPrevious.isEnabled()) btnPrevious.setEnabled(true);
            if (setNo == 1) {
                if (inputPosition == 0 && backed == 0 && !editing) {
                    sessions.add(0, new Session(0, new float[]{0, 0, 0, 0, 0, 0, 0, 0}, new int[]{0, 0, 0, 0, 0, 0, 0, 0}));
                    sessions.get(0).setDate(df.format(Calendar.getInstance().getTime()));
                    adapter.notifyItemInserted(0);
                    sessionsRV.smoothScrollToPosition(0);
                }
                load = sessions.get(inputPosition).getLoad();
                reps = sessions.get(inputPosition).getReps();
            }
            imgLoad.setImageResource(R.drawable.ic_hexagon_triple_empty);
            imgRep.setImageResource(R.drawable.ic_hexagon_triple_empty);
            getValuesAndClear();


            if (exercises.get(position).getSets() == setNo + 1) {
                isInputFinished = true;
            }
            setNo++;
            if (backed > 0) backed--;
        }
    }

    private void handleNextAndPrevious(int position) {
        setTitleAndTempo();
        sessions = exercises.get(position).getSessions();
        adapter.setSessions(sessions);
        adapter.setExercise(exercises.get(position));
        adapter.notifyDataSetChanged();
        if (position + 1 == exercises.size()) {
            Helpers.disableEFABClickable(btnNextExercise, this);
        } else {
            Helpers.enableEFABClickable(btnNextExercise, this);
        }
        if (position == 0) {
            Helpers.disableEFABClickable(btnPreviousExercise, this);
        } else {
            Helpers.enableEFABClickable(btnPreviousExercise, this);
        }
        if (sessions.size() < 2) btnViewChart.setVisibility(GONE);
        else btnViewChart.show();
    }

    private void showViews() {
        inputPosition = 0;
        isInputOngoing = true;
        viewDisableRV.setVisibility(VISIBLE);
        btnNext.setVisibility(VISIBLE);
        btnPrevious.setVisibility(VISIBLE);
        tilLoad.setVisibility(VISIBLE);
        tilRep.setVisibility(VISIBLE);
        imgLoad.setVisibility(VISIBLE);
        imgRep.setVisibility(VISIBLE);
        btnAddSession.setVisibility(View.GONE);
        btnViewChart.hide();
        btnNextExercise.hide();
        btnPreviousExercise.hide();
        edtLoad.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

    }

    private void setTitleAndTempo() {
        String tempo;
        tilRep.setHint(R.string.repsSet);
        if (exercises.get(position).getTempo() == 9999) {
            tempo = getResources().getString(R.string.iso);
            tilRep.setHint(R.string.timeSet);
        } else if (exercises.get(position).getTempo() == 0) {
            tempo = "";
        } else {
            tempo = String.valueOf(exercises.get(position).getTempo());
        }

//        setTitle(exercises.get(position).getName() + "    " + tempo);
        imgSuperset=Helpers.setupActionBar(exercises.get(position).getName(), tempo, getSupportActionBar(), this);
        if (exercises.get(position).getSuperSet() == 1 || exercises.get(position).getSuperSet() == 2) {
            imgSuperset.setVisibility(VISIBLE);
        }else if(imgSuperset.isShown()){
            imgSuperset.setVisibility(GONE);
        }
    }

    private void handleImportSessions() {
        selectedWorkout = 0;
        workouts = Utils.getInstance(this).getAllWorkouts();
        //Toast.makeText(this, workouts.get(0).getExercises().get(0).getSessions().get(0).getDate()+"", Toast.LENGTH_SHORT).show();
        workoutNames = new String[workouts.size()];
//        workoutNames= new String[]{"a", "b", "d"};
        for (int i = 0; i < workouts.size(); i++) {
            workoutNames[i] = workouts.get(i).getName();
        }
        new AlertDialog.Builder(this, R.style.DefaultAlertDialogTheme)
                .setTitle(R.string.loadSessions)
                .setIcon(R.drawable.ic_load)
                .setSingleChoiceItems(workoutNames, 0, (dialog, which) -> selectedWorkout = which)
                .setPositiveButton(R.string.next, (dialog, which) -> handleShowExercises())
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void handleShowExercises() {
        Exercise exerciseToRemove = null;
        selectedExercise = 0;
        exercisesImporting = workouts.get(selectedWorkout).getExercises();
        if (exercisesImporting.isEmpty()) {
            Toast.makeText(this, R.string.noExercisesToLoadFrom, Toast.LENGTH_SHORT).show();
        } else {
            for (Exercise e : exercisesImporting) {
                if (e.getId() == exercises.get(position).getId()) {
                    exerciseToRemove = e;
                }
            }
            exercisesImporting.remove(exerciseToRemove);
            if (exercisesImporting.isEmpty()) {
                Toast.makeText(this, R.string.noExercisesToLoadFrom, Toast.LENGTH_SHORT).show();
            } else {
                exerciseNames = new String[exercisesImporting.size()];
                for (int i = 0; i < exercisesImporting.size(); i++) {
                    exerciseNames[i] = exercisesImporting.get(i).getName();
                }
                new AlertDialog.Builder(this, R.style.DefaultAlertDialogTheme)
                        .setTitle(R.string.selectExercise)
                        .setIcon(R.drawable.ic_load)
                        .setSingleChoiceItems(exerciseNames, 0, (dialog, which) -> selectedExercise = which)
                        .setPositiveButton(R.string.next, (dialog, which) -> {
                            if (exercisesImporting.get(selectedExercise).getSessions().isEmpty()) {
                                Toast.makeText(SessionsActivity.this, R.string.noSessionsToLoad, Toast.LENGTH_SHORT).show();
                            } else {
                                handleShowOptions();
                            }
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
            }
        }
    }

    private void handleShowOptions() {
        selectedOptions = 0;
        new AlertDialog.Builder(this, R.style.DefaultAlertDialogTheme)
                .setTitle(R.string.selectOptions)
                .setIcon(R.drawable.ic_load)
                .setSingleChoiceItems(new String[]{getString(R.string.overwrite), getString(R.string.addSortRemove)}, 0, (dialog, which) -> selectedOptions = which)
                .setPositiveButton(R.string.load, (dialog, which) -> handleImporting())
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void handleImporting() {
        switch (selectedOptions) {
            case 0:
                sessions = exercisesImporting.get(selectedExercise).getSessions();
                adapter.setSessions(sessions);
                adapter.notifyDataSetChanged();
                exercises.get(position).setSessions(sessions);
                Utils.getInstance(this).updateWorkoutsExercisesWithoutWorkout(exercises);
                break;
            case 1:
                for (Session s : exercisesImporting.get(selectedExercise).getSessions()) {
                    sessions.removeIf(sToRemove -> sToRemove.getDate().equals(s.getDate()) && Arrays.equals(sToRemove.getLoad(), s.getLoad()) && Arrays.equals(sToRemove.getReps(), s.getReps()));
                    adapter.notifyDataSetChanged();

                    sessions.add(0, s);
                    adapter.notifyItemInserted(0);
                }
                sessions.sort((o1, o2) -> o2.getDate().compareTo(o1.getDate()));
                adapter.notifyItemRangeChanged(0, sessions.size());
                exercises.get(position).setSessions(sessions);
                Utils.getInstance(this).updateWorkoutsExercisesWithoutWorkout(exercises);
                break;
            default:
                break;

        }
    }

    private void getValuesAndClear() {
        load[setNo - 1] = Float.parseFloat(String.valueOf(edtLoad.getText()));
        reps[setNo - 1] = Integer.parseInt(String.valueOf(edtRep.getText()));
        if (!editing && backed == 0) {
            edtRep.setText(null);
            edtLoad.setText(null);
        } else {
            if (reps[setNo] == 0) {
                edtRep.setText("");
            } else {
                edtRep.setText(String.valueOf(reps[setNo]));
                edtRep.setSelection(edtRep.getText().length());
            }
            if (load[setNo] == 0) {
                edtLoad.setText("");
            } else {
                edtLoad.setText(String.valueOf(load[setNo]));
                edtLoad.setSelection(edtLoad.getText().length());
            }

        }

        edtLoad.requestFocus();

        sessions.get(inputPosition).setLoad(load);
        sessions.get(inputPosition).setReps(reps);

        exercises.get(position).setSessions(sessions);
        adapter.setHighlightPosition(setNo);
        adapter.notifyItemChanged(inputPosition);

//        Toast.makeText(this, "here", Toast.LENGTH_SHORT).show();
        Utils.getInstance(this).updateWorkoutsExercises(displayedWorkout, exercises);
    }

    public void hideViewsAndReset() {
        adapter.setHighlightPosition(-1);
        adapter.notifyItemChanged(inputPosition);

        setNo = 1;
//        btnNext.setText(R.string.nextSet);
        btnNext.setVisibility(GONE);
        btnPrevious.setVisibility(GONE);
        btnPrevious.setEnabled(false);
//        if (position + 1 != exercises.size()) {
        btnNextExercise.setVisibility(VISIBLE);
//        }
//        if (position != 0) {
        btnPreviousExercise.setVisibility(VISIBLE);
//        }
        if (sessions.size() > 1) btnViewChart.show();
        btnAddSession.setVisibility(VISIBLE);
//        txtRep.setVisibility(View.GONE);
//        txtLoad.setVisibility(View.GONE);
        tilRep.setVisibility(View.GONE);
        tilLoad.setVisibility(View.GONE);
        edtRep.setText(null);
        edtLoad.setText(null);
        imgRep.setVisibility(View.GONE);
        imgLoad.setVisibility(View.GONE);
        isInputFinished = false;
        isInputOngoing = false;
        editing = false;
        viewDisableRV.setVisibility(GONE);
    }

    @Override
    public void onItemClick(int positionRV) {
        if (editing) {
            Toast.makeText(this, R.string.finishInputFirst, Toast.LENGTH_SHORT).show();
        } else {
            viewDisableRV.setVisibility(VISIBLE);
            showViews();
            editing = true;
            edtLoad.setText(Helpers.stringFormat(sessions.get(positionRV).getLoad()[0]));
            edtLoad.setSelection(edtLoad.getText().length());
            edtRep.setText(String.valueOf(sessions.get(positionRV).getReps()[0]));
            edtRep.setSelection(edtRep.getText().length());
            inputPosition = positionRV;
            adapter.setHighlightPosition(0);
            adapter.notifyItemChanged(inputPosition);
        }


    }

    @Override
    public void onItemLongClick(int positionRV, View v) {
//        Toast.makeText(this, ""+ positionRV, Toast.LENGTH_SHORT).show();
        PopupMenu popupMenu = new PopupMenu(this, v);
        popupMenu.inflate(R.menu.popup_menu_session);
        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.menuDeleteSession:
                    new AlertDialog.Builder(SessionsActivity.this, R.style.DefaultAlertDialogTheme)
                            .setMessage(R.string.sureDeleteThisSession)
                            .setIcon(R.drawable.ic_delete)
                            .setPositiveButton(R.string.yes, (dialog, which) -> {
                                try {
                                    sessions.remove(sessions.get(positionRV));
                                    exercises.get(position).setSessions(sessions);
                                    Utils.getInstance(SessionsActivity.this).updateWorkoutsExercisesWithoutWorkout(exercises);
                                    adapter.notifyItemRemoved(positionRV);
                                    adapter.notifyItemRangeChanged(positionRV, sessions.size() - 2 - positionRV);
                                    if (sessions.size() < 2) btnViewChart.hide();
                                } catch (Exception e) {
                                    Toast.makeText(SessionsActivity.this, "" + e, Toast.LENGTH_SHORT).show();
                                }

                            })
                            .setNegativeButton(R.string.no, null)
                            .show();
                    return true;
                default:
                    return false;
            }
        });
        popupMenu.show();
    }

//    public static String stringFormat(double d) {
//        if (d == (long) d)
//            return String.format("%d", (long) d);
//        else
//            return String.format("%s", d);
//    }
}
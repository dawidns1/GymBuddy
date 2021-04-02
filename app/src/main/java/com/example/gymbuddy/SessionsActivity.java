package com.example.gymbuddy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import static android.view.View.GONE;
import static com.example.gymbuddy.ExercisesRVAdapter.WORKOUT_KEY;
import static com.example.gymbuddy.WorkoutsRVAdapter.EXERCISES_KEY;
import static com.example.gymbuddy.WorkoutsRVAdapter.POSITION_KEY;

public class SessionsActivity extends AppCompatActivity implements SessionsRVAdapter.OnItemClickListener, SessionsRVAdapter.OnItemLongClickListener {

    private ExtendedFloatingActionButton btnAddSession, btnViewChart;
    private ExtendedFloatingActionButton btnNextExercise, btnPreviousExercise;
    private ArrayList<Exercise> exercises, exercisesImporting;
    private ArrayList<Session> sessions;
    private RecyclerView sessionsRV;
    private int position, positionSession;
    private TextView txtLoad, txtRep;
    private EditText edtLoad, edtRep;
    private Button btnNext;
    private int setNo = 1;
    private boolean isInputFinished = false;
    private boolean isInputOngoing = false;
    private boolean editing = false;
    private float load[] = {0, 0, 0, 0, 0, 0, 0, 0};
    private int reps[] = {0, 0, 0, 0, 0, 0, 0, 0};
    private ConstraintLayout parentConstraintLayout;
    SessionsRVAdapter adapter = new SessionsRVAdapter(this);
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    private ArrayList<Workout> workouts;
    private String[] workoutNames, exerciseNames;
    private int selectedWorkout = 0, selectedExercise = 0, selectedOptions = 0;
    private ImageView imgRep, imgLoad;
    private Workout displayedWorkout;
    private View viewDisableRV;
    public static final String CHART_KEY = "chart";
    private AdView sessionsAd;

    @Override
    public void onBackPressed() {
        if (edtLoad.isShown()) {
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
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                sessions = new ArrayList<Session>();
                                adapter.setSessions(sessions);
                                adapter.notifyDataSetChanged();
                                exercises.get(position).setSessions(sessions);
                                Utils.getInstance(SessionsActivity.this).updateWorkoutsExercisesWithoutWorkout(exercises);
                            }
                        })
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
        exercises = (ArrayList<Exercise>) intent.getSerializableExtra(EXERCISES_KEY);
        position = (int) intent.getIntExtra(POSITION_KEY, 0);
        displayedWorkout = (Workout) intent.getSerializableExtra(WORKOUT_KEY);

//        if (exercises.get(position).getSessions().isEmpty()) {
//            sessions = new ArrayList<Session>();
//        } else {
        sessions = exercises.get(position).getSessions();

//        }

//        getSupportActionBar().setBackgroundDrawable(
//                new ColorDrawable(getResources().getColor(R.color.orange_500)));
        setTitleAndTempo();

        sessionsAd=findViewById(R.id.sessionsAd);
        AdRequest adRequest = new AdRequest.Builder().build();
        sessionsAd.loadAd(adRequest);

        txtLoad = findViewById(R.id.txtLoad);
        txtRep = findViewById(R.id.txtRep);

        imgRep = findViewById(R.id.imgRep);
        imgLoad = findViewById(R.id.imgLoad);

        edtLoad = findViewById(R.id.edtLoad);
        edtRep = findViewById(R.id.edtRep);
        btnNext = findViewById(R.id.btnNext);
        parentConstraintLayout = findViewById(R.id.parentConstraintLayout);

        viewDisableRV = findViewById(R.id.viewDisableRV);

        viewDisableRV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(SessionsActivity.this, R.string.finishInputFirst, Toast.LENGTH_SHORT).show();
            }
        });

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

        btnViewChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SessionsActivity.this, ChartActivity.class);
                intent.putExtra(CHART_KEY, exercises.get(position));
                startActivity(intent);
            }
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

        btnAddSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showViews();
                isInputOngoing = true;
                btnNext.setVisibility(View.VISIBLE);
                txtLoad.setVisibility(View.VISIBLE);
                txtRep.setVisibility(View.VISIBLE);
                edtLoad.setVisibility(View.VISIBLE);
                edtRep.setVisibility(View.VISIBLE);
                imgLoad.setVisibility(View.VISIBLE);
                imgRep.setVisibility(View.VISIBLE);
                btnAddSession.setVisibility(View.GONE);
                btnNextExercise.setVisibility(View.GONE);
                btnPreviousExercise.setVisibility(View.GONE);
                edtLoad.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            }
        });

        btnAddSession.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                handleImportSessions();
                return false;
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edtRep.getText().toString().isEmpty() || edtLoad.getText().toString().isEmpty()) {
                    Toast.makeText(SessionsActivity.this, R.string.insertRepsAndLoad, Toast.LENGTH_SHORT).show();
                    imgLoad.setImageResource(R.drawable.ic_hexagon_triple_red);
                    shake(imgLoad);
                    imgRep.setImageResource(R.drawable.ic_hexagon_triple_red);
                    shake(imgRep);
                } else if (isInputFinished) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                    getValuesAndClear();
                    hideViewsAndReset();
                } else {
                    if (setNo == 1) {
                        if (!editing) {
                            sessions.add(0, new Session(0, new float[]{0, 0, 0, 0, 0, 0, 0, 0}, new int[]{0, 0, 0, 0, 0, 0, 0, 0}));
                            sessions.get(0).setDate(df.format(Calendar.getInstance().getTime()));
                            adapter.notifyItemInserted(0);
                            sessionsRV.smoothScrollToPosition(0);
                            load = sessions.get(0).getLoad();
                            reps = sessions.get(0).getReps();
                        } else {
                            load = sessions.get(positionSession).getLoad();
                            reps = sessions.get(positionSession).getReps();
                        }

                    }
                    imgLoad.setImageResource(R.drawable.ic_hexagon_triple_empty);
                    imgRep.setImageResource(R.drawable.ic_hexagon_triple_empty);
                    getValuesAndClear();


                    if (exercises.get(position).getSets() == setNo + 1) {
                        btnNext.setText(R.string.done);
                        isInputFinished = true;
                    }
                    setNo++;
                }
            }
        });

        btnNextExercise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                position++;
                handleNextAndPrevious(position);

            }
        });


        btnPreviousExercise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                position--;
                handleNextAndPrevious(position);
            }
        });

        if (exercises.size() == position + 1) {
            disableEFABClickable(btnNextExercise);

        }

        if (position == 0) {
            disableEFABClickable(btnPreviousExercise);
        }
    }

    private void enableEFABClickable(ExtendedFloatingActionButton efab) {
        efab.setClickable(true);
        efab.setTextColor(getResources().getColor(R.color.white));
        int colorInt = getResources().getColor(R.color.orange_500);
        ColorStateList csl = ColorStateList.valueOf(colorInt);
        efab.setStrokeColor(csl);
        int colorIntB = getResources().getColor(R.color.orange_500_alpha);
        ColorStateList cslB = ColorStateList.valueOf(colorIntB);
        efab.setBackgroundTintList(cslB);
    }

    private void disableEFABClickable(ExtendedFloatingActionButton efab) {
        efab.setClickable(false);
        efab.setTextColor(getResources().getColor(R.color.grey_500));
        int colorInt = getResources().getColor(R.color.grey_500);
        ColorStateList csl = ColorStateList.valueOf(colorInt);
        efab.setStrokeColor(csl);
        int colorIntB = getResources().getColor(R.color.grey_700_alpha);
        ColorStateList cslB = ColorStateList.valueOf(colorIntB);
        efab.setBackgroundTintList(cslB);
    }

    private void handleNextAndPrevious(int position) {
        setTitleAndTempo();
        sessions = exercises.get(position).getSessions();
        adapter.setSessions(sessions);
        adapter.setExercise(exercises.get(position));
        adapter.notifyDataSetChanged();
        if (position + 1 == exercises.size()) {
            disableEFABClickable(btnNextExercise);
        } else {
            enableEFABClickable(btnNextExercise);
        }
        if (position == 0) {
            disableEFABClickable(btnPreviousExercise);
        } else {
            enableEFABClickable(btnPreviousExercise);
        }
        if (sessions.size() < 2) btnViewChart.setVisibility(GONE);
        else btnViewChart.show();
    }

    private void showViews() {
        isInputOngoing = true;
        viewDisableRV.setVisibility(View.VISIBLE);
        btnNext.setVisibility(View.VISIBLE);
        txtLoad.setVisibility(View.VISIBLE);
        txtRep.setVisibility(View.VISIBLE);
        edtLoad.setVisibility(View.VISIBLE);
        edtRep.setVisibility(View.VISIBLE);
        imgLoad.setVisibility(View.VISIBLE);
        imgRep.setVisibility(View.VISIBLE);
        btnAddSession.setVisibility(View.GONE);
        btnViewChart.hide();
        btnNextExercise.setVisibility(View.GONE);
        btnPreviousExercise.setVisibility(View.GONE);
        edtLoad.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

    }

    private void setTitleAndTempo() {
        String tempo;
        if (exercises.get(position).getTempo() == 9999) {
            tempo = getResources().getString(R.string.iso);
        } else if (exercises.get(position).getTempo() == 0) {
            tempo = "";
        } else {
            tempo = String.valueOf(exercises.get(position).getTempo());
        }

//        setTitle(exercises.get(position).getName() + "    " + tempo);
        setupActionBar(exercises.get(position).getName(), tempo);
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
                .setSingleChoiceItems(workoutNames, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedWorkout = which;
                    }
                })
                .setPositiveButton(R.string.next, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        handleShowExercises();
                    }
                })
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
                        .setSingleChoiceItems(exerciseNames, 0, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                selectedExercise = which;
                            }
                        })
                        .setPositiveButton(R.string.next, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (exercisesImporting.get(selectedExercise).getSessions().isEmpty()) {
                                    Toast.makeText(SessionsActivity.this, R.string.noSessionsToLoad, Toast.LENGTH_SHORT).show();
                                } else {
                                    handleShowOptions();
                                }
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
                .setSingleChoiceItems(new String[]{getString(R.string.overwrite), getString(R.string.addSortRemove)}, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedOptions = which;
                    }
                })
                .setPositiveButton(R.string.load, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        handleImporting();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
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
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        sessions.removeIf(sToRemove -> sToRemove.getDate().equals(s.getDate()) && Arrays.equals(sToRemove.getLoad(), s.getLoad()) && Arrays.equals(sToRemove.getReps(), s.getReps()));
                        adapter.notifyDataSetChanged();
                    }

                    sessions.add(0, s);
                    adapter.notifyItemInserted(0);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    sessions.sort((o1, o2) -> o2.getDate().compareTo(o1.getDate()));
                    adapter.notifyItemRangeChanged(0, sessions.size());
                }
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
        if (!editing) {
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
                edtLoad.setText(stringFormat(load[setNo]));
                edtLoad.setSelection(edtLoad.getText().length());
            }

        }

        edtLoad.requestFocus();

        if (editing) {
            sessions.get(positionSession).setLoad(load);
            sessions.get(positionSession).setReps(reps);
        } else {
            sessions.get(0).setLoad(load);
            sessions.get(0).setReps(reps);
        }

        exercises.get(position).setSessions(sessions);
        adapter.notifyItemChanged(0);

        Utils.getInstance(this).updateWorkoutsExercises(displayedWorkout, exercises);
    }

    public void hideViewsAndReset() {
        setNo = 1;
        btnNext.setText(R.string.nextSet);
        btnNext.setVisibility(GONE);
//        if (position + 1 != exercises.size()) {
        btnNextExercise.setVisibility(View.VISIBLE);
//        }
//        if (position != 0) {
        btnPreviousExercise.setVisibility(View.VISIBLE);
//        }
        if (sessions.size() > 1) btnViewChart.show();
        btnAddSession.setVisibility(View.VISIBLE);
        txtRep.setVisibility(View.GONE);
        txtLoad.setVisibility(View.GONE);
        edtRep.setVisibility(View.GONE);
        edtLoad.setVisibility(View.GONE);
        edtRep.setText(null);
        edtLoad.setText(null);
        imgRep.setVisibility(View.GONE);
        imgLoad.setVisibility(View.GONE);
        isInputFinished = false;
        isInputOngoing = false;
        editing = false;
        viewDisableRV.setVisibility(GONE);
    }

    private void shake(View v) {
        ObjectAnimator
                .ofFloat(v, "translationX", 0, 25, -25, 25, -25, 15, -15, 6, -6, 0)
                .setDuration(200)
                .start();
    }

    @Override
    public void onItemClick(int positionRV) {
        if (editing) {
            Toast.makeText(this, R.string.finishInputFirst, Toast.LENGTH_SHORT).show();
        } else {
            viewDisableRV.setVisibility(View.VISIBLE);
            showViews();
            editing = true;
            edtLoad.setText(stringFormat(sessions.get(positionRV).getLoad()[0]));
            edtLoad.setSelection(edtLoad.getText().length());
            edtRep.setText(String.valueOf(sessions.get(positionRV).getReps()[0]));
            edtRep.setSelection(edtRep.getText().length());
            positionSession = positionRV;
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
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    try {
                                        sessions.remove(sessions.get(positionRV));
                                        exercises.get(position).setSessions(sessions);
                                        Utils.getInstance(SessionsActivity.this).updateWorkoutsExercisesWithoutWorkout(exercises);
                                        adapter.notifyItemRemoved(positionRV);
                                        adapter.notifyItemRangeChanged(positionRV, sessions.size()-2- positionRV);
                                        if (sessions.size() < 2) btnViewChart.hide();
                                    } catch (Exception e) {
                                        Toast.makeText(SessionsActivity.this, ""+e, Toast.LENGTH_SHORT).show();
                                    }

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

    public static String stringFormat(double d) {
        if (d == (long) d)
            return String.format("%d", (long) d);
        else
            return String.format("%s", d);
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
        ImageView imgSupersetBar = findViewById(R.id.imgSupersetBar);
        abText1.setText(text1);
        abText2.setText(text2);
        if (exercises.get(position).getSuperSet() != 0) {
            imgSupersetBar.setVisibility(View.VISIBLE);
        } else {
            imgSupersetBar.setVisibility(GONE);
        }
    }
}
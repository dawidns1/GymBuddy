package com.example.gymbuddy;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;

import static android.view.View.GONE;
import static com.example.gymbuddy.WorkoutsRVAdapter.EXERCISES_KEY;

public class  ExercisesActivity extends AppCompatActivity {

    Workout incomingWorkout;
    ArrayList<Exercise> exercises = new ArrayList<>();
    ArrayList<Exercise> exercisesImporting = new ArrayList<>();
    ArrayList<Workout> workouts = new ArrayList<>();
    private ExtendedFloatingActionButton btnAddExercise;
    private ExtendedFloatingActionButton btnStartWorkout, btnResumeWorkout;
    ExercisesRVAdapter adapter = new ExercisesRVAdapter(this);
    public final static String NEW_EXERCISE_KEY = "new exercise key";
    public final static String NEW_SESSION_KEY = "new session key";
    public static final String RESUMED_KEY = "resumed";
    private RecyclerView exercisesRV;
    private int selectedWorkoutID, selectedExercise, selectedOptions;
    private String[] workoutNames, exerciseNames;
    private int[] workoutIDs;
    private boolean[] areChecked;
    private boolean isScrolled;
    private AdView exercisesAd;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                Exercise newExercise = (Exercise) data.getSerializableExtra(NEW_EXERCISE_KEY);
                exercises.add(newExercise);
                Utils.getInstance(this).addExerciseToWorkout(incomingWorkout, newExercise);
                adapter.notifyDataSetChanged();
                btnStartWorkout.setVisibility(View.VISIBLE);
            }
            if (resultCode == Activity.RESULT_CANCELED) {

            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        workouts = Utils.getInstance(this).getAllWorkouts();
        if (null != workouts) {
            for (Workout w : workouts) {
                if (w.getId() == incomingWorkout.getId()) {
                    exercises = w.getExercises();
                    adapter.setExercises(exercises);
                    incomingWorkout = w;
                    if (w.getState()[0] == 0 && !isScrolled) {
                        btnResumeWorkout.setVisibility(View.VISIBLE);
                    }
                    else{
                        btnResumeWorkout.setVisibility(GONE);
                    }
                }
            }
            adapter.notifyDataSetChanged();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.exercises_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.submenuDeleteAllExercises:
                new AlertDialog.Builder(this, R.style.DefaultAlertDialogTheme)
                        .setTitle(getResources().getString(R.string.deleting) + " " + incomingWorkout.getName())
                        .setMessage(R.string.sureDelete)
                        .setIcon(R.drawable.ic_delete)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                exercises = new ArrayList<Exercise>();
                                adapter.setExercises(exercises);
                                adapter.notifyDataSetChanged();
                                incomingWorkout.setExercises(exercises);
                                incomingWorkout.setExerciseNumber(0);
                                Utils.getInstance(ExercisesActivity.this).updateWorkoutsExercises(incomingWorkout, exercises);
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
        setContentView(R.layout.activity_exercises);

        Intent intent = getIntent();
        if (intent != null) {
            incomingWorkout = (Workout) intent.getSerializableExtra(EXERCISES_KEY);
            exercises = incomingWorkout.getExercises();
        }

        exercisesAd=findViewById(R.id.chartAd);
        AdRequest adRequest = new AdRequest.Builder().build();
        exercisesAd.loadAd(adRequest);

        btnStartWorkout = findViewById(R.id.btnStartWorkout);
        btnResumeWorkout = findViewById(R.id.btnResumeWorkout);

        if (exercises.isEmpty()) {
            Toast.makeText(this, R.string.noExercises, Toast.LENGTH_SHORT).show();
            btnStartWorkout.setVisibility(View.GONE);
            btnResumeWorkout.setVisibility(View.GONE);
        }

        if (incomingWorkout.getState()[0] == 0) {
            btnResumeWorkout.setVisibility(View.VISIBLE);
        }

        Helpers.setupActionBar(incomingWorkout.getName(), "",getSupportActionBar(),this);

        btnAddExercise = findViewById(R.id.btnAddExercise);
        exercisesRV = findViewById(R.id.exercisesRV);

        adapter.setExercises(exercises);
        adapter.setDisplayedWorkout(incomingWorkout);

//        Toast.makeText(this, ""+exercises.get(0).getSuperSet(), Toast.LENGTH_SHORT).show();

        exercisesRV.setAdapter(adapter);

        exercisesRV.setLayoutManager(new LinearLayoutManager(this));

        exercisesRV.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    if (btnAddExercise.isShown()) {
                        btnAddExercise.setVisibility(GONE);
                    }
                    if (btnStartWorkout.isShown()) {
                        btnStartWorkout.setVisibility(GONE);
                    }
                    if (btnResumeWorkout.isShown()) {
                        btnResumeWorkout.setVisibility(GONE);
                    }
                    isScrolled=true;
                } else if (dy < 0) {
                    if (!btnAddExercise.isShown()) {
                        btnAddExercise.show();
                    }
                    if (!btnStartWorkout.isShown()) {
                        btnStartWorkout.show();
                    }
                    if (!btnResumeWorkout.isShown() && incomingWorkout.getState()[0] == 0) {
                        btnResumeWorkout.show();
                    }
                    isScrolled=false;
                }
            }
        });

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(exercisesRV);
//
        btnAddExercise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int LAUNCH_SECOND_ACTIVITY = 1;
                Intent i = new Intent(ExercisesActivity.this, AddExerciseActivity.class);
//                startActivity(i);
                startActivityForResult(i, LAUNCH_SECOND_ACTIVITY);
            }
        });

        btnAddExercise.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                handleImportExercises();
                return false;
            }
        });

        btnStartWorkout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ExercisesActivity.this, NewSessionActivity.class);
                incomingWorkout.setExercises(adapter.getExercises());
                intent.putExtra(NEW_SESSION_KEY, incomingWorkout);
                startActivity(intent);
            }
        });

        btnResumeWorkout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ExercisesActivity.this, NewSessionActivity.class);
                incomingWorkout.setExercises(adapter.getExercises());
                intent.putExtra(NEW_SESSION_KEY, incomingWorkout);
                intent.putExtra(RESUMED_KEY, true);
                startActivity(intent);
            }
        });
    }

    private void handleImportExercises() {
        workouts = Utils.getInstance(this).getAllWorkouts();
        if (workouts.size() == 1) {
            Toast.makeText(this, R.string.noOtherWorkouts, Toast.LENGTH_SHORT).show();
        } else {
            workoutNames = new String[workouts.size() - 1];
            workoutIDs=new int[workouts.size()-1];
            int i = 0;
            for (Workout w : workouts) {
                if (w.getId() != incomingWorkout.getId()) {
                    workoutNames[i] = w.getName();
                    workoutIDs[i]=w.getId();
                    i++;
                }
            }
            selectedWorkoutID=workoutIDs[0];
            new AlertDialog.Builder(this, R.style.DefaultAlertDialogTheme)
                    .setTitle(R.string.loadExercises)
                    .setIcon(R.drawable.ic_load)
                    .setSingleChoiceItems(workoutNames, 0, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
//                            Toast.makeText(ExercisesActivity.this, ""+which, Toast.LENGTH_SHORT).show();
                            selectedWorkoutID = workoutIDs[which];
                        }
                    })
                    .setPositiveButton(R.string.next, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
//                            Toast.makeText(ExercisesActivity.this, ""+selectedWorkoutID, Toast.LENGTH_SHORT).show();
                            handleShowExercises();
                        }
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        }
    }

    private void handleShowExercises() {
        selectedExercise = 0;
        for(Workout w:workouts){
            if(w.getId()==selectedWorkoutID)exercisesImporting=w.getExercises();
        }
//        exercisesImporting = workouts.get(selectedWorkoutID).getExercises();
        if (exercisesImporting.isEmpty()) {
            Toast.makeText(this, R.string.noExercisesToLoad, Toast.LENGTH_SHORT).show();
        } else {
            exerciseNames = new String[exercisesImporting.size()];
            areChecked = new boolean[exercisesImporting.size()];
            for (int i = 0; i < exercisesImporting.size(); i++) {
                exerciseNames[i] = exercisesImporting.get(i).getName();
                areChecked[i] = false;
            }
            new AlertDialog.Builder(this, R.style.DefaultAlertDialogTheme)
                    .setTitle(R.string.selectExercise)
                    .setIcon(R.drawable.ic_load)
                    .setMultiChoiceItems(exerciseNames, areChecked, new DialogInterface.OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                            areChecked[which] = isChecked;
                        }
                    })
                    .setPositiveButton(R.string.next, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            handleShowOptions();
                        }
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        }
    }

    private void handleShowOptions() {
        selectedOptions = 0;
        new AlertDialog.Builder(this, R.style.DefaultAlertDialogTheme)
                .setTitle(R.string.selectOptions)
                .setIcon(R.drawable.ic_load)
                .setSingleChoiceItems(new String[]{getString(R.string.overwrite), getString(R.string.add)}, 0, new DialogInterface.OnClickListener() {
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
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void handleImporting() {
        switch (selectedOptions) {
            case 0:
                exercises = new ArrayList<>();
                adapter.setExercises(exercises);
                break;
            default:
                break;
        }
        for (int i=0; i<exercisesImporting.size();i++) {
            if(areChecked[i]){
                exercises.add(exercisesImporting.get(i));
                Utils.getInstance(this).addExerciseToWorkout(incomingWorkout, exercisesImporting.get(i));
            }


        }
        adapter.notifyDataSetChanged();
    }

    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN |
            ItemTouchHelper.START | ItemTouchHelper.END, 0) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            int fromPosition = viewHolder.getAdapterPosition();
            int toPosition = target.getAdapterPosition();
            Collections.swap(exercises, fromPosition, toPosition);
            adapter.notifyItemMoved(fromPosition, toPosition);
//            adapter.setExercises(exercises);
//            adapter.notifyDataSetChanged();
            Utils.getInstance(ExercisesActivity.this).updateWorkoutsExercises(incomingWorkout, exercises);

            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

        }
    };
}


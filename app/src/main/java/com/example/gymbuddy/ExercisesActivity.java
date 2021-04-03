package com.example.gymbuddy;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;

import static android.view.View.GONE;
import static com.example.gymbuddy.ExercisesRVAdapter.WORKOUT_KEY;
import static com.example.gymbuddy.WorkoutsRVAdapter.EXERCISES_KEY;
import static com.example.gymbuddy.WorkoutsRVAdapter.POSITION_KEY;

public class ExercisesActivity extends AppCompatActivity implements ExercisesRVAdapter.OnItemClickListener {

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
    private int selectedWorkoutID, selectedExercise, selectedOptions;
    private int[] workoutIDs;
    private boolean[] areChecked;
    private boolean isScrolled;
    private String[] exercisesForSuperset;

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
                if(exercises.size()>8){
                    Helpers.disableEFABClickable(btnAddExercise,this);
                    Toast.makeText(this, getResources().getString(R.string.maximumExerciseNumberReached), Toast.LENGTH_SHORT).show();
                }
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
                    } else {
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
                        .setPositiveButton(R.string.yes, (dialog, which) -> {
                            exercises = new ArrayList<>();
                            adapter.setExercises(exercises);
                            adapter.notifyDataSetChanged();
                            incomingWorkout.setExercises(exercises);
                            incomingWorkout.setExerciseNumber(0);
                            Utils.getInstance(ExercisesActivity.this).updateWorkoutsExercises(incomingWorkout, exercises);
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

        AdView exercisesAd = findViewById(R.id.chartAd);
        AdRequest adRequest = new AdRequest.Builder().build();
        exercisesAd.loadAd(adRequest);

        btnStartWorkout = findViewById(R.id.btnStartWorkout);
        btnResumeWorkout = findViewById(R.id.btnResumeWorkout);
        btnAddExercise = findViewById(R.id.btnAddExercise);
        RecyclerView exercisesRV = findViewById(R.id.exercisesRV);

        if (exercises.size() > 8) Helpers.disableEFABClickable(btnAddExercise, this);

        if (exercises.isEmpty()) {
            Toast.makeText(this, R.string.noExercises, Toast.LENGTH_SHORT).show();
            btnStartWorkout.setVisibility(View.GONE);
            btnResumeWorkout.setVisibility(View.GONE);
        }

        if (incomingWorkout.getState()[0] == 0) {
            btnResumeWorkout.setVisibility(View.VISIBLE);
        }

        Helpers.setupActionBar(incomingWorkout.getName(), "", getSupportActionBar(), this);

        adapter.setOnItemClickListener(this);
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
                    isScrolled = true;
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
                    isScrolled = false;
                }
            }
        });

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(exercisesRV);
//
        btnAddExercise.setOnClickListener(v -> {
            if (exercises.size() > 8) {
                Toast.makeText(ExercisesActivity.this, getResources().getString(R.string.cannotAddMoreExercises), Toast.LENGTH_SHORT).show();
            } else {
                int LAUNCH_SECOND_ACTIVITY = 1;
                Intent i = new Intent(ExercisesActivity.this, AddExerciseActivity.class);
                startActivityForResult(i, LAUNCH_SECOND_ACTIVITY);
            }
        });

        btnAddExercise.setOnLongClickListener(v -> {
            if (exercises.size()>8){
                Toast.makeText(ExercisesActivity.this, getResources().getString(R.string.cannotAddMoreExercises), Toast.LENGTH_SHORT).show();
            }else{
                handleImportExercises();
            }
            return false;
        });

        btnStartWorkout.setOnClickListener(v -> {
            Intent intent12 = new Intent(ExercisesActivity.this, NewSessionActivity.class);
            incomingWorkout.setExercises(adapter.getExercises());
            intent12.putExtra(NEW_SESSION_KEY, incomingWorkout);
            startActivity(intent12);
        });

        btnResumeWorkout.setOnClickListener(v -> {
            Intent intent1 = new Intent(ExercisesActivity.this, NewSessionActivity.class);
            incomingWorkout.setExercises(adapter.getExercises());
            intent1.putExtra(NEW_SESSION_KEY, incomingWorkout);
            intent1.putExtra(RESUMED_KEY, true);
            startActivity(intent1);
        });
    }

    private void handleImportExercises() {
        workouts = Utils.getInstance(this).getAllWorkouts();
        if (workouts.size() == 1) {
            Toast.makeText(this, R.string.noOtherWorkouts, Toast.LENGTH_SHORT).show();
        } else {
            String[] workoutNames = new String[workouts.size() - 1];
            workoutIDs = new int[workouts.size() - 1];
            int i = 0;
            for (Workout w : workouts) {
                if (w.getId() != incomingWorkout.getId()) {
                    workoutNames[i] = w.getName();
                    workoutIDs[i] = w.getId();
                    i++;
                }
            }
            selectedWorkoutID = workoutIDs[0];
            new AlertDialog.Builder(this, R.style.DefaultAlertDialogTheme)
                    .setTitle(R.string.loadExercises)
                    .setIcon(R.drawable.ic_load)
                    .setSingleChoiceItems(workoutNames, 0, (dialog, which) -> {
//                            Toast.makeText(ExercisesActivity.this, ""+which, Toast.LENGTH_SHORT).show();
                        selectedWorkoutID = workoutIDs[which];
                    })
                    .setPositiveButton(R.string.next, (dialog, which) -> {
//                            Toast.makeText(ExercisesActivity.this, ""+selectedWorkoutID, Toast.LENGTH_SHORT).show();
                        handleShowExercises();
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        }
    }

    private void handleShowExercises() {
        selectedExercise = 0;
        for (Workout w : workouts) {
            if (w.getId() == selectedWorkoutID) exercisesImporting = w.getExercises();
        }
//        exercisesImporting = workouts.get(selectedWorkoutID).getExercises();
        if (exercisesImporting.isEmpty()) {
            Toast.makeText(this, R.string.noExercisesToLoad, Toast.LENGTH_SHORT).show();
        } else {
            String[] exerciseNames = new String[exercisesImporting.size()];
            areChecked = new boolean[exercisesImporting.size()];
            for (int i = 0; i < exercisesImporting.size(); i++) {
                exerciseNames[i] = exercisesImporting.get(i).getName();
                areChecked[i] = false;
            }
            new AlertDialog.Builder(this, R.style.DefaultAlertDialogTheme)
                    .setTitle(R.string.selectExercise)
                    .setIcon(R.drawable.ic_load)
                    .setMultiChoiceItems(exerciseNames, areChecked, (dialog, which, isChecked) -> areChecked[which] = isChecked)
                    .setPositiveButton(R.string.next, (dialog, which) -> handleShowOptions())
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        }
    }

    private void handleShowOptions() {
        selectedOptions = 0;
        new AlertDialog.Builder(this, R.style.DefaultAlertDialogTheme)
                .setTitle(R.string.selectOptions)
                .setIcon(R.drawable.ic_load)
                .setSingleChoiceItems(new String[]{getString(R.string.overwrite), getString(R.string.add)}, 0, (dialog, which) -> selectedOptions = which)
                .setPositiveButton(R.string.load, (dialog, which) -> handleImporting())
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
        for (int i = 0; i < exercisesImporting.size(); i++) {
            if (areChecked[i] && exercises.size() < 9) {
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

    @Override
    public void onItemClick(int positionRV, View v) {
        PopupMenu popupMenu = new PopupMenu(this, v);
        popupMenu.inflate(R.menu.popup_menu_exercise);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menuEditE:
                        Intent intent = new Intent(ExercisesActivity.this, AddExerciseActivity.class);
                        intent.putExtra(EXERCISES_KEY, exercises);
                        intent.putExtra(POSITION_KEY, positionRV);
                        intent.putExtra(WORKOUT_KEY, incomingWorkout);
                        startActivity(intent);
                        return true;
                    case R.id.menuDeleteE:
                        new AlertDialog.Builder(ExercisesActivity.this, R.style.DefaultAlertDialogTheme)
                                .setTitle(R.string.deletingExercise)
                                .setMessage(R.string.sureDeleteThis)
                                .setIcon(R.drawable.ic_delete)
                                .setPositiveButton(R.string.yes, (dialog, which) -> {
                                    if (exercises.get(positionRV).getSuperSet() != 0) {
                                        handleDisableSuperset();
                                    }
                                    Utils.getInstance(ExercisesActivity.this).deleteExerciseFromWorkout(exercises.get(positionRV));
                                    exercises.remove(exercises.get(positionRV));
                                    adapter.notifyItemRemoved(positionRV);
                                    if(exercises.size()==8){Helpers.enableEFABClickable(btnAddExercise,ExercisesActivity.this);}

                                })
                                .setNegativeButton(R.string.no, null)
                                .show();
                        return true;
                    case R.id.menuSuperset:
                        if (exercises.get(positionRV).getSuperSet() != 0) {
                            new AlertDialog.Builder(ExercisesActivity.this, R.style.DefaultAlertDialogTheme)
                                    .setTitle(R.string.disablingSuperset)
                                    .setMessage(R.string.disablingSupersetMsg)
                                    .setIcon(R.drawable.ic_superset)
                                    .setPositiveButton(R.string.yes, (dialog, which) -> {
                                        handleDisableSuperset();
                                        switch (exercises.get(positionRV).getSuperSet()) {
                                            case 1:
                                                exercises.get(positionRV).setSuperSet(0);
                                                adapter.notifyItemChanged(positionRV);
                                                exercises.get(positionRV + 1).setSuperSet(0);
                                                adapter.notifyItemChanged(positionRV + 1);
                                                break;
                                            case 3:
                                                exercises.get(positionRV).setSuperSet(0);
                                                adapter.notifyItemChanged(positionRV);
                                                exercises.get(positionRV - 1).setSuperSet(0);
                                                adapter.notifyItemChanged(positionRV - 1);
                                                break;
                                            case 2:
                                                exercises.get(positionRV).setSuperSet(0);
                                                adapter.notifyItemChanged(positionRV);
                                                if (positionRV == 0) {
                                                    exercises.get(positionRV + 1).setSuperSet(0);
                                                    adapter.notifyItemChanged(positionRV + 1);
                                                } else if (positionRV == exercises.size() - 1) {
                                                    exercises.get(positionRV - 1).setSuperSet(0);
                                                    adapter.notifyItemChanged(positionRV - 1);
                                                } else if (exercises.get(positionRV - 1).getSuperSet() == 1) {
                                                    exercises.get(positionRV - 1).setSuperSet(0);
                                                    adapter.notifyItemChanged(positionRV - 1);
                                                } else {
                                                    exercises.get(positionRV + 1).setSuperSet(0);
                                                    adapter.notifyItemChanged(positionRV + 1);
                                                }
                                                break;
                                            default:
                                                break;
                                        }
                                        Utils.getInstance(ExercisesActivity.this).updateWorkoutsExercises(incomingWorkout, exercises);
                                    })
                                    .setNegativeButton(R.string.no, null)
                                    .show();

                        } else {
                            boolean available = true;
                            if (exercises.size() == 1 || (positionRV == 0 && exercises.get(1).getSuperSet() != 0) ||
                                    (positionRV == exercises.size() - 1 && exercises.get(exercises.size() - 2).getSuperSet() != 0)) {
                                Toast.makeText(ExercisesActivity.this, R.string.noAvailableExercises, Toast.LENGTH_SHORT).show();
                                available = false;
                            } else if (positionRV == 0) {
                                exercisesForSuperset = new String[]{exercises.get(1).getName()};
                            } else if (positionRV == exercises.size() - 1) {
                                exercisesForSuperset = new String[]{exercises.get(exercises.size() - 2).getName()};
                            } else if (exercises.get(positionRV - 1).getSuperSet() != 0 && exercises.get(positionRV + 1).getSuperSet() != 0) {
                                Toast.makeText(ExercisesActivity.this, R.string.noAvailableExercises, Toast.LENGTH_SHORT).show();
                                available = false;
                            } else if (exercises.get(positionRV - 1).getSuperSet() != 0) {
                                exercisesForSuperset = new String[]{exercises.get(positionRV + 1).getName()};
                            } else if (exercises.get(positionRV + 1).getSuperSet() != 0) {
                                exercisesForSuperset = new String[]{exercises.get(positionRV - 1).getName()};
                            } else {
                                exercisesForSuperset = new String[]{exercises.get(positionRV - 1).getName(), exercises.get(positionRV + 1).getName()};
                            }
                            if (available) {
                                new AlertDialog.Builder(ExercisesActivity.this, R.style.DefaultAlertDialogTheme)
                                        .setTitle(exercises.get(positionRV).getName() + " " + getResources().getString(R.string.supersetWith))
                                        .setIcon(R.drawable.ic_superset)
                                        .setSingleChoiceItems(exercisesForSuperset, 0, (dialog, which) -> selectedExercise = which)
                                        .setPositiveButton(R.string.ok, (dialog, which) -> {
                                            if (positionRV == 0) {
                                                exercises.get(positionRV).setSuperSet(1);
                                                adapter.notifyItemChanged(positionRV);
                                                exercises.get(1).setSuperSet(2);
                                                adapter.notifyItemChanged(1);
                                            } else if (positionRV == exercises.size() - 1) {
                                                exercises.get(positionRV).setSuperSet(2);
                                                adapter.notifyItemChanged(positionRV);
                                                exercises.get(exercises.size() - 2).setSuperSet(1);
                                                adapter.notifyItemChanged(exercises.size() - 2);
                                            } else {
                                                if (selectedExercise == 0) {
                                                    exercises.get(positionRV).setSuperSet(2);
                                                    adapter.notifyItemChanged(positionRV);
                                                    exercises.get(positionRV - 1).setSuperSet(1);
                                                    adapter.notifyItemChanged(positionRV - 1);
                                                } else {
                                                    exercises.get(positionRV).setSuperSet(1);
                                                    adapter.notifyItemChanged(positionRV);
                                                    exercises.get(positionRV + 1).setSuperSet(2);
                                                    adapter.notifyItemChanged(positionRV + 1);
                                                }
                                            }
                                            Utils.getInstance(ExercisesActivity.this).updateWorkoutsExercises(incomingWorkout, exercises);
                                        })
                                        .setNegativeButton(R.string.cancel, null)
                                        .show();
                            }
                        }
                        return true;
                    default:
                        return false;
                }
            }

            private void handleDisableSuperset() {
                switch (exercises.get(positionRV).getSuperSet()) {
                    case 1:
                        exercises.get(positionRV).setSuperSet(0);
                        adapter.notifyItemChanged(positionRV);
                        exercises.get(positionRV + 1).setSuperSet(0);
                        adapter.notifyItemChanged(positionRV + 1);
                        break;
                    case 2:
                        exercises.get(positionRV).setSuperSet(0);
                        adapter.notifyItemChanged(positionRV);
                        exercises.get(positionRV - 1).setSuperSet(0);
                        adapter.notifyItemChanged(positionRV - 1);
                        break;
                    default:
                        break;
                }
                Utils.getInstance(ExercisesActivity.this).updateWorkoutsExercises(incomingWorkout, exercises);
            }
        });
        popupMenu.show();
    }


}


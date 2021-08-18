package com.example.gymbuddy.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gymbuddy.R;
import com.example.gymbuddy.helpers.TemplateView;
import com.example.gymbuddy.helpers.Helpers;
import com.example.gymbuddy.helpers.MyItemAnimator;
import com.example.gymbuddy.helpers.Utils;
import com.example.gymbuddy.model.Exercise;
import com.example.gymbuddy.model.Workout;
import com.example.gymbuddy.recyclerViewAdapters.ExercisesRVAdapter;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;

import static android.view.View.GONE;

public class ExercisesActivity extends AppCompatActivity implements ExercisesRVAdapter.OnItemClickListener {

    Workout incomingWorkout;
    ArrayList<Exercise> exercises = new ArrayList<>();
    ArrayList<Exercise> exercisesImporting = new ArrayList<>();
    ArrayList<Workout> workouts = new ArrayList<>();
    private ExtendedFloatingActionButton btnAddExercise;
    private ExtendedFloatingActionButton btnStartWorkout, btnResumeWorkout;
    private ExercisesRVAdapter adapter;
    private int selectedWorkoutID, selectedExercise, selectedOptions;
    private int[] workoutIDs;
    private boolean[] areChecked;
    private boolean isScrolled;
    private String[] exercisesForSuperset;
    private FrameLayout exercisesAdContainer;
    private TemplateView exercisesAdTemplate;
    private int[] moveParameters = {0, 0, 0};
    private ArrayList<Integer> removedIDs = new ArrayList<>();
    private boolean result=false;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                result=true;
                if (data.getSerializableExtra(Helpers.EDITED_EXERCISE_KEY) == null) {
                    Exercise newExercise = (Exercise) data.getSerializableExtra(Helpers.NEW_EXERCISE_KEY);
                    newExercise.setId(Utils.getInstance(this).getExercisesId());
                    Utils.getInstance(this).setExercisesId(newExercise.getId() + 1);
                    exercises.add(newExercise);
//                    Utils.getInstance(this).addExerciseToWorkout(incomingWorkout, newExercise);
                    if(!btnStartWorkout.isShown())btnStartWorkout.setVisibility(View.VISIBLE);
                    if (exercises.size() > 8) {
                        Helpers.disableEFABClickable(btnAddExercise, this);
                        Toast.makeText(this, getResources().getString(R.string.maximumExerciseNumberReached), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Exercise editedExercise = (Exercise) data.getSerializableExtra(Helpers.EDITED_EXERCISE_KEY);
                    for (int i = 0; i < exercises.size(); i++) {
                        if (exercises.get(i).getId() == editedExercise.getId()) {
                            int tempSets = exercises.get(i).getSets();
                            exercises.remove(i);
                            exercises.add(i, editedExercise);
                            if (exercises.get(i).getSets() != tempSets) {
                                if (exercises.get(i).getSuperSet() == 1)
                                    exercises.get(i + 1).setSets(exercises.get(i).getSets());
                                else if (exercises.get(i).getSuperSet() == 2)
                                    handleDisableSuperset(i, false);
                            }
                        }
                    }
                }
                adapter.notifyDataSetChanged();

            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!result) {
            workouts = Utils.getInstance(this).getAllWorkouts();
            if (null != workouts) {
                for (Workout w : workouts) {
                    if (w.getId() == incomingWorkout.getId()) {
                        exercises = w.getExercises();
                        adapter.setExercises(exercises);
                        incomingWorkout = w;
//                        Toast.makeText(this, ""+w.getState()[0]+" "+w.getState()[1]+" "+w.getState()[2], Toast.LENGTH_SHORT).show();
                        if (w.getState()[0] == 0 && !isScrolled) {
                            btnResumeWorkout.setVisibility(View.VISIBLE);
                        } else {
                            btnResumeWorkout.setVisibility(GONE);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }
            result=false;
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
                            incomingWorkout.setState(new int[]{1, 1, 1});
                            btnResumeWorkout.setVisibility(GONE);
                            btnStartWorkout.setVisibility(GONE);
//                            Utils.getInstance(ExercisesActivity.this).updateWorkoutsExercises(incomingWorkout, exercises);
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
    protected void onPause() {
        Utils.getInstance(this).updateWorkoutsExercises(incomingWorkout, exercises);
        super.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_GymBuddy);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercises);

        Intent intent = getIntent();
        if (intent != null) {
            incomingWorkout = (Workout) intent.getSerializableExtra(Helpers.EXERCISES_KEY);
            exercises = incomingWorkout.getExercises();
        }

        exercisesAdContainer = findViewById(R.id.exercisesdAdContainer);
        exercisesAdTemplate = findViewById(R.id.exercisesAdTemplate);
//        Helpers.handleAds(exercisesAdContainer,this, Helpers.AD_ID_EXERCISES);
        Helpers.handleNativeAds(exercisesAdTemplate, this, Helpers.AD_ID_EXERCISES_NATIVE, null);

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

        adapter=new ExercisesRVAdapter(this,exercises);
        adapter.setHasStableIds(true);
        adapter.setOnItemClickListener(this);
        adapter.setDisplayedWorkout(incomingWorkout);

//        Toast.makeText(this, ""+exercises.get(0).getSuperSet(), Toast.LENGTH_SHORT).show();

        exercisesRV.setAdapter(adapter);
        exercisesRV.setLayoutManager(new LinearLayoutManager(this));
        exercisesRV.setItemAnimator(new MyItemAnimator());
//        ((SimpleItemAnimator) Objects.requireNonNull(exercisesRV.getItemAnimator())).setSupportsChangeAnimations(false);
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
            if (exercises.size() > 8) {
                Toast.makeText(ExercisesActivity.this, getResources().getString(R.string.cannotAddMoreExercises), Toast.LENGTH_SHORT).show();
            } else {
                handleImportExercises();
            }
            return false;
        });

        btnStartWorkout.setOnClickListener(v -> {
            Intent intent12 = new Intent(ExercisesActivity.this, NewSessionActivity.class);
            incomingWorkout.setExercises(adapter.getExercises());
            intent12.putExtra(Helpers.NEW_SESSION_KEY, incomingWorkout);
            startActivity(intent12);
        });

        btnResumeWorkout.setOnClickListener(v -> {
            Intent intent1 = new Intent(ExercisesActivity.this, NewSessionActivity.class);
            incomingWorkout.setExercises(adapter.getExercises());
            intent1.putExtra(Helpers.NEW_SESSION_KEY, incomingWorkout);
            intent1.putExtra(Helpers.RESUMED_KEY, true);
            startActivity(intent1);
        });
    }

    private void handleImportExercises() {
//        workouts = Utils.getInstance(this).getAllWorkouts();
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
        if (selectedOptions == 0) {
            exercises = new ArrayList<>();
            adapter.setExercises(exercises);
        }
        for (int i = 0; i < exercisesImporting.size(); i++) {
            if (areChecked[i] && exercises.size() < 9) {
                exercises.add(exercisesImporting.get(i));
//                Utils.getInstance(this).addExerciseToWorkout(incomingWorkout, exercisesImporting.get(i));
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
//            Toast.makeText(ExercisesActivity.this, fromPosition+" "+toPosition, Toast.LENGTH_SHORT).show();
            if (exercises.get(fromPosition).getSuperSet() == 1) {
                removedIDs.add(exercises.get(fromPosition).getId());
                removedIDs.add(exercises.get(fromPosition + 1).getId());
                exercises.get(fromPosition).setSuperSet(0);
                exercises.get(fromPosition + 1).setSuperSet(0);
            } else if (exercises.get(fromPosition).getSuperSet() == 2) {
                removedIDs.add(exercises.get(fromPosition).getId());
                removedIDs.add(exercises.get(fromPosition - 1).getId());
                exercises.get(fromPosition).setSuperSet(0);
                exercises.get(fromPosition - 1).setSuperSet(0);
            } else if (exercises.get(toPosition).getSuperSet() == 1) {
                removedIDs.add(exercises.get(toPosition).getId());
                removedIDs.add(exercises.get(toPosition + 1).getId());
                exercises.get(fromPosition).setSuperSet(0);
                exercises.get(toPosition + 1).setSuperSet(0);
            } else if (exercises.get(toPosition).getSuperSet() == 2) {
                removedIDs.add(exercises.get(toPosition).getId());
                removedIDs.add(exercises.get(toPosition - 1).getId());
                exercises.get(toPosition).setSuperSet(0);
                exercises.get(toPosition - 1).setSuperSet(0);
            }
            Collections.swap(exercises, fromPosition, toPosition);
            adapter.notifyItemMoved(fromPosition, toPosition);
            incomingWorkout.setState(new int[]{1, 1, 1});
            btnResumeWorkout.setVisibility(GONE);
//            Utils.getInstance(ExercisesActivity.this).updateWorkoutsExercises(incomingWorkout, exercises);

            return false;
        }

        @Override
        public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            for (Integer id : removedIDs) {
                for (int i = 0; i < exercises.size(); i++) {
                    if (exercises.get(i).getId() == id) {
                        adapter.notifyItemChanged(i);
                    }
                }
            }
            removedIDs.clear();
            super.clearView(recyclerView, viewHolder);
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        }
    };

    @Override
    public void onItemClick(int positionRV, View v) {
        PopupMenu popupMenu = new PopupMenu(this, v);
        popupMenu.inflate(R.menu.popup_menu_exercise);
        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.menuEditE:
                    Intent intent = new Intent(ExercisesActivity.this, AddExerciseActivity.class);
                    int LAUNCH_SECOND_ACTIVITY = 1;
                    intent.putExtra(Helpers.EXERCISES_KEY, exercises);
                    intent.putExtra(Helpers.POSITION_KEY, positionRV);
                    intent.putExtra(Helpers.WORKOUT_KEY, incomingWorkout);
                    startActivityForResult(intent, LAUNCH_SECOND_ACTIVITY);
                    return true;
                case R.id.menuDeleteE:
                    new AlertDialog.Builder(ExercisesActivity.this, R.style.DefaultAlertDialogTheme)
                            .setTitle(R.string.deletingExercise)
                            .setMessage(R.string.sureDeleteThis)
                            .setIcon(R.drawable.ic_delete)
                            .setPositiveButton(R.string.yes, (dialog, which) -> {
                                if (exercises.get(positionRV).getSuperSet() != 0) {
                                    handleDisableSuperset(positionRV, true);
                                }
//                                Utils.getInstance(ExercisesActivity.this).deleteExerciseFromWorkout(exercises.get(positionRV));
                                btnResumeWorkout.setVisibility(GONE);
                                exercises.remove(exercises.get(positionRV));
                                adapter.notifyItemRemoved(positionRV);
                                if(exercises.isEmpty()){btnStartWorkout.setVisibility(GONE);
                                btnResumeWorkout.setVisibility(GONE);
                                }
                                if (exercises.size() == 8) {
                                    Helpers.enableEFABClickable(btnAddExercise, ExercisesActivity.this);
                                }

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
                                    handleDisableSuperset(positionRV, true);
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
                                            exercises.get(1).setSuperSet(2);
                                            exercises.get(1).setSets(exercises.get(positionRV).getSets());
                                            adapter.notifyItemChanged(1);
                                        } else if (positionRV == exercises.size() - 1) {
                                            exercises.get(positionRV).setSuperSet(2);
                                            exercises.get(exercises.size() - 2).setSets(exercises.get(positionRV).getSets());
                                            exercises.get(exercises.size() - 2).setSuperSet(1);
                                            adapter.notifyItemChanged(exercises.size() - 2);
                                        } else {
                                            if (selectedExercise == 0) {
                                                exercises.get(positionRV).setSuperSet(2);
                                                exercises.get(positionRV).setSets(exercises.get(positionRV - 1).getSets());
                                                exercises.get(positionRV - 1).setSuperSet(1);
                                                adapter.notifyItemChanged(positionRV - 1);
                                            } else {
                                                exercises.get(positionRV).setSuperSet(1);
                                                exercises.get(positionRV + 1).setSuperSet(2);
                                                exercises.get(positionRV + 1).setSets(exercises.get(positionRV).getSets());
                                                adapter.notifyItemChanged(positionRV + 1);
                                            }
                                        }
                                        adapter.notifyItemChanged(positionRV);
//                                        Utils.getInstance(ExercisesActivity.this).updateWorkoutsExercises(incomingWorkout, exercises);
                                    })
                                    .setNegativeButton(R.string.cancel, null)
                                    .show();
                        }
                    }
                    return true;
                default:
                    return false;
            }
        });
        popupMenu.show();
    }

    private void handleDisableSuperset(int positionRV, boolean notify) {
        switch (exercises.get(positionRV).getSuperSet()) {
            case 1:
                exercises.get(positionRV).setSuperSet(0);

                exercises.get(positionRV + 1).setSuperSet(0);

                moveParameters[2] = positionRV + 1;
                if (notify) {
                    adapter.notifyItemChanged(positionRV);
                    adapter.notifyItemChanged(positionRV + 1);

                }
                break;
            case 2:
                exercises.get(positionRV).setSuperSet(0);

                exercises.get(positionRV - 1).setSuperSet(0);

                moveParameters[2] = positionRV - 1;
                if (notify) {
                    adapter.notifyItemChanged(positionRV);
                    adapter.notifyItemChanged(positionRV - 1);
                }
                break;
            default:
                break;
        }
        Toast.makeText(this, "" + moveParameters[2], Toast.LENGTH_SHORT).show();
//        Utils.getInstance(ExercisesActivity.this).updateWorkoutsExercises(incomingWorkout, exercises);
    }

}


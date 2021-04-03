package com.example.gymbuddy;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

import static com.example.gymbuddy.MainActivity.NEW_WORKOUT_KEY;
import static com.example.gymbuddy.WorkoutsRVAdapter.POSITION_KEY;
import static com.example.gymbuddy.WorkoutsRVAdapter.WORKOUTS_KEY;

public class AddWorkoutActivity extends AppCompatActivity {

    private EditText edtWorkoutName;
    private Spinner spinnerWorkoutType;
    private EditText edtMuscleGroup;
    private EditText edtMuscleGroupSecondary;
    private Button btnAdd;
    private ImageView imgWorkoutName;
    boolean isEdited = false, changeMade = false;
    ArrayList<Workout> allWorkouts;
    int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_GymBuddy);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_workout);

        Helpers.setupActionBar(getString(R.string.addNewWorkout),"",getSupportActionBar(),this);

        edtWorkoutName = findViewById(R.id.edtWorkoutName);
        spinnerWorkoutType = findViewById(R.id.spinnerWorkoutType);
        edtMuscleGroup = findViewById(R.id.edtMuscleGroup);
        edtMuscleGroupSecondary = findViewById(R.id.edtMuscleGroupSecondary);
        btnAdd = findViewById(R.id.btnAdd);
        imgWorkoutName = findViewById(R.id.imgWorkoutName);

        edtWorkoutName.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

        edtMuscleGroupSecondary.setEnabled(false);
//        edtMuscleGroupSecondary.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);

        edtMuscleGroup.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() != 0) {
                    edtMuscleGroupSecondary.setEnabled(true);
//                    edtMuscleGroupSecondary.getBackground().setColorFilter(Color.parseColor("#FF5722"), PorterDuff.Mode.SRC_IN);
                } else {
                    edtMuscleGroupSecondary.setEnabled(false);
//                    edtMuscleGroupSecondary.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        Intent intent = getIntent();
        if (intent != null) {
            if (intent.getSerializableExtra(WORKOUTS_KEY) != null) {
                allWorkouts = (ArrayList<Workout>) intent.getSerializableExtra(WORKOUTS_KEY);
                position = (int) intent.getIntExtra(POSITION_KEY, 0);
                isEdited = true;
                Helpers.setupActionBar(getString(R.string.editing)+" "+ allWorkouts.get(position).getName(),"",getSupportActionBar(),this);
                edtWorkoutName.setText(allWorkouts.get(position).getName());
                edtWorkoutName.setSelection(edtWorkoutName.getText().length());
                edtMuscleGroup.setText(allWorkouts.get(position).getMuscleGroup());
                edtMuscleGroupSecondary.setText((allWorkouts.get(position).getMuscleGroupSecondary()));
                btnAdd.setText(R.string.save);
                switch (allWorkouts.get(position).getType()) {
                    case "---":
                        spinnerWorkoutType.setSelection(0);
                        break;
                    case "FBW":
                        spinnerWorkoutType.setSelection(1);
                        break;
                    case "Push":
                        spinnerWorkoutType.setSelection(2);
                        break;
                    case "Pull":
                        spinnerWorkoutType.setSelection(3);
                        break;
                    case "Legs":
                        spinnerWorkoutType.setSelection(4);
                        break;
                    case "Upper":
                        spinnerWorkoutType.setSelection(5);
                        break;
                    case "Lower":
                        spinnerWorkoutType.setSelection(6);
                        break;
                    case "Split":
                        spinnerWorkoutType.setSelection(7);
                        break;
                    default:
                        spinnerWorkoutType.setSelection(0);
                        break;
                }
            }
        }

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEdited) {
                    if (!edtWorkoutName.getText().toString().isEmpty() &&
                            !edtWorkoutName.getText().toString().equals(allWorkouts.get(position).getName())) {
                        changeMade = true;
                        allWorkouts.get(position).setName(edtWorkoutName.getText().toString());
                    }
                    if (!spinnerWorkoutType.getSelectedItem().toString().equals(allWorkouts.get(position).getType())) {
                        changeMade = true;
                        allWorkouts.get(position).setType(spinnerWorkoutType.getSelectedItem().toString());
                    }
                    if (edtMuscleGroup.getText().toString().isEmpty()) {
                        allWorkouts.get(position).setMuscleGroup("");
                        allWorkouts.get(position).setMuscleGroupSecondary("");
                        changeMade = true;
                    } else {
                        if (!edtMuscleGroup.getText().toString().equals(allWorkouts.get(position).getMuscleGroup())) {
                            changeMade = true;
                            allWorkouts.get(position).setMuscleGroup(edtMuscleGroup.getText().toString());
                        }
                        if (!edtMuscleGroupSecondary.getText().toString().equals(allWorkouts.get(position).getMuscleGroupSecondary())) {
                            changeMade = true;
                            allWorkouts.get(position).setMuscleGroupSecondary(edtMuscleGroupSecondary.getText().toString());
                        }
                    }
                    if(changeMade){
                        Utils.getInstance(AddWorkoutActivity.this).updateWorkouts(allWorkouts);
                    }
//                    Intent intent=new Intent(AddWorkoutActivity.this, MainActivity.class);
//                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                    startActivity(intent);
                    finish();


                } else {
                    if (edtWorkoutName.getText().toString().isEmpty()) {
                        Toast.makeText(AddWorkoutActivity.this, R.string.nameIsRequired, Toast.LENGTH_SHORT).show();
                        imgWorkoutName.setImageResource(R.drawable.ic_hexagon_single_red);
                        shake(imgWorkoutName);
                    } else {
                        String workoutName = edtWorkoutName.getText().toString();
                        String workoutType = spinnerWorkoutType.getSelectedItem().toString();
                        String muscleGroup = edtMuscleGroup.getText().toString();
                        String muscleGroupSecondary = "";
                        if (edtMuscleGroupSecondary.isEnabled()) {
                            muscleGroupSecondary = edtMuscleGroupSecondary.getText().toString();
                        }
                        Workout workout = new Workout(5, workoutName, workoutType, muscleGroup, muscleGroupSecondary, 0);
//                        Toast.makeText(AddWorkoutActivity.this, workout.toString(), Toast.LENGTH_SHORT).show();
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra(NEW_WORKOUT_KEY, workout);
                        setResult(Activity.RESULT_OK, returnIntent);
                        InputMethodManager imm = null;
                        imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                        finish();
                    }
                }

            }
        });
    }

    private void shake(View v) {
        ObjectAnimator
                .ofFloat(v, "translationX", 0, 25, -25, 25, -25, 15, -15, 6, -6, 0)
                .setDuration(200)
                .start();
    }
}

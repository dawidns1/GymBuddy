package com.example.gymbuddy;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.util.ArrayList;

import static com.example.gymbuddy.ExercisesActivity.NEW_EXERCISE_KEY;
import static com.example.gymbuddy.ExercisesRVAdapter.WORKOUT_KEY;
import static com.example.gymbuddy.MainActivity.NEW_WORKOUT_KEY;
import static com.example.gymbuddy.WorkoutsRVAdapter.EXERCISES_KEY;
import static com.example.gymbuddy.WorkoutsRVAdapter.POSITION_KEY;
import static com.example.gymbuddy.WorkoutsRVAdapter.WORKOUTS_KEY;

public class AddExerciseActivity extends AppCompatActivity {

    private Spinner edtSets, edtIso;
    private EditText edtExerciseName;
    private EditText edtMuscleGroupE;
    private EditText edtMuscleGroupSecondaryE;
    private EditText edtBreaks;
    private EditText edtTempo;
    private Button btnAddNewExercise;
    private ImageView imgSets, imgBreaks, imgExerciseName, imgTempo;
    private ArrayList<Exercise> allExercises;
    int position;
    private boolean isEdited;
    private boolean changeMade;
    private Workout workout;
    private Button btnSelect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_GymBuddy);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_exercise);

//        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.orange_500)));
//        setTitle("Add new exercise");

        setupActionBar(getString(R.string.addNewExercise), "");

        edtIso = findViewById(R.id.edtIso);
        edtSets = findViewById(R.id.edtSets);
        edtExerciseName = findViewById(R.id.edtExerciseName);
        edtMuscleGroupE = findViewById(R.id.edtMuscleGroupE);
        edtMuscleGroupSecondaryE = findViewById(R.id.edtMuscleGroupSecondaryE);
        edtBreaks = findViewById(R.id.edtTxtBreaks);
        btnAddNewExercise = findViewById(R.id.btnAddNewExercise);
        imgSets = findViewById(R.id.imgSets);
        imgExerciseName = findViewById(R.id.imgExerciseName);
        imgBreaks = findViewById(R.id.imgBreaks);
        edtTempo = findViewById(R.id.edtTempo);
        imgTempo = findViewById(R.id.imgTempo);
        btnSelect=findViewById(R.id.btnSelect);

        edtExerciseName.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

        edtMuscleGroupSecondaryE.setEnabled(false);
//        edtMuscleGroupSecondaryE.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);

        NumberPicker.Formatter formatter = new NumberPicker.Formatter() {
            @Override
            public String format(int value) {
                int temp = value * 5;
                return "" + temp;
            }
        };

        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
//                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                final AlertDialog.Builder d = new AlertDialog.Builder(AddExerciseActivity.this,R.style.DefaultAlertDialogTheme);
                LayoutInflater inflater = getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.breaks_dialog, null);
                d.setTitle(R.string.breakDialog);
                d.setMessage(R.string.breakDialogMessage);
                d.setView(dialogView);
                d.setIcon(R.drawable.ic_timer);
                final NumberPicker numberPicker = (NumberPicker) dialogView.findViewById(R.id.dialogNumberPicker);
                numberPicker.setFormatter(formatter);
                numberPicker.setMaxValue(60);
                numberPicker.setMinValue(4);

                numberPicker.setWrapSelectorWheel(false);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    numberPicker.setTextSize(80);
                }
                numberPicker.setValue(24);
                try {
                    Field field = NumberPicker.class.getDeclaredField("mInputText");
                    field.setAccessible(true);
                    EditText inputText = (EditText) field.get(numberPicker);
                    inputText.setVisibility(View.INVISIBLE);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                    @Override
                    public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                    }
                });
                d.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        edtBreaks.setText(String.valueOf(numberPicker.getValue()*5));
                    }
                });
                d.setNegativeButton(R.string.cancel, null);
                AlertDialog alertDialog = d.create();
                alertDialog.show();
            }
        });

        edtIso.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 1) {
                    edtTempo.setEnabled(false);
//                    edtTempo.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
                } else {
                    edtTempo.setEnabled(true);
//                    edtTempo.getBackground().setColorFilter(Color.parseColor("#FF5722"), PorterDuff.Mode.SRC_IN);

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        edtMuscleGroupE.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() != 0) {
                    edtMuscleGroupSecondaryE.setEnabled(true);
//                    edtMuscleGroupSecondaryE.getBackground().setColorFilter(Color.parseColor("#FF5722"), PorterDuff.Mode.SRC_IN);
                } else {
                    edtMuscleGroupSecondaryE.setEnabled(false);
//                    edtMuscleGroupSecondaryE.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        Intent intent = getIntent();
        if (intent != null) {
            if (intent.getSerializableExtra(EXERCISES_KEY) != null) {
                allExercises = (ArrayList<Exercise>) intent.getSerializableExtra(EXERCISES_KEY);
                position = (int) intent.getIntExtra(POSITION_KEY, 0);
                workout = (Workout) intent.getSerializableExtra(WORKOUT_KEY);
                isEdited = true;
//                setTitle("Editing " + allExercises.get(position).getName());
                setupActionBar(getString(R.string.editing) + " " + allExercises.get(position).getName(), "");
                edtExerciseName.setText(allExercises.get(position).getName());
                edtExerciseName.setSelection(edtExerciseName.getText().length());
                edtMuscleGroupE.setText(allExercises.get(position).getMuscleGroup());
                edtMuscleGroupSecondaryE.setText((allExercises.get(position).getMuscleGroupSecondary()));
                if (allExercises.get(position).getTempo() == 9999) {
                    edtTempo.setText(null);
                    edtTempo.setEnabled(false);
                    edtIso.setSelection(1);
                } else {
                    edtIso.setSelection(0);
                    if (allExercises.get(position).getTempo() == 0) {
                        edtTempo.setText(null);
                    } else {
                        edtTempo.setText(String.valueOf(allExercises.get(position).getTempo()));
                    }
                }
                edtBreaks.setText(String.valueOf(allExercises.get(position).getBreaks()));
                btnAddNewExercise.setText(R.string.save);
                switch (allExercises.get(position).getSets()) {
                    case 3:
                        edtSets.setSelection(0);
                        break;
                    case 4:
                        edtSets.setSelection(1);
                        break;
                    case 5:
                        edtSets.setSelection(2);
                        break;
                    case 6:
                        edtSets.setSelection(3);
                        break;
                    case 7:
                        edtSets.setSelection(4);
                        break;
                    case 8:
                        edtSets.setSelection(5);
                        break;
                    default:
                        edtSets.setSelection(0);
                        break;
                }
            }
        }

        btnAddNewExercise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEdited) {
                    if (!edtExerciseName.getText().toString().isEmpty() &&
                            !edtExerciseName.getText().toString().equals(allExercises.get(position).getName())) {
                        changeMade = true;
                        allExercises.get(position).setName(edtExerciseName.getText().toString());
                    }
                    if (!edtBreaks.getText().toString().isEmpty() && Integer.parseInt(edtBreaks.getText().toString()) != 0 &&
                            !edtBreaks.getText().toString().equals(String.valueOf(allExercises.get(position).getBreaks()))) {
                        changeMade = true;
                        allExercises.get(position).setBreaks(Integer.parseInt(edtBreaks.getText().toString()));
                    }
                    if (edtIso.getSelectedItem().toString().equals(getResources().getString(R.string.yes))) {
                        allExercises.get(position).setTempo(9999);
                        changeMade = true;
                    } else if (edtTempo.getText().toString().isEmpty()) {
                        allExercises.get(position).setTempo(0);
                        changeMade = true;
                    } else if (edtTempo.getText().toString().length() == 4) {
                        allExercises.get(position).setTempo(Integer.parseInt(edtTempo.getText().toString()));
                        changeMade = true;

                    }
                    if (edtMuscleGroupE.getText().toString().isEmpty()) {
                        allExercises.get(position).setMuscleGroup("");
                        allExercises.get(position).setMuscleGroupSecondary("");
                        changeMade = true;
                    } else {
                        if (!edtMuscleGroupE.getText().toString().equals(allExercises.get(position).getMuscleGroup())) {
                            changeMade = true;
                            allExercises.get(position).setMuscleGroup(edtMuscleGroupE.getText().toString());
                        }
                        if (!edtMuscleGroupSecondaryE.getText().toString().equals(allExercises.get(position).getMuscleGroupSecondary())) {
                            changeMade = true;
                            allExercises.get(position).setMuscleGroupSecondary(edtMuscleGroupSecondaryE.getText().toString());
                        }
                    }
                    if (!edtSets.getSelectedItem().toString().equals(String.valueOf(allExercises.get(position).getSets()))) {
                        changeMade = true;
                        allExercises.get(position).setSets(Integer.parseInt(edtSets.getSelectedItem().toString()));
                    }
                    if (changeMade) {
                        Utils.getInstance(AddExerciseActivity.this).updateWorkoutsExercises(workout, allExercises);
                    }
                    finish();
                } else {

                    if (edtExerciseName.getText().toString().isEmpty()) {
                        Toast.makeText(AddExerciseActivity.this, R.string.nameIsRequired, Toast.LENGTH_SHORT).show();
                        imgExerciseName.setImageResource(R.drawable.ic_hexagon_double_vertical_red);
                        shake(imgExerciseName);
                    } else if (edtBreaks.getText().toString().isEmpty()) {
                        Toast.makeText(AddExerciseActivity.this, R.string.breakTimeIsRequired, Toast.LENGTH_SHORT).show();
                        imgBreaks.setImageResource(R.drawable.ic_hexagon_double_vertical_red);
                        shake(imgBreaks);
                    } else if (Integer.parseInt(edtBreaks.getText().toString()) == 0) {
                        Toast.makeText(AddExerciseActivity.this, R.string.breakTimeNotZero, Toast.LENGTH_SHORT).show();
                        imgBreaks.setImageResource(R.drawable.ic_hexagon_double_vertical_red);
                        shake(imgBreaks);
                    } else if (!edtTempo.getText().toString().isEmpty() && edtTempo.getText().toString().length() < 4 && edtIso.getSelectedItem().toString().equals("No")) {
                        Toast.makeText(AddExerciseActivity.this, R.string.tempoDigits, Toast.LENGTH_SHORT).show();
                        imgTempo.setImageResource(R.drawable.ic_hexagon_double_vertical_red);
                        shake(imgTempo);
                    } else {
                        String exerciseName = edtExerciseName.getText().toString();
                        String muscleGroupE = edtMuscleGroupE.getText().toString();
                        String muscleGroupSecondaryE = "";
                        if (edtMuscleGroupSecondaryE.isEnabled()) {
                            muscleGroupSecondaryE = edtMuscleGroupSecondaryE.getText().toString();
                        }
                        int breaks = Integer.parseInt(edtBreaks.getText().toString());
                        int sets = Integer.parseInt(edtSets.getSelectedItem().toString());
                        int tempo = 0;
                        if(edtIso.getSelectedItem().toString().equals(R.string.yes)){
                            tempo=9999;
                        }
                        else if (!edtTempo.getText().toString().isEmpty()) {
                            tempo = Integer.parseInt(edtTempo.getText().toString());
                        }
                        Exercise exercise = new Exercise(5, exerciseName, sets, breaks, muscleGroupE, muscleGroupSecondaryE, tempo);
//                        Toast.makeText(AddExerciseActivity.this, exercise.toString(), Toast.LENGTH_SHORT).show();
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra(NEW_EXERCISE_KEY, exercise);
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
}

package com.example.gymbuddy.view

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.gymbuddy.R
import com.example.gymbuddy.databinding.ActivityAddWorkoutBinding
import com.example.gymbuddy.helpers.Helpers
import com.example.gymbuddy.helpers.Utils
import com.example.gymbuddy.model.Workout
import java.util.*
import kotlin.collections.ArrayList

class AddWorkoutActivity : AppCompatActivity() {
    private var isEdited = false
    private var changeMade = false
    private lateinit var allWorkouts: ArrayList<Workout>
    private var position = 0
    private lateinit var binding: ActivityAddWorkoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_GymBuddy)
        super.onCreate(savedInstanceState)
        binding = ActivityAddWorkoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Helpers.setupActionBar(getString(R.string.addNewWorkout), "", supportActionBar, this)
        binding.edtWorkoutName.requestFocus()
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
        binding.tilMuscleGroupSecondary.isEnabled = false
        binding.edtMuscleGroup.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                binding.tilMuscleGroupSecondary.isEnabled = s.isNotEmpty()
            }

            override fun afterTextChanged(s: Editable) {}
        })
        if (intent != null) {
            if (intent.getSerializableExtra(Helpers.WORKOUTS_KEY) != null) {
                allWorkouts = intent.getSerializableExtra(Helpers.WORKOUTS_KEY) as ArrayList<Workout>? ?: ArrayList()
                position = intent.getIntExtra(Helpers.POSITION_KEY, 0)
                isEdited = true
                Helpers.setupActionBar(getString(R.string.editing) + " " + allWorkouts[position].name, "", supportActionBar, this)
                binding.edtWorkoutName.setText(allWorkouts[position].name)
                binding.edtWorkoutName.setSelection(binding.edtWorkoutName.text?.length ?: 0)
                binding.edtMuscleGroup.setText(allWorkouts[position].muscleGroup)
                binding.edtMuscleGroupSecondary.setText(allWorkouts[position].muscleGroupSecondary)
                binding.btnAdd.setText(R.string.save)
                binding.edtWorkoutType.setText(allWorkouts[position].type, false)
            }else{
                binding.edtWorkoutType.setText("---", false)
            }
        }
        val arrayAdapter = ArrayAdapter(this, R.layout.dropdown_item, resources.getStringArray(R.array.workoutTypes))
        binding.edtWorkoutType.setAdapter(arrayAdapter)
        binding.btnAdd.setOnClickListener {
            if (isEdited) {
                if (binding.edtWorkoutName.text.toString().isNotEmpty() &&
                    binding.edtWorkoutName.text.toString() != allWorkouts[position].name
                ) {
                    changeMade = true
                    allWorkouts[position].name = binding.edtWorkoutName.text.toString()
                }
                if (binding.edtWorkoutType.text.toString() != allWorkouts[position].type) {
                    changeMade = true
                    allWorkouts[position].type = binding.edtWorkoutType.text.toString()
                }
                if (binding.edtMuscleGroup.text.toString().isEmpty()) {
                    allWorkouts[position].muscleGroup = ""
                    allWorkouts[position].muscleGroupSecondary = ""
                    changeMade = true
                } else {
                    if (binding.edtMuscleGroup.text.toString() != allWorkouts[position].muscleGroup) {
                        changeMade = true
                        allWorkouts[position].muscleGroup = binding.edtMuscleGroup.text.toString()
                    }
                    if (binding.edtMuscleGroupSecondary.text.toString() != allWorkouts[position].muscleGroupSecondary) {
                        changeMade = true
                        allWorkouts[position].muscleGroupSecondary = binding.edtMuscleGroupSecondary.text.toString()
                    }
                }
                if (changeMade) {
                    Utils.getInstance(this@AddWorkoutActivity).updateWorkouts(allWorkouts)
                }
                finish()
            } else {
                if (binding.edtWorkoutName.text.toString().isEmpty()) {
                    Toast.makeText(this@AddWorkoutActivity, R.string.nameIsRequired, Toast.LENGTH_SHORT).show()
                    binding.imgWorkoutName.setImageResource(R.drawable.ic_hexagon_single_red)
                    shake(binding.imgWorkoutName)
                } else {
                    val workoutName = binding.edtWorkoutName.text.toString()
                    val workoutType = binding.edtWorkoutType.text.toString()
                    val muscleGroup = binding.edtMuscleGroup.text.toString()
                    var muscleGroupSecondary = ""
                    if (binding.edtMuscleGroupSecondary.isEnabled) {
                        muscleGroupSecondary = binding.edtMuscleGroupSecondary.text.toString()
                    }
                    val workout = Workout(5, workoutName, workoutType, muscleGroup, muscleGroupSecondary, 0)
                    //                        Toast.makeText(AddWorkoutActivity.this, workout.toString(), Toast.LENGTH_SHORT).show();
                    val returnIntent = Intent()
                    returnIntent.putExtra(Helpers.NEW_WORKOUT_KEY, workout)
                    setResult(RESULT_OK, returnIntent)
                    val imm1: InputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm1.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
                    finish()
                }
            }
        }
    }

    private fun shake(v: View?) {
        ObjectAnimator
            .ofFloat(v, "translationX", 0f, 25f, -25f, 25f, -25f, 15f, -15f, 6f, -6f, 0f)
            .setDuration(200)
            .start()
    }
}
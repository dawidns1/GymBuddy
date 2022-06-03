package com.example.gymbuddy.view

import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.gymbuddy.R
import com.example.gymbuddy.databinding.ActivityAddExerciseBinding
import com.example.gymbuddy.helpers.Helpers
import com.example.gymbuddy.model.Exercise
import com.example.gymbuddy.model.Workout
import java.util.*
import kotlin.collections.ArrayList

class AddExerciseActivity : AppCompatActivity() {
    private lateinit var allExercises: ArrayList<Exercise>
    var position = 0
    private var isEdited = false
    private var changeMade = false
    private var workout: Workout? = null
    private lateinit var binding: ActivityAddExerciseBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_GymBuddy)
        super.onCreate(savedInstanceState)
        binding = ActivityAddExerciseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Helpers.setupActionBar(getString(R.string.addNewExercise), "", supportActionBar, this)

        binding.edtExerciseName.requestFocus()
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
        binding.tilMuscleGroupSecondaryE.isEnabled = false

        val formatter = NumberPicker.Formatter { value: Int ->
            val temp = value * 5
            "" + temp
        }
        binding.btnSelect.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.breaks_dialog, null)
            val d = AlertDialog.Builder(this@AddExerciseActivity, R.style.DefaultAlertDialogTheme).apply {
                setTitle(R.string.breakDialog)
                setMessage(R.string.breakDialogMessage)
                setView(dialogView)
                setIcon(R.drawable.ic_timer)
            }
            val numberPicker = dialogView.findViewById<NumberPicker>(R.id.dialogNumberPicker).apply {
                setFormatter(formatter)
                maxValue = 60
                minValue = 4
                wrapSelectorWheel = false
                value = 24
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                numberPicker.textSize = 80f
            }
            try {
                val field = NumberPicker::class.java.getDeclaredField("mInputText")
                field.isAccessible = true
                val inputText = field[numberPicker] as EditText
                inputText.visibility = View.INVISIBLE
            } catch (e: Exception) {
                e.printStackTrace()
            }
            d.setPositiveButton(R.string.ok) { _: DialogInterface?, _: Int -> binding.edtTxtBreaks.setText((numberPicker.value * 5).toString()) }
            d.setNegativeButton(R.string.cancel, null)
            d.create().show()

        }

        binding.edtIso.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(s: CharSequence, p1: Int, p2: Int, p3: Int) {
//                if(s.toString() == resources.getString(R.string.yes)){
//                    binding.tilTempo.isEnabled=false
//                }else{
//                    binding.tilTempo.isEnabled=false
//                }
            }
            override fun afterTextChanged(s: Editable) {
                binding.tilTempo.isEnabled = s.toString() != resources.getString(R.string.yes)
            }
        })
//        binding.edtIso.onItemSelectedListener = object : OnItemSelectedListener {
//            override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
//                binding.tilTempo.isEnabled = position != 1
//            }
//
//            override fun onNothingSelected(parent: AdapterView<*>?) {}
//        }
        binding.edtMuscleGroupE.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                binding.tilMuscleGroupSecondaryE.isEnabled = s.isNotEmpty()
            }
            override fun afterTextChanged(s: Editable) {}
        })
        if (intent != null) {
            if (intent.getSerializableExtra(Helpers.EXERCISES_KEY) != null) {
                allExercises = intent.getSerializableExtra(Helpers.EXERCISES_KEY) as ArrayList<Exercise>? ?: ArrayList()
                position = intent.getIntExtra(Helpers.POSITION_KEY, 0)
                workout = intent.getSerializableExtra(Helpers.WORKOUT_KEY) as Workout?
                isEdited = true
                Helpers.setupActionBar(getString(R.string.editing) + " " + allExercises[position].name, "", supportActionBar, this)
                binding.edtExerciseName.setText(allExercises[position].name)
                binding.edtExerciseName.setSelection(binding.edtExerciseName.text?.length ?: 0)
                binding.edtMuscleGroupE.setText(allExercises[position].muscleGroup)
                binding.edtMuscleGroupSecondaryE.setText(allExercises[position].muscleGroupSecondary)
                if (allExercises[position].tempo == 9999) {
                    binding.edtTempo.text = null
                    binding.tilTempo.isEnabled = false
                    binding.edtIso.setText(R.string.yes)
                } else {
                    binding.edtIso.setText(R.string.no)
                    if (allExercises[position].tempo == 0) {
                        binding.edtTempo.text = null
                    } else {
                        binding.edtTempo.setText(allExercises[position].tempo.toString())
                    }
                }
                binding.edtTxtBreaks.setText(allExercises[position].breaks.toString())
                binding.btnAddNewExercise.setText(R.string.save)
                binding.edtSets.setText(allExercises[position].sets.toString())
            }
        }

        val isoArrayAdapter = ArrayAdapter(this, R.layout.dropdown_item, resources.getStringArray(R.array.isometric))
        binding.edtIso.setAdapter(isoArrayAdapter)
        val setsArrayAdapter = ArrayAdapter(this, R.layout.dropdown_item, resources.getStringArray(R.array.setsNumber))
        binding.edtSets.setAdapter(setsArrayAdapter)

        binding.btnAddNewExercise.setOnClickListener {
            if (isEdited) {
                val editedExercise = allExercises[position]
                if (binding.edtExerciseName.text.toString().isNotEmpty() &&
                    binding.edtExerciseName.text.toString() != editedExercise.name
                ) {
                    changeMade = true
                    editedExercise.name = binding.edtExerciseName.text.toString()
                }
                if (binding.edtTxtBreaks.text.toString().isNotEmpty() && binding.edtTxtBreaks.text.toString().toInt() != 0 &&
                    binding.edtTxtBreaks.text.toString() != editedExercise.breaks.toString()
                ) {
                    changeMade = true
                    editedExercise.breaks = binding.edtTxtBreaks.text.toString().toInt()
                }
                when {
                    binding.edtIso.text.toString() == resources.getString(R.string.yes) -> {
                        editedExercise.tempo = 9999
                        changeMade = true
                    }
                    binding.edtTempo.text.toString().isEmpty() -> {
                        editedExercise.tempo = 0
                        changeMade = true
                    }
                    binding.edtTempo.text.toString().length == 4 -> {
                        editedExercise.tempo = binding.edtTempo.text.toString().toInt()
                        changeMade = true
                    }
                }
                if (binding.edtMuscleGroupE.text.toString().isEmpty()) {
                    editedExercise.muscleGroup = ""
                    editedExercise.muscleGroupSecondary = ""
                    changeMade = true
                } else {
                    if (binding.edtMuscleGroupE.text.toString() != editedExercise.muscleGroup) {
                        changeMade = true
                        editedExercise.muscleGroup = binding.edtMuscleGroupE.text.toString()
                    }
                    if (binding.edtMuscleGroupSecondaryE.text.toString() != editedExercise.muscleGroupSecondary) {
                        changeMade = true
                        editedExercise.muscleGroupSecondary = binding.edtMuscleGroupSecondaryE.text.toString()
                    }
                }
                if (binding.edtSets.text.toString() != editedExercise.sets.toString()) {
                    changeMade = true
                    editedExercise.sets = binding.edtSets.text.toString().toInt()
                }
                val returnIntent = Intent()
                setResult(RESULT_OK, returnIntent)
                if (changeMade) {
                    returnIntent.putExtra(Helpers.EDITED_EXERCISE_KEY, editedExercise)
                }
                val imm1: InputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm1.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
                finish()
            } else {
                if (binding.edtExerciseName.text.toString().isEmpty()) {
                    Toast.makeText(this@AddExerciseActivity, R.string.nameIsRequired, Toast.LENGTH_SHORT).show()
                    binding.imgExerciseName.setImageResource(R.drawable.ic_hexagon_double_vertical_red)
                    shake(binding.imgExerciseName)
                } else if (binding.edtTxtBreaks.text.toString().isEmpty()) {
                    Toast.makeText(this@AddExerciseActivity, R.string.breakTimeIsRequired, Toast.LENGTH_SHORT).show()
                    binding.imgBreaks.setImageResource(R.drawable.ic_hexagon_double_vertical_red)
                    shake(binding.imgBreaks)
                } else if (binding.edtTxtBreaks.text.toString().toInt() == 0) {
                    Toast.makeText(this@AddExerciseActivity, R.string.breakTimeNotZero, Toast.LENGTH_SHORT).show()
                    binding.imgBreaks.setImageResource(R.drawable.ic_hexagon_double_vertical_red)
                    shake(binding.imgBreaks)
                } else if (binding.edtTempo.text.toString().isNotEmpty() && binding.edtTempo.text.toString().length < 4 && binding.edtIso.text.toString() == resources.getString(R.string.no)) {
                    Toast.makeText(this@AddExerciseActivity, R.string.tempoDigits, Toast.LENGTH_SHORT).show()
                    binding.imgTempo.setImageResource(R.drawable.ic_hexagon_double_vertical_red)
                    shake(binding.imgTempo)
                } else {
                    val exerciseName = binding.edtExerciseName.text.toString()
                    val muscleGroupE = binding.edtMuscleGroupE.text.toString()
                    var muscleGroupSecondaryE = ""
                    if (binding.edtMuscleGroupSecondaryE.isEnabled) {
                        muscleGroupSecondaryE = binding.edtMuscleGroupSecondaryE.text.toString()
                    }
                    val breaks = binding.edtTxtBreaks.text.toString().toInt()
                    val sets = binding.edtSets.text.toString().toInt()
                    var tempo = 0
                    if (binding.edtIso.text.toString() == resources.getString(R.string.yes)) {
                        tempo = 9999
                    } else if (binding.edtTempo.text.toString().isNotEmpty()) {
                        tempo = binding.edtTempo.text.toString().toInt()
                    }
                    val exercise = Exercise(5, exerciseName, sets, breaks, muscleGroupE, muscleGroupSecondaryE, tempo)
                    val returnIntent = Intent()
                    returnIntent.putExtra(Helpers.NEW_EXERCISE_KEY, exercise)
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
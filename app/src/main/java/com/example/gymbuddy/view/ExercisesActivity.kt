package com.example.gymbuddy.view

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gymbuddy.R
import com.example.gymbuddy.databinding.ActivityExercisesBinding
import com.example.gymbuddy.helpers.Helpers
import com.example.gymbuddy.helpers.Helpers.disableEFABClickable
import com.example.gymbuddy.helpers.Helpers.enableEFABClickable
import com.example.gymbuddy.helpers.Helpers.handleNativeAds
import com.example.gymbuddy.helpers.Helpers.setupActionBar
import com.example.gymbuddy.helpers.MyItemAnimator
import com.example.gymbuddy.helpers.Utils.INSTANCE.getInstance
import com.example.gymbuddy.model.Exercise
import com.example.gymbuddy.model.Workout
import com.example.gymbuddy.recyclerViewAdapters.ExercisesRVAdapter
import java.util.*

class ExercisesActivity : AppCompatActivity() {
    private lateinit var incomingWorkout: Workout
    private var exercises = ArrayList<Exercise>()
    private var exercisesImporting = ArrayList<Exercise>()
    private var workouts: ArrayList<Workout>? = ArrayList()
    private lateinit var exercisesRVAdapter: ExercisesRVAdapter
    private var selectedWorkoutID = 0
    private var selectedExercise = 0
    private var selectedOptions = 0
    private lateinit var workoutIDs: IntArray
    private lateinit var areChecked: BooleanArray
    private var isScrolled = false
    private lateinit var exercisesForSuperset: Array<String>
    private val moveParameters = intArrayOf(0, 0, 0)
    private val removedIDs = ArrayList<Int>()
    private var workoutEdited = false
    private lateinit var binding: ActivityExercisesBinding
    private var addExerciseLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result -> onAddExerciseResult(result) }
    private var editExerciseLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result -> onEditExerciseResult(result) }

    private fun onAddExerciseResult(result: ActivityResult?) {
        result?.let {
            if (it.resultCode == RESULT_OK) {
                addNewExercise(result.data!!)
                exercisesRVAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun onEditExerciseResult(result: ActivityResult?) {
        result?.let {
            if (it.resultCode == RESULT_OK) {
                workoutEdited = true
                editExistingExercise(it.data!!)
                exercisesRVAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun addNewExercise(data: Intent) {
        val newExercise = data.getSerializableExtra(Helpers.NEW_EXERCISE_KEY) as Exercise?
        newExercise!!.id = getInstance(this).exercisesId
        getInstance(this).exercisesId = newExercise.id + 1
        exercises.add(newExercise)
        incomingWorkout.exercises = exercises
        getInstance(this).addExerciseToWorkout(incomingWorkout, newExercise)
        if (!binding.btnStartWorkout.isShown) binding.btnStartWorkout.visibility = View.VISIBLE
        if (exercises.size > 8) {
            disableEFABClickable(binding.btnAddExercise, this)
            Toast.makeText(this, resources.getString(R.string.maximumExerciseNumberReached), Toast.LENGTH_SHORT).show()
        }
    }

    private fun editExistingExercise(data: Intent) {
//        Toast.makeText(this, "here", Toast.LENGTH_SHORT).show()
        val editedExercise = data.getSerializableExtra(Helpers.EDITED_EXERCISE_KEY) as Exercise?
//        Toast.makeText(this, "${editedExercise?.name}", Toast.LENGTH_SHORT).show()
        for (i in exercises.indices) {
            if (exercises[i].id == editedExercise!!.id) {
                val tempSets = exercises[i].sets
                exercises.removeAt(i)
                exercises.add(i, editedExercise)
                if (exercises[i].sets != tempSets) {
                    if (exercises[i].superSet == 1) exercises[i + 1].sets = exercises[i].sets else if (exercises[i].superSet == 2) handleDisableSuperset(i, false)
                }
            }
        }
    }

    //TODO sprawdzić co się wysrywa przy usuwaniu wszystkich sesji, czemu nie ma update

    override fun onResume() {
        super.onResume()
        if (!workoutEdited) {
            workouts = getInstance(this).allWorkouts
            if (null != workouts) {
                for (w in workouts!!) {
                    if (w.id == incomingWorkout.id) {
                        incomingWorkout.exercises = w.exercises
                        exercises=incomingWorkout.exercises
                        incomingWorkout.state=w.state
                        exercisesRVAdapter.exercises = exercises
                        if (w.state[0] == 0 && !isScrolled) {
                            binding.btnResumeWorkout.visibility = View.VISIBLE
                        } else {
                            binding.btnResumeWorkout.visibility = View.GONE
                        }
                    }
                }
                exercisesRVAdapter.notifyDataSetChanged()
            }
        }
        workoutEdited=false
//        Toast.makeText(this, "${incomingWorkout.state[0]}${incomingWorkout.state[1]}${incomingWorkout.state[2]} EA", Toast.LENGTH_SHORT).show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.exercises_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.submenuDeleteAllExercises -> {
                AlertDialog.Builder(this, R.style.DefaultAlertDialogTheme)
                    .setTitle(resources.getString(R.string.deleting) + " " + incomingWorkout.name)
                    .setMessage(R.string.sureDelete)
                    .setIcon(R.drawable.ic_delete)
                    .setPositiveButton(R.string.yes) { _: DialogInterface?, _: Int ->
                        exercises = ArrayList()
                        exercisesRVAdapter.exercises = exercises
                        exercisesRVAdapter.notifyDataSetChanged()
                        incomingWorkout.exercises = exercises
                        incomingWorkout.exerciseNumber = 0
                        incomingWorkout.state = intArrayOf(1, 1, 1)
                        binding.btnResumeWorkout.visibility = View.GONE
                        binding.btnStartWorkout.visibility = View.GONE
                    }
                    .setNegativeButton(R.string.no, null)
                    .show()
                true
            }
            R.id.menuItem0 -> false
            else ->
                super.onOptionsItemSelected(item)
        }
    }

    override fun onPause() {
        getInstance(this).updateWorkoutsExercises(incomingWorkout, exercises)
        super.onPause()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_GymBuddy)
        super.onCreate(savedInstanceState)
        binding = ActivityExercisesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        incomingWorkout = intent.getSerializableExtra(Helpers.EXERCISES_KEY) as Workout
        exercises = incomingWorkout.exercises

        handleNativeAds(binding.exercisesAdTemplate, this, Helpers.AD_ID_EXERCISES_NATIVE, null)
        if (exercises.size > 8) disableEFABClickable(binding.btnAddExercise, this)
        if (exercises.isEmpty()) {
            Toast.makeText(this, R.string.noExercises, Toast.LENGTH_SHORT).show()
            binding.btnStartWorkout.visibility = View.GONE
            binding.btnResumeWorkout.visibility = View.GONE
        }
        if (incomingWorkout.state[0] == 0) {
            binding.btnResumeWorkout.visibility = View.VISIBLE
        }
        setupActionBar(incomingWorkout.name, "", supportActionBar, this)
        exercisesRVAdapter = ExercisesRVAdapter(
            this,
            { position: Int, view: View -> onMenuClick(position, view) },
            { position: Int -> onParentClick(position, incomingWorkout) },
            exercises
        ).apply {
            setHasStableIds(true)
        }
        binding.exercisesRV.apply {
            this.adapter = exercisesRVAdapter
            layoutManager = LinearLayoutManager(this@ExercisesActivity)
            itemAnimator = MyItemAnimator()
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (dy > 0) {
                        if (binding.btnAddExercise.isShown) {
                            binding.btnAddExercise.visibility = View.GONE
                        }
                        if (binding.btnStartWorkout.isShown) {
                            binding.btnStartWorkout.visibility = View.GONE
                        }
                        if (binding.btnResumeWorkout.isShown) {
                            binding.btnResumeWorkout.visibility = View.GONE
                        }
                        isScrolled = true
                    } else if (dy < 0) {
                        if (!binding.btnAddExercise.isShown) {
                            binding.btnAddExercise.show()
                        }
                        if (!binding.btnStartWorkout.isShown) {
                            binding.btnStartWorkout.show()
                        }
                        if (!binding.btnResumeWorkout.isShown && incomingWorkout.state[0] == 0) {
                            binding.btnResumeWorkout.show()
                        }
                        isScrolled = false
                    }
                }
            })
        }
        val itemTouchHelper = ItemTouchHelper(simpleCallback)
        itemTouchHelper.attachToRecyclerView(binding.exercisesRV)
        binding.btnAddExercise.setOnClickListener {
            if (exercises.size > 8) {
                Toast.makeText(this@ExercisesActivity, resources.getString(R.string.cannotAddMoreExercises), Toast.LENGTH_SHORT).show()
            } else {
                val i = Intent(this@ExercisesActivity, AddExerciseActivity::class.java)
                addExerciseLauncher.launch(i)
            }
        }
        binding.btnAddExercise.setOnLongClickListener {
            if (exercises.size > 8) {
                Toast.makeText(this@ExercisesActivity, resources.getString(R.string.cannotAddMoreExercises), Toast.LENGTH_SHORT).show()
            } else {
                handleImportExercises()
            }
            false
        }
        binding.btnStartWorkout.setOnClickListener {
            val intentStart = Intent(this@ExercisesActivity, NewSessionActivity::class.java).apply {
                putExtra(Helpers.NEW_SESSION_KEY, incomingWorkout)
            }
            startActivity(intentStart)
        }
        binding.btnResumeWorkout.setOnClickListener {
            val intentResume = Intent(this@ExercisesActivity, NewSessionActivity::class.java).apply {
                putExtra(Helpers.NEW_SESSION_KEY, incomingWorkout)
                putExtra(Helpers.RESUMED_KEY, true)
            }
            startActivity(intentResume)
        }
    }

    private fun handleImportExercises() {
        if (workouts!!.size == 1) {
            Toast.makeText(this, R.string.noOtherWorkouts, Toast.LENGTH_SHORT).show()
        } else {
            val workoutNames = arrayOfNulls<String>(workouts!!.size - 1)
            workoutIDs = IntArray(workouts!!.size - 1)
            var i = 0
            for (w in workouts!!) {
                if (w.id != incomingWorkout.id) {
                    workoutNames[i] = w.name
                    workoutIDs[i] = w.id
                    i++
                }
            }
            selectedWorkoutID = workoutIDs[0]
            AlertDialog.Builder(this, R.style.DefaultAlertDialogTheme)
                .setTitle(R.string.loadExercises)
                .setIcon(R.drawable.ic_load)
                .setSingleChoiceItems(workoutNames, 0) { _: DialogInterface?, which: Int ->
                    selectedWorkoutID = workoutIDs[which]
                }
                .setPositiveButton(R.string.next) { _: DialogInterface?, _: Int ->
                    handleShowExercises()
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }
    }

    private fun handleShowExercises() {
        selectedExercise = 0
        for (w in workouts!!) {
            if (w.id == selectedWorkoutID) exercisesImporting = w.exercises
        }
        if (exercisesImporting.isEmpty()) {
            Toast.makeText(this, R.string.noExercisesToLoad, Toast.LENGTH_SHORT).show()
        } else {
            val exerciseNames = arrayOfNulls<String>(exercisesImporting.size)
            areChecked = BooleanArray(exercisesImporting.size)
            for (i in exercisesImporting.indices) {
                exerciseNames[i] = exercisesImporting[i].name
                areChecked[i] = false
            }
            AlertDialog.Builder(this, R.style.DefaultAlertDialogTheme)
                .setTitle(R.string.selectExercise)
                .setIcon(R.drawable.ic_load)
                .setMultiChoiceItems(exerciseNames, areChecked) { _: DialogInterface?, which: Int, isChecked: Boolean -> areChecked[which] = isChecked }
                .setPositiveButton(R.string.next) { _: DialogInterface?, _: Int -> handleShowOptions() }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }
    }

    private fun handleShowOptions() {
        selectedOptions = 0
        AlertDialog.Builder(this, R.style.DefaultAlertDialogTheme)
            .setTitle(R.string.selectOptions)
            .setIcon(R.drawable.ic_load)
            .setSingleChoiceItems(arrayOf(getString(R.string.overwrite), getString(R.string.add)), 0) { _: DialogInterface?, which: Int -> selectedOptions = which }
            .setPositiveButton(R.string.load) { _: DialogInterface?, _: Int -> handleImporting() }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun handleImporting() {
        if (selectedOptions == 0) {
            exercises = ArrayList()
            exercisesRVAdapter.exercises = exercises
        }
        for (i in exercisesImporting.indices) {
            if (areChecked[i] && exercises.size < 9) {
                exercises.add(exercisesImporting[i])
            }
        }
        exercisesRVAdapter.notifyDataSetChanged()
    }

    private var simpleCallback: ItemTouchHelper.SimpleCallback = object : ItemTouchHelper.SimpleCallback(
        ItemTouchHelper.UP or ItemTouchHelper.DOWN or
                ItemTouchHelper.START or ItemTouchHelper.END, 0
    ) {
        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            val fromPosition = viewHolder.adapterPosition
            val toPosition = target.adapterPosition
            when {
                exercises[fromPosition].superSet == 1 -> {
                    removedIDs.add(exercises[fromPosition].id)
                    removedIDs.add(exercises[fromPosition + 1].id)
                    exercises[fromPosition].superSet = 0
                    exercises[fromPosition + 1].superSet = 0
                }
                exercises[fromPosition].superSet == 2 -> {
                    removedIDs.add(exercises[fromPosition].id)
                    removedIDs.add(exercises[fromPosition - 1].id)
                    exercises[fromPosition].superSet = 0
                    exercises[fromPosition - 1].superSet = 0
                }
                exercises[toPosition].superSet == 1 -> {
                    removedIDs.add(exercises[toPosition].id)
                    removedIDs.add(exercises[toPosition + 1].id)
                    exercises[fromPosition].superSet = 0
                    exercises[toPosition + 1].superSet = 0
                }
                exercises[toPosition].superSet == 2 -> {
                    removedIDs.add(exercises[toPosition].id)
                    removedIDs.add(exercises[toPosition - 1].id)
                    exercises[toPosition].superSet = 0
                    exercises[toPosition - 1].superSet = 0
                }
            }
            Collections.swap(exercises, fromPosition, toPosition)
            exercisesRVAdapter.notifyItemMoved(fromPosition, toPosition)
            incomingWorkout.state = intArrayOf(1, 1, 1)
            binding.btnResumeWorkout.visibility = View.GONE
            return false
        }

        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
            for (id in removedIDs) {
                for (i in exercises.indices) {
                    if (exercises[i].id == id) {
                        exercisesRVAdapter.notifyItemChanged(i)
                    }
                }
            }
            removedIDs.clear()
            super.clearView(recyclerView, viewHolder)
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
    }

    private fun onMenuClick(positionRV: Int, v: View) {
        val popupMenu = PopupMenu(this, v)
        popupMenu.inflate(R.menu.popup_menu_exercise)
        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.menuEditE -> {
                    val intent = Intent(this@ExercisesActivity, AddExerciseActivity::class.java).apply {
                        putExtra(Helpers.EXERCISES_KEY, exercises)
                        putExtra(Helpers.POSITION_KEY, positionRV)
                        putExtra(Helpers.WORKOUT_KEY, incomingWorkout)
                    }
                    editExerciseLauncher.launch(intent)
                    return@setOnMenuItemClickListener true
                }
                R.id.menuDeleteE -> {
                    AlertDialog.Builder(this@ExercisesActivity, R.style.DefaultAlertDialogTheme)
                        .setTitle(R.string.deletingExercise)
                        .setMessage(R.string.sureDeleteThis)
                        .setIcon(R.drawable.ic_delete)
                        .setPositiveButton(R.string.yes) { _: DialogInterface?, _: Int ->
                            if (exercises[positionRV].superSet != 0) {
                                handleDisableSuperset(positionRV, true)
                            }
                            binding.btnResumeWorkout.visibility = View.GONE
                            exercises.remove(exercises[positionRV])
                            exercisesRVAdapter.notifyItemRemoved(positionRV)
                            if (exercises.isEmpty()) {
                                binding.btnStartWorkout.visibility = View.GONE
                                binding.btnResumeWorkout.visibility = View.GONE
                            }
                            if (exercises.size == 8) {
                                enableEFABClickable(binding.btnAddExercise, this@ExercisesActivity)
                            }
                        }
                        .setNegativeButton(R.string.no, null)
                        .show()
                    return@setOnMenuItemClickListener true
                }
                R.id.menuSuperset -> {
                    if (exercises[positionRV].superSet != 0) {
                        AlertDialog.Builder(this@ExercisesActivity, R.style.DefaultAlertDialogTheme)
                            .setTitle(R.string.disablingSuperset)
                            .setMessage(R.string.disablingSupersetMsg)
                            .setIcon(R.drawable.ic_superset)
                            .setPositiveButton(R.string.yes) { _: DialogInterface?, _: Int -> handleDisableSuperset(positionRV, true) }
                            .setNegativeButton(R.string.no, null)
                            .show()
                    } else {
                        var available = true
                        if (exercises.size == 1 || positionRV == 0 && exercises[1].superSet != 0 ||
                            positionRV == exercises.size - 1 && exercises[exercises.size - 2].superSet != 0
                        ) {
                            Toast.makeText(this@ExercisesActivity, R.string.noAvailableExercises, Toast.LENGTH_SHORT).show()
                            available = false
                        } else if (positionRV == 0) {
                            exercisesForSuperset = arrayOf(exercises[1].name)
                        } else if (positionRV == exercises.size - 1) {
                            exercisesForSuperset = arrayOf(exercises[exercises.size - 2].name)
                        } else if (exercises[positionRV - 1].superSet != 0 && exercises[positionRV + 1].superSet != 0) {
                            Toast.makeText(this@ExercisesActivity, R.string.noAvailableExercises, Toast.LENGTH_SHORT).show()
                            available = false
                        } else if (exercises[positionRV - 1].superSet != 0) {
                            exercisesForSuperset = arrayOf(exercises[positionRV + 1].name)
                        } else if (exercises[positionRV + 1].superSet != 0) {
                            exercisesForSuperset = arrayOf(exercises[positionRV - 1].name)
                        } else {
                            exercisesForSuperset = arrayOf(exercises[positionRV - 1].name, exercises[positionRV + 1].name)
                        }
                        if (available) {
                            AlertDialog.Builder(this@ExercisesActivity, R.style.DefaultAlertDialogTheme)
                                .setTitle(exercises[positionRV].name + " " + resources.getString(R.string.supersetWith))
                                .setIcon(R.drawable.ic_superset)
                                .setSingleChoiceItems(exercisesForSuperset, 0) { _: DialogInterface?, which: Int -> selectedExercise = which }
                                .setPositiveButton(R.string.ok) { _: DialogInterface?, _: Int ->
                                    if (positionRV == 0) {
                                        exercises[positionRV].superSet = 1
                                        exercises[1].superSet = 2
                                        exercises[1].sets = exercises[positionRV].sets
                                        exercisesRVAdapter.notifyItemChanged(1)
                                    } else if (positionRV == exercises.size - 1) {
                                        exercises[positionRV].superSet = 2
                                        exercises[exercises.size - 2].sets = exercises[positionRV].sets
                                        exercises[exercises.size - 2].superSet = 1
                                        exercisesRVAdapter.notifyItemChanged(exercises.size - 2)
                                    } else {
                                        if (selectedExercise == 0) {
                                            exercises[positionRV].superSet = 2
                                            exercises[positionRV].sets = exercises[positionRV - 1].sets
                                            exercises[positionRV - 1].superSet = 1
                                            exercisesRVAdapter.notifyItemChanged(positionRV - 1)
                                        } else {
                                            exercises[positionRV].superSet = 1
                                            exercises[positionRV + 1].superSet = 2
                                            exercises[positionRV + 1].sets = exercises[positionRV].sets
                                            exercisesRVAdapter.notifyItemChanged(positionRV + 1)
                                        }
                                    }
                                    exercisesRVAdapter.notifyItemChanged(positionRV)
                                }
                                .setNegativeButton(R.string.cancel, null)
                                .show()
                        }
                    }
                    return@setOnMenuItemClickListener true
                }
                else -> return@setOnMenuItemClickListener false
            }
        }
        popupMenu.show()
    }

    private fun onParentClick(position: Int, displayedWorkout: Workout) {
        val intent = Intent(this, SessionsActivity::class.java).apply {
            putExtra(Helpers.EXERCISES_KEY, exercises)
            putExtra(Helpers.POSITION_KEY, position)
            putExtra(Helpers.WORKOUT_KEY, displayedWorkout)
        }
        startActivity(intent)
    }

    private fun handleDisableSuperset(positionRV: Int, notify: Boolean) {
        when (exercises[positionRV].superSet) {
            1 -> {
                exercises[positionRV].superSet = 0
                exercises[positionRV + 1].superSet = 0
                moveParameters[2] = positionRV + 1
                if (notify) {
                    exercisesRVAdapter.notifyItemChanged(positionRV)
                    exercisesRVAdapter.notifyItemChanged(positionRV + 1)
                }
            }
            2 -> {
                exercises[positionRV].superSet = 0
                exercises[positionRV - 1].superSet = 0
                moveParameters[2] = positionRV - 1
                if (notify) {
                    exercisesRVAdapter.notifyItemChanged(positionRV)
                    exercisesRVAdapter.notifyItemChanged(positionRV - 1)
                }
            }
            else -> {
            }
        }
//        Toast.makeText(this, "" + moveParameters[2], Toast.LENGTH_SHORT).show()
    }
}
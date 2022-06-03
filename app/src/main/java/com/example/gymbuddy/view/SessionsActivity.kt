package com.example.gymbuddy.view

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gymbuddy.R
import com.example.gymbuddy.databinding.ActivitySessionsBinding
import com.example.gymbuddy.helpers.DecimalDigitsInputFilter
import com.example.gymbuddy.helpers.Helpers
import com.example.gymbuddy.helpers.Helpers.handleNativeAds
import com.example.gymbuddy.helpers.Helpers.setup
import com.example.gymbuddy.helpers.Helpers.setupActionBar
import com.example.gymbuddy.helpers.Helpers.shake
import com.example.gymbuddy.helpers.Helpers.toPrettyString
import com.example.gymbuddy.helpers.Helpers.toggleEnabled
import com.example.gymbuddy.helpers.Helpers.toggleVisibility
import com.example.gymbuddy.helpers.Helpers.toggleVisibilityEFAB
import com.example.gymbuddy.helpers.Utils.INSTANCE.getInstance
import com.example.gymbuddy.model.Exercise
import com.example.gymbuddy.model.Session
import com.example.gymbuddy.model.Workout
import com.example.gymbuddy.recyclerViewAdapters.SessionsRVAdapter
import com.example.gymbuddy.viewModel.SessionsViewModel
import com.google.android.gms.ads.AdLoader
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class SessionsActivity : AppCompatActivity() {
    private lateinit var exercises: ArrayList<Exercise>
    private lateinit var exercisesImporting: ArrayList<Exercise>
    private lateinit var sessions: ArrayList<Session>
    private var isInputFinished = false
    private var editing = false
    private var load = FloatArray(8) { 0f }
    private var reps = IntArray(8) { 0 }
    private lateinit var adapter: SessionsRVAdapter
    private val df: DateFormat = SimpleDateFormat("yyyy-MM-dd")
    private var workouts: ArrayList<Workout>? = null
    private lateinit var workoutNames: Array<String?>
    private lateinit var exerciseNames: Array<String?>
    private var displayedWorkout: Workout? = null
    private var backed = 0
    private var inputPosition = 0
    private var adLoader: AdLoader? = null
    private lateinit var binding: ActivitySessionsBinding
    private var menuEntry: MenuItem? = null
    private lateinit var viewModel: SessionsViewModel
    private lateinit var imm: InputMethodManager
    private lateinit var exercise: Exercise
    private var rir = intArrayOf(0, 0, 0, 0, 0, 0, 0, 0)

    override fun onBackPressed() {
        if (binding.tilLoad.isShown) {
            hideViewsAndReset()
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.sessions_menu, menu)
        menuEntry = menu.getItem(0)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.submenuDeleteAllSessions -> {
                AlertDialog.Builder(this, R.style.DefaultAlertDialogTheme)
                    .setTitle(resources.getString(R.string.deletingSessions) + " " + exercises[viewModel.position.value!!].name)
                    .setMessage(R.string.deletingSessionsInfo)
                    .setPositiveButton(R.string.yes) { _: DialogInterface?, _: Int ->
                        if (sessions.isNotEmpty()) {
                            sessions.clear()
                            adapter.notifyDataSetChanged()
                            exercises[viewModel.position.value!!].sessions = sessions
                            getInstance(this@SessionsActivity).updateWorkoutsExercisesWithoutWorkout(exercises)
                        }
                    }
                    .setNegativeButton(R.string.no, null)
                    .show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_GymBuddy)
        super.onCreate(savedInstanceState)
        binding = ActivitySessionsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        viewModel = ViewModelProvider(this).get(SessionsViewModel::class.java)
        exercises = intent.getSerializableExtra(Helpers.EXERCISES_KEY) as ArrayList<Exercise>
        viewModel.setPosition(intent.getIntExtra(Helpers.POSITION_KEY, 0))
        displayedWorkout = intent.getSerializableExtra(Helpers.WORKOUT_KEY) as Workout?

        binding.rirNumberPicker.setup()

        viewModel.position.observe(this, { position ->
            exercise = exercises[position]
            sessions = exercise.sessions
            setTitleAndTempo()
            adapter.apply {
                setSessions(sessions)
                setExercise(exercise)
                notifyDataSetChanged()
            }
            binding.btnNextExercise.toggleEnabled(this, position + 1 != exercises.size)
            binding.btnPreviousExercise.toggleEnabled(this, position != 0)
            binding.btnViewChart.toggleVisibility(sessions.size >= 2)
        })

        viewModel.isInputOngoing.observe(this, { isInputOngoing ->
            if(isInputOngoing){
                if(editing){
                    binding.rirNumberPicker.value= sessions[inputPosition].rir?.get(0) ?: 3
                }else{
                    binding.rirNumberPicker.value=3
                }

            }
            menuEntry?.isEnabled = !isInputOngoing
            binding.viewDisableRV.toggleVisibility(isInputOngoing)
            if (isInputOngoing) {
                showViews()
            }
        })

        viewModel.setNo.observe(this, { setNo ->
            binding.btnPrevious.toggleEnabled(setNo != 1)
        })

        adLoader = handleNativeAds(binding.sessionsAdTemplate, this, Helpers.AD_ID_SESSIONS_NATIVE, null)
        binding.edtLoad.setOnEditorActionListener { _: TextView?, actionId: Int, _: KeyEvent? ->
            if (actionId == KeyEvent.KEYCODE_CALL) {
                if (binding.edtRep.text.toString().isEmpty()) {
                    binding.edtRep.requestFocus()
                } else {
                    handleNextInput()
                }
            }
            true
        }
        binding.edtLoad.filters = arrayOf<InputFilter>(DecimalDigitsInputFilter(3, 2))
        binding.edtRep.setOnEditorActionListener { _: TextView?, actionId: Int, _: KeyEvent? ->
            if (actionId == KeyEvent.KEYCODE_CALL) {
                if (binding.edtLoad.text.toString().isEmpty()) {
                    binding.edtLoad.requestFocus()
                } else {
                    handleNextInput()
                }
            }
            true
        }

        binding.viewDisableRV.setOnClickListener { Toast.makeText(this@SessionsActivity, R.string.finishInputFirst, Toast.LENGTH_SHORT).show() }
        adapter = SessionsRVAdapter(
            { position: Int -> onParentClick(position) },
            { position: Int, view: View -> onParentLongClick(position, view) },
            this
        )
        binding.sessionsRV.adapter = adapter
        binding.sessionsRV.layoutManager = LinearLayoutManager(this)
        binding.btnViewChart.setOnClickListener {
            Intent(this@SessionsActivity, ChartActivity::class.java).apply {
                putExtra(Helpers.CHART_KEY, exercises[viewModel.position.value!!])
                startActivity(this)
            }
        }
        binding.sessionsRV.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!viewModel.isInputOngoing.value!!) {
                    binding.apply {
                        btnNextExercise.toggleVisibility(dy <= 0)
                        btnPreviousExercise.toggleVisibility(dy <= 0)
                        btnAddSession.toggleVisibility(dy <= 0)
                        if (sessions.size > 0) btnViewChart.toggleVisibility(dy <= 0)
                    }
                }
            }
        })
        binding.btnAddSession.setOnClickListener {
            viewModel.setIsInputOngoing(true)
            inputPosition = 0
        }
        binding.btnAddSession.setOnLongClickListener {
            handleImportSessions()
            false
        }
        binding.btnNext.setOnClickListener { handleNextInput() }
        binding.btnPrevious.setOnClickListener { handlePreviousInput() }
        binding.btnNextExercise.setOnClickListener {
            viewModel.setPosition(viewModel.position.value!!.plus(1))
        }
        binding.btnPreviousExercise.setOnClickListener {
            viewModel.setPosition(viewModel.position.value!!.minus(1))
        }
    }

    private fun handlePreviousInput() {
        if (isInputFinished) isInputFinished = false
        backed++
        viewModel.setSetNo(viewModel.setNo.value!!.minus(1))
        adapter.setHighlightPosition(viewModel.setNo.value!!.minus(1))
        adapter.notifyItemChanged(inputPosition)
        putValuesAndSetSelection(viewModel.setNo.value!!.minus(1))
    }

    private fun putValuesAndSetSelection(index: Int) {
        binding.apply {
            edtRep.setText(reps[index].toString())
            edtRep.setSelection(edtRep.text?.length ?: 0)
            edtLoad.setText(load[index].toPrettyString())
            edtLoad.setSelection(edtLoad.text?.length ?: 0)
            rirNumberPicker.value = rir[index]
        }
    }

    private fun handleNextInput() {
        if (binding.edtRep.text.toString().isEmpty() || binding.edtLoad.text.toString().isEmpty()) {
            Toast.makeText(this@SessionsActivity, R.string.insertRepsAndLoad, Toast.LENGTH_SHORT).show()
            binding.imgLoad.setImageResource(R.drawable.ic_hexagon_triple_red)
            shake(binding.imgLoad)
            binding.imgRep.setImageResource(R.drawable.ic_hexagon_triple_red)
            shake(binding.imgRep)
        } else if (isInputFinished) {
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
            getValuesAndClear()
            hideViewsAndReset()
            handleNativeAds(binding.sessionsAdTemplate, this, Helpers.AD_ID_SESSIONS_NATIVE, adLoader)
        } else {
            if (viewModel.setNo.value!! == 1) {
                if (inputPosition == 0 && backed == 0 && !editing) {
                    sessions.add(0, Session(0, floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f), intArrayOf(0, 0, 0, 0, 0, 0, 0, 0)))
                    sessions[0].date = df.format(Calendar.getInstance().time)
                    adapter.notifyItemInserted(0)
                    binding.sessionsRV.smoothScrollToPosition(0)
                }
                load = sessions[inputPosition].load
                reps = sessions[inputPosition].reps
                rir = sessions[inputPosition].rir ?: intArrayOf(6, 6, 6, 6, 6, 6, 6, 6)
            }
            binding.imgLoad.setImageResource(R.drawable.ic_hexagon_triple_empty)
            binding.imgRep.setImageResource(R.drawable.ic_hexagon_triple_empty)
            getValuesAndClear()
            if (exercises[viewModel.position.value!!].sets == viewModel.setNo.value!! + 1) {
                isInputFinished = true
            }
            viewModel.setSetNo(viewModel.setNo.value!!.plus(1))
            if (backed > 0) backed--
        }
    }

    private fun showViews() {
        binding.apply {
            input.toggleVisibility(true)
            btnAddSession.hide()
            btnViewChart.hide()
            btnNextExercise.hide()
            btnPreviousExercise.hide()
            edtLoad.requestFocus()
        }

        imm.showSoftInput(binding.edtLoad, InputMethodManager.SHOW_FORCED)
    }

    private fun setTitleAndTempo() {
        val tempo: String
        binding.tilRep.hint = resources.getString(R.string.repsSet)
        when (exercises[viewModel.position.value!!].tempo) {
            9999 -> {
                tempo = resources.getString(R.string.iso)
                binding.tilRep.hint = resources.getString(R.string.timeSet)
            }
            0 -> {
                tempo = ""
            }
            else -> {
                tempo = exercises[viewModel.position.value!!].tempo.toString()
            }
        }
        val imgSuperset = setupActionBar(exercises[viewModel.position.value!!].name, tempo, supportActionBar, this)
        imgSuperset?.let { img ->
            if (exercises[viewModel.position.value!!].superSet == 1 || exercises[viewModel.position.value!!].superSet == 2) {
                img.visibility = View.VISIBLE
            } else if (img.isShown) {
                img.visibility = View.GONE
            }
        }
    }

    private fun handleImportSessions() {
        var selectedWorkout = 0
        workouts = getInstance(this).allWorkouts
        workoutNames = arrayOfNulls(workouts!!.size)
        for (i in workouts!!.indices) {
            workoutNames[i] = workouts!![i].name
        }
        AlertDialog.Builder(this, R.style.DefaultAlertDialogTheme)
            .setTitle(R.string.loadSessions)
            .setIcon(R.drawable.ic_load)
            .setSingleChoiceItems(workoutNames, 0) { _: DialogInterface?, which: Int -> selectedWorkout = which }
            .setPositiveButton(R.string.next) { _: DialogInterface?, _: Int -> handleShowExercises(selectedWorkout) }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun handleShowExercises(selectedWorkout: Int) {
        var exerciseToRemove: Exercise? = null
        var selectedExercise = 0
        exercisesImporting = workouts!![selectedWorkout].exercises
        if (exercisesImporting.isEmpty()) {
            Toast.makeText(this, R.string.noExercisesToLoadFrom, Toast.LENGTH_SHORT).show()
        } else {
            for (e in exercisesImporting) {
                if (e.id == exercises[viewModel.position.value!!].id) {
                    exerciseToRemove = e
                }
            }
            exercisesImporting.remove(exerciseToRemove!!)
            if (exercisesImporting.isEmpty()) {
                Toast.makeText(this, R.string.noExercisesToLoadFrom, Toast.LENGTH_SHORT).show()
            } else {
                exerciseNames = arrayOfNulls(exercisesImporting.size)
                for (i in exercisesImporting.indices) {
                    exerciseNames[i] = exercisesImporting[i].name
                }
                AlertDialog.Builder(this, R.style.DefaultAlertDialogTheme)
                    .setTitle(R.string.selectExercise)
                    .setIcon(R.drawable.ic_load)
                    .setSingleChoiceItems(exerciseNames, 0) { _: DialogInterface?, which: Int -> selectedExercise = which }
                    .setPositiveButton(R.string.next) { _: DialogInterface?, _: Int ->
                        if (exercisesImporting[selectedExercise].sessions.isEmpty()) {
                            Toast.makeText(this@SessionsActivity, R.string.noSessionsToLoad, Toast.LENGTH_SHORT).show()
                        } else {
                            handleShowOptions(selectedExercise)
                        }
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .show()
            }
        }
    }

    private fun handleShowOptions(selectedExercise: Int) {
        var selectedOptions = 0
        AlertDialog.Builder(this, R.style.DefaultAlertDialogTheme)
            .setTitle(R.string.selectOptions)
            .setIcon(R.drawable.ic_load)
            .setSingleChoiceItems(arrayOf(getString(R.string.overwrite), getString(R.string.addSortRemove)), 0) { _: DialogInterface?, which: Int -> selectedOptions = which }
            .setPositiveButton(R.string.load) { _: DialogInterface?, _: Int -> handleImporting(selectedExercise, selectedOptions) }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun handleImporting(selectedExercise: Int, selectedOptions: Int) {
        when (selectedOptions) {
            0 -> {
                sessions = exercisesImporting[selectedExercise].sessions
                adapter.setSessions(sessions)
                adapter.notifyDataSetChanged()
                exercises[viewModel.position.value!!].sessions = sessions
                getInstance(this).updateWorkoutsExercisesWithoutWorkout(exercises)
            }
            1 -> {
                for (s in exercisesImporting[selectedExercise].sessions) {
                    sessions.removeIf { sToRemove: Session -> sToRemove.date == s.date && sToRemove.load.contentEquals(s.load) && sToRemove.reps.contentEquals(s.reps) }
                    adapter.notifyDataSetChanged()
                    sessions.add(0, s)
                    adapter.notifyItemInserted(0)
                }
                sessions.sortWith { o1: Session, o2: Session -> o2.date!!.compareTo(o1.date!!) }
                adapter.notifyItemRangeChanged(0, sessions.size)
                exercises[viewModel.position.value!!].sessions = sessions
                getInstance(this).updateWorkoutsExercisesWithoutWorkout(exercises)
            }
            else -> {
            }
        }
    }

    private fun getValuesAndClear() {
        load[viewModel.setNo.value!!.minus(1)] = binding.edtLoad.text.toString().toFloat()
        reps[viewModel.setNo.value!!.minus(1)] = binding.edtRep.text.toString().toInt()
        rir[viewModel.setNo.value!!.minus(1)] = if (binding.rirNumberPicker.value.toString() == "-") 6 else binding.rirNumberPicker.value.toString().toInt()

        if (!editing && backed == 0) {
            binding.edtRep.text = null
            binding.edtLoad.text = null
        } else {
            if (reps[viewModel.setNo.value!!] == 0) {
                binding.edtRep.setText("")
            } else {
                binding.edtRep.setText(reps[viewModel.setNo.value!!].toString())
                binding.edtRep.setSelection(binding.edtRep.text?.length ?: 0)
            }
            if (load[viewModel.setNo.value!!] == 0F) {
                binding.edtLoad.setText("")
            } else {
                binding.edtLoad.setText(load[viewModel.setNo.value!!].toPrettyString())
                binding.edtLoad.setSelection(binding.edtLoad.text?.length ?: 0)
            }
            if (rir[viewModel.setNo.value!!] != 0) binding.rirNumberPicker.value = rir[viewModel.setNo.value!!]
        }
        binding.edtLoad.requestFocus()
        sessions[inputPosition].load = load
        sessions[inputPosition].reps = reps
        sessions[inputPosition].rir = rir
        exercises[viewModel.position.value!!].sessions = sessions
        adapter.setHighlightPosition(viewModel.setNo.value!!)
        adapter.notifyItemChanged(inputPosition)
        getInstance(this).updateWorkoutsExercises(displayedWorkout!!, exercises)
    }

    private fun hideViewsAndReset() {
        adapter.setHighlightPosition(-1)
        adapter.notifyItemChanged(inputPosition)
        viewModel.setSetNo(1)
        binding.apply {
            btnNextExercise.toggleVisibilityEFAB(true)
            btnPreviousExercise.toggleVisibilityEFAB(true)
            btnViewChart.toggleVisibilityEFAB(sessions.size > 1)
            btnAddSession.toggleVisibilityEFAB(true)
            input.toggleVisibility(false)
            edtRep.text = null
            edtLoad.text = null
            rirNumberPicker.value = 3
        }
        isInputFinished = false
        viewModel.setIsInputOngoing(false)
        editing = false
    }

    private fun onParentClick(positionRV: Int) {
        if (editing) {
            Toast.makeText(this, R.string.finishInputFirst, Toast.LENGTH_SHORT).show()
        } else {
            reps = sessions[positionRV].reps
            load = sessions[positionRV].load
            rir = sessions[positionRV].rir ?: intArrayOf(0, 0, 0, 0, 0, 0, 0, 0)
            inputPosition = positionRV
            viewModel.setIsInputOngoing(true)
            editing = true
            putValuesAndSetSelection(0)
            adapter.setHighlightPosition(0)
            adapter.notifyItemChanged(inputPosition)
        }
    }

    private fun onParentLongClick(positionRV: Int, v: View?) {
        val popupMenu = PopupMenu(this, v!!)
        popupMenu.inflate(R.menu.popup_menu_session)
        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.menuDeleteSession -> {
                    AlertDialog.Builder(this@SessionsActivity, R.style.DefaultAlertDialogTheme)
                        .setMessage(R.string.sureDeleteThisSession)
                        .setIcon(R.drawable.ic_delete)
                        .setPositiveButton(R.string.yes) { _: DialogInterface?, _: Int ->
                            try {
                                sessions.remove(sessions[positionRV])
                                exercises[viewModel.position.value!!].sessions = sessions
                                getInstance(this@SessionsActivity).updateWorkoutsExercisesWithoutWorkout(exercises)
                                adapter.notifyItemRemoved(positionRV)
                                adapter.notifyItemRangeChanged(positionRV, sessions.size - 2 - positionRV)
                                if (sessions.size < 2) binding.btnViewChart.hide()
                            } catch (e: Exception) {
                                Toast.makeText(this@SessionsActivity, "" + e, Toast.LENGTH_SHORT).show()
                            }
                        }
                        .setNegativeButton(R.string.no, null)
                        .show()
                    return@setOnMenuItemClickListener true
                }
                else -> return@setOnMenuItemClickListener false
            }
        }
        popupMenu.show()
    }
}

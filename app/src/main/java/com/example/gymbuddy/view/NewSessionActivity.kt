package com.example.gymbuddy.view

import android.annotation.SuppressLint
import android.content.*
import android.icu.util.Calendar
import android.icu.util.TimeZone
import android.os.*
import android.provider.CalendarContract
import android.text.InputFilter
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gymbuddy.R
import com.example.gymbuddy.databinding.ActivityNewSessionBinding
import com.example.gymbuddy.helpers.DecimalDigitsInputFilter
import com.example.gymbuddy.helpers.Helpers
import com.example.gymbuddy.helpers.Helpers.handleNativeAds
import com.example.gymbuddy.helpers.Helpers.hideKeyboard
import com.example.gymbuddy.helpers.Helpers.setup
import com.example.gymbuddy.helpers.Helpers.setupActionBar
import com.example.gymbuddy.helpers.Helpers.shake
import com.example.gymbuddy.helpers.Helpers.showKeyboard
import com.example.gymbuddy.helpers.Helpers.toPrettyString
import com.example.gymbuddy.helpers.Helpers.toggleMarque
import com.example.gymbuddy.helpers.Utils.INSTANCE.getInstance
import com.example.gymbuddy.model.Exercise
import com.example.gymbuddy.model.Session
import com.example.gymbuddy.model.Set
import com.example.gymbuddy.model.Workout
import com.example.gymbuddy.recyclerViewAdapters.SessionsRVAdapter
import com.example.gymbuddy.recyclerViewAdapters.WorkoutsExercisesRVAdapter
import com.example.gymbuddy.services.NotificationService
import com.example.gymbuddy.services.NotificationService.LocalBinder
import com.example.gymbuddy.viewModel.NewSessionViewModel
import com.google.android.gms.ads.AdLoader
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("SetTextI18n", "NotifyDataSetChanged")
class NewSessionActivity : AppCompatActivity() {
    private lateinit var load: FloatArray
    private lateinit var reps: IntArray
    private lateinit var rir: IntArray
    private var countDownTimer: CountDownTimer? = null
    private lateinit var adapter: SessionsRVAdapter
    private var doubleBackToExitPressedOnce = false
    private var df: DateFormat = SimpleDateFormat("yyyy-MM-dd")
    private var isInBackground = false
    private var wasBackPressed = false
    private var wasDoubleBackPressed = false
    private var adapterExercises = WorkoutsExercisesRVAdapter(this)
    private var timerRequired = false
    private var isStopped = false
    private var timerHandler: Handler = Handler(Looper.getMainLooper())
    private lateinit var timerRunnable: Runnable
    private var startingTime = 0
    private var adLoader: AdLoader? = null
    private var mBound = false
    private var notificationService: NotificationService? = null
    private lateinit var serviceIntent: Intent
    private lateinit var mConnection: ServiceConnection
    private lateinit var binding: ActivityNewSessionBinding
    private lateinit var viewModel: NewSessionViewModel

    override fun onResume() {
        notificationService?.let {
            it.cancelAll()
            stopService(serviceIntent)
        }
        isInBackground = false
        wasDoubleBackPressed = false
        super.onResume()
    }

    override fun onPause() {
        if (!wasBackPressed) {
            isInBackground = true
            if (viewModel.breakRunning.value!!) {
                notificationService?.apply {
                    setCurrentSet(viewModel.currentSession[viewModel.currentSet.value!!].exerciseSet)
                    exercise = viewModel.exercise.value
                    isBreakEnded = false
                    setTimeTxt(binding.txtTimer.text.toString())
                    startService(serviceIntent)
                }
            }
        }
        super.onPause()
    }

    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            wasBackPressed = true
            wasDoubleBackPressed = true
            viewModel.exercises[viewModel.currentSession[viewModel.currentSet.value!!].exercise].sessions = viewModel.sessions
            viewModel.workout.state = intArrayOf(0, viewModel.currentSet.value!!, viewModel.backed.value!!)
            getInstance(this@NewSessionActivity).updateWorkoutsExercises(viewModel.workout, viewModel.exercises)
            super.onBackPressed()
            finish()
            return
        }
        doubleBackToExitPressedOnce = true
        Toast.makeText(this, R.string.pressBackToExitSession, Toast.LENGTH_SHORT).show()
        Handler(Looper.getMainLooper()).postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_GymBuddy)
        super.onCreate(savedInstanceState)
        binding = ActivityNewSessionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        serviceIntent = Intent(this, NotificationService::class.java)
        viewModel = ViewModelProvider(this).get(NewSessionViewModel::class.java)

        mConnection = object : ServiceConnection {
            override fun onServiceDisconnected(name: ComponentName) {
                mBound = false
                notificationService = null
            }

            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                mBound = true
                val mLocalBinder = service as LocalBinder
                notificationService = mLocalBinder.serverInstance
            }
        }
        bindService(serviceIntent, mConnection, BIND_AUTO_CREATE)
        timerRunnable = Runnable {
            timerHandler.postDelayed(timerRunnable, 1000)
            startingTime++
            if (startingTime > 300) {
                timerHandler.removeCallbacks(timerRunnable)
            }
            updateTimer(((startingTime - 1) * 1000).toLong())
        }

        initViews()

        if (viewModel.currentSession.isEmpty()) {
            viewModel.buildSession(intent.getSerializableExtra(Helpers.NEW_SESSION_KEY) as Workout)
            if (intent.getBooleanExtra(Helpers.RESUMED_KEY, false)) {
                viewModel.setCurrentSet(viewModel.workout.state[1])
                viewModel.setBacked(viewModel.workout.state[2])
                if (getInstance(this).breakLeft != 0L) {
                    if (System.currentTimeMillis() - getInstance(this).systemTime < getInstance(this).breakLeft) {
                        startTimer(getInstance(this).breakLeft - System.currentTimeMillis() + getInstance(this).systemTime)
                        viewModel.setBreakRunning(true)
                    }
                }
            }
        }

        setupActionBar(viewModel.workout.name, "", supportActionBar, this)
        supportActionBar?.hide()
        binding.workoutName.text = viewModel.workout.name

        setupRVsAndAdapters()
        setupNextExercise()

        if (viewModel.backed.value!! > 0) {
            putValuesIntoFields(viewModel.currentSession[viewModel.currentSet.value!!].exerciseSet)
        }
        if (viewModel.exercises[viewModel.currentSession[viewModel.currentSet.value!!].exercise].tempo == 9999 && !viewModel.breakRunning.value!!) {
            binding.btnSkipTimer.setText(R.string.start)
        }

        binding.edtLoads.requestFocus()
        showKeyboard(this)
        binding.btnSkipTimer.setOnClickListener { handleSkip() }
        binding.btnNextSet.setOnClickListener { handleNextSet() }
        //binding.btnNextSet.setOnLongClickListener { handleSwitchExercises() }
        binding.btnPreviousSet.setOnClickListener { handlePreviousSet() }

        viewModel.exerciseNo.observe(this, { exerciseNo ->
            changeColorToHighlighted(exerciseNo)
            if (viewModel.previousExercise != -1) changeColorToDone(viewModel.previousExercise)
        })

        viewModel.exerciseSet.observe(this, { exerciseSet ->
            binding.txtSetCount.text = exerciseSet.toString()
            adapter.setHighlightPosition(exerciseSet - 1)
        })

        viewModel.exercise.observe(this, { exercise ->
            setTempo(exercise)
        })

        viewModel.timeLeftInMs.observe(this, { timeLeftInMs ->
            binding.progressBar.setProgress((timeLeftInMs - 1000).toString().toInt(), true)
            updateTimer(timeLeftInMs)
        })

        viewModel.backed.observe(this, { backed ->
            if (backed == 3) binding.btnPreviousSet.isEnabled = false
        })

        viewModel.currentSet.observe(this, { currentSet ->
            if (currentSet == 0) binding.btnPreviousSet.isEnabled = false
        })

        viewModel.timerRunning.observe(this, { timerRunning ->
            if (timerRunning) {
                timerHandler.post(timerRunnable)
                binding.btnSkipTimer.setText(R.string.stop)
                binding.btnNextSet.isEnabled = false
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        })

        viewModel.breakRunning.observe(this, { breakRunning ->
            if (!breakRunning) {
                binding.apply {
                    progressBar.progress = 0
                    btnNextSet.isEnabled = true
                    btnSkipTimer.isEnabled = false
                    txtTimer.text = "0:00"
                    if (viewModel.exercise.value!!.tempo == 9999) {
                        timerRequired = true
                        btnSkipTimer.setText(R.string.start)
                        btnSkipTimer.isEnabled = true
                    }
                }
            } else {
                binding.apply {
                    progressBar.progress = 100
                    btnNextSet.isEnabled = false
                    btnSkipTimer.isEnabled = true
                }

            }
        })

    }

    private fun setupNextExercise() {
        viewModel.setExercise(viewModel.currentSet.value!!)
        viewModel.sessions = viewModel.exercise.value!!.sessions

        if (viewModel.backed.value!! < 1 && viewModel.currentSession[viewModel.currentSet.value!!].exerciseSet == 0) {
            viewModel.sessions.add(0, Session())
            viewModel.sessions[0].date = df.format(Calendar.getInstance().time)
        }

        load = viewModel.sessions[0].load
        reps = viewModel.sessions[0].reps
        rir = viewModel.sessions[0].rir ?: intArrayOf(6, 6, 6, 6, 6, 6, 6, 6)

        adapter.apply {
            setSessions(viewModel.sessions)
            setExercise(viewModel.exercises[viewModel.currentSession[viewModel.currentSet.value!!].exercise])
            notifyDataSetChanged()
        }
    }

    private fun handleSwitchExercises(): Boolean {
        val sourceExercise = viewModel.currentSession[viewModel.currentSet.value!!].exercise
        val tempSetList = ArrayList<Set>()
        if (viewModel.currentSession[viewModel.currentSet.value!!].exerciseSet == 0 &&
            sourceExercise != viewModel.exercises.size - 1
        ) {
            for (i in 1..viewModel.exercises[sourceExercise].sets) {
                tempSetList.add(viewModel.currentSession[viewModel.currentSet.value!!])
                viewModel.currentSession.removeAt(viewModel.currentSet.value!!)
            }
            var offset = 3
            while (viewModel.currentSet.value!! + offset < viewModel.currentSession.size && viewModel.currentSession[viewModel.currentSet.value!! + offset - 1].exercise
                == viewModel.currentSession[viewModel.currentSet.value!! + offset].exercise
            ) offset++
            val targetPosition = viewModel.currentSet.value!! + offset
            viewModel.currentSession.addAll(targetPosition, tempSetList)
            for (s in viewModel.currentSession) if (s.state != 0) s.state = 2
            viewModel.currentSession.last().state = 3
//            for(s in viewModel.currentSession) Toast.makeText(this, "${s.exercise} ${s.exerciseSet}", Toast.LENGTH_SHORT).show()
            //TODO ogarnąć zmianę wyświetlania po zamianie ćwiczeń
        } else {
            Toast.makeText(this, "Cannot swap exercises", Toast.LENGTH_SHORT).show()
        }
        return true
    }

    private fun handleSkip() {
        if (timerRequired) {
            when {
                isStopped -> {
                    startingTime = 0
                    binding.txtTimer.text = "0:00"
                    viewModel.setTimerRunning(false)
                    isStopped = false
                    binding.btnSkipTimer.setText(R.string.start)
                }
                viewModel.timerRunning.value == true -> {
                    timerHandler.removeCallbacks(timerRunnable)
                    binding.edtReps.setText((startingTime - 1).toString())
                    binding.edtReps.setSelection(binding.edtReps.text?.length ?: 0)
                    viewModel.setTimerRunning(false)
                    isStopped = true
                    binding.btnSkipTimer.setText(R.string.reset)
                    binding.btnNextSet.isEnabled = true
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
                else -> {
                    viewModel.setTimerRunning(true)
                }
            }
        } else {
            if (viewModel.timerRunning.value == true) {
                timerHandler.removeCallbacks(timerRunnable)
                viewModel.setTimerRunning(false)
                isStopped = true
            }
            viewModel.setBreakRunning(false)
            countDownTimer!!.cancel()
        }
    }

    private fun handlePreviousSet() {
        viewModel.setBacked(viewModel.backed.value!!.plus(1))
        viewModel.setCurrentSet(viewModel.currentSet.value!!.minus(1))
        if (viewModel.currentSession[viewModel.currentSet.value!!].state == 2) {
            viewModel.setExercise(viewModel.currentSet.value!!)
            viewModel.sessions = viewModel.exercise.value!!.sessions
            adapter.setExercise(viewModel.exercise.value)
            adapter.setSessions(viewModel.sessions)
            adapter.notifyDataSetChanged()
            load = viewModel.sessions[0].load
            reps = viewModel.sessions[0].reps
            rir = viewModel.sessions[0].rir ?: intArrayOf(6, 6, 6, 6, 6, 6, 6, 6)
        } else {
            adapter.notifyItemChanged(0)
        }
        putValuesIntoFields(viewModel.currentSession[viewModel.currentSet.value!!].exerciseSet)
        isStopped = true
        handleSkip()
        viewModel.workout.state = intArrayOf(0, viewModel.currentSet.value!!, viewModel.backed.value!!)
        getInstance(this).updateWorkoutsExercises(viewModel.workout, viewModel.exercises)
    }

    private fun putValuesIntoFields(exerciseSet: Int) {
        binding.apply {
            rirNumberPicker.value = rir[exerciseSet]
            edtReps.setText(reps[exerciseSet].toString())
            edtReps.setSelection(edtReps.text?.length ?: 0)
            edtLoads.setText(load[exerciseSet].toPrettyString())
            edtLoads.setSelection(edtLoads.text?.length ?: 0)
        }
    }

    private fun handleNextSet() {
        viewModel.setTimerRunning(false)
        isStopped = false
        startingTime = 0
        if (binding.edtLoads.text.toString().isEmpty() || binding.edtReps.text.toString().isEmpty()) {
            Toast.makeText(this@NewSessionActivity, R.string.insertRepsAndLoad, Toast.LENGTH_SHORT).show()
            binding.imgLoads.setImageResource(R.drawable.ic_hexagon_triple_red)
            shake(binding.imgLoads)
            binding.imgReps.setImageResource(R.drawable.ic_hexagon_triple_red)
            shake(binding.imgReps)
        } else {
            getValuesAndClear()
            if (viewModel.currentSession[viewModel.currentSet.value!!].state == 3) {
                hideKeyboard(this)
                viewModel.workout.state = intArrayOf(1, 0, 0)
                if (getInstance(this@NewSessionActivity).isLoggingEnabled) {
                    try {
                        handleAddCalendarEntry(calendarEntryStringBuilder())
                    } catch (e: Exception) {
                        Toast.makeText(this@NewSessionActivity, resources.getString(R.string.grantPermission), Toast.LENGTH_LONG).show()
                        getInstance(this@NewSessionActivity).isLoggingEnabled = false
                    }
                }
                viewModel.exercises[viewModel.currentSession[viewModel.currentSet.value!!].exercise].sessions = viewModel.sessions
                getInstance(this@NewSessionActivity).updateWorkoutsExercises(viewModel.workout, viewModel.exercises)
                viewModel.setBreakRunning(false)
                finish()
            } else {
                viewModel.setCurrentSet(viewModel.currentSet.value!!.plus(1))
                if (viewModel.currentSession[viewModel.currentSet.value!! - 1].state == 2) {
                    handleNativeAds(binding.newSessionAdTemplate, this, Helpers.AD_ID_NEW_SESSION_NATIVE, adLoader)
                    viewModel.exercises[viewModel.currentSession[viewModel.currentSet.value!! - 1].exercise].sessions = viewModel.sessions
                    //TODO ten fragment
                    setupNextExercise()
//                    viewModel.setExercise(viewModel.currentSet.value!!)
//                    viewModel.sessions = viewModel.exercise.value!!.sessions
//                    if (viewModel.backed.value!! < 1 && viewModel.currentSession[viewModel.currentSet.value!!].exerciseSet == 0) {
//                        viewModel.sessions.add(0, Session(0, floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f), intArrayOf(0, 0, 0, 0, 0, 0, 0, 0)))
//                        viewModel.sessions[0].date = df.format(Calendar.getInstance().time)
//                    }
//                    adapter.setExercise(viewModel.exercise.value)
//                    adapter.setSessions(viewModel.sessions)
//                    adapter.notifyDataSetChanged()
//                    load = viewModel.sessions[0].load
//                    reps = viewModel.sessions[0].reps
//                    rir = viewModel.sessions[0].rir ?: intArrayOf(6, 6, 6, 6, 6, 6, 6, 6)
                    //TODO ten fragment
                    getInstance(this@NewSessionActivity).updateSingleWorkout(viewModel.workout)
                } else {
                    binding.btnSkipTimer.setText(R.string.skipTimer)
                    adapter.notifyItemChanged(0)
                }
                timerRequired = false
                startTimer((viewModel.exercise.value!!.breaks * 1000).toLong())
                if (viewModel.backed.value!! > 0) {
                    viewModel.setBacked(viewModel.backed.value!!.minus(1))
                    if (viewModel.backed.value!! > 0) putValuesIntoFields(viewModel.currentSession[viewModel.currentSet.value!!].exerciseSet)
                }
                viewModel.workout.state = intArrayOf(0, viewModel.currentSet.value!!, viewModel.backed.value!!)
                getInstance(this).updateWorkoutsExercises(viewModel.workout, viewModel.exercises)
            }
        }
    }

    private fun initViews() {
        binding.rirNumberPicker.setup()
        binding.edtLoads.filters = arrayOf<InputFilter>(DecimalDigitsInputFilter(3, 2))
        binding.edtLoads.setOnEditorActionListener { _: TextView?, actionId: Int, _: KeyEvent? ->
            if (actionId == KeyEvent.KEYCODE_CALL) {
                if (binding.edtReps.text.toString().isEmpty()) {
                    binding.edtReps.requestFocus()
                } else if (binding.btnNextSet.isEnabled) {
                    handleNextSet()
                }
            }
            true
        }
        binding.edtReps.setOnEditorActionListener { _: TextView?, actionId: Int, _: KeyEvent? ->
            if (actionId == KeyEvent.KEYCODE_CALL) {
                if (binding.edtLoads.text.toString().isEmpty()) {
                    binding.edtLoads.requestFocus()
                } else if (binding.btnNextSet.isEnabled) {
                    handleNextSet()
                }
            }
            true
        }
        adLoader = handleNativeAds(binding.newSessionAdTemplate, this, Helpers.AD_ID_NEW_SESSION_NATIVE, null)
    }

    private fun setTempo(exercise: Exercise) {
        if (exercise.tempo == 9999) {
            binding.tilReps.hint = resources.getString(R.string.timeSet)
            binding.txtTempoSession.visibility = View.GONE
            binding.tempoTxt.setText(R.string.isometric)
            binding.tempoTxt.visibility = View.VISIBLE
            binding.btnSkipTimer.isEnabled = true
            if (!viewModel.breakRunning.value!!) timerRequired = true
        } else {
            binding.tilReps.hint = resources.getString(R.string.repsSet)
            binding.btnSkipTimer.setText(R.string.skipTimer)
            timerRequired = false
            if (exercise.tempo == 0) {
                binding.txtTempoSession.visibility = View.GONE
                binding.tempoTxt.visibility = View.GONE
            } else {
                binding.txtTempoSession.visibility = View.VISIBLE
                binding.tempoTxt.setText(R.string.tempo)
                binding.tempoTxt.visibility = View.VISIBLE
                binding.txtTempoSession.text = exercise.tempo.toString()
            }
        }
    }

    private fun changeColorToHighlighted(position: Int) {
        binding.newSessionExercisesRV.findViewHolderForAdapterPosition(position)?.let { viewHolder ->
            viewHolder.itemView.findViewById<TextView>(R.id.txtExerciseNameSimple).apply {
                setTextColor(ContextCompat.getColor(this@NewSessionActivity, R.color.grey_700))
                toggleMarque(true)
            }
            viewHolder.itemView.findViewById<TextView>(R.id.txtSetsNoSimple).apply { setTextColor(ContextCompat.getColor(this@NewSessionActivity, R.color.grey_700)) }
            viewHolder.itemView.findViewById<CardView>(R.id.parentExerciseSimple).apply { setCardBackgroundColor(ContextCompat.getColor(this@NewSessionActivity, R.color.orange_500)) }
            viewHolder.itemView.findViewById<ImageView>(R.id.imgExerciseSimple).apply { setImageResource(R.drawable.ic_hexagon_double_vertical_empty) }
        }
    }

    private fun changeColorToDone(position: Int) {
        binding.newSessionExercisesRV.findViewHolderForAdapterPosition(position)?.let {
            it.itemView.findViewById<TextView>(R.id.txtExerciseNameSimple).apply {
                setTextColor(ContextCompat.getColor(this@NewSessionActivity, R.color.grey_200))
                toggleMarque(false)
            }
            it.itemView.findViewById<TextView>(R.id.txtSetsNoSimple).apply { setTextColor(ContextCompat.getColor(this@NewSessionActivity, R.color.grey_200)) }
            it.itemView.findViewById<CardView>(R.id.parentExerciseSimple).apply { setCardBackgroundColor(ContextCompat.getColor(this@NewSessionActivity, R.color.grey_500)) }
        }
    }

    private fun vibrate() {
        val vibrator = if (Build.VERSION.SDK_INT >= 31) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }
        vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(50, 50, 50), intArrayOf(25, 0, 25), -1))
    }

    override fun onDestroy() {
        getInstance(this).breakLeft = if (viewModel.breakRunning.value!!) {
            getInstance(this).systemTime = System.currentTimeMillis()
            viewModel.timeLeftInMs.value!!
        } else {
            0
        }
        if (mBound) {
            unbindService(mConnection)
            stopService(serviceIntent)
            mBound = false
        }
        super.onDestroy()
    }

    private fun calendarEntryStringBuilder(): String {
        val workoutLog = StringBuilder().apply {
            for (e in viewModel.exercises) {
                append(e.name)
                append(": ")
                for (i in 0 until e.sets) {
                    append(e.sessions[0].load[i])
                    append("x")
                    append(e.sessions[0].reps[i])
                    append("|")
                    append(e.sessions[0].rir?.get(i) ?: "-")
                    append("  ")
                }
                append("\n")
            }
        }
        return workoutLog.toString()
    }

    private fun handleAddCalendarEntry(entry: String) {
        val startMillis: Long
        val endMillis: Long
        val now = Calendar.getInstance(TimeZone.getDefault())
        val beginTime = Calendar.getInstance()
        val year = now[Calendar.YEAR]
        val month = now[Calendar.MONTH]
        val day = now[Calendar.DAY_OF_MONTH]
        val hour = now[Calendar.HOUR_OF_DAY]
        val minute = now[Calendar.MINUTE]
        beginTime[year, month, day, hour] = minute
        startMillis = beginTime.timeInMillis
        val endTime = Calendar.getInstance()
        endTime[year, month, day, hour] = minute
        endMillis = endTime.timeInMillis
        val cr = contentResolver
        val values = ContentValues()
        values.apply {
            put(CalendarContract.Events.DTSTART, startMillis)
            put(CalendarContract.Events.DTEND, endMillis)
            put(CalendarContract.Events.TITLE, resources.getString(R.string.workout) + " " + viewModel.workout.name)
            put(CalendarContract.Events.DESCRIPTION, entry)
            put(
                CalendarContract.Events.CALENDAR_ID, when (getInstance(this@NewSessionActivity).calendarID) {
                    "" -> 1
                    else -> getInstance(this@NewSessionActivity).calendarID.toInt()
                }
            )
            put(CalendarContract.Events.EVENT_COLOR, ContextCompat.getColor(this@NewSessionActivity, R.color.orange_500))
            put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
        }
        cr.insert(CalendarContract.Events.CONTENT_URI, values)
    }

    private fun startTimer(time: Long) {
        viewModel.setBreakRunning(true)
        binding.progressBar.max = time.toString().toInt()
        countDownTimer = object : CountDownTimer(time + 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                viewModel.setTimeLeftInMs(millisUntilFinished)
                if (isInBackground) {
                    notificationService?.updateTime(updateTimer(millisUntilFinished))
                }
            }

            override fun onFinish() {
                viewModel.setBreakRunning(false)
                if (isInBackground) {
                    notificationService?.displayBreakFinishedNotification()
                } else if (!wasDoubleBackPressed) {
                    vibrate()
                }
            }
        }.start()
    }

    private fun updateTimer(timeLeftInMs: Long): String {
        val minutes = timeLeftInMs.toInt() / 60000
        val seconds = timeLeftInMs.toInt() % 60000 / 1000
        var timeLeftTxt: String = "" + minutes
        timeLeftTxt += ":"
        if (seconds < 10) {
            timeLeftTxt += "0"
        }
        timeLeftTxt += seconds
        binding.txtTimer.text = timeLeftTxt
        return timeLeftTxt
    }

    private fun getValuesAndClear() {
        if (!binding.btnPreviousSet.isEnabled) binding.btnPreviousSet.isEnabled = true
        binding.imgLoads.setImageResource(R.drawable.ic_hexagon_triple_empty)
        binding.imgReps.setImageResource(R.drawable.ic_hexagon_triple_empty)
        viewModel.currentSet.value?.let {
            load[viewModel.currentSession[it].exerciseSet] = binding.edtLoads.text.toString().toFloat()
            reps[viewModel.currentSession[it].exerciseSet] = binding.edtReps.text.toString().toInt()
            rir[viewModel.currentSession[it].exerciseSet] = if (binding.rirNumberPicker.value.toString() == "-") 6 else binding.rirNumberPicker.value.toString().toInt()
            binding.edtReps.text = null
            binding.edtLoads.text = null
            binding.edtLoads.requestFocus()
            viewModel.sessions[0].load = load
            viewModel.sessions[0].reps = reps
            viewModel.sessions[0].rir = rir
        }
    }

    private fun setupRVsAndAdapters() {
        adapterExercises.setNewSession(true)
        adapterExercises.exercises = viewModel.exercises
        adapterExercises.setExerciseNo(viewModel.currentSession[viewModel.currentSet.value!!].exercise)
        binding.newSessionExercisesRV.adapter = adapterExercises
        binding.newSessionExercisesRV.layoutManager = LinearLayoutManager(this)
        adapter = SessionsRVAdapter({ }, { _: Int, _: View -> }, this).apply { setMenuShown(false) }
        binding.newSessionsRV.adapter = adapter
        binding.newSessionsRV.layoutManager = LinearLayoutManager(this)
    }
}
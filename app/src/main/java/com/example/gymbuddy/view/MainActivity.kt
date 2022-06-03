package com.example.gymbuddy.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gymbuddy.R
import com.example.gymbuddy.databinding.ActivityMainBinding
import com.example.gymbuddy.helpers.Helpers
import com.example.gymbuddy.helpers.Helpers.MAIN_ACTIVITY_TAG
import com.example.gymbuddy.helpers.Helpers.MODE_ADD
import com.example.gymbuddy.helpers.Helpers.MODE_DELETE
import com.example.gymbuddy.helpers.Helpers.MODE_SAVE_TO_CLOUD
import com.example.gymbuddy.helpers.Helpers.MODE_SAVE_TO_FILE
import com.example.gymbuddy.helpers.Helpers.MODE_SHARE
import com.example.gymbuddy.helpers.Helpers.PERMISSIONS_CALENDAR
import com.example.gymbuddy.helpers.Helpers.REQUEST_CALENDAR
import com.example.gymbuddy.helpers.Helpers.SESSION_DATE_KEY
import com.example.gymbuddy.helpers.Helpers.handleNativeAds
import com.example.gymbuddy.helpers.Helpers.hideProgressBar
import com.example.gymbuddy.helpers.Helpers.setupActionBar
import com.example.gymbuddy.helpers.Helpers.showProgressBar
import com.example.gymbuddy.helpers.Helpers.showRatingUserInterface
import com.example.gymbuddy.helpers.Helpers.workoutTypeComparator
import com.example.gymbuddy.helpers.Utils
import com.example.gymbuddy.helpers.Utils.INSTANCE.getInstance
import com.example.gymbuddy.model.Exercise
import com.example.gymbuddy.model.Exercise.Companion.exerciseToMap
import com.example.gymbuddy.model.Exercise.Companion.mapToExercise
import com.example.gymbuddy.model.Session
import com.example.gymbuddy.model.Session.Companion.mapToSession
import com.example.gymbuddy.model.Session.Companion.sessionToMap
import com.example.gymbuddy.model.Workout
import com.example.gymbuddy.model.Workout.Companion.mapToWorkout
import com.example.gymbuddy.model.Workout.Companion.workoutToMap
import com.example.gymbuddy.recyclerViewAdapters.WorkoutsRVAdapter
import com.example.gymbuddy.viewModel.MainViewModel
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.AuthUI.IdpConfig.*
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.android.gms.ads.*
import com.google.android.gms.ads.initialization.InitializationStatus
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.tasks.Task
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.*
import java.util.*

class MainActivity : AppCompatActivity() {
    private var workoutsToSave = ArrayList<Workout>()
    private var workouts: ArrayList<Workout> = ArrayList()
    private var returnList = ArrayList<Workout>()
    private lateinit var workworkoutsRVAdapter: WorkoutsRVAdapter
    private var exercisesShown = false
    private var loggingEnabled = false
    private var groupingEnabled = false
    private var doubleBackToExitPressedOnce = false
    private var calendarId: ArrayList<String>? = null
    private var calendarName: ArrayList<String>? = null
    private var selected = 0
    private var checkingForCalendarForScheduling = false
    private val gson = Gson()
    private var directoryFile: File? = null
    private var tryingToLoad = false
    private var tryingToSave = false
    private var tryingToDelete = false
    private var authMenuItem: MenuItem? = null
    private var userRef: DocumentReference? = null
    private var user: FirebaseUser? = null
    private val db = FirebaseFirestore.getInstance()
    private lateinit var binding: ActivityMainBinding
    private var addWorkoutLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result -> onAddWorkoutResult(result) }
    private var shareWorkoutsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result -> onShareWorkoutsResult(result) }
    private var createFileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result -> onCreateFileResult(result) }
    private var openFileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result -> onOpenFileResult(result) }
    private val signInLauncher = registerForActivityResult(FirebaseAuthUIActivityResultContract()) { result: FirebaseAuthUIAuthenticationResult -> onSignInResult(result) }
    private var counter = 0
    private lateinit var viewModel: MainViewModel
    private var mRewardedAd: RewardedAd? = null
    private val TAG = "MainActivity"

    private fun onCreateFileResult(result: ActivityResult?) {
        result?.let {
            if (it.resultCode == RESULT_OK) {
                saveToFile(it.data?.data)
            }
        }
    }

    private fun onOpenFileResult(result: ActivityResult?) {
        result?.let {
            if (it.resultCode == RESULT_OK) {
                loadFromFile(it.data?.data)
            }
        }
    }

    private fun onShareWorkoutsResult(result: ActivityResult?) {
        result?.let {
            if (it.resultCode == RESULT_OK) {
                directoryFile?.let { file ->
                    if (file.delete()) {
                        Log.d(MAIN_ACTIVITY_TAG, "onActivityResult: shared file deleted")
                        directoryFile = null
                    } else Log.d(MAIN_ACTIVITY_TAG, "onActivityResult: failed to delete")
                }
            }
        }
    }

    private fun onAddWorkoutResult(result: ActivityResult?) {
        result?.let {
            if (it.resultCode == RESULT_OK) {
                val newWorkout = result.data?.getSerializableExtra(Helpers.NEW_WORKOUT_KEY) as Workout?
                getInstance(this).addToAllWorkouts(newWorkout!!)
            }
        }
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        if (result.resultCode == RESULT_OK) {
            user = FirebaseAuth.getInstance().currentUser
            authMenuItem!!.title = resources.getString(R.string.signOut)
            buildAndShowSignedInMessage(user)
            when {
                tryingToLoad -> {
                    tryingToLoad = false
                    handleLoadFromCloud(MODE_ADD)
                }
                tryingToSave -> {
                    tryingToSave = false
                    handlePickToExport(MODE_SAVE_TO_CLOUD)
                }
                tryingToDelete -> {
                    tryingToDelete = false
                    handleLoadFromCloud(MODE_DELETE)
                }
            }
        } else {
            user = null
            Toast.makeText(this, resources.getString(R.string.errorSigningIn), Toast.LENGTH_SHORT).show()
            authMenuItem!!.title = resources.getString(R.string.signIn)
            Log.d(MAIN_ACTIVITY_TAG, "onSignInResultError: $response")
        }
    }

    private fun createFile() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TITLE, "myWorkout")
        createFileLauncher.launch(intent)
    }

    private fun saveToFile(uri: Uri?) {
        try {
            val pfd = this.contentResolver.openFileDescriptor(uri!!, "w")
            val fileOutputStream = FileOutputStream(pfd!!.fileDescriptor)
            fileOutputStream.write(gson.toJson(workoutsToSave).toByteArray())
            fileOutputStream.close()
            workoutsToSave.clear()
        } catch (e: IOException) {
            e.printStackTrace()
            Log.d(MAIN_ACTIVITY_TAG, "saveToFile: $e")
        }
    }

    private fun loadFromFile(uri: Uri?) {
        val stringBuilder = StringBuilder()
        try {
            contentResolver.openInputStream(uri!!).use { inputStream ->
                BufferedReader(
                    InputStreamReader(Objects.requireNonNull(inputStream))
                ).use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        stringBuilder.append(line)
                    }
                    val type = object : TypeToken<ArrayList<Workout?>?>() {}.type
                    returnList = gson.fromJson(stringBuilder.toString(), type)
                    handleImportFromFile(returnList)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun openFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "text/plain"
        openFileLauncher.launch(intent)
    }

    private fun verifyCalendarPermissions(activity: Activity?): Boolean {
        val permission = ActivityCompat.checkSelfPermission(activity!!, Manifest.permission.READ_CALENDAR)
        return if (permission != PackageManager.PERMISSION_GRANTED) {
            AlertDialog.Builder(this, R.style.DefaultAlertDialogTheme)
                .setTitle(R.string.permissions)
                .setIcon(R.drawable.ic_info)
                .setMessage(R.string.permissionInfoCalendar)
                .setPositiveButton(R.string.ok) { _: DialogInterface?, _: Int -> ActivityCompat.requestPermissions(activity, PERMISSIONS_CALENDAR, REQUEST_CALENDAR) }
                .show()
            false
        } else true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (checkingForCalendarForScheduling) {
                handleSchedule()
            }
            checkingForCalendarForScheduling = false
        }
    }

    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }
        doubleBackToExitPressedOnce = true
        Toast.makeText(this, R.string.pressBackToExit, Toast.LENGTH_SHORT).show()
        Handler(Looper.getMainLooper()).postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
    }

    override fun onPause() {
        workouts.let { getInstance(this).updateWorkouts(it) }
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        workouts.clear()
        workouts.addAll(getInstance(this).allWorkouts!!)
        workworkoutsRVAdapter.notifyDataSetChanged()
        doubleBackToExitPressedOnce = false
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        authMenuItem = menu.findItem(R.id.auth)
        if (FirebaseAuth.getInstance().currentUser != null) authMenuItem?.setTitle(R.string.signOut)
        val submenuExercises=menu.findItem(R.id.submenuExercises)
        val submenuGrouping=menu.findItem(R.id.submenuGrouping)
        val submenuLogging=menu.findItem(R.id.submenuLogging)
        val submenuRir=menu.findItem(R.id.submenuRir)
        if(!getInstance(this).areExercisesShown) submenuExercises.title=resources.getString(R.string.showExercises)
        if(!getInstance(this).isGroupingEnabled) submenuGrouping.title=resources.getString(R.string.enableGrouping)
        if(getInstance(this).isLoggingEnabled) submenuLogging.title=resources.getString(R.string.disableLogging)
        if(!getInstance(this).rirEnabled) submenuRir.title=resources.getString(R.string.enableRir)
        return super.onCreateOptionsMenu(menu)
    }

    @SuppressLint("NonConstantResourceId")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.submenuExercises -> {
                exercisesShown = !exercisesShown
                getInstance(this).areExercisesShown = exercisesShown
                finish()
                startActivity(intent)
                true
            }
            R.id.submenuGrouping -> {
                groupingEnabled = !groupingEnabled
                getInstance(this).isGroupingEnabled = groupingEnabled
                finish()
                startActivity(intent)
                true
            }
            R.id.submenuRir -> {
                var rirEnabled = getInstance(this).rirEnabled
                rirEnabled=!rirEnabled
                getInstance(this).rirEnabled=rirEnabled
                item.title=resources.getString(if(rirEnabled)R.string.disableRir else R.string.enableRir)
                true
            }
            R.id.submenuLogging -> {
                if (verifyCalendarPermissions(this@MainActivity)) {
                    if (loggingEnabled) {
                        AlertDialog.Builder(this, R.style.DefaultAlertDialogTheme)
                            .setTitle(R.string.disableLogging)
                            .setIcon(R.drawable.ic_calendar)
                            .setMessage(R.string.disableLoggingMsg)
                            .setPositiveButton(R.string.yes) { _: DialogInterface?, _: Int ->
                                loggingEnabled = false
                                getInstance(this@MainActivity).isLoggingEnabled = loggingEnabled
                                item.title=resources.getString(R.string.enableLogging)
                            }
                            .setNegativeButton(R.string.no, null)
                            .show()
                    } else {
                        AlertDialog.Builder(this, R.style.DefaultAlertDialogTheme)
                            .setTitle(R.string.enableLogging)
                            .setIcon(R.drawable.ic_calendar)
                            .setMessage(R.string.enableLoggingMsg)
                            .setPositiveButton(R.string.yes) { _: DialogInterface?, _: Int -> handleCalendarSelection(item) }
                            .setNegativeButton(R.string.no, null)
                            .show()
                    }
                }
                true
            }
            R.id.submenuShare -> {
                handlePickToExport(MODE_SHARE)
                true
            }
            R.id.importFromFile -> {
                openFile()
                true
            }
            R.id.exportToFile -> {
                handlePickToExport(MODE_SAVE_TO_FILE)
                true
            }
            R.id.loadFromCloud -> {
                if (FirebaseAuth.getInstance().currentUser == null) {
                    handleAuth()
                    tryingToLoad = true
                } else handleLoadFromCloud(MODE_ADD)
                true
            }
            R.id.saveToCloud -> {
                if (FirebaseAuth.getInstance().currentUser == null) {
                    handleAuth()
                    tryingToSave = true
                } else handlePickToExport(MODE_SAVE_TO_CLOUD)
                true
            }
            R.id.deleteFromCloud -> {
                if (FirebaseAuth.getInstance().currentUser == null) {
                    handleAuth()
                    tryingToDelete = true
                } else handleLoadFromCloud(MODE_DELETE)
                true
            }
            R.id.auth -> {
                handleAuth()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun handlePickToExport(mode: String) {
        val areChecked = BooleanArray(workouts.size)
        val workoutNames = arrayOfNulls<String>(workouts.size)
        for (i in workouts.indices) {
            workoutNames[i] = workouts[i].name
            areChecked[i] = false
        }
        AlertDialog.Builder(this, R.style.DefaultAlertDialogTheme)
            .setTitle(if (mode == MODE_SHARE) R.string.selectWorkoutsToShare else R.string.selectWorkoutsToSave)
            .setIcon(if (mode == MODE_SHARE) R.drawable.ic_share else R.drawable.ic_save)
            .setMultiChoiceItems(workoutNames, areChecked) { _: DialogInterface?, which: Int, isChecked: Boolean -> areChecked[which] = isChecked }
            .setPositiveButton(if (mode == MODE_SHARE) R.string.share else R.string.save) { _: DialogInterface?, _: Int ->
                workoutsToSave.clear()
                val size = workouts.size
                for (i in 0 until size) {
                    if (areChecked[i]) {
                        workoutsToSave.add(workouts[i])
                    }
                }
                if (workoutsToSave.isEmpty()) {
                    Toast.makeText(this@MainActivity, R.string.noWorkoutsSelected, Toast.LENGTH_SHORT).show()
                } else {
                    when (mode) {
                        MODE_SHARE -> handleShare(workoutsToSave)
                        MODE_SAVE_TO_FILE -> handleSaveToFile()
                        MODE_SAVE_TO_CLOUD -> handleSaveToCloud(workoutsToSave)
                    }
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun handleSaveToFile() {
        createFile()
    }

    private fun handleSaveToCloud(workouts: ArrayList<Workout>) {
        if (user != null) {
            val usersRef = db.collection("users")
            val query = usersRef.whereEqualTo("id", user!!.uid)
            query.get().addOnCompleteListener { task: Task<QuerySnapshot> ->
                if (task.result.size() == 0) {
                    addNewUser(workouts)
                } else {
                    userRef = task.result.documents[0].reference
                    addWorkoutsToUser(workouts)
                }
            }
        }
    }

    private fun addNewUser(workouts: ArrayList<Workout>) {
        val newUser: MutableMap<String, String> = HashMap()
        newUser["id"] = user!!.uid
        db.collection("users").add(newUser)
            .addOnSuccessListener { documentReference: DocumentReference ->
                userRef = documentReference
                addWorkoutsToUser(workouts)
                Log.d(MAIN_ACTIVITY_TAG, "onSuccess: " + documentReference.id)
            }
            .addOnFailureListener { e: Exception -> Log.d(MAIN_ACTIVITY_TAG, "onFailure: $e") }
    }

    private fun addWorkoutsToUser(workouts: ArrayList<Workout>) {
        for (w in workouts) {
            if (w.cloudID == "") {
                insertWorkoutToCloud(w)
            } else {
                userRef!!.collection("workouts").whereEqualTo("id", w.id).get().addOnCompleteListener { task: Task<QuerySnapshot> ->
                    if (task.result.isEmpty) {
                        insertWorkoutToCloud(w)
                    } else {
                        updateWorkoutInCloud(task.result.documents[0].reference, w)
                    }
                }
            }
        }
    }

    private fun updateWorkoutInCloud(workoutRef: DocumentReference, w: Workout?) {
        workoutRef.update(workoutToMap(w!!))
            .addOnSuccessListener { Toast.makeText(this@MainActivity, w.name + " - " + resources.getString(R.string.updated), Toast.LENGTH_SHORT).show() }
            .addOnFailureListener { Toast.makeText(this@MainActivity, w.name + " - " + resources.getString(R.string.unableToUpdate), Toast.LENGTH_SHORT).show() }
    }

    private fun insertWorkoutToCloud(w: Workout?) {
        userRef!!.collection("workouts").add(workoutToMap(w!!))
            .addOnSuccessListener { workoutRef: DocumentReference ->
                Toast.makeText(this@MainActivity, w.name + " - " + resources.getString(R.string.saved), Toast.LENGTH_SHORT).show()
                workoutRef.update("cloudId", workoutRef.id)
                w.cloudID = workoutRef.id
                if (w.exercises.isNotEmpty()) insertExercisesToWorkout(workoutRef, w)
            }
            .addOnFailureListener { Toast.makeText(this@MainActivity, w.name + " - " + resources.getString(R.string.unableToSave), Toast.LENGTH_SHORT).show() }
    }

    private fun insertExercisesToWorkout(workoutRef: DocumentReference, workout: Workout?) {
        for (i in workout!!.exercises.indices) {
            val exercise = workout.exercises[i]
            workoutRef.collection("exercises").add(exerciseToMap(exercise))
                .addOnSuccessListener { exerciseRef: DocumentReference ->
                    exerciseRef.update("position", i)
                    if (exercise.sessions.isNotEmpty()) insertSessionsToExercise(exerciseRef, exercise)
                }
                .addOnFailureListener { }
        }
    }

    private fun insertSessionsToExercise(exerciseRef: DocumentReference, exercise: Exercise) {
        for (i in exercise.sessions.indices) {
            val session = exercise.sessions[i]
            exerciseRef.collection("sessions").add(sessionToMap(session))
                .addOnSuccessListener { }
                .addOnFailureListener { }
        }
    }

    private fun handleLoadFromCloud(mode: String) {
        showProgressBar(binding.cloudProgressBarParent, binding.parent)
        val usersRef = db.collection("users")
        val query = usersRef.whereEqualTo("id", user!!.uid)
        query.get().addOnCompleteListener { task: Task<QuerySnapshot?> ->
            binding.cloudProgressBar.visibility = View.GONE
            if (task.result != null) {
                if (task.result!!.documents.isNotEmpty()) {
                    userRef = task.result!!.documents[0].reference
                    buildQueriedWorkouts(userRef!!, mode)
                } else {
                    Toast.makeText(this, "" + resources.getString(R.string.noWorkoutsToLoad), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun buildQueriedWorkouts(userRef: DocumentReference, mode: String) {
        val workoutsRef = userRef.collection("workouts")
        workoutsRef.orderBy("timestamp").get().addOnCompleteListener { task: Task<QuerySnapshot> ->
            hideProgressBar(binding.cloudProgressBarParent, binding.parent)
            val workoutsAndExercisesReferences: MutableMap<Workout, CollectionReference> = HashMap()
            for (docSnapshot in task.result.documents) {
                if (docSnapshot.data != null) workoutsAndExercisesReferences[mapToWorkout(docSnapshot.data!!)] = docSnapshot.reference.collection("exercises")
            }
            handlePickFromCloud(workoutsAndExercisesReferences, mode, workoutsRef)
        }
    }

    private fun handlePickFromCloud(workoutsAndExercisesReferences: MutableMap<Workout, CollectionReference>, mode: String, workoutsRef: CollectionReference) {
        val queriedWorkouts = ArrayList(workoutsAndExercisesReferences.keys)
        val areChecked = BooleanArray(queriedWorkouts.size)
        val workoutNames = arrayOfNulls<String>(queriedWorkouts.size)
        for (i in queriedWorkouts.indices) {
            workoutNames[i] = queriedWorkouts[i].name
            areChecked[i] = false
        }
        AlertDialog.Builder(this, R.style.DefaultAlertDialogTheme)
            .setTitle(if (mode == MODE_ADD) R.string.selectWorkoutsToLoad else R.string.selectWorkoutsToDelete)
            .setIcon(if (mode == MODE_ADD) R.drawable.ic_load else R.drawable.ic_delete)
            .setMultiChoiceItems(workoutNames, areChecked) { _: DialogInterface?, which: Int, isChecked: Boolean -> areChecked[which] = isChecked }
            .setPositiveButton(if (mode == MODE_ADD) R.string.load else R.string.delete) { _: DialogInterface?, _: Int ->
                if (mode == MODE_ADD) {
                    showProgressBar(binding.cloudProgressBarParent, binding.parent)
                    val size = queriedWorkouts.size
                    for (i in 0 until size) {
                        if (!areChecked[i]) {
                            workoutsAndExercisesReferences.remove(queriedWorkouts[i])
                        }
                    }
                    if (workoutsAndExercisesReferences.keys.isEmpty()) {
                        Toast.makeText(this@MainActivity, R.string.noWorkoutsSelected, Toast.LENGTH_SHORT).show()
                    } else {
                        addExercisesToQueriedWorkouts(workoutsAndExercisesReferences)
                    }
                } else if (mode == MODE_DELETE) handleDeleteFromCloud(areChecked, queriedWorkouts, workoutsRef)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun handleDeleteFromCloud(areChecked: BooleanArray, queriedWorkouts: ArrayList<Workout>, workoutsRef: CollectionReference) {
        for (i in queriedWorkouts.indices) {
            if (areChecked[i]) {
                val workoutRef = workoutsRef.document(queriedWorkouts[i].cloudID)
                workoutRef.delete().addOnCompleteListener { task: Task<Void?> ->
                    if (task.isSuccessful) {
                        Toast.makeText(this@MainActivity, queriedWorkouts[i].name + " - " + resources.getString(R.string.deleted), Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@MainActivity, queriedWorkouts[i].name + " - " + resources.getString(R.string.unableToDelete), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun addExercisesToQueriedWorkouts(workoutsAndExercisesReferences: Map<Workout, CollectionReference>) {
        val queriedWorkouts = ArrayList(workoutsAndExercisesReferences.keys)
        val exerciseReferences = ArrayList(workoutsAndExercisesReferences.values)
        for (i in queriedWorkouts.indices) {
            val exercisesRef = exerciseReferences[i]
            exercisesRef.orderBy("position").get().addOnCompleteListener { task: Task<QuerySnapshot> ->
                counter++
                val queriedExercises = ArrayList<Exercise>()
                val sessionReferences = ArrayList<CollectionReference>()
                for (docSnapshot in task.result.documents) {
                    if (docSnapshot.data != null) {
                        queriedExercises.add(mapToExercise(docSnapshot.data!!))
                        sessionReferences.add(docSnapshot.reference.collection("sessions"))
                    }
                }
                queriedWorkouts[i].exercises = queriedExercises
                addWorkoutAndSetIds(queriedWorkouts[i])
                addSessionsToQueriedWorkouts(sessionReferences, workouts[workouts.size - 1].exercises)
            }
        }
    }

    private fun addWorkoutAndSetIds(w: Workout) {
        var id = getInstance(this).workoutsId
        w.id = id
        id++
        getInstance(this).workoutsId = id
        if (w.exercises.isNotEmpty()) {
            var idE = getInstance(this).exercisesId
            for (e in w.exercises) {
                e.id = idE
                idE++
            }
            getInstance(this).exercisesId = idE
        }
        workouts.add(w)
        workworkoutsRVAdapter.notifyItemInserted(workouts.size - 1)
        workouts[0].exercises[0].sessions[0].rir?.let {
            Toast.makeText(this, "is not null", Toast.LENGTH_LONG).show()
        } ?: run {
            Toast.makeText(this, "is null", Toast.LENGTH_LONG).show()
        }
    }

    private fun addSessionsToQueriedWorkouts(sessionReferences: ArrayList<CollectionReference>, exercises: ArrayList<Exercise>) {
        for (i in exercises.indices) {
            val sessionsRef = sessionReferences[i]
            val queriedSessions = ArrayList<Session>()
            sessionsRef.orderBy(SESSION_DATE_KEY, Query.Direction.DESCENDING).get().addOnCompleteListener { task: Task<QuerySnapshot> ->
                for (docSnapshot in task.result.documents) {
                    if (docSnapshot.data != null) queriedSessions.add(mapToSession(docSnapshot.data!!))
                }
                exercises[i].sessions = queriedSessions
            }
            hideProgressBar(binding.cloudProgressBarParent, binding.parent)
        }
    }

    private fun handleAuth() {
        if (user == null) {
            val providers = listOf(
                EmailBuilder().build(),
                PhoneBuilder().build(),
                GoogleBuilder().build()
            )
            val signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build()
            signInLauncher.launch(signInIntent)
        } else {
            AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener {
                    Toast.makeText(this@MainActivity, resources.getString(R.string.signedOut), Toast.LENGTH_SHORT).show()
                    user = null
                }
            authMenuItem!!.title = resources.getString(R.string.signIn)
        }
    }

    private fun handleShare(workoutsToSave: ArrayList<Workout>) {
        if (workoutsToSave.isEmpty()) {
            Toast.makeText(this@MainActivity, R.string.noWorkoutsSelected, Toast.LENGTH_SHORT).show()
        } else {
            val directory = File(filesDir.toString())
            if (!directory.exists()) {
                directory.mkdirs()
            }
            directoryFile = File("$directory/gbShare.txt")
            try {
                val uri = FileProvider.getUriForFile(this, "com.example.gymbuddy.fileprovider", directoryFile!!)
                val pfd = contentResolver.openFileDescriptor(uri, "w")
                val fileOutputStream = FileOutputStream(pfd!!.fileDescriptor)
                fileOutputStream.write(gson.toJson(workoutsToSave).toByteArray())
                fileOutputStream.close()
                val intent = Intent().apply {
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    type = "text/plain"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    action = Intent.ACTION_SEND
                }
                shareWorkoutsLauncher.launch(Intent.createChooser(intent, getString(R.string.shareWorkoutsVia)))
            } catch (e: IOException) {
                e.printStackTrace()
                Log.d(MAIN_ACTIVITY_TAG, "saveToFile: $e")
            }
        }
    }

    private fun handleImportFromFile(workoutsToLoad: ArrayList<Workout>?) {
        if (workoutsToLoad == null || workoutsToLoad.isEmpty()) {
            Toast.makeText(this@MainActivity, R.string.noWorkoutsToLoad, Toast.LENGTH_SHORT).show()
        } else {
            val areChecked = BooleanArray(workoutsToLoad.size)
            val workoutNames = arrayOfNulls<String>(workoutsToLoad.size)
            for (i in workoutsToLoad.indices) {
                workoutNames[i] = workoutsToLoad[i].name
                areChecked[i] = false
            }
            AlertDialog.Builder(this, R.style.DefaultAlertDialogTheme)
                .setTitle(R.string.selectWorkoutsToLoad)
                .setIcon(R.drawable.ic_load)
                .setMultiChoiceItems(workoutNames, areChecked) { _: DialogInterface?, which12: Int, isChecked: Boolean -> areChecked[which12] = isChecked }
                .setPositiveButton(R.string.load) { _: DialogInterface?, _: Int ->
                    val size = workoutsToLoad.size
                    for (i in 0 until size) {
                        if (areChecked[i]) {
                            addWorkoutAndSetIds(workoutsToLoad[i])
                        }
                    }
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }
    }

    private fun handleCalendarSelection(item: MenuItem) {
        getGmailCalendarIds(this)
        val accountNames: Array<String?>
        var checkedItem = 0
        if (calendarName!!.isEmpty()) {
            accountNames = arrayOf(resources.getString(R.string.def))
        } else {
            accountNames = arrayOfNulls(calendarName!!.size + 1)
            accountNames[0] = resources.getString(R.string.def)
            for (i in calendarName!!.indices) {
                accountNames[i + 1] = calendarName!![i]
                if (calendarId!![i] == getInstance(this@MainActivity).calendarID) {
                    checkedItem = i + 1
                }
            }
        }
        AlertDialog.Builder(this, R.style.DefaultAlertDialogTheme)
            .setTitle(R.string.selectCalendar)
            .setIcon(R.drawable.ic_calendar)
            .setSingleChoiceItems(accountNames, checkedItem) { _: DialogInterface?, which: Int -> selected = which }
            .setPositiveButton(R.string.ok) { _: DialogInterface?, _: Int ->
                loggingEnabled = true
                getInstance(this@MainActivity).isLoggingEnabled = true
                item.title=resources.getString(R.string.disableLogging)
                if (selected == 0) {
                    getInstance(this@MainActivity).calendarID = ""
                } else {
                    getInstance(this@MainActivity).calendarID = calendarId!![selected - 1]
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun getGmailCalendarIds(c: Context) {
        calendarId = ArrayList()
        calendarName = ArrayList()
        val projection = arrayOf("_id", "calendar_displayName")
        val calendars: Uri = Uri.parse("content://com.android.calendar/calendars")
        val contentResolver = c.contentResolver
        val managedCursor = contentResolver.query(
            calendars,
            projection, null, null, null
        )
        if (managedCursor!!.moveToFirst()) {
            var calName: String
            var calID: String
            val nameCol = managedCursor.getColumnIndex(projection[1])
            val idCol = managedCursor.getColumnIndex(projection[0])
            do {
                calName = managedCursor.getString(nameCol)
                calID = managedCursor.getString(idCol)
                if (calName.contains("@gmail")) {
                    calendarId!!.add(calID)
                    calendarName!!.add(calName)
                }
            } while (managedCursor.moveToNext())
            managedCursor.close()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        exercisesShown = getInstance(this).areExercisesShown
        setTheme(R.style.Theme_GymBuddy)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        setContentView(binding.root)
        FirebaseAnalytics.getInstance(this)
        user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            buildAndShowSignedInMessage(user)
        }
//        getInstance(this).rewardGranted = 0
        setupActionBar(getString(R.string.workouts), "", supportActionBar, this)
        getInstance(this).lastAdShown = 0
        showRatingUserInterface(this)
        MobileAds.initialize(this) { initializationStatus: InitializationStatus ->
            val statusMap = initializationStatus.adapterStatusMap
            for (adapterClass in statusMap.keys) {
                val status = statusMap[adapterClass]
                Log.d(
                    "GB", String.format(
                        "Adapter name: %s, Description: %s, Latency: %d",
                        adapterClass, if (status != null) status.description else "null", status?.latency ?: 0
                    )
                )
            }
            handleNativeAds(binding.mainAdTemplate, this, Helpers.AD_ID_MAIN_NATIVE, null, Helpers.isRewardGranted(getInstance(this).rewardGranted))
            if (!Helpers.rewardGrantedThisSession) loadRewardedAd()
        }

        viewModel.rewardGranted.observe(this, { rewardGranted ->
            binding.mainAdContainer.visibility = if (rewardGranted) View.GONE else View.VISIBLE
            if (rewardGranted) binding.rewardedAdButton.visibility = View.GONE
        })

        viewModel.setRewardGranted(Helpers.rewardGrantedThisSession)

        workouts = getInstance(this).allWorkouts!!
        if (workouts.isNotEmpty()) {
            if (workouts[0].cloudID == "") {
                for (w in workouts) {
                    try {
                        w.cloudID = ""
                        w.timestamp = null
                        Log.d(MAIN_ACTIVITY_TAG, "setWorkoutCloudIdSuccessful: " + w.name)
                        if (w.exercises.isNotEmpty()) {
                            if (w.exercises[0].cloudID == "") {
                                for (e in w.exercises) {
                                    try {
                                        e.cloudID = ""
                                        e.timestamp = null
                                        Log.d(MAIN_ACTIVITY_TAG, "setExerciseCloudIdSuccessful: " + e.name)
                                    } catch (exception: Exception) {
                                        Log.d(MAIN_ACTIVITY_TAG, "setExerciseCloudIdFailed: " + e.name + " " + e)
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.d(MAIN_ACTIVITY_TAG, "setWorkoutCloudIdFailed: " + w.name + " " + e)
                    }
                }
                getInstance(this).updateWorkouts(workouts)
            }
            Log.d(MAIN_ACTIVITY_TAG, "setWorkoutCloudId: all good")
        }
        loggingEnabled = getInstance(this).isLoggingEnabled
        groupingEnabled = getInstance(this).isGroupingEnabled
        workworkoutsRVAdapter = WorkoutsRVAdapter(mContext = this,
            menuListener = { position, view -> onMenuClick(position, view) },
            parentListener = { position -> onParentClick(position) }).apply {
            setGroupingEnabled(groupingEnabled)
            setWorkouts(workouts)
        }
        binding.workoutsRV.apply {
            adapter = workworkoutsRVAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (dy > 0) {
                        if (binding.btnAddWorkout.isShown) {
                            binding.btnAddWorkout.visibility = View.GONE
                        }
                    } else if (dy < 0) {
                        if (!binding.btnAddWorkout.isShown) {
                            binding.btnAddWorkout.show()
                        }
                    }
                }
            })
        }

        val itemTouchHelper = ItemTouchHelper(simpleCallback)
        itemTouchHelper.attachToRecyclerView(binding.workoutsRV)
        binding.btnSchedule.setOnClickListener {
            checkingForCalendarForScheduling = true
            if (verifyCalendarPermissions(this@MainActivity)) {
                handleSchedule()
            }
        }

        binding.rewardedAdButton.setOnClickListener {
            AlertDialog.Builder(this, R.style.DefaultAlertDialogTheme)
                .setTitle(R.string.disableAds)
                .setMessage(R.string.disableAdsInfo)
                .setIcon(R.drawable.ic_info)
                .setPositiveButton(R.string.ok) { _: DialogInterface?, _: Int ->
                    if (mRewardedAd != null) {
                        mRewardedAd?.show(this) {
                            grantReward()
                        }
                    } else {
                        AlertDialog.Builder(this, R.style.DefaultAlertDialogTheme)
                            .setTitle(R.string.failedToLoad)
                            .setMessage(R.string.failedToLoadInfo)
                            .setIcon(R.drawable.ic_info)
                            .setPositiveButton(R.string.ok) { _: DialogInterface?, _: Int ->
                                grantReward()
                            }
                            .show()
                    }
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }

        binding.btnAddWorkout.setOnClickListener {
            val i = Intent(this@MainActivity, AddWorkoutActivity::class.java)
            addWorkoutLauncher.launch(i)
        }
    }

    private fun grantReward() {
        viewModel.setRewardGranted(true)
        getInstance(this).rewardGranted = System.currentTimeMillis()
        Helpers.rewardGrantedThisSession = true
    }

    private fun loadRewardedAd() {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(this, Helpers.AD_ID_REWARDED, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
//                Toast.makeText(this@MainActivity, "failed to load $adError", Toast.LENGTH_LONG).show()
                mRewardedAd = null
            }

            override fun onAdLoaded(rewardedAd: RewardedAd) {
                binding.rewardedAdButton.visibility = View.VISIBLE
                mRewardedAd = rewardedAd
            }
        })
    }

    private fun buildAndShowSignedInMessage(user: FirebaseUser?) {
        val text = StringBuilder()
        text.append(resources.getString(R.string.signedInAs))
        text.append(" ")
        if (user!!.phoneNumber != null && user.phoneNumber!!.isNotEmpty()) text.append(user.phoneNumber)
        if (user.email != null && user.email!!.isNotEmpty()) text.append(user.email)
        Toast.makeText(this, text.toString(), Toast.LENGTH_SHORT).show()
    }

    private fun handleSchedule() {
        val intent = Intent(this, ScheduleActivity::class.java)
        startActivity(intent)
    }

    private var simpleCallback: ItemTouchHelper.SimpleCallback = object : ItemTouchHelper.SimpleCallback(
        ItemTouchHelper.UP or ItemTouchHelper.DOWN or
                ItemTouchHelper.START or ItemTouchHelper.END, 0
    ) {
        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            val fromPosition = viewHolder.adapterPosition
            val toPosition = target.adapterPosition
            Collections.swap(workouts, fromPosition, toPosition)
            workworkoutsRVAdapter.notifyItemMoved(fromPosition, toPosition)
            getInstance(this@MainActivity).updateWorkouts(workouts)
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
            super.clearView(recyclerView, viewHolder)
            refreshGrouping()
        }
    }

    private fun refreshGrouping() {
        if (groupingEnabled && workouts.size > 2) {
            var header = binding.workoutsRV.findViewHolderForAdapterPosition(0)!!.itemView.findViewById<TextView>(R.id.txtTypeHeader)
            if (!header.isShown) workworkoutsRVAdapter.notifyItemChanged(0)
            for (i in 0 until workouts.size - 1) {
                header = binding.workoutsRV.findViewHolderForAdapterPosition(i + 1)!!.itemView.findViewById(R.id.txtTypeHeader)
                if (workoutTypeComparator(workouts[i].type, workouts[i + 1].type) && header.isShown)
                    workworkoutsRVAdapter.notifyItemChanged(i + 1)
                else if (!workoutTypeComparator(workouts[i].type, workouts[i + 1].type) && !header.isShown) workworkoutsRVAdapter.notifyItemChanged(i + 1)
            }
        }
    }

    private fun onMenuClick(position: Int, view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.inflate(R.menu.popup_menu)
        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.menuEdit -> {
                    val intent = Intent(this, AddWorkoutActivity::class.java)
                    intent.putExtra(Helpers.WORKOUTS_KEY, workouts)
                    intent.putExtra(Helpers.POSITION_KEY, position)
                    startActivity(intent)
                    return@setOnMenuItemClickListener true
                }
                R.id.menuDelete -> {
                    AlertDialog.Builder(this, R.style.DefaultAlertDialogTheme)
                        .setMessage(R.string.sureDeleteThisWorkout)
                        .setIcon(R.drawable.ic_delete)
                        .setPositiveButton(R.string.yes) { _: DialogInterface?, _: Int ->
                            getInstance(this).deleteWorkout(workouts[position])
                            workouts.remove(workouts[position])
                            workworkoutsRVAdapter.notifyItemRemoved(position)
                            refreshGrouping()
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

    private fun onParentClick(position: Int) {
        val intent = Intent(this, ExercisesActivity::class.java)
        intent.putExtra(Helpers.EXERCISES_KEY, workouts[position])
        startActivity(intent)
    }
}
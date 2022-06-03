package com.example.gymbuddy.helpers

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.gymbuddy.helpers.Helpers.REWARD_GRANTED_KEY
import com.example.gymbuddy.helpers.Helpers.RIR_KEY
import com.example.gymbuddy.model.Exercise
import com.example.gymbuddy.model.Workout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

class Utils(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("workouts_db", Context.MODE_PRIVATE)
    private val exportedWorkouts: ArrayList<Workout>
    private fun initData() {
        val workouts = ArrayList<Workout>()
        val editor = sharedPreferences.edit()
        val gson = Gson()
        editor.putString(ALL_WORKOUTS_KEY, gson.toJson(workouts))
        editor.apply()
    }

    var rirEnabled:Boolean
        get() = sharedPreferences.getBoolean(RIR_KEY,true)
        set(rirEnabled) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(RIR_KEY,rirEnabled)
            editor.apply()
        }

    var rewardGranted:Long
        get() = sharedPreferences.getLong(REWARD_GRANTED_KEY,0)
        set(rewardGranted) {
            val editor = sharedPreferences.edit()
            editor.putLong(REWARD_GRANTED_KEY,rewardGranted)
            editor.apply()
        }

    var lastAdShown: Long
        get() = sharedPreferences.getLong(LAST_AD_SHOWN_KEY, 0)
        set(lastAdShown) {
            val editor = sharedPreferences.edit()
            editor.putLong(LAST_AD_SHOWN_KEY, lastAdShown)
            editor.apply()
        }
    var lastAppRating: Long
        get() = sharedPreferences.getLong(APP_RATING_KEY, 0)
        set(lastAppRating) {
            val editor = sharedPreferences.edit()
            editor.putLong(APP_RATING_KEY, lastAppRating)
            editor.apply()
        }
    val allWorkouts: ArrayList<Workout>?
        get() {
            val gson = Gson()
            val type = object : TypeToken<ArrayList<Workout?>?>() {}.type
            return gson.fromJson(sharedPreferences.getString(ALL_WORKOUTS_KEY, null), type)
        }
    var calendarID: String
        get() = sharedPreferences.getString(CALENDAR_ID_KEY, "").toString()
        set(calendarID) {
            val editor = sharedPreferences.edit()
            editor.putString(CALENDAR_ID_KEY, calendarID)
            editor.apply()
        }
    var workoutsId: Int
        get() = sharedPreferences.getInt(WORKOUTS_ID_KEY, 0)
        set(workoutsID) {
            val editor = sharedPreferences.edit()
            editor.putInt(WORKOUTS_ID_KEY, workoutsID)
            editor.apply()
        }
    var exercisesId: Int
        get() = sharedPreferences.getInt(EXERCISES_ID_KEY, 0)
        set(exercisesID) {
            val editor = sharedPreferences.edit()
            editor.putInt(EXERCISES_ID_KEY, exercisesID)
            editor.apply()
        }
    var breakLeft: Long
        get() = sharedPreferences.getLong(BREAK_LEFT_KEY, 0)
        set(breakLeft) {
            val editor = sharedPreferences.edit()
            editor.putLong(BREAK_LEFT_KEY, breakLeft)
            editor.apply()
        }
    var systemTime: Long
        get() = sharedPreferences.getLong(SYSTEM_TIME_KEY, 0)
        set(systemTime) {
            val editor = sharedPreferences.edit()
            editor.putLong(SYSTEM_TIME_KEY, systemTime)
            editor.apply()
        }

    fun getExportedWorkouts(): ArrayList<Workout> {
        val gson = Gson()
        val type = object : TypeToken<ArrayList<Workout?>?>() {}.type
        return gson.fromJson(sharedPreferences.getString(EXPORTED_WORKOUTS_KEY, null), type)
    }

    fun setExportedWorkouts(exportedWorkouts: ArrayList<Workout?>?) {
        val gson = Gson()
        val editor = sharedPreferences.edit()
        editor.remove(EXPORTED_WORKOUTS_KEY)
        editor.putString(EXPORTED_WORKOUTS_KEY, gson.toJson(exportedWorkouts))
        editor.apply()
    }

    var areExercisesShown: Boolean
        get() = sharedPreferences.getBoolean(ARE_EXERCISES_SHOWN_KEY, true)
        set(areExercisesShown) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(ARE_EXERCISES_SHOWN_KEY, areExercisesShown)
            editor.apply()
        }
    var isLoggingEnabled: Boolean
        get() = sharedPreferences.getBoolean(LOGGING_KEY, false)
        set(loggingEnabled) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(LOGGING_KEY, loggingEnabled)
            editor.apply()
        }
    var isGroupingEnabled: Boolean
        get() = sharedPreferences.getBoolean(GROUPING_KEY, false)
        set(groupingEnabled) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(GROUPING_KEY, groupingEnabled)
            editor.apply()
        }

    fun updateWorkouts(workouts: ArrayList<Workout>) {
        val gson = Gson()
        val editor = sharedPreferences.edit()
        editor.remove(ALL_WORKOUTS_KEY)
        editor.putString(ALL_WORKOUTS_KEY, gson.toJson(workouts))
        editor.apply()
    }

    fun updateSingleWorkout(workout: Workout) {
        val workouts = allWorkouts
        if (null != workouts) {
            for (w in workouts) {
                if (w.id == workout.id) {
                    w.state = workout.state
                    w.timestamp = workout.timestamp
                    w.cloudID = workout.cloudID
                    val gson = Gson()
                    val editor = sharedPreferences.edit()
                    editor.remove(ALL_WORKOUTS_KEY)
                    editor.putString(ALL_WORKOUTS_KEY, gson.toJson(workouts))
                    editor.apply()
                    return
                }
            }
        }
    }

    fun addToAllWorkouts(workout: Workout) {
        var id = workoutsId
        workout.id = id
        id++
        workoutsId = id
        if (workout.exercises.isNotEmpty()) {
            var idE = exercisesId
            for (e in workout.exercises) {
                e.id = idE
                idE++
            }
            exercisesId = idE
        }
        val workouts = allWorkouts
        if (null != workouts) {
            if (workouts.add(workout)) {
                val gson = Gson()
                val editor = sharedPreferences.edit()
                editor.remove(ALL_WORKOUTS_KEY)
                editor.putString(ALL_WORKOUTS_KEY, gson.toJson(workouts))
                editor.apply()
            }
        }
    }

    fun deleteWorkout(workout: Workout) {
        val workouts = allWorkouts
        if (null != workouts) {
            for (w in workouts) {
                if (w.id == workout.id) {
                    if (workouts.remove(w)) {
                        val gson = Gson()
                        val editor = sharedPreferences.edit()
                        editor.remove(ALL_WORKOUTS_KEY)
                        editor.putString(ALL_WORKOUTS_KEY, gson.toJson(workouts))
                        editor.apply()
                        return
                    }
                }
            }
        }
    }

    fun addExerciseToWorkout(workout: Workout, exercise: Exercise) {
        var id = exercisesId
        exercise.id = id
        id++
        exercisesId = id
        val workouts = allWorkouts
        if (null != workouts) {
            for (w in workouts) {
                if (w.id == workout.id) {
                    val exercises = w.exercises
                    if (exercises.add(exercise)) {
                        w.exercises = exercises
                        w.exerciseNumber = w.exercises.size
                        val gson = Gson()
                        val editor = sharedPreferences.edit()
                        editor.remove(ALL_WORKOUTS_KEY)
                        editor.putString(ALL_WORKOUTS_KEY, gson.toJson(workouts))
                        editor.apply()
                        return
                    }
                }
            }
        }
    }

    fun deleteExerciseFromWorkout(exercise: Exercise) {
        val workouts = allWorkouts
        if (null != workouts) {
            for (w in workouts) {
                val exercises = w.exercises
                for (e in exercises) if (e.id == exercise.id) {
                    if (exercises.remove(e)) {
                        w.exercises = exercises
                        w.state = intArrayOf(1, 1, 1)
                        w.exerciseNumber = w.exercises.size
                        val gson = Gson()
                        val editor = sharedPreferences.edit()
                        editor.remove(ALL_WORKOUTS_KEY)
                        editor.putString(ALL_WORKOUTS_KEY, gson.toJson(workouts))
                        editor.apply()
                        return
                    }
                }
            }
        }
    }

    fun updateWorkoutsExercises(workout: Workout, exercises: ArrayList<Exercise>) {
        val workouts = allWorkouts
        if (null != workouts) {
            for (w in workouts) {
                if (w.id == workout.id) {
                    w.state = workout.state
                    w.exercises = exercises
                    Log.d(TAG, "updateWorkoutsExercises: updated")
                    val gson = Gson()
                    val editor = sharedPreferences.edit()
                    editor.remove(ALL_WORKOUTS_KEY)
                    editor.putString(ALL_WORKOUTS_KEY, gson.toJson(workouts))
                    editor.apply()
                    return
                }
            }
        }
    }

    fun updateWorkoutsExercisesWithoutWorkout(exercises: ArrayList<Exercise>) {
        val workouts = allWorkouts
        if (null != workouts) {
            for (w in workouts) {
                if (w.exercises[0].id == exercises[0].id) {
                    w.exercises = exercises
                    val gson = Gson()
                    val editor = sharedPreferences.edit()
                    editor.remove(ALL_WORKOUTS_KEY)
                    editor.putString(ALL_WORKOUTS_KEY, gson.toJson(workouts))
                    editor.apply()
                    return
                }
            }
        }
    }

    companion object INSTANCE {
        private var instance: Utils? = null
        private const val ALL_WORKOUTS_KEY = "all workouts"
        private const val WORKOUTS_ID_KEY = "workoutsID"
        private const val EXERCISES_ID_KEY = "exercisesID"
        const val ARE_EXERCISES_SHOWN_KEY = "are shown"
        const val EXPORTED_WORKOUTS_KEY = "exported"
        const val SYSTEM_TIME_KEY = "system time"
        const val BREAK_LEFT_KEY = "break left"
        const val LOGGING_KEY = "logging"
        const val GROUPING_KEY = "grouping"
        const val CALENDAR_ID_KEY = "calendar"
        const val APP_RATING_KEY = "app rating"
        const val LAST_AD_SHOWN_KEY = "last ad"
        private const val TAG = "Utils"
        var areExercisesShown: Boolean = false
        var loggingEnabled: Boolean = false
        var groupingEnabled: Boolean = false
        var workoutsID: Int = 0
        var exercisesID: Int = 0
        var breakLeft: Long = 0
        var systemTime: Long = 0
        var lastAppRating: Long = 0
        var lastAdShown: Long = 0
        var calendarID: String = ""
        var rewardGranted:Long = 0
        var rirEnabled:Boolean = true

        @JvmStatic
        fun getInstance(context: Context): Utils {
            if (null == instance) {
                instance = Utils(context)
            }
            return instance as Utils
        }
    }

    init {
        if (null == allWorkouts) {
            initData()
        }
        INSTANCE.rirEnabled = rirEnabled
        INSTANCE.calendarID = calendarID
        INSTANCE.workoutsID = workoutsId
        INSTANCE.exercisesID = exercisesId
        INSTANCE.areExercisesShown = areExercisesShown
        INSTANCE.loggingEnabled = isLoggingEnabled
        INSTANCE.groupingEnabled = isGroupingEnabled
        INSTANCE.breakLeft = breakLeft
        INSTANCE.systemTime = systemTime
        INSTANCE.lastAppRating = lastAppRating
        INSTANCE.lastAdShown = lastAdShown
        INSTANCE.rewardGranted = rewardGranted
        exportedWorkouts = ArrayList()
    }
}
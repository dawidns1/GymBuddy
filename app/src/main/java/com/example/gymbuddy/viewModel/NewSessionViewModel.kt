package com.example.gymbuddy.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.gymbuddy.model.Exercise
import com.example.gymbuddy.model.Session
import com.example.gymbuddy.model.Set
import com.example.gymbuddy.model.Workout
import java.util.ArrayList

class NewSessionViewModel: ViewModel() {

    lateinit var exercises: ArrayList<Exercise>
    lateinit var workout: Workout
    var currentSession: ArrayList<Set> = ArrayList()
    private var previous = false
    var previousExercise=-1
    lateinit var sessions: ArrayList<Session>

    private val _exercise=MutableLiveData<Exercise>()
    var exercise:LiveData<Exercise> = _exercise
    fun setExercise(set:Int, switchedToPrevious:Boolean=false){
        previous = switchedToPrevious
        _exercise.value=exercises[currentSession[set].exercise]
    }

    private val _timeLeftInMs=MutableLiveData(0L)
    var timeLeftInMs:LiveData<Long> = _timeLeftInMs
    fun setTimeLeftInMs(timeLeftInMs: Long){
        _timeLeftInMs.value=timeLeftInMs
    }

    private val _exerciseNo=MutableLiveData(0)
    var exerciseNo:LiveData<Int> = _exerciseNo

    private val _exerciseSet=MutableLiveData(1)
    var exerciseSet:LiveData<Int> = _exerciseSet

    private val _currentSet=MutableLiveData(0)
    var currentSet:LiveData<Int> = _currentSet
    fun setCurrentSet(currentSet: Int){
        if(currentSession[currentSet].exercise!=currentSession[_currentSet.value!!].exercise){
            previousExercise=currentSession[_currentSet.value!!].exercise
            _exerciseNo.value=currentSession[currentSet].exercise
        }
        _currentSet.value=currentSet
        _exerciseSet.value=currentSession[currentSet].exerciseSet+1
    }

    private val _backed=MutableLiveData(0)
    var backed:LiveData<Int> = _backed
    fun setBacked(backed: Int){
        _backed.value=backed
    }

    private val _timerRunning=MutableLiveData(false)
    var timerRunning:LiveData<Boolean> = _timerRunning
    fun setTimerRunning(timerRunning: Boolean){
        _timerRunning.value=timerRunning
    }

    private val _breakRunning=MutableLiveData(false)
    var breakRunning:LiveData<Boolean> = _breakRunning
    fun setBreakRunning(breakRunning:Boolean){
        _breakRunning.value=breakRunning
    }

    fun buildSession(workout: Workout) {
        this.workout=workout
        exercises = this.workout.exercises
        for (i in exercises.indices) {
            for (j in 0 until exercises[i].sets) {
                if (exercises[i].superSet != 1 && exercises[i].superSet != 2) {   //if is not superset
                    currentSession.add(Set(i, j))
                    if (j + 1 == exercises[i].sets) {
                        currentSession.last().state = 2
                    }
                } else if (exercises[i].superSet == 1) {    //if is superset
                    currentSession.add(Set(i, j))
                    currentSession.last().state = 2
                    currentSession.add(Set(i + 1, j))
                    currentSession.last().state = 2
                }
            }
        }
        currentSession.last().state = 3
    }

}
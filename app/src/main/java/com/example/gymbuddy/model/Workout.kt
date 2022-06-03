package com.example.gymbuddy.model

import com.example.gymbuddy.helpers.Helpers.WORKOUT_CLOUD_ID_KEY
import com.example.gymbuddy.helpers.Helpers.WORKOUT_EXERCISE_NUMBER_KEY
import com.example.gymbuddy.helpers.Helpers.WORKOUT_ID_KEY
import com.example.gymbuddy.helpers.Helpers.WORKOUT_MUSCLE_GROUP_KEY
import com.example.gymbuddy.helpers.Helpers.WORKOUT_MUSCLE_GROUP_SECONDARY_KEY
import com.example.gymbuddy.helpers.Helpers.WORKOUT_NAME_KEY
import com.example.gymbuddy.helpers.Helpers.WORKOUT_TIMESTAMP_KEY
import com.example.gymbuddy.helpers.Helpers.WORKOUT_TYPE_KEY
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import java.io.Serializable
import java.util.*

@IgnoreExtraProperties
class Workout : Serializable {
    var id: Int
    var name: String
    var exerciseNumber: Int
    var type: String
    var muscleGroup: String
    var muscleGroupSecondary: String
    var exercises: ArrayList<Exercise>
    var state: IntArray
    var cloudID: String

    @ServerTimestamp
    var timestamp: Date?

    constructor(id: Int, name: String, type: String, muscleGroup: String, muscleGroupSecondary: String, exerciseNumber: Int) {
        this.id = id
        this.name = name
        this.type = type
        this.muscleGroup = muscleGroup
        this.muscleGroupSecondary = muscleGroupSecondary
        this.exerciseNumber = exerciseNumber
        exercises = ArrayList()
        state = intArrayOf(1, 1, 1)
        cloudID = ""
        timestamp = null
    }

    constructor(id: Int, name: String, type: String, muscleGroup: String, muscleGroupSecondary: String, exerciseNumber: Int, cloudID: String) {
        this.id = id
        this.name = name
        this.type = type
        this.muscleGroup = muscleGroup
        this.muscleGroupSecondary = muscleGroupSecondary
        this.exerciseNumber = exerciseNumber
        exercises = ArrayList()
        state = intArrayOf(1, 1, 1)
        this.cloudID = cloudID
        timestamp = null
    }

    override fun toString(): String {
        return "Workout{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", exerciseNumber='" + exerciseNumber + '\'' +
                ", type='" + type + '\'' +
                ", muscleGroup='" + muscleGroup + '\'' +
                ", muscleGroupSecondary='" + muscleGroupSecondary + '\'' +
                '}'
    }

    companion object {
        @JvmStatic
        fun workoutToMap(workout: Workout): HashMap<String, Any> {
            val workoutHashMap = HashMap<String, Any>()
            workoutHashMap[WORKOUT_ID_KEY] = workout.id
            workoutHashMap[WORKOUT_NAME_KEY] = workout.name
            workoutHashMap[WORKOUT_EXERCISE_NUMBER_KEY] = workout.exerciseNumber
            workoutHashMap[WORKOUT_TYPE_KEY] = workout.type
            workoutHashMap[WORKOUT_MUSCLE_GROUP_KEY] = workout.muscleGroup
            workoutHashMap[WORKOUT_MUSCLE_GROUP_SECONDARY_KEY] = workout.muscleGroupSecondary
            workoutHashMap[WORKOUT_TIMESTAMP_KEY] = FieldValue.serverTimestamp()
            return workoutHashMap
        }

        @JvmStatic
        fun mapToWorkout(m: Map<String?, Any>): Workout {
            val id = m[WORKOUT_ID_KEY] as Long?
            return Workout(
                id?.toInt() ?: 0, m[WORKOUT_NAME_KEY].toString(), m[WORKOUT_TYPE_KEY].toString(), m[WORKOUT_MUSCLE_GROUP_KEY].toString(), m[WORKOUT_MUSCLE_GROUP_SECONDARY_KEY].toString(),
                0, m[WORKOUT_CLOUD_ID_KEY].toString()
            )
        }
    }
}
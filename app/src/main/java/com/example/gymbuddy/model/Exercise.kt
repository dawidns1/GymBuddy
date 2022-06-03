package com.example.gymbuddy.model

import com.example.gymbuddy.helpers.Helpers.EXERCISE_BREAKS_KEY
import com.example.gymbuddy.helpers.Helpers.EXERCISE_ID_KEY
import com.example.gymbuddy.helpers.Helpers.EXERCISE_MUSCLE_GROUP_KEY
import com.example.gymbuddy.helpers.Helpers.EXERCISE_MUSCLE_GROUP_SECONDARY_KEY
import com.example.gymbuddy.helpers.Helpers.EXERCISE_NAME_KEY
import com.example.gymbuddy.helpers.Helpers.EXERCISE_SETS_KEY
import com.example.gymbuddy.helpers.Helpers.EXERCISE_SUPERSET_KEY
import com.example.gymbuddy.helpers.Helpers.EXERCISE_TEMPO_KEY
import com.example.gymbuddy.helpers.Helpers.EXERCISE_TIMESTAMP_KEY
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import java.io.Serializable
import java.util.*

@IgnoreExtraProperties
class Exercise(var id: Int, var name: String, var sets: Int, var breaks: Int, var muscleGroup: String, var muscleGroupSecondary: String, var tempo: Int) : Serializable, Cloneable {
    var superSet: Int = 0
    var sessions: ArrayList<Session> = ArrayList()
    var cloudID: String = ""
    @ServerTimestamp
    var timestamp: Date? = null
    override fun toString(): String {
        return "Exercise{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", sets='" + sets + '\'' +
                ", breaks='" + breaks + '\'' +
                ", muscleGroup='" + muscleGroup + '\'' +
                ", muscleGroupSecondary='" + muscleGroupSecondary + '\'' +
                '}'
    }

    companion object {
        @JvmStatic
        fun exerciseToMap(exercise: Exercise): HashMap<String, Any> {
            val exerciseHashMap = HashMap<String, Any>()
            exerciseHashMap[EXERCISE_ID_KEY] = exercise.id
            exerciseHashMap[EXERCISE_NAME_KEY] = exercise.name
            exerciseHashMap[EXERCISE_SETS_KEY] = exercise.sets
            exerciseHashMap[EXERCISE_BREAKS_KEY] = exercise.breaks
            exerciseHashMap[EXERCISE_TEMPO_KEY] = exercise.tempo
            exerciseHashMap[EXERCISE_MUSCLE_GROUP_KEY] = exercise.muscleGroup
            exerciseHashMap[EXERCISE_MUSCLE_GROUP_SECONDARY_KEY] = exercise.muscleGroupSecondary
            exerciseHashMap[EXERCISE_SUPERSET_KEY] = exercise.superSet
            exerciseHashMap[EXERCISE_TIMESTAMP_KEY] = FieldValue.serverTimestamp()
            return exerciseHashMap
        }

        @JvmStatic
        fun mapToExercise(m: Map<String?, Any>): Exercise {
            val id = m[EXERCISE_ID_KEY] as Long?
            val sets = m[EXERCISE_SETS_KEY] as Long?
            val breaks = m[EXERCISE_BREAKS_KEY] as Long?
            val tempo = m[EXERCISE_TEMPO_KEY] as Long?
            return Exercise(
                id?.toInt() ?: 0, m[EXERCISE_NAME_KEY].toString(),
                sets?.toInt() ?: 3,
                breaks?.toInt() ?: 120, m[EXERCISE_MUSCLE_GROUP_KEY].toString(), m[EXERCISE_MUSCLE_GROUP_SECONDARY_KEY].toString(),
                tempo?.toInt() ?: 0
            )
        }
    }
}
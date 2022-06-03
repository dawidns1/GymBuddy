package com.example.gymbuddy.model

import com.example.gymbuddy.helpers.Helpers.SESSION_DATE_KEY
import com.example.gymbuddy.helpers.Helpers.SESSION_ID_KEY
import com.example.gymbuddy.helpers.Helpers.SESSION_LOAD_KEY
import com.example.gymbuddy.helpers.Helpers.SESSION_REPS_KEY
import com.example.gymbuddy.helpers.Helpers.SESSION_RIR_KEY
import java.io.Serializable
import java.util.*
import java.util.stream.Collectors

class Session : Serializable {
    var id: Int
    var load: FloatArray
    var reps: IntArray
    var date: String? = null
    var rir: IntArray? = intArrayOf(6,6,6,6,6,6,6,6)

    constructor(id: Int=0, load: FloatArray=floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f), reps: IntArray=intArrayOf(0, 0, 0, 0, 0, 0, 0, 0), rir:IntArray?=intArrayOf(6,6,6,6,6,6,6,6)) {
        this.id = id
        this.load = load
        this.reps = reps
        this.rir=rir
    }

    constructor(id: Int, load: FloatArray, reps: IntArray, date: String?, rir:IntArray?=intArrayOf(6,6,6,6,6,6,6,6)) {
        this.id = id
        this.load = load
        this.reps = reps
        this.date = date
        this.rir = rir
    }

    val total: Float
        get() {
            var total = 0
            for (i in load.indices) {
                total += (load[i] * reps[i]).toInt()
            }
            return total.toFloat()
        }

    companion object {
        @JvmStatic
        fun sessionToMap(session: Session): HashMap<String, Any?> {
            val loadAsDouble = ArrayList<Double>()
            for (i in session.load.indices) {
                loadAsDouble.add(session.load[i].toDouble())
            }
            val sessionHashMap = HashMap<String, Any?>()
            sessionHashMap[SESSION_ID_KEY] = session.id
            sessionHashMap[SESSION_LOAD_KEY] = loadAsDouble
            sessionHashMap[SESSION_REPS_KEY] = Arrays.stream(session.reps).boxed().collect(Collectors.toList())
            sessionHashMap[SESSION_RIR_KEY] = session.rir?.let {
                Arrays.stream(it).boxed().collect(Collectors.toList())
            } ?: run{
                intArrayOf(6,6,6,6,6,6,6,6)
            }
            sessionHashMap[SESSION_DATE_KEY] = session.date
            return sessionHashMap
        }

        @JvmStatic
        fun mapToSession(m: Map<String?, Any>): Session {
            val id = m[SESSION_ID_KEY] as Long?
            val load = m[SESSION_LOAD_KEY] as ArrayList<Double>?
            val reps = m[SESSION_REPS_KEY] as ArrayList<Long>?
            val rir = m[SESSION_RIR_KEY] as ArrayList<Long>?
            val loadPrimitive = FloatArray(load!!.size)
            var k = 0
            for (d in load) {
                loadPrimitive[k++] = d.toFloat()
            }
            return Session(
                id?.toInt() ?: 0,
                loadPrimitive,
                if (reps != null) reps.stream().mapToInt { value: Long? -> Math.toIntExact(value!!) }.toArray() else intArrayOf(0, 0, 0, 0, 0, 0, 0, 0),
                m[SESSION_DATE_KEY].toString(),
                if (rir != null) rir.stream().mapToInt { value: Long? -> Math.toIntExact(value!!) }.toArray() else intArrayOf(6, 6, 6, 6, 6, 6, 6, 6),
            )
        }
    }
}
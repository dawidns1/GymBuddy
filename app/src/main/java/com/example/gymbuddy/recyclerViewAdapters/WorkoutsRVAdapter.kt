package com.example.gymbuddy.recyclerViewAdapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.GONE
import android.view.ViewGroup.MarginLayoutParams
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gymbuddy.R
import com.example.gymbuddy.databinding.ItemListWorkoutBinding
import com.example.gymbuddy.helpers.Helpers.workoutTypeComparator
import com.example.gymbuddy.helpers.Helpers.workoutTypeHeaderGenerator
import com.example.gymbuddy.helpers.Utils
import com.example.gymbuddy.model.Workout
import java.util.*

class WorkoutsRVAdapter(
    private val mContext: Context,
    private val menuListener: (position: Int, view: View) -> Unit,
    private val parentListener: (position: Int) -> Unit
) : RecyclerView.Adapter<WorkoutsRVAdapter.ViewHolder>() {
    private var workouts = ArrayList<Workout>()
    private var adapterInner: WorkoutsExercisesRVAdapter? = null
    private var showHeader = false
    private var groupingEnabled = false
    fun setGroupingEnabled(groupingEnabled: Boolean) {
        this.groupingEnabled = groupingEnabled
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemListWorkoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        adapterInner = WorkoutsExercisesRVAdapter(mContext).apply {
            exercises = workouts[position].exercises
        }
        holder.binding.apply {
            workoutsExercisesRV.adapter = adapterInner
            workoutsExercisesRV.layoutManager = GridLayoutManager(mContext, 2)
            workoutsExercisesRV.suppressLayout(true)
            workoutsExercisesRV.visibility = if (Utils.getInstance(mContext).areExercisesShown) View.VISIBLE else View.GONE
            txtWorkoutName.text = workouts[position].name
            txtExercisesNo.text = when (workouts[position].exercises.size) {
                1 -> "${workouts[position].exercises.size} ${mContext.getString(R.string.exercises1)}"
                2, 3, 4 -> "${workouts[position].exercises.size} ${mContext.getString(R.string.exercises234)}"
                else -> "${workouts[position].exercises.size} ${mContext.getString(R.string.exercises)}"
            }
            txtType.text = workouts[position].type
            txtMuscleGroupSecondary.visibility = View.GONE
            workoutSeparator.visibility = View.GONE

            val params = muscleGroupTxt.layoutParams as MarginLayoutParams
            params.leftMargin = if (txtType.text.toString() == "---" || groupingEnabled) {
                txtType.visibility = View.GONE
                typeTxt.visibility = View.GONE
                0
            } else {
                txtType.visibility = View.VISIBLE
                typeTxt.visibility = View.VISIBLE
                20
            }
            muscleGroupTxt.layoutParams = params
            if (workouts[position].muscleGroup.isEmpty()) {
                txtMuscleGroup.visibility = View.GONE
                muscleGroupTxt.visibility = View.GONE
            } else {
                txtMuscleGroup.text = workouts[position].muscleGroup
                txtMuscleGroup.visibility = View.VISIBLE
                muscleGroupTxt.visibility = View.VISIBLE
                if (workouts[position].muscleGroupSecondary.isNotEmpty()) {
                    txtMuscleGroupSecondary.text = workouts[position].muscleGroupSecondary
                    txtMuscleGroupSecondary.visibility = View.VISIBLE
                    workoutSeparator.visibility = View.VISIBLE
                }
            }
            txtId.text = workouts[position].id.toString()
            txtTypeHeader.text = workoutTypeHeaderGenerator(workouts[position].type, mContext)
            if (showHeader && groupingEnabled) {
                if (position == 0) txtTypeHeader.visibility = View.VISIBLE
                else if (!workoutTypeComparator(workouts[position - 1].type, workouts[position].type)) txtTypeHeader.visibility = View.VISIBLE
                else txtTypeHeader.visibility = View.GONE
            }
        }
    }

    override fun getItemCount(): Int {
        return workouts.size
    }

    fun setWorkouts(workouts: ArrayList<Workout>) {
        this.workouts = workouts
        notifyDataSetChanged()
        if (workouts.size > 1) {
            for (i in 1 until workouts.size) {
                if (workouts[0].type != workouts[i].type) {
                    showHeader = true
                    break
                }
            }
        }
    }

    inner class ViewHolder(var binding: ItemListWorkoutBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.txtTypeHeader.visibility = GONE
            binding.parent.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    parentListener.invoke(adapterPosition)
                }
            }
            binding.btnMenuWorkout.setOnClickListener { v: View ->
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    menuListener.invoke(adapterPosition, v)
                }
            }
        }
    }
}
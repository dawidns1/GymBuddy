package com.example.gymbuddy.recyclerViewAdapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.gymbuddy.R
import com.example.gymbuddy.databinding.ItemListExerciseBinding
import com.example.gymbuddy.model.Exercise
import java.util.*

class ExercisesRVAdapter(
    private val mContext: Context,
    private val menuListener: (position: Int, view: View) -> Unit,
    private val parentListener: (Int) -> Unit,
    var exercises: ArrayList<Exercise>
) : RecyclerView.Adapter<ExercisesRVAdapter.ViewHolder>() {

    override fun getItemId(position: Int): Long {
        return exercises[position].id.toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemListExerciseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.apply {
            txtExerciseName.text = exercises[position].name
            txtIdE.text = exercises[position].id.toString()
            txtSetsNo.text = "${exercises[position].sets} ${
                when (exercises[position].sets) {
                    3, 4 -> mContext.getString(R.string.setTxt34)
                    else -> mContext.getString(R.string.setTxt)
                }
            }"
            when (exercises[position].tempo) {
                9999 -> {
                    txtTempo.text = mContext.resources.getString(R.string.iso)
                    txtTempo.visibility = View.VISIBLE
                }
                0 -> {
                    txtTempo.visibility = View.GONE
                }
                else -> {
                    txtTempo.text = exercises[position].tempo.toString()
                    txtTempo.visibility = View.VISIBLE
                }
            }
            txtMuscleGroupSecondaryE.visibility = View.GONE
            exerciseSeparator.visibility = View.GONE
            if (exercises[position].muscleGroup.isEmpty()) {
                txtMuscleGroupE.visibility = View.GONE
                muscleGroupETxt.visibility = View.GONE
            } else {
                txtMuscleGroupE.text = exercises[position].muscleGroup
                txtMuscleGroupE.visibility = View.VISIBLE
                muscleGroupETxt.visibility = View.VISIBLE
                if (exercises[position].muscleGroupSecondary.isNotEmpty()) {
                    txtMuscleGroupSecondaryE.text = exercises[position].muscleGroupSecondary
                    txtMuscleGroupSecondaryE.visibility = View.VISIBLE
                    exerciseSeparator.visibility = View.VISIBLE
                }
            }
            txtBreaks.text = "${exercises[position].breaks}s"
            if (exercises[position].superSet != 0) {
                imgSuperset.visibility = View.VISIBLE
            } else {
                imgSuperset.visibility = View.GONE
            }
        }
    }

    override fun getItemCount(): Int {
        return exercises.size
    }

    inner class ViewHolder(var binding: ItemListExerciseBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.parentExercise.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                   parentListener.invoke(adapterPosition)
                }
            }
            binding.btnMenuExercise.setOnClickListener { v: View ->
                if (adapterPosition != RecyclerView.NO_POSITION) menuListener.invoke(adapterPosition,v) }
        }
    }
}
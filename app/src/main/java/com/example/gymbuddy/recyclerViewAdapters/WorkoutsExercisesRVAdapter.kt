package com.example.gymbuddy.recyclerViewAdapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.gymbuddy.R
import com.example.gymbuddy.databinding.ItemListExerciseSimpleBinding
import com.example.gymbuddy.helpers.Helpers.toggleMarque
import com.example.gymbuddy.model.Exercise
import java.util.*

class WorkoutsExercisesRVAdapter(private val mContext: Context) : RecyclerView.Adapter<WorkoutsExercisesRVAdapter.ViewHolder>() {
    var exercises = ArrayList<Exercise>()
    private var newSession = false
    private var exerciseNo = 0
    fun setExerciseNo(exerciseNo: Int) {
        this.exerciseNo = exerciseNo
    }

    fun setNewSession(newSession: Boolean) {
        this.newSession = newSession
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemListExerciseSimpleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.apply {
            if (newSession) {
                txtExerciseNameSimple.isSelected = true
                if (position == exerciseNo) {
                    parentExerciseSimple.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.orange_500))
                    txtExerciseNameSimple.setTextColor(ContextCompat.getColor(mContext, R.color.grey_700))
                    txtSetsNoSimple.setTextColor(ContextCompat.getColor(mContext, R.color.grey_700))
                    imgExerciseSimple.setImageResource(R.drawable.ic_hexagon_double_vertical_empty)
                    txtExerciseNameSimple.toggleMarque(true)
                }
                if (position < exerciseNo) {
                    txtExerciseNameSimple.setTextColor(ContextCompat.getColor(mContext, R.color.grey_200))
                    txtSetsNoSimple.setTextColor(ContextCompat.getColor(mContext, R.color.grey_200))
                    imgExerciseSimple.setImageResource(R.drawable.ic_hexagon_double_vertical_empty)
                }
            }
            txtExerciseNameSimple.text = exercises[position].name
            txtSetsNoSimple.text = "${exercises[position].sets} ${
                when (exercises[position].sets) {
                    3, 4 -> mContext.getString(R.string.setTxt34)
                    else -> mContext.getString(R.string.setTxt)
                }
            }"
        }
    }

    override fun getItemCount(): Int {
        return exercises.size
    }

    class ViewHolder(var binding: ItemListExerciseSimpleBinding) : RecyclerView.ViewHolder(binding.root) {
    }
}
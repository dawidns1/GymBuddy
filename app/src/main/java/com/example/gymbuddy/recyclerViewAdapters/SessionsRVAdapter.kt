package com.example.gymbuddy.recyclerViewAdapters

import android.animation.*
import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.gymbuddy.databinding.ItemListSessionBinding
import com.example.gymbuddy.helpers.Helpers.rirSuffix
import com.example.gymbuddy.helpers.Helpers.shakeVertically
import com.example.gymbuddy.model.Exercise
import com.example.gymbuddy.model.Session
import com.example.gymbuddy.helpers.Helpers.toPrettyString
import com.example.gymbuddy.helpers.Helpers.toggleVisibility
import java.util.*
import kotlin.math.roundToInt

class SessionsRVAdapter(
    private val parentListener: (Int) -> Unit,
    private val parentLongListener: (Int, View) -> Unit,
    private var mContext: Context? = null
) : RecyclerView.Adapter<SessionsRVAdapter.ViewHolder>() {
    private var sessions = ArrayList<Session>()
    private var exercise: Exercise? = null
    private var menuShown = true
    private var highlightPosition = -1
    private var lastHighlightPosition = -1
    private var params: ConstraintLayout.LayoutParams? = null

    fun setHighlightPosition(highlightPosition: Int) {
        lastHighlightPosition = this.highlightPosition
        this.highlightPosition = highlightPosition
    }

    fun setMenuShown(menuShown: Boolean) {
        this.menuShown = menuShown
    }

    fun setSessions(sessions: ArrayList<Session>) {
        this.sessions = sessions
    }

    fun setExercise(exercise: Exercise?) {
        this.exercise = exercise
//        highlightPosition = -1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemListSessionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val session = sessions[position]
        holder.binding.apply {
            txtDate.text = session.date
            if (!menuShown) {
                btnMenuSession.visibility = View.GONE
            }
            exercise?.let { exercise ->
                txtLxR4.toggleVisibility(exercise.sets >= 4)
                separator3.toggleVisibility(exercise.sets >= 4)
                txtLxR5.toggleVisibility(exercise.sets >= 5)
                txtLxR6.toggleVisibility(exercise.sets >= 6)
                separator4.toggleVisibility(exercise.sets >= 6)
                txtLxR7.toggleVisibility(exercise.sets >= 7)
                separator5.toggleVisibility(exercise.sets >= 7)
                txtLxR8.toggleVisibility(exercise.sets >= 8)
                separator6.toggleVisibility(exercise.sets >= 0)
            }

            var total = 0f
            for (i in holder.txtLxRs.indices) {
                holder.txtLxRs[i].text = "${session.load[i].toPrettyString()}x${session.reps[i]}${mContext?.let { rirSuffix(session.rir, i, it) }}"
                total += session.load[i] * session.reps[i]
            }
            txtTotal.text = total.roundToInt().toString()
            if (highlightPosition != -1 && (position == 0 || menuShown)) {
                highlightSession.alpha = 0.0f
                if (!highlightSession.isShown) highlightSession.visibility = View.VISIBLE
                params = highlightSession.layoutParams as ConstraintLayout.LayoutParams
                params?.endToEnd = holder.txtLxRs[highlightPosition].id
                params?.topToTop = holder.txtLxRs[highlightPosition].id
                highlightSession.requestLayout()
                shakeVertically(highlightSession, 500)
                highlightSession.animate().alpha(1.0f)
                    .setDuration(500)
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            super.onAnimationEnd(animation)
                        }
                    })
                if (highlightPosition > 0 && highlightPosition > lastHighlightPosition) {
                    val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1.3f)
                    val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.3f)
                    ObjectAnimator.ofPropertyValuesHolder(holder.txtLxRs[highlightPosition - 1], scaleX, scaleY).apply {
                        repeatCount = 1
                        repeatMode = ValueAnimator.REVERSE
                        duration = 250
                        start()
                    }
                }
            } else {
                highlightSession.visibility = View.GONE
            }
        }
    }

    override fun getItemCount(): Int {
        return sessions.size
    }

    inner class ViewHolder(var binding: ItemListSessionBinding) : RecyclerView.ViewHolder(binding.root) {
        val txtLxRs = ArrayList<TextView>()

        init {
            txtLxRs.apply {
                add(binding.txtLxR1)
                add(binding.txtLxR2)
                add(binding.txtLxR3)
                add(binding.txtLxR4)
                add(binding.txtLxR5)
                add(binding.txtLxR6)
                add(binding.txtLxR7)
                add(binding.txtLxR8)
            }
            binding.btnMenuSession.visibility = View.GONE
            binding.parentSession.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    parentListener.invoke(adapterPosition)
                }
            }
            if (menuShown) {
                binding.parentSession.setOnLongClickListener { v: View ->
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        parentLongListener.invoke(adapterPosition, v)
                        return@setOnLongClickListener true
                    } else {
                        return@setOnLongClickListener false
                    }
                }
            }
        }
    }
}

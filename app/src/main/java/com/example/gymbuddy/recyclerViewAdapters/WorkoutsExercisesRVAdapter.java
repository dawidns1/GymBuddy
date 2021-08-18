package com.example.gymbuddy.recyclerViewAdapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gymbuddy.R;
import com.example.gymbuddy.model.Exercise;

import java.util.ArrayList;

public class WorkoutsExercisesRVAdapter extends RecyclerView.Adapter<WorkoutsExercisesRVAdapter.ViewHolder> {

    private ArrayList<Exercise> exercises = new ArrayList<>();
    private final Context mContext;
    private boolean newSession;
    private int exerciseNo;


    public WorkoutsExercisesRVAdapter(Context mContext) {
        this.mContext = mContext;
        this.newSession = false;
        this.exerciseNo = 0;
    }

    public void setExerciseNo(int exerciseNo) {
        this.exerciseNo = exerciseNo;
    }

    public void setNewSession(boolean newSession) {
        this.newSession = newSession;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_exercise_simple, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (newSession) {
            if (position == exerciseNo) {
                holder.parentExerciseSimple.setCardBackgroundColor(ContextCompat.getColor(mContext,R.color.orange_500));
                holder.txtExerciseNameSimple.setTextColor(ContextCompat.getColor(mContext,R.color.grey_700));
                holder.txtSetsNoSimple.setTextColor(ContextCompat.getColor(mContext,R.color.grey_700));
                holder.imgExerciseSimple.setImageResource(R.drawable.ic_hexagon_double_vertical_empty);
            }
            if (position < exerciseNo) {
                holder.txtExerciseNameSimple.setTextColor(ContextCompat.getColor(mContext,R.color.grey_200));
                holder.txtSetsNoSimple.setTextColor(ContextCompat.getColor(mContext,R.color.grey_200));
                holder.imgExerciseSimple.setImageResource(R.drawable.ic_hexagon_double_vertical_empty);
            }

        }

        holder.txtExerciseNameSimple.setText(exercises.get(position).getName());
        if (exercises.get(position).getSets() == 3 || exercises.get(position).getSets() == 4) {
            holder.txtSetsNoSimple.setText(exercises.get(position).getSets() + " " + mContext.getString(R.string.setTxt34));
        } else {
            holder.txtSetsNoSimple.setText(exercises.get(position).getSets() + " " + mContext.getString(R.string.setTxt));
        }

    }

    @Override
    public int getItemCount() {
        return exercises.size();
    }

    public ArrayList<Exercise> getExercises() {
        return exercises;
    }

    public void setExercises(ArrayList<Exercise> exercises) {
        this.exercises = exercises;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final CardView parentExerciseSimple;
        private final TextView txtExerciseNameSimple;
        private final TextView txtSetsNoSimple;
        private final ImageView imgExerciseSimple;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            parentExerciseSimple = itemView.findViewById(R.id.parentExerciseSimple);
            txtExerciseNameSimple = itemView.findViewById(R.id.txtExerciseNameSimple);
            txtSetsNoSimple = itemView.findViewById(R.id.txtSetsNoSimple);
            imgExerciseSimple = itemView.findViewById(R.id.imgExerciseSimple);

        }
    }
}

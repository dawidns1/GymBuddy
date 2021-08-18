package com.example.gymbuddy.recyclerViewAdapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gymbuddy.R;
import com.example.gymbuddy.view.SessionsActivity;
import com.example.gymbuddy.helpers.Utils;
import com.example.gymbuddy.helpers.Helpers;
import com.example.gymbuddy.model.Exercise;
import com.example.gymbuddy.model.Workout;

import java.text.MessageFormat;
import java.util.ArrayList;

public class ExercisesRVAdapter extends RecyclerView.Adapter<ExercisesRVAdapter.ViewHolder> {

    private ArrayList<Exercise> exercises;
    private Context mContext;
    private boolean editsStarted = false;
    private boolean changeMade = false;
    private Workout displayedWorkout;
    private int selectedExercise;
    private String[] exercisesForSuperset;

    public ExercisesRVAdapter(Context mContext, ArrayList<Exercise> exercises) {
        this.mContext = mContext;
        this.exercises=exercises;
    }

    public void setDisplayedWorkout(Workout displayedWorkout) {
        this.displayedWorkout = displayedWorkout;
    }

    @Override
    public long getItemId(int position) {
        return exercises.get(position).getId();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_exercise, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.txtExerciseName.setText(exercises.get(position).getName());
        holder.txtIdE.setText(String.valueOf(exercises.get(position).getId()));
        if (exercises.get(position).getSets() == 3 || exercises.get(position).getSets() == 4) {

            holder.txtSetsNo.setText(MessageFormat.format("{0} {1}", exercises.get(position).getSets(), mContext.getString(R.string.setTxt34)));
        } else {
            holder.txtSetsNo.setText(MessageFormat.format("{0} {1}", exercises.get(position).getSets(), mContext.getString(R.string.setTxt)));
        }
        if (exercises.get(position).getTempo() == 9999) {
            holder.txtTempo.setText(mContext.getResources().getString(R.string.iso));
            holder.txtTempo.setVisibility(View.VISIBLE);
        } else if (exercises.get(position).getTempo() == 0) {
            holder.txtTempo.setVisibility(View.GONE);
        } else {
            holder.txtTempo.setText(String.valueOf(exercises.get(position).getTempo()));
            holder.txtTempo.setVisibility(View.VISIBLE);
        }

        holder.txtMuscleGroupSecondary.setVisibility(View.GONE);
        holder.exerciseSeparator.setVisibility(View.GONE);

        if (exercises.get(position).getMuscleGroup().isEmpty()) {
            holder.txtMuscleGroup.setVisibility(View.GONE);
            holder.muscleGroupETxt.setVisibility(View.GONE);
        } else {
            holder.txtMuscleGroup.setText(exercises.get(position).getMuscleGroup());
            holder.txtMuscleGroup.setVisibility(View.VISIBLE);
            holder.muscleGroupETxt.setVisibility(View.VISIBLE);
            if (!exercises.get(position).getMuscleGroupSecondary().isEmpty()) {
                holder.txtMuscleGroupSecondary.setText(exercises.get(position).getMuscleGroupSecondary());
                holder.txtMuscleGroupSecondary.setVisibility(View.VISIBLE);
                holder.exerciseSeparator.setVisibility(View.VISIBLE);
            }
        }

        holder.txtBreaks.setText(MessageFormat.format("{0}s", exercises.get(position).getBreaks()));

        if (exercises.get(position).getSuperSet() != 0) {
            holder.imgSuperset.setVisibility(View.VISIBLE);
        } else {
            holder.imgSuperset.setVisibility(View.GONE);
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

    public class ViewHolder extends RecyclerView.ViewHolder {

        private CardView parentExercise;
        private TextView txtExerciseName;
        private TextView txtSetsNo;
        private TextView txtMuscleGroup;
        private TextView txtMuscleGroupSecondary;
        private TextView txtBreaks;
        private TextView exerciseSeparator;
        private TextView muscleGroupETxt;
        private TextView txtTempo;
        private ImageView btnMenuExercise;
        private ImageView imgSuperset;
        private TextView txtIdE;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            parentExercise = itemView.findViewById(R.id.parentExercise);
            txtExerciseName = itemView.findViewById(R.id.txtExerciseName);
            txtSetsNo = itemView.findViewById(R.id.txtSetsNo);
            txtMuscleGroup = itemView.findViewById(R.id.txtMuscleGroupE);
            txtMuscleGroupSecondary = itemView.findViewById(R.id.txtMuscleGroupSecondaryE);
            txtBreaks = itemView.findViewById(R.id.txtBreaks);
            exerciseSeparator = itemView.findViewById(R.id.exerciseSeparator);
            muscleGroupETxt = itemView.findViewById(R.id.muscleGroupETxt);
            btnMenuExercise = itemView.findViewById(R.id.btnMenuExercise);
            txtIdE = itemView.findViewById(R.id.txtIdE);
            txtTempo = itemView.findViewById(R.id.txtTempo);
            imgSuperset = itemView.findViewById(R.id.imgSuperset);

            parentExercise.setOnClickListener(v -> {
                if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                    Intent intent = new Intent(mContext, SessionsActivity.class);
                    intent.putExtra(Helpers.EXERCISES_KEY, exercises);
                    intent.putExtra(Helpers.POSITION_KEY, getAdapterPosition());
                    intent.putExtra(Helpers.WORKOUT_KEY, displayedWorkout);
                    mContext.startActivity(intent);
                }
            });

            btnMenuExercise.setOnClickListener(v -> {
                if(getAdapterPosition()!=RecyclerView.NO_POSITION) onItemClickListener.onItemClick(getAdapterPosition(), v);
            });

        }

        private void handleDisableSuperset() {
            switch (exercises.get(getAdapterPosition()).getSuperSet()) {
                case 1:
                    exercises.get(getAdapterPosition()).setSuperSet(0);
                    notifyItemChanged(getAdapterPosition());
                    exercises.get(getAdapterPosition() + 1).setSuperSet(0);
                    notifyItemChanged(getAdapterPosition() + 1);
                    break;
                case 2:
                    exercises.get(getAdapterPosition()).setSuperSet(0);
                    notifyItemChanged(getAdapterPosition());
                    exercises.get(getAdapterPosition() - 1).setSuperSet(0);
                    notifyItemChanged(getAdapterPosition() - 1);
                    break;
//                case 2:
//                    exercises.get(getAdapterPosition()).setSuperSet(0);
//                    notifyItemChanged(getAdapterPosition());
//                    if (getAdapterPosition() == 0) {
//                        exercises.get(getAdapterPosition() + 1).setSuperSet(0);
//                        notifyItemChanged(getAdapterPosition() + 1);
//                    } else if (getAdapterPosition() == exercises.size() - 1) {
//                        exercises.get(getAdapterPosition() - 1).setSuperSet(0);
//                        notifyItemChanged(getAdapterPosition() - 1);
//                    } else if (exercises.get(getAdapterPosition() - 1).getSuperSet() == 1) {
//                        exercises.get(getAdapterPosition() - 1).setSuperSet(0);
//                        notifyItemChanged(getAdapterPosition() - 1);
//                    } else {
//                        exercises.get(getAdapterPosition() + 1).setSuperSet(0);
//                        notifyItemChanged(getAdapterPosition() + 1);
//                    }
//                    break;
                default:
                    break;
            }
            Utils.getInstance(mContext).updateWorkoutsExercises(displayedWorkout, exercises);
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(int positionRV, View v);
    }

    OnItemClickListener onItemClickListener;
}

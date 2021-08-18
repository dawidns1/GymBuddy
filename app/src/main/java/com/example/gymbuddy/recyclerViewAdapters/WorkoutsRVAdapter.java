package com.example.gymbuddy.recyclerViewAdapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gymbuddy.view.ExercisesActivity;
import com.example.gymbuddy.R;
import com.example.gymbuddy.helpers.Utils;
import com.example.gymbuddy.helpers.Helpers;
import com.example.gymbuddy.model.Workout;
import com.example.gymbuddy.view.AddWorkoutActivity;

import java.text.MessageFormat;
import java.util.ArrayList;

public class WorkoutsRVAdapter extends RecyclerView.Adapter<WorkoutsRVAdapter.ViewHolder> {

    private ArrayList<Workout> workouts = new ArrayList<>();
    private final Context mContext;
    public WorkoutsExercisesRVAdapter adapterInner;
    private boolean showHeader=false;
    private boolean groupingEnabled=false;

    public WorkoutsRVAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public void setGroupingEnabled(boolean groupingEnabled) {
        this.groupingEnabled = groupingEnabled;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_workout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        adapterInner = new WorkoutsExercisesRVAdapter(mContext);
        adapterInner.setExercises(workouts.get(position).getExercises());
        holder.workoutsExercisesRV.setAdapter(adapterInner);
        holder.workoutsExercisesRV.setLayoutManager(new GridLayoutManager(mContext, 2));
        holder.workoutsExercisesRV.suppressLayout(true);
        if (Utils.getInstance(mContext).isAreExercisesShown()) {
            holder.workoutsExercisesRV.setVisibility(View.VISIBLE);
        } else {
            holder.workoutsExercisesRV.setVisibility(View.GONE);
        }
        holder.txtWorkoutName.setText(workouts.get(position).getName());
        if (workouts.get(position).getExercises().size() == 1) {
            holder.txtExercisesNo.setText(MessageFormat.format("{0} {1}", workouts.get(position).getExercises().size(), mContext.getString(R.string.exercises1)));
        } else if (workouts.get(position).getExercises().size() == 2 || workouts.get(position).getExercises().size() == 3 ||
                workouts.get(position).getExercises().size() == 4) {
            holder.txtExercisesNo.setText(MessageFormat.format("{0} {1}", workouts.get(position).getExercises().size(), mContext.getString(R.string.exercises234)));
        } else {
            holder.txtExercisesNo.setText(MessageFormat.format("{0} {1}", workouts.get(position).getExercises().size(), mContext.getString(R.string.exercises)));
        }

        holder.txtType.setText(workouts.get(position).getType());
        if (holder.txtType.getText().toString().equals("---") || groupingEnabled) {
            holder.txtType.setVisibility(View.GONE);
            holder.typeTxt.setVisibility(View.GONE);
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) holder.muscleGroupTxt.getLayoutParams();
            params.leftMargin = 0;
            holder.muscleGroupTxt.setLayoutParams(params);
        } else {
            holder.txtType.setVisibility(View.VISIBLE);
            holder.typeTxt.setVisibility(View.VISIBLE);
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) holder.muscleGroupTxt.getLayoutParams();
            params.leftMargin = 20;
            holder.muscleGroupTxt.setLayoutParams(params);
        }

        holder.txtMuscleGroupSecondary.setVisibility(View.GONE);
        holder.workoutSeparator.setVisibility(View.GONE);

        if (workouts.get(position).getMuscleGroup().isEmpty()) {
            holder.txtMuscleGroup.setVisibility(View.GONE);
            holder.muscleGroupTxt.setVisibility(View.GONE);
        } else {
            holder.txtMuscleGroup.setText(workouts.get(position).getMuscleGroup());
            holder.txtMuscleGroup.setVisibility(View.VISIBLE);
            holder.muscleGroupTxt.setVisibility(View.VISIBLE);
            if (!workouts.get(position).getMuscleGroupSecondary().isEmpty()) {
                holder.txtMuscleGroupSecondary.setText(workouts.get(position).getMuscleGroupSecondary());
                holder.txtMuscleGroupSecondary.setVisibility(View.VISIBLE);
                holder.workoutSeparator.setVisibility(View.VISIBLE);
            }
        }
        holder.txtId.setText(String.valueOf(workouts.get(position).getId()));

        holder.txtTypeHeader.setText(Helpers.workoutTypeHeaderGenerator(workouts.get(position).getType(),mContext));

        if(showHeader && groupingEnabled){
            if(position==0)holder.txtTypeHeader.setVisibility(View.VISIBLE);
            else if(!Helpers.workoutTypeComparator(workouts.get(position-1).getType(),workouts.get(position).getType()))holder.txtTypeHeader.setVisibility(View.VISIBLE);
            else holder.txtTypeHeader.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return workouts.size();
    }

    public void setWorkouts(ArrayList<Workout> workouts) {
        this.workouts = workouts;
        notifyDataSetChanged();
        if(workouts.size()>1){
            for(int i=1;i<workouts.size();i++){
                if(!workouts.get(0).getType().equals(workouts.get(i).getType())){
                    showHeader=true;
                    break;
                }
            }

        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView txtWorkoutName;
        private final TextView txtExercisesNo;
        private final TextView txtType;
        private final TextView txtMuscleGroup;
        private final TextView txtMuscleGroupSecondary;
        private final TextView txtId;
        private final TextView workoutSeparator;
        private final TextView muscleGroupTxt;
        private final TextView typeTxt;
        private final TextView txtTypeHeader;
        private final RecyclerView workoutsExercisesRV;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            CardView parent = itemView.findViewById(R.id.parent);
            txtWorkoutName = itemView.findViewById(R.id.txtWorkoutName);
            txtExercisesNo = itemView.findViewById(R.id.txtExercisesNo);
            txtType = itemView.findViewById(R.id.txtType);
            txtMuscleGroup = itemView.findViewById(R.id.txtMuscleGroup);
            txtMuscleGroupSecondary = itemView.findViewById(R.id.txtMuscleGroupSecondary);
            txtId = itemView.findViewById(R.id.txtId);
            workoutSeparator = itemView.findViewById(R.id.workoutSeparator);
            muscleGroupTxt = itemView.findViewById(R.id.muscleGroupTxt);
            ImageView btnMenuWorkout = itemView.findViewById(R.id.btnMenuWorkout);
            txtTypeHeader=itemView.findViewById(R.id.txtTypeHeader);
            workoutsExercisesRV = itemView.findViewById(R.id.workoutsExercisesRV);
            typeTxt = itemView.findViewById(R.id.typeTxt);

            txtTypeHeader.setVisibility(View.GONE);

            parent.setOnClickListener(v -> {
                if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                    Intent intent = new Intent(mContext, ExercisesActivity.class);
                    intent.putExtra(Helpers.EXERCISES_KEY, workouts.get(getAdapterPosition()));
                    mContext.startActivity(intent);
                }
            });

            btnMenuWorkout.setOnClickListener(v -> {
                if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                    PopupMenu popupMenu = new PopupMenu(mContext, v);
                    popupMenu.inflate(R.menu.popup_menu);
                    popupMenu.setOnMenuItemClickListener(item -> {
                        switch (item.getItemId()) {
                            case R.id.menuEdit:
                                Intent intent = new Intent(mContext, AddWorkoutActivity.class);
                                intent.putExtra(Helpers.WORKOUTS_KEY, workouts);
                                intent.putExtra(Helpers.POSITION_KEY, getAdapterPosition());
                                mContext.startActivity(intent);
                                return true;
                            case R.id.menuDelete:
                                new AlertDialog.Builder(mContext, R.style.DefaultAlertDialogTheme)
                                        .setMessage(R.string.sureDeleteThisWorkout)
                                        .setIcon(R.drawable.ic_delete)
                                        .setPositiveButton(R.string.yes, (dialog, which) -> {
                                            Utils.getInstance(mContext).deleteWorkout(workouts.get(getAdapterPosition()));
                                            workouts.remove(workouts.get(getAdapterPosition()));
                                            notifyItemRemoved(getAdapterPosition());
                                            onItemClickListener.onItemClick(getAdapterPosition());
                                        })
                                        .setNegativeButton(R.string.no, null)
                                        .show();
                                return true;
                            default:
                                return false;
                        }
                    });
                    popupMenu.show();
                }
            });
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(int positionRV);
    }

    OnItemClickListener onItemClickListener;

}

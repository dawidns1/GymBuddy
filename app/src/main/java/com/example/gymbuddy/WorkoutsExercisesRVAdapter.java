package com.example.gymbuddy;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ColorStateListInflaterCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;

public class WorkoutsExercisesRVAdapter extends RecyclerView.Adapter<WorkoutsExercisesRVAdapter.ViewHolder> {

    private ArrayList<Exercise> exercises = new ArrayList<>();
    private Context mContext;
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
                holder.parentExerciseSimple.setCardBackgroundColor(mContext.getResources().getColor(R.color.orange_500));
                holder.txtExerciseNameSimple.setTextColor(mContext.getResources().getColor(R.color.grey_700));
                holder.txtSetsNoSimple.setTextColor(mContext.getResources().getColor(R.color.grey_700));
                holder.imgExerciseSimple.setImageResource(R.drawable.ic_hexagon_double_vertical_empty);
            }
            if (position < exerciseNo) {
                holder.txtExerciseNameSimple.setTextColor(mContext.getResources().getColor(R.color.grey_200));
                holder.txtSetsNoSimple.setTextColor(mContext.getResources().getColor(R.color.grey_200));
                holder.imgExerciseSimple.setImageResource(R.drawable.ic_hexagon_double_vertical_empty);
            }

        }

        holder.txtExerciseNameSimple.setText(exercises.get(position).getName());
        if (exercises.get(position).getSets() == 3 || exercises.get(position).getSets() == 4) {
            //TODO sprawdzic co się wysrało
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

    public class ViewHolder extends RecyclerView.ViewHolder {

        private CardView parentExerciseSimple;
        private TextView txtExerciseNameSimple;
        private TextView txtSetsNoSimple;
        private ImageView imgExerciseSimple;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            parentExerciseSimple = itemView.findViewById(R.id.parentExerciseSimple);
            txtExerciseNameSimple = itemView.findViewById(R.id.txtExerciseNameSimple);
            txtSetsNoSimple = itemView.findViewById(R.id.txtSetsNoSimple);
            imgExerciseSimple = itemView.findViewById(R.id.imgExerciseSimple);

        }
    }
}

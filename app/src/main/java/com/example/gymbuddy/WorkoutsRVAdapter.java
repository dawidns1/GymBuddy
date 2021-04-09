package com.example.gymbuddy;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Handler;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class WorkoutsRVAdapter extends RecyclerView.Adapter<WorkoutsRVAdapter.ViewHolder> {

    private ArrayList<Workout> workouts = new ArrayList<>();
    private Context mContext;
    private boolean editsStarted = false, changeMade = false, wasVisible = false;
    public WorkoutsExercisesRVAdapter adapterInner;

    public WorkoutsRVAdapter(Context mContext) {
        this.mContext = mContext;
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
            holder.txtExercisesNo.setText(workouts.get(position).getExercises().size() + " " + mContext.getString(R.string.exercises1));
        } else if (workouts.get(position).getExercises().size() == 2 || workouts.get(position).getExercises().size() == 3 ||
                workouts.get(position).getExercises().size() == 4) {
            holder.txtExercisesNo.setText(workouts.get(position).getExercises().size() + " " + mContext.getString(R.string.exercises234));
        } else {
            holder.txtExercisesNo.setText(workouts.get(position).getExercises().size() + " " + mContext.getString(R.string.exercises));
        }

        holder.txtType.setText(workouts.get(position).getType());
        if (holder.txtType.getText().toString().equals("---")) {
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

        int state[] = workouts.get(position).getState();

        holder.txtId.setText(String.valueOf(workouts.get(position).getId()));

//        holder.parent.setOnClickListener(new View.OnClickListener()
//
//    {
//        @Override
//        public void onClick (View v){
//        Intent intent = new Intent(mContext, ExercisesActivity.class);
//        intent.putExtra(EXERCISES_KEY, workouts.get(position));
//        mContext.startActivity(intent);
//    }
//    });

//        holder.edtNewMuscleGroup.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                if (s.length() != 0) {
//                    holder.edtNewMuscleGroupSecondary.setEnabled(true);
//                    holder.edtNewMuscleGroupSecondary.getBackground().setColorFilter(Color.parseColor("#FF5722"), PorterDuff.Mode.SRC_IN);
//                } else {
//                    holder.edtNewMuscleGroupSecondary.setEnabled(false);
//                    holder.edtNewMuscleGroupSecondary.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
//                }
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//
//            }
//        });

//        holder.btnMenuWorkout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                PopupMenu popupMenu = new PopupMenu(mContext, v);
//                popupMenu.inflate(R.menu.popup_menu);
//                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//                    @Override
//                    public boolean onMenuItemClick(MenuItem item) {
//                        switch (item.getItemId()) {
//                            case R.id.menuEdit:
//                                if (editsStarted) {
////                                    Toast.makeText(mContext, "Finish previous editing first", Toast.LENGTH_SHORT).show();
////                                } else {
////                                    switch (workouts.get(position).getType()) {
////                                        case "---":
////                                            holder.edtNewWorkoutType.setSelection(0);
////                                            break;
////                                        case "FBW":
////                                            holder.edtNewWorkoutType.setSelection(1);
////                                            break;
////                                        case "Push":
////                                            holder.edtNewWorkoutType.setSelection(2);
////                                            break;
////                                        case "Pull":
////                                            holder.edtNewWorkoutType.setSelection(3);
////                                            break;
////                                        case "Legs":
////                                            holder.edtNewWorkoutType.setSelection(4);
////                                            break;
////                                        case "Upper":
////                                            holder.edtNewWorkoutType.setSelection(5);
////                                            break;
////                                        case "Lower":
////                                            holder.edtNewWorkoutType.setSelection(6);
////                                            break;
////                                        case "Split":
////                                            holder.edtNewWorkoutType.setSelection(7);
////                                            break;
////                                        default:
////                                            holder.edtNewWorkoutType.setSelection(0);
////                                            break;
////                                    }
////                                    editsStarted = true;
//////                                    if (holder.workoutsExercisesRV.getVisibility() == View.VISIBLE) {
//////                                        wasVisible = true;
//////                                        holder.workoutsExercisesRV.setVisibility(View.GONE);
//////                                    }
////                                    holder.expandedWorkoutView.setVisibility(View.VISIBLE);
////                                    holder.edtNewWorkoutName.setText(workouts.get(position).getName());
////                                    holder.edtNewMuscleGroup.setText(workouts.get(position).getMuscleGroup());
////                                    holder.edtNewMuscleGroupSecondary.setText(workouts.get(position).getMuscleGroupSecondary());
////                                    if (position == 0) {
////                                        holder.btnMoveUpW.setEnabled(false);
////                                        holder.btnMoveUpW.getBackground().setColorFilter(ContextCompat.getColor(mContext, R.color.grey_500), PorterDuff.Mode.MULTIPLY);
////                                    } else {
////                                        holder.btnMoveUpW.setEnabled(true);
////                                        holder.btnMoveUpW.getBackground().setColorFilter(ContextCompat.getColor(mContext, R.color.orange_500), PorterDuff.Mode.MULTIPLY);
////                                    }
////                                    if (position + 1 == workouts.size()) {
////                                        holder.btnMoveDownW.setEnabled(false);
////                                        holder.btnMoveDownW.getBackground().setColorFilter(ContextCompat.getColor(mContext, R.color.grey_500), PorterDuff.Mode.MULTIPLY);
////                                    } else {
////                                        holder.btnMoveDownW.setEnabled(true);
////                                        holder.btnMoveDownW.getBackground().setColorFilter(ContextCompat.getColor(mContext, R.color.orange_500), PorterDuff.Mode.MULTIPLY);
////                                    }
////                                    holder.btnMenuWorkout.setVisibility(View.GONE);
////                                }
////                                return true;
////                            case R.id.menuDelete:
////                                new AlertDialog.Builder(mContext)
////                                        .setMessage("Are you sure you want to delete this workout?")
////                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
////                                            @Override
////                                            public void onClick(DialogInterface dialog, int which) {
////                                                Utils.getInstance(mContext).deleteWorkout(workouts.get(position));
////                                                workouts.remove(workouts.get(position));
////                                                notifyItemRemoved(position);
////                                                notifyItemRangeChanged(position, workouts.size());
////                                            }
////                                        })
////                                        .setNegativeButton("No", null)
////                                        .show();
////                                return true;
////                            default:
////                                return false;
////                        }
////                    }
////                });
////                popupMenu.show();
////            }
////        });
//
////        holder.btnDoneEditingW.setOnClickListener(new View.OnClickListener() {
////            @Override
////            public void onClick(View v) {
////                if (!holder.edtNewWorkoutName.getText().toString().isEmpty() &&
////                        !holder.edtNewWorkoutName.getText().toString().equals(holder.txtWorkoutName.getText().toString())) {
////                    changeMade = true;
////                    workouts.get(position).setName(holder.edtNewWorkoutName.getText().toString());
////                }
////                if (!holder.edtNewWorkoutType.getSelectedItem().toString().equals(holder.txtType.getText().toString())) {
////                    changeMade = true;
////                    workouts.get(position).setType(holder.edtNewWorkoutType.getSelectedItem().toString());
////                }
////
////                if (holder.edtNewMuscleGroup.getText().toString().isEmpty()) {
////                    workouts.get(position).setMuscleGroup("");
////                    workouts.get(position).setMuscleGroupSecondary("");
////                    changeMade = true;
////                } else {
////                    if (!holder.edtNewMuscleGroup.getText().toString().equals(holder.txtMuscleGroup.getText().toString())) {
////                        changeMade = true;
////                        workouts.get(position).setMuscleGroup(holder.edtNewMuscleGroup.getText().toString());
////                    }
////                    if (!holder.edtNewMuscleGroupSecondary.getText().toString().equals(holder.txtMuscleGroupSecondary.getText().toString())) {
////                        changeMade = true;
////                        workouts.get(position).setMuscleGroupSecondary(holder.edtNewMuscleGroupSecondary.getText().toString());
////                    }
////                }
////                if (changeMade) {
////                    notifyItemChanged(position);
////                    changeMade = false;
////                }
////                holder.expandedWorkoutView.setVisibility(View.GONE);
//////                if (wasVisible) {
//////                    wasVisible = false;
//////                    holder.workoutsExercisesRV.setVisibility(View.VISIBLE);
//////                }
////                holder.btnMenuWorkout.setVisibility(View.VISIBLE);
////                editsStarted = false;
////                Utils.getInstance(mContext).updateWorkouts(workouts);
////            }
////        });

//        holder.btnCancelEditingW.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                holder.expandedWorkoutView.setVisibility(View.GONE);
//                holder.btnMenuWorkout.setVisibility(View.VISIBLE);
//                if (wasVisible) {
//                    wasVisible = false;
//                    holder.workoutsExercisesRV.setVisibility(View.VISIBLE);
//                }
//                editsStarted = false;
//            }
//        });
//
//        holder.btnMoveUpW.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                holder.expandedWorkoutView.setVisibility(View.GONE);
//                holder.btnMenuWorkout.setVisibility(View.VISIBLE);
//                editsStarted = false;
//                Workout toMove = workouts.get(position);
//                workouts.remove(position);
//                notifyItemRemoved(position);
//                workouts.add(position - 1, toMove);
//                notifyItemInserted(position - 1);
//                Utils.getInstance(mContext).updateWorkouts(workouts);
//            }
//        });
//
//        holder.btnMoveUpW.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                holder.expandedWorkoutView.setVisibility(View.GONE);
//                holder.btnMenuWorkout.setVisibility(View.VISIBLE);
//                editsStarted = false;
//                Workout toMove = workouts.get(position);
//                workouts.remove(position);
//                notifyItemRemoved(position);
//                workouts.add(0, toMove);
//                notifyItemInserted(0);
//                Utils.getInstance(mContext).updateWorkouts(workouts);
//                return false;
//            }
//        });
//
//        holder.btnMoveDownW.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                holder.expandedWorkoutView.setVisibility(View.GONE);
//                holder.btnMenuWorkout.setVisibility(View.VISIBLE);
//                editsStarted = false;
//                Workout toMove = workouts.get(position);
//                workouts.remove(position);
//                notifyItemRemoved(position);
//                workouts.add(position + 1, toMove);
//                notifyItemInserted(position + 1);
//                Utils.getInstance(mContext).updateWorkouts(workouts);
//            }
//        });
//
//        holder.btnMoveDownW.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                holder.expandedWorkoutView.setVisibility(View.GONE);
//                holder.btnMenuWorkout.setVisibility(View.VISIBLE);
//                editsStarted = false;
//                Workout toMove = workouts.get(position);
//                workouts.remove(position);
//                notifyItemRemoved(position);
//                workouts.add(toMove);
//                notifyItemInserted(workouts.size() - 1);
//                Utils.getInstance(mContext).updateWorkouts(workouts);
//                return false;
//            }
//        });
    }

    @Override
    public int getItemCount() {
        return workouts.size();
    }

    public void setWorkouts(ArrayList<Workout> workouts) {
        this.workouts = workouts;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private CardView parent;
        private TextView txtWorkoutName;
        private TextView txtExercisesNo;
        private TextView txtType;
        private TextView txtMuscleGroup;
        private TextView txtMuscleGroupSecondary;
        private TextView txtId;
        private TextView workoutSeparator;
        private TextView muscleGroupTxt;
        private TextView typeTxt;
        private ImageView btnMenuWorkout;
        //        private EditText edtNewWorkoutName, edtNewMuscleGroup, edtNewMuscleGroupSecondary;
//        private Spinner edtNewWorkoutType;
//        private ConstraintLayout expandedWorkoutView;
//        private ImageButton btnMoveUpW, btnMoveDownW;
//        private Button btnDoneEditingW, btnCancelEditingW;
        private RecyclerView workoutsExercisesRV;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            parent = itemView.findViewById(R.id.parent);
            txtWorkoutName = itemView.findViewById(R.id.txtWorkoutName);
            txtExercisesNo = itemView.findViewById(R.id.txtExercisesNo);
            txtType = itemView.findViewById(R.id.txtType);
            txtMuscleGroup = itemView.findViewById(R.id.txtMuscleGroup);
            txtMuscleGroupSecondary = itemView.findViewById(R.id.txtMuscleGroupSecondary);
            txtId = itemView.findViewById(R.id.txtId);
            workoutSeparator = itemView.findViewById(R.id.workoutSeparator);
            muscleGroupTxt = itemView.findViewById(R.id.muscleGroupTxt);
            btnMenuWorkout = itemView.findViewById(R.id.btnMenuWorkout);
//            edtNewWorkoutName = itemView.findViewById(R.id.edtNewWorkoutName);
//            edtNewWorkoutType = itemView.findViewById(R.id.edtNewWorkoutType);
//            edtNewMuscleGroup = itemView.findViewById(R.id.edtNewMuscleGroup);
//            edtNewMuscleGroupSecondary = itemView.findViewById(R.id.edtNewMuscleGroupSecondary);
//            expandedWorkoutView = itemView.findViewById(R.id.expandedWorkoutView);
//            btnMoveUpW = itemView.findViewById(R.id.btnMoveUpW);
//            btnMoveDownW = itemView.findViewById(R.id.btnMoveDownW);
//            btnDoneEditingW = itemView.findViewById(R.id.btnDoneEditingW);
//            btnCancelEditingW = itemView.findViewById(R.id.btnCancelEditingW);
            workoutsExercisesRV = itemView.findViewById(R.id.workoutsExercisesRV);
            typeTxt = itemView.findViewById(R.id.typeTxt);

            parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, ExercisesActivity.class);
                    intent.putExtra(Helpers.EXERCISES_KEY, workouts.get(getAdapterPosition()));
                    mContext.startActivity(intent);
                }
            });

            btnMenuWorkout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu popupMenu = new PopupMenu(mContext, v);
                    popupMenu.inflate(R.menu.popup_menu);
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.menuEdit:
                                    Intent intent = new Intent(mContext, AddWorkoutActivity.class);
                                    intent.putExtra(Helpers.WORKOUTS_KEY, workouts);
                                    intent.putExtra(Helpers.POSITION_KEY, getAdapterPosition());
                                    mContext.startActivity(intent);
                                    return true;
                                case R.id.menuDelete:
                                    new AlertDialog.Builder(mContext,R.style.DefaultAlertDialogTheme)
                                            .setMessage(R.string.sureDeleteThisWorkout)
                                            .setIcon(R.drawable.ic_delete)
                                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    Utils.getInstance(mContext).deleteWorkout(workouts.get(getAdapterPosition()));
                                                    workouts.remove(workouts.get(getAdapterPosition()));
                                                    notifyItemRemoved(getAdapterPosition());
                                                }
                                            })
                                            .setNegativeButton(R.string.no, null)
                                            .show();
                                    return true;
                                default:
                                    return false;
                            }
                        }
                    });
                    popupMenu.show();
                }
            });
        }
    }
}

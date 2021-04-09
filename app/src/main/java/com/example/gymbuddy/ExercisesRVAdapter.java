package com.example.gymbuddy;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
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
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;

public class ExercisesRVAdapter extends RecyclerView.Adapter<ExercisesRVAdapter.ViewHolder> {

    private ArrayList<Exercise> exercises = new ArrayList<>();
    private Context mContext;
    private boolean editsStarted = false;
    private boolean changeMade = false;
    private Workout displayedWorkout;
    private int selectedExercise;
    private String[] exercisesForSuperset;

    public ExercisesRVAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public void setDisplayedWorkout(Workout displayedWorkout) {
        this.displayedWorkout = displayedWorkout;
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

            holder.txtSetsNo.setText(exercises.get(position).getSets() + " " + mContext.getString(R.string.setTxt34));
        } else {
            holder.txtSetsNo.setText(exercises.get(position).getSets() + " " + mContext.getString(R.string.setTxt));
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

        holder.txtBreaks.setText(exercises.get(position).getBreaks() + "s");

        if (exercises.get(position).getSuperSet() != 0) {
            holder.imgSuperset.setVisibility(View.VISIBLE);
        } else {
            holder.imgSuperset.setVisibility(View.GONE);
        }

//        holder.parentExercise.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(mContext,SessionsActivity.class);
//                intent.putExtra(EXERCISES_KEY,exercises);
//                intent.putExtra(POSITION_KEY,position);
//                intent.putExtra(WORKOUT_KEY,displayedWorkout);
//                mContext.startActivity(intent);
//            }
//        });

//        holder.edtNewMuscleGroupE.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                if (s.length() != 0) {
//                    holder.edtNewMuscleGroupSecondaryE.setEnabled(true);
//                    holder.edtNewMuscleGroupSecondaryE.getBackground().setColorFilter(Color.parseColor("#FF5722"), PorterDuff.Mode.SRC_IN);
//                } else {
//                    holder.edtNewMuscleGroupSecondaryE.setEnabled(false);
//                    holder.edtNewMuscleGroupSecondaryE.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
//                }
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//
//            }
//        });

//        holder.btnMenuExercise.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                PopupMenu popupMenu=new PopupMenu(mContext,v);
//                popupMenu.inflate(R.menu.popup_menu);
//                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//                    @Override
//                    public boolean onMenuItemClick(MenuItem item) {
//                        notifyDataSetChanged();
//                        switch (item.getItemId()){
//                            case R.id.menuEdit:
//                                if(editsStarted){
//                                    Toast.makeText(mContext, "Finish previous editing first", Toast.LENGTH_SHORT).show();
//                                }
//                                else{
//                                    switch (exercises.get(holder.getAdapterPosition()).getSets()){
//                                        case 3:
//                                            holder.edtNewSets.setSelection(0);
//                                            break;
//                                        case 4:
//                                            holder.edtNewSets.setSelection(1);
//                                            break;
//                                        case 5:
//                                            holder.edtNewSets.setSelection(2);
//                                            break;
//                                        case 6:
//                                            holder.edtNewSets.setSelection(3);
//                                            break;
//                                        case 7:
//                                            holder.edtNewSets.setSelection(4);
//                                            break;
//                                        case 8:
//                                            holder.edtNewSets.setSelection(5);
//                                            break;
//                                        default:
//                                            holder.edtNewSets.setSelection(0);
//                                            break;
//                                    }
//                                    editsStarted=true;
//                                    holder.expandedExerciseView.setVisibility(View.VISIBLE);
//                                    holder.edtNewExerciseName.setText(exercises.get(holder.getAdapterPosition()).getName());
//                                    holder.edtNewMuscleGroupE.setText(exercises.get(holder.getAdapterPosition()).getMuscleGroup());
//                                    holder.edtNewMuscleGroupSecondaryE.setText(exercises.get(holder.getAdapterPosition()).getMuscleGroupSecondary());
//                                    holder.edtNewBreakLength.setText(String.valueOf(exercises.get(holder.getAdapterPosition()).getBreaks()));
//                                    if(exercises.get(position).getTempo()!=0){
//                                        holder.edtNewTempo.setText(String.valueOf(exercises.get(holder.getAdapterPosition()).getTempo()));
//                                    }
////                                    if(position==0){
////                                        holder.btnMoveUp.setEnabled(false);
////                                        holder.btnMoveUp.getBackground().setColorFilter(ContextCompat.getColor(mContext, R.color.grey_500), PorterDuff.Mode.MULTIPLY);
////                                    } else {
////                                        holder.btnMoveUp.setEnabled(true);
////                                        holder.btnMoveUp.getBackground().setColorFilter(ContextCompat.getColor(mContext, R.color.orange_500), PorterDuff.Mode.MULTIPLY);
////                                    }
////                                    if(position+1==exercises.size()){
////                                        holder.btnMoveDown.setEnabled(false);
////                                        holder.btnMoveDown.getBackground().setColorFilter(ContextCompat.getColor(mContext, R.color.grey_500), PorterDuff.Mode.MULTIPLY);
////                                    } else {
////                                        holder.btnMoveDown.setEnabled(true);
////                                        holder.btnMoveDown.getBackground().setColorFilter(ContextCompat.getColor(mContext, R.color.orange_500), PorterDuff.Mode.MULTIPLY);
////                                    }
//                                    holder.btnMenuExercise.setVisibility(View.GONE);
//                                }
//                                return true;
//                            case R.id.menuDelete:
//                                new AlertDialog.Builder(mContext)
//                                        .setMessage("Are you sure you want to delete this exercise?")
//                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//                                            @Override
//                                            public void onClick(DialogInterface dialog, int which) {
//                                                Utils.getInstance(mContext).deleteExerciseFromWorkout(exercises.get(position));
//                                                exercises.remove(exercises.get(position));
//                                                notifyItemRemoved(position);
//                                                notifyItemRangeChanged(position, exercises.size());
//                                            }
//                                        })
//                                        .setNegativeButton("No",null)
//                                        .show();
//                                return true;
//                            default:
//                                return false;
//                        }
//                    }
//                });
//                popupMenu.show();
//            }
//        });

//        holder.btnDoneEditing.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (!holder.edtNewExerciseName.getText().toString().isEmpty() &&
//                        !holder.edtNewExerciseName.getText().toString().equals(holder.txtExerciseName.getText().toString())) {
//                    changeMade = true;
//                    exercises.get(position).setName(holder.edtNewExerciseName.getText().toString());
//                }
//                if (!holder.edtNewBreakLength.getText().toString().isEmpty() && Integer.parseInt(holder.edtNewBreakLength.getText().toString()) != 0 &&
//                        !holder.edtNewBreakLength.getText().toString().equals(holder.txtBreaks.getText().toString())) {
//                    changeMade = true;
//                    exercises.get(position).setBreaks(Integer.parseInt(holder.edtNewBreakLength.getText().toString()));
//                }
//                if (!holder.edtNewTempo.getText().toString().isEmpty() && holder.edtNewTempo.getText().toString().length() == 4 &&
//                        !holder.edtNewTempo.getText().toString().equals(holder.txtTempo.getText().toString())) {
//                    changeMade = true;
//                    exercises.get(position).setTempo(Integer.parseInt(holder.edtNewTempo.getText().toString()));
//                }
//                if (holder.edtNewMuscleGroupE.getText().toString().isEmpty()) {
//                    exercises.get(position).setMuscleGroup("");
//                    exercises.get(position).setMuscleGroupSecondary("");
//                    changeMade = true;
//                } else {
//                    if (!holder.edtNewMuscleGroupE.getText().toString().equals(holder.txtMuscleGroup.getText().toString())) {
//                        changeMade = true;
//                        exercises.get(position).setMuscleGroup(holder.edtNewMuscleGroupE.getText().toString());
//                    }
//                    if (!holder.edtNewMuscleGroupSecondaryE.getText().toString().equals(holder.txtMuscleGroupSecondary.getText().toString())) {
//                        changeMade = true;
//                        exercises.get(position).setMuscleGroupSecondary(holder.edtNewMuscleGroupSecondaryE.getText().toString());
//                    }
//                }
//                if (!holder.edtNewSets.getSelectedItem().toString().equals(holder.txtSetsNo.getText().toString())) {
//                    changeMade = true;
//                    exercises.get(position).setSets(Integer.parseInt(holder.edtNewSets.getSelectedItem().toString()));
//                }
//                if (changeMade) {
//                    notifyItemChanged(position);
//                    changeMade = false;
//                }
//                holder.expandedExerciseView.setVisibility(View.GONE);
//                holder.btnMenuExercise.setVisibility(View.VISIBLE);
//                editsStarted = false;
//                Utils.getInstance(mContext).updateWorkoutsExercises(displayedWorkout, exercises);
//            }
//        });
//
//        holder.btnCancelEditing.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                holder.expandedExerciseView.setVisibility(View.GONE);
//                holder.btnMenuExercise.setVisibility(View.VISIBLE);
//                editsStarted = false;
//            }
//        });

//        holder.btnMoveUp.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                holder.expandedExerciseView.setVisibility(View.GONE);
//                holder.btnMenuExercise.setVisibility(View.VISIBLE);
//                editsStarted=false;
//                Exercise toMove=exercises.get(position);
//                exercises.remove(position);
//                notifyItemRemoved(position);
//                exercises.add(position-1,toMove);
//                notifyItemInserted(position-1);
//                Utils.getInstance(mContext).updateWorkoutsExercises(displayedWorkout,exercises);
//            }
//        });
//
//        holder.btnMoveUp.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                holder.expandedExerciseView.setVisibility(View.GONE);
//                holder.btnMenuExercise.setVisibility(View.VISIBLE);
//                editsStarted=false;
//                Exercise toMove=exercises.get(position);
//                exercises.remove(position);
//                notifyItemRemoved(position);
//                exercises.add(0,toMove);
//                notifyItemInserted(0);
//                Utils.getInstance(mContext).updateWorkoutsExercises(displayedWorkout,exercises);
//                return false;
//            }
//        });
//
//        holder.btnMoveDown.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                holder.expandedExerciseView.setVisibility(View.GONE);
//                holder.btnMenuExercise.setVisibility(View.VISIBLE);
//                editsStarted=false;
//                Exercise toMove=exercises.get(position);
//                exercises.remove(position);
//                notifyItemRemoved(position);
//                exercises.add(position+1,toMove);
//                notifyItemInserted(position+1);
//                Utils.getInstance(mContext).updateWorkoutsExercises(displayedWorkout,exercises);
//            }
//        });
//
//        holder.btnMoveDown.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                holder.expandedExerciseView.setVisibility(View.GONE);
//                holder.btnMenuExercise.setVisibility(View.VISIBLE);
//                editsStarted=false;
//                Exercise toMove=exercises.get(position);
//                exercises.remove(position);
//                notifyItemRemoved(position);
//                exercises.add(toMove);
//                notifyItemInserted(exercises.size()-1);
//                Utils.getInstance(mContext).updateWorkoutsExercises(displayedWorkout,exercises);
//                return false;
//            }
//        });
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

            parentExercise.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, SessionsActivity.class);
                    intent.putExtra(Helpers.EXERCISES_KEY, exercises);
                    intent.putExtra(Helpers.POSITION_KEY, getAdapterPosition());
                    intent.putExtra(Helpers.WORKOUT_KEY, displayedWorkout);
                    mContext.startActivity(intent);
                }
            });

            btnMenuExercise.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onItemClick(getAdapterPosition(),v);
//                    PopupMenu popupMenu = new PopupMenu(mContext, v);
//                    popupMenu.inflate(R.menu.popup_menu_exercise);
//                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//                        @Override
//                        public boolean onMenuItemClick(MenuItem item) {
//                            switch (item.getItemId()) {
//                                case R.id.menuEditE:
//                                    Intent intent = new Intent(mContext, AddExerciseActivity.class);
//                                    intent.putExtra(EXERCISES_KEY, exercises);
//                                    intent.putExtra(POSITION_KEY, getAdapterPosition());
//                                    intent.putExtra(WORKOUT_KEY, displayedWorkout);
//                                    mContext.startActivity(intent);
//                                    return true;
//                                case R.id.menuDeleteE:
//                                    new AlertDialog.Builder(mContext,R.style.DefaultAlertDialogTheme)
//                                            .setTitle(R.string.deletingExercise)
//                                            .setMessage(R.string.sureDeleteThis)
//                                            .setIcon(R.drawable.ic_delete)
//                                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
//                                                @Override
//                                                public void onClick(DialogInterface dialog, int which) {
//                                                    if (exercises.get(getAdapterPosition()).getSuperSet() != 0) {
//                                                        handleDisableSuperset();
//                                                    }
//                                                    Utils.getInstance(mContext).deleteExerciseFromWorkout(exercises.get(getAdapterPosition()));
//                                                    exercises.remove(exercises.get(getAdapterPosition()));
//                                                    notifyItemRemoved(getAdapterPosition());
//
//                                                }
//                                            })
//                                            .setNegativeButton(R.string.no, null)
//                                            .show();
//                                    return true;
//                                case R.id.menuSuperset:
//                                    if (exercises.get(getAdapterPosition()).getSuperSet() != 0) {
//                                        new AlertDialog.Builder(mContext, R.style.DefaultAlertDialogTheme)
//                                                .setTitle(R.string.disablingSuperset)
//                                                .setMessage(R.string.disablingSupersetMsg)
//                                                .setIcon(R.drawable.ic_superset)
//                                                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
//                                                    @Override
//                                                    public void onClick(DialogInterface dialog, int which) {
//                                                        handleDisableSuperset();
//                                                        switch (exercises.get(getAdapterPosition()).getSuperSet()) {
//                                                            case 1:
//                                                                exercises.get(getAdapterPosition()).setSuperSet(0);
//                                                                notifyItemChanged(getAdapterPosition());
//                                                                exercises.get(getAdapterPosition() + 1).setSuperSet(0);
//                                                                notifyItemChanged(getAdapterPosition() + 1);
//                                                                break;
//                                                            case 3:
//                                                                exercises.get(getAdapterPosition()).setSuperSet(0);
//                                                                notifyItemChanged(getAdapterPosition());
//                                                                exercises.get(getAdapterPosition() - 1).setSuperSet(0);
//                                                                notifyItemChanged(getAdapterPosition() - 1);
//                                                                break;
//                                                            case 2:
//                                                                exercises.get(getAdapterPosition()).setSuperSet(0);
//                                                                notifyItemChanged(getAdapterPosition());
//                                                                if(getAdapterPosition()==0){
//                                                                    exercises.get(getAdapterPosition() + 1).setSuperSet(0);
//                                                                    notifyItemChanged(getAdapterPosition() + 1);
//                                                                } else if(getAdapterPosition()==exercises.size()-1){
//                                                                    exercises.get(getAdapterPosition() - 1).setSuperSet(0);
//                                                                    notifyItemChanged(getAdapterPosition() - 1);
//                                                                }else if (exercises.get(getAdapterPosition() - 1).getSuperSet() == 1) {
//                                                                    exercises.get(getAdapterPosition() - 1).setSuperSet(0);
//                                                                    notifyItemChanged(getAdapterPosition() - 1);
//                                                                } else {
//                                                                    exercises.get(getAdapterPosition() + 1).setSuperSet(0);
//                                                                    notifyItemChanged(getAdapterPosition() + 1);
//                                                                }
//                                                                break;
//                                                            default:
//                                                                break;
//                                                        }
//                                                        Utils.getInstance(mContext).updateWorkoutsExercises(displayedWorkout,exercises);
//                                                    }
//                                                })
//                                                .setNegativeButton(R.string.no, null)
//                                                .show();
//
//                                    } else {
//                                        boolean available = true;
//                                        if (exercises.size() == 1 || (getAdapterPosition() == 0 && exercises.get(1).getSuperSet() != 0) ||
//                                                (getAdapterPosition() == exercises.size() - 1 && exercises.get(exercises.size() - 2).getSuperSet() != 0)) {
//                                            Toast.makeText(mContext, R.string.noAvailableExercises, Toast.LENGTH_SHORT).show();
//                                            available = false;
//                                        } else if (getAdapterPosition() == 0) {
//                                            exercisesForSuperset = new String[]{exercises.get(1).getName()};
//                                        } else if (getAdapterPosition() == exercises.size() - 1) {
//                                            exercisesForSuperset = new String[]{exercises.get(exercises.size() - 2).getName()};
//                                        } else if (exercises.get(getAdapterPosition() - 1).getSuperSet() != 0 && exercises.get(getAdapterPosition() + 1).getSuperSet() != 0) {
//                                            Toast.makeText(mContext, R.string.noAvailableExercises, Toast.LENGTH_SHORT).show();
//                                            available = false;
//                                        } else if (exercises.get(getAdapterPosition() - 1).getSuperSet() != 0) {
//                                            exercisesForSuperset = new String[]{exercises.get(getAdapterPosition() + 1).getName()};
//                                        } else if (exercises.get(getAdapterPosition() + 1).getSuperSet() != 0) {
//                                            exercisesForSuperset = new String[]{exercises.get(getAdapterPosition() - 1).getName()};
//                                        } else {
//                                            exercisesForSuperset = new String[]{exercises.get(getAdapterPosition() - 1).getName(), exercises.get(getAdapterPosition() + 1).getName()};
//                                        }
//                                        if (available) {
//                                            new AlertDialog.Builder(mContext, R.style.DefaultAlertDialogTheme)
//                                                    .setTitle(exercises.get(getAdapterPosition()).getName() + " " + mContext.getResources().getString(R.string.supersetWith))
//                                                    .setIcon(R.drawable.ic_superset)
//                                                    .setSingleChoiceItems(exercisesForSuperset, 0, new DialogInterface.OnClickListener() {
//                                                        @Override
//                                                        public void onClick(DialogInterface dialog, int which) {
//                                                            selectedExercise = which;
//                                                        }
//                                                    })
//                                                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
//                                                        @Override
//                                                        public void onClick(DialogInterface dialog, int which) {
//                                                            if (getAdapterPosition() == 0) {
//                                                                exercises.get(getAdapterPosition()).setSuperSet(1);
//                                                                notifyItemChanged(getAdapterPosition());
//                                                                exercises.get(1).setSuperSet(2);
//                                                                notifyItemChanged(1);
//                                                            } else if (getAdapterPosition() == exercises.size() - 1) {
//                                                                exercises.get(getAdapterPosition()).setSuperSet(2);
//                                                                notifyItemChanged(getAdapterPosition());
//                                                                exercises.get(exercises.size() - 2).setSuperSet(1);
//                                                                notifyItemChanged(exercises.size() - 2);
//                                                            } else {
//                                                                if (selectedExercise == 0) {
//                                                                    exercises.get(getAdapterPosition()).setSuperSet(2);
//                                                                    notifyItemChanged(getAdapterPosition());
//                                                                    exercises.get(getAdapterPosition() - 1).setSuperSet(1);
//                                                                    notifyItemChanged(getAdapterPosition() - 1);
//                                                                } else {
//                                                                    exercises.get(getAdapterPosition()).setSuperSet(1);
//                                                                    notifyItemChanged(getAdapterPosition());
//                                                                    exercises.get(getAdapterPosition() + 1).setSuperSet(2);
//                                                                    notifyItemChanged(getAdapterPosition() + 1);
//                                                                }
//                                                            }
//                                                            Utils.getInstance(mContext).updateWorkoutsExercises(displayedWorkout, exercises);
//                                                        }
//                                                    })
//                                                    .setNegativeButton(R.string.cancel, null)
//                                                    .show();
//                                        }
//                                    }
//
//                                    return true;
//                                default:
//                                    return false;
//                            }
//                        }
//                    });
//                    popupMenu.show();
                }
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

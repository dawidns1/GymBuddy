package com.example.gymbuddy.recyclerViewAdapters;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gymbuddy.R;
import com.example.gymbuddy.helpers.Helpers;
import com.example.gymbuddy.model.Exercise;
import com.example.gymbuddy.model.Session;

import java.text.MessageFormat;
import java.util.ArrayList;

public class SessionsRVAdapter extends RecyclerView.Adapter<SessionsRVAdapter.ViewHolder> {

    private ArrayList<Session> sessions = new ArrayList<>();
    private final Context mContext;
    private ArrayList<Exercise> exercises = new ArrayList<>();
    private Exercise exercise;
    private boolean menuShown = true;
    private int highlightPosition = -1;
    private int lastHighlightPosition =-1;
    private final ConstraintSet constraintSet = new ConstraintSet();
    private ConstraintLayout.LayoutParams params, targetParams;

    public void setHighlightPosition(int highlightPosition) {
        this.lastHighlightPosition=this.highlightPosition;
        this.highlightPosition = highlightPosition;
    }

    public SessionsRVAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public void setMenuShown(boolean menuShown) {
        this.menuShown = menuShown;
    }

    public void setSessions(ArrayList<Session> sessions) {
        this.sessions = sessions;
    }

    public void setExercises(ArrayList<Exercise> exercises) {
        this.exercises = exercises;
    }

    public void setExercise(Exercise exercise) {
        this.exercise = exercise;
        highlightPosition = -1;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_session, parent, false);
        return new SessionsRVAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.txtDate.setText(sessions.get(position).getDate());
        if (!menuShown) {
            holder.btnMenuSession.setVisibility(View.GONE);
        }
        if (exercise.getSets() >= 4) {
            holder.txtLxR4.setVisibility(View.VISIBLE);
            holder.separator3.setVisibility(View.VISIBLE);
        } else {
            holder.txtLxR4.setVisibility(View.GONE);
            holder.separator3.setVisibility(View.GONE);
        }
        if (exercise.getSets() >= 5) {
            holder.txtLxR5.setVisibility(View.VISIBLE);
        } else {
            holder.txtLxR5.setVisibility(View.GONE);
        }
        if (exercise.getSets() >= 6) {
            holder.txtLxR6.setVisibility(View.VISIBLE);
            holder.separator4.setVisibility(View.VISIBLE);
        } else {
            holder.txtLxR6.setVisibility(View.GONE);
            holder.separator4.setVisibility(View.GONE);
        }
        if (exercise.getSets() >= 7) {
            holder.txtLxR7.setVisibility(View.VISIBLE);
            holder.separator5.setVisibility(View.VISIBLE);
        } else {
            holder.txtLxR7.setVisibility(View.GONE);
            holder.separator5.setVisibility(View.GONE);
        }
        if (exercise.getSets() >= 8) {
            holder.txtLxR8.setVisibility(View.VISIBLE);
            holder.separator6.setVisibility(View.VISIBLE);
        } else {
            holder.txtLxR8.setVisibility(View.GONE);
            holder.separator6.setVisibility(View.GONE);
        }
        float total = 0;
        for (int i = 0; i < holder.txtLxRs.size(); i++) {
            holder.txtLxRs.get(i).setText(MessageFormat.format("{0}x{1}",
                    Helpers.stringFormat(Math.round(10.0 * sessions.get(position).getLoad()[i]) / 10.0),
                    sessions.get(position).getReps()[i]));
            total += sessions.get(position).getLoad()[i] * sessions.get(position).getReps()[i];
        }

        holder.txtTotal.setText(Helpers.stringFormat(Math.round(total)));

        if (highlightPosition != -1 && (position == 0 || menuShown)) {
            holder.highlightSession.setAlpha(0.0f);
            if(!holder.highlightSession.isShown())holder.highlightSession.setVisibility(View.VISIBLE);
            params = (ConstraintLayout.LayoutParams) holder.highlightSession.getLayoutParams();
            params.endToEnd = holder.txtLxRs.get(highlightPosition).getId();
            params.topToTop = holder.txtLxRs.get(highlightPosition).getId();
            holder.highlightSession.requestLayout();
            Helpers.shakeVertically(holder.highlightSession,500);
            holder.highlightSession.animate().alpha(1.0f)
                    .setDuration(500)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                        }
                    });
            if(highlightPosition>0 && highlightPosition>lastHighlightPosition){
//                holder.txtLxRs.get(highlightPosition-1).animate().scaleY(1.2f)
////                        .scaleX(1.2f)
////                        .setDuration(250)
////                        .setListener(new AnimatorListenerAdapter() {
////                            @Override
////                            public void onAnimationEnd(Animator animation) {
////                                super.onAnimationEnd(animation);
////                                if(highlightPosition>1) {
////                                    holder.txtLxRs.get(highlightPosition - 1).animate().scaleY(1.0f)
////                                            .scaleX(1.0f)
////                                            .setDuration(250);
////                                }
////                            }
////                        });
                PropertyValuesHolder scalex = PropertyValuesHolder.ofFloat(View.SCALE_X, 1.3f);
                PropertyValuesHolder scaley = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.3f);
                ObjectAnimator anim = ObjectAnimator.ofPropertyValuesHolder(holder.txtLxRs.get(highlightPosition-1), scalex, scaley);
                anim.setRepeatCount(1);
                anim.setRepeatMode(ValueAnimator.REVERSE);
                anim.setDuration(250);
                anim.start();
            }
        } else {
            holder.highlightSession.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return sessions.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView txtDate;
        private final TextView txtTotal;
        private final TextView txtLxR1;
        private final TextView txtLxR2;
        private final TextView txtLxR3;
        private final TextView txtLxR4;
        private final TextView txtLxR5;
        private final TextView txtLxR6;
        private final TextView txtLxR7;
        private final TextView txtLxR8;
        private final View separator3;
        private final View separator4;
        private final View separator5;
        private final View separator6;
        private final ImageView btnMenuSession;
        private final ImageView highlightSession;
        private final ArrayList<TextView> txtLxRs = new ArrayList<>();

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

//            ConstraintLayout parentSessionConstraint = itemView.findViewById(R.id.parentSessionConstraint);
            CardView parentSession = itemView.findViewById(R.id.parentSession);
            txtDate = itemView.findViewById(R.id.txtDate);
            txtTotal = itemView.findViewById(R.id.txtTotal);
            txtLxR1 = itemView.findViewById(R.id.txtLxR1);
            txtLxR2 = itemView.findViewById(R.id.txtLxR2);
            txtLxR3 = itemView.findViewById(R.id.txtLxR3);
            txtLxR4 = itemView.findViewById(R.id.txtLxR4);
            txtLxR5 = itemView.findViewById(R.id.txtLxR5);
            txtLxR6 = itemView.findViewById(R.id.txtLxR6);
            txtLxR7 = itemView.findViewById(R.id.txtLxR7);
            txtLxR8 = itemView.findViewById(R.id.txtLxR8);
            txtLxRs.add(txtLxR1);
            txtLxRs.add(txtLxR2);
            txtLxRs.add(txtLxR3);
            txtLxRs.add(txtLxR4);
            txtLxRs.add(txtLxR5);
            txtLxRs.add(txtLxR6);
            txtLxRs.add(txtLxR7);
            txtLxRs.add(txtLxR8);
            separator3 = itemView.findViewById(R.id.separator3);
            separator4 = itemView.findViewById(R.id.separator4);
            separator5 = itemView.findViewById(R.id.separator5);
            separator6 = itemView.findViewById(R.id.separator6);
            highlightSession = itemView.findViewById(R.id.highlightSession);

            btnMenuSession = itemView.findViewById(R.id.btnMenuSession);
            btnMenuSession.setVisibility(View.GONE);

            parentSession.setOnClickListener(v -> {
                if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                    onItemClickListener.onItemClick(getAdapterPosition());
                }
            });

            if (menuShown) {
                parentSession.setOnLongClickListener(v -> {
                    if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                        onItemLongClickListener.onItemLongClick(getAdapterPosition(), v);
                        return true;
                    } else {
                        return false;
                    }
                });
            }
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    OnItemClickListener onItemClickListener;

    OnItemLongClickListener onItemLongClickListener;

    public interface OnItemClickListener {
        void onItemClick(int positionRV);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(int positionRV, View v);
    }

}


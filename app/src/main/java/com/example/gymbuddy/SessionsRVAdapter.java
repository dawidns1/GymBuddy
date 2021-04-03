package com.example.gymbuddy;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class SessionsRVAdapter extends RecyclerView.Adapter<SessionsRVAdapter.ViewHolder> {

    private ArrayList<Session> sessions = new ArrayList<>();
    private Context mContext;
    private ArrayList<Exercise> exercises = new ArrayList<>();
    private Exercise exercise;
    private boolean menuShown = true;

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
        holder.txtLxR1.setText(stringFormat(Math.round(10.0 * sessions.get(position).getLoad()[0]) / 10.0) + "x" + sessions.get(position).getReps()[0]);
        holder.txtLxR2.setText(stringFormat(Math.round(10.0 * sessions.get(position).getLoad()[1]) / 10.0) + "x" + sessions.get(position).getReps()[1]);
        holder.txtLxR3.setText(stringFormat(Math.round(10.0 * sessions.get(position).getLoad()[2]) / 10.0) + "x" + sessions.get(position).getReps()[2]);
        holder.txtLxR4.setText(stringFormat(Math.round(10.0 * sessions.get(position).getLoad()[3]) / 10.0) + "x" + sessions.get(position).getReps()[3]);
        holder.txtLxR5.setText(stringFormat(Math.round(10.0 * sessions.get(position).getLoad()[4]) / 10.0) + "x" + sessions.get(position).getReps()[4]);
        holder.txtLxR6.setText(stringFormat(Math.round(10.0 * sessions.get(position).getLoad()[5]) / 10.0) + "x" + sessions.get(position).getReps()[5]);
        holder.txtLxR7.setText(stringFormat(Math.round(10.0 * sessions.get(position).getLoad()[6]) / 10.0) + "x" + sessions.get(position).getReps()[6]);
        holder.txtLxR8.setText(stringFormat(Math.round(10.0 * sessions.get(position).getLoad()[7]) / 10.0) + "x" + sessions.get(position).getReps()[7]);

        float total = 0;
        for (int i = 0; i < sessions.get(position).getReps().length; i++) {
            total += sessions.get(position).getLoad()[i] * sessions.get(position).getReps()[i];
        }
        holder.txtTotal.setText(stringFormat(Math.round(total)));

//        if (position + 1 == getItemCount()) {
//            setBottomMargin(holder.itemView, (int) (5 * Resources.getSystem().getDisplayMetrics().density));
//        } else {
//            setBottomMargin(holder.itemView, 0);
//        }

    }

    @Override
    public int getItemCount() {
        return sessions.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private CardView parentSession;
        private TextView txtDate;
        private TextView txtTotal;
        private TextView txtLxR1;
        private TextView txtLxR2;
        private TextView txtLxR3;
        private TextView txtLxR4;
        private TextView txtLxR5;
        private TextView txtLxR6;
        private TextView txtLxR7;
        private TextView txtLxR8;
        private View separator3, separator4, separator5, separator6;
        ImageView btnMenuSession;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            parentSession = itemView.findViewById(R.id.parentSession);
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
            separator3 = itemView.findViewById(R.id.separator3);
            separator4 = itemView.findViewById(R.id.separator4);
            separator5 = itemView.findViewById(R.id.separator5);
            separator6 = itemView.findViewById(R.id.separator6);

            btnMenuSession = itemView.findViewById(R.id.btnMenuSession);
            btnMenuSession.setVisibility(View.GONE);

            parentSession.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onItemClick(getAdapterPosition());
                }
            });

            if (menuShown) {
                parentSession.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        onItemLongClickListener.onItemLongClick(getAdapterPosition(),v);
                    return true;
                    }
                });
            }
        }
    }

    public static String stringFormat(double d) {
        if (d == (long) d)
            return String.format("%d", (long) d);
        else
            return String.format("%s", d);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener){
        this.onItemLongClickListener=onItemLongClickListener;
    }

    OnItemClickListener onItemClickListener;

    OnItemLongClickListener onItemLongClickListener;

    public interface OnItemClickListener {
        void onItemClick(int positionRV);
    }

    public interface OnItemLongClickListener{
        void onItemLongClick(int positionRV, View v);
    }

}


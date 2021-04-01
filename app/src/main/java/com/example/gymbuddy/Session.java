package com.example.gymbuddy;

import android.os.Build;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Calendar;

public class Session implements Serializable {
    private int id;
    private float load[];
    private int reps[];
    private String date;

    public Session(int id, float[] load, int[] reps) {
        this.id = id;
        this.load = load;
        this.reps = reps;
    }

    @Override
    public String toString() {
        return "Session{" +
                "id=" + id +
                ", load=" + Arrays.toString(load) +
                ", reps=" + Arrays.toString(reps) +
                ", date='" + date + '\'' +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public float[] getLoad() {
        return load;
    }

    public void setLoad(float[] load) {
        this.load = load;
    }

    public int[] getReps() {
        return reps;
    }

    public void setReps(int[] reps) {
        this.reps = reps;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}

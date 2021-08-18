package com.example.gymbuddy.model;

import java.io.Serializable;

public class Session implements Serializable {
    private int id;
    private float[] load;
    private int[] reps;
    private String date;

    public Session(int id, float[] load, int[] reps) {
        this.id = id;
        this.load = load;
        this.reps = reps;
    }

//    @Override
//    public String toString() {
//        return "Session{" +
//                "id=" + id +
//                ", load=" + Arrays.toString(load) +
//                ", reps=" + Arrays.toString(reps) +
//                ", date='" + date + '\'' +
//                '}';
//    }

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

    public float getTotal(){
        int total=0;
        for(int i=0;i<load.length;i++){
            total+=load[i]*reps[i];
        }
        return total;
    }
}

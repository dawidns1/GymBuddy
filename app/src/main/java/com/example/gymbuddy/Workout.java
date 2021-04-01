package com.example.gymbuddy;

import android.widget.ListView;

import java.io.Serializable;
import java.util.ArrayList;

public class Workout implements Serializable {
    private int id;
    private String name;
    private int exerciseNumber;
    private String type;
    private String muscleGroup;
    private String muscleGroupSecondary;
    private ArrayList<Exercise> exercises;
    private int[] state;

    public Workout(int id, String name, String type, String muscleGroup, String muscleGroupSecondary, int exerciseNumber) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.muscleGroup = muscleGroup;
        this.muscleGroupSecondary = muscleGroupSecondary;
        this.exerciseNumber=exerciseNumber;
        this.exercises=new ArrayList<Exercise>();
        this.state=new int[]{1,1,1};
    }

    @Override
    public String toString() {
        return "Workout{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", exerciseNumber='" + exerciseNumber + '\'' +
                ", type='" + type + '\'' +
                ", muscleGroup='" + muscleGroup + '\'' +
                ", muscleGroupSecondary='" + muscleGroupSecondary + '\'' +
                '}';
    }

    public int[] getState() {
        return state;
    }

    public void setState(int[] state) {
        this.state = state;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getExerciseNumber() {
        return exerciseNumber;
    }

    public void setExerciseNumber(int exerciseNumber) {
        this.exerciseNumber = exerciseNumber;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMuscleGroup() {
        return muscleGroup;
    }

    public void setMuscleGroup(String muscleGroup) {
        this.muscleGroup = muscleGroup;
    }

    public String getMuscleGroupSecondary() {
        return muscleGroupSecondary;
    }

    public void setMuscleGroupSecondary(String muscleGroupSecondary) {
        this.muscleGroupSecondary = muscleGroupSecondary;
    }

    public ArrayList<Exercise> getExercises() {
        return exercises;
    }

    public void setExercises(ArrayList<Exercise> exercises) {
        this.exercises = exercises;
    }
}

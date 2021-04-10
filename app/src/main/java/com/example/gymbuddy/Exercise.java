package com.example.gymbuddy;

import java.io.Serializable;
import java.util.ArrayList;

public class Exercise implements Serializable {
    private int id;
    private String name;
    private int sets;
    private int breaks;
    private int tempo;
    private int superSet;
    private String muscleGroup;
    private String muscleGroupSecondary;
    private ArrayList<Session> sessions;


    public Exercise(int id, String name, int sets, int breaks, String muscleGroup, String muscleGroupSecondary, int tempo) {
        this.id = id;
        this.name = name;
        this.sets = sets;
        this.breaks = breaks;
        this.tempo=tempo;
        this.muscleGroup = muscleGroup;
        this.muscleGroupSecondary=muscleGroupSecondary;
        this.sessions= new ArrayList<>();
        this.superSet=0;
    }

    public int getSuperSet() {
        return superSet;
    }

    public void setSuperSet(int superSet) {
        this.superSet = superSet;
    }

    public int getTempo() {
        return tempo;
    }

    public void setTempo(int tempo) {
        this.tempo = tempo;
    }

    public String getMuscleGroupSecondary() {
        return muscleGroupSecondary;
    }

    public void setMuscleGroupSecondary(String muscleGroupSecondary) {
        this.muscleGroupSecondary = muscleGroupSecondary;
    }

    public ArrayList<Session> getSessions() {
        return sessions;
    }

    public void setSessions(ArrayList<Session> sessions) {
        this.sessions = sessions;
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

    public int getSets() {
        return sets;
    }

    public void setSets(int sets) {
        this.sets = sets;
    }

    public int getBreaks() {
        return breaks;
    }

    public void setBreaks(int breaks) {
        this.breaks = breaks;
    }

    public String getMuscleGroup() {
        return muscleGroup;
    }

    public void setMuscleGroup(String muscleGroup) {
        this.muscleGroup = muscleGroup;
    }

    @Override
    public String toString() {
        return "Exercise{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", sets='" + sets + '\'' +
                ", breaks='" + breaks + '\'' +
                ", muscleGroup='" + muscleGroup + '\'' +
                ", muscleGroupSecondary='" + muscleGroupSecondary + '\'' +
                '}';
    }
}

package com.example.gymbuddy.model;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.ServerTimestamp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.gymbuddy.helpers.Helpers.WORKOUT_EXERCISE_NUMBER_KEY;
import static com.example.gymbuddy.helpers.Helpers.WORKOUT_ID_KEY;
import static com.example.gymbuddy.helpers.Helpers.WORKOUT_MUSCLE_GROUP_KEY;
import static com.example.gymbuddy.helpers.Helpers.WORKOUT_MUSCLE_GROUP_SECONDARY_KEY;
import static com.example.gymbuddy.helpers.Helpers.WORKOUT_NAME_KEY;
import static com.example.gymbuddy.helpers.Helpers.WORKOUT_TIMESTAMP_KEY;
import static com.example.gymbuddy.helpers.Helpers.WORKOUT_TYPE_KEY;

@IgnoreExtraProperties
public class Workout implements Serializable {
    private int id;
    private String name;
    private int exerciseNumber;
    private String type;
    private String muscleGroup;
    private String muscleGroupSecondary;
    private ArrayList<Exercise> exercises;
    int[] state;
    private String cloudID;
    @ServerTimestamp
    private Date timestamp;

    public Workout(int id, String name, String type, String muscleGroup, String muscleGroupSecondary, int exerciseNumber) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.muscleGroup = muscleGroup;
        this.muscleGroupSecondary = muscleGroupSecondary;
        this.exerciseNumber = exerciseNumber;
        this.exercises = new ArrayList<>();
        this.state = new int[]{1, 1, 1};
        this.cloudID = "asd";
        this.timestamp = null;
    }

    public static HashMap<String, Object> workoutToHashmap(Workout workout) {
        HashMap<String, Object> workoutHashMap = new HashMap<>();
        workoutHashMap.put(WORKOUT_ID_KEY, workout.getId());
        workoutHashMap.put(WORKOUT_NAME_KEY, workout.getName());
        workoutHashMap.put(WORKOUT_EXERCISE_NUMBER_KEY, workout.getExerciseNumber());
        workoutHashMap.put(WORKOUT_TYPE_KEY, workout.getType());
        workoutHashMap.put(WORKOUT_MUSCLE_GROUP_KEY, workout.getMuscleGroup());
        workoutHashMap.put(WORKOUT_MUSCLE_GROUP_SECONDARY_KEY, workout.getMuscleGroupSecondary());
        workoutHashMap.put(WORKOUT_TIMESTAMP_KEY, FieldValue.serverTimestamp());
        return workoutHashMap;
    }

    public static ArrayList<Workout> mapOfWorkoutListToArrayList(List<Map<String, Object>> workoutsMaps) {
        ArrayList<Workout> workouts = new ArrayList<>();
        for (Map<String, Object> m : workoutsMaps) {
            workouts.add(new Workout((int) m.get(WORKOUT_ID_KEY),
                    String.valueOf(m.get(WORKOUT_NAME_KEY)),
                    String.valueOf(m.get(WORKOUT_TYPE_KEY)),
                    String.valueOf(m.get(WORKOUT_MUSCLE_GROUP_KEY)),
                    String.valueOf(m.get(WORKOUT_MUSCLE_GROUP_SECONDARY_KEY)),
                    0));
        }
        return workouts;
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

    public int getExerciseNumber() {
        return exerciseNumber;
    }

    public String getCloudID() {
        return cloudID;
    }

    public void setCloudID(String cloudID) {
        this.cloudID = cloudID;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
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

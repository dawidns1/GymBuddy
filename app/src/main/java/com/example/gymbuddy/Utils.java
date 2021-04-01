package com.example.gymbuddy;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class Utils {

    private static Utils instance;
    private SharedPreferences sharedPreferences;
    private static final String ALL_WORKOUTS_KEY = "all workouts";
    private static final String WORKOUTS_ID_KEY = "workoutsID";
    private static final String EXERCISES_ID_KEY = "exercisesID";
    public static final String ARE_EXERCISES_SHOWN_KEY = "are shown";
    public static final String EXPORTED_WORKOUTS_KEY = "exported";
    public static final String SYSTEM_TIME_KEY="system time";
    public static final String BREAK_LEFT_KEY="break left";
    public static final String LOGGING_KEY="logging";
    public static final String CALENDAR_ID_KEY="calendar";
//    public static final String WORKOUT_STATE_KEY="workout state";

    static boolean areExercisesShown;
    static boolean loggingEnabled;
    static int workoutsID;
    static int exercisesID;
    static long breakLeft;
    static long systemTime;

    static String calendarID;

    private ArrayList<Workout> exportedWorkouts;

    public Utils(Context context) {

        sharedPreferences = context.getSharedPreferences("workouts_db", Context.MODE_PRIVATE);

        if (null == getAllWorkouts()) {
            initData();
        }

//        workoutState=getWorkoutState();

        calendarID=getCalendarID();

        workoutsID = getWorkoutsId();

        exercisesID = getExercisesId();

        areExercisesShown = isAreExercisesShown();

        loggingEnabled=isLoggingEnabled();

        exportedWorkouts = new ArrayList<>();

        breakLeft=getBreakLeft();

        systemTime=getSystemTime();

        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
    }

    private void initData() {

        ArrayList<Workout> workouts = new ArrayList<>();
        ArrayList<Exercise> exercises1 = new ArrayList<>();
        ArrayList<Exercise> exercises2 = new ArrayList<>();
        ArrayList<Session> sessions1 = new ArrayList<>();
        ArrayList<Session> sessions2 = new ArrayList<>();

//        workouts.add(new Workout(99, "FBW5", "FBW", "Legs", "Lats", 2));
//        workouts.add(new Workout(98, "FBW2", "FBW", "Delts", "Traps", 1));
//
//        exercises1.add(new Exercise(99, "Bench press", 3, 90, "Pecs", "Delts", 1010));
//        exercises1.add(new Exercise(98, "Pull up", 3, 90, "Lats", "Traps", 1010));
//        exercises2.add(new Exercise(97, "Military press", 3, 90, "Delts", "None", 1010));
//
//        sessions1.add(new Session(1, new float[]{1, 1, 1, 0, 0, 0, 0, 0}, new int[]{1, 1, 1, 0, 0, 0, 0, 0}));
//        sessions1.add(new Session(2, new float[]{1, 2, 3, 0, 0, 0, 0, 0}, new int[]{1, 1, 1, 0, 0, 0, 0, 0}));
//        exercises1.get(0).setSessions(sessions1);
//
//        sessions2.add(new Session(3, new float[]{2, 2, 2, 0, 0, 0, 0, 0}, new int[]{3, 3, 3, 0, 0, 0, 0, 0}));
//        sessions2.add(new Session(4, new float[]{1, 2, 3, 0, 0, 0, 0, 0}, new int[]{1, 1, 4, 0, 0, 0, 0, 0}));
//        exercises1.get(1).setSessions(sessions2);
////
//        workouts.get(0).setExercises(exercises1);
//        workouts.get(1).setExercises(exercises2);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        editor.putString(ALL_WORKOUTS_KEY, gson.toJson(workouts));
        editor.commit();
    }

    public static Utils getInstance(Context context) {
        if (null == instance) {
            instance = new Utils(context);
        }
        return instance;
    }

    public ArrayList<Workout> getAllWorkouts() {
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<Workout>>() {
        }.getType();
        ArrayList<Workout> workouts = gson.fromJson(sharedPreferences.getString(ALL_WORKOUTS_KEY, null), type);
        return workouts;
    }

    public String getCalendarID() {
        String calendarID=sharedPreferences.getString(CALENDAR_ID_KEY,"");
        return calendarID;
    }

    public void setCalendarID(String calendarID) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(CALENDAR_ID_KEY, calendarID);
        editor.commit();
    }

    public int getWorkoutsId() {
        int workoutsId = sharedPreferences.getInt(WORKOUTS_ID_KEY, 0);
        return workoutsId;
    }

    public int getExercisesId() {
        int exercisesId = sharedPreferences.getInt(EXERCISES_ID_KEY, 0);
        return exercisesId;
    }

    public boolean setWorkoutsId(int workoutsID) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(WORKOUTS_ID_KEY, workoutsID);
        editor.commit();
        return true;
    }

    public boolean setExercisesId(int exercisesID) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(EXERCISES_ID_KEY, exercisesID);
        editor.commit();
        return true;
    }

    public long getBreakLeft() {
        long breakLeft = sharedPreferences.getLong(BREAK_LEFT_KEY, 0);
        return breakLeft;
    }

    public long getSystemTime() {
        long systemTime = sharedPreferences.getLong(SYSTEM_TIME_KEY, 0);
        return systemTime;
    }

    public boolean setBreakLeft(long breakLeft) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(BREAK_LEFT_KEY, breakLeft);
        editor.commit();
        return true;
    }

    public boolean setSystemTime(long systemTime) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(SYSTEM_TIME_KEY, systemTime);
        editor.commit();
        return true;
    }

//    public static int[] getWorkoutState() {
//        return workoutState;
//    }
//
//    public static void setWorkoutState(int[] workoutState) {
//        Utils.workoutState = workoutState;
//    }

    public ArrayList<Workout> getExportedWorkouts() {
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<Workout>>() {
        }.getType();
        ArrayList<Workout> workouts = gson.fromJson(sharedPreferences.getString(EXPORTED_WORKOUTS_KEY, null), type);
        return workouts;
    }

    public void setExportedWorkouts(ArrayList<Workout> exportedWorkouts) {
        Gson gson = new Gson();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(EXPORTED_WORKOUTS_KEY);
        editor.putString(EXPORTED_WORKOUTS_KEY, gson.toJson(exportedWorkouts));
        editor.commit();
    }

    public boolean isAreExercisesShown() {
        boolean areExercisesShown = sharedPreferences.getBoolean(ARE_EXERCISES_SHOWN_KEY, true);
        return areExercisesShown;
    }

    public void setAreExercisesShown(boolean areExercisesShown) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(ARE_EXERCISES_SHOWN_KEY, areExercisesShown);
        editor.commit();
    }

    public boolean isLoggingEnabled() {
        boolean loggingEnabled = sharedPreferences.getBoolean(LOGGING_KEY, false);
        return loggingEnabled;
    }

    public void setLoggingEnabled(boolean loggingEnabled) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(LOGGING_KEY, loggingEnabled);
        editor.commit();
    }

    public boolean updateWorkouts(ArrayList<Workout> workouts) {
        Gson gson = new Gson();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(ALL_WORKOUTS_KEY);
        editor.putString(ALL_WORKOUTS_KEY, gson.toJson(workouts));
        editor.commit();
        return true;
    }

    public boolean updateSingleWorkouts(Workout workout) {
        ArrayList<Workout> workouts = getAllWorkouts();
        if (null != workouts) {
            for (Workout w : workouts) {
                if (w.getId() == workout.getId()) {
                    w.setState(workout.getState());
                    Gson gson = new Gson();
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.remove(ALL_WORKOUTS_KEY);
                    editor.putString(ALL_WORKOUTS_KEY, gson.toJson(workouts));
                    editor.commit();
                    return true;
                }
            }
        }
        return false;
    }

    public boolean addToAllWorkouts(Workout workout) {
        int id = getWorkoutsId();
        workout.setId(id);
        id++;
        setWorkoutsId(id);
        if(!workout.getExercises().isEmpty()){
            int idE=getExercisesId();
            for(Exercise e:workout.getExercises()){
                e.setId(idE);
                idE++;
            }
            setExercisesId(idE);
        }
        ArrayList<Workout> workouts = getAllWorkouts();
        if (null != workouts) {
            if (workouts.add(workout)) {
                Gson gson = new Gson();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.remove(ALL_WORKOUTS_KEY);
                editor.putString(ALL_WORKOUTS_KEY, gson.toJson(workouts));
                editor.commit();
                return true;
            }
        }
        return false;
    }

    public boolean deleteWorkout(Workout workout) {
        ArrayList<Workout> workouts = getAllWorkouts();
        if (null != workouts) {
            for (Workout w : workouts) {
                if (w.getId() == workout.getId()) {
                    if (workouts.remove(w)) {
                        Gson gson = new Gson();
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.remove(ALL_WORKOUTS_KEY);
                        editor.putString(ALL_WORKOUTS_KEY, gson.toJson(workouts));
                        editor.commit();
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean addExerciseToWorkout(Workout workout, Exercise exercise) {
        int id = getExercisesId();
        exercise.setId(id);
        id++;
        setExercisesId(id);
        ArrayList<Workout> workouts = getAllWorkouts();
        if (null != workouts) {
            for (Workout w : workouts) {
                if (w.getId() == workout.getId()) {
                    ArrayList<Exercise> exercises = w.getExercises();
                    if (exercises.add(exercise)) {
                        w.setExercises(exercises);
                        w.setExerciseNumber(w.getExercises().size());
                        Gson gson = new Gson();
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.remove(ALL_WORKOUTS_KEY);
                        editor.putString(ALL_WORKOUTS_KEY, gson.toJson(workouts));
                        editor.commit();
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean deleteExerciseFromWorkout(Exercise exercise) {
        ArrayList<Workout> workouts = getAllWorkouts();
        if (null != workouts) {
            for (Workout w : workouts) {
                ArrayList<Exercise> exercises = w.getExercises();
                for (Exercise e : exercises)
                    if (e.getId() == exercise.getId()) {
                        if (exercises.remove(e)) {
                            w.setExercises(exercises);
                            w.setExerciseNumber(w.getExercises().size());
                            Gson gson = new Gson();
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.remove(ALL_WORKOUTS_KEY);
                            editor.putString(ALL_WORKOUTS_KEY, gson.toJson(workouts));
                            editor.commit();
                            return true;
                        }
                    }
            }
        }
        return false;
    }

    public boolean updateWorkoutsExercises(Workout workout, ArrayList<Exercise> exercises) {
        ArrayList<Workout> workouts = getAllWorkouts();
        if (null != workouts) {
            for (Workout w : workouts) {
                if (w.getId() == workout.getId()) {
                    w.setExercises(exercises);
                    Gson gson = new Gson();
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.remove(ALL_WORKOUTS_KEY);
                    editor.putString(ALL_WORKOUTS_KEY, gson.toJson(workouts));
                    editor.commit();
                    return true;
                }
            }
        }
        return false;
    }

    public boolean updateWorkoutsExercisesWithoutWorkout(ArrayList<Exercise> exercises) {
        ArrayList<Workout> workouts = getAllWorkouts();
        if (null != workouts) {
            for (Workout w : workouts) {
                if (w.getExercises().get(0).getId() == exercises.get(0).getId()) {
                    w.setExercises(exercises);
                    Gson gson = new Gson();
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.remove(ALL_WORKOUTS_KEY);
                    editor.putString(ALL_WORKOUTS_KEY, gson.toJson(workouts));
                    editor.commit();
                    return true;
                }
            }
        }
        return false;
    }
}




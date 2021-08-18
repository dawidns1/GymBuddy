package com.example.gymbuddy.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ShareCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gymbuddy.R;
import com.example.gymbuddy.helpers.TemplateView;
import com.example.gymbuddy.helpers.Helpers;
import com.example.gymbuddy.helpers.Utils;
import com.example.gymbuddy.model.Exercise;
import com.example.gymbuddy.model.Workout;
import com.example.gymbuddy.recyclerViewAdapters.WorkoutsRVAdapter;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.AdapterStatus;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static android.view.View.GONE;
import static com.example.gymbuddy.helpers.Helpers.CREATE_FILE;
import static com.example.gymbuddy.helpers.Helpers.FILE_SHARING;
import static com.example.gymbuddy.helpers.Helpers.MODE_ADD;
import static com.example.gymbuddy.helpers.Helpers.MODE_DELETE;
import static com.example.gymbuddy.helpers.Helpers.MODE_SAVE_TO_CLOUD;
import static com.example.gymbuddy.helpers.Helpers.MODE_SAVE_TO_FILE;
import static com.example.gymbuddy.helpers.Helpers.MODE_SHARE;
import static com.example.gymbuddy.helpers.Helpers.PICK_FILE;

public class MainActivity extends AppCompatActivity implements WorkoutsRVAdapter.OnItemClickListener {

    private RecyclerView workoutsRV;
    ArrayList<Workout> workoutsToSave = new ArrayList<>();
    ArrayList<Workout> workouts = new ArrayList<>();
    ArrayList<Workout> returnList = new ArrayList<>();
    private ExtendedFloatingActionButton btnAddWorkout;
    public WorkoutsRVAdapter adapter = new WorkoutsRVAdapter(this);
    private boolean areExercisesShown;
    private boolean loggingEnabled;
    private boolean groupingEnabled;
    private boolean doubleBackToExitPressedOnce = false;
    public static final int REQUEST_CALENDAR = 2;
    private static final String[] PERMISSIONS_CALENDAR = {
            Manifest.permission.READ_CALENDAR,
            Manifest.permission.WRITE_CALENDAR};
    private ArrayList<String> calendarId, calendarName;
    private int selected;
    private boolean checkingForCalendarForScheduling = false;
    private TemplateView mainAdTemplate;
    private static final String TAG = "MainActivity";
    private final Gson gson = new Gson();
    private File directoryFile = null;
    private boolean tryingToLoad = false;
    private boolean tryingToSave = false;
    private boolean tryingToDelete = false;
    private MenuItem authMenuItem;
    private DocumentReference userRef;
    private FirebaseUser user;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(), this::onSignInResult);

    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        IdpResponse response = result.getIdpResponse();
        if (result.getResultCode() == RESULT_OK) {
            user = FirebaseAuth.getInstance().getCurrentUser();
            authMenuItem.setTitle(getResources().getString(R.string.signOut));
            buildAndShowSignedInMessage(user);
            if (tryingToLoad) {
                tryingToLoad = false;
                handleLoadFromCloud(MODE_ADD);
            } else if (tryingToSave) {
                tryingToSave = false;
                handlePickToExport(MODE_SAVE_TO_CLOUD);
            } else if (tryingToDelete) {
                tryingToDelete = false;
                handleLoadFromCloud(MODE_DELETE);
            }
        } else {
            user = null;
            Toast.makeText(this, getResources().getString(R.string.errorSigningIn), Toast.LENGTH_SHORT).show();
            authMenuItem.setTitle(getResources().getString(R.string.signIn));
            Log.d(TAG, "onSignInResultError: " + response);
        }
    }

    private void createFile() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TITLE, "myWorkout.txt");
        startActivityForResult(intent, CREATE_FILE);
    }

    private void saveToFile(Uri uri) {
        try {
            ParcelFileDescriptor pfd = this.getContentResolver().
                    openFileDescriptor(uri, "w");
            FileOutputStream fileOutputStream =
                    new FileOutputStream(pfd.getFileDescriptor());
            fileOutputStream.write((gson.toJson(workoutsToSave)).getBytes());
            fileOutputStream.close();
            workoutsToSave.clear();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "saveToFile: " + e);
        }
    }

    private void loadFromFile(Uri uri) {
        StringBuilder stringBuilder = new StringBuilder();
        try (InputStream inputStream =
                     getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(Objects.requireNonNull(inputStream)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            Type type = new TypeToken<ArrayList<Workout>>() {
            }.getType();
            returnList = gson.fromJson(stringBuilder.toString(), type);
            handleImportFromFile(returnList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/*");
        startActivityForResult(intent, PICK_FILE);
    }

    public boolean verifyCalendarPermissions(Activity activity) {
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_CALENDAR);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            new AlertDialog.Builder(this, R.style.DefaultAlertDialogTheme)
                    .setTitle(R.string.permissions)
                    .setIcon(R.drawable.ic_info)
                    .setMessage(R.string.permissionInfoCalendar)
                    .setPositiveButton(R.string.ok, (dialog, which) -> ActivityCompat.requestPermissions(activity, PERMISSIONS_CALENDAR, REQUEST_CALENDAR))
                    .show();
            return false;
        } else return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (checkingForCalendarForScheduling) {
                handleSchedule();
            }
            checkingForCalendarForScheduling = false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FILE_SHARING) {
            if (directoryFile.delete()) {
                Log.d(TAG, "onActivityResult: shared file deleted");
                directoryFile = null;
            } else Log.d(TAG, "onActivityResult: failed to delete");
        }

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {
                Workout newWorkout = (Workout) data.getSerializableExtra(Helpers.NEW_WORKOUT_KEY);
                Utils.getInstance(this).addToAllWorkouts(newWorkout);
            } else if (requestCode == CREATE_FILE) {
                saveToFile(data.getData());
            } else if (requestCode == PICK_FILE) {
                loadFromFile(data.getData());
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, R.string.pressBackToExit, Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
    }

    @Override
    protected void onPause() {
        Utils.getInstance(this).updateWorkouts(workouts);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        workouts.clear();
        workouts.addAll(Utils.getInstance(this).getAllWorkouts());
        adapter.notifyDataSetChanged();
        doubleBackToExitPressedOnce = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        authMenuItem = menu.findItem(R.id.auth);
        if (FirebaseAuth.getInstance().getCurrentUser() != null)
            authMenuItem.setTitle(R.string.signOut);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.submenuHideShow:
                areExercisesShown = !areExercisesShown;
                Utils.getInstance(this).setAreExercisesShown(areExercisesShown);
                finish();
                startActivity(getIntent());
                return true;
            case R.id.submenuGrouping:
                groupingEnabled = !groupingEnabled;
                Utils.getInstance(this).setGroupingEnabled(groupingEnabled);
                finish();
                startActivity(getIntent());
                return true;
            case R.id.logInCalendar:
                if (verifyCalendarPermissions(MainActivity.this)) {
                    if (loggingEnabled) {
                        new AlertDialog.Builder(this, R.style.DefaultAlertDialogTheme)
                                .setTitle(R.string.disableLogging)
                                .setIcon(R.drawable.ic_calendar)
                                .setMessage(R.string.disableLoggingMsg)
                                .setPositiveButton(R.string.yes, (dialog, which) -> {
                                    loggingEnabled = false;
                                    Utils.getInstance(MainActivity.this).setLoggingEnabled(loggingEnabled);
                                })
                                .setNegativeButton(R.string.no, null)
                                .show();
                    } else {
                        new AlertDialog.Builder(this, R.style.DefaultAlertDialogTheme)
                                .setTitle(R.string.enableLogging)
                                .setIcon(R.drawable.ic_calendar)
                                .setMessage(R.string.enableLoggingMsg)
                                .setPositiveButton(R.string.yes, (dialog, which) -> handleCalendarSelection())
                                .setNegativeButton(R.string.no, null)
                                .show();
                    }
                }
                return true;
            case R.id.submenuShare:
                handlePickToExport(MODE_SHARE);
                return true;
            case R.id.importFromFile:
                openFile();
                return true;
            case R.id.exportToFile:
                handlePickToExport(MODE_SAVE_TO_FILE);
                return true;
            case R.id.loadFromCloud:
                if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                    handleAuth();
                    tryingToLoad = true;
                } else handleLoadFromCloud(MODE_ADD);
                return true;
            case R.id.saveToCloud:
                if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                    handleAuth();
                    tryingToSave = true;
                } else handlePickToExport(MODE_SAVE_TO_CLOUD);
                return true;
            case R.id.deleteFromCloud:
                if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                    handleAuth();
                    tryingToDelete = true;
                } else handleLoadFromCloud(MODE_DELETE);
                return true;
            case R.id.auth:
                handleAuth();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void handlePickToExport(String mode) {
        boolean[] areChecked = new boolean[workouts.size()];
        String[] workoutNames = new String[workouts.size()];
        for (int i = 0; i < workouts.size(); i++) {
            workoutNames[i] = workouts.get(i).getName();
            areChecked[i] = false;
        }
        new AlertDialog.Builder(this, R.style.DefaultAlertDialogTheme)
                .setTitle((mode.equals("MODE_SHARE")) ? R.string.selectWorkoutsToShare : R.string.selectWorkoutsToSave)
                .setIcon((mode.equals("MODE_SHARE")) ? R.drawable.ic_share : R.drawable.ic_save)
                .setMultiChoiceItems(workoutNames, areChecked, (dialog, which, isChecked) -> areChecked[which] = isChecked)
                .setPositiveButton((mode.equals("MODE_SHARE")) ? R.string.share : R.string.save, (dialog, which) -> {
                    workoutsToSave.clear();
                    int size = workouts.size();
                    for (int i = 0; i < size; i++) {
                        if (areChecked[i]) {
                            workoutsToSave.add(workouts.get(i));
                        }
                    }
                    if (workoutsToSave.isEmpty()) {
                        Toast.makeText(MainActivity.this, R.string.noWorkoutsSelected, Toast.LENGTH_SHORT).show();
                    } else {
                        switch (mode) {
                            case MODE_SHARE:
                                handleShare(workoutsToSave);
                                break;
                            case MODE_SAVE_TO_FILE:
                                handleSaveToFile(workoutsToSave);
                                break;
                            case MODE_SAVE_TO_CLOUD:
                                handleSaveToCloud(workoutsToSave);
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void handleSaveToFile(ArrayList<Workout> workoutsToSave) {
        createFile();
    }

    private void handleSaveToCloud(ArrayList<Workout> workouts) {
        if (user != null) {
            CollectionReference usersRef = db.collection("users");
            Query query = usersRef.whereEqualTo("id", user.getUid());
            query.get().addOnCompleteListener(task -> {
                if (task.getResult().size() == 0) {
                    addNewUser(workouts);
                } else {
                    userRef = task.getResult().getDocuments().get(0).getReference();
                    addWorkoutsToUser(workouts);
                }
            });
        }
    }

    private void addNewUser(ArrayList<Workout> workouts) {
        HashMap<String, String> newUser = new HashMap<>();
        newUser.put("id", user.getUid());
        db.collection("users").add(newUser)
                .addOnSuccessListener(documentReference -> {
                    userRef = documentReference;
                    addWorkoutsToUser(workouts);
                    Log.d(TAG, "onSuccess: " + documentReference.getId());
                })
                .addOnFailureListener(e -> Log.d(TAG, "onFailure: " + e));
    }

    private void addWorkoutsToUser(ArrayList<Workout> workouts) {
        for (Workout w : workouts) {
            if (w.getCloudID().equals("")) {
                insertWorkoutToCloud(w);
            } else {
                userRef.collection("workouts").whereEqualTo("cloudId", w.getCloudID()).get().addOnCompleteListener(task -> {
                    if (task.getResult().isEmpty()) {
                        insertWorkoutToCloud(w);
                    } else {
                        updateWorkoutInCloud(task.getResult().getDocuments().get(0).getReference(), w);
                    }
                });
            }
        }
    }

    private void updateWorkoutInCloud(DocumentReference workoutRef, Workout w) {
        workoutRef.update(Workout.workoutToHashmap(w))
                .addOnSuccessListener(unused -> Toast.makeText(MainActivity.this, w.getName() + " - " + getResources().getString(R.string.updated), Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(MainActivity.this, w.getName() + " - " + getResources().getString(R.string.unableToUpdate), Toast.LENGTH_SHORT).show());
    }

    private void insertWorkoutToCloud(Workout w) {
        userRef.collection("workouts").add(Workout.workoutToHashmap(w))
                .addOnSuccessListener(workoutRef -> {
                    Toast.makeText(MainActivity.this, w.getName() + " - " + getResources().getString(R.string.saved), Toast.LENGTH_SHORT).show();
                    workoutRef.update("cloudId", workoutRef.getId());
                    w.setCloudID(workoutRef.getId());
                })
                .addOnFailureListener(e -> Toast.makeText(MainActivity.this, w.getName() + " - " + getResources().getString(R.string.unableToSave), Toast.LENGTH_SHORT).show());
    }

    private void handleLoadFromCloud(String mode) {
        Toast.makeText(this, "load from cloud", Toast.LENGTH_SHORT).show();
    }

    private void handleAuth() {
        if (user == null) {
            List<AuthUI.IdpConfig> providers = Arrays.asList(
                    new AuthUI.IdpConfig.EmailBuilder().build(),
                    new AuthUI.IdpConfig.PhoneBuilder().build(),
                    new AuthUI.IdpConfig.GoogleBuilder().build());

            Intent signInIntent = AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(providers)
                    .build();
            signInLauncher.launch(signInIntent);
        } else {
            AuthUI.getInstance()
                    .signOut(this)
                    .addOnCompleteListener(task -> {
                        Toast.makeText(MainActivity.this, getResources().getString(R.string.signedOut), Toast.LENGTH_SHORT).show();
                        user = null;
                    });
            authMenuItem.setTitle(getResources().getString(R.string.signIn));
        }
    }

    private void handleShare(ArrayList<Workout> workoutsToSave) {
        boolean[] areChecked = new boolean[workouts.size()];
        String[] workoutNames = new String[workouts.size()];
        for (int i = 0; i < workouts.size(); i++) {
            workoutNames[i] = workouts.get(i).getName();
            areChecked[i] = false;
        }
        new AlertDialog.Builder(this, R.style.DefaultAlertDialogTheme)
                .setTitle(R.string.selectWorkoutsToShare)
                .setIcon(R.drawable.ic_share)
                .setMultiChoiceItems(workoutNames, areChecked, (dialog, which, isChecked) -> areChecked[which] = isChecked)
                .setPositiveButton(R.string.share, (dialog, which) -> {
                    ArrayList<Workout> exportedWorkouts = new ArrayList<>();
                    int removed = 0;
                    int size = workouts.size();
                    for (int i = 0; i < size; i++) {
                        if (areChecked[i]) {
                            exportedWorkouts.add(workouts.get(i - removed));
                        }
                    }
                    if (exportedWorkouts.isEmpty()) {
                        Toast.makeText(MainActivity.this, R.string.noWorkoutsSelected, Toast.LENGTH_SHORT).show();
                    } else {
                        File directory = new File(getFilesDir().toString());
                        if (!directory.exists()) {
                            directory.mkdirs();
                        }
                        directoryFile = new File(directory + "/gbShare.txt");
                        try {
                            ShareCompat.IntentBuilder shareIntent = ShareCompat.IntentBuilder.from(this).setType("text/plain");
                            Uri uri = FileProvider.getUriForFile(this, "com.example.gymbuddy.fileprovider", directoryFile);
                            ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(uri, "w");
                            FileOutputStream fileOutputStream = new FileOutputStream(pfd.getFileDescriptor());
                            fileOutputStream.write((gson.toJson(exportedWorkouts)).getBytes());
                            fileOutputStream.close();
                            shareIntent.addStream(uri);
                            Intent intent = shareIntent.getIntent();
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            startActivityForResult(Intent.createChooser(intent, getString(R.string.shareWorkoutsVia)), FILE_SHARING);
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.d(TAG, "saveToFile: " + e);
                        }

                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void handleImportFromFile(ArrayList<Workout> workoutsToLoad) {
        if (workoutsToLoad == null || workoutsToLoad.isEmpty()) {
            Toast.makeText(MainActivity.this, R.string.noWorkoutsToLoad, Toast.LENGTH_SHORT).show();
        } else {
            boolean[] areChecked = new boolean[workoutsToLoad.size()];
            String[] workoutNames = new String[workoutsToLoad.size()];
            for (int i = 0; i < workoutsToLoad.size(); i++) {
                workoutNames[i] = workoutsToLoad.get(i).getName();
                areChecked[i] = false;
            }
            new AlertDialog.Builder(MainActivity.this, R.style.DefaultAlertDialogTheme)
                    .setTitle(R.string.selectWorkoutsToLoad)
                    .setIcon(R.drawable.ic_load)
                    .setMultiChoiceItems(workoutNames, areChecked, (dialog12, which12, isChecked) -> areChecked[which12] = isChecked)
                    .setPositiveButton(R.string.load, (dialog1, which1) -> {
                        int removed = 0;
                        int size = workoutsToLoad.size();
                        for (int i = 0; i < size; i++) {
                            if (areChecked[i]) {
                                workouts.add(workoutsToLoad.get(i - removed));
                                Utils.getInstance(MainActivity.this).addToAllWorkouts(workoutsToLoad.get(i - removed));
                                workoutsToLoad.remove(i - removed);
                                removed++;
                            }
                        }
                        adapter.setWorkouts(workouts);
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        }
    }

//    private void handleExportToFile() {
//        boolean[] areChecked = new boolean[workouts.size()];
//        String[] workoutNames = new String[workouts.size()];
//        for (int i = 0; i < workouts.size(); i++) {
//            workoutNames[i] = workouts.get(i).getName();
//            areChecked[i] = false;
//        }
//        new AlertDialog.Builder(this, R.style.DefaultAlertDialogTheme)
//                .setTitle(R.string.selectWorkoutsToSave)
//                .setIcon(R.drawable.ic_save)
//                .setMultiChoiceItems(workoutNames, areChecked, (dialog, which, isChecked) -> areChecked[which] = isChecked)
//                .setPositiveButton(R.string.next, (dialog, which) -> {
//                    workoutsToSave.clear();
//                    int removed = 0;
//                    int size = workouts.size();
//                    for (int i = 0; i < size; i++) {
//                        if (areChecked[i]) {
//                            workoutsToSave.add(workouts.get(i - removed));
//                        }
//                    }
//                    if (workoutsToSave.isEmpty()) {
//                        Toast.makeText(MainActivity.this, R.string.noWorkoutsSelected, Toast.LENGTH_SHORT).show();
//                    } else {
//                        createFile();
//                    }
//                })
//                .setNegativeButton(R.string.cancel, null)
//                .show();
//    }

//    private void handleExportToArchive() {
//        boolean[] areChecked = new boolean[workouts.size()];
//        String[] workoutNames = new String[workouts.size()];
//        for (int i = 0; i < workouts.size(); i++) {
//            workoutNames[i] = workouts.get(i).getName();
//            areChecked[i] = false;
//        }
//        new AlertDialog.Builder(this, R.style.DefaultAlertDialogTheme)
//                .setTitle(R.string.savingToAppMemory)
//                .setMessage(R.string.savingToAppMemoryInfo)
//                .setIcon(R.drawable.ic_info)
//                .setPositiveButton(R.string.ok, (dialog, which) -> new AlertDialog.Builder(MainActivity.this, R.style.DefaultAlertDialogTheme)
//                        .setTitle(R.string.selectWorkoutsToSave)
//                        .setIcon(R.drawable.ic_save)
//                        .setMultiChoiceItems(workoutNames, areChecked, (dialog12, which12, isChecked) -> areChecked[which12] = isChecked)
//                        .setPositiveButton(R.string.save, (dialog1, which1) -> {
//                            ArrayList<Workout> exportedWorkouts = Utils.getInstance(MainActivity.this).getExportedWorkouts();
//                            if (exportedWorkouts == null) {
//                                exportedWorkouts = new ArrayList<>();
//                            }
//                            int removed = 0;
//                            int size = workouts.size();
//                            for (int i = 0; i < size; i++) {
//
//                                if (areChecked[i]) {
//                                    exportedWorkouts.add(workouts.get(i - removed));
//                                    workouts.remove(i - removed);
//                                    removed++;
//
//                                }
//                            }
//                            adapter.setWorkouts(workouts);
//                            Utils.getInstance(MainActivity.this).updateWorkouts(workouts);
//                            Utils.getInstance(MainActivity.this).setExportedWorkouts(exportedWorkouts);
//                        })
//                        .setNegativeButton(R.string.cancel, null)
//                        .show()).show();
//
//    }

//    private void handleImportFromArchive() {
//        ArrayList<Workout> workoutsImport = Utils.getInstance(MainActivity.this).getExportedWorkouts();
//        if (workoutsImport == null || workoutsImport.isEmpty()) {
//            Toast.makeText(this, R.string.noWorkoutsToLoad, Toast.LENGTH_SHORT).show();
//        } else {
//            boolean[] areChecked = new boolean[workoutsImport.size()];
//            String[] workoutNames = new String[workoutsImport.size()];
//            for (int i = 0; i < workoutsImport.size(); i++) {
//                workoutNames[i] = workoutsImport.get(i).getName();
//                areChecked[i] = false;
//            }
//            new AlertDialog.Builder(this, R.style.DefaultAlertDialogTheme)
//                    .setTitle(R.string.selectWorkoutsToLoad)
//                    .setIcon(R.drawable.ic_load)
//                    .setMultiChoiceItems(workoutNames, areChecked, (dialog, which, isChecked) -> areChecked[which] = isChecked)
//                    .setPositiveButton(R.string.load, (dialog, which) -> {
//                        int removed = 0;
//                        int size = workoutsImport.size();
////                        workouts = Utils.getInstance(MainActivity.this).getAllWorkouts();
//                        for (int i = 0; i < size; i++) {
//                            if (areChecked[i]) {
//                                workouts.add(workoutsImport.get(i - removed));
//                                workoutsImport.remove(i - removed);
//                                removed++;
//                            }
//                        }
//                        adapter.setWorkouts(workouts);
//                        Utils.getInstance(MainActivity.this).updateWorkouts(workouts);
//                        Utils.getInstance(MainActivity.this).setExportedWorkouts(workoutsImport);
//                    })
//                    .setNegativeButton(R.string.cancel, null)
//                    .show();
//        }
//    }

    private void handleCalendarSelection() {
        getGmailCalendarIds(this);
        String[] accountNames;
        int checkedItem = 0;
        if (calendarName.isEmpty()) {
            accountNames = new String[]{getResources().getString(R.string.def)};
        } else {
            accountNames = new String[calendarName.size() + 1];
            accountNames[0] = getResources().getString(R.string.def);
            for (int i = 0; i < calendarName.size(); i++) {
                accountNames[i + 1] = calendarName.get(i);
                if (calendarId.get(i).equals(Utils.getInstance(MainActivity.this).getCalendarID())) {
                    checkedItem = i + 1;
                }
            }
        }

        new AlertDialog.Builder(this, R.style.DefaultAlertDialogTheme)
                .setTitle(R.string.selectCalendar)
                .setIcon(R.drawable.ic_calendar)
                .setSingleChoiceItems(accountNames, checkedItem, (dialog, which) -> selected = which)
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    loggingEnabled = true;
                    Utils.getInstance(MainActivity.this).setLoggingEnabled(true);
                    if (selected == 0) {
                        Utils.getInstance(MainActivity.this).setCalendarID("");
                    } else {
                        Utils.getInstance(MainActivity.this).setCalendarID(calendarId.get(selected - 1));
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();

    }

    private void getGmailCalendarIds(Context c) {
        calendarId = new ArrayList<>();
        calendarName = new ArrayList<>();
        String[] projection = {"_id", "calendar_displayName"};
        Uri calendars;
        calendars = Uri.parse("content://com.android.calendar/calendars");
        ContentResolver contentResolver = c.getContentResolver();
        Cursor managedCursor = contentResolver.query(calendars,
                projection, null, null, null);
        if (managedCursor.moveToFirst()) {
            String calName;
            String calID;
            int nameCol = managedCursor.getColumnIndex(projection[1]);
            int idCol = managedCursor.getColumnIndex(projection[0]);
            do {
                calName = managedCursor.getString(nameCol);
                calID = managedCursor.getString(idCol);
                if (calName.contains("@gmail")) {
                    calendarId.add(calID);
                    calendarName.add(calName);
                }
            } while (managedCursor.moveToNext());
            managedCursor.close();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        areExercisesShown = Utils.getInstance(this).isAreExercisesShown();
        setTheme(R.style.Theme_GymBuddy);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);


        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            buildAndShowSignedInMessage(user);
        }

        Helpers.setupActionBar(getString(R.string.workouts), "", getSupportActionBar(), this);

        Utils.getInstance(this).setLastAdShown(0);
        Helpers.showRatingUserInterface(this);
        btnAddWorkout = findViewById(R.id.btnAddWorkout);
        workoutsRV = findViewById(R.id.workoutsRV);
        ExtendedFloatingActionButton btnSchedule = findViewById(R.id.btnSchedule);
        mainAdTemplate = findViewById(R.id.mainAdTemplate);

        MobileAds.initialize(this, initializationStatus -> {
            Map<String, AdapterStatus> statusMap = initializationStatus.getAdapterStatusMap();
            for (String adapterClass : statusMap.keySet()) {
                AdapterStatus status = statusMap.get(adapterClass);
                Log.d("GB", String.format(
                        "Adapter name: %s, Description: %s, Latency: %d",
                        adapterClass, status != null ? status.getDescription() : "null", status != null ? status.getLatency() : 0));
            }
            Helpers.handleNativeAds(mainAdTemplate, this, Helpers.AD_ID_MAIN_NATIVE, null);
        });


//        Helpers.handleAds(mainAdContainer, this, Helpers.AD_ID_MAIN);

//        receiver=new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                Toast.makeText(MainActivity.this, "got a notification", Toast.LENGTH_SHORT).show();
//            }
//        };

        workouts = Utils.getInstance(this).getAllWorkouts();

        if (!workouts.isEmpty()) {
            if (workouts.get(0).getCloudID() == null) {
                for (Workout w : workouts) {
                    try {
                        w.setCloudID("");
                        w.setTimestamp(null);
                        Log.d(TAG, "setWorkoutCloudIdSuccessful: " + w.getName());
                        if (!w.getExercises().isEmpty()) {
                            if (w.getExercises().get(0).getCloudID() == null) {
                                for (Exercise e : w.getExercises()) {
                                    try {
                                        e.setCloudID("");
                                        e.setTimestamp(null);
                                        Log.d(TAG, "setExerciseCloudIdSuccessful: " + e.getName());
                                    } catch (Exception exception) {
                                        Log.d(TAG, "setExerciseCloudIdFailed: " + e.getName() + " " + e);
                                    }
                                }
                            }
                        }

                    } catch (Exception e) {
                        Log.d(TAG, "setWorkoutCloudIdFailed: " + w.getName() + " " + e);
                    }

                }
                Utils.getInstance(this).updateWorkouts(workouts);
            }
            Log.d(TAG, "setWorkoutCloudId: all good");
        }

        loggingEnabled = Utils.getInstance(this).isLoggingEnabled();
        groupingEnabled = Utils.getInstance(this).isGroupingEnabled();

        adapter.setGroupingEnabled(groupingEnabled);
        adapter.setWorkouts(workouts);
        adapter.setOnItemClickListener(this);

        workoutsRV.setAdapter(adapter);

        workoutsRV.setLayoutManager(new LinearLayoutManager(this));

        workoutsRV.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    if (btnAddWorkout.isShown()) {
                        btnAddWorkout.setVisibility(GONE);
                    }
                } else if (dy < 0) {
                    if (!btnAddWorkout.isShown()) {
                        btnAddWorkout.show();
                    }
                }
            }
        });

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(workoutsRV);

        btnSchedule.setOnClickListener(v -> {
            checkingForCalendarForScheduling = true;
            if (verifyCalendarPermissions(MainActivity.this)) {
                handleSchedule();
            }
        });

        btnAddWorkout.setOnClickListener(v -> {
            int LAUNCH_SECOND_ACTIVITY = 1;
            Intent i = new Intent(MainActivity.this, AddWorkoutActivity.class);
            startActivityForResult(i, LAUNCH_SECOND_ACTIVITY);
        });
    }

    private void buildAndShowSignedInMessage(FirebaseUser user) {
        StringBuilder text = new StringBuilder();
        text.append(getResources().getString(R.string.signedInAs));
        text.append(" ");
        if (user.getPhoneNumber() != null && !user.getPhoneNumber().isEmpty()) text.append(user.getPhoneNumber());
        if (user.getEmail() != null && !user.getEmail().isEmpty()) text.append(user.getEmail());
        Toast.makeText(this, text.toString(), Toast.LENGTH_SHORT).show();
    }

    private void handleSchedule() {
        Intent intent = new Intent(this, ScheduleActivity.class);
        startActivity(intent);
    }


    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN |
            ItemTouchHelper.START | ItemTouchHelper.END, 0) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            int fromPosition = viewHolder.getAdapterPosition();
            int toPosition = target.getAdapterPosition();
//            workouts = Utils.getInstance(MainActivity.this).getAllWorkouts();
            Collections.swap(workouts, fromPosition, toPosition);
            adapter.notifyItemMoved(fromPosition, toPosition);
            Utils.getInstance(MainActivity.this).updateWorkouts(workouts);
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

        }

        @Override
        public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            super.clearView(recyclerView, viewHolder);
            refreshGrouping();
        }
    };

    private void refreshGrouping() {
        if (groupingEnabled && workouts.size() > 2) {
            TextView header = workoutsRV.findViewHolderForAdapterPosition(0).itemView.findViewById(R.id.txtTypeHeader);
            if (!header.isShown()) adapter.notifyItemChanged(0);
            for (int i = 0; i < workouts.size() - 1; i++) {
                header = workoutsRV.findViewHolderForAdapterPosition(i + 1).itemView.findViewById(R.id.txtTypeHeader);
                if (Helpers.workoutTypeComparator(workouts.get(i).getType(), workouts.get(i + 1).getType()) && header.isShown())
                    adapter.notifyItemChanged(i + 1);
                else if (!Helpers.workoutTypeComparator(workouts.get(i).getType(), workouts.get(i + 1).getType()) && !header.isShown())
                    adapter.notifyItemChanged(i + 1);
            }
        }
    }

    @Override
    public void onItemClick(int positionRV) {
        refreshGrouping();
    }
}

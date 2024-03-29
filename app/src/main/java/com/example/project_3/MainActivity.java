package com.example.project_3;

import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 101;
    private Intent AttendeeIntent;
    private Intent AdminIntent;
    private Intent OrganizerIntent;
    private String profileId;
    private User user;
    private FirebaseFirestore db;
    private CollectionReference profilesRef;
    private Map<String, Object> profileDocDetails;
    private CollectionReference tokenRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_selector);

        // Check if the app has notification permission
        if (!hasNotificationPermission()) {
            // Request notification permission
            requestNotificationPermission();
        }

        AttendeeIntent = new Intent(this, AttendeeActivity.class);
        OrganizerIntent = new Intent(this, OrganizerActivity.class);
        AdminIntent = new Intent(this, AdminActivity.class);
//
        setContentView(R.layout.account_selector);
        Button Attendee_button = findViewById(R.id.Attendee_button);
        Button Organizer_button = findViewById(R.id.Organizer_button);
        Button Admin_button = findViewById(R.id.Admin_button);

        Attendee_button.setOnClickListener(V -> {
            AttendeeIntent = new Intent(this, AttendeeActivity.class);
            startActivity(AttendeeIntent);
        });
        Organizer_button.setOnClickListener(V -> {
            OrganizerIntent = new Intent(this, OrganizerActivity.class);
            startActivity(OrganizerIntent);
        });
        Admin_button.setOnClickListener(V -> {
            AdminIntent = new Intent(this, AdminActivity.class);
            startActivity(AdminIntent);
        });
        FirebaseMessaging.getInstance().subscribeToTopic("all");
        getFcmToken();

    }
    void getFcmToken(){
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener((OnCompleteListener<String>) task -> {
            if(task.isSuccessful()){
                String token = task.getResult();
                Log.i("My token", token);
                saveTokenToFirestore(token);
            }
        });
    }

    void saveTokenToFirestore(String token) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference tokenRef = db.collection("Tokens");

        // Check if document exists and retrieve its data
        tokenRef.document("tokenz").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // Document exists, retrieve token list and update
                    List<String> tokensList = (List<String>) document.get("TokenList");
                    if (tokensList != null && !tokensList.isEmpty()) {
                        // Add the new token to the end of the list
                        tokensList.add(token);
                    } else {
                        // Token list is empty, create a new list with the token
                        tokensList = new ArrayList<>();
                        tokensList.add(token);
                    }
                    // Update Firestore with the modified token list
                    updateTokenListInFirestore(tokenRef, tokensList);
                } else {
                    // Document doesn't exist, create a new one with the token
                    List<String> tokensList = new ArrayList<>();
                    tokensList.add(token);
                    // Update Firestore with the new token list
                    updateTokenListInFirestore(tokenRef, tokensList);
                }
            } else {
                Log.e("Firestore", "Error getting document", task.getException());
            }
        });
    }

    void updateTokenListInFirestore(CollectionReference tokenRef, List<String> tokensList) {
        // Create a HashMap to store the token list
        HashMap<String, Object> data = new HashMap<>();
        data.put("TokenList", tokensList);

        // Update Firestore with the token list
        tokenRef.document("tokenz").set(data)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Token list updated in Firestore"))
                .addOnFailureListener(e -> Log.e("Firestore", "Error updating token list in Firestore", e));
    }

    // Method to check if the app has notification permission
    private boolean hasNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            return notificationManager != null && notificationManager.getNotificationChannel(String.valueOf(NotificationManager.IMPORTANCE_DEFAULT)) != null;
        }
        return true; // On versions lower than Oreo, no permission is required.
    }
    // Method to request notification permission
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Request permission for notification
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                    NOTIFICATION_PERMISSION_REQUEST_CODE);
        } else {
            // No permission required for pre-Oreo devices
            Log.d("MainActivity", "Notification permission not required for pre-Oreo devices.");
        }
    }

    // Override onRequestPermissionsResult to handle permission request result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Notification permission granted
                Log.d("MainActivity", "Notification permission granted.");
            } else {
                // Notification permission denied
                Log.d("MainActivity", "Notification permission denied.");
            }
        }
    }
    // Rest of your MainActivity class...
}

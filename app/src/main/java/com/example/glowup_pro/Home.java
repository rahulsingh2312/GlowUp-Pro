package com.example.glowup_pro;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.common.reflect.TypeToken;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


public class Home extends AppCompatActivity {

    private LinearLayout customAlertContainer;
    private EditText customAlertInput;
    private FirebaseFirestore firestore;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        firestore = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        customAlertContainer = findViewById(R.id.customAlertContainer);
        customAlertInput = findViewById(R.id.customAlertInput);

        setupSubscribeCard(R.id.waterAlertCheckbox, "Water Alerts");
        setupSubscribeCard(R.id.sunscreenAlertCheckbox, "Sunscreen Alerts");
        setupSubscribeCard(R.id.medicationAlertCheckbox, "Vitamin E Alerts");
        setupSubscribeCard(R.id.skinCareAlert2Checkbox, "Moisturizer Alert");
        setupSubscribeCard(R.id.skinCareAlert4Checkbox, "Retinol Alerts");

        Button addCustomAlertButton = findViewById(R.id.addCustomAlertButton);
        addCustomAlertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addCustomAlert();
            }
        });

        setupLogoutButton();
        setupHealthWrapButton();
        // Load user preferences from Firestore
        loadUserPreferences();
    }

    private void setupSubscribeCard(int checkboxId, final String subscriptionType) {
        CheckBox subscribeCheckBox = findViewById(checkboxId);
        subscribeCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isChecked = ((CheckBox) view).isChecked();
                // Update user preferences on checkbox click
                updateSubscriptionPreference(subscriptionType, isChecked);

                if (!isChecked) {
                    // If the checkbox is unchecked, remove the field from the Firestore document
                    removeSubscriptionPreference(subscriptionType);
                }
            }
        });
    }

    private void removeSubscriptionPreference(String subscriptionType) {
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DocumentReference userRef = firestore.collection("users").document(userId);

            // Remove the corresponding field from the Firestore document
            userRef.update(subscriptionType, null)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d("Debug", "Subscription preference removed: " + subscriptionType);
                        } else {
                            // Handle the error
                            Log.w("Debug", "Error removing subscription preference", task.getException());
                        }
                    });
        }
    }

    private void updateSubscriptionPreference(String subscriptionType, boolean isChecked) {
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DocumentReference userRef = firestore.collection("users").document(userId);

            // Update the corresponding field in the Firestore document
            userRef.update(subscriptionType, isChecked)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            showSubscriptionAlert(subscriptionType, isChecked);
                        } else {
                            // Handle the error
                        }
                    });
        }
    }

    private void loadUserPreferences() {
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DocumentReference userRef = firestore.collection("users").document(userId);

            // Retrieve user preferences from Firestore
            userRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    DocumentSnapshot document = task.getResult();

                    // Display general user preferences
                    displayUserPreferences(document);

                    // Display custom alerts
//                    displayCustomAlerts(document);
                }
            });
        }
    }


    private void displayUserPreferences(DocumentSnapshot document) {
        if (document.exists()) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> {
                List<String> subscriptions = new ArrayList<>();
                subscriptions.add("Water Alerts");
                subscriptions.add("Sunscreen Alerts");
                subscriptions.add("Vitamin E Alerts");
                subscriptions.add("Moisturizer Alert");
                subscriptions.add("Retinol Alerts");

                for (String subscription : subscriptions) {
                    Boolean subscriptionStatus = document.getBoolean(subscription);
                    CheckBox checkBox = findViewById(getCheckboxId(subscription));
                    if (subscriptionStatus != null && subscriptionStatus) {
                        checkBox.setChecked(true);
                    }
                }

                // Display custom alerts
//                List<String> favFoods = (List<String>) document.get("favFoods");
                List<String> customAlertsList =(List<String>) document.get("customAlerts");
                if (customAlertsList != null) {
                    for (String customAlertText : customAlertsList) {

                        displayCustomAlert(customAlertText);
                    }
                }
            });
        }
    }
    private void displayCustomAlerts(DocumentSnapshot document) {
        // Retrieve the custom alerts from the document
        // Display custom alerts
//        List<String> favFoods = (List<String>) document.get("favFoods");
        List<String> customAlertsList = (List<String>) document.get("customAlerts");

        if (customAlertsList != null) {
            for (String customAlertText : customAlertsList) {
                displayCustomAlert(customAlertText);
            }
        }

    }


    private void displayCustomAlert(String customAlertText) {
        // Create a new CardView for the custom alert
        CardView customAlertCard = new CardView(Home.this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(8, 8, 8, 8);
        customAlertCard.setLayoutParams(layoutParams);

        // Create a new LinearLayout to hold the text and delete button
        LinearLayout alertLayout = new LinearLayout(Home.this);
        alertLayout.setOrientation(LinearLayout.HORIZONTAL);

        // Create a new TextView for the custom alert text
        TextView customAlertTextView = new TextView(Home.this);
        customAlertTextView.setText(customAlertText);
        customAlertTextView.setTextSize(20);
        customAlertTextView.setTextColor(getResources().getColor(android.R.color.black));
        customAlertTextView.setGravity(Gravity.CENTER);
        customAlertTextView.setPadding(6, 16, 36, 16);

        // Create a delete button (cross emoji)
        TextView deleteButton = new TextView(Home.this);
        deleteButton.setText("❌");
        deleteButton.setTextSize(20);
        deleteButton.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        LinearLayout.LayoutParams buttonLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        buttonLayoutParams.gravity = Gravity.END; // Align to the right
        deleteButton.setLayoutParams(buttonLayoutParams);
        deleteButton.setOnClickListener(view -> {
            // Call the method to delete the custom alert
            deleteCustomAlert(customAlertText);
            // Remove the CardView from the customAlertContainer
            customAlertContainer.removeView(customAlertCard);
        });

        // Add the TextView and delete button to the LinearLayout

        alertLayout.addView(customAlertTextView);
        alertLayout.addView(deleteButton);

        // Add the LinearLayout to the CardView
        customAlertCard.addView(alertLayout);

        // Add the CardView to the customAlertContainer
        customAlertContainer.addView(customAlertCard);
    }

    private void deleteCustomAlert(String customAlertText) {
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DocumentReference userRef = firestore.collection("users").document(userId);

            // Remove the custom alert from the Firestore document
            userRef.update("customAlerts", FieldValue.arrayRemove(customAlertText))
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            showCustomAlertDeletedDialog(customAlertText);
                            Log.d("Debug", "Custom alert removed from database: " + customAlertText);
                        } else {
                            // Handle the error
                            Log.w("Debug", "Error removing custom alert from database", task.getException());
                        }
                    });
        }
    }

    private void showCustomAlertDeletedDialog(String customAlertText) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Custom Alert Deleted");
        builder.setMessage("Alert for \"" + customAlertText + "\" deleted.");
        builder.setPositiveButton("OK", (dialogInterface, i) -> {
            // Do nothing or add any specific action
        });
        builder.create().show();
    }


    private int getCheckboxId(String subscriptionType) {
        switch (subscriptionType) {
            case "Water Alerts":
                return R.id.waterAlertCheckbox;
            case "Sunscreen Alerts":
                return R.id.sunscreenAlertCheckbox;
            case "Vitamin E Alerts":
                return R.id.medicationAlertCheckbox;
            case "Moisturizer Alert":
                return R.id.skinCareAlert2Checkbox;
            case "Retinol Alerts":
                return R.id.skinCareAlert4Checkbox;
            default:
                return 0;
        }
    }

    private void showSubscriptionAlert(String subscriptionType, boolean isChecked) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Subscription Alert");
        if (isChecked) {
            builder.setMessage("You subscribed to  " + subscriptionType);
        } else {
            builder.setMessage("You unsubscribed from " + subscriptionType);
        }
        builder.setPositiveButton("OK", (dialogInterface, i) -> {
            // Do nothing or add any specific action
        });
        builder.create().show();
    }

    private void addCustomAlert() {
        String customAlertText = customAlertInput.getText().toString().trim();

        if (!customAlertText.isEmpty()) {
            // Create a new CardView for the custom alert
            CardView customAlertCard = new CardView(Home.this);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            layoutParams.setMargins(8, 8, 8, 8);
            customAlertCard.setLayoutParams(layoutParams);

            // Create a new TextView for the custom alert text
            TextView customAlertTextView = new TextView(Home.this);
            customAlertTextView.setText(customAlertText);
            customAlertTextView.setTextSize(20);
            customAlertTextView.setTextColor(getResources().getColor(android.R.color.black));
            customAlertTextView.setGravity(Gravity.END);
            customAlertTextView.setPadding(16, 16, 16, 16);

            // Add the TextView to the CardView
            customAlertCard.addView(customAlertTextView);

            // Add the CardView to the customAlertContainer
            customAlertContainer.addView(customAlertCard);

            // Save the custom alert to the database
            saveCustomAlertToDatabase(customAlertText);

            // Optionally, you can clear the EditText after adding a custom alert
            customAlertInput.getText().clear();

            // Show a dialog box confirming the addition of the custom alert
            showCustomAlertAddedDialog(customAlertText);
        }
    }

    private void saveCustomAlertToDatabase(String customAlertText) {
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DocumentReference userRef = firestore.collection("users").document(userId);

            // Create a new field in the Firestore document to store the custom alert
            // You can choose a field name, for example, "customAlerts"
            // This assumes you have a field in your user document that stores a list of custom alerts
            userRef.update("customAlerts", FieldValue.arrayUnion(customAlertText))
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d("Debug", "Custom alert added to database: " + customAlertText);
                        } else {
                            // Handle the error
                            Log.e("Error", "Error adding custom alert to database", task.getException());
                        }
                    });
        }
    }

    private void showCustomAlertAddedDialog(String customAlertText) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Custom Alert Added");
        builder.setMessage("Alert for \"" + customAlertText + "\" added.");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Do nothing or add any specific action
            }
        });
        builder.create().show();
    }

    private void setupLogoutButton() {
        Button button = findViewById(R.id.logout);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void setupHealthWrapButton() {
        Button btnHealthWrap = findViewById(R.id.btnHealthWrap);
        btnHealthWrap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), HealthWrap.class);
                startActivity(intent);
                finish();
            }
        });
    }
}

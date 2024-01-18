package com.example.glowup_pro;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.List;

public class HealthWrap extends AppCompatActivity {

    private Button btnGoBack;
    private FirebaseFirestore firestore;
    private FirebaseUser currentUser;
    private List<String> subscriptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_wrap);

        btnGoBack = findViewById(R.id.goBack);
        firestore = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        subscriptions = new ArrayList<>();
        subscriptions.add("Water Alerts");
        subscriptions.add("Sunscreen Alerts");
        subscriptions.add("Vitamin E Alerts");
        subscriptions.add("Moisturizer Alert");
        subscriptions.add("Retinol Alerts");

        btnGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Home.class);
                startActivity(intent);
                finish();
            }
        });

        loadUserPreferences();
    }

    private void loadUserPreferences() {
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DocumentReference userRef = firestore.collection("users").document(userId);

            // Retrieve user preferences and health data from Firestore
            userRef.get().addOnCompleteListener(task -> {
                try {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        Log.d("HealthWrap", "Document data: " + document.getData());

                        // Retrieve user information
                        String userName = document.getString("name");
                        String ageStr = document.getString("age");
                        String weightStr = document.getString("weight");
                        String heightStr = document.getString("height");

                        // Convert age, weight, and height to appropriate types
                        int age = 0;
                        double weight = 0.0;
                        double height = 0.0;

                        if (ageStr != null && !ageStr.isEmpty()) {
                            age = Integer.parseInt(ageStr);
                        }

                        if (weightStr != null && !weightStr.isEmpty()) {
                            weight = Double.parseDouble(weightStr);
                        }

                        if (heightStr != null && !heightStr.isEmpty()) {
                            height = Double.parseDouble(heightStr);
                        }

                        // Retrieve selected alerts
                        List<String> selectedAlerts = new ArrayList<>();
                        for (String subscription : subscriptions) {
                            Boolean subscriptionStatus = document.getBoolean(subscription);
                            if (subscriptionStatus != null && subscriptionStatus) {
                                selectedAlerts.add(subscription);
                            }
                        }

                        // Display user information
                        displayUserInfo(userName, age, weight, height);

                        // Now you have user information and selected alerts
                        // You can use this data to create a health graph
                        createHealthGraph(age, weight, height, selectedAlerts);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private void displayUserInfo(String userName, int age, double weight, double height) {
        Log.d("HealthWrap", "UserName: " + userName);
        Log.d("HealthWrap", "Age: " + age);
        Log.d("HealthWrap", "Weight: " + weight);
        Log.d("HealthWrap", "Height: " + height);

        if (userName != null && !userName.isEmpty()) {
            TextView userNameTextView = findViewById(R.id.userNameTextView);
            userNameTextView.setText("Hello " + userName + "!");
        }

        // Display age, weight, and height
        TextView ageTextView = findViewById(R.id.ageTextView);
        ageTextView.setText("Age: " + age);

        TextView weightTextView = findViewById(R.id.weightTextView);
        weightTextView.setText("Weight: " + weight + " kg");

        TextView heightTextView = findViewById(R.id.heightTextView);
        heightTextView.setText("Height: " + height + " cm");

        // Calculate BMI
        double bmi = calculateBMI(weight, height);

        // Display BMI with different colors
        TextView bmiTextView = findViewById(R.id.bmiTextView);
        bmiTextView.setText("BMI: " + bmi);
        if (bmi < 18.5) {
            bmiTextView.setTextColor(getResources().getColor(R.color.bmi_underweight));
        } else if (bmi >= 18.5 && bmi < 25) {
            bmiTextView.setTextColor(getResources().getColor(R.color.bmi_normal));
        } else if (bmi >= 25 && bmi < 30) {
            bmiTextView.setTextColor(getResources().getColor(R.color.bmi_overweight));
        } else {
            bmiTextView.setTextColor(getResources().getColor(R.color.bmi_obese));
        }
        showFitnessComparison(bmi);
    }
    private void showFitnessComparison(double userBMI) {
        // You can adjust the reference BMI based on your criteria for average or healthy BMI
        double referenceBMI = 22.5;

        // Calculate the fitness score as a percentage
        double fitnessScore = ((referenceBMI - userBMI) / referenceBMI) * 100;

        // Display the fitness comparison message
        TextView fitnessComparisonTextView = findViewById(R.id.fitnessComparisonTextView);
        if (fitnessScore > 0) {
            // If fitter than average, set text color to green and bold
            fitnessComparisonTextView.setText(String.format("You are %.2f%% fitter than the average population!", fitnessScore));
            fitnessComparisonTextView.setTextColor(getResources().getColor(R.color.fitness_fitter)); // Define this color in your resources
            fitnessComparisonTextView.setTypeface(null, Typeface.BOLD);
        } else {
            // If on par or less fit than average, set text color to red and italic
            fitnessComparisonTextView.setText("You are on par with the average population!");
            fitnessComparisonTextView.setTextColor(getResources().getColor(R.color.fitness_on_par)); // Define this color in your resources
            fitnessComparisonTextView.setTypeface(null, Typeface.ITALIC);
        }
    }

    private double calculateBMI(double weight, double height) {
        // BMI formula: weight (kg) / (height (m) * height (m))
        return weight / ((height / 100) * (height / 100));
    }

    private void createHealthGraph(int age, double weight, double height, List<String> selectedAlerts) {
        GraphView graph = findViewById(R.id.healthGraph);

        // Calculate BMI
        double bmi = calculateBMI(weight, height);

        // Create series with BMI data
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(new DataPoint[]{
                new DataPoint(height, weight)
                // Add more data points as needed for different categories
        });

        // Add series to the graph
        graph.addSeries(series);

        // Customize graph appearance
        graph.setTitle("BMI Overview");
        graph.getGridLabelRenderer().setHorizontalAxisTitle("Height (cm)");
        graph.getGridLabelRenderer().setVerticalAxisTitle("Weight (kg)");

        // Customize the viewport to show different BMI categories
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(30);  // Adjust the minimum BMI value based on your requirements
        graph.getViewport().setMaxY(140);  // Adjust the maximum BMI value based on your requirements
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(140);  // Adjust the minimum BMI value based on your requirements
        graph.getViewport().setMaxX(200);
        // Highlight the user's BMI point
        series.setDrawDataPoints(true);
        series.setDataPointsRadius(10);
        series.setThickness(20);
        series.setDrawBackground(true);
        series.setTitle("YOU");
        series.setBackgroundColor(Color.argb(50, 255, 0, 0)); // Highlight color (red with transparency)

        // Refresh the graph
        graph.invalidate();
    }
}

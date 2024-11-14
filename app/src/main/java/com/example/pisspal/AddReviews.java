package com.example.pisspal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AddReviews extends AppCompatActivity {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ImageButton backButton;
    Button submitButton;
    Spinner spinnerLocation;
    RatingBar cleanliness, maintenance, accessibility;
    EditText otherComments;
    double cleanlinessRating, maintenanceRating, accessibilityRating, averageRating;
    String otherCommentsStr;

    List<String> locationNamesList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_reviews);
        backButton = findViewById(R.id.backButton3);
        submitButton = findViewById(R.id.button_submit);
        cleanliness = findViewById(R.id.ratingBar_Cleanliness);
        maintenance = findViewById(R.id.ratingBar_Maintenance);
        accessibility = findViewById(R.id.ratingBar_Accessibility);
        otherComments = findViewById(R.id.text_OtherComments);
        spinnerLocation = findViewById(R.id.spinner_Location);

        backButton.setOnClickListener(goBack);
        submitButton.setOnClickListener(onSubmit);

        fetchLocationNames();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinnerlayout, locationNamesList);
        adapter.setDropDownViewResource(R.layout.spinnerdroplistlayout);
        spinnerLocation.setAdapter(adapter);
    }

    // get locationname from db
    private void fetchLocationNames() {
        db.collection("ToiletLocations")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String locationName = documentSnapshot.getString("locationName");
                        if (locationName != null) {
                            locationNamesList.add(locationName);
                        }
                    }
                    ((ArrayAdapter<String>) spinnerLocation.getAdapter()).notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getApplicationContext(), "Error fetching location names: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private View.OnClickListener onSubmit = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            cleanlinessRating = cleanliness.getRating();
            maintenanceRating = maintenance.getRating();
            accessibilityRating = accessibility.getRating();
            averageRating = (cleanlinessRating + maintenanceRating + accessibilityRating) / 3;
            otherCommentsStr = otherComments.getText().toString().trim();
            String selectedLocation = spinnerLocation.getSelectedItem().toString();

            fetchLatLonAndSubmitReview(selectedLocation, averageRating, otherCommentsStr);
        }
    };

    private void fetchLatLonAndSubmitReview(String location, double averageRating, String otherCommentsStr) {
        DocumentReference docRef = db.collection("ToiletLocations").document(location);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    Double latitude = document.getDouble("latitude");
                    Double longitude = document.getDouble("longitude");
                    submitReviewData(location, averageRating, otherCommentsStr, latitude, longitude);
                }
            }
        });
    }

    // submit data to db
    private void submitReviewData(String location, double averageRating, String otherCommentsStr, double latitude, double longitude) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("locationName", location);
        hashMap.put("averageRating", averageRating);
        hashMap.put("otherCommentsStr", otherCommentsStr);
        hashMap.put("latitude", latitude);
        hashMap.put("longitude", longitude);

        db.collection("Reviews")
                .document(location)
                .set(hashMap)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getApplicationContext(), "Review saved successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), Reviews.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getApplicationContext(), "Error saving Review: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private View.OnClickListener goBack = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(AddReviews.this, Reviews.class);
            startActivity(intent);
            finish();
        }
    };
}

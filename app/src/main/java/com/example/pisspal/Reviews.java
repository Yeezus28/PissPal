package com.example.pisspal;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class Reviews extends AppCompatActivity {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    StorageReference storageRef = FirebaseStorage.getInstance().getReference();
    ImageButton backButton;
    ImageButton addReviewButton;
    RecyclerView reviews;
    ReviewAdapter reviewAdapter;
    List<ReviewModel> reviewsList;
    CollectionReference reviewsCollection = db.collection("Reviews");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviews);

        backButton = findViewById(R.id.backButton);
        addReviewButton = findViewById(R.id.addreviewButton);
        reviews = findViewById(R.id.reviewlist);

        backButton.setOnClickListener(goBack);
        addReviewButton.setOnClickListener(addReview);

        reviewsList = new ArrayList<>();
        reviewAdapter = new ReviewAdapter(this, reviewsList);
        reviews.setLayoutManager(new LinearLayoutManager(this));
        reviews.setAdapter(reviewAdapter);

        getData();

    }

    private void getData() {
        reviewsCollection.get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                String locationName = document.getString("locationName");
                String otherComments = document.getString("otherCommentsStr");
                Double averageRating = document.getDouble("averageRating");
                Double latitude = document.getDouble("latitude");
                Double longitude = document.getDouble("longitude");

                StorageReference imageRef = storageRef.child("toilet_images/" + locationName + ".png");

                imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();
                    ReviewModel reviewModel = new ReviewModel(locationName, otherComments, averageRating, imageUrl, latitude, longitude);
                    reviewsList.add(reviewModel);
                    reviewAdapter.notifyDataSetChanged();
                }).addOnFailureListener(e -> {
                    Log.e("ReviewsActivity", "Failed to get download URL for image: " + e.getMessage());
                });
            }});}


    private View.OnClickListener goBack = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Reviews.this, Main.class);
            startActivity(intent);
            finish();
        }
    };

    private View.OnClickListener addReview = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Reviews.this, AddReviews.class);
            startActivity(intent);
            finish();
        }
    };
}

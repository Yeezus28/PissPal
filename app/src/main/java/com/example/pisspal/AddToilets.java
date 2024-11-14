package com.example.pisspal;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;

public class AddToilets extends AppCompatActivity {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    StorageReference storageRef = FirebaseStorage.getInstance().getReference();
    EditText inputLocationName, inputLatitude, inputLongitude;
    ImageButton backButton, locationButton, cameraButton;
    ImageView toiletimg;
    Button saveButton;
    String locationName;
    Double latitude, longitude;
    Bitmap squareBitmap, roundedBitmap;

    private static final int FINE_PERMISSION_CODE = 1;
    private FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_toilets);

        inputLocationName = findViewById(R.id.input_Location);
        inputLatitude = findViewById(R.id.input_latitude);
        inputLongitude = findViewById(R.id.input_longitude);
        backButton = findViewById(R.id.backButton2);
        locationButton = findViewById(R.id.locationButton);
        cameraButton = findViewById(R.id.cameraButton);
        saveButton = findViewById(R.id.button_save);
        toiletimg = findViewById(R.id.toiletimg);

        backButton.setOnClickListener(goBack);
        locationButton.setOnClickListener(setLocation);
        cameraButton.setOnClickListener(goCamera);
        saveButton.setOnClickListener(onSave);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // get image from camera
        String imagePath = getIntent().getStringExtra("imagePath");
        if (imagePath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            Bitmap rotatedBitmap = rotateBitmap(imagePath, bitmap);
            squareBitmap = cropToSquare(rotatedBitmap);
            roundedBitmap = getRoundedCornerBitmap(squareBitmap, 150); // adjust the corner radius
            toiletimg.setImageBitmap(roundedBitmap);
        }
    }

    private void saveToiletDetails(String locationName, double latitude, double longitude, Bitmap roundedBitmap) {
        if (roundedBitmap != null) {
            uploadImageToFirebase(locationName, latitude, longitude, roundedBitmap);
        } else {
            saveToiletDetailsInFirestore(locationName, latitude, longitude, null);
        }
    }

    private void uploadImageToFirebase(String locationName, double latitude, double longitude, Bitmap roundedBitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        roundedBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();

        StorageReference imageRef = storageRef.child("toilet_images/" + locationName + ".png");
        UploadTask uploadTask = imageRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Toast.makeText(AddToilets.this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String imageUrl = uri.toString();
                        saveToiletDetailsInFirestore(locationName, latitude, longitude, imageUrl);
                    }
                });
            }
        });
    }

    private void saveToiletDetailsInFirestore(String locationName, double latitude, double longitude, String imageUrl) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("locationName", locationName);
        hashMap.put("latitude", latitude);
        hashMap.put("longitude", longitude);

        db.collection("ToiletLocations")
                .document(locationName)
                .set(hashMap)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getApplicationContext(), "Toilet location saved successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getApplicationContext(), "Error saving toilet location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // method to crop bitmap into a square
    public Bitmap cropToSquare(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int newWidth = Math.min(width, height);
        int newHeight = Math.min(width, height);

        int cropW = (width - newWidth) / 2;
        int cropH = (height - newHeight) / 2;

        return Bitmap.createBitmap(bitmap, cropW, cropH, newWidth, newHeight);
    }

    // method to create bitmap with rounded corners
    public Bitmap getRoundedCornerBitmap(Bitmap bitmap, float cornerRadius) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, paint);

        paint.setXfermode(new android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    // Method to rotate bitmap
    public Bitmap rotateBitmap(String imagePath, Bitmap bitmap) {
        ExifInterface exif;
        try {
            exif = new ExifInterface(imagePath);
        } catch (IOException e) {
            e.printStackTrace();
            return bitmap;
        }

        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(bitmap, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(bitmap, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(bitmap, 270);
            default:
                return bitmap;
        }
    }

    // method to rotate bitmap
    public Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    // start camera
    private View.OnClickListener goCamera = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(AddToilets.this, CameraApp.class);
            startActivity(intent);
        }
    };

    private View.OnClickListener goBack = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(AddToilets.this, Main.class);
            startActivity(intent);
            finish();
        }
    };

    private View.OnClickListener setLocation = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            getLocation();
        }
    };

    // check inputs
    private View.OnClickListener onSave = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            locationName = inputLocationName.getText().toString().trim();
            if (locationName.isEmpty()) {
                Toast.makeText(AddToilets.this, "Please enter a location name!", Toast.LENGTH_SHORT).show();
                return;
            }

            String latitudeStr = inputLatitude.getText().toString().trim();
            String longitudeStr = inputLongitude.getText().toString().trim();

            if (latitudeStr.isEmpty() || longitudeStr.isEmpty()) {
                Toast.makeText(AddToilets.this, "Please enter latitude and longitude!", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                latitude = Double.parseDouble(latitudeStr);
                longitude = Double.parseDouble(longitudeStr);
            } catch (NumberFormatException e) {
                Toast.makeText(AddToilets.this, "Invalid latitude or longitude!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (roundedBitmap == null) {
                Toast.makeText(AddToilets.this, "No image captured!", Toast.LENGTH_SHORT).show();
                return;
            }

            saveToiletDetails(locationName, latitude, longitude, roundedBitmap);
            Intent intent = new Intent(getApplicationContext(), Main.class);
            startActivity(intent);
            finish();
        }
    };

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(AddToilets.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(AddToilets.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(AddToilets.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_PERMISSION_CODE);
            return;
        }

        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(AddToilets.this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();

                    DecimalFormat decimalFormat = new DecimalFormat("#.###");
                    String formattedLatitude = decimalFormat.format(latitude);
                    String formattedLongitude = decimalFormat.format(longitude);

                    inputLatitude.setText(formattedLatitude);
                    inputLongitude.setText(formattedLongitude);
                } else {
                    Toast.makeText(AddToilets.this, "Unable to retrieve location. Make sure location services are enabled.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == FINE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            } else {
                Toast.makeText(this, "Location permission denied. Cannot retrieve location.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

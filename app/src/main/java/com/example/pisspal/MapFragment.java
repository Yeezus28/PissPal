package com.example.pisspal;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private static final int FINE_PERMISSION_CODE = 1;
    StorageReference storageRef = FirebaseStorage.getInstance().getReference();
    private GoogleMap mMap;
    private BottomNavigationView bottomNavView;
    private Location currentLocation;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference toiletLocations = db.collection("ToiletLocations");
    private Polyline currentPolyline;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        getLocation();

        View view = inflater.inflate(R.layout.fragment_map, container, false);
        bottomNavView = view.findViewById(R.id.MapBottomNav);
        bottomNavView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.tab_map) {
                    return true;
                } else if (id == R.id.tab_addtoilet) {
                    Intent intent = new Intent(getActivity(), AddToilets.class);
                    startActivity(intent);
                    return true;
                } else if (id == R.id.tab_review) {
                    Intent intent = new Intent(getActivity(), Reviews.class);
                    startActivity(intent);
                    return true;
                }
                return false;
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        return view;
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_PERMISSION_CODE);
            return;
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    currentLocation = location;
                    Log.d("MapFragment", "Location retrieved: " + location.getLatitude() + ", " + location.getLongitude());
                    if (mMap != null) {
                        updateMapLocation();
                    }
                } else {
                    Log.d("MapFragment", "Location is null");
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Log.d("MapFragment", "Map is ready");
        mMap.setOnMarkerClickListener(this);
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());

        if (currentLocation != null) {
            updateMapLocation();
        }
        loadToiletLocations();
    }


    private void updateMapLocation() {
        if (currentLocation != null && mMap != null) {
            LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("My Current Location")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));
            mMap.addMarker(markerOptions);
            Log.d("MapFragment", "Location updated on map");
        } else {
            Log.d("MapFragment", "updateMapLocation called but location or map is null");
        }
    }

    private class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
        private final View mWindow;

        CustomInfoWindowAdapter() {
            mWindow = LayoutInflater.from(getContext()).inflate(R.layout.custom_info_window, null);
        }

        private void renderInfoWindow(Marker marker, View view) {
            ImageView infoWindowImage = view.findViewById(R.id.info_window_image);
            TextView infoWindowName = view.findViewById(R.id.info_window_name);

            // Set the name
            infoWindowName.setText(marker.getTitle());

            // Set the image from the marker's tag
            Bitmap bitmap = (Bitmap) marker.getTag();
            if (bitmap != null) {
                infoWindowImage.setImageBitmap(bitmap);
            } else {
                infoWindowImage.setImageResource(R.drawable.tonyleung); // Set a default image if no image is available
            }
        }

        @Override
        public View getInfoWindow(Marker marker) {
            renderInfoWindow(marker, mWindow);
            return mWindow;
        }

        @Override
        public View getInfoContents(Marker marker) {
            return null;
        }
    }



    private void loadToiletLocations() {
        toiletLocations.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    double latitude = document.getDouble("latitude");
                    double longitude = document.getDouble("longitude");
                    String locationName = document.getString("locationName");

                    StorageReference imageRef = storageRef.child("toilet_images/" + locationName + ".png");

                    try {
                        File localFile = File.createTempFile("images", "png");
                        imageRef.getFile(localFile).addOnSuccessListener(taskSnapshot -> {
                            Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 100, 100, false);
                            LatLng latLng = new LatLng(latitude, longitude);
                            MarkerOptions markerOptions = new MarkerOptions()
                                    .position(latLng)
                                    .title(locationName)
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));

                            Marker marker = mMap.addMarker(markerOptions);
                            if (marker != null) {
                                marker.setTag(resizedBitmap);
                            }
                        }).addOnFailureListener(exception -> {
                            // Handle any errors
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }





    @Override
    public boolean onMarkerClick(final Marker marker) {
        if (currentLocation != null && !marker.getTitle().equals("My Current Location")) {
            LatLng destination = marker.getPosition();
            getRoute(currentLocation, destination);
        }
        return false;
    }

    private void getRoute(Location currentLocation, LatLng destination) {
        String url = "https://maps.googleapis.com/maps/api/directions/json?origin=" +
                currentLocation.getLatitude() + "," + currentLocation.getLongitude() +
                "&destination=" + destination.latitude + "," + destination.longitude +
                "&mode=walking" +
                "&key=AIzaSyCWAjZjvOsffSe2bdwcnA3qtNzRlwp6Tzg";

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String res = response.body().string();
                try {
                    JSONObject jsonObject = new JSONObject(res);
                    JSONArray routes = jsonObject.getJSONArray("routes");
                    if (routes.length() > 0) {
                        JSONObject route = routes.getJSONObject(0);
                        JSONObject overviewPolyline = route.getJSONObject("overview_polyline");
                        String encodedPolyline = overviewPolyline.getString("points");
                        getActivity().runOnUiThread(() -> drawRoute(encodedPolyline));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void drawRoute(String encodedPolyline) {
        // Clear the existing polyline if it exists
        if (currentPolyline != null) {
            currentPolyline.remove();
        }

        List<LatLng> polylinePoints = decodePolyline(encodedPolyline);
        List<PatternItem> pattern = Arrays.asList(new Dot(), new Gap(20));
        PolylineOptions polylineOptions = new PolylineOptions()
                .addAll(polylinePoints)
                .width(24)
                .color(Color.parseColor("#FBA834"))
                .pattern(pattern);

        // Add the new polyline to the map and keep a reference to it
        currentPolyline = mMap.addPolyline(polylineOptions);
    }

    private List<LatLng> decodePolyline(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == FINE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            }
        }
    }
}

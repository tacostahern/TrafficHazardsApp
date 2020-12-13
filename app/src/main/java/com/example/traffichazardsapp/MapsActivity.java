package com.example.traffichazardsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.collect.Maps;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.GeoPoint;

import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    Button Logout;
    Button toMarker;

    CameraPosition cameraPosition;

    private FusedLocationProviderClient fusedLocationClient;

    String desc;
    String hType;
    double latitude;
    double longitude;
    String fieldName;

    boolean googleLogin;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Intent intent = getIntent();
        googleLogin = intent.getBooleanExtra("Google Login", false);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // ...
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Logout = findViewById(R.id.Logout);
        toMarker = findViewById(R.id.Marker);

        //Logout Button
        Logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (googleLogin == true) {
                    mGoogleSignInClient.signOut().addOnCompleteListener(MapsActivity.this, new OnCompleteListener<Void>() {
                        Intent logout;
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Log.d("Sign Out", "Signed out of Google!");
                            logout = new Intent(MapsActivity.this, LoginActivity.class);
                            startActivity(logout);
                        }
                    });
                }
                else {
                    mAuth.signOut();
                    toLogin();
                }

            }
        });

        toMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toMarker();
            }
        });
    }
    public void toLogin(){
        Log.d("Sign out", "Signed out of Firebase!");
        Intent intent;
        intent= new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
    public void toMarker(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Customise the styling of the base map using a JSON object defined
        // in a string resource file. First create a MapStyleOptions object
        // from the JSON styles string, then pass this to the setMapStyle
        // method of the GoogleMap object.
        boolean success = googleMap.setMapStyle(new MapStyleOptions(getResources()
                .getString(R.string.style_json)));

        if (!success) {
            Log.e("TAG", "Style parsing failed.");
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("TAG", "No location!");
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(MapsActivity.this);
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener( new OnSuccessListener<Location>() {
                    LatLng coords;
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.

                        if (location != null) {
                            coords = new LatLng(location.getLatitude(), location.getLongitude());
                            cameraPosition = new CameraPosition(coords, 15, 0, 0);
                            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                            Marker home = mMap.addMarker(new MarkerOptions().position(coords).icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("home",100,100))));
                        }
                    }
                });

        localMarker(mMap);

    }

    public Bitmap resizeMapIcons(String iconName, int width, int height){
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(),getResources().getIdentifier(iconName, "drawable", getPackageName()));
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return resizedBitmap;
    }

    //For getting info from previous activity and making a marker
    public void localMarker (GoogleMap map) {
        Intent fromMain = getIntent();
        desc = fromMain.getStringExtra("Description");
        hType = fromMain.getStringExtra("Hazard Type");
        latitude = fromMain.getDoubleExtra("Latitude", 0.0);
        longitude = fromMain.getDoubleExtra("Longitude", 0.0);
        fieldName = fromMain.getStringExtra("Time Taken");

        final LatLng location = new LatLng(latitude, longitude);

        Marker hazard = map.addMarker(new MarkerOptions().position(location).title(hType));
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Intent intent = new Intent(MapsActivity.this, MarkerActivity.class);
                intent.putExtra("Latitude", latitude);
                intent.putExtra("Longitude", longitude);
                intent.putExtra("Time Taken", fieldName);
                startActivity(intent);
                return false;
            }
        });
        hazard.showInfoWindow();

    }

}
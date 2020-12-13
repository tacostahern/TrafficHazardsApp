package com.example.traffichazardsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class MarkerActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private StorageReference mStorageRef;

    String imageLink;

    TextView hazardType;
    TextView description;
    ImageView imageView;
    Button back;
    double latitude;
    double longitude;
    String timeTaken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marker);

        hazardType = findViewById(R.id.hazardType);
        description = findViewById(R.id.dS1);
        imageView = findViewById(R.id.imageView1);
        back = findViewById(R.id.backMain1);

        Intent intent = getIntent();
        latitude = intent.getDoubleExtra("Latitude", 0.0);
        longitude = intent.getDoubleExtra("Longitude", 0.0);
        timeTaken = intent.getStringExtra("Time Taken");
        Log.d("TAG", "Time Taken: " + timeTaken);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        //imageLink = "/" + mAuth.getUid() + "/" + timeTaken + ".jpg";
        mStorageRef = FirebaseStorage.getInstance().getReferenceFromUrl("gs://traffic-hazards-app.appspot.com");


        /*
        if(data.containsKey("imageUri")) {
            Glide.with(getApplicationContext()).load(String.valueOf(data.get("imageUri"))).into(imageView);
        }

         */

        db.collection("Markers")
                .whereEqualTo("timeTaken", timeTaken)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Hazard hazard = document.toObject(Hazard.class);
                                description.setText(hazard.getDescription());
                                hazardType.setText(hazard.getHazardType());
                                //imageView.setImageURI(Uri.parse(hazard.getImageUri()));
                            }
                        } else {
                            Log.d("TAG", "Error getting documents: ", task.getException());
                        }
                    }
                });

        db.collection("Markers")
                .whereEqualTo("timeTaken", timeTaken)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if(document.contains("imageUri")) {
                                    Glide.with(getApplicationContext()).load(String.valueOf(document.get("imageUri"))).into(imageView);
                                }
                            }
                        } else {

                        }
                    }
                });


        Log.d("TAG", "Latitude is " + latitude);
        Log.d("TAG", "Longitude is " + longitude);



        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MarkerActivity.this, MapsActivity.class);
                startActivity(intent);
            }
        });

    }

}

class Hazard {

    private String description;
    private String hazardType;
    private GeoPoint location;
    private String timeTaken;
    private String imageUri;

    public Hazard(String desc, String ht, GeoPoint loc, String tt, String imageUri) {
        setDescription(desc);
        setHazardType(ht);
        setLocation(loc);
        setTimeTaken(tt);
        setImageUri(imageUri);
    }

    public Hazard() {
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHazardType() {
        return hazardType;
    }

    public void setHazardType(String hazardType) {
        this.hazardType = hazardType;
    }

    public GeoPoint getLocation() {
        return location;
    }

    public void setLocation(GeoPoint location) {
        this.location = location;
    }

    public String getTimeTaken() {
        return timeTaken;
    }

    public void setTimeTaken(String timeTaken) {
        this.timeTaken = timeTaken;
    }


    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }


}
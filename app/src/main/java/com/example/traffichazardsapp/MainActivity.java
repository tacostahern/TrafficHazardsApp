package com.example.traffichazardsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.security.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static java.lang.System.currentTimeMillis;


public class MainActivity extends AppCompatActivity {
    Button back;
    Spinner Type;
    Button Submit;
    Button Upload;
    EditText description;
    Spinner hType;

    //For use in getting the image
    File currentImageFile;
    String uri;
    Bitmap fieldImageBitmap;


    String fieldName; //for naming the photo

    //for taking in input from the user
    String desc;
    String hazardType;

    //for use in connecting to Firebase
    Uri downloadUri;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private StorageReference mStorageRef;

    //For use in getting photo
    private int REQUEST_IMAGE_GALLERY = 1;
    private int REQUEST_IMAGE_CAPTURE = 2;
    private static int FILE_REQUEST_CODE = 120;

    GeoPoint location;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Submit = findViewById(R.id.Submit);
        back = findViewById(R.id.backMain);
        Type = findViewById(R.id.Type);
        Upload = findViewById(R.id.Upload);
        description = findViewById(R.id.dS);
        hType = findViewById(R.id.Type);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReferenceFromUrl("gs://traffic-hazards-app.appspot.com");

        location = new GeoPoint(0.0, 0.0);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        //Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.Types_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Type.setAdapter(adapter);

        if (Build.VERSION.SDK_INT >= 24) {
            try {
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toBack();
            }
        });
        Upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage(view);
            }
        });
        Submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fieldName = String.valueOf(currentTimeMillis());
                desc = description.getText().toString();
                hazardType = String.valueOf(hType.getSelectedItem());
                addNewMarker(v);
                toMaps();
            }
        });


    }



    public void getLocation() {


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("TAG", "No location!");

        }

        Log.d("Location", "Location!");
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener( new OnSuccessListener<Location>() {
                    GeoPoint coords;
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.

                        if (location != null) {
                            // Logic to handle location object
                            coords = new GeoPoint(location.getLatitude(), location.getLongitude());

                            //Used to upload data to firestore database
                            Map<String, Object> field = new HashMap<>();

                            field.put("Description", desc);
                            field.put("Hazard Type", hazardType);
                            field.put("Location", coords);
                            //field.put("Location", coords);
                            db.collection("Markers").add(field)
                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            Log.d("Finished", "Finished upload!");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w("TAG", "Error writing document", e);
                                        }
                                    });
                            Log.d("TAG", "Not Null Location!");
                        } else {
                            // Handle failures
                            // ...
                            Log.d("TAG", "Null Location!");
                        }
                    }
                    });

    }



    //takes us back to LoginActivity.class
    public void toBack(){
        Intent intent = new Intent(this,LoginActivity.class);
        startActivity(intent);
    }
    public void toMaps(){
        Intent intent = new Intent(this,MapsActivity.class);
        startActivity(intent);
    }

    public void chooseImage(View view) {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    },
                    FILE_REQUEST_CODE);
        } else {
            chooseProfilePhoto();
        }
    }

    public void chooseProfilePhoto() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        builder.setPositiveButton("Camera", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                doCamera(findViewById(android.R.id.content));
            }
        });
        builder.setNegativeButton("Gallery", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                doGallery(findViewById(android.R.id.content));
            }
        });
        builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        builder.setTitle("Field Picture");
        builder.setMessage("Take picture from:");

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void doCamera(View v) {
        currentImageFile = new File(getExternalCacheDir(), "appimage_" + currentTimeMillis() + ".jpg");
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(currentImageFile));
        takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
    }

    public void doGallery(View v) {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, REQUEST_IMAGE_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_GALLERY && resultCode == RESULT_OK) {
            try {
                processGallery(data);
            } catch (Exception e) {
                Toast.makeText(this, "onActivityResult: " + e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            try {
                processCamera();
            } catch (Exception e) {
                Toast.makeText(this, "onActivityResult: " + e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    }

    private void processCamera() {
        Uri selectedImage = Uri.fromFile(currentImageFile);
        uri = String.valueOf(selectedImage);
        InputStream imageStream = null;

        try {
            imageStream = getContentResolver().openInputStream(selectedImage);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        fieldImageBitmap = BitmapFactory.decodeStream(imageStream);
    }

    private void processGallery(Intent data) {
        Uri galleryImageUri = data.getData();
        uri = String.valueOf(galleryImageUri);
        if (galleryImageUri == null)
            return;

        InputStream imageStream = null;
        try {
            imageStream = getContentResolver().openInputStream(galleryImageUri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        fieldImageBitmap = BitmapFactory.decodeStream(imageStream);
        makeCustomToast(this,
                String.format(Locale.getDefault(),
                        "Gallery Image Size:%n%,d bytes", fieldImageBitmap.getByteCount()),
                Toast.LENGTH_LONG);

    }

    public static void makeCustomToast(Context context, String message, int time) {
        Toast toast = Toast.makeText(context, message, time);
        View toastView = toast.getView();
        TextView tv = toast.getView().findViewById(android.R.id.message);
        tv.setPadding(50, 25, 50, 25);
        tv.setTextColor(Color.WHITE);
        toast.show();
    }

    public void addNewMarker(View view) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        fieldImageBitmap.compress(Bitmap.CompressFormat.JPEG, 25, baos);
        byte[] data = baos.toByteArray();
        String path = mAuth.getUid() + "/" + fieldName + ".jpg";
        mStorageRef = mStorageRef.child(path);
        UploadTask uploadTask = mStorageRef.putBytes(data);

        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                Log.d("Photo upload", "Photo Uploaded!");
                // Continue with the task to get the download URL
                return mStorageRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    downloadUri = task.getResult();
                    getLocation();
                }
            }
        });

    }



}


package com.waterworks.close;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.PermissionRequest;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.security.Provider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 1;
    private static final String TAG = "main";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btn = findViewById(R.id.button);

        final Location[] l = new Location[1];
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            final FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users/").document(user.getUid()).get().addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // TODO: 10/1/2020 move to info activity for username and dob and age
                    Toast.makeText(MainActivity.this, "in fail", Toast.LENGTH_SHORT).show();
                }
            }).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    DocumentSnapshot doc = task.getResult();
                    assert doc != null;
                    User tempu = doc.toObject(User.class);
                    assert tempu != null;
                    if (tempu.getDob() != null && !tempu.getDob().equalsIgnoreCase(" ")) {
                        Toast.makeText(MainActivity.this, "has data", Toast.LENGTH_SHORT).show();
                    } else {
                        User u = null;

                        // get prompts.xml view
                        //region code for Dialog box for username
                        LayoutInflater li = LayoutInflater.from(MainActivity.this);
                        View promptsView = li.inflate(R.layout.dialogbox, null);

                        final EditText userInput = (EditText) promptsView
                                .findViewById(R.id.input);
                        final TextInputLayout inputLayout = (TextInputLayout) promptsView
                                .findViewById(R.id.textInputLayout);
                        final Button btnconfirm = (Button) promptsView
                                .findViewById(R.id.btnconfirm);
                        inputLayout.setPrefixText("");
                        userInput.setText("");
                        // dialog box for username
                        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                                MainActivity.this);
                        alertDialogBuilder.setView(promptsView);
                        alertDialogBuilder.setCancelable(false);
                        alertDialogBuilder.setTitle("Enter Username");
                        final AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                        btnconfirm.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
//                             this will check if the username is already taken or not
                                String userInputText = userInput.getText().toString();
                                db.collection("users/").whereEqualTo("username", userInputText).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.getResult().isEmpty()) {
                                            inputLayout.setHelperTextColor(ColorStateList.valueOf(Color.RED));
                                            inputLayout.setHelperText("this username is taken already");
                                        } else {
                                            alertDialog.dismiss();
                                        }
                                    }
                                });
                            }
                        });
                        //endregion
                    }
                }
            });

//        final Intent lu = new Intent(getBaseContext(),Locationupdate.class);
//        startService(lu);

            btn.setText("Near Me");

            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    LocationListener lo = new LocationListener() {
                        @Override
                        public void onLocationChanged(Location location) {
                            l[0] = location;
                            Toast.makeText(MainActivity.this, location.getLatitude() + "," + location.getLongitude(), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onStatusChanged(String provider, int status, Bundle extras) {

                        }

                        @Override
                        public void onProviderEnabled(String provider) {

                        }

                        @Override
                        public void onProviderDisabled(String provider) {

                        }
                    };
                    LocationManager lm = (LocationManager) getSystemService(MainActivity.this.LOCATION_SERVICE);
                    //for permission
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION}, REQUEST_CODE);
                    } else {
                        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, lo);

                        l[0] = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);


                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        final String[] username = {null};
                        db.collection("locations")
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                Log.d(TAG, document.getId() + " => " + document.getData());
                                                if (document.get("location") != null) {
                                                    GeoPoint l2 = (GeoPoint) document.get("location");
                                                    if (Nearby(l[0], l2)) {
                                                        username[0] = (String) document.get("Name");
                                                        Toast.makeText(MainActivity.this, username[0], Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            }
                                        } else {
                                            Log.d(TAG, "Error getting documents: ", task.getException());
                                        }
                                    }
                                });
                    }
                }
            });

            // TODO: 9/24/2020 temp
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        Map<String, User> user = new HashMap<>();
//        ArrayList<String> friends = new ArrayList<>();
//        friends.add("a");
//        friends.add("b");
//        ArrayList<String> c = new ArrayList<>();
//        c.add("a");
//        c.add("b");
//        GeoPoint g = new GeoPoint(0, 0);
//        User u = new User("sam", g, friends, c, true);
//        user.put("sam", u);
//
//
//        db.collection("users").add(user).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentReference> task) {
//                Log.d(TAG, "onComplete: ");
//                Toast.makeText(MainActivity.this, "user added", Toast.LENGTH_SHORT).show();
//            }
//
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//
//            }
//        });


//
//        db.collection("locations").whereEqualTo("location",)
//        if (l!= null)
//            Nearby(l[0]);
//        else
//            Toast.makeText(this, "null location", Toast.LENGTH_SHORT).show();
        } else {
            // TODO: 10/1/2020  add when it will do when auth is null
        }
    }


    //for checking if people are nearby
    public boolean Nearby(Location l1, GeoPoint l2) {
        float[] results = new float[1];
        Location.distanceBetween(l1.getLatitude(), l1.getLongitude(), l2.getLatitude(), l2.getLongitude(), results);
        float distanceInMeters = results[0];
        boolean isWithin10km = distanceInMeters < 10000;
        return isWithin10km;
    }

}
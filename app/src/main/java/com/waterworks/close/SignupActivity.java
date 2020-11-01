package com.waterworks.close;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 001;
    private static final String TAG = "a";
    private static final int LOCATION_CODE = 002;
    private FirebaseAuth auth;
    private GoogleSignInClient mGoogleSignInClient;
    SignInButton googlesignin;
    ConstraintLayout parent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        auth = FirebaseAuth.getInstance();
        checkIfSignedin();
        googlesignin = findViewById(R.id.googlesignin);
        parent = findViewById(R.id.parent);
        handlepermssion();
        handleGoogleSignin();

    }

    private void checkIfSignedin() {
        FirebaseUser u = auth.getCurrentUser();
        if (u!=null){
            Intent In = new Intent(this, MainActivity.class);
            startActivity(In);
        }
    }

    private void handlepermssion(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                showsnackbar();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_CODE);
            }
        }
    }

    private void handleGoogleSignin() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        googlesignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        final AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Toast.makeText(SignupActivity.this, "sign up", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = auth.getCurrentUser();
                            // TODO: 9/24/2020 temp data
                            Map<String, User> u = new HashMap<>();
                            ArrayList<String> friends = new ArrayList<>();
                            friends.add("a");
                            friends.add("b");
                            ArrayList<String> c = new ArrayList<>();
                            c.add("a");
                            c.add("b");
                            GeoPoint g = new GeoPoint(0, 0);
                            String date =" ";

                            assert user != null;
                            User uu = new User(user.getEmail().substring(0, user.getEmail().indexOf("@")), g, null, c, true,date);
                            u.put(user.getUid(), uu);
                            //User us = new User(user.getEmail(),, "", 0.0,0);
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            db.collection("users/").document(user.getUid()).set(u).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Intent intent = new Intent(SignupActivity.this, MainActivity.class);
//                                    intent.putExtra("emailname", auth.getCurrentUser().getEmail().substring(0, auth.getCurrentUser().getEmail().toString().indexOf("@")));
                                    startActivity(intent);
                                }
                            });

//                        todo delete it
//                        .add(u).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
//                                @Override
//                                public void onComplete(@NonNull Task<DocumentReference> task) {
//
//                                }
//                            }).addOnFailureListener(new OnFailureListener() {
//                                @Override
//                                public void onFailure(@NonNull Exception e) {
//                                    Toast.makeText(SignupActivity.this, "failed to sign up", Toast.LENGTH_SHORT).show();
//                                }
//                            });


                            //DatabaseReference dr = FirebaseDatabase.getInstance().getReference("User/" + user.getUid());
//                            dr.setValue(us).addOnFailureListener(new OnFailureListener() {
//                                @Override
//                                public void onFailure(@NonNull Exception e) {
//                                    Toast.makeText(SignupActivity.this, "Signiup failed", Toast.LENGTH_SHORT).show();
//                                }
//                            }).addOnCompleteListener(new OnCompleteListener<Void>() {
//                                @Override
//                                public void onComplete(@NonNull Task<Void> task) {
//
//                                    Toast.makeText(SignupActivity.this, "Sign In Successful", Toast.LENGTH_SHORT).show();
//                                    Intent intent = new Intent(SignupActivity.this, AfterLoginActivity.class);
//                                    intent.putExtra("emailname", auth.getCurrentUser().getEmail().substring(0, auth.getCurrentUser().getEmail().toString().indexOf("@")));
//                                    startActivity(intent);
//                                }
//                            });

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(SignupActivity.this, "Signiup failed", Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });

    }
    private void showsnackbar() {
        Snackbar.make(parent, "need location permission", Snackbar.LENGTH_INDEFINITE).setAction("Grant Permission", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                in.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(in, LOCATION_CODE);
            }
        }).show();
    }
}
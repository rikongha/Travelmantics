package com.leroi.travelmantics.utils;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.leroi.travelmantics.ListActivity;
import com.leroi.travelmantics.model.TravelDeal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FirebaseUtil {
    public static FirebaseDatabase sFirebaseDatabase;
    public static DatabaseReference sDatabaseReference;
    private static FirebaseUtil sFirebaseutil;
    public static FirebaseAuth sFirebaseAuth;
    public static FirebaseStorage sFirebaseStorage;
    public static StorageReference sStorageReference;
    public static FirebaseAuth.AuthStateListener sAuthListener;
    public static ArrayList<TravelDeal> mDeals;
    private static Activity caller;
    private static int RC_SIGN_IN = 123;
    public static boolean isAdmin;


    private FirebaseUtil() {
    }

    public static void openFbReference(String ref, final Activity callerActivity) {
        if (sFirebaseutil == null) {
            sFirebaseutil = new FirebaseUtil();
            sFirebaseDatabase = FirebaseDatabase.getInstance();
            sFirebaseAuth = FirebaseAuth.getInstance();
            caller = callerActivity;
            sAuthListener = new FirebaseAuth.AuthStateListener() {

                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    if (firebaseAuth.getCurrentUser() == null) {
                        FirebaseUtil.signIn();
                    } else {
                        String userId = firebaseAuth.getUid();
                        checkAdmin(userId);
                    }
                    Toast.makeText(callerActivity.getBaseContext(), "Welcome back", Toast.LENGTH_LONG).show();
                }
            };
            connectStorage();
        }
        mDeals = new ArrayList<TravelDeal>();
        sDatabaseReference = sFirebaseDatabase.getReference().child(ref);
    }

    public static void signIn() {
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build()
        );

        // Create and launch sign-in intent
        caller.startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);
    }

    public static void attachListener() {
        sFirebaseAuth.addAuthStateListener(sAuthListener);
    }

    public static void detachListener() {
        sFirebaseAuth.removeAuthStateListener(sAuthListener);
    }

    public static void checkAdmin(String uid) {
        FirebaseUtil.isAdmin = false;
        DatabaseReference ref = sFirebaseDatabase.getReference().child("administrators").child(uid);
        ChildEventListener listener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                FirebaseUtil.isAdmin = true;
                caller.invalidateOptionsMenu(); //TODO fix showMenu() not work for some reason
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        ref.addChildEventListener(listener);
    }

    public static void connectStorage() {
        sFirebaseStorage = FirebaseStorage.getInstance();
        sStorageReference = sFirebaseStorage.getReference().child("deals_picture");
    }
}
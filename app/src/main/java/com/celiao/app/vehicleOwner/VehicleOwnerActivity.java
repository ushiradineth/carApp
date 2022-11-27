package com.celiao.app.vehicleOwner;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.celiao.app.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

// this is a page handles whether the vehicle owner has already added a vehicle or not
public class VehicleOwnerActivity extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        Intent emailIntent = getIntent();
        String email = emailIntent.getStringExtra("email");

        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //updates the database with the new records if the conditions are met
        FirebaseFirestore.getInstance().collection("users").document(email).get()
        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.getString("vehicle") != null) {
                        FirebaseFirestore.getInstance().collection("vehicles").document(document.getString("vehicle")).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if(document.exists()){
                                        Intent intent = new Intent(getApplicationContext(), VehicleOwnerAddedActivity.class);
                                        intent.putExtra("email", email);
                                        startActivity(intent);
                                    } else {
                                        Map<String, Object> temp = new HashMap<>();
                                        temp.put("vehicle", FieldValue.delete());
                                        FirebaseFirestore.getInstance().collection("users").document(email).set(temp, SetOptions.merge());
                                        Intent intent = new Intent(getApplicationContext(), VehicleOwnerAddVehicleActivity.class);
                                        intent.putExtra("email", email);
                                        startActivity(intent);
                                    }
                                }
                            }
                        });
                    } else {
                        Intent intent = new Intent(getApplicationContext(), VehicleOwnerAddVehicleActivity.class);
                        intent.putExtra("email", email);
                        startActivity(intent);
                    }
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        this.moveTaskToBack(true);
    }
}
package com.celiao.app.vehicleOwner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.celiao.app.LoginActivity;
import com.celiao.app.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class VehicleOwnerActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_owner);

        Intent emailIntent = getIntent();
        String email = emailIntent.getStringExtra("email");

        //updates the database with the new records if the conditions are met
        FirebaseFirestore.getInstance().collection("users").document(email).get()
        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.getString("vehicle") == null) {
                        Intent intent = new Intent(getApplicationContext(), VehicleOwnerAddVehicleActivity.class);
                        intent.putExtra("email", email);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(getApplicationContext(), VehicleOwnerAddedActivity.class);
                        intent.putExtra("email", email);
                        startActivity(intent);
                    }
                }
            }
        });
    }
}
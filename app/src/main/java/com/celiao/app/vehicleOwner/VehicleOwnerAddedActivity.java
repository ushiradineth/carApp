package com.celiao.app.vehicleOwner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.celiao.app.LoginActivity;
import com.celiao.app.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

// this is the page vehicle owner who have already registered their vehicle sees
public class VehicleOwnerAddedActivity extends AppCompatActivity {
    Button deleteVehicle, logout;
    TextView vehicle, price, description, category, model, availability, greeting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_owner_added);

        greeting = (TextView) findViewById(R.id.textView_greeting);
        vehicle = (TextView) findViewById(R.id.textView_vehicle);
        price = (TextView) findViewById(R.id.textView_price);
        description = (TextView) findViewById(R.id.textView_description);
        category = (TextView) findViewById(R.id.textView_category);
        model = (TextView) findViewById(R.id.textView_model);
        availability = (TextView) findViewById(R.id.textView_availability);

        Intent intent = getIntent();
        String email = intent.getStringExtra("email");

        deleteVehicle = (Button) findViewById(R.id.button_delete);

        //getting owners vehicle information
        FirebaseFirestore.getInstance().collection("users").document(email).get()
        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    greeting.setText("Hello, " + document.getString("fullName"));
                    if (document.getString("vehicle") != null) {

                        //getting vehicle information
                        FirebaseFirestore.getInstance().collection("vehicles").document(document.getString("vehicle")).get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                DocumentSnapshot doc = task.getResult();
                                vehicle.setText(doc.getString("vehicleName"));
                                price.setText(doc.getString("price"));
                                description.setText(doc.getString("description"));
                                category.setText(doc.getString("category"));
                                model.setText(doc.getString("model"));
                                availability.setText(doc.getString("isAvailable"));

                                if(doc.getString("isAvailable").equals("true")){
                                    deleteVehicle.setVisibility(View.VISIBLE);
                                    deleteVehicle.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Log.d("TAG", document.getString("vehicle"));

                                            //deleting the vehicle
                                            FirebaseFirestore.getInstance().collection("vehicles").document(document.getString("vehicle")).delete()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    Map<String, Object> temp = new HashMap<>();
                                                    temp.put("vehicle", null);

                                                    //removing the vehicle from the vehicle owners record
                                                    FirebaseFirestore.getInstance().collection("users").document(email).set(temp, SetOptions.merge());

                                                    Intent intent = new Intent(getApplicationContext(), VehicleOwnerAddVehicleActivity.class);
                                                    intent.putExtra("email", email);
                                                    startActivity(intent);
                                                    Toast.makeText(getApplicationContext(),"Vehicle Deleted", Toast.LENGTH_LONG).show();
                                                }
                                            });
                                        }
                                    });
                                } else {
                                    deleteVehicle.setVisibility(View.GONE);
                                }
                           }
                       });
                    }
                }
            }
        });

        logout = (Button) findViewById(R.id.button_logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(view.getContext())
                    .setTitle("Logout?")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            //removing user login information
                            view.getContext().getSharedPreferences("shared", 0).edit().clear().commit();

                            //sending the user back to the login page
                            Intent intent = new Intent(view.getContext(), LoginActivity.class);
                            startActivity(intent);
                            ((Activity) view.getContext()).overridePendingTransition(0, 0);
                        }
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        this.moveTaskToBack(true);
    }
}
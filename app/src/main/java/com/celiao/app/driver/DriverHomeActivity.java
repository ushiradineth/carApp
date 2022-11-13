package com.celiao.app.driver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
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

import java.util.HashMap;
import java.util.Map;

public class DriverHomeActivity extends AppCompatActivity {
    TextView greeting, msg, job, number, date, address, vehicle;
    Switch availability;
    Button logout, deleteAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_home);

        Intent intent = getIntent();
        String email = intent.getStringExtra("email");

        greeting = (TextView) findViewById(R.id.textView_greeting);
        msg = (TextView) findViewById(R.id.textView_msg);
        availability = (Switch) findViewById(R.id.switch_available);

        job = (TextView) findViewById(R.id.textView_job);
        number = (TextView) findViewById(R.id.textView_number);
        date = (TextView) findViewById(R.id.textView_date);
        address = (TextView) findViewById(R.id.textView_address);
        vehicle = (TextView) findViewById(R.id.textView_vehicle);

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

        deleteAccount = (Button) findViewById(R.id.button_delete);
        deleteAccount.setVisibility(View.GONE);

        FirebaseFirestore.getInstance().collection("users").document(email).get()
            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists() && document.getString("role").equals("Driver")) {
                            if(document.getString("isAvailable") != null){
                                if(document.getString("isAvailable").equals("true")) availability.setChecked(true);
                            }
                            if(document.getString("booking") == null){
                                deleteAccount.setVisibility(View.VISIBLE);
                                deleteAccount.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        getApplicationContext().getSharedPreferences("shared", 0).edit().clear().commit();

                                        FirebaseFirestore.getInstance().collection("users").document(email).delete();
                                        Toast.makeText(getApplicationContext(), "User deleted!", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                        startActivity(intent);
                                    }
                                });
                            }
                        }
                    }
                }
            });

        availability.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                //checks if the driver is a booked driver or not, this is the first page driver sees after they login
                FirebaseFirestore.getInstance().collection("users").document(email).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists() && document.getString("role").equals("Driver")) {
                                    //changing the vehicle availability to false
                                    Map<String, Object> temp = new HashMap<>();
                                    temp.put("isAvailable", String.valueOf(b));
                                    FirebaseFirestore.getInstance().collection("users").document(email).set(temp, SetOptions.merge());
                                    if(b) msg.setText("You will get a Request soon!"); else msg.setText("");

                                }
                            }
                        }
                    });
            }
        });

        FirebaseFirestore.getInstance().collection("users").document(email).get()
            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists() && document.getString("role").equals("Driver")) {
                            if(document.getString("booking") != null){
                                FirebaseFirestore.getInstance().collection("bookings").document(document.getString("booking")).get()
                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                DocumentSnapshot document = task.getResult();
                                                if (document.exists()) {
                                                    Log.d("asd", "onComplete: here");
                                                    availability.setVisibility(View.GONE);
                                                    msg.setVisibility(View.GONE);

                                                    job.setVisibility(View.VISIBLE);

                                                    date.setVisibility(View.VISIBLE);
                                                    address.setVisibility(View.VISIBLE);
                                                    vehicle.setVisibility(View.VISIBLE);

                                                    date.setText("From " + document.getString("bookedDate") + " to " + document.getString("endDate"));
                                                    address.setText("Address: " + document.getString("address"));
                                                    vehicle.setText("Vehicle: " + document.getString("vehicle"));

                                                    date.setGravity(Gravity.CENTER_VERTICAL);

                                                    FirebaseFirestore.getInstance().collection("users").document(document.getString("email")).get()
                                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                if (task.isSuccessful()) {
                                                                    DocumentSnapshot document = task.getResult();
                                                                    if (document.exists()) {
                                                                        number.setVisibility(View.VISIBLE);
                                                                        number.setText("Number: " + document.getString("mobileNo"));
                                                                    }
                                                                }
                                                            }
                                                        });
                                                }
                                            }
                                        }
                                    });
                            }
                        }
                    }
                }
            });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        String email = intent.getStringExtra("email");

        //checks if the driver is a booked driver or not, this is the first page driver sees after they login
        FirebaseFirestore.getInstance().collection("users").document(email).get()
            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists() && document.getString("role").equals("Driver")) {
                            greeting.setText("Hello, " + document.getString("fullName") + "!");
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
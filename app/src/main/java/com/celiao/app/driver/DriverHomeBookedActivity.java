package com.celiao.app.driver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.celiao.app.LoginActivity;
import com.celiao.app.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

//this is
public class DriverHomeBookedActivity extends AppCompatActivity {
    TextView greeting, job, number, date, address, vehicle;
    ImageView logout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_home_booked);

        Intent intent = getIntent();
        String email = intent.getStringExtra("email");

        greeting = (TextView) findViewById(R.id.textView_greeting);

        job = (TextView) findViewById(R.id.textView_job);
        number = (TextView) findViewById(R.id.textView_number);
        date = (TextView) findViewById(R.id.textView_date);
        address = (TextView) findViewById(R.id.textView_address);
        vehicle = (TextView) findViewById(R.id.textView_vehicle);

        logout = (ImageView) findViewById(R.id.button_logout);
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

        //handling driver info
        FirebaseFirestore.getInstance().collection("users").document(email).get()
            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists() && document.getString("role").equals("Driver")) {
                            if(document.getString("booking") == null){
                                Intent intent = new Intent(getApplicationContext(), DriverHomeNotBookedActivity.class);
                                intent.putExtra("email", email);
                                startActivity(intent);
                            } else {
                                //getting booking details
                                FirebaseFirestore.getInstance().collection("bookings").document(document.getString("booking")).get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    DocumentSnapshot document = task.getResult();
                                                    if (document.exists()) {
                                                        date.setText(document.getString("bookedDate") + " - " + document.getString("endDate"));
                                                        address.setText(document.getString("address"));
                                                        vehicle.setText(document.getString("vehicle"));

                                                        //getting customers number
                                                        FirebaseFirestore.getInstance().collection("users").document(document.getString("email")).get()
                                                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                        if (task.isSuccessful()) {
                                                                            DocumentSnapshot document = task.getResult();
                                                                            if (document.exists()) {
                                                                                number.setText(document.getString("mobileNo"));
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
    public void onBackPressed() {
        this.moveTaskToBack(true);
    }
}
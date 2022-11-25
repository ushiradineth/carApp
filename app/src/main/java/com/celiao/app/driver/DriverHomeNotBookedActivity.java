
package com.celiao.app.driver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.celiao.app.LoginActivity;
import com.celiao.app.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class DriverHomeNotBookedActivity extends AppCompatActivity {
    Switch availability;
    TextView greeting;
    ImageView logout, deleteAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_home_not_booked);

        Intent intent = getIntent();
        String email = intent.getStringExtra("email");

        greeting = (TextView) findViewById(R.id.textView_greeting);
        availability = (Switch) findViewById(R.id.switch_available);

        //setting the greeting
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

        deleteAccount = (ImageView) findViewById(R.id.button_delete);
        deleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(view.getContext())
                        .setTitle("Delete account?")
                        .setMessage("Are you sure you want to delete your account?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                getApplicationContext().getSharedPreferences("shared", 0).edit().clear().commit();

                                FirebaseFirestore.getInstance().collection("users").document(email).delete();
                                Toast.makeText(getApplicationContext(), "User deleted!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .show();
            }
        });

        //for availability management
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
                        }
                    }
                }
            });

        //handling available switch
        availability.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
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

                                    //changing availability on the drivers records
                                    FirebaseFirestore.getInstance().collection("users").document(email).set(temp, SetOptions.merge());
                                }
                            }
                        }
                    });
            }
        });
    }

    @Override
    public void onBackPressed() { this.moveTaskToBack(true); }
}
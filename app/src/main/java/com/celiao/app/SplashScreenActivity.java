package com.celiao.app;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import com.celiao.app.customer.HomeActivity;
import com.celiao.app.driver.DriverHomeActivity;
import com.celiao.app.vehicleOwner.VehicleOwnerActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        //checking if the user has already logged in
        SharedPreferences shared = getSharedPreferences("shared", MODE_PRIVATE);
        if(shared.contains("email")){
            //making sure that the admin dint delete the user
            FirebaseFirestore.getInstance().collection("users").document(shared.getString("email", "defaultStringIfNothingFound")).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot doc = task.getResult();
                        if (doc.exists()) {
                            login(doc, shared.getString("email", "defaultStringIfNothingFound"));
                        }
                        else {
                            getApplicationContext().getSharedPreferences("shared", 0).edit().clear().commit();
                            Toast.makeText(getApplicationContext(),"Your account has being deleted by the Admin.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        } else {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onBackPressed() {
        this.moveTaskToBack(true);
    }

    public void login(DocumentSnapshot doc, String email) {
        switch (doc.getString("role")){
            case "Customer":
                FirebaseFirestore.getInstance().collection("bookings").document(email).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                if (document.getString("email") != null) {
                                    dbUpdate(document);
                                }
                            }
                        }
                    }
                });
                Intent Customer = new Intent(getApplicationContext(), HomeActivity.class);
                Customer.putExtra("email", email);
                startActivity(Customer);
                break;
            case "Driver":
                FirebaseFirestore.getInstance().collection("users").document(email).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot doc1 = task.getResult();
                            if (doc1.exists()) {
                                if(doc1.getString("booking") != null){
                                    FirebaseFirestore.getInstance().collection("bookings").document(doc1.getString("booking")).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                DocumentSnapshot document = task.getResult();
                                                if (document.exists()) {
                                                    if (document.getString("email") != null) {
                                                        dbUpdate(document);
                                                    }
                                                }
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    }
                });
                Intent Driver = new Intent(getApplicationContext(), DriverHomeActivity.class);
                Driver.putExtra("email", email);
                startActivity(Driver);
                break;
            case "Vehicle owner":
                FirebaseFirestore.getInstance().collection("users").document(email).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot doc1 = task.getResult();
                            if (doc1.exists()) {
                                FirebaseFirestore.getInstance().collection("bookings").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                if (document.getString("vehicle") != null) {
                                                    if(document.getString("vehicle").equals(doc1.getString("vehicle"))){
                                                        dbUpdate(document);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                });
                            }
                        }
                    }
                });
                Intent owner = new Intent(getApplicationContext(), VehicleOwnerActivity.class);
                owner.putExtra("email", email);
                startActivity(owner);
                break;
            default:
                Toast.makeText(getApplicationContext(),"Role doesn't exist", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void dbUpdate(DocumentSnapshot document) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        if(document.getString("endDate").compareTo(formatter.format(LocalDate.now())) < 0) {
            Map<String, Object> booking = new HashMap<>();
            booking.put("email", document.getString("email"));
            booking.put("vehicle", document.getString("vehicle"));
            booking.put("driver", document.getString("driver"));
            booking.put("duration", document.getString("duration"));
            booking.put("address", document.getString("address"));
            booking.put("bookedDate", document.getString("bookedDate"));
            booking.put("endDate", document.getString("endDate"));

            FirebaseFirestore.getInstance().collection("bookings").document(document.getString("email")).collection("oldbookings").document(document.getString("endDate").split("/")[0]+document.getString("endDate").split("/")[1]+document.getString("endDate").split("/")[2]).set(booking);

            Map<String, Object> temp = new HashMap<>();
            temp.put("isAvailable", "true");
            FirebaseFirestore.getInstance().collection("vehicles").document(document.getString("vehicle")).update(temp);
            temp.put("booking", FieldValue.delete());
            FirebaseFirestore.getInstance().collection("users").document(document.getString("driver")).set(temp, SetOptions.merge());

            Map<String, Object> empty = new HashMap<>();
            empty.put("email", FieldValue.delete());
            empty.put("vehicle", FieldValue.delete());
            empty.put("driver", FieldValue.delete());
            empty.put("duration", FieldValue.delete());
            empty.put("address", FieldValue.delete());
            empty.put("bookedDate", FieldValue.delete());
            empty.put("endDate", FieldValue.delete());

            FirebaseFirestore.getInstance().collection("bookings").document(document.getString("email")).set(empty, SetOptions.merge());

            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
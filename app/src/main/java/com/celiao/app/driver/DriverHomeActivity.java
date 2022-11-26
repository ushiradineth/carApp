package com.celiao.app.driver;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import com.celiao.app.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.TimeUnit;

// this is a cannot be seen by drivers, this is just a loading state
public class DriverHomeActivity extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        Intent intent = getIntent();
        String email = intent.getStringExtra("email");

        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        FirebaseFirestore.getInstance().collection("users").document(email).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists() && document.getString("role").equals("Driver")) {
                                if(document.getString("booking") != null){
                                    Intent intent = new Intent(getApplicationContext(), DriverHomeBookedActivity.class);
                                    intent.putExtra("email", email);
                                    startActivity(intent);
                                } else {
                                    Intent intent = new Intent(getApplicationContext(), DriverHomeNotBookedActivity.class);
                                    intent.putExtra("email", email);
                                    startActivity(intent);
                                }
                            }
                        }
                    }
                });
    }
}
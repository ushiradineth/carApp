package com.celiao.app.customer;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.celiao.app.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.TimeUnit;

/*
    this page is not visible to the user, this page holds the paths to the 5 fragments
    home, homebooked, booking, bookingbooked, profile
*/

public class HomeActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    HomeFragment home = new HomeFragment();
    BookingFragment booking = new BookingFragment();
    ProfileFragment profile = new ProfileFragment();
    HomeBookedFragment homeBooked = new HomeBookedFragment();
    BookingBookedFragment bookingBooked  = new BookingBookedFragment();
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_home);

        Intent intent = getIntent();
        String email = intent.getStringExtra("email");
        String vehicle = intent.getStringExtra("vehicle");

        Bundle bundle = new Bundle();
        bundle.putString("email", email);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        //for bookings through view vehicle activity
        if(vehicle != null){
            bundle.putString("vehicle", vehicle);
            booking.setArguments(bundle);

            //changing highlighted icon
            bottomNavigationView.setSelectedItemId(R.id.booking);

            getSupportFragmentManager().beginTransaction().replace(R.id.fragmentFrame, booking).commit();
            bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()){
                        case R.id.home:
                            home.setArguments(bundle);
                            getSupportFragmentManager().beginTransaction().replace(R.id.fragmentFrame,home).commit();
                            break;
                        case R.id.booking:
                            booking.setArguments(bundle);
                            getSupportFragmentManager().beginTransaction().replace(R.id.fragmentFrame,booking).commit();
                            break;
                        case R.id.profile:
                            profile.setArguments(bundle);
                            getSupportFragmentManager().beginTransaction().replace(R.id.fragmentFrame,profile).commit();
                            break;
                    }
                    return true;
                }
            });
            return;
        }

        //checks if the user is a booked user or not, this is the first page user sees after they login
        firestore.collection("bookings").document(email).get()
            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            if(document.getString("email") == null){
                                home.setArguments(bundle);
                                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentFrame, home).commit();
                            } else {
                                homeBooked.setArguments(bundle);
                                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentFrame, homeBooked).commit();
                            }
                        } else {
                            home.setArguments(bundle);
                            getSupportFragmentManager().beginTransaction().replace(R.id.fragmentFrame, home).commit();
                        }
                    }
                }
            });

        //controls what happens when a user clicks one of the three navigation buttons
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                firestore.collection("bookings").document(email).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                //user gets sent to different pages depending on if the user is a booked user or not
                                if (document.exists()) {
                                    if(document.getString("email") == null){
                                        switch (item.getItemId()){
                                            case R.id.home:
                                                home.setArguments(bundle);
                                                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentFrame,home).commit();
                                                break;
                                            case R.id.booking:
                                                booking.setArguments(bundle);
                                                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentFrame,booking).commit();
                                                break;
                                            case R.id.profile:
                                                profile.setArguments(bundle);
                                                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentFrame,profile).commit();
                                                break;
                                        }
                                    } else {
                                        switch (item.getItemId()) {
                                            case R.id.home:
                                                homeBooked.setArguments(bundle);
                                                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentFrame, homeBooked).commit();
                                                break;
                                            case R.id.booking:
                                                bookingBooked.setArguments(bundle);
                                                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentFrame, bookingBooked).commit();
                                                break;
                                            case R.id.profile:
                                                profile.setArguments(bundle);
                                                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentFrame, profile).commit();
                                                break;
                                        }
                                    }
                                } else {
                                    switch (item.getItemId()){
                                        case R.id.home:
                                            home.setArguments(bundle);
                                            getSupportFragmentManager().beginTransaction().replace(R.id.fragmentFrame,home).commit();
                                            break;
                                        case R.id.booking:
                                            booking.setArguments(bundle);
                                            getSupportFragmentManager().beginTransaction().replace(R.id.fragmentFrame,booking).commit();
                                            break;
                                        case R.id.profile:
                                            profile.setArguments(bundle);
                                            getSupportFragmentManager().beginTransaction().replace(R.id.fragmentFrame,profile).commit();
                                            break;
                                    }
                                }
                            }
                        }
                    });
                return true;
            }
        });
    }

    @Override
    public void onBackPressed() {
        this.moveTaskToBack(true);
    }
}
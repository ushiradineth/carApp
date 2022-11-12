package com.celiao.app.customer;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.celiao.app.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

//this page is the home page for booked users
public class HomeBookedFragment extends Fragment {
    TextView textView_greeting, textView_bookingDaysLeft;
    Button button_support, button_viewBooking;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_customer_home_booked, container, false);

        textView_greeting = (TextView) view.findViewById(R.id.textView_greeting);
        textView_bookingDaysLeft = (TextView) view.findViewById(R.id.bookingdaysleft);

        String email = getArguments().getString("email");

        //gets user's name and sets a greeting
        FirebaseFirestore.getInstance().collection("users").document(email).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        textView_greeting.setText(new StringBuilder().append("Welcome back, ").append(doc.getString("fullName")).append("!").toString());

                    }
                }
            }
        });

        //sets a msg based on how many days are left on the users booking
        FirebaseFirestore.getInstance().collection("bookings").document(email).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        String today = LocalDate.now().toString();
                        String endDate = doc.getString("endDate");

                        //converting the dates to the same format
                        try {
                            Date start = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
                                    .parse(today);
                            Date end = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
                                    .parse(endDate);

                            //comparing the dates
                            long diff = end.getTime() - start.getTime();

                            textView_bookingDaysLeft.setText(String.valueOf(TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS))+" days left on your current booking.");

                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

        //takes the user to see details of their current booking
        button_viewBooking = view.findViewById(R.id.btn_viewBooking);
        button_viewBooking.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //changing highlighted icon
                BottomNavigationView mBottomNavigationView = getActivity().findViewById(R.id.bottomNavigationView);
                mBottomNavigationView.setSelectedItemId(R.id.booking);

                //sending user to booking fragment
                BookingBookedFragment bookingBooked  = new BookingBookedFragment();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragmentFrame, bookingBooked).addToBackStack(null).commit();
                Bundle bundle = new Bundle();
                bundle.putString("email", email);
                bookingBooked.setArguments(bundle);
            }
        });

        button_support = view.findViewById(R.id.btn_support);
        button_support.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), ViewSupportActivity.class);
                intent.putExtra("email", email);
                startActivity(intent);
            }
        });

        return view;
    }
}
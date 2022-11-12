package com.celiao.app.customer;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
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

//this page is the home page for users who has not made a booking yet
public class HomeFragment extends Fragment {
    TextView textView_greeting;
    Button button_viewVehicles, button_viewBooking;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_customer_home, container, false);

        textView_greeting = (TextView) view.findViewById(R.id.textView_greeting);

        String email = getArguments().getString("email");

        //gets user's name and sets a greeting
        FirebaseFirestore.getInstance().collection("users").document(email).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        textView_greeting.setText(new StringBuilder().append("Hello, ").append(doc.getString("fullName")).append("!").toString());
                    }
                } else {

                }
            }
        });

        //takes the user to see a list of vehicles
        button_viewVehicles = view.findViewById(R.id.button_viewVehicles);
        button_viewVehicles.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(getContext(), ViewVehiclesActivity.class);
                intent.putExtra("email", email);
                startActivity(intent);
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
                BookingFragment booking  = new BookingFragment();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragmentFrame, booking).addToBackStack(null).commit();
                Bundle bundle = new Bundle();
                bundle.putString("email", email);
                booking.setArguments(bundle);
            }
        });

        return view;
    }
}
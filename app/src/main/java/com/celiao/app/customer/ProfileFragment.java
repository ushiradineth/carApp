package com.celiao.app.customer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.widget.Toast;

import com.celiao.app.LoginActivity;
import com.celiao.app.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

//this page is for user to see their user profile and to logout from their account
public class ProfileFragment extends Fragment {
    TextView textView_fullname, textView_email, textView_mobileno;
    Button logout, btnchoice;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_customer_profile, container, false);

        textView_fullname = (TextView) view.findViewById(R.id.textView_fullName);
        textView_email = (TextView) view.findViewById(R.id.textView_email);
        textView_mobileno = (TextView) view.findViewById(R.id.textView_vehicleno);

        String email = getArguments().getString("email");

        //getting user details
        FirebaseFirestore.getInstance().collection("users").document(email).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        textView_fullname.setText(doc.getString("fullName"));
                        textView_email.setText(doc.getString("email"));
                        textView_mobileno.setText(doc.getString("mobileNo"));
                    }
                }
            }
        });

        //button for user to see their booking if they have one
        btnchoice = (Button) view.findViewById(R.id.btn_seebookingordelleteuser);
        FirebaseFirestore.getInstance().collection("bookings").document(email).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        btnchoice.setText("See booking");
                        btnchoice.setVisibility(View.VISIBLE);
                        btnchoice.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

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
                    } else {
                        btnchoice.setText("Delete profile");
                        btnchoice.setVisibility(View.VISIBLE);
                        btnchoice.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                //double checking if the user wants to logout
                                new AlertDialog.Builder(view.getContext())
                                        .setTitle("Delete account?")
                                        .setMessage("Are you sure you want to delete your account?")
                                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                //removing user login information
                                                getContext().getSharedPreferences("shared", 0).edit().clear().commit();

                                                //deleting the user if they dont have a booking
                                                FirebaseFirestore.getInstance().collection("users").document(email).delete();
                                                Toast.makeText(getContext(), "User deleted!", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(getActivity(), LoginActivity.class);
                                                startActivity(intent);
                                            }
                                        })
                                        .setNegativeButton(android.R.string.no, null)
                                        .show();
                            }
                        });
                    }
                }
            }
        });

        logout = (Button) view.findViewById(R.id.btn_logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //double checking if the user wants to logout
                new AlertDialog.Builder(getContext())
                        .setTitle("Logout?")
                        .setMessage("Are you sure you want to logout?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                //removing user login information
                                getContext().getSharedPreferences("shared", 0).edit().clear().commit();

                                //sending the user back to the login page
                                Intent intent = new Intent(getActivity(), LoginActivity.class);
                                startActivity(intent);
                                ((Activity) getActivity()).overridePendingTransition(0, 0);
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .show();
            }
        });

        return view;
    }
}
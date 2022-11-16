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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.celiao.app.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//this page lets users make their booking
public class BookingFragment extends Fragment {
    Spinner vehicles, duration, drivers;
    EditText address;
    Button button_confirm;
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_customer_booking, container, false);

        vehicles = (Spinner) view.findViewById(R.id.spinner_vehicles);
        duration = (Spinner) view.findViewById(R.id.spinner_duration);
        drivers = (Spinner) view.findViewById(R.id.spinner_driver);
        address = (EditText) view.findViewById(R.id.address);
        button_confirm = (Button) view.findViewById(R.id.btn_confirm);

        String vehicle = getArguments().getString("vehicle");

        //creating an array for the vehicle spinner
        firestore.collection("vehicles")
            .get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        List<String> vehiclesList = new ArrayList<String>();

                        //adds all available vehicles to a list
                        for (QueryDocumentSnapshot document : task.getResult()) {

                            //gets the available vehicles
                            if(document.getString("isAvailable").equals("true")){
                                vehiclesList.add(document.getString("vehicleName"));
                            }
                        }

                        String[] vehiclesArray = new String[vehiclesList.size()];
                        vehiclesList.toArray(vehiclesArray);

                        //pushes the array to the spinner
                        ArrayAdapter vehiclesAdapter = new ArrayAdapter(getContext(), androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, vehiclesArray);
                        vehicles.setAdapter(vehiclesAdapter);

                        //for bookings through view vehicles activity
                        for(int i = 0; i < vehiclesList.size(); i++){
                            if(vehiclesList.get(i).equals(vehicle)){
                                vehicles.setSelection(i);
                            }
                        }
                    }
                }
            });

        //creating an array for the driver spinner
        firestore.collection("users")
            .get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        List<String> driverList = new ArrayList<String>();

                        //adds all available vehicles to a list
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if(document.getString("role").equals("Driver")) {
                                //gets the available vehicles
                                if (document.getString("isAvailable").equals("true")) {
                                    driverList.add(document.getString("email"));
                                }
                            }
                        }

                        String[] driversArray = new String[driverList.size()];
                        driverList.toArray(driversArray);

                        //pushes the array to the spinner
                        ArrayAdapter driverAdapter = new ArrayAdapter(getContext(), androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, driversArray);
                        drivers.setAdapter(driverAdapter);
                    }
                }
            });

        //array for the duration spinner
        String[] durationArray = {"1 day", "1 week", "2 weeks", "1 month", "3 months"};
        ArrayAdapter durationAdapter = new ArrayAdapter(getContext(), androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, durationArray);
        duration.setAdapter(durationAdapter);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        button_confirm.setOnClickListener(new View.OnClickListener(){
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view){

                //form validation
                if(address.getText().toString().equals("")){
                    Toast.makeText(getContext(), "Enter an address", Toast.LENGTH_SHORT).show();
                    return;
                }

                //calculating end date
                LocalDate date = LocalDate.now();
                switch (duration.getSelectedItem().toString()){
                    case "1 day":
                        date = LocalDate.now().plusDays(1);
                        break;
                    case "1 week":
                        date = LocalDate.now().plusDays(7);
                        break;
                    case "2 weeks":
                        date = LocalDate.now().plusDays(14);
                        break;
                    case "1 month":
                        date = LocalDate.now().plusDays(30);
                        break;
                    case "3 months":
                        date = LocalDate.now().plusDays(90);
                        break;
                }

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

                String email = getArguments().getString("email");

                //creates a booking hashmap
                Map<String, Object> booking = new HashMap<>();
                booking.put("email", email);
                booking.put("vehicle", vehicles.getSelectedItem().toString());
                booking.put("driver", drivers.getSelectedItem().toString());
                booking.put("duration", duration.getSelectedItem().toString());
                booking.put("address", address.getText().toString());
                booking.put("bookedDate", formatter.format(LocalDate.now()));
                booking.put("endDate", formatter.format(date));

                //creates the booking
                firestore.collection("bookings").document(email).set(booking)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {

                            //changing the vehicle and driver availability to false
                            Map<String, Object> temp = new HashMap<>();
                            temp.put("isAvailable", "false");
                            firestore.collection("vehicles").document(vehicles.getSelectedItem().toString()).update(temp);
                            temp.put("booking", email);
                            firestore.collection("users").document(drivers.getSelectedItem().toString()).update(temp);

                            Toast.makeText(getContext(), "Your vehicle has been booked!", Toast.LENGTH_LONG).show();

                            //takes the user to the booking details page
                            Intent intent = new Intent(getContext(), BookingDetailsActivity.class);
                            intent.putExtra("email", email);
                            startActivity(intent);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getContext(), "Failed to book, please try again.", Toast.LENGTH_LONG).show();
                        }
                    });
            }
        });
    }
}
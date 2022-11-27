package com.celiao.app.customer;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.celiao.app.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

//this page lets users make their booking
public class BookingFragment extends Fragment {
    Spinner vehicles, duration, drivers;
    EditText address;
    ImageView button_confirm;
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_customer_booking, container, false);

        vehicles = (Spinner) view.findViewById(R.id.spinner_vehicles);
        duration = (Spinner) view.findViewById(R.id.spinner_duration);
        drivers = (Spinner) view.findViewById(R.id.spinner_driver);
        address = (EditText) view.findViewById(R.id.address);
        button_confirm = (ImageView) view.findViewById(R.id.btn_confirm);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

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

                            if(vehiclesList.size() == 0){
                                vehicles.setEnabled(false);
                                vehicles.setClickable(false);

                                vehiclesList.add("No Vehicles Available");
                                String[] vehiclesArray = new String[vehiclesList.size()];
                                vehiclesList.toArray(vehiclesArray);

                                //pushes the array to the spinner
                                ArrayAdapter vehiclesAdapter = new ArrayAdapter(getContext(), androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, vehiclesArray);

                                vehicles.setAdapter(vehiclesAdapter);
                            } else {
                                vehiclesList.add("Vehicle");
                                String[] vehiclesArray = new String[vehiclesList.size()];
                                vehiclesList.toArray(vehiclesArray);
                                //pushes the array to the spinner
                                ArrayAdapter vehiclesAdapter = new ArrayAdapter(getContext(), androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, vehiclesArray){
                                    @Override
                                    public int getCount() {
                                        return vehiclesArray.length-1;
                                    }
                                };

                                vehicles.setAdapter(vehiclesAdapter);
                                vehicles.setSelection(vehiclesArray.length-1);
                            }

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

                            if(driverList.size() == 0){
                                drivers.setEnabled(false);
                                drivers.setClickable(false);

                                driverList.add("No Drivers Available");
                                String[] driversArray = new String[driverList.size()];
                                driverList.toArray(driversArray);

                                //pushes the array to the spinner
                                ArrayAdapter driversAdapter = new ArrayAdapter(getContext(), androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, driversArray);

                                drivers.setAdapter(driversAdapter);
                            } else {
                                driverList.add("Driver");
                                String[] driversArray = new String[driverList.size()];
                                driverList.toArray(driversArray);
                                //pushes the array to the spinner
                                ArrayAdapter driverAdapter = new ArrayAdapter(getContext(), androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, driversArray){
                                    @Override
                                    public int getCount() {
                                        return driversArray.length-1;
                                    }
                                };

                                drivers.setAdapter(driverAdapter);
                                drivers.setSelection(driversArray.length-1);
                            }
                        }
                    }
                });

        //array for the duration spinner
        String[] durationArray = {"1 day", "1 week", "2 weeks", "1 month", "3 months", "Duration"};
        ArrayAdapter durationAdapter = new ArrayAdapter(getContext(), androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, durationArray){
            @Override
            public int getCount() {
                return durationArray.length-1;
            }
        };
        duration.setAdapter(durationAdapter);
        duration.setSelection(durationArray.length-1);

        button_confirm.setOnClickListener(new View.OnClickListener(){
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view){

                //form validation
                if(vehicles.getSelectedItem().toString().equals("Vehicle")){
                    Toast.makeText(getContext(), "Choose a vehicle", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(vehicles.getSelectedItem().toString().equals("No Vehicles Available")){
                    Toast.makeText(getContext(), "No Vehicles Available...Please try again later", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(duration.getSelectedItem().toString().equals("Duration")){
                    Toast.makeText(getContext(), "Choose a duration", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(drivers.getSelectedItem().toString().equals("Driver")){
                    Toast.makeText(getContext(), "Choose a driver", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(vehicles.getSelectedItem().toString().equals("No Drivers Available")){
                    Toast.makeText(getContext(), "No Drivers Available...Please try again later", Toast.LENGTH_SHORT).show();
                    return;
                }

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

                            firestore.collection("users").document(email).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if(task.isComplete()){
                                        DocumentSnapshot doc = task.getResult();
                                        //sending a confirmation email through an email
                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                try  {
                                                    String to = email;
                                                    String from = "celiaootp@gmail.com";
                                                    String host = "smtp.gmail.com";
                                                    Properties properties = System.getProperties();
                                                    properties.put("mail.smtp.host", host);
                                                    properties.put("mail.smtp.port", "465");
                                                    properties.put("mail.smtp.ssl.enable", "true");
                                                    properties.put("mail.smtp.auth", "true");
                                                    Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
                                                        protected PasswordAuthentication getPasswordAuthentication() {
                                                            return new PasswordAuthentication( from, "fohxqqzczvmuyvht");
                                                        }
                                                    });
                                                    session.setDebug(true);

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

                                                    try {
                                                        MimeMessage message = new MimeMessage(session);
                                                        message.setFrom(new InternetAddress(from));
                                                        message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
                                                        message.setSubject("Your Booking from Celiao Has Being Confirmed!");
                                                        message.setContent("Dear<b> " + doc.getString("fullName") +
                                                                        " </b>,<p>Your booking has been confirmed</p> " +
                                                                        "<p>Address: " + address.getText().toString() +
                                                                        "</p><p>Duration: " + duration.getSelectedItem().toString() +
                                                                        "</p><p>End date: " + formatter.format(date) +
                                                                        "</p><p>Driver: " + drivers.getSelectedItem().toString() +
                                                                        "</p><p>Vehicle: " + vehicles.getSelectedItem().toString() +
                                                                        "</p>"+
                                                                        "<p>Thank You!</p>",
                                                                "text/html;  charset=\"utf-8\"");
                                                        Transport.send(message);
                                                    } catch (MessagingException mex) {
                                                        Toast.makeText(getContext(), "Error, Please try again later.", Toast.LENGTH_SHORT).show();
                                                    }
                                                } catch (Exception e) {
                                                    Toast.makeText(getContext(), "Error, Please try again later.", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        }).start();
                                    }
                                }
                            });

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
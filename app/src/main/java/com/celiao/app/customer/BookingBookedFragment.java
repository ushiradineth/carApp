package com.celiao.app.customer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.celiao.app.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

//this page shows bookings details to a booked user
public class BookingBookedFragment extends Fragment {
    TextView textView_address, textView_duration, textView_endDate, textView_vehicleNo, textView_model, textView_category, textView_price, textView_description, textView_bookedDate, textView_driver;
    ImageView imageView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_customer_booking_booked, container, false);
        textView_address = (TextView) view.findViewById(R.id.textView_Address);
        textView_duration = (TextView) view.findViewById(R.id.textView_Duration);
        textView_endDate = (TextView) view.findViewById(R.id.textView_EndDate);
        textView_vehicleNo = (TextView) view.findViewById(R.id.textView_VehicleNo);
        textView_model = (TextView) view.findViewById(R.id.textView_Model);
        textView_category = (TextView) view.findViewById(R.id.textView_Category);
        textView_price = (TextView) view.findViewById(R.id.textView_Price);
        textView_description = (TextView) view.findViewById(R.id.textView_Description);
        textView_bookedDate = (TextView) view.findViewById(R.id.textView_BookedDate);
        textView_driver = (TextView) view.findViewById(R.id.textView_Driver);
        imageView = (ImageView) view.findViewById(R.id.imageView2);

        String email = getArguments().getString("email");

        //getting duration and vehicleno from booking database
        FirebaseFirestore.getInstance().collection("bookings").document(email).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        textView_vehicleNo.setText(doc.getString("vehicle"));
                        textView_duration.setText(doc.getString("duration"));
                        textView_bookedDate.setText(doc.getString("bookedDate"));
                        textView_endDate.setText(doc.getString("endDate"));
                        textView_address.setText(doc.getString("address"));
                        textView_driver.setText(doc.getString("driver"));

                        //getting vehicle details
                        FirebaseFirestore.getInstance().collection("vehicles").document(doc.getString("vehicle")).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if(task.isSuccessful()){
                                    DocumentSnapshot doc = task.getResult();
                                    if (doc.exists()) {
                                        textView_model.setText(doc.getString("model"));
                                        textView_category.setText(doc.getString("category"));
                                        textView_price.setText(doc.getString("price"));
                                        textView_description.setText(doc.getString("description"));

                                        //getting image from the storage bucket
                                        FirebaseStorage.getInstance().getReference().child("images/"+doc.getId()).getBytes(1024 * 1024).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                            @Override
                                            public void onSuccess(byte[] bytes) {
                                                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                                imageView.setImageBitmap(bitmap);
                                            }
                                        });
                                    }
                                }
                            }
                        });
                    }
                }
            }
        });

        return view;
    }
}
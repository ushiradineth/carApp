package com.celiao.app.customer;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.celiao.app.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

//this page shows booking details to a user after they complete their booking
public class BookingDetailsActivity extends AppCompatActivity {
    TextView textView_address, textView_duration, textView_endDate, textView_vehicleNo, textView_model, textView_category, textView_price, textView_description, textView_driver;
    ImageView imageView;
    Button exit;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_booking_details);

        textView_address = (TextView) findViewById(R.id.textView_Address);
        textView_duration = (TextView) findViewById(R.id.textView_Duration);
        textView_endDate = (TextView) findViewById(R.id.textView_EndDate);
        textView_vehicleNo = (TextView) findViewById(R.id.textView_VehicleNo);
        textView_model = (TextView) findViewById(R.id.textView_Model);
        textView_category = (TextView) findViewById(R.id.textView_Category);
        textView_price = (TextView) findViewById(R.id.textView_Price);
        textView_description = (TextView) findViewById(R.id.textView_Description);
        textView_driver = (TextView) findViewById(R.id.textView_Driver);
        imageView = (ImageView) findViewById(R.id.imageView2);

        Intent intent = getIntent();
        String email = intent.getStringExtra("email");

        //getting booking details from booking database
        FirebaseFirestore.getInstance().collection("bookings").document(email).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        textView_vehicleNo.setText(doc.getString("vehicle"));
                        textView_duration.setText(doc.getString("duration"));
                        textView_endDate.setText(doc.getString("endDate"));
                        textView_address.setText(doc.getString("address"));
                        textView_driver.setText(doc.getString("driver"));

                        //getting vehicle details from vehicle database
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

        //exit button to return the the main menu
        exit = (Button) findViewById(R.id.btn_exit);
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                intent.putExtra("email", email);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(false);
        Intent intenta = getIntent();
        String email = intenta.getStringExtra("email");
        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
        intent.putExtra("email", email);
        startActivity(intent);
    }
}
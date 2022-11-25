package com.celiao.app.customer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

//this page shows users the specific vehicle details that was clicked on
public class ViewVehiclesActivityVehicle extends AppCompatActivity {
    TextView textView_vehicleNo, textView_model, textView_category, textView_price, textView_description, textView_available;
    ImageView imageView,booknow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_view_vehicles_vehicle);

        textView_vehicleNo = (TextView) findViewById(R.id.textView_VehicleNo);
        textView_model = (TextView) findViewById(R.id.textView_Model);
        textView_category = (TextView) findViewById(R.id.textView_Category);
        textView_price = (TextView) findViewById(R.id.textView_Price);
        textView_description = (TextView) findViewById(R.id.textView_Description);
        textView_available = (TextView) findViewById(R.id.textView_availability);
        imageView = (ImageView) findViewById(R.id.imageView);
        booknow = (ImageView) findViewById(R.id.booknow);

        Intent intent = getIntent();
        String vehicle = intent.getStringExtra("vehicle");
        String email = intent.getStringExtra("email");

        //getting vehicle details
        FirebaseFirestore.getInstance().collection("vehicles").document(vehicle).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {

                        //sets vehicle details
                        textView_vehicleNo.setText(doc.getString("vehicleName"));
                        textView_model.setText(doc.getString("model"));
                        textView_category.setText(doc.getString("category"));
                        textView_price.setText(doc.getString("price"));
                        textView_description.setText(doc.getString("description"));
                        textView_available.setText(doc.getString("isAvailable"));

                        //gets the vehicle image
                        FirebaseStorage.getInstance().getReference().child("images/"+doc.getString("vehicleName")).getBytes(1024 * 1024).addOnSuccessListener(new OnSuccessListener<byte[]>() {
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

        booknow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                intent.putExtra("vehicle", vehicle);
                intent.putExtra("email", email);
                startActivity(intent);
            }
        });
    }
}
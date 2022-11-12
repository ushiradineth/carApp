package com.celiao.app.customer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.celiao.app.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

//this page shows a list of vehicles to the customer
public class ViewVehiclesActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_view_vehicles);

        //getting vehicle details
        FirebaseFirestore.getInstance().collection("vehicles")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<String> vehicleNo = new ArrayList<>();
                            List<String> category = new ArrayList<>();
                            List<String> model = new ArrayList<>();
                            List<String> price = new ArrayList<>();
                            List<String> description = new ArrayList<>();

                            //setting vehicle details into lists
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if(document.getString("isAvailable").equals("true")) {
                                    vehicleNo.add(document.getString("vehicleName"));
                                    category.add(document.getString("category"));
                                    model.add(document.getString("model"));
                                    price.add(document.getString("price"));
                                    description.add(document.getString("description"));
                                }
                            }

                            //converting list to arrays
                            String[] vehicleNoArray = new String[vehicleNo.size()];
                            String[] categoryArray = new String[vehicleNo.size()];
                            String[] modelArray = new String[vehicleNo.size()];
                            String[] priceArray = new String[vehicleNo.size()];
                            String[] descriptionArray = new String[vehicleNo.size()];

                            vehicleNo.toArray(vehicleNoArray);
                            category.toArray(categoryArray);
                            model.toArray(modelArray);
                            price.toArray(priceArray);
                            description.toArray(descriptionArray);

                            //sets the list custom adapter to show the list of vehicles
                            ListView listView = findViewById(R.id.viewVehiclesList);
                            ViewVehiclesActivityCustomBaseAdapter viewVehiclesActivityCustomBaseAdapter = new ViewVehiclesActivityCustomBaseAdapter(getApplicationContext(), vehicleNoArray, modelArray, categoryArray, priceArray, descriptionArray);
                            listView.setAdapter(viewVehiclesActivityCustomBaseAdapter);

                            //takes user to a specific vehicle when clicked
                            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                    Intent emailintent = getIntent();
                                    String email = emailintent.getStringExtra("email");
                                    Intent intent = new Intent(getApplicationContext(), ViewVehiclesActivityVehicle.class);
                                    intent.putExtra("vehicle", vehicleNo.get(i));
                                    intent.putExtra("email", email);
                                    startActivity(intent);
                                }
                            });
                        }
                    }
                });
    }

    @Override
    public void onRestart() {
        super.onRestart();

        //refreshing content when the page is revisited
        finish();
        startActivity(getIntent());
    }
}
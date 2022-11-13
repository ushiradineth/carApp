package com.celiao.app.vehicleOwner;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.celiao.app.LoginActivity;
import com.celiao.app.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

//this page lets vehicle owners add new vehicles to the database
public class VehicleOwnerAddVehicleActivity extends AppCompatActivity {
    EditText vehicleName, model, description, price, category;
    Button button_addVehicle, button_uploadImage, logout, deleteAccount;
    ImageView imgPreview;
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();
    Uri file = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_owner_add_vehicle);

        Intent intent = getIntent();
        String email = intent.getStringExtra("email");

        vehicleName = (EditText) findViewById(R.id.editText_vehicleName);
        model = (EditText) findViewById(R.id.editText_model);
        description = (EditText) findViewById(R.id.editText_description);
        price = (EditText) findViewById(R.id.editText_Price);
        category = (EditText) findViewById(R.id.editText_category);
        imgPreview = (ImageView) findViewById(R.id.image_view);
        button_addVehicle = (Button) findViewById(R.id.btn_addNewVehicle);
        button_uploadImage = (Button) findViewById(R.id.btn_addImage);
        button_uploadImage.setOnClickListener(v -> setContent.launch("image/*"));

        logout = (Button) findViewById(R.id.button_logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(view.getContext())
                    .setTitle("Logout?")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            //removing user login information
                            view.getContext().getSharedPreferences("shared", 0).edit().clear().commit();

                            //sending the user back to the login page
                            Intent intent = new Intent(view.getContext(), LoginActivity.class);
                            startActivity(intent);
                            ((Activity) view.getContext()).overridePendingTransition(0, 0);
                        }
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .show();
            }
        });

        deleteAccount = (Button) findViewById(R.id.button_delete);
        deleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getApplicationContext().getSharedPreferences("shared", 0).edit().clear().commit();

                FirebaseFirestore.getInstance().collection("users").document(email).delete();
                Toast.makeText(getApplicationContext(), "User deleted!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    //for image upload
    ActivityResultLauncher<String> setContent = registerForActivityResult(
        new ActivityResultContracts.GetContent(),
        new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri result) {
                if (result != null) {
                    imgPreview.setImageURI(result);
                    file  = result;
                }
            }
        });

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = getIntent();
        String email = intent.getStringExtra("email");

        button_addVehicle.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                //form validation
                if(vehicleName.getText().toString().equals("") || model.getText().toString().equals("") || description.getText().toString().equals("") || price.getText().toString().equals("") || category.getText().toString().equals("")){
                    Toast.makeText(getApplicationContext(),"Enter all the details", Toast.LENGTH_LONG).show();
                    return;
                }

                if(file == null){
                    Toast.makeText(getApplicationContext(),"Upload an image", Toast.LENGTH_LONG).show();
                    return;
                }

                //updates the database with the new records if the conditions are met
                firestore.collection("vehicles").document(vehicleName.getText().toString()).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    Toast.makeText(getApplicationContext(), "Vehicle already exists", Toast.LENGTH_LONG).show();
                                } else {
                                    //creates a vehicle hashmap
                                    Map<String, Object> vehicle = new HashMap<>();
                                    vehicle.put("vehicleName", vehicleName.getText().toString());
                                    vehicle.put("model", model.getText().toString());
                                    vehicle.put("description", description.getText().toString());
                                    vehicle.put("price", price.getText().toString());
                                    vehicle.put("category", category.getText().toString());
                                    vehicle.put("isAvailable", "true");
                                    vehicle.put("owner", email);

                                    //updates the database
                                    firestore.collection("vehicles").document(vehicleName.getText().toString()).set(vehicle)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                //uploads the image
                                                StorageReference uploadRef = storageRef.child("images/"+vehicleName.getText().toString());
                                                UploadTask uploadTask = uploadRef.putFile(file);

                                                Map<String, Object> temp = new HashMap<>();
                                                temp.put("vehicle", vehicleName.getText().toString());

                                                FirebaseFirestore.getInstance().collection("users").document(email).set(temp, SetOptions.merge());

                                                Toast.makeText(getApplicationContext(), "Vehicle Added!", Toast.LENGTH_LONG).show();
                                                Intent intent = new Intent(getApplicationContext(), VehicleOwnerAddedActivity.class);
                                                intent.putExtra("email", email);
                                                startActivity(intent);
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(getApplicationContext(), "Failed to add vehicle, please try again.", Toast.LENGTH_LONG).show();
                                            }
                                        });
                                }
                            }
                        }
                    });
            }
        });
    }

    @Override
    public void onBackPressed() {
        this.moveTaskToBack(true);
    }
}
package com.celiao.app;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

//this is the sign up page for users
public class SignUpActivity extends AppCompatActivity {
    EditText fullName, email, mobileNo, password, confirmPassword;
    Spinner role;
    Button button_signUp, button_signIn;
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        fullName = (EditText) findViewById(R.id.editText_FullName);
        email = (EditText) findViewById(R.id.editText_Email);
        mobileNo = (EditText) findViewById(R.id.editText_MobileNo);
        password = (EditText) findViewById(R.id.editText_password);
        confirmPassword = (EditText) findViewById(R.id.editText_ConfirmPassword);
        role = (Spinner) findViewById(R.id.spinner_role);

        button_signUp = (Button) findViewById(R.id.btnSignup);
        button_signIn = (Button) findViewById(R.id.btnSigninref);

        //array for the role spinner
        String[] roleArray = {"Customer", "Driver", "Vehicle owner"};
        ArrayAdapter durationAdapter = new ArrayAdapter(getApplicationContext(), androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, roleArray);
        role.setAdapter(durationAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        button_signUp.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){

                //form validation
                if(fullName.getText().toString().equals("")){
                    Toast.makeText(getApplicationContext(), "Enter Full name", Toast.LENGTH_LONG).show();
                    return;
                }

                if(email.getText().toString().equals("")){
                    Toast.makeText(getApplicationContext(), "Enter Email Address", Toast.LENGTH_LONG).show();
                    return;
                } else {

                    //regex to check if the email is valid
                    String regexPattern = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";

                    if(!Pattern.compile(regexPattern).matcher(email.getText().toString()).matches()){
                        Toast.makeText(getApplicationContext(), "Enter a valid Email Address", Toast.LENGTH_LONG).show();
                        return;
                    }
                }

                if(mobileNo.getText().toString().equals("")){
                    Toast.makeText(getApplicationContext(), "Enter Mobile Number", Toast.LENGTH_LONG).show();
                    return;
                } else {

                    //regex to check if the number is valid
                    String regexPattern = "^\\d{10}$";
                    if(!Pattern.compile(regexPattern).matcher(mobileNo.getText().toString()).matches()){
                        Toast.makeText(getApplicationContext(), "Enter a valid Mobile Number", Toast.LENGTH_LONG).show();
                        return;
                    }
                }

                if(password.getText().toString().equals("")){
                    Toast.makeText(getApplicationContext(), "Enter Password", Toast.LENGTH_LONG).show();
                    return;
                } else {

                    //regex to check if the password is valid
                    String regexPattern = "^(?=.*[0-9])(?=.*[a-z])(?=\\S+$).{8,20}$";

                    if(!Pattern.compile(regexPattern).matcher(password.getText().toString()).matches()){
                        Toast.makeText(getApplicationContext(), "Enter a valid password (8-20 characters with at least one digit)", Toast.LENGTH_LONG).show();
                        return;
                    } else {
                        if(!password.getText().toString().equals(confirmPassword.getText().toString())){
                            Toast.makeText(getApplicationContext(), "Password does not match", Toast.LENGTH_LONG).show();
                            return;
                        }
                    }
                }

                //creating the user record in database
                firestore.collection("users").document(email.getText().toString()).get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @RequiresApi(api = Build.VERSION_CODES.O)
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();

                                    //checking if the user already exists
                                    if (document.exists()) {
                                        Toast.makeText(getApplicationContext(), "Email is already linked to a registered account", Toast.LENGTH_LONG).show();

                                    } else {

                                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

                                        //creating user hashmap
                                        Map<String, Object> user = new HashMap<>();
                                        user.put("fullName", fullName.getText().toString());
                                        user.put("email", email.getText().toString());
                                        user.put("mobileNo", mobileNo.getText().toString());
                                        user.put("password", password.getText().toString());
                                        user.put("registeredDate", formatter.format(LocalDate.now()));
                                        user.put("role", role.getSelectedItem().toString());

                                        //creating the user
                                        firestore.collection("users").document(email.getText().toString()).set(user)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        Toast.makeText(getApplicationContext(), "You have been registered!", Toast.LENGTH_LONG).show();
                                                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                                        intent.putExtra("email", email.getText().toString());
                                                        startActivity(intent);
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(getApplicationContext(), "Failed to register, please try again.", Toast.LENGTH_LONG).show();
                                                    }
                                                });
                                    }
                                }
                            }
                        });
//
            }
        });

        button_signIn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            }
        });
    }
}
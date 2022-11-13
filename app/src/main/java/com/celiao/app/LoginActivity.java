package com.celiao.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import com.celiao.app.customer.HomeActivity;
import com.celiao.app.driver.DriverHomeActivity;
import com.celiao.app.vehicleOwner.VehicleOwnerActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.regex.Pattern;

//this is the first page a user sees when they login
public class LoginActivity extends AppCompatActivity {
    EditText email, password;
    Button button_signIn, Button_signUp;
    CheckBox button_remember;
    SharedPreferences shared;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //checking if the user has already logged in
        shared = getSharedPreferences("shared", MODE_PRIVATE);
        if(shared.contains("email")){
            //making sure that the admin dint delete the user
            FirebaseFirestore.getInstance().collection("users").document(shared.getString("email", "defaultStringIfNothingFound")).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot doc = task.getResult();
                        if (doc.exists()) {
                            switch (doc.getString("role")){
                                case "Customer":
                                    Intent Customer = new Intent(getApplicationContext(), HomeActivity.class);
                                    Customer.putExtra("email", shared.getString("email", "defaultStringIfNothingFound"));
                                    startActivity(Customer);
                                    break;
                                case "Driver":
                                    Intent Driver = new Intent(getApplicationContext(), DriverHomeActivity.class);
                                    Driver.putExtra("email", shared.getString("email", "defaultStringIfNothingFound"));
                                    startActivity(Driver);
                                    break;
                                case "Vehicle owner":
                                    Intent owner = new Intent(getApplicationContext(), VehicleOwnerActivity.class);
                                    owner.putExtra("email", shared.getString("email", "defaultStringIfNothingFound"));
                                    startActivity(owner);
                                    break;
                                default:
                                    Toast.makeText(getApplicationContext(),"Role doesn't exist", Toast.LENGTH_SHORT).show();
                                    break;
                            }
                        }
                        else {
                            Toast.makeText(getApplicationContext(),"Your account has being deleted by the Admin.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        }

        setContentView(R.layout.activity_login);

        email = (EditText) findViewById(R.id.editText_LoginEmail);
        password = (EditText) findViewById(R.id.editText_Password);
        button_signIn = (Button) findViewById(R.id.btn_Signin);
        Button_signUp = (Button) findViewById(R.id.btn_Signupref);
        button_remember = (CheckBox) findViewById(R.id.btn_rememberme);

        Intent intent = getIntent();
        String emailIntent = intent.getStringExtra("email");
        if(emailIntent != null){
            email.setText(emailIntent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        button_signIn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){

                //form validation
                if(email.getText().toString().equals("") || password.getText().toString().equals("")){
                    Toast.makeText(getApplicationContext(),"Enter username and password", Toast.LENGTH_LONG).show();
                    return;
                } else {
                    //regex to validate email
                    String regexPattern = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
                    if(!Pattern.compile(regexPattern).matcher(email.getText().toString()).matches()){
                        Toast.makeText(getApplicationContext(), "Enter a valid Email Address", Toast.LENGTH_LONG).show();
                        return;
                    }
                }

                //getting user password
                FirebaseFirestore.getInstance().collection("users").document(email.getText().toString()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            DocumentSnapshot doc = task.getResult();
                            if (doc.exists()) {
                                //checking if the password is correct
                                if(password.getText().toString().equals(String.valueOf(doc.get("password")))){
                                    //saving user state if the checkbox is checked
                                    if(button_remember.isChecked()){
                                        SharedPreferences shared = getSharedPreferences("shared", MODE_PRIVATE);
                                        SharedPreferences.Editor editor = shared.edit();
                                        editor.putString("email", email.getText().toString());
                                        editor.commit();
                                    }

                                    switch (doc.getString("role")){
                                        case "Customer":
                                            Intent Customer = new Intent(getApplicationContext(), HomeActivity.class);
                                            Customer.putExtra("email", email.getText().toString());
                                            startActivity(Customer);
                                            Toast.makeText(getApplicationContext(), "Logged in!", Toast.LENGTH_LONG).show();
                                            break;
                                        case "Driver":
                                            Intent Driver = new Intent(getApplicationContext(), DriverHomeActivity.class);
                                            Driver.putExtra("email", email.getText().toString());
                                            startActivity(Driver);
                                            Toast.makeText(getApplicationContext(), "Logged in!", Toast.LENGTH_LONG).show();
                                            break;
                                        case "Vehicle owner":
                                            Intent owner = new Intent(getApplicationContext(), VehicleOwnerActivity.class);
                                            owner.putExtra("email", email.getText().toString());
                                            startActivity(owner);
                                            Toast.makeText(getApplicationContext(), "Logged in!", Toast.LENGTH_LONG).show();
                                            break;
                                        default:
                                            Toast.makeText(getApplicationContext(),"Role doesn't exist", Toast.LENGTH_SHORT).show();
                                            break;
                                    }
                                } else {
                                    Toast.makeText(getApplicationContext(), "Wrong password", Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(getApplicationContext(), "User is not registered", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Error with database, please try again later", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });

        Button_signUp.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        this.moveTaskToBack(true);
    }
}
package com.celiao.app;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import org.w3c.dom.Document;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

//this is the first page a user sees when they login
public class LoginActivity extends AppCompatActivity {
    EditText email, password;
    Button button_signIn, Button_signUp;
    CheckBox button_remember;
    SharedPreferences shared;
    String otp = "";

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
                            login(doc, shared.getString("email", "defaultStringIfNothingFound"));
                        }
                        else {
                            getApplicationContext().getSharedPreferences("shared", 0).edit().clear().commit();
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
                                otp = "";

                                //checking if the password is correct
                                if(password.getText().toString().equals(String.valueOf(doc.get("password")))){

                                    if(button_remember.isChecked()){
                                        SharedPreferences shared = getSharedPreferences("shared", MODE_PRIVATE);
                                        SharedPreferences.Editor editor = shared.edit();
                                        editor.putString("email", email.getText().toString());
                                        editor.commit();
                                    }

                                    login(doc, email.getText().toString());

//                                    //creating otp
//                                    for (int i = 0; i < 6; i++) {
//                                        otp+=(int)(Math.random() * 9);
//                                    }
//
//                                    //sending the otp code through an email
//                                    new Thread(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            try  {
//                                                String to = email.getText().toString();
//                                                String from = "celiaootp@gmail.com";
//                                                String host = "smtp.gmail.com";
//                                                Properties properties = System.getProperties();
//                                                properties.put("mail.smtp.host", host);
//                                                properties.put("mail.smtp.port", "465");
//                                                properties.put("mail.smtp.ssl.enable", "true");
//                                                properties.put("mail.smtp.auth", "true");
//                                                Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
//                                                    protected PasswordAuthentication getPasswordAuthentication() {
//                                                        return new PasswordAuthentication( from, "fohxqqzczvmuyvht");
//                                                    }
//                                                });
//                                                session.setDebug(true);
//                                                try {
//                                                    MimeMessage message = new MimeMessage(session);
//                                                    message.setFrom(new InternetAddress(from));
//                                                    message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
//                                                    message.setSubject("Your OTP Code for Celiao Car App!");
//                                                    message.setText("Your Code is: " + otp);
//                                                    Transport.send(message);
//                                                } catch (MessagingException mex) {
//                                                    Toast.makeText(LoginActivity.this, "Error, Please try again later.", Toast.LENGTH_SHORT).show();
//                                                }
//                                            } catch (Exception e) {
//                                                Toast.makeText(LoginActivity.this, "Error, Please try again later.", Toast.LENGTH_SHORT).show();
//                                            }
//                                        }
//                                    }).start();
//
//                                    //alert box to take otp input
//                                    EditText otpEditText = new EditText(view.getContext());
//                                    new AlertDialog.Builder(view.getContext())
//                                        .setTitle("Two Factor Authentication")
//                                        .setMessage("Check your email for the code!")
//                                        .setView(otpEditText)
//                                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
//                                            public void onClick(DialogInterface dialog, int which) {
//                                                if(otpEditText.getText().toString().equals(otp)){
//                                                    //saving user state if the checkbox is checked
//                                                    if(button_remember.isChecked()){
//                                                        SharedPreferences shared = getSharedPreferences("shared", MODE_PRIVATE);
//                                                        SharedPreferences.Editor editor = shared.edit();
//                                                        editor.putString("email", email.getText().toString());
//                                                        editor.commit();
//                                                    }
//                                                    login(doc, email.getText().toString());
//                                                } else {
//                                                    otp = "";
//                                                    Toast.makeText(LoginActivity.this, "Incorrect otp, Try again", Toast.LENGTH_SHORT).show();
//                                                }
//                                            }
//                                        })
//                                        .setNegativeButton("Cancel", null)
//                                        .show();
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

    public void login(DocumentSnapshot doc, String email) {
        switch (doc.getString("role")){
            case "Customer":
                FirebaseFirestore.getInstance().collection("bookings").document(email).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                if (document.getString("email") != null) {
                                    dbUpdate(document);
                                }
                            }
                        }
                    }
                });
                Intent Customer = new Intent(getApplicationContext(), HomeActivity.class);
                Customer.putExtra("email", email);
                startActivity(Customer);
                break;
            case "Driver":
                FirebaseFirestore.getInstance().collection("users").document(email).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot doc1 = task.getResult();
                            if (doc1.exists()) {
                                if(doc1.getString("booking") != null){
                                    FirebaseFirestore.getInstance().collection("bookings").document(doc1.getString("booking")).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                DocumentSnapshot document = task.getResult();
                                                if (document.exists()) {
                                                    if (document.getString("email") != null) {
                                                        dbUpdate(document);
                                                    }
                                                }
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    }
                });
                Intent Driver = new Intent(getApplicationContext(), DriverHomeActivity.class);
                Driver.putExtra("email", email);
                startActivity(Driver);
                break;
            case "Vehicle owner":
                FirebaseFirestore.getInstance().collection("users").document(email).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot doc1 = task.getResult();
                            if (doc1.exists()) {
                                FirebaseFirestore.getInstance().collection("bookings").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                if (document.getString("vehicle") != null) {
                                                    if(document.getString("vehicle").equals(doc1.getString("vehicle"))){
                                                        dbUpdate(document);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                });
                            }
                        }
                    }
                });
                Intent owner = new Intent(getApplicationContext(), VehicleOwnerActivity.class);
                owner.putExtra("email", email);
                startActivity(owner);
                break;
            default:
                Toast.makeText(getApplicationContext(),"Role doesn't exist", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void dbUpdate(DocumentSnapshot document) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        if(document.getString("endDate").compareTo(formatter.format(LocalDate.now())) < 0) {
            Map<String, Object> booking = new HashMap<>();
            booking.put("email", document.getString("email"));
            booking.put("vehicle", document.getString("vehicle"));
            booking.put("driver", document.getString("driver"));
            booking.put("duration", document.getString("duration"));
            booking.put("address", document.getString("address"));
            booking.put("bookedDate", document.getString("bookedDate"));
            booking.put("endDate", document.getString("endDate"));

            FirebaseFirestore.getInstance().collection("bookings").document(document.getString("email")).collection("oldbookings").document(document.getString("endDate").split("/")[0]+document.getString("endDate").split("/")[1]+document.getString("endDate").split("/")[2]).set(booking);

            Map<String, Object> temp = new HashMap<>();
            temp.put("isAvailable", "true");
            FirebaseFirestore.getInstance().collection("vehicles").document(document.getString("vehicle")).update(temp);
            temp.put("booking", FieldValue.delete());
            FirebaseFirestore.getInstance().collection("users").document(document.getString("driver")).set(temp, SetOptions.merge());

            Map<String, Object> empty = new HashMap<>();
            empty.put("email", FieldValue.delete());
            empty.put("vehicle", FieldValue.delete());
            empty.put("driver", FieldValue.delete());
            empty.put("duration", FieldValue.delete());
            empty.put("address", FieldValue.delete());
            empty.put("bookedDate", FieldValue.delete());
            empty.put("endDate", FieldValue.delete());

            FirebaseFirestore.getInstance().collection("bookings").document(document.getString("email")).set(empty, SetOptions.merge());

            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
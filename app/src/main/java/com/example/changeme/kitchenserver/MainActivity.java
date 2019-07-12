package com.example.changeme.kitchenserver;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.changeme.kitchenserver.Common.Common;
import com.example.changeme.kitchenserver.Model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;

import static android.view.View.GONE;

public class MainActivity extends AppCompatActivity {


    Button buttonsignin;
    TextView textviewlgin;
    private RelativeLayout errorlayout;
    private TextView errormessage, errortitle;
    private ImageView errorimage;
    private Button retry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonsignin = (Button) findViewById(R.id.btnSignIn);
        textviewlgin = (TextView) findViewById(R.id.txt);
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/AllerDisplay.ttf");
        errorlayout = findViewById(R.id.errorlayout);
        errormessage = findViewById(R.id.errormessage);
        errortitle = findViewById(R.id.errortitle);
        errormessage = findViewById(R.id.errormessage);
        errorimage = findViewById(R.id.errorimage);
        retry = findViewById(R.id.retry);
        textviewlgin.setTypeface(typeface);
        Paper.init(this);


        buttonsignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signin = new Intent(MainActivity.this, Signin.class);
                startActivity(signin);
            }
        });


        String user = Paper.book().read(Common.USER_KEY);
        String pwd = Paper.book().read(Common.PWD_KEY);
        if (user != null && pwd != null) {
            if (!user.isEmpty() && !pwd.isEmpty()) {
                if (Common.isConnectedToInternet(getBaseContext())) {
                    login(user, pwd);
                } else {
                    showErrorMessage(R.drawable.no_result, "Error", "Check your internet connection");

                }
            }
        }
    }

    private void login(String user, String pwd) {
        final android.app.AlertDialog waitingdialog = new SpotsDialog(MainActivity.this);
        waitingdialog.show();
        waitingdialog.setTitle("Loading...");

        final String localphone = user;
        final String localpassword = pwd;
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference table_user = database.getReference("User");
        table_user.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(localphone).exists()) {

                    waitingdialog.dismiss();
                    User user = dataSnapshot.child(localphone).getValue(User.class);
                    user.setPhone(localphone);
                    if (Boolean.parseBoolean(user.getIsStaff())) {
                        if (user.getPassword().equals(localpassword)) {

                            Intent intent = new Intent(MainActivity.this, Home.class);
                            Common.currentuser = user;
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(MainActivity.this, "Password incorrect", Toast.LENGTH_SHORT).show();

                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Please login with staff account", Toast.LENGTH_SHORT).show();

                    }
                } else {
                    waitingdialog.dismiss();
                    Toast.makeText(MainActivity.this, "User not exist", Toast.LENGTH_SHORT).show();


                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void showErrorMessage(int imageview, String title, String message) {
        if (errorlayout.getVisibility() == GONE) {
            errorlayout.setVisibility(View.VISIBLE);
        }
        errorimage.setImageResource(imageview);
        errortitle.setText(title);
        errormessage.setText(message);

        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Common.isConnectedToInternet(getBaseContext())) {
                    errorlayout.setVisibility(View.GONE);
                    recreate();

                }
            }
        });
    }

}
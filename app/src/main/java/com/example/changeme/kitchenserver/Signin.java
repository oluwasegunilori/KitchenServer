package com.example.changeme.kitchenserver;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.changeme.kitchenserver.Common.Common;
import com.example.changeme.kitchenserver.Model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;

public class Signin extends AppCompatActivity implements Animation.AnimationListener {
    EditText editPhone, editpassword;
    Button signin;

    ImageView imageView;
    Animation animation;
    FirebaseDatabase db;
    DatabaseReference users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        editPhone = (MaterialEditText) findViewById(R.id.Editphone);
        editpassword = (MaterialEditText) findViewById(R.id.EditPassword);
        signin = (Button) findViewById(R.id.btnsignin);

        imageView = findViewById(R.id.errorimage);
        db = FirebaseDatabase.getInstance();
        users = db.getReference("User");


        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Common.isConnectedToInternet(getBaseContext())) {
                    Paper.book().write(Common.USER_KEY, editPhone.getText().toString());
                    Paper.book().write(Common.PWD_KEY, editpassword.getText().toString());
                    signinuse(editPhone.getText().toString(), editpassword.getText().toString());
                } else {
                    Toast.makeText(Signin.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                }
            }
        });
        animation = AnimationUtils.loadAnimation(this,
                R.anim.blink);
        animation.setAnimationListener(this);

        imageView.startAnimation(animation);

    }


    private void signinuse(String phone, String password) {
        final android.app.AlertDialog waitingdialog = new SpotsDialog(Signin.this);
        waitingdialog.show();
        waitingdialog.setTitle("Loading...");

        final String localphone = phone;
        final String localpassword = password;

        users.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(localphone).exists()) {

                    waitingdialog.dismiss();
                    User user = dataSnapshot.child(localphone).getValue(User.class);
                    user.setPhone(localphone);
                    if (Boolean.parseBoolean(user.getIsStaff())) {
                        if (user.getPassword().equals(localpassword)) {

                            Intent intent = new Intent(Signin.this, Home.class);
                            Common.currentuser = user;
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(Signin.this, "Password incorrect", Toast.LENGTH_SHORT).show();

                        }
                    } else {
                        Toast.makeText(Signin.this, "Please login with staff account", Toast.LENGTH_SHORT).show();

                    }
                } else {
                    waitingdialog.dismiss();
                    Toast.makeText(Signin.this, "User not exist", Toast.LENGTH_SHORT).show();


                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onAnimationStart(Animation animation) {

    }

    @Override
    public void onAnimationEnd(Animation animation) {

    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }
}

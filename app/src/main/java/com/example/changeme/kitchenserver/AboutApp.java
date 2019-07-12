package com.example.changeme.kitchenserver;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.Toolbar;

public class AboutApp extends AppCompatActivity {

    FloatingActionButton call, message;
    ImageView email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_app);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle("About");
        call = (FloatingActionButton) findViewById(R.id.callert);
        message = (FloatingActionButton) findViewById(R.id.messa);
        email = (ImageView) findViewById(R.id.em);
        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String num = "08168511238";
                String num1 = "tel:" + num.toString();
                Uri text = Uri.parse(num1);
                Intent act = new Intent(Intent.ACTION_DIAL, text);
                Toast.makeText(AboutApp.this, "Calling Developer", Toast.LENGTH_SHORT).show();
                startActivity(act);
            }
        });

        message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String num = "08168511238";
                String num1 = "tel:" + num.toString();
                Uri text = Uri.parse(num1);
                Intent act = new Intent(Intent.ACTION_SENDTO, text);
                Toast.makeText(AboutApp.this, "Calling Developer", Toast.LENGTH_SHORT).show();
                startActivity(act);

            }
        });

        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

    }
}

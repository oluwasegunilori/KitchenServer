package com.example.changeme.kitchenserver;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.changeme.kitchenserver.Common.Common;
import com.example.changeme.kitchenserver.Model.DataMessage;
import com.example.changeme.kitchenserver.Model.MyResponse;
import com.example.changeme.kitchenserver.Model.Token;
import com.example.changeme.kitchenserver.Remote.APIService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import info.hoang8f.widget.FButton;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Notifier extends AppCompatActivity {
    FirebaseDatabase database;
    DatabaseReference requesst;

    APIService mservice2;
    MaterialEditText mesaage, messagetitle;
    FButton butt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifier);

        messagetitle = findViewById(R.id.Edittitle);
        mesaage = findViewById(R.id.Editmessage);
        mservice2 = Common.getFCMClient();

        butt = (FButton) findViewById(R.id.btnsendmessage);

        butt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //  Notification notification = new Notification(messagetitle.getText().toString(),mesaage.getText().toString());
                //Sender totopic = new Sender();
                //totopic.to = new StringBuilder("/topics/").append(Common.topic_name).toString();
                //totopic.notification = notification;

                sendNotification();

            }
        });
    }


    private void sendNotification() {
        final android.app.AlertDialog waitingdialog = new SpotsDialog(Notifier.this);

        final DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query data = tokens.orderByChild("serverToken").equalTo(false);
        data.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Token serverToken = null;

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {


                    serverToken = postSnapshot.getValue(Token.class);


                    Map<String, String> datasend = new HashMap<>();

                    datasend.put("title", messagetitle.getText().toString());
                    datasend.put("message", mesaage.getText().toString());
                    DataMessage dataMessage = new DataMessage(serverToken.getToken(), datasend);
                    waitingdialog.show();

                    mservice2.sendNotification(dataMessage)
                            .enqueue(new Callback<MyResponse>() {

                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if (response.body().success == 1) {

                                    } else {


                                    }
                                }


                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {
                                    Log.d("Error", t.getMessage());
                                }
                            });


                }
                waitingdialog.dismiss();
                Toast.makeText(Notifier.this, "Notification Sent", Toast.LENGTH_SHORT).show();
                finish();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

}

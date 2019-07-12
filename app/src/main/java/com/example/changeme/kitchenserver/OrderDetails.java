package com.example.changeme.kitchenserver;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.changeme.kitchenserver.Common.Common;
import com.example.changeme.kitchenserver.Model.Utils;
import com.example.changeme.kitchenserver.ViewHolder.OrderDetailAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class OrderDetails extends AppCompatActivity {


    TextView order_id, order_phone, order_address, order_total, naming_id, order_messge, payment_status, order_time;
    String order_id_value = "";
    RecyclerView lstfood;
    ImageView call;

    ProgressBar progressBar;
    FirebaseDatabase db;
    DatabaseReference requests;
    ImageView goback;
    RecyclerView.LayoutManager layoutManager;
    private RelativeLayout errorlayout;
    private TextView errormessage, errortitle;
    private ImageView errorimage;
    private Button retry, cancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);


        order_time = findViewById(R.id.time);
        cancel = findViewById(R.id.btncancelorder);
        db = FirebaseDatabase.getInstance();
        requests = db.getReference("Requests");
        order_id = (TextView) findViewById(R.id.order_id);
        order_phone = (TextView) findViewById(R.id.order_phone);
        payment_status = findViewById(R.id.statusofPay);
        goback = findViewById(R.id.gooback);
        order_address = (TextView) findViewById(R.id.order_address);
        order_total = (TextView) findViewById(R.id.order_total);
        naming_id = (TextView) findViewById(R.id.naming_id);
        order_messge = (TextView) findViewById(R.id.order_message);
        FloatingActionButton fcallle = (FloatingActionButton) findViewById(R.id.callere);
        call = findViewById(R.id.phone_icon);
        progressBar = findViewById(R.id.progress_load_photo);
        errorlayout = findViewById(R.id.errorlayout);
        errormessage = findViewById(R.id.errormessage);
        errortitle = findViewById(R.id.errortitle);
        errormessage = findViewById(R.id.errormessage);
        errorimage = findViewById(R.id.errorimage);
        retry = findViewById(R.id.retry);
        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String num = order_phone.getText().toString();
                String num1 = "tel:" + num.toString();
                Uri text = Uri.parse(num1);
                Intent act = new Intent(Intent.ACTION_DIAL, text);
                Toast.makeText(OrderDetails.this, "Calling customer", Toast.LENGTH_SHORT).show();
                startActivity(act);
            }
        });
        goback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goback = new Intent(OrderDetails.this, Orders.class);
                goback.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(goback);
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                requests.child(order_id_value).removeValue();
                Intent back = new Intent(OrderDetails.this, Orders.class);
                back.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(back);

            }
        });
        lstfood = (RecyclerView) findViewById(R.id.lstFoods);
        lstfood.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        lstfood.setLayoutManager(layoutManager);
        if (Common.currentuser == null) {


        }

        loadOrderdetails();


    }

    private void loadOrderdetails() {
        if (Common.isConnectedToInternet(getBaseContext())) {
            if (getIntent() != null) {
                order_id_value = getIntent().getStringExtra("OrderId");

            }


            naming_id.setText(Common.currentRequest.getName());
            order_id.setText("Order ID: " + order_id_value);

            order_phone.setText(Common.currentRequest.getPhone());
            order_total.setText(Common.currentRequest.getTotal());
            order_address.setText(Common.currentRequest.getAddress());
            payment_status.setText(Common.currentRequest.getPay_status());
            order_messge.setText(Common.currentRequest.getMessage());
            order_time.setText(Utils.DateToTimeFormat(Common.currentRequest.getTime()));
            OrderDetailAdapter adapter = new OrderDetailAdapter(Common.currentRequest.getLifes());

            adapter.notifyDataSetChanged();
            lstfood.setAdapter(adapter);


        } else {
            showErrorMessage(R.drawable.no_result, "Error", "Check your internet connection");

        }
    }

    private void showErrorMessage(int imageview, String title, String message) {
        if (errorlayout.getVisibility() == View.GONE) {
            errorlayout.setVisibility(View.VISIBLE);
        }
        errorimage.setImageResource(imageview);
        errortitle.setText(title);
        errormessage.setText(message);

        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                loadOrderdetails();

            }
        });
    }

}

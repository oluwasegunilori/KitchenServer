package com.example.changeme.kitchenserver;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.changeme.kitchenserver.Common.Common;
import com.example.changeme.kitchenserver.Interface.ItemClickListener;
import com.example.changeme.kitchenserver.Model.DataMessage;
import com.example.changeme.kitchenserver.Model.MyResponse;
import com.example.changeme.kitchenserver.Model.Request;
import com.example.changeme.kitchenserver.Model.Token;
import com.example.changeme.kitchenserver.Remote.APIService;
import com.example.changeme.kitchenserver.ViewHolder.OrderViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jaredrummler.materialspinner.MaterialSpinner;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderStatus extends AppCompatActivity {
    FirebaseDatabase db;
    DatabaseReference requests;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    ProgressBar progressBar;
    FirebaseRecyclerAdapter<Request, OrderViewHolder> adapter;
    MaterialSpinner spinner;
    APIService mService, mService2;
    private RelativeLayout errorlayout;
    private TextView errormessage, errortitle;
    private ImageView errorimage;
    private Button retry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_status);
        db = FirebaseDatabase.getInstance();
        requests = db.getReference("Requests");
        mService = Common.getFCMClient();


        recyclerView = (RecyclerView) findViewById(R.id.listorders);
        progressBar = findViewById(R.id.progress_load_order);
        errorlayout = findViewById(R.id.errorlayout);
        errormessage = findViewById(R.id.errormessage);
        errortitle = findViewById(R.id.errortitle);
        errormessage = findViewById(R.id.errormessage);
        errorimage = findViewById(R.id.errorimage);
        retry = findViewById(R.id.retry);

        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);

        recyclerView.setLayoutManager(layoutManager);

        loadOrders();
    }

    private void loadOrders() {

        if (Common.isConnectedToInternet(getBaseContext())) {


            FirebaseRecyclerOptions<Request> options = new FirebaseRecyclerOptions.Builder<Request>()
                    .setQuery(requests, Request.class)
                    .build();

            adapter = new FirebaseRecyclerAdapter<Request, OrderViewHolder>(options) {
                @Override
                protected void onBindViewHolder(@NonNull OrderViewHolder viewHolder, int position, @NonNull final Request model) {
                    progressBar.setVisibility(View.GONE);
                    viewHolder.txtOrderid.setText("Order ID: " + adapter.getRef(position).getKey());
                    viewHolder.txtOrderstatus.setText("Status: " + Common.convertCodeToStatus(model.getStatus()));

                    viewHolder.setItemClickListener(new ItemClickListener() {
                        @Override
                        public void onClick(View view, int position, boolean isLongClick) {


                            if (!isLongClick) {


                                Intent intent2 = new Intent(OrderStatus.this, OrderDetails.class);
                                Common.currentRequest = model;
                                intent2.putExtra("OrderId", adapter.getRef(position).getKey());
                                startActivity(intent2);
                            }


                        }
                    });


                }

                @Override
                public OrderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                    View itemview = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.order_layout, parent, false);


                    return new OrderViewHolder(itemview);

                }
            };
            adapter.startListening();

            adapter.notifyDataSetChanged();
            recyclerView.setAdapter(adapter);


        } else {
            Toast.makeText(OrderStatus.this, "Check Internet Connection", Toast.LENGTH_SHORT).show();

        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getTitle().equals(Common.UPDATE))
            showUpdateDialog(adapter.getRef(item.getOrder()).getKey(), adapter.getItem(item.getOrder()));
        else if (item.getTitle().equals(Common.DELETE))
            deleteOrder(adapter.getRef(item.getOrder()).getKey());


        return super.onContextItemSelected(item);
    }

    private void deleteOrder(String key) {
        if (Common.isConnectedToInternet(getBaseContext())) {
            requests.child(key).removeValue();
        } else {
            showErrorMessage(R.drawable.no_result, "Error", "Check your internet connection");

        }
    }

    private void showUpdateDialog(String key, final Request item) {
        if (Common.isConnectedToInternet(getBaseContext())) {
            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(OrderStatus.this);
            alertDialog.setTitle("Update Status");
            alertDialog.setMessage("Please choose status");

            LayoutInflater inflater = this.getLayoutInflater();
            final View view = inflater.inflate(R.layout.update_oreder_layout, null);

            spinner = (MaterialSpinner) view.findViewById(R.id.statusspinner);
            spinner.setItems("Placed", "On my Way", "Delivered");
            alertDialog.setView(view);
            final String localkey = key;
            alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    item.setStatus(String.valueOf(spinner.getSelectedIndex()));

                    requests.child(localkey).setValue(item);

                    sendOrderStatusToUser(localkey, item);


                }
            });
            alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            alertDialog.show();
        } else {
            showErrorMessage(R.drawable.no_result, "Error", "Check your internet connection");

        }
    }

    private void sendOrderStatusToUser(final String key, final Request item) {

        DatabaseReference tokens = db.getReference("Tokens");
        tokens.orderByKey().equalTo(item.getPhone())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Token token = null;
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {

                            token = postSnapshot.getValue(Token.class);


                            //    Notification notification = new Notification("Shegz Kitchen", "Your Order"+"\n"+key+"\n"+ "was updated");
                            //  Sender content= new Sender(token.getToken(), notification);

                            Map<String, String> datasend = new HashMap<>();
                            datasend.put("title", "Shegz Kitchen");
                            datasend.put("message", "Your Order " + key + " was updated");
                            DataMessage dataMessage = new DataMessage(token.getToken(), datasend);
                            mService.sendNotification(dataMessage)
                                    .enqueue(new Callback<MyResponse>() {
                                        @Override
                                        public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                            if (response.body().success == 1) {
                                                Toast.makeText(OrderStatus.this, "Order was Updated !", Toast.LENGTH_SHORT).show();

                                            } else {
                                                Toast.makeText(OrderStatus.this, "Order was Updated but failed to send Notification!", Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<MyResponse> call, Throwable t) {
                                            Log.e("ERROR", t.getMessage());
                                        }
                                    });


                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


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

                loadOrders();

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOrders();
    }
}

package com.example.changeme.kitchenserver;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
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
import com.example.changeme.kitchenserver.Model.Utils;
import com.example.changeme.kitchenserver.Remote.APIService;
import com.example.changeme.kitchenserver.ViewHolder.OrderViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.jaredrummler.materialspinner.MaterialSpinner;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PastOrders.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PastOrders#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PastOrders extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    public String email;
    FirebaseDatabase db;
    DatabaseReference requests;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    ProgressBar progressBar;
    FirebaseRecyclerAdapter<Request, OrderViewHolder> adapter;
    MaterialSpinner spinner;
    APIService mService, mService2;
    LinearLayoutManager mLayoutManager;
    private RelativeLayout errorlayout;
    private TextView errormessage, errortitle;
    private ImageView errorimage;
    private Button retry;
    private View rootView;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public PastOrders() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PastOrders.
     */
    // TODO: Rename and change types and number of parameters
    public static PastOrders newInstance(String param1, String param2) {
        PastOrders fragment = new PastOrders();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.activity_order_status, container, false);
        db = FirebaseDatabase.getInstance();
        requests = db.getReference("Requests");
        mService = Common.getFCMClient();


        recyclerView = (RecyclerView) rootView.findViewById(R.id.listorders);
        progressBar = rootView.findViewById(R.id.progress_load_order);
        errorlayout = rootView.findViewById(R.id.errorlayout);
        errormessage = rootView.findViewById(R.id.errormessage);
        errortitle = rootView.findViewById(R.id.errortitle);
        errormessage = rootView.findViewById(R.id.errormessage);
        errorimage = rootView.findViewById(R.id.errorimage);
        retry = rootView.findViewById(R.id.retry);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);


        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setHasFixedSize(true);

        Context c = getContext();
        if (Common.currentuser != null) {

        } else {
            Intent rt = new Intent(c, MainActivity.class);
            rt.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(rt);
        }
        loadOrders();
        return rootView;

    }

    private void loadOrders() {

        if (Common.isConnectedToInternet(getContext())) {
            final Query orderVyUser = requests.orderByChild("status")
                    .equalTo("2");


            orderVyUser.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {


                        FirebaseRecyclerOptions<Request> foodoptions = new FirebaseRecyclerOptions.Builder<Request>()
                                .setQuery(orderVyUser, Request.class)
                                .build();


                        adapter = new FirebaseRecyclerAdapter<Request, OrderViewHolder>(foodoptions) {
                            @Override
                            protected void onBindViewHolder(@NonNull OrderViewHolder viewHolder, int position, @NonNull final Request model) {
                                progressBar.setVisibility(View.GONE);
                                viewHolder.txtOrderid.setText("Order ID: " + adapter.getRef(position).getKey());
                                viewHolder.txtOrderstatus.setText("Status: " + Common.convertCodeToStatus(model.getStatus()));
                                viewHolder.txtTime.setText(Utils.DateToTimeFormat(model.getTime()));
                                viewHolder.setItemClickListener(new ItemClickListener() {
                                    @Override
                                    public void onClick(View view, int position, boolean isLongClick) {

                                        Common.currentRequest = model;
                                        if (!isLongClick) {


                                            Intent intent2 = new Intent(getContext(), OrderDetails.class);
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


                            @Override
                            public void onDataChanged() {
                                recyclerView.removeAllViews();

                                super.onDataChanged();
                            }

                        };
                        adapter.startListening();

                        adapter.notifyDataSetChanged();
                        recyclerView.setAdapter(adapter);


                    } else {

                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getActivity(), "No Orders", Toast.LENGTH_LONG).show();

                    }


                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


        } else {
            progressBar.setVisibility(View.GONE);

            Toast.makeText(getContext(), "Check Internet Connection", Toast.LENGTH_SHORT).show();

        }
    }

    public String mert(String key) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Requests");
        DatabaseReference mostafa = ref.child(key).child("phone");
        mostafa.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                email = dataSnapshot.getValue(String.class);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return email;
    }


    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (getUserVisibleHint()) {
            if (item.getTitle().equals(Common.UPDATE))
                showUpdateDialog(adapter.getRef(item.getOrder()).getKey(), adapter.getItem(item.getOrder()), mert(adapter.getRef(item.getOrder()).getKey()));
            else if (item.getTitle().equals(Common.DELETE))
                deleteOrder(adapter.getRef(item.getOrder()).getKey());


        }

        return super.onContextItemSelected(item);
    }

    private void deleteOrder(String key) {
        if (Common.isConnectedToInternet(getContext())) {
            requests.child(key).removeValue();
            adapter.notifyDataSetChanged();
        } else {
            Toast.makeText(getContext(), "Check Internet Connection", Toast.LENGTH_SHORT).show();

        }
    }

    private void showUpdateDialog(String key, final Request item, final String ty) {
        if (Common.isConnectedToInternet(getContext())) {
            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
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

                    if (spinner.getSelectedIndex() == 0) {
                        item.setPhoneplusstatus(Common.currentRequest.getPhone() + "_0");
                    } else if (spinner.getSelectedIndex() == 1) {
                        item.setPhoneplusstatus(Common.currentRequest.getPhone() + "_1");
                    } else {
                        item.setPhoneplusstatus(Common.currentRequest.getPhone() + "_2");
                    }


                    requests.child(localkey).setValue(item);

                    sendOrderStatusToUser(localkey, item);

                    loadOrders();


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
            Toast.makeText(getContext(), "Check Internet Connection", Toast.LENGTH_SHORT).show();

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
                                                Toast.makeText(getContext(), "Order was Updated !", Toast.LENGTH_SHORT).show();

                                            } else {
                                                Toast.makeText(getContext(), "Order was Updated but failed to send Notification!", Toast.LENGTH_SHORT).show();
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


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadOrders();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}

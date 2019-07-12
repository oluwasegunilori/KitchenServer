package com.example.changeme.kitchenserver;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.example.changeme.kitchenserver.Common.Common;
import com.example.changeme.kitchenserver.Model.Banner;
import com.example.changeme.kitchenserver.Model.Utils;
import com.example.changeme.kitchenserver.ViewHolder.BannerViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import info.hoang8f.widget.FButton;

public class BannerActivity extends AppCompatActivity {

    public String id;
    FButton btnselect, btnupload;
    RelativeLayout rootlayout;
    RecyclerView.LayoutManager layoutManager;
    FirebaseDatabase db;
    RecyclerView recyclerView;
    FloatingActionButton fab;
    DatabaseReference banners;
    StorageReference storageReference;
    FirebaseStorage storage;
    FirebaseDatabase db2;
    DatabaseReference foodidget;
    FirebaseRecyclerAdapter<Banner, BannerViewHolder> adapter;

    MaterialEditText foodid, foodname;
    Banner newBanner;
    Uri filepath;
    ProgressBar progressBar, progressBar2;
    private RelativeLayout errorlayout;
    private TextView errormessage, errortitle;
    private ImageView errorimage;
    private Button retry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_banner);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Banner Management");
        setSupportActionBar(toolbar);

        db = FirebaseDatabase.getInstance();
        banners = db.getReference("Banner");
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        progressBar = findViewById(R.id.progress_load_photosmall);
        progressBar2 = findViewById(R.id.progress_load_photobanner);

        errorlayout = findViewById(R.id.errorlayout);
        errormessage = findViewById(R.id.errormessage);
        errortitle = findViewById(R.id.errortitle);
        errormessage = findViewById(R.id.errormessage);
        errorimage = findViewById(R.id.errorimage);
        retry = findViewById(R.id.retry);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_banner);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE) {

            recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        } else if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_NORMAL) {

            recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        } else if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_XLARGE) {

            recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        } else if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_SMALL) {

            recyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        }

        rootlayout = (RelativeLayout) findViewById(R.id.rootlayout);

        fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Common.isConnectedToInternet(getBaseContext())) {
                    showAddBanner();
                } else {
                    Toast.makeText(BannerActivity.this, "Please check your internet", Toast.LENGTH_SHORT).show();
                }

            }
        });


        loadlistBanner();
    }

    private void loadlistBanner() {
        if (Common.isConnectedToInternet(getBaseContext())) {

            FirebaseRecyclerOptions<Banner> options = new FirebaseRecyclerOptions.Builder<Banner>()
                    .setQuery(banners, Banner.class)
                    .build();


            adapter = new FirebaseRecyclerAdapter<Banner, BannerViewHolder>(options) {
                @Override
                protected void onBindViewHolder(@NonNull final BannerViewHolder viewHolder, int position, @NonNull Banner model) {
                    progressBar2.setVisibility(View.GONE);
                    RequestOptions requestOptions = new RequestOptions();
                    requestOptions.placeholder(Utils.getRandomDrawbleColor());
                    requestOptions.error(Utils.getRandomDrawbleColor());
                    requestOptions.diskCacheStrategy(DiskCacheStrategy.ALL);
                    requestOptions.centerCrop();
                    requestOptions.timeout(3000);

                    Glide.with(getBaseContext())
                            .load(model.getImage())
                            .apply(requestOptions)
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, com.bumptech.glide.request.target.Target<Drawable> target, boolean isFirstResource) {
                                    viewHolder.progressBar.setVisibility(View.GONE);
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, com.bumptech.glide.request.target.Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    viewHolder.progressBar.setVisibility(View.GONE);

                                    return false;
                                }
                            })
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .into(viewHolder.bannerimage);


                    viewHolder.bannername.setText(model.getName());

                }

                @Override
                public BannerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                    View itemView = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.banner_layout, parent, false);
                    return new BannerViewHolder(itemView);
                }
            };
            adapter.startListening();
            adapter.notifyDataSetChanged();
            recyclerView.setAdapter(adapter);
        } else {
            showErrorMessage(R.drawable.no_result, "Error", "Check your internet connection");

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    private void showAddBanner() {


        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(BannerActivity.this);
        alertDialog.setTitle("Add new Food");
        alertDialog.setMessage("Please fill full information");

        LayoutInflater inflater = this.getLayoutInflater();
        View v = inflater.inflate(R.layout.add_new_banner_layout, null);
        foodid = v.findViewById(R.id.editfoodid);
        foodname = v.findViewById(R.id.editfoodname);

        btnselect = v.findViewById(R.id.btnselect);
        btnupload = v.findViewById(R.id.btnupload);

        btnselect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        btnupload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });

        alertDialog.setView(v);
        alertDialog.setIcon(R.drawable.ic_timelapse_black_24dp);


        alertDialog.setPositiveButton("CREATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {

            }
        });
        alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        final AlertDialog dialog = alertDialog.create();
        dialog.show();


        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean wantToCloseDialog = false;
                //Do stuff, possibly set wantToCloseDialog to true then...
                if (Common.isConnectedToInternet(getBaseContext())) {

                    if (foodid.getText().toString().length() > 1 && foodname.getText().length() > 1) {
                        newBanner.setName(String.valueOf(foodname.getText()));
                        newBanner.setId(String.valueOf(foodid.getText()));
                    } else {

                    }

                    if (newBanner != null) {
                        if (newBanner.getName().length() > 0 && newBanner.getId().length() > 0) {
                            if (doIt()) {

                                banners.push()
                                        .setValue(newBanner);
                                Toast.makeText(BannerActivity.this, "Food added successfully ", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                                loadlistBanner();


                            } else {
                                Toast.makeText(BannerActivity.this, "Food with ID " + newBanner.getId() + " does not exist", Toast.LENGTH_LONG).show();
                                showAddBanner();

                                loadlistBanner();
                            }
                        } else {
                            Toast.makeText(BannerActivity.this, "Inavlid ID/Name", Toast.LENGTH_LONG).show();


                        }

                    } else {
                        Toast.makeText(getApplicationContext(), "Please fill all fields ", Toast.LENGTH_LONG).show();


                    }
                } else {
                    Toast.makeText(BannerActivity.this, "Check Internet Connection ", Toast.LENGTH_LONG).show();

                }
                //else dialog stays open. Make sure you have an obvious way to close the dialog especially if you              set cancellable to false.
            }
        });

        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                loadlistBanner();
                //else dialog stays open. Make sure you have an obvious way to close the dialog especially if                     you set cancellable to false.
            }
        });


    }

    private void uploadImage() {
        if (Common.isConnectedToInternet(getBaseContext())) {
            if (filepath != null) {
                final ProgressDialog mDialog = new ProgressDialog(this);
                mDialog.setMessage("Uploading - --");
                mDialog.show();


                String imagename = UUID.randomUUID().toString();
                final StorageReference imagefolder = storageReference.child("images/" + imagename);
                imagefolder.putFile(filepath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        mDialog.dismiss();
                        Toast.makeText(BannerActivity.this, "Uploaded---", Toast.LENGTH_SHORT).show();
                        imagefolder.getDownloadUrl()
                                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        newBanner = new Banner();
                                        id = foodname.getText().toString();
                                        newBanner.setName(foodname.getText().toString());
                                        newBanner.setId(foodid.getText().toString());
                                        newBanner.setImage(uri.toString());


                                    }
                                });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mDialog.dismiss();
                        Toast.makeText(BannerActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                        mDialog.setMessage("Uploaded " + progress + "%");

                    }
                });
            } else {
                Toast.makeText(BannerActivity.this, "Please Select an Image", Toast.LENGTH_SHORT).show();

            }


        } else {
            Toast.makeText(BannerActivity.this, "Check Internet Connection", Toast.LENGTH_SHORT).show();
        }
    }

    private void chooseImage() {
        if (Common.isConnectedToInternet(getBaseContext())) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), Common.PICK_IMAGE_REQUEST);
        } else {
            Toast.makeText(BannerActivity.this, "Check Internet Connection", Toast.LENGTH_SHORT).show();
        }


    }

    public boolean doIt() {
        String me;
        db2 = FirebaseDatabase.getInstance();
        foodidget = db.getReference("Life");

        foodidget.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String rt = foodid.getText().toString().substring(0, foodid.getText().toString().length());
                if (dataSnapshot.child(rt).exists()) {

                    id = "a";

                } else {
                    //
                    //Toast.makeText(BannerActivity.this, "Food with ID "+ newBanner.getId() + " does not exist", Toast.LENGTH_SHORT ).show();
                    //showAddBanner()
                    //
                    id = "b";
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        if (id.contains("a")) {
            return true;
        } else {
            return false;
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Common.PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            filepath = data.getData();
            btnselect.setText("Image Selected");


        }

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getTitle().equals(Common.UPDATE)) {
            if (Common.isConnectedToInternet(getBaseContext())) {

                showUpdateBannerDialog(adapter.getRef(item.getOrder()).getKey(), adapter.getItem(item.getOrder()));
            } else {
                Toast.makeText(BannerActivity.this, "Check Internet Connection", Toast.LENGTH_SHORT).show();
            }

        } else if (item.getTitle().equals(Common.DELETE)) {
            if (Common.isConnectedToInternet(getBaseContext())) {
                deleteBanner(adapter.getRef(item.getOrder()).getKey());
            } else {
                Toast.makeText(BannerActivity.this, "Check Internet Connection", Toast.LENGTH_SHORT).show();
            }

        }
        return super.onContextItemSelected(item);


    }

    private void showUpdateBannerDialog(final String key, final Banner item) {


        AlertDialog.Builder alertDialog = new AlertDialog.Builder(BannerActivity.this);
        alertDialog.setTitle("Edit Food");
        alertDialog.setMessage("Please fill full information");
        LayoutInflater inflater = this.getLayoutInflater();
        View add_menu_layout = inflater.inflate(R.layout.add_new_banner_layout, null);
        foodname = add_menu_layout.findViewById(R.id.editfoodname);
        foodid = add_menu_layout.findViewById(R.id.editfoodid);


        foodid.setText(item.getId());
        foodname.setText(item.getName());
        foodid.setEnabled(false);
        btnupload = add_menu_layout.findViewById(R.id.btnupload);
        btnselect = add_menu_layout.findViewById(R.id.btnselect);


        alertDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);

        alertDialog.setView(add_menu_layout);

        btnselect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
            }


        });

        btnupload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeImage(item);

            }


        });


        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {


                    }

                }

        )
        ;


        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });

        final AlertDialog dialog = alertDialog.create();
        dialog.show();


        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Common.isConnectedToInternet(getBaseContext())) {

                    if (foodname.getText().toString().length() > 1) {


                        item.setName(foodname.getText().toString());
                        item.setId(foodid.getText().toString());


                        Map<String, Object> update = new HashMap<>();
                        update.put("id", item.getId());
                        update.put("name", item.getName());
                        update.put("image", item.getImage());

                        banners.child(key)
                                .updateChildren(update)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        Snackbar.make(rootlayout, "Updated successfully", Snackbar.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                        loadlistBanner();
                                    }
                                });
                    } else {
                        Toast.makeText(BannerActivity.this, "Please fill all fields", Toast.LENGTH_LONG).show();

                    }
                } else {
                    Toast.makeText(BannerActivity.this, "Check Internet Connection", Toast.LENGTH_SHORT).show();

                }


            }
        });
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                loadlistBanner();
                //else dialog stays open. Make sure you have an obvious way to close the dialog especially if                     you set cancellable to false.
            }
        });
    }


    private void deleteBanner(String key) {
        banners.child(key).removeValue();
    }


    private void changeImage(final Banner item) {

        if (filepath != null) {
            final ProgressDialog mDialog = new ProgressDialog(this);
            mDialog.setMessage("Uploading - --");
            mDialog.show();

            String imagename = UUID.randomUUID().toString();
            final StorageReference imagefolder = storageReference.child("images/" + imagename);
            imagefolder.putFile(filepath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    mDialog.dismiss();
                    Toast.makeText(BannerActivity.this, "Uploaded---", Toast.LENGTH_SHORT).show();
                    imagefolder.getDownloadUrl()
                            .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    item.setImage(uri.toString());

                                }
                            });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    mDialog.dismiss();
                    Toast.makeText(BannerActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                    mDialog.setMessage("Uploaded " + progress + "%");

                }
            });
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.banner, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        //noinspection SimplifiableIfStatement
        if (item.getItemId() == R.id.action_settings) {
            loadlistBanner();
        }

        return super.onOptionsItemSelected(item);
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

                loadlistBanner();
            }
        });
    }


}
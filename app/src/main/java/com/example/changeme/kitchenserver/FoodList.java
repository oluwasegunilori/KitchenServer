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
import android.text.Editable;
import android.text.TextWatcher;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.example.changeme.kitchenserver.Common.Common;
import com.example.changeme.kitchenserver.Model.Banner;
import com.example.changeme.kitchenserver.Model.Food;
import com.example.changeme.kitchenserver.Model.Utils;
import com.example.changeme.kitchenserver.ViewHolder.FoodViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import info.hoang8f.widget.FButton;

public class FoodList extends AppCompatActivity {

    public String wert;
    FButton btnselect, btnupload;
    DatabaseReference banners;

    RelativeLayout rootlayout;
    RecyclerView.LayoutManager layoutManager;
    FirebaseDatabase db, db2;
    RecyclerView recyclerView;
    FloatingActionButton fab;
    DatabaseReference foodlist;
    Uri saveur;
    MaterialSearchBar materialSearchBar;

    List<String> suggestlist = new ArrayList<>();
    ProgressBar progressBar, progressBar2;
    FirebaseRecyclerAdapter<Food, FoodViewHolder> searchadapter;
    Banner newBanner;
    String subString;
    MaterialEditText edittname, editdescription, editdiscount, editprice;
    FirebaseStorage storage;
    StorageReference storageReference;
    Food newfood;
    String categoryId = "";
    FirebaseRecyclerAdapter<Food, FoodViewHolder> adapter;
    private RelativeLayout errorlayout;
    private TextView errormessage, errortitle;
    private ImageView errorimage;
    private Button retry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_list);
        progressBar = findViewById(R.id.progress_load_photobig);
        progressBar2 = findViewById(R.id.progress_load_photo1);

        errorlayout = findViewById(R.id.errorlayout);
        errormessage = findViewById(R.id.errormessage);
        errortitle = findViewById(R.id.errortitle);
        errormessage = findViewById(R.id.errormessage);
        errorimage = findViewById(R.id.errorimage);
        retry = findViewById(R.id.retry);

        db = FirebaseDatabase.getInstance();
        foodlist = db.getReference("Life");
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        recyclerView = (RecyclerView) findViewById(R.id.recycler_life);
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
                    showAddFoodDalog();
                } else {
                    Toast.makeText(FoodList.this, "Check Internet Connection", Toast.LENGTH_SHORT).show();

                }
            }
        });

        if (getIntent() != null) {
            categoryId = getIntent().getStringExtra("CategoryID");
        }
        if (!categoryId.isEmpty() && categoryId != null) {
            loadList(categoryId);
        }
        materialSearchBar = findViewById(R.id.searchbar);
        materialSearchBar.setHint("Search Food");
        loadSuggest();
        materialSearchBar.setCardViewElevation(10);

        materialSearchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                List<String> suggest = new ArrayList<String>();
                for (String search : suggestlist) {
                    if (search.toLowerCase().contains(materialSearchBar.getText().toLowerCase()))
                        suggest.add(search);
                }
                materialSearchBar.setLastSuggestions(suggest);

            }


            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
                if (!enabled)
                    recyclerView.setAdapter(adapter);
            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                startSearch(text);
            }

            @Override
            public void onButtonClicked(int buttonCode) {

            }
        });


    }

    private void startSearch(CharSequence text) {
        if (Common.isConnectedToInternet(getBaseContext())) {
            Query searchByName = foodlist.orderByChild("name").equalTo(text.toString());
            FirebaseRecyclerOptions<Food> foodoptions = new FirebaseRecyclerOptions.Builder<Food>()
                    .setQuery(searchByName, Food.class)
                    .build();

            searchadapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(foodoptions) {
                @Override
                protected void onBindViewHolder(@NonNull FoodViewHolder viewHolder, int position, @NonNull Food model) {
                    progressBar.setVisibility(View.GONE);
                    viewHolder.foodname.setText(model.getName());
                    Picasso.with(getBaseContext()).load(model.getImage()).into(viewHolder.foodimage);


                    final Food local = model;


                }

                @NonNull
                @Override
                public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {

                    View itemView = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.food_item, parent, false);
                    return new FoodViewHolder(itemView);

                }
            };
            searchadapter.startListening();
            recyclerView.setAdapter(searchadapter);

        } else {
            showErrorMessage(R.drawable.no_result, "Error", "Check your internet connection");

        }
    }

    private void loadSuggest() {
        foodlist.orderByChild("menuid").equalTo(categoryId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            Food item = postSnapshot.getValue(Food.class);
                            suggestlist.add(item.getName());
                        }

                        materialSearchBar.setLastSuggestions(suggestlist);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {


                    }
                });

    }


    private void showAddFoodDalog() {


        AlertDialog.Builder alertDialog = new AlertDialog.Builder(FoodList.this);
        alertDialog.setTitle("Add new Food");
        alertDialog.setMessage("Please fill full information");
        LayoutInflater inflater = this.getLayoutInflater();
        View add_menu_layout = inflater.inflate(R.layout.add_new_food, null);
        edittname = (MaterialEditText) add_menu_layout.findViewById(R.id.editname);
        editdescription = (MaterialEditText) add_menu_layout.findViewById(R.id.editdeescription);
        editdiscount = (MaterialEditText) add_menu_layout.findViewById(R.id.editDiscount);
        editprice = (MaterialEditText) add_menu_layout.findViewById(R.id.editprice);
        btnselect = (FButton) add_menu_layout.findViewById(R.id.btnselect);
        btnupload = (FButton) add_menu_layout.findViewById(R.id.btnupload);
        alertDialog.setView(add_menu_layout);
        alertDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);


        btnselect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
            }


        });

        btnupload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImage();
            }


        });


        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();

                if (newfood != null) {
                    foodlist.push().setValue(newfood);


                    Snackbar.make(rootlayout, "New category" + newfood.getName() + "was added", Snackbar.LENGTH_SHORT).show();

                }

            }
        });
        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        alertDialog.show();


    }

    private void chooseImage() {


        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), Common.PICK_IMAGE_REQUEST);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Common.PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            saveur = data.getData();
            btnselect.setText("Image Selected");


        }
    }


    private void uploadImage() {

        if (saveur != null) {
            final ProgressDialog mDialog = new ProgressDialog(this);
            mDialog.setMessage("Uploading - --");
            mDialog.show();

            String imagename = UUID.randomUUID().toString();
            final StorageReference imagefolder = storageReference.child("images/" + imagename);
            imagefolder.putFile(saveur).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    mDialog.dismiss();
                    Toast.makeText(FoodList.this, "Uploaded---", Toast.LENGTH_SHORT).show();
                    imagefolder.getDownloadUrl()
                            .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    newfood = new Food();
                                    newfood.setName(edittname.getText().toString());
                                    newfood.setDescription(editdescription.getText().toString());
                                    newfood.setDiscount(editdiscount.getText().toString());
                                    newfood.setPrice(editprice.getText().toString());
                                    newfood.setImage(uri.toString());
                                    newfood.setMenuid(categoryId);

                                }
                            });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    mDialog.dismiss();
                    Toast.makeText(FoodList.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                    mDialog.setMessage("Uploaded" + progress + "%");

                }
            });
        }


    }

    private void loadList(String categoryId) {
        if (Common.isConnectedToInternet(getBaseContext())) {
            Query listfoodbycategoryid = foodlist.orderByChild("menuid").equalTo(categoryId);
            FirebaseRecyclerOptions<Food> options = new FirebaseRecyclerOptions.Builder<Food>()
                    .setQuery(listfoodbycategoryid, Food.class)
                    .build();

            adapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(options) {
                @Override
                protected void onBindViewHolder(@NonNull final FoodViewHolder viewHolder, int position, @NonNull Food model) {
                    progressBar.setVisibility(View.GONE);
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
                                    viewHolder.progressBar2.setVisibility(View.GONE);
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, com.bumptech.glide.request.target.Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    viewHolder.progressBar2.setVisibility(View.GONE);

                                    return false;
                                }
                            })
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .into(viewHolder.foodimage);


                    viewHolder.foodname.setText(model.getName());


                }

                @Override
                public FoodViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                    View itemview = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.food_item, parent, false);


                    return new FoodViewHolder(itemview);
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

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        newBanner = new Banner();
        if (item.getTitle().equals(Common.UPDATE)) {

            showUpdateFoodDialog(adapter.getRef(item.getOrder()).getKey(), adapter.getItem(item.getOrder()));

        } else if (item.getTitle().equals(Common.DELETE)) {
            deleteFood(adapter.getRef(item.getOrder()).getKey());

        } else if (item.getTitle().equals(Common.ADDTOBANNER)) {
            DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
            Food itemer = adapter.getItem(item.getOrder());

            Query query = rootRef.child("Life").orderByChild("name").equalTo(itemer.getName());
            ValueEventListener valueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        wert = ds.getKey();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            };
            query.addListenerForSingleValueEvent(valueEventListener);
            db = FirebaseDatabase.getInstance();

            banners = db.getReference("Banner");

            newBanner.setName(itemer.getName());
            newBanner.setImage(itemer.getImage());
            newBanner.setId(String.valueOf(adapter.getSnapshots().getSnapshot(item.getOrder()).getKey()));

            banners.push()
                    .setValue(newBanner);


        }
        return super.onContextItemSelected(item);
    }

    private void deleteFood(String key) {
        foodlist.child(key).removeValue();
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

                loadList(categoryId);
            }
        });
    }

    private void showUpdateFoodDialog(final String key, final Food item) {


        AlertDialog.Builder alertDialog = new AlertDialog.Builder(FoodList.this);
        alertDialog.setTitle("Edit Food");
        alertDialog.setMessage("Please fill full information");
        LayoutInflater inflater = this.getLayoutInflater();
        View add_menu_layout = inflater.inflate(R.layout.add_new_food, null);
        edittname = (MaterialEditText) add_menu_layout.findViewById(R.id.editname);
        editdescription = (MaterialEditText) add_menu_layout.findViewById(R.id.editdeescription);
        editdiscount = (MaterialEditText) add_menu_layout.findViewById(R.id.editDiscount);

        editprice = (MaterialEditText) add_menu_layout.findViewById(R.id.editprice);


        String thefood = item.getName();
        int iend = thefood.indexOf("@");

        if (iend != -1) {
            subString = thefood.substring(0, iend);
        } else {
            subString = thefood;
        }

        edittname.setText(subString);

        editdescription.setText(item.getDescription());
        editdiscount.setText(item.getDiscount());
        editprice.setText(item.getPrice());
        btnselect = (FButton) add_menu_layout.findViewById(R.id.btnselect);
        btnupload = (FButton) add_menu_layout.findViewById(R.id.btnupload);
        alertDialog.setView(add_menu_layout);
        alertDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);


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
                dialogInterface.dismiss();


                item.setName(edittname.getText().toString() + "  " +
                        "@ ₦" + editprice.getText().toString());

                item.setDiscount(editdiscount.getText().toString());
                item.setDescription(editdescription.getText().toString());
                item.setPrice(editprice.getText().toString());
                foodlist.child(key).setValue(item);


                Snackbar.make(rootlayout, "category" + item.getName() + "was edited", Snackbar.LENGTH_SHORT).show();


            }
        });
        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        alertDialog.show();


    }

    private void changeImage(final Food item) {

        if (saveur != null) {
            final ProgressDialog mDialog = new ProgressDialog(this);
            mDialog.setMessage("Uploading - --");
            mDialog.show();

            String imagename = UUID.randomUUID().toString();
            final StorageReference imagefolder = storageReference.child("images/" + imagename);
            imagefolder.putFile(saveur).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    mDialog.dismiss();
                    Toast.makeText(FoodList.this, "Uploaded---", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(FoodList.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                    mDialog.setMessage("Uploaded" + progress + "%");

                }
            });
        }


    }
}



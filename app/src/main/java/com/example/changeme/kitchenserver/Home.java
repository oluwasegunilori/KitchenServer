package com.example.changeme.kitchenserver;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
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
import com.bumptech.glide.request.target.Target;
import com.example.changeme.kitchenserver.Common.Common;
import com.example.changeme.kitchenserver.Interface.ItemClickListener;
import com.example.changeme.kitchenserver.Model.Category;
import com.example.changeme.kitchenserver.Model.Token;
import com.example.changeme.kitchenserver.Model.Utils;
import com.example.changeme.kitchenserver.ViewHolder.MenuViewHolder;
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
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.UUID;

import info.hoang8f.widget.FButton;
import io.paperdb.Paper;

import static android.view.View.GONE;

public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    public TextView txtfullname;
    DrawerLayout drawer;
    FirebaseStorage storage;
    StorageReference storagereference;
    RecyclerView reycler_menu;
    RecyclerView.LayoutManager layoutManager;
    FirebaseDatabase database;
    ProgressBar progressBar;
    DatabaseReference categories;
    FirebaseRecyclerAdapter<Category, MenuViewHolder> adapter;
    MaterialEditText editname;
    FButton btnupload, btnselect;
    DatabaseReference category;
    SwipeRefreshLayout swipeRefreshLayout;
    Category newcategory;
    Uri saveur;
    private RelativeLayout errorlayout;
    private TextView errormessage, errortitle;
    private ImageView errorimage;
    private Button retry;

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        verifyStoragePermissions(Home.this);
        /**
         * Checks if the app has permission to write to device storage
         *
         * If the app does not has permission then the user will be prompted to grant permissions
         *
         * @param activity
         */
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Menu Management");
        setSupportActionBar(toolbar);

        storage = FirebaseStorage.getInstance();
        storagereference = storage.getReference();
        progressBar = findViewById(R.id.progress_load_photohome);
        errorlayout = findViewById(R.id.errorlayout);
        errormessage = findViewById(R.id.errormessage);
        errortitle = findViewById(R.id.errortitle);
        errormessage = findViewById(R.id.errormessage);
        errorimage = findViewById(R.id.errorimage);
        retry = findViewById(R.id.retry);
        swipeRefreshLayout = findViewById(R.id.swipe_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);


        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (Common.isConnectedToInternet(getBaseContext())) {
                    swipeRefreshLayout.setRefreshing(false);

                    loadMenu();

                } else {
                    Toast.makeText(Home.this, "Check Your internet Connection!!", Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(true);
                    return;
                }

            }
        });

        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if (Common.isConnectedToInternet(getBaseContext())) {

                    loadMenu();
                } else {
                    Toast.makeText(Home.this, "Check Your internet Connection!!", Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(true);
                    return;
                }

            }
        });

        database = FirebaseDatabase.getInstance();
        category = database.getReference("Category");


        FirebaseRecyclerOptions<Category> options = new FirebaseRecyclerOptions.Builder<Category>()
                .setQuery(category, Category.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Category, MenuViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final MenuViewHolder viewHolder, int position, @NonNull Category model) {

                progressBar.setVisibility(GONE);
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
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                viewHolder.progressBar.setVisibility(GONE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                viewHolder.progressBar.setVisibility(GONE);

                                return false;
                            }
                        })
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(viewHolder.imageView);

                viewHolder.textmenuName.setText(model.getName());


                final Category clickItem = model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {

                        if (Common.isConnectedToInternet(getBaseContext())) {

                            Intent intent = new Intent(Home.this, FoodList.class);
                            intent.putExtra("CategoryID", adapter.getRef(position).getKey());
                            startActivity(intent);
                        } else {
                            showErrorMessage(R.drawable.no_result, "Error", "Check your internet connection");

                        }


                    }
                });
            }

            @NonNull
            @Override
            public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.menu_item, parent, false);
                return new MenuViewHolder(itemView);

            }
        };


        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);
        txtfullname = (TextView) headerView.findViewById(R.id.textFullName);


        if (Common.currentuser != null) {

            txtfullname.setText(Common.currentuser.getName());
        }
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();


        reycler_menu = (RecyclerView) findViewById(R.id.recylcer_menu);
        reycler_menu.setHasFixedSize(true);
        LayoutAnimationController animationController = AnimationUtils.loadLayoutAnimation(reycler_menu.getContext(),
                R.anim.layout_fall_down);
        reycler_menu.setLayoutAnimation(animationController);

        if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE) {

            reycler_menu.setLayoutManager(new GridLayoutManager(this, 2));
        } else if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_NORMAL) {

            reycler_menu.setLayoutManager(new GridLayoutManager(this, 1));
        } else if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_XLARGE) {

            reycler_menu.setLayoutManager(new GridLayoutManager(this, 3));
        } else if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_SMALL) {

            reycler_menu.setLayoutManager(new GridLayoutManager(this, 1));
        }


        if (Common.currentuser != null) {

            updateToken(FirebaseInstanceId.getInstance().getToken());

        }


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
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

                loadMenu();
            }
        });
    }

    private void updateToken(String token) {

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference tokens = db.getReference("Tokens");
        Token data = new Token(token, true);
        tokens.child(Common.currentuser.getPhone()).setValue(data);
    }

    private void showDialog() {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Add new Category");
        alertDialog.setMessage("Please fill full information");
        LayoutInflater inflater = this.getLayoutInflater();
        View add_menu_layout = inflater.inflate(R.layout.add_new_menu_layout, null);
        editname = (MaterialEditText) add_menu_layout.findViewById(R.id.editname);
        btnselect = (FButton) add_menu_layout.findViewById(R.id.btnselect);
        btnupload = (FButton) add_menu_layout.findViewById(R.id.btnupload);
        alertDialog.setView(add_menu_layout);
        alertDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);
        final AlertDialog alertDia = alertDialog.create();


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

                if (newcategory != null) {
                    categories.push().setValue(newcategory);

                    Snackbar.make(drawer, "New category" + newcategory.getName() + "was added", Snackbar.LENGTH_SHORT).show();

                }

            }
        });
        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });


        alertDialog.show();


        btnselect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDia.dismiss();

                chooseImage();
            }


        });

    }


    private void uploadImage() {

        if (saveur != null) {
            final ProgressDialog mDialog = new ProgressDialog(this);
            mDialog.setMessage("Uploading - --");
            mDialog.show();

            String imagename = UUID.randomUUID().toString();
            final StorageReference imagefolder = storagereference.child("images/" + imagename);
            imagefolder.putFile(saveur).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    mDialog.dismiss();
                    Toast.makeText(Home.this, "Uploaded---", Toast.LENGTH_SHORT).show();
                    imagefolder.getDownloadUrl()
                            .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    newcategory = new Category(editname.getText().toString(), uri.toString());

                                }
                            });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    mDialog.dismiss();
                    Toast.makeText(Home.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Common.PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            saveur = data.getData();
            btnselect.setText("Image Selected");

        }
    }

    private void chooseImage() {


        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), Common.PICK_IMAGE_REQUEST);


    }

    private void loadMenu() {
        if (Common.isConnectedToInternet(getBaseContext())) {
            errorlayout.setVisibility(GONE);
            progressBar.setVisibility(View.VISIBLE);
            swipeRefreshLayout.setRefreshing(true);

            adapter.startListening();
            reycler_menu.setAdapter(adapter);


            reycler_menu.getAdapter().notifyDataSetChanged();
            reycler_menu.scheduleLayoutAnimation();


            swipeRefreshLayout.setRefreshing(false);
        } else {
            showErrorMessage(R.drawable.no_result, "Error", "Check your internet connection");
        }

    }


    protected void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
            this.moveTaskToBack(true);
        }
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        //noinspection SimplifiableIfStatement
        if (item.getItemId() == R.id.action_settings) {
            loadMenu();
        } else if (item.getItemId() == R.id.About) {
            Intent order = new Intent(Home.this, AboutApp.class);
            startActivity(order);

        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onContextItemSelected(MenuItem item) {

        if (item.getTitle().equals(Common.UPDATE)) {
            showUpdateDialog(adapter.getRef(item.getOrder()).getKey(), adapter.getItem(item.getOrder()));


        } else if (item.getTitle().equals(Common.DELETE)) {
            deleteCategory(adapter.getRef(item.getOrder()).getKey(), adapter.getItem(item.getOrder()));


        }


        return super.onContextItemSelected(item);


    }

    private void deleteCategory(String key, Category item) {

        DatabaseReference foods = database.getReference("Life");
        Query foodincategory = foods.orderByChild("menuid").equalTo(key);
        foodincategory.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    postSnapshot.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        categories.child(key).removeValue();
        Toast.makeText(Home.this, "Item Deleted", Toast.LENGTH_SHORT).show();

    }

    private void showUpdateDialog(final String key, final Category item) {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Home.this);
        alertDialog.setTitle("Update Category");
        alertDialog.setMessage("Please fill full information");
        LayoutInflater inflater = this.getLayoutInflater();
        View add_menu_layout = inflater.inflate(R.layout.add_new_menu_layout, null);
        editname = (MaterialEditText) add_menu_layout.findViewById(R.id.editname);
        btnselect = (FButton) add_menu_layout.findViewById(R.id.btnselect);
        btnupload = (FButton) add_menu_layout.findViewById(R.id.btnupload);

        editname.setText(item.getName());


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

                item.setName(editname.getText().toString());
                categories.child(key).setValue(item);

            }
        });
        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        alertDialog.show();


    }

    private void changeImage(final Category item) {

        if (saveur != null) {
            final ProgressDialog mDialog = new ProgressDialog(this);
            mDialog.setMessage("Uploading - - -");
            mDialog.show();

            String imagename = UUID.randomUUID().toString();
            final StorageReference imagefolder = storagereference.child("images/" + imagename);
            imagefolder.putFile(saveur).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    mDialog.dismiss();
                    Toast.makeText(Home.this, "Uploaded Successfully", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(Home.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

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

    @SuppressWarnings("StatementWithEmptyBody")


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_menu) {

        } else if (id == R.id.nav_orders) {

            Intent orders = new Intent(Home.this, Orders.class);
            startActivity(orders);

        } else if (id == R.id.banner) {

            Intent orders = new Intent(Home.this, BannerActivity.class);
            startActivity(orders);

        } else if (id == R.id.nav_notifier) {
            Intent notify = new Intent(Home.this, Notifier.class);
            startActivity(notify);

        } else if (id == R.id.nav_signout) {
            Paper.book().destroy();
            Intent soff = new Intent(Home.this, Signin.class);
            soff.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            startActivity(soff);

        }


        // Handle the camera action

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;


    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Common.currentuser != null && adapter != null) {
            super.onResume();

            txtfullname.setText(Common.currentuser.getName());

            loadMenu();

        } else {
            Intent inte = new Intent(Home.this, MainActivity.class);


            startActivity(inte);
        }


    }
}

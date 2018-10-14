package com.example.hoangcongtuan.combannau.customer.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.example.hoangcongtuan.combannau.LoginActivity;
import com.example.hoangcongtuan.combannau.R;
import com.example.hoangcongtuan.combannau.Utils.Common;
import com.example.hoangcongtuan.combannau.Utils.CustomerMenuManager;
import com.example.hoangcongtuan.combannau.Utils.ImageCached;
import com.example.hoangcongtuan.combannau.Utils.Utils;
import com.example.hoangcongtuan.combannau.customer.RVMenuCallBack;
import com.example.hoangcongtuan.combannau.customer.adapter.MenuAdapter;
import com.example.hoangcongtuan.combannau.models.Dish;
import com.example.hoangcongtuan.combannau.models.DishObj;
import com.example.hoangcongtuan.combannau.models.Menu;
import com.example.hoangcongtuan.combannau.services.GPSTracker;
import com.example.hoangcongtuan.combannau.widget.MarkerInfoWindow;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener, GoogleMap.OnMarkerClickListener{

    private final static int RC_LOCATION = 1;
    private static final String TAG = MainActivity.class.getName();

    private GoogleMap googleMap;
    private GoogleSignInClient mGoogleSignInClient;
    private TextView tvUserName;
    private TextView tvEmail;
    private LatLng current_latlng;
    private TextView tvStoreName;
    @BindView(R.id.tvAddress)
    TextView tvAddress;
    private TextView tvDistance;
    private TextView tvEndTime;
    @BindView(R.id.rvMenu)
    RecyclerView rvMenu;

    @BindView(R.id.layout_bottom_sheet)
    LinearLayout layout_bottom_sheet;

    BottomSheetBehavior bottomSheetBehavior;
    MenuAdapter adapter;


//    private FirebaseFunctions mFunctions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        init();
        initWidget();
        initMapFragment();

        bottomSheetBehavior = BottomSheetBehavior.from(layout_bottom_sheet);
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_HIDDEN:
                        break;

                    case BottomSheetBehavior.STATE_EXPANDED:
                        break;

                    case BottomSheetBehavior.STATE_COLLAPSED:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

    }

    private void init() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        adapter = new MenuAdapter(new ArrayList<Dish>());
        adapter.setCallBack(new RVMenuCallBack() {
            @Override
            public void onOrder(int position) {
                orderItem(position);
            }
        });
    }

    private void orderItem(int position) {
        Toast.makeText(this, "Order Item " + position, Toast.LENGTH_SHORT).show();
    }

    private void initWidget() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        final NavigationView navigationView = findViewById(R.id.navigation_view);

        tvUserName = navigationView.getHeaderView(0).findViewById(R.id.tvUserName);
        tvEmail = navigationView.getHeaderView(0).findViewById(R.id.tvEmail);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(R.string.app_name);

        ActionBarDrawerToggle toggleButton = new ActionBarDrawerToggle(MainActivity.this,
                drawerLayout, toolbar, 0, 0);
        drawerLayout.addDrawerListener(toggleButton);

        toggleButton.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        rvMenu.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));
        rvMenu.setItemAnimator(new DefaultItemAnimator());
        rvMenu.setAdapter(adapter);

        //get avatar
        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref_avatar_url = FirebaseDatabase.getInstance().getReference()
                .child("/user/" + currentUser.getUid() + "/profile/avatar_url");
        ref_avatar_url.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String avatar_url = "";
                if (dataSnapshot.exists())
                    avatar_url = dataSnapshot.getValue().toString();
                else if (currentUser.getPhotoUrl() != null)
                    avatar_url = currentUser.getPhotoUrl().toString();
                if (!avatar_url.isEmpty()) {
                    ImageRequest avatarRequest = new ImageRequest(avatar_url,
                            new Response.Listener<Bitmap>() {
                                @Override
                                public void onResponse(Bitmap response) {
                                    ImageView imageView = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.imgAvatar);
                                    imageView.setImageBitmap(response);
                                    Common.getInstance().setBmpAvatar(response);
                                }
                            },
                            0, 0,
                            ImageView.ScaleType.FIT_CENTER,
                            Bitmap.Config.RGB_565,
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    //todo: error here
                                    Log.d(TAG, "onErrorResponse: error");
                                }
                            }
                    );

                    Utils.VolleyUtils.getsInstance(getApplicationContext()).getRequestQueue().add(avatarRequest);
                }
                else {
                    ImageView imageView = (ImageView)navigationView.getHeaderView(0).findViewById(R.id.imgAvatar);
                    imageView.setImageDrawable(getResources().getDrawable(R.drawable.avatar_circle_blue_512dp));
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        DatabaseReference ref_user_name = FirebaseDatabase.getInstance().getReference()
                .child("/user/" + currentUser.getUid() + "/profile/user_name");
        ref_user_name.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    tvUserName.setText(dataSnapshot.getValue().toString());
                }
                else {
                    //todo: error here
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        tvEmail.setText(currentUser.getEmail());
        Common.getInstance().setUserName(tvUserName.getText().toString());
        Common.getInstance().setUser(FirebaseAuth.getInstance().getCurrentUser());
    }

    private void initMapFragment() {
        ((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);
    }

    private void logout() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signOut();
        mGoogleSignInClient.signOut();

        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_cat_dat:
                break;
            case R.id.item_dang_xuat:
                logout();
                break;
            case R.id.item_functions:
                //add_message_functions();
                https_functions();
                break;

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void https_functions() {
        GetPostsTask get_https_json = new GetPostsTask();
        get_https_json.execute("https://us-central1-combannau-1520822090740.cloudfunctions.net/getAvailableProvider");
    }

    private void get_menus(JSONObject json_posts) {

        Iterator<String> providers = json_posts.keys();
        List<String> menusId = new ArrayList<>();

        try {
            while(providers.hasNext()) {
                String provider = providers.next();
                if (json_posts.get(provider) instanceof JSONArray) {
                    JSONArray json_menus = json_posts.getJSONArray(provider);
                    for(int i = 0; i < json_menus.length(); i++) {
                        menusId.add(json_menus.getString(i));
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        show_provider_location(menusId);


//        List<String> menusId = new ArrayList<>();
//        try {
//            JSONArray json_menus = json_posts.getJSONArray("menus");
//            for(int i = 0; i < json_menus.length(); i++) {
//                menusId.add(json_menus.getString(i));
//            }
//
//            show_provider_location(menusId);
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//            // TODO: 10/13/18 Handle exception
//        }
    }

    private void show_provider_location(List<String> menusId) {
        DatabaseReference ref_menu = FirebaseDatabase.getInstance().getReference().child("menu");
        int count = 0;
        for(String key: menusId) {
            count++;
            Query query_getMenu = ref_menu.orderByKey().equalTo(key);
            final int finalCount = count;
            query_getMenu.addListenerForSingleValueEvent(new ValueEventListener() {
                int tmp_index = finalCount - 1;
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for(DataSnapshot snapshot: dataSnapshot.getChildren()) {
                            Menu menu = snapshot.getValue(Menu.class);
                            CustomerMenuManager.getsInstance().items.add(menu);
                            LatLng latLng = new LatLng(
                                    menu.latitude, menu.longtitude
                            );

                            Marker marker = googleMap.addMarker(new MarkerOptions()
                                    .position(latLng)
                                    .title(menu.name)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_icon))
                                    .snippet(menu.address)
                            );
                            marker.setTag(tmp_index);
                        }
                    }
                    else {
                        // TODO: 10/13/18 Handle exception
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.showInfoWindow();
        int menuPosition = (int) marker.getTag();
        showMenu(menuPosition);
        return true;
    }

    private void showMenu(int menuPosition) {
        List<DishObj> items_obj = CustomerMenuManager.getsInstance().items.get(menuPosition).items;
        List<Dish> items = new ArrayList<>();
        for(DishObj obj: items_obj) {
            Dish dish = new Dish(obj);
            if (ImageCached.getInstance().bitmapHashMap.containsKey(obj.imageUrl)) {
                dish.bitmap = ImageCached.getInstance().bitmapHashMap.get(obj.imageUrl);
            }
            else {
                downLoadBitmap(obj.imageUrl, items_obj.indexOf(obj), adapter);
                dish.bitmap = null;
            }
            items.add(dish);
        }
        tvAddress.setText(CustomerMenuManager.getsInstance().items.get(menuPosition).address);

        adapter.replace(items);
        adapter.notifyDataSetChanged();
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    private void downLoadBitmap(final String imageUrl, final int pos, final MenuAdapter adapter) {
        ImageRequest imageRequest = new ImageRequest(imageUrl,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        ImageCached.getInstance().bitmapHashMap.put(imageUrl, response);
                        adapter.getItems().get(pos).bitmap = response;
                        adapter.notifyItemChanged(pos);
                    }
                }, 0, 0, ImageView.ScaleType.FIT_CENTER,
                Bitmap.Config.RGB_565,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });

        Utils.VolleyUtils.getsInstance(getApplicationContext()).getRequestQueue().add(imageRequest);
    }

    private class GetPostsTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected String doInBackground(String... params) {

            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();


                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line+"\n");
                    Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)

                }

                return buffer.toString();


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            try {
                JSONObject json_posts = new JSONObject(result);
                get_menus(json_posts);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        this.googleMap = googleMap;

        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.getUiSettings().setMapToolbarEnabled(true);


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(R.string.location_permission_title);
                builder.setMessage(R.string.location_peermission_message);
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                                RC_LOCATION);                    }
                });
                builder.setCancelable(false);
                builder.create().show();

            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                        RC_LOCATION);
            }

            return;
        }

        googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.setOnMarkerClickListener(this);

        MarkerInfoWindow markerInfoWindow = new MarkerInfoWindow(this);
        googleMap.setInfoWindowAdapter(markerInfoWindow);

        GPSTracker gpsTracker = new GPSTracker(MainActivity.this);
        if (gpsTracker.canGetLocation()) {
            current_latlng = new LatLng(gpsTracker.getLatitude(), gpsTracker.getLongitude());
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(current_latlng, 15));
        }
        else {
            gpsTracker.showSettingsAlert();
        }
        https_functions();
    }


    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == RC_LOCATION) {
            if (permissions.length == 1 &&
                    permissions[0] == Manifest.permission.ACCESS_FINE_LOCATION &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                googleMap.setMyLocationEnabled(true);
                googleMap.getUiSettings().setMyLocationButtonEnabled(true);
                googleMap.setOnMyLocationButtonClickListener(this);
                googleMap.setOnMyLocationClickListener(this);
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {

    }
}

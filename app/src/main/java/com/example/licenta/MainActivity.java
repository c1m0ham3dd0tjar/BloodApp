package com.example.licenta;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationClient;

    private static final int PERMISSIONS_REQUEST = 100;
    private FirebaseAuth mAuth;

    // PENTRU SEARCH VIEW
  /*  @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();

        inflater.inflate(R.menu., menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default


        return true;
    }*/

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            item -> {
                Fragment selectedFragment = null;

                switch (item.getItemId()) {
                    case R.id.nav_home:
                        selectedFragment = new HomeFragment();
                        break;
                    case R.id.nav_donate:
                        selectedFragment = new DonationFragment();
                        break;
                    case R.id.nav_request:
                        selectedFragment = new RequestFragment();
                        break;
                    case R.id.nav_profile:
                        selectedFragment = new ProfileFragment();
                        break;
                }

                assert selectedFragment != null;
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        selectedFragment).commit();

                return true;
            };

    // If the user is not logged in, skip the MainActivity and go to the LoginActivity.
    FirebaseAuth.AuthStateListener authStateListener = new FirebaseAuth.AuthStateListener() {
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
            if (firebaseUser == null) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MaterialToolbar toolbar = findViewById(R.id.appToolbar);
        // Designate toolbar as the action bar for the activity
        setSupportActionBar(toolbar);

        toolbar.setTitleTextColor(getResources().getColor(R.color.color_white));
        toolbar.setNavigationIcon(R.drawable.ic_action_profile);
        toolbar.setNavigationOnClickListener(v -> startActivity(new Intent(MainActivity.this, ProfileActivity.class)));



        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            //doMySearch(query);
        }

        mAuth = FirebaseAuth.getInstance();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);

        // Start the app with home fragment
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new HomeFragment()).commit();

        // Check wheter GPS tracking is enabled
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        assert lm != null;
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            finish();
        }

        // Chech wheter this app has acces to the location permission
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        // If the location permission has been granted, then start the TrackerService
        if (permission == PackageManager.PERMISSION_GRANTED) {
            startTrackerService();
        } else {
            // If the app doesn't currently have access to the user's location, then request access.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST);
        }
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(
                intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);

            //doMySearch(query);

        }
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the options menu from the xml
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.search_menu, menu);
//
//        // Get the SearchView and set the searchable configuration
//        SearchManager searchManager = (SearchManager)getSystemService(Context.SEARCH_SERVICE);
//        SearchView searchView = (SearchView)menu.findItem(R.id.action_search).getActionView();
//
//        // Assumes current activity is the searchable activity
//        ComponentName componentName = new ComponentName(MainActivity.this, SearchResultsActivity.class);
//        searchView.setSearchableInfo(Objects.requireNonNull(searchManager).getSearchableInfo(componentName));
//        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
//
//        return true;
//    }

    // Print the hash key for facebook on logcat
    private void printKeyHash() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo("com.example.licenta", PackageManager.GET_SIGNATURES);

            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // If the permission has been granted
        if (requestCode == PERMISSIONS_REQUEST && grantResults.length == 1
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // THEN START THE GPS TRACKING SERVICE
            startTrackerService();
        } else {
            // User denies the permision request, display a toast with info
            Toast.makeText(this, "Please enable location services", Toast.LENGTH_LONG).show();
        }
    }

    private void startTrackerService() {
      //  try {
            startService(new Intent(this, TrackingService.class));
       // } catch (Exception e) {
        //    if (e != null)
       // }
        // Notify the user that tracking has been enabled
       //  Toast.makeText(this, "GPS tracking enabled", Toast.LENGTH_LONG).show();

        // Close MainActivity
       // finish();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Start listening for changes
        mAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();

        mAuth.removeAuthStateListener(authStateListener);
    }
}

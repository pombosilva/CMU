package pt.ulisboa.tecnico.cmov.project;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.Manifest;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

import pt.ulisboa.tecnico.cmov.project.databinding.ActivityLibrariesBinding;

public class LibrariesActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pt.ulisboa.tecnico.cmov.project.databinding.ActivityLibrariesBinding binding = ActivityLibrariesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.libraries_map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
    }

    public void searchLocation(View view){
        // Remove previous search
        mMap.clear();
        EditText searchText = findViewById(R.id.searchText);
        Geocoder geocoder = new Geocoder(this);
        try{
            // Get address from text in searchText
            List<Address> addresses = geocoder.getFromLocationName(searchText.getText().toString(),1);
            if(addresses != null && !addresses.isEmpty()) {
                // Close the keyboard if there is at least one search result
                closeKeyboard(view);
                Address address = addresses.get(0);
                LatLng searchedLatLng = new LatLng(address.getLatitude(), address.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(searchedLatLng, 20));
                mMap.addMarker(new MarkerOptions().position(searchedLatLng).title("Add new Library"));
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    // Close the keyboard if it is opened
    public void closeKeyboard(View view){
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (inputMethodManager != null){
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Display zoom controls
        mMap.getUiSettings().setZoomControlsEnabled(true);

        // Enable zoom gestures
        mMap.getUiSettings().setZoomGesturesEnabled(true);

        // Get location permissions
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1001);
        }
        // Display myLocation button and enable it
        else {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        }
    }
}
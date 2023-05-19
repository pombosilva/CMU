package pt.ulisboa.tecnico.cmov.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class LibraryInfo_Activity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library_info);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.library_map);
        mapFragment.getMapAsync(this);

        loadLibraryInfo(getIntent());

        configureButtons();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        double libraryLat = getIntent().getDoubleExtra("libraryLat",49.621271);
        double libraryLng = getIntent().getDoubleExtra("libraryLng", -86.942096);
        LatLng coordinates = new LatLng(libraryLat, libraryLng);


        mMap.addMarker(new MarkerOptions().position(coordinates));

        float zoomLevel = 16.0f;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinates, zoomLevel));

        Log.d("MensagensDebug", "VAI VAI");
    }


    private void loadLibraryInfo(Intent intent)
    {

        Bundle intentContents = intent.getExtras();

        if ( intentContents != null) {
            String libraryName = intentContents.getString("libraryName");
            TextView libraryNameTv = (TextView) findViewById(R.id.library_name);
            libraryNameTv.setText(libraryName);

            ImageView libraryImageIm = (ImageView) findViewById(R.id.library_image);
            int libraryImage = intentContents.getInt("libraryImage");
            libraryImageIm.setImageResource(libraryImage);

            double libraryLat = intentContents.getDouble("libraryLat");
            double libraryLng = intentContents.getDouble("libraryLng");

            LatLng coordinates = new LatLng(libraryLat, libraryLng);
            Log.d("MensagensDebug","Lat = " + libraryLat + " Lng = " + libraryLng);

            Log.d("MensagensDebug","LatLng" + coordinates);

        }
    }

    private void configureButtons()
    {
        Button checkInButton = (Button) findViewById(R.id.checkin_btn);
        checkInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText( getApplicationContext(), "Clicked check in", Toast.LENGTH_SHORT).show();
            }
        });



        Button checkoutButton = (Button) findViewById(R.id.checkout_btn);
        checkoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText( getApplicationContext(), "Clicked check out", Toast.LENGTH_SHORT).show();
            }
        });


        Button favouriteButton = (Button) findViewById(R.id.library_favourite_button);
        favouriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText( getApplicationContext(), "Clicked favourite button", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
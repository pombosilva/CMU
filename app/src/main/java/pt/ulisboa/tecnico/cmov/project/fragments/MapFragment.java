package pt.ulisboa.tecnico.cmov.project.fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

import pt.ulisboa.tecnico.cmov.project.adapters.CustomWindowInfoAdapter;
import pt.ulisboa.tecnico.cmov.project.activities.LibraryInfo_Activity;
import pt.ulisboa.tecnico.cmov.project.R;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().
                findFragmentById(R.id.libraries_map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        Button searchButton = rootView.findViewById(R.id.searchButton);
        searchButton.setOnClickListener(this::searchLocation);

        return rootView;
    }

    public void searchLocation(View view) {
        // Remove previous search
        mMap.clear();
        EditText searchText = requireView().findViewById(R.id.searchText);
        Geocoder geocoder = new Geocoder(getActivity());
        try {
            List<Address> addresses = geocoder.getFromLocationName(searchText.getText().toString(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                closeKeyboard(view);
                Address address = addresses.get(0);
                LatLng searchedLatLng = new LatLng(address.getLatitude(), address.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(searchedLatLng, 20));
                mMap.addMarker(new MarkerOptions().position(searchedLatLng).title("Add new Library"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);

        if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1001);
        } else {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        }

        loadLibrariesMarkers();

        mMap.setInfoWindowAdapter(new CustomWindowInfoAdapter(getActivity()));
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(@NonNull Marker marker) {
                Intent intent = new Intent(getActivity(), LibraryInfo_Activity.class);
                intent.putExtra("libraryName",marker.getTitle());
                // Need Database
                intent.putExtra("libraryImage", R.drawable.img);
                intent.putExtra("libraryLat", marker.getPosition().latitude);
                intent.putExtra("libraryLng", marker.getPosition().longitude);


                marker.hideInfoWindow();

                startActivity(intent);
            }
        });
    }


    private void loadLibrariesMarkers()
    {
        loadNormalMarkers();
        loadFavouriteMarkers();
    }

    private void loadNormalMarkers()
    {
        // Lookup on database

        // add them to the map

        LatLng sydney = new LatLng(-33.857143, 151.215147);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Sydney"));
        LatLng lisbon = new LatLng(38.713912, -9.133397);
        mMap.addMarker(new MarkerOptions().position(lisbon).title("Lisbon"));
        LatLng madrid = new LatLng(40.416891, -3.703739);
        mMap.addMarker(new MarkerOptions().position(madrid).title("Madrid"));
    }

    private void loadFavouriteMarkers()
    {
        // Lookup on database

        // add them to the map
        LatLng zaragoza = new LatLng(41.657059, -0.875448);
        MarkerOptions mkOpt = new MarkerOptions().position(zaragoza).title("Zaragoza").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        mMap.addMarker(mkOpt);
    }
}

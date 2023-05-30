package pt.ulisboa.tecnico.cmov.project.fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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
import java.util.concurrent.Executors;

import pt.ulisboa.tecnico.cmov.project.R;
import pt.ulisboa.tecnico.cmov.project.activities.LibraryInfoActivity;
import pt.ulisboa.tecnico.cmov.project.adapters.CustomMarkerBaseAdapter;
import pt.ulisboa.tecnico.cmov.project.objects.WebConnector;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private final WebConnector webConnector;
    private Marker searchMarker;

    public MapFragment(WebConnector webConnector) {
        this.webConnector = webConnector;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.webConnector.setHandler(this.handler);
    }

    @Override
    public void onResume(){
        super.onResume();
        this.webConnector.setHandler(this.handler);

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                webConnector.getMarkers();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
        if (searchMarker != null)
            searchMarker.remove();

        EditText searchText = requireView().findViewById(R.id.searchText);
        Geocoder geocoder = new Geocoder(getActivity());

        try {
            List<Address> addresses = geocoder.getFromLocationName(searchText.getText().
                    toString(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                closeKeyboard(view);
                Address address = addresses.get(0);
                LatLng searchedLatLng = new LatLng(address.getLatitude(), address.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(searchedLatLng, 20));
                searchMarker = mMap.addMarker(new MarkerOptions().position(searchedLatLng).
                        title("Add new Library"));
                //TODO: Add description field to each library
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) requireActivity().
                getSystemService(Context.INPUT_METHOD_SERVICE);
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

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                webConnector.getMarkers();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        mMap.setInfoWindowAdapter(new CustomMarkerBaseAdapter(getActivity()));
        mMap.setOnInfoWindowClickListener(marker -> {
            Intent intent = new Intent(getActivity(), LibraryInfoActivity.class);

            String markerSnippet = marker.getSnippet();
            assert markerSnippet != null;
            int markerId = Integer.parseInt(markerSnippet.split(":")[0]);

            intent.putExtra("libraryId", markerId);
            intent.putExtra("libraryName",marker.getTitle());
            intent.putExtra("libraryLat", marker.getPosition().latitude);
            intent.putExtra("libraryLng", marker.getPosition().longitude);
            intent.putExtra("libraryEncodedImage", markerSnippet.split(":")[1]);

            marker.hideInfoWindow();
            startActivity(intent);
        });
    }

    private void loadNormalMarker(pt.ulisboa.tecnico.cmov.project.objects.Marker marker) {
        LatLng markerLocation = new LatLng(marker.getLat(), marker.getLng());
        MarkerOptions mkOpt = new MarkerOptions().position(markerLocation).
                title(marker.getName()).snippet(marker.getId() + ":a");

        // set favourite markers blue
        if (marker.isFav())
            mkOpt.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        
        sendMessageToHandler(mkOpt);
    }

    private void sendMessageToHandler(Object obj) {
        Message msg = new Message();
        msg.obj = obj;
        msg.what = MapFragment.MARKER_MSG;
        handler.sendMessage(msg);
    }

    private static final int MARKER_MSG = 0;
    private static final int NO_INTERNET = 1;
    private static final int NEW_MARKER = 2;

    private final Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MARKER_MSG:
                    mMap.addMarker((MarkerOptions)msg.obj);
                    return;
                case NO_INTERNET:
                    Toast.makeText(requireActivity().getApplicationContext(),
                            (String) msg.obj, Toast.LENGTH_LONG).show();
                    return;
                case NEW_MARKER:
                    loadNormalMarker((pt.ulisboa.tecnico.cmov.project
                            .objects.Marker) msg.obj);
            }
        }
    };
}

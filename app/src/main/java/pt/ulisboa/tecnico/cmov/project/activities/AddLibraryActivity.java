package pt.ulisboa.tecnico.cmov.project.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import pt.ulisboa.tecnico.cmov.project.R;
import pt.ulisboa.tecnico.cmov.project.objects.Book;
import pt.ulisboa.tecnico.cmov.project.objects.Library;
import pt.ulisboa.tecnico.cmov.project.objects.WebConnector;
import pt.ulisboa.tecnico.cmov.project.utils.ImageUtils;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import pt.ulisboa.tecnico.cmov.project.R;
import pt.ulisboa.tecnico.cmov.project.objects.Book;
import pt.ulisboa.tecnico.cmov.project.objects.WebConnector;
import pt.ulisboa.tecnico.cmov.project.utils.ImageUtils;

public class AddLibraryActivity extends AppCompatActivity {

    private static final int requestCode = 100;
    ImageView imageView;
    Button takePictureBtn;
    private String base64Picture;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_library);

        imageView = findViewById(R.id.imageView);
        takePictureBtn = findViewById(R.id.takePictureBtn);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, requestCode);
        }
        configureButtons();

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void configureButtons() {
        configureTakePictureButton();
        configureSaveLibButton();
    }

    private void configureSaveLibButton() {
        Button saveLibBtn = findViewById(R.id.saveLibBtn);

        saveLibBtn.setOnClickListener(v -> {

            TextView libNameTv = findViewById(R.id.libName);
            String libName = libNameTv.getText().toString();

            TextView searchLocationTv = findViewById(R.id.searchLocation);
            String searchLocation = searchLocationTv.getText().toString();

            LatLng coordinates = getCoordinatesFromLocation(searchLocation);

            if (isEmpty(libName) || isEmpty(searchLocation)) {
                Toast.makeText(AddLibraryActivity.this, "Please fill all fields", Toast.LENGTH_LONG).show();
            } else if(coordinates==null) {
                Toast.makeText(AddLibraryActivity.this, "Please enter a valid location", Toast.LENGTH_LONG).show();
            }
            else {
                if (isEmpty(base64Picture)){ Toast.makeText(AddLibraryActivity.this, "Please take a picture of the library", Toast.LENGTH_LONG).show(); }
                else {
                    Executor executor = Executors.newSingleThreadExecutor();
                    executor.execute(() -> {
                                int libraryId = WebConnector.getNextLibID();
                                registerLibToCloud(libraryId, libName, coordinates.latitude, coordinates.longitude, base64Picture);
                                finish();
                            }
                    );
                }
            }
        });
    }

    public LatLng getCoordinatesFromLocation(String location) {
        LatLng searchedLatLng = null;
        Geocoder geocoder = new Geocoder(this);

        try {
            List<Address> addresses = geocoder.getFromLocationName(location, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                searchedLatLng = new LatLng(address.getLatitude(), address.getLongitude());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return searchedLatLng;
    }


    public void registerLibToCloud(int id, String name, double lat, double lng, String libEncodedImage) {
        WebConnector.registerLib(new Library(id, name, lat, lng, false, libEncodedImage));
    }


    //TODO: change deprecated method
    private void configureTakePictureButton(){
        takePictureBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent,requestCode);
        });
    }

    @Override
    protected void onActivityResult(int requestC, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestC == requestCode) {
            assert data != null;
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(bitmap);
            base64Picture = ImageUtils.encodeBitmapToBase64(bitmap, Bitmap.CompressFormat.JPEG, 64);
        }
    }

    private boolean isEmpty(String string) {
        return string == null || string.equals("");
    }
}

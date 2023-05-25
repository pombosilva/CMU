package pt.ulisboa.tecnico.cmov.project.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import pt.ulisboa.tecnico.cmov.project.adapters.CustomBaseAdapter;
import pt.ulisboa.tecnico.cmov.project.objects.Book;
import pt.ulisboa.tecnico.cmov.project.R;

public class LibraryInfo_Activity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int CHECKIN = 0;
    private static final int CHECKOUT = 1;

    private ArrayList<Book> bookList = new ArrayList<Book>();
    private ListView bookListView;

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library_info);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.library_map);
        mapFragment.getMapAsync(this);


        configureBookListView();
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
        else
        {
            Toast.makeText( getApplicationContext(), "Wasn't able to load library contents", Toast.LENGTH_SHORT).show();
        }
    }

    private void configureBookListView()
    {

//        new Thread (() -> {
            bookList.add(new Book(1,"Biblia", "palavra de deus", R.drawable.bible_, 1234567));
            bookList.add(new Book(2,"Harry poter", "feiticos", R.drawable.harry, 1234));
            bookList.add(new Book(3,"Game of thrones", "porrada", R.drawable.gow, 6544));
            bookList.add(new Book(4,"Ben 10", "bue fixe", R.drawable.ben, 98));
            bookList.add(new Book(5,"Geronimo Stilton", "Rolemodel", R.drawable.g_ronimo, 43292));
            bookList.add(new Book(6, "Manual de portugues 8ano", "Camoes glorioso", R.drawable.manual, 1234567));

            bookListView = (ListView) findViewById(R.id.library_bookListView);
            CustomBaseAdapter customBaseAdapter = new CustomBaseAdapter(getApplicationContext(), bookList);
            bookListView.setAdapter(customBaseAdapter);
            bookListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    // TODO: Complete reaction code
                    Intent intent = new Intent(LibraryInfo_Activity.this, BookInfo_Activity.class);
//                intent.putExtra("noteTitle", noteslist.get(position).getTitle());
//                intent.putExtra("noteText", noteslist.get(position).getText());
                    startActivity(intent);
                }
            });
//        }).start();
    }

    private void configureButtons()
    {
        Button checkInButton = (Button) findViewById(R.id.checkin_btn);
        checkInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText( getApplicationContext(), "Clicked check in", Toast.LENGTH_SHORT).show();
                scanBarcode(CHECKIN);

            }
        });



        Button checkoutButton = (Button) findViewById(R.id.checkout_btn);
        checkoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanBarcode(CHECKOUT);

                // TODO: Reagir ao id retornado

                Toast.makeText( getApplicationContext(), "Clicked check out", Toast.LENGTH_SHORT).show();

            }
        });


        Button favouriteButton = (Button) findViewById(R.id.library_favourite_button);
        favouriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText( getApplicationContext(), "Clicked favourite button", Toast.LENGTH_SHORT).show();
                // TODO: Adicionar/Remover das user preferences
            }
        });
    }

    private void scanBarcode(int mode)
    {
        ScanOptions options = new ScanOptions();
        options.setPrompt("------------------Volume up to flash on-----------------");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(Capture_Activity.class);
        switch (mode)
        {
            case CHECKIN:
                checkInScanner.launch(options);
            case CHECKOUT:
                checkOutScanner.launch(options);
            default:
                Toast.makeText(this, "NAO FOI NADA",Toast.LENGTH_LONG).show();
        }
    }

    ActivityResultLauncher<ScanOptions> checkInScanner = registerForActivityResult(new ScanContract(), result ->
    {
        if (result.getContents() != null)
        {
            // TODO: Faltar implementar a reacao ao input (Database needed)
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("CheckIn Result");
            builder.setMessage(result.getContents());
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                     dialog.dismiss();
                }
            }).show();
        }
    });

    ActivityResultLauncher<ScanOptions> checkOutScanner = registerForActivityResult(new ScanContract(), result ->
    {
        if (result.getContents() != null)
        {
            // TODO: Faltar implementar a reacao ao input (Database needed)
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Checkout Result");
            builder.setMessage(result.getContents());
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).show();
        }
    });
}
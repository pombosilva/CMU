package pt.ulisboa.tecnico.cmov.project.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import pt.ulisboa.tecnico.cmov.project.R;
import pt.ulisboa.tecnico.cmov.project.adapters.CustomLibraryBaseAdapter;
import pt.ulisboa.tecnico.cmov.project.objects.Book;
import pt.ulisboa.tecnico.cmov.project.objects.Marker;
import pt.ulisboa.tecnico.cmov.project.objects.WebConnector;
import pt.ulisboa.tecnico.cmov.project.utils.ImageUtils;

public class BookInfoActivity extends AppCompatActivity {

    private final ArrayList<Marker> libraryList = new ArrayList<>();
    private int bookBarcode;
    private CustomLibraryBaseAdapter libraryListCustomBaseAdapter;
    private WebConnector webConnector;

    public BookInfoActivity() {
        // empty constructor
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.info_book);

        webConnector = new WebConnector(this.getApplicationContext());
//        webConnector.startWebSocket();

        loadBookInfo(getIntent());
        configureListView();
        configureButtons();
    }

    private void loadBookInfo(Intent intent) {
        Bundle intentContents = intent.getExtras();

        if (intentContents != null) {
            String bookTitle = intentContents.getString("bookTitle");
            TextView bookTitleTv = findViewById(R.id.bookTitle);
            bookTitleTv.setText(bookTitle);


            ImageView bookCoverIm = findViewById(R.id.bookCover);

            try {
                String bookCover = intentContents.getString("bookCover");
                Bitmap bitmap = ImageUtils.decodeBase64ToBitmap(bookCover);
                bookCoverIm.setImageBitmap(bitmap);
            } catch (Exception e) {
                bookCoverIm.setImageResource(Book.unloadedBookCover);
            }


            bookBarcode = intentContents.getInt("bookBarcode");
        } else {
            Toast.makeText(getApplicationContext(),
                    "Wasn't able to load book contents", Toast.LENGTH_SHORT).show();
        }
    }

    private void configureListView() {
        ListView listView = findViewById(R.id.bookAvailableLibraries);
        webConnector.setHandler(this.handler);
        libraryList.clear();
        libraryListCustomBaseAdapter = new CustomLibraryBaseAdapter(getApplicationContext(),
                libraryList);
        listView.setAdapter(libraryListCustomBaseAdapter);

        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                webConnector.getLibrariesThatContainBook(bookBarcode);
                getLibrariesDistances();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(BookInfoActivity.this, LibraryInfoActivity.class);
            intent.putExtra("libraryName", libraryList.get(position).getName());
            intent.putExtra("libraryId", libraryList.get(position).getId());
            intent.putExtra("libraryLat", libraryList.get(position).getLat());
            intent.putExtra("libraryLng", libraryList.get(position).getLng());
            startActivity(intent);
        });
    }

    private void configureButtons() {
        Button notificationsButton = findViewById(R.id.buttonFav);
        notificationsButton.setOnClickListener(v -> {
            Toast.makeText(getApplicationContext(), "Added to favourites", Toast.LENGTH_LONG).show();
            // TODO: Adicionar aos favoritos
        });
    }

    public void getLibrariesDistances() {
        FusedLocationProviderClient location = LocationServices.
                getFusedLocationProviderClient(getApplicationContext());
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        location.getLastLocation().
                addOnSuccessListener(this, location1 -> {
                    if (location1 != null){
                        for(Marker m: libraryList) {
                            Location library = new Location("library");
                            library.setLongitude(m.getLng());
                            library.setLatitude(m.getLat());
                            String distance = (int) location1.distanceTo(library) / 1000 + "km";
                            sendMessageToHandler(3, distance);
                        }
                    }
                });
    }

    public void sendMessageToHandler(int what, Object obj){
        Message msg = new Message();
        msg.what = what;
        msg.obj = obj;
        handler.sendMessage(msg);
    }

    private static final int NO_INTERNET = 1;
    private static final int ADD_LIBRARY_TO_LIST = 2;
    private static final int ADD_DISTANCE_TO_LIBRARY = 3;

    private final Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ADD_DISTANCE_TO_LIBRARY:
                    for(Marker m: libraryList) {
                        if (m.getDistance() == null) {
                            m.setDistance((String) msg.obj);
                            break;
                        }
                    }
                    libraryListCustomBaseAdapter.notifyDataSetChanged();
                    return;
                case ADD_LIBRARY_TO_LIST:
                    libraryList.add((Marker) msg.obj);
                    libraryListCustomBaseAdapter.notifyDataSetChanged();
                    return;
                case NO_INTERNET:
                    Toast.makeText(getApplicationContext(),
                            (String) msg.obj, Toast.LENGTH_LONG).show();
            }
        }
    };

}
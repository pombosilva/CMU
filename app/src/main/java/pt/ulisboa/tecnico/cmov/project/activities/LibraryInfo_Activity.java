package pt.ulisboa.tecnico.cmov.project.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import pt.ulisboa.tecnico.cmov.project.R;
import pt.ulisboa.tecnico.cmov.project.adapters.CustomBaseAdapter;
import pt.ulisboa.tecnico.cmov.project.fragments.MapFragment;
import pt.ulisboa.tecnico.cmov.project.objects.Book;
import pt.ulisboa.tecnico.cmov.project.objects.WebConnector;
import pt.ulisboa.tecnico.cmov.project.utils.ImageUtils;

public class LibraryInfo_Activity extends AppCompatActivity implements OnMapReadyCallback {

    private int libraryId;
    private static final int CHECKIN = 0;
    private static final int CHECKOUT = 1;

    private Context mContext;

    private final ArrayList<Book> bookList = new ArrayList<>();

    private CustomBaseAdapter bookListCustomBaseAdapter;

    private WebConnector webConnector;

    private ImageView libraryImage;

    public LibraryInfo_Activity() {
        /* TODO: Perguntar ao professor sobre a melhor implementacao. Criar novas
            instancias de web connector ou conseguir passar de alguma forma entre elas
        *   Talvez Webconnector ser singleton*/
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.info_library);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.library_map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        mContext = getApplicationContext();

        webConnector = new WebConnector(mContext);
        webConnector.startWebSocket();
        webConnector.setHandler(this.handler);

        loadLibraryInfo(getIntent());

        try {
            configureBookListView();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        configureButtons();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        double libraryLat = getIntent().getDoubleExtra("libraryLat",49.621271);
        double libraryLng = getIntent().getDoubleExtra("libraryLng", -86.942096);
        LatLng coordinates = new LatLng(libraryLat, libraryLng);

        googleMap.addMarker(new MarkerOptions().position(coordinates));

        float zoomLevel = 16.0f;
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinates, zoomLevel));
    }


    private void loadLibraryInfo(Intent intent) {
        Bundle intentContents = intent.getExtras();

        if (intentContents != null) {
            libraryId = intentContents.getInt("libraryId");

            String libraryName = intentContents.getString("libraryName");
            TextView libraryNameTv = findViewById(R.id.library_name);
            libraryNameTv.setText(libraryName);

            libraryImage = findViewById(R.id.library_image);

//            webConnector.getLibraryImage(intentContents.getInt("libraryId"));

//            String[] projection = {MediaStore.Images.Media.DATA};
//            String selection = MediaStore.Images.Media.DISPLAY_NAME + " = ?";
//            String[] selectionArgs = {"library"+libraryId+".jpg"};
//            Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, selection, selectionArgs, null);
//            if (cursor != null && cursor.moveToFirst()) {
//                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
//                String imagePath = cursor.getString(columnIndex);
//                cursor.close();
//
//                File imageFile = new File(imagePath);
//                if (imageFile.exists()) {
//                    Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
//                    libraryImage.setImageBitmap(bitmap);
//                }
//            }
        }
        else {
            Toast.makeText( getApplicationContext(), "Wasn't able to load library contents",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void configureBookListView() throws IOException {
        bookList.clear();
        ListView bookListView = findViewById(R.id.library_bookListView);
        bookListCustomBaseAdapter = new CustomBaseAdapter(getApplicationContext(), bookList);
        bookListView.setAdapter(bookListCustomBaseAdapter);

        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
//                bookList.addAll(webConnector.getBooks(libraryId));
//                sendMessageToHandler(null);
                webConnector.getBooks(this.libraryId);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        bookListView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(LibraryInfo_Activity.this, BookInfo_Activity.class);
            intent.putExtra("bookTitle", bookList.get(position).getTitle());
            intent.putExtra("bookCover", bookList.get(position).getCover());
            startActivity(intent);
        });
    }

    private void configureButtons() {
        Button checkInButton = findViewById(R.id.checkin_btn);
        checkInButton.setOnClickListener(v -> {
            scanBarcode(CHECKIN);
            Toast.makeText(getApplicationContext(), "Clicked check in", Toast.LENGTH_SHORT).show();
        });


        Button checkoutButton = findViewById(R.id.checkout_btn);
        checkoutButton.setOnClickListener(v -> {
            scanBarcode(CHECKOUT);
            Toast.makeText(getApplicationContext(), "Clicked check out", Toast.LENGTH_SHORT).show();
        });


        Button favouriteButton = findViewById(R.id.library_favourite_button);
        favouriteButton.setOnClickListener(v -> {
            webConnector.setLibraryFav(libraryId);
            Toast.makeText(getApplicationContext(), "Clicked favourite button", Toast.LENGTH_SHORT).show();
        });
    }

    private void scanBarcode(int mode) {
        ScanOptions options = new ScanOptions();
        options.setPrompt("------------------Volume up to flash on-----------------");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(Capture_Activity.class);
        switch (mode) {
            case CHECKIN:
                checkInScanner.launch(options);
                break;
            case CHECKOUT:
                checkOutScanner.launch(options);
                break;
        }
    }

    ActivityResultLauncher<ScanOptions> checkInScanner = registerForActivityResult(new ScanContract(), result -> {
        if (result.getContents() != null)
        {
            // TODO: Faltar implementar a reacao ao input (Database needed)
            String bookBarcode = result.getContents();

            Executor executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                boolean bookExistance = webConnector.bookExists(bookBarcode);
                Log.d("MensagensDebug", "Existe? " + bookExistance);
            });


            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("CheckIn Result");
            builder.setMessage(bookBarcode);
            builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss()).show();
        }
    });

    ActivityResultLauncher<ScanOptions> checkOutScanner = registerForActivityResult(new ScanContract(), result -> {
        if (result.getContents() != null)
        {
            // TODO: Faltar implementar a reacao ao input (Database needed)
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Checkout Result");
            builder.setMessage(result.getContents());
            builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss()).show();
        }
    });

    private void sendMessageToHandler(Object obj) {
        Message msg = new Message();
        msg.obj = obj;
        msg.what = UPDATE_UI_MSG;
        handler.sendMessage(msg);
    }

    private static final int  UPDATE_UI_MSG= 0;
    private static final int TOAST_MSG = 1;

    private static final int UPDATE_BOOK_LIST = 2;
    private static final int LIBRARY_IMG_MSG = 3;

    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_BOOK_LIST:
                    bookList.add((Book) msg.obj);
                    bookListCustomBaseAdapter.notifyDataSetChanged();
                    return;
                case UPDATE_UI_MSG:
                    bookListCustomBaseAdapter.notifyDataSetChanged();
                    return;
                case LIBRARY_IMG_MSG:
                    libraryImage.setImageBitmap(ImageUtils.decodeBase64ToBitmap((String) msg.obj));
                    return;
                case TOAST_MSG:
                    Toast.makeText(getApplicationContext(),
                            (String) msg.obj, Toast.LENGTH_LONG).show();
            }
        }
    };
}
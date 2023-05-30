package pt.ulisboa.tecnico.cmov.project.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import pt.ulisboa.tecnico.cmov.project.R;
import pt.ulisboa.tecnico.cmov.project.adapters.CustomBookBaseAdapter;
import pt.ulisboa.tecnico.cmov.project.objects.Book;
import pt.ulisboa.tecnico.cmov.project.objects.WebConnector;

public class LibraryInfoActivity extends AppCompatActivity implements OnMapReadyCallback {

    private int libraryId;
    private static final int CHECKIN = 0;
    private static final int CHECKOUT = 1;

    private final ArrayList<Book> bookList = new ArrayList<>();

    private CustomBookBaseAdapter bookListCustomBaseAdapter;

    private WebConnector webConnector;

    public LibraryInfoActivity() {
        // empty constructor
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.info_library);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.library_map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        Context mContext = getApplicationContext();

        webConnector = new WebConnector(this.getApplicationContext());
        webConnector.startWebSocket();

        loadLibraryInfo(getIntent());

        try {
            configureBookListView();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        configureButtons();
    }

    @Override
    protected void onResume() {
        super.onResume();
        webConnector.setHandler(this.handler);
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

        }
        else {
            Toast.makeText( getApplicationContext(), "Wasn't able to load library contents",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void configureBookListView() throws IOException {
        webConnector.setHandler(this.handler);
        bookList.clear();
        ListView bookListView = findViewById(R.id.library_bookListView);
        bookListCustomBaseAdapter = new CustomBookBaseAdapter(getApplicationContext(), bookList);
        bookListView.setAdapter(bookListCustomBaseAdapter);

        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
//                bookList.addAll(webConnector.getBooks(libraryId));
//                sendMessageToHandler(null);
                Log.i("ENTREI: ", String.valueOf(this.libraryId));
                webConnector.getBooks(this.libraryId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        bookListView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(LibraryInfoActivity.this, BookInfoActivity.class);
            intent.putExtra("bookTitle", bookList.get(position).getTitle());
            intent.putExtra("bookCover", bookList.get(position).getCover());
            intent.putExtra("bookBarcode", bookList.get(position).getId());
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
            webConnector.setFavouriteLibrary(libraryId);
            Toast.makeText(getApplicationContext(), "Clicked favourite button", Toast.LENGTH_SHORT).show();
        });
    }

    private void scanBarcode(int mode) {
        ScanOptions options = new ScanOptions();
        options.setPrompt("------------------Volume up to flash on-----------------");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureActivity.class);
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
            String bookBarcode = result.getContents();

            Executor executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                boolean bookExists = webConnector.bookExists(bookBarcode);
                Log.d("MensagensDebug", "Existe? " + bookExists);
                if ( bookExists )
                {
                    webConnector.checkInBook(this.libraryId, bookBarcode);
                    sendMessageToHandler(ADD_UPDATE_BOOK_LIST, webConnector.getBook(bookBarcode));
                }
                else {
                    // TODO: Esta a dar aquele erro do context e tal
                    Intent newIntent  = new Intent(this, CreateBookActivity.class);
                    newIntent.putExtra("bookId", result.getContents());
                    startActivityForResult(new Intent(this, CreateBookActivity.class),1);
                }
            });
        }
    });

    ActivityResultLauncher<ScanOptions> checkOutScanner = registerForActivityResult(new ScanContract(), result -> {
        if (result.getContents() != null)
        {
            String bookBarcode = result.getContents();

            Executor executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                boolean bookExists = webConnector.bookExists(bookBarcode);
                if ( bookExists )
                {
                    webConnector.checkOutBook(this.libraryId, bookBarcode);
                    sendMessageToHandler(REMOVE_UPDATE_BOOK_LIST, Integer.parseInt(bookBarcode));
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "Can checkout books that are not in the library", Toast.LENGTH_SHORT).show();
                }
            });
            
            // TODO: Faltar implementar a reacao ao input (Database needed)
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Checkout Result");
            builder.setMessage(result.getContents());
            builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss()).show();
        }
    });


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("MensagensDebug", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        if ( data != null ) {
            if (requestCode == 1) {
                if (resultCode == RESULT_OK) {
                    webConnector.registerBook(getBookFromResult(data));
                }
            }
        }else
        {
            Log.d("MensagensDebug", "Data is null");
        }
    }


    private Book getBookFromResult(Intent data)
    {
        Book newBook = null;
        Bundle bundle = data.getExtras();

        if ( !bundle.isEmpty() )
        {
            int bookId = Integer.parseInt(bundle.getString("bookId"));
            String bookTitle = bundle.getString("bookTitle");
            String bookDescription = bundle.getString("bookDescription");
            String base64bookCover = bundle.getString("bookEncodedImage");

            newBook = new Book(bookId, bookTitle, bookDescription, base64bookCover);
        }
        else
        {
            Toast.makeText(LibraryInfoActivity.this, "Couldn't register new book =(", Toast.LENGTH_LONG).show();
        }

        return newBook;
    }


    private void sendMessageToHandler(int msgType, Object obj) {
        Message msg = new Message();
        msg.obj = obj;
        msg.what = msgType;
        handler.sendMessage(msg);
    }

    private static final int  UPDATE_UI_MSG= 0;
    private static final int TOAST_MSG = 1;

    private static final int ADD_UPDATE_BOOK_LIST = 2;
    private static final int REMOVE_UPDATE_BOOK_LIST = 3;

    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ADD_UPDATE_BOOK_LIST:
                    bookList.add((Book) msg.obj);
                    bookListCustomBaseAdapter.notifyDataSetChanged();
                    return;
                case REMOVE_UPDATE_BOOK_LIST:
                    bookList.removeIf(book -> book.getId() == (int) msg.obj);
                    bookListCustomBaseAdapter.notifyDataSetChanged();
                    return;
                case UPDATE_UI_MSG:
                    bookListCustomBaseAdapter.notifyDataSetChanged();
                    return;
                case TOAST_MSG:
                    Toast.makeText(getApplicationContext(),
                            (String) msg.obj, Toast.LENGTH_LONG).show();
            }
        }
    };
}
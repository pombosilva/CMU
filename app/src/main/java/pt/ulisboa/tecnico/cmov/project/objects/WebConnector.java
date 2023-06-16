package pt.ulisboa.tecnico.cmov.project.objects;

import static androidx.core.content.ContextCompat.getSystemService;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;

import pt.ulisboa.tecnico.cmov.project.Constants.DomainConstants;
import pt.ulisboa.tecnico.cmov.project.Threads.LoadOfflineLibraries;
import pt.ulisboa.tecnico.cmov.project.utils.JSONFileWriter;

public class WebConnector {
    //private static final String endpoint = "https://192.92.147.96:5000";
    private static final String endpoint = "http://192.92.147.96:5000";
    private Handler handler;
    private static Context context;

    public WebConnector(Context applicationContext) {
        this.context = applicationContext;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    private static JsonReader getData(String path) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(endpoint + path).openConnection();

        /**HttpsURLConnection connection = (HttpsURLConnection) new URL(endpoint + path).openConnection();
        connection.setSSLSocketFactory((SSLSocketFactory) SSLSocketFactory.getDefault());
        connection.setHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });*/

        int respCode = connection.getResponseCode();

        if (respCode == 200) {
            JsonReader jsonReader = new JsonReader(new InputStreamReader(connection.getInputStream()));
            jsonReader.setLenient(true);
            return jsonReader;
        }
        throw new RuntimeException("Unexpected response: " + connection.getResponseMessage());
    }

    private static void putData(String path, Object data) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(endpoint + path).openConnection();

        /**HttpsURLConnection connection = (HttpsURLConnection) new URL(endpoint + path).openConnection();
        connection.setSSLSocketFactory((SSLSocketFactory) SSLSocketFactory.getDefault());
        connection.setHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                try {
                    X509Certificate certificate = (X509Certificate) session.getPeerCertificates()[0];
                    Date currentDate = new Date();
                    if (currentDate.before(certificate.getNotBefore()) || currentDate.after(certificate.getNotAfter())) {
                        return false; // Certificate is expired or not yet valid
                    }
                    Principal principal = certificate.getSubjectDN();
                    String subjectDn = principal.getName();
                    if (subjectDn.contains("CMU")) return true;
                    else return false;
                } catch (SSLPeerUnverifiedException e) {
                    throw new RuntimeException(e);
                }
            }
        });*/

        connection.setRequestMethod("PUT");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        if (data instanceof Boolean || data instanceof Integer) {
            new DataOutputStream(connection.getOutputStream())
                    .write((data.toString()).getBytes(StandardCharsets.UTF_8));
        } else {
            new DataOutputStream(connection.getOutputStream())
                    .write(("" + data.toString() + "").getBytes(StandardCharsets.UTF_8));
        }

        if (connection.getResponseCode() != 200) {
            throw new RuntimeException("Unexpected response: " + connection.getResponseMessage());
        }
    }

    public void getMarkers() throws IOException {
        try {
            JsonReader data = getData(DomainConstants.MARKERS);
            data.beginArray();
            while (data.hasNext()) {
                Message msg = new Message();
                msg.what = 2;
                msg.obj = extractMarker(data);
                handler.sendMessage(msg);
            }
            data.close();
        } catch (IOException e) {
            Message msg = new Message();
            msg.obj = "No Internet Connection";
            msg.what = 1;
            handler.sendMessage(msg);
        }
    }

    private Library extractMarker(JsonReader jReader) throws IOException {
        Gson gson = new Gson();
        return gson.fromJson(jReader, Library.class);
    }

    public int getBooks(String domain, int libraryId, int startId, String filter, int successMessageType, int unsuccessfulMessageType) throws IOException {
        int numberDownloadedBooks = 0;
        try {
            JsonReader jsonReader;
            Log.d("SearchBook", "Search Text = " + filter);
            String queryParameters = "?libraryId=" + libraryId + "&startId=" + startId + "&filter=" + filter;

            String url = domain + queryParameters;
            jsonReader = getData(url);

            jsonReader.setLenient(true);
            jsonReader.beginArray();
            while (jsonReader.hasNext()) {
                numberDownloadedBooks++;
                Message msg = new Message();
                msg.what = successMessageType;
                msg.obj = extractBook(jsonReader);
                handler.sendMessage(msg);
            }
        } catch (Exception e) {
            Message msg = new Message();
            msg.obj = "No Internet Connection";
            msg.what = unsuccessfulMessageType;
            handler.sendMessage(msg);
        }
        return numberDownloadedBooks;
    }

    private static Book extractBook(JsonReader book) throws IOException {
        Gson gson = new Gson();
        return gson.fromJson(book, Book.class);
    }

    public void getLibrariesThatContainBook(long bookBarcode, double latitude, double longitude) {
        try {
            String query = DomainConstants.BOOK_IN_LIBRARY + "?bookId=" + bookBarcode
                    + "&lat=" + latitude + "&lng=" + longitude;
            JsonReader jsonReader = getData(query);

            jsonReader.setLenient(true);
            jsonReader.beginArray();
            while (jsonReader.hasNext()) {
                Message msg = new Message();
                msg.what = 2;
                msg.obj = extractMarker(jsonReader);
                handler.sendMessage(msg);
            }
        } catch (Exception e) {
            Message msg = new Message();
            msg.obj = "Error loading Markers";
            msg.what = 1;
            handler.sendMessage(msg);
        }
    }

    public static int getNextLibID() {
        try {
            String query = DomainConstants.NEXT_LIB_ID;
            Log.i("AJUDA", getData(query).toString());
            return getData(query).nextInt();
        } catch (Exception e) {
            Log.i("NEM ENTREI LOL", "-1");
            e.printStackTrace();
        }
        return -1;
    }

    public static void setFavouriteLibrary(int libraryId) {
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                putData(DomainConstants.FAV_MARKER, libraryId);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static void setFavouriteBook(long bookId) {
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                putData(DomainConstants.FAV_BOOK, bookId);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static boolean bookExists(String bookId) {
        try {
            String query = DomainConstants.HAS_BOOK + "?bookId=" + bookId;
            return getData(query).nextBoolean();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void checkInBook(int libraryId, String bookId) {
        try {
            String data = "{\"libraryId\":" + libraryId + ",\"bookId\":" + bookId + "}";
            putData(DomainConstants.CHECK_BOOK_IN, data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void checkOutBook(int libraryId, String bookId) {
        try {
            String data = "{\"libraryId\":" + libraryId + ",\"bookId\":" + bookId + "}";
            putData(DomainConstants.CHECK_BOOK_OUT, data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void registerBook(Book newBook, int libraryId) {
        try {
            Log.d("RegisterBook", "Vou registar um livro na livraria " + libraryId);
            putData(DomainConstants.REGISTER_BOOK + "/" + libraryId, newBook.toJson());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static Book getBook(String bookId) {
        try {
            String query = DomainConstants.GET_BOOK + "?bookId=" + bookId;
            return extractBook(getData(query));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getBookCover(long bookId) {
        try {
            return getData(DomainConstants.GET_BOOK_COVER + "/" + bookId).nextString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void getContentsWithinRadius(Context ctx) {

        FusedLocationProviderClient fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(ctx);

        if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
           return;
        }
        LocationManager locationManager;

        // Inside your method or constructor
//        locationManager = (LocationManager) getSystemService(ctx, ctx.LOCATION_SERVICE);
//        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {

//        JsonReader jReader = getData("/test");
            Executors.newSingleThreadExecutor().execute(() -> {
                String query = DomainConstants.CONTENTS_WITHIN_RADIUS + "?lat=" + location.getLatitude() + "&lng=" + location.getLongitude();
                JsonReader jReader = null;
                try {
                    jReader = getData(query);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
//        Gson gson = new Gson();
//        String j = gson.toJson(data);
//                if (jReader != null) {
                    JSONFileWriter jsonFileWriter = new JSONFileWriter();
                    jsonFileWriter.writeJsonToFile(ctx, jReader, "output.json");
//                }

                Log.d("InternalStorage", "Dados Json = ");

//                Gson gson = new GsonBuilder()
//                        .registerTypeAdapter(Library.class, new LoadOfflineLibraries.LibraryDeserializer())
//                        .create();

            });
        });
    }

    public static void registerLib(Library newLib) {
        try {
            putData(DomainConstants.REGISTER_LIB, newLib.toJson());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}

package pt.ulisboa.tecnico.cmov.project.objects;

import android.content.ContentProvider;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import org.java_websocket.client.WebSocketClient;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import pt.ulisboa.tecnico.cmov.project.Constants.DomainConstants;
import pt.ulisboa.tecnico.cmov.project.utils.JSONFileWriter;

public class WebConnector {

    private static final String endpoint = "http://192.92.147.96:5000";

    private Handler handler;

    public WebConnector(Context applicationContext) {
        // empty constructor
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }



    private static JsonReader getData(String path) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(endpoint + path).openConnection();
//        Log.d("ImageDownloads", "Passei o teste");
        int respCode = connection.getResponseCode();

//        Log.d("ImageDownloads", "Passei o teste");


        if (respCode == 200) {
            JsonReader jsonReader = new JsonReader(new InputStreamReader(connection.getInputStream()));
            jsonReader.setLenient(true);
            return jsonReader;
        }
        throw new RuntimeException("Unexpected response: " + connection.getResponseMessage());
    }

    private static void putData(String path, Object data) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(endpoint + path).openConnection();
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
            String queryParameters = "?libraryId="+libraryId+"&startId="+startId+"&filter="+filter;

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
        }catch(Exception e){
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
            String query = DomainConstants.BOOK_IN_LIBRARY+"?bookId=" + bookBarcode
                    +"&lat="+latitude+"&lng="+longitude;
            JsonReader jsonReader = getData(query);

            jsonReader.setLenient(true);
            jsonReader.beginArray();
            while (jsonReader.hasNext()) {
                Message msg = new Message();
                msg.what = 2;
                msg.obj = extractMarker(jsonReader);
                handler.sendMessage(msg);
            }
        }catch(Exception e){
            Message msg = new Message();
            msg.obj = "Error loading Markers";
            msg.what = 1;
            handler.sendMessage(msg);
        }
    }

    public static void setFavouriteLibrary(int libraryId) {
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                putData(DomainConstants.FAV_MARKER,libraryId);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static void setFavouriteBook(long bookId) {
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                putData(DomainConstants.FAV_BOOK,bookId);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static boolean bookExists(String bookId) {
        try {
            String query = DomainConstants.HAS_BOOK+"?bookId=" + bookId;
            return getData(query).nextBoolean();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void checkInBook(int libraryId, String bookId) {
        try {
            String data = "{\"libraryId\":"+ libraryId +",\"bookId\":"+bookId+"}";
            putData(DomainConstants.CHECK_BOOK_IN, data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void checkOutBook(int libraryId, String bookId) {
        try {
            String data = "{\"libraryId\":"+ libraryId +",\"bookId\":"+bookId+"}";
            putData(DomainConstants.CHECK_BOOK_OUT, data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void registerBook(Book newBook, int libraryId) {
        try {
            Log.d("RegisterBook", "Vou registar um livro na livraria " + libraryId);
            putData(DomainConstants.REGISTER_BOOK + "/" +libraryId, newBook.toJson());
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

    public static String getContentsWithinRadius(Context ctx) throws IOException
    {
        JsonReader jReader = getData("/test");
//        Gson gson = new Gson();
//        String j = gson.toJson(data);

        JSONFileWriter jsonFileWriter = new JSONFileWriter();
        jsonFileWriter.writeJsonToFile(ctx, jReader, "output.json");

        Log.d("InternalStorage","Dados Json = "  );
        return "";
    }

}

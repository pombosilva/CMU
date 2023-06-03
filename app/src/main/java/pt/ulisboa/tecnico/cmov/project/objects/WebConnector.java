package pt.ulisboa.tecnico.cmov.project.objects;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.LruCache;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import org.java_websocket.client.WebSocketClient;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class WebConnector {

    private static final String endpoint = "http://192.92.147.96:5000";
    private static final String wsEndpoint = "ws://192.92.147.96:5000/ws";
    private WebSocketClient webSocketClient = null;

    private Handler handler;

    public WebConnector(Context applicationContext) {
        // empty constructor
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

//    public void startWebSocket() {
//        if (webSocketClient != null) {
//            webSocketClient.close();
//        }
//
//        try {
//            webSocketClient = new WSClient(new URI(wsEndpoint), new HashMap<>());
//            webSocketClient.connect();
//        } catch (URISyntaxException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    public void closeWebSocket() {
//        webSocketClient.close();
//    }



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
            JsonReader data = getData("/markers");
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

    private Marker extractMarker(JsonReader jReader) throws IOException {
        Gson gson = new Gson();
        return gson.fromJson(jReader, Marker.class);
    }

    /**public void getLibraryImage(int libraryId) {
        String a = "a";
        Message msg = new Message();
        try {
            JsonReader data = getData("/libraryExtras/1");
            msg.obj = data.nextString();
            data.close();
            msg.what = 2;
        } catch (IOException e)
        {
            msg.obj = "No Internet Connection";
            msg.what = 1;
        }
//        handler.sendMessage(msg);
//        return librayImage;
    }*/


    private String getBookUrl(int libraryId, boolean hasUnmeteredConnection)
    {
        String booksUrl = "/libraryBooksWithoutImage";

        if ( hasUnmeteredConnection )
        {
            booksUrl = "/libraryBooks";
        }

        if ( libraryId == -1 )
        {
            booksUrl = "/booksWithoutImage";

            if ( hasUnmeteredConnection )
            {
                booksUrl = "/books";
            }

        }
//        Log.d("ImageDownloads", "Vou usar a url: " + booksUrl);
        return booksUrl;
    }

    public int getBooks(int libraryId, int startId, boolean hasUnmeteredConnection) throws IOException {
        int numberDownloadedBooks = 0;
        try {
            JsonReader jsonReader;
            String queryParameters = "?libraryId="+libraryId+"&startId="+startId;

            String url = getBookUrl(libraryId, hasUnmeteredConnection) + queryParameters;
            Log.d("ImageDownloads", "Cheguei aqui com a url = " + url);
            jsonReader = getData(url);
            Log.d("ImageDownloads", "Passei para aqui");

            jsonReader.setLenient(true);
            jsonReader.beginArray();
            while (jsonReader.hasNext()) {
                numberDownloadedBooks++;
                Message msg = new Message();
                msg.what = 2;
                msg.obj = extractBook(jsonReader);
                handler.sendMessage(msg);
            }
        }catch(Exception e){
            Message msg = new Message();
            msg.obj = "No Internet Connection";
            msg.what = 1;
            handler.sendMessage(msg);
        }
        return numberDownloadedBooks;
    }

    private static Book extractBook(JsonReader book) throws IOException {
        Gson gson = new Gson();
        Log.d("ImageDownloads", "Cheguei aqui");
        Book newBook = gson.fromJson(book, Book.class);
        Log.d("ImageDownloads", "Novo livro = " + newBook.toString());
        return newBook;
    }

    public void getLibrariesThatContainBook(int bookBarcode) {
        try {
            String query = "/bookInLibrary?bookId=" + bookBarcode;
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
            msg.obj = "No Internet Connection";
            msg.what = 1;
            handler.sendMessage(msg);
        }
    }

    public static void setFavouriteLibrary(int libraryId) {
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                putData("/favMarker",libraryId);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static boolean bookExists(String bookId) {
        try {
            String query = "/bookExistence?bookId=" + bookId;
            return getData(query).nextBoolean();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



    public static void checkInBook(int libraryId, String bookId) {
        try {
            String data = "{\"libraryId\":"+ libraryId +",\"bookId\":"+bookId+"}";
            putData("/checkBookIn", data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void checkOutBook(int libraryId, String bookId) {
        try {
            String data = "{\"libraryId\":"+ libraryId +",\"bookId\":"+bookId+"}";
            putData("/checkBookOut", data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void registerBook(Book newBook, int libraryId) {
        try {
            Log.d("RegisterBook", "Vou registar um livro na livraria " + libraryId);
            putData("/registerBook/"+libraryId, newBook.toJson());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



    public static Book getBook(String bookId) {
        try {
            String query = "/getBook?bookId=" + bookId;
            return extractBook(getData(query));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getBookCover(int bookId) {
        try {
            return getData("/bookCover/" + bookId).nextString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}

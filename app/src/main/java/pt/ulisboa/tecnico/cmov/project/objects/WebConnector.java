package pt.ulisboa.tecnico.cmov.project.objects;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

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

    public void startWebSocket() {
        if (webSocketClient != null) {
            webSocketClient.close();
        }

        try {
            webSocketClient = new WSClient(new URI(wsEndpoint), new HashMap<>());
            webSocketClient.connect();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public void closeWebSocket() {
        webSocketClient.close();
    }

    private JsonReader getData(String path) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(endpoint + path).openConnection();
        int respCode = connection.getResponseCode();

        if (respCode == 200) {
            JsonReader jsonReader = new JsonReader(new InputStreamReader(connection.getInputStream()));
            jsonReader.setLenient(true);
            return jsonReader;
        }
        throw new RuntimeException("Unexpected response: " + connection.getResponseMessage());
    }

    private void putData(String path, Object data) throws IOException {
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

    public void getBooks(int libraryId) throws IOException {
        try {
            JsonReader jsonReader;
            if (libraryId == -1) jsonReader = getData("/books");
            else jsonReader = getData("/libraryBooks/" + libraryId);

            jsonReader.setLenient(true);
            jsonReader.beginArray();
            while (jsonReader.hasNext()) {
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
    }

    private Book extractBook(JsonReader book) throws IOException {
        Gson gson = new Gson();
        return gson.fromJson(book, Book.class);
    }

    public void getLibrariesThatContainBook(int bookBarcode) {
        try {
            JsonReader jsonReader = getData("/bookInLibrary/" + bookBarcode);

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

    public void setFavouriteLibrary(int libraryId) {
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                putData("/favMarker",libraryId);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public boolean bookExists(String bookId) {
        try {
            return getData("/bookExistence/" + bookId).nextBoolean();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void checkInBook(int libraryId, String bookId) {
        try {
            String data = "{\"libraryId\":"+ libraryId +",\"bookId\":"+bookId+"}";
            putData("/checkBookIn", data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void checkOutBook(int libraryId, String bookId) {
        try {
            String data = "{\"libraryId\":"+ libraryId +",\"bookId\":"+bookId+"}";
            putData("/checkBookOut", data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void registerBook(Book newBook) {
        try {
            putData("/registerBook", newBook.toJson());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Book getBook(String bookId) {
        try {
            return extractBook(getData("/getBook/" + bookId));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

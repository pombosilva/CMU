package pt.ulisboa.tecnico.cmov.project.objects;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.gson.Gson;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.google.gson.stream.JsonReader;

public class WebConnector {

    private static final String endpoint = "http://192.92.147.96:5000";
    private static final String wsEndpoint = "ws://192.92.147.96:5000/ws";
    private WebSocketClient webSocketClient = null;

    Handler handler;

    public WebConnector(Context applicationContext) {
        // empty constructor
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    private class WSClient extends WebSocketClient {
        public WSClient(URI serverUri, Map<String, String> httpHeaders) {
            super(serverUri, httpHeaders);
        }

        @Override
        public void onOpen(ServerHandshake handshake) {
            Log.i("MessageDebug", "Opened");
        }

        @Override
        public void onMessage(String message) {
            Log.i("MessageDebug", "Message: " + message);

            if (message != null) {
                JsonReader jsonReader = new JsonReader(new StringReader(message));
                jsonReader.setLenient(true);
            }
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            if (this != webSocketClient) return;
            Log.i("MessageDebug", (remote ? "Remotely " : "Locally ") + "Closed " + reason);
        }

        @Override
        public void onError(Exception ex) {
            if (this != webSocketClient) return;
            Log.i("MessageDebug", "Error " + ex.getMessage());
        }
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

    public void closeWebSocket()
    {
        webSocketClient.close();
    }

    //TODO: isto tem de aceitar uma string para conectar a bd
    private JsonReader getData(String path) throws IOException {
//        HttpURLConnection connection = (HttpURLConnection) new URL(endpoint + "/markers").openConnection();
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
//                    .write(("\"" + data.toString() + "\"").getBytes(StandardCharsets.UTF_8));
        }

        switch (connection.getResponseCode()) {
            case 200: {
            }
            break;
            default: {
                throw new RuntimeException("Unexpected response: " + connection.getResponseMessage());
            }
        }
    }

    public ArrayList<Marker> getMarkers() throws IOException {
        ArrayList<Marker> markers = new ArrayList<>();
        try {
            JsonReader data = getData("/markers");
            data.beginArray();
            while (data.hasNext()) {
                markers.add(extractMarkers(data));
            }
            data.close();
        } catch (IOException e)
        {
            Message msg = new Message();
            msg.obj = "No Internet Connection";
            msg.what = 1;
            handler.sendMessage(msg);
        }
        return markers;
    }

    private Marker extractMarkers(JsonReader jReader) throws IOException {
        Gson gson = new Gson();
        return gson.fromJson(jReader, Marker.class);
    }

    public void getLibraryImage(int libraryId) {
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
    }

    public void getBooks(int libraryId) throws IOException {
        JsonReader jsonReader;
        if (libraryId == -1) jsonReader = getData("/books");
        else jsonReader = getData("/libraryBooks/" + libraryId);

        jsonReader.setLenient(true);
        jsonReader.beginArray();
        while (jsonReader.hasNext()) {
            Message msg = new Message();
            msg.what = 2;
            msg.obj = extractBook(jsonReader);
            try {
                handler.sendMessage(msg);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private Book extractBook(JsonReader book) throws IOException {
        Gson gson = new Gson();
        Book b = gson.fromJson(book, Book.class);
        Log.i("BOOKSSS: ", b.toJson());
        Log.i("BOOK: ", b.toString());
        return b;
    }

    public void setLibraryFav(int libraryId) {
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

    public void checkBookIn(int libraryId, String bookId) {
        try {
            String data = "{\"libraryId\":"+ libraryId +",\"bookId\":"+bookId+"}";
            putData("/checkBookIn", data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void checkBookOut(int libraryId, String bookId) {
        try {
            String data = "{\"libraryId\":"+ libraryId +",\"bookId\":"+bookId+"}";
            putData("/checkBookOut", data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void registerBook(Book newBook)
    {
        try {
            putData("/registerBook", newBook.toJson());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Book getBook(String bookId)
    {
        try {
            return extractBook(getData("/getBook/" + bookId));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

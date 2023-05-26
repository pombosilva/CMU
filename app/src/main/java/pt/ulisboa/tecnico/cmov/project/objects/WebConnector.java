package pt.ulisboa.tecnico.cmov.project.objects;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.JsonReader;
import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import pt.ulisboa.tecnico.cmov.project.adapters.CustomBaseAdapter;

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

    //TODO: isto tem de aceitar uma string para conectar a bd
    private JsonReader getData() throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(endpoint + "/markers").openConnection();
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
                    .write(("\"" + data.toString() + "\"").getBytes(StandardCharsets.UTF_8));
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
            JsonReader data = getData();
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

    // TODO: Mudar isto para o class GSON
    private Marker extractMarkers(JsonReader jReader) throws IOException {
        jReader.beginObject();

        jReader.nextName();
        String libraryImage = jReader.nextString();

        jReader.nextName();
        boolean markerFav = jReader.nextBoolean();

        jReader.nextName();
        int markerId = jReader.nextInt();

        jReader.nextName();
        double markerLat =  jReader.nextDouble();

        jReader.nextName();
        double markerLng =  jReader.nextDouble();

        jReader.nextName();
        String markerName = jReader.nextString();

        jReader.endObject();

        return new Marker(markerId,markerName, markerLat, markerLng, markerFav,libraryImage);
    }

    // TODO: Isto ta so nojento. Usar uma funcao pa conectar(getData) e esta so aplica os resultados
    public ArrayList<Book> getBooks(int libraryId/*, CustomBaseAdapter customBaseAdapter*/) throws IOException{
        ArrayList<Book> books = new ArrayList<>();
        URL url = new URL("http://192.92.147.96:5000/libraryBooks/" + libraryId);

        if(libraryId == -1)
            url = new URL("http://192.92.147.96:5000/books");

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            InputStream inputStream = connection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                //TODO: Fazer disto uma thread para meter na UI
                response.append(line);
            }
            bufferedReader.close();

            String jsonResponse = response.toString();
            Gson gson = new Gson();
            Log.i("RESPONSE: ", jsonResponse);
            Type bookListType = new TypeToken<List<Book>>() {}.getType();
            books = gson.fromJson(jsonResponse, bookListType);
        }

        connection.disconnect();
        return books;
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
}

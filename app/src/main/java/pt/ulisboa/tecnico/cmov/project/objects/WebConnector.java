package pt.ulisboa.tecnico.cmov.project.objects;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.JsonReader;
import android.util.Log;

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

public class WebConnector {

    private static final String endpoint = "http://192.92.147.96:5000";
    private static final String wsEndpoint = "ws://192.92.147.96:5000/ws";
    private WebSocketClient webSocketClient = null;

    Handler handler;

    public WebConnector(Context applicationContext) {
        // empty constructor
    }


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

    private void putData(Object data) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(endpoint + "/favMarker").openConnection();
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

        if (connection.getResponseCode() != 200) {
            throw new RuntimeException("Unexpected response: " + connection.getResponseMessage());
        }
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
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

    public void setLibraryFav(int libraryId) {
        try {
            putData(libraryId);
        } catch (IOException e) {
            System.err.println("Error sending favourite value");
            throw new RuntimeException(e);
        }
    }

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
}

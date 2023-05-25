package pt.ulisboa.tecnico.cmov.project.objects;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.JsonReader;
import android.util.Log;
import android.widget.Toast;

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

import pt.ulisboa.tecnico.cmov.project.activities.MainActivity;

public class WebConnector {

    private static final String endpoint = "http://192.92.147.96:5000";
    private static final String wsEndpoint = "ws://192.92.147.96:5000/ws";
    private WebSocketClient webSocketClient = null;

    private Context context;

    public WebConnector(Context context)
    {
        this.context = context;
    }
    private JsonReader getData(String path) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(endpoint + path).openConnection();
        int respCode = connection.getResponseCode();

        switch (respCode) {
            case 200: {
                JsonReader jsonReader = new JsonReader(new InputStreamReader(connection.getInputStream()));
                jsonReader.setLenient(true);
                Log.d("MensagensDebug","DDDDDDDDDDDDDDDDDDDDDDDDDDDD");
                return jsonReader;
            }
            default: {
                throw new RuntimeException("Unexpected response: " + connection.getResponseMessage());
            }
        }
    }

    private void putData(String path, Object data) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(endpoint + path).openConnection();
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        if (data instanceof Boolean || data instanceof Integer) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                new DataOutputStream(connection.getOutputStream())
                        .write((data.toString()).getBytes(StandardCharsets.UTF_8));
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                new DataOutputStream(connection.getOutputStream())
                        .write(("\"" + data.toString() + "\"").getBytes(StandardCharsets.UTF_8));
            }
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

    public JsonReader extractNoteContent(JsonReader jReader) throws IOException
    {
        jReader.beginObject();
        jReader.nextName();
        String noteTitle = jReader.nextString();
//        Log.d("MensagensDebugNotes", "Notes super : " + noteTitle);
        jReader.nextName();
        String noteText =  jReader.nextString();
//        Log.d("MensagensDebugNotes", "Notes : " + noteText);

        jReader.nextName();
        String encodedImage = jReader.nextString();
//        Log.d("MensagensDebugNotes", "Notes : " + encodedImage);

        jReader.endObject();

//        handler.sendEmptyMessage(0);
        return jReader;
    }

    public ArrayList<Marker> getMarkers() throws IOException
    {
        ArrayList<Marker> markers = new ArrayList<Marker>();
        try {
            JsonReader data = getData("/markers");
            data.beginArray();
            while ( data.hasNext() )
            {
                markers.add(extractMarkers(data));
            }
            data.close();
        } catch ( IOException e )
        {
            Message msg = new Message();
            msg.obj = "No Internet Connection";
            msg.what = 1;
            handler.sendMessage(msg);
//            Looper.prepare();
//            Handler mHandler = new Handler() {
//                public void handleMessage(Message msg) {
//                    Toast.makeText(context, "No internet connection", Toast.LENGTH_LONG).show();
//                }
//            };
//            Looper.loop();

        }
        return markers;
    }

    private Marker extractMarkers(JsonReader jReader) throws IOException
    {
        jReader.beginObject();
        jReader.nextName();
         boolean markerFav = jReader.nextBoolean();
//        Log.d("MensagensDebugNotes", "Notes super : " + noteTitle);
        jReader.nextName();
        double markerLat =  jReader.nextDouble();
//        Log.d("MensagensDebugNotes", "Notes : " + noteText);
        jReader.nextName();
        double markerLng =  jReader.nextDouble();
//        Log.d("MensagensDebugNotes", "Notes : " + noteText);

        jReader.nextName();
        String markerName = jReader.nextString();
//        Log.d("MensagensDebugNotes", "Notes : " + encodedImage);

        jReader.endObject();

        return new Marker(markerName, markerLat, markerLng, markerFav);
    }

    public String getNotes() throws IOException {
        String notes = "";
        JsonReader data = getData("/notes");
        data.beginArray();
        while ( data.hasNext() )
        {
            extractNoteContent(data);
        }
        data.close();
        return notes;
    }

//    public void setNotes(ArrayList<Note> notes) throws IOException {
//        putData("/notes", notes);
//    }

    public void startWebSocket() {
        if (webSocketClient != null) {
            webSocketClient.close();
        }

        try {
            webSocketClient = new WSClient(new URI(wsEndpoint), new HashMap<String,String>());
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
        public void onOpen(ServerHandshake handshakedata) {
            Log.i("MensagensDebug", "Opened");
        }

        @Override
        public void onMessage(String message) {
            Log.i("MensagensDebug", "Message: " + message);

            if (message != null) {
//                try {
                JsonReader jsonReader = new JsonReader(new StringReader(message));
                jsonReader.setLenient(true);
                Log.i("MensagensDebug", "aljherkwbhcrkwsehncrlkekl ");
//                    boolean state = jsonReader.nextBoolean();
//                    runOnUiThread(() -> {
//                        ((Switch) findViewById(R.id.state_switch)).setChecked(state);
//                    });
//                } catch (IOException e) {
//                    throw new RuntimeException("Malformed message on websocket", e);
//                }
            }
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            if (this != webSocketClient) return;
            Log.i("MensagensDebug", (remote ? "Remotely " : "Locally ") + "Closed " + reason);
        }

        @Override
        public void onError(Exception ex) {
            if (this != webSocketClient) return;
            Log.i("MensagensDebug", "Error " + ex.getMessage());
        }
    }



    Handler handler;
    public void setHandler(Handler handler)
    {
        this.handler = handler;
    }
}

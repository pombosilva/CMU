package pt.ulisboa.tecnico.cmov.project.objects;

import android.util.Log;

import com.google.gson.stream.JsonReader;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.StringReader;
import java.net.URI;
import java.util.Map;

public class WSClient extends WebSocketClient {

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

    }

    @Override
    public void onError(Exception ex) {

    }
}

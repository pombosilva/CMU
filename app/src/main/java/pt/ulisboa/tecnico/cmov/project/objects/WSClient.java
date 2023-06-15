package pt.ulisboa.tecnico.cmov.project.objects;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.Executors;

import pt.ulisboa.tecnico.cmov.project.R;
import pt.ulisboa.tecnico.cmov.project.utils.NetworkUtils;

public class WSClient extends WebSocketClient {

    private final Context context;
    private static final String CHANNEL_ID = "1";
    private NotificationManager notificationManager;
    public String[] availableFavBooks;

    public WSClient(URI serverUri, Map<String, String> httpHeaders, Context context) {
        super(serverUri, httpHeaders);
        this.context = context;
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        Log.i("MessageDebug", "Opened");
    }

    @Override
    public void onMessage(String message) {
        if (message != null) {

            String cleanedString = message.
                    replace("[", "").replace("]", "").
                    replace("'", "");

            if(!cleanedString.replace(" ","").equals("")) {
                availableFavBooks = cleanedString.split(", ");
                checkIfFavBookIsAvailable();
            }
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {

    }

    @Override
    public void onError(Exception ex) {

    }

    private void checkIfFavBookIsAvailable() {
        if ( NetworkUtils.hasUnmeteredConnection(context)) {
            Executors.newSingleThreadExecutor().execute(() -> {
                    // create notificationManager
                    notificationManager =
                            context.getSystemService(NotificationManager.class);
                    // create notification channel
                    createNotificationChannel();

                    for (int i = 0; i != availableFavBooks.length; i++) {
                        //check if notification is still active
                        if (!getActiveNotificationIDs().contains(i)) {
                            // send notification for each fav book available
                            sendNotification(i);
                        }

                    }
            });
        }
    }

    public void createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "notificationChannel",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public ArrayList<Integer> getActiveNotificationIDs(){
        ArrayList<Integer> activeIds = new ArrayList<>();
        StatusBarNotification[] activeNotifications = notificationManager.getActiveNotifications();
        for(StatusBarNotification notification: activeNotifications)
            activeIds.add(notification.getId());
        return activeIds;
    }

    public void sendNotification(Integer i){
        String notif = availableFavBooks[i];
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.unloaded_book)
                .setContentText(notif)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        notificationManager.notify(i, builder.build());
    }
}

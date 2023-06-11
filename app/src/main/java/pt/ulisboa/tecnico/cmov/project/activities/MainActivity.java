package pt.ulisboa.tecnico.cmov.project.activities;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentProvider;
import android.content.ContextWrapper;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Executors;

import pt.ulisboa.tecnico.cmov.project.Constants.DomainConstants;
import pt.ulisboa.tecnico.cmov.project.R;
import pt.ulisboa.tecnico.cmov.project.databinding.ActivityMainBinding;
import pt.ulisboa.tecnico.cmov.project.fragments.BooksFragment;
import pt.ulisboa.tecnico.cmov.project.fragments.MapFragment;
import pt.ulisboa.tecnico.cmov.project.fragments.UserFragment;
import pt.ulisboa.tecnico.cmov.project.objects.Book;
import pt.ulisboa.tecnico.cmov.project.objects.WebConnector;
import pt.ulisboa.tecnico.cmov.project.utils.InternalStorage;
import pt.ulisboa.tecnico.cmov.project.utils.NetworkUtils;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    private WebConnector webConnector;
    private static final String CHANNEL_ID = "1";

    private NotificationManager notificationManager;
    private ArrayList<Book> availableFavBooks;

    public MainActivity(){
        //empty constructor
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        webConnector = new WebConnector(this.getApplicationContext());

        new StoreRadiusLibrariesContent(this).start();

        replaceFragment(new MapFragment(webConnector));

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.map) {
                replaceFragment(new MapFragment(webConnector));
            } else if (item.getItemId() == R.id.books) {
                replaceFragment(new BooksFragment(webConnector));
            } else if (item.getItemId() == R.id.user) {
                replaceFragment(new UserFragment());
            }
            return true;
        });
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkIfFavBookIsAvailable();
//        webConnector.startWebSocket();
    }

    @Override
    public void onStop() {
        super.onStop();
//        webConnector.closeWebSocket();
    }


    private class StoreRadiusLibrariesContent extends Thread
    {

        private final ContextWrapper ctxWrp;
        public StoreRadiusLibrariesContent(ContextWrapper ctxWrp)
        {
            this.ctxWrp = ctxWrp;
        }
        @Override
        public void run()
        {
            try {
                Log.d("InternalStorage","Vou fazer download das livrarias");
                String librariesContents = WebConnector.getContentsWithinRadius(getApplicationContext());
                InternalStorage.write("contents.json", librariesContents, ctxWrp);
                Log.d("InternalStorage","Fiz download das livrarias");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }



















    private void checkIfFavBookIsAvailable() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // get every fav book available
                availableFavBooks = WebConnector.getAvailableFavBooks();

                //check if there is any fav book available
                if (!availableFavBooks.isEmpty()) {
                    // create notificationManager
                    notificationManager = getSystemService(NotificationManager.class);

                    // create notification channel
                    createNotificationChannel();

                    for(int i = 0; i!= availableFavBooks.size(); i++) {
                        //check if notification is still active
                        if(!getActiveNotificationIDs().contains(i)) {
                            // send notification for each fav book available
                            sendNotification(i);
                        }

                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
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
        Book book = availableFavBooks.get(i);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.unloaded_book)
                .setContentTitle(book.getTitle())
                .setContentText(book.getDescription())
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        notificationManager.notify(i, builder.build());
    }
}
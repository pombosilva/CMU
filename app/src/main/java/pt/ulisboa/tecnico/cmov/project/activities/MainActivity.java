package pt.ulisboa.tecnico.cmov.project.activities;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
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

import pt.ulisboa.tecnico.cmov.project.R;
import pt.ulisboa.tecnico.cmov.project.Threads.LoadOfflineLibraries;
import pt.ulisboa.tecnico.cmov.project.databinding.ActivityMainBinding;
import pt.ulisboa.tecnico.cmov.project.fragments.BooksFragment;
import pt.ulisboa.tecnico.cmov.project.fragments.MapFragment;
import pt.ulisboa.tecnico.cmov.project.fragments.UserFragment;
import pt.ulisboa.tecnico.cmov.project.objects.Book;
import pt.ulisboa.tecnico.cmov.project.objects.Cache;
import pt.ulisboa.tecnico.cmov.project.objects.WebConnector;
import pt.ulisboa.tecnico.cmov.project.utils.NetworkUtils;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    private static final String CHANNEL_ID = "1";

    private NotificationManager notificationManager;
    private ArrayList<Book> availableFavBooks;

    private Cache cache;

    public MainActivity(){
        //empty constructor
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState==null){
            replaceFragment(new MapFragment(this.cache));
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        cache = Cache.getInstance();

        new LoadOfflineLibraries(this).start();

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.map) {
                replaceFragment(new MapFragment(this.cache));
            } else if (item.getItemId() == R.id.books) {
                replaceFragment(new BooksFragment());
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
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void checkIfFavBookIsAvailable() {
        if ( NetworkUtils.hasUnmeteredConnection(getApplicationContext())) {
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

                        for (int i = 0; i != availableFavBooks.size(); i++) {
                            //check if notification is still active
                            if (!getActiveNotificationIDs().contains(i)) {
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
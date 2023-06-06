package pt.ulisboa.tecnico.cmov.project.activities;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import pt.ulisboa.tecnico.cmov.project.R;
import pt.ulisboa.tecnico.cmov.project.databinding.ActivityMainBinding;
import pt.ulisboa.tecnico.cmov.project.fragments.BooksFragment;
import pt.ulisboa.tecnico.cmov.project.fragments.MapFragment;
import pt.ulisboa.tecnico.cmov.project.fragments.UserFragment;
import pt.ulisboa.tecnico.cmov.project.objects.Book;
import pt.ulisboa.tecnico.cmov.project.objects.WebConnector;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    private WebConnector webConnector;

    public MainActivity(){
        //empty constructor
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkIfFavBookIsAvailable();

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        webConnector = new WebConnector(this.getApplicationContext());

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

    private void checkIfFavBookIsAvailable() {
        Executors.newSingleThreadExecutor().execute(() -> {
            //TODO: SEND NOTIFICATION WHEN BOOK IS FAV AND IS AVAILABLE
            try {
                ArrayList<Book> availableFavBooks = WebConnector.getAvailableFavBooks();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationChannel channel = new NotificationChannel("1", "test", NotificationManager.IMPORTANCE_DEFAULT);
                    channel.setDescription("this is a test");
                    channel.enableLights(true);
                    channel.setLightColor(Color.GREEN);

                    NotificationManager notificationManager = getSystemService(NotificationManager.class);
                    notificationManager.createNotificationChannel(channel);
                }

                NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "1")
                        .setSmallIcon(R.drawable.unloaded_book)
                        .setContentTitle(availableFavBooks.get(0).getTitle())
                        .setContentText(availableFavBooks.get(0).getDescription())
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);
                NotificationManager notificationManager = getSystemService(NotificationManager.class);
                notificationManager.notify(1, builder.build());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
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
//        webConnector.startWebSocket();
    }

    @Override
    public void onStop() {
        super.onStop();
//        webConnector.closeWebSocket();
    }
}
package pt.ulisboa.tecnico.cmov.project.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import org.java_websocket.client.WebSocketClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

import pt.ulisboa.tecnico.cmov.project.R;
import pt.ulisboa.tecnico.cmov.project.Threads.LoadOfflineLibraries;
import pt.ulisboa.tecnico.cmov.project.databinding.ActivityMainBinding;
import pt.ulisboa.tecnico.cmov.project.fragments.BooksFragment;
import pt.ulisboa.tecnico.cmov.project.fragments.MapFragment;
import pt.ulisboa.tecnico.cmov.project.fragments.UserFragment;
import pt.ulisboa.tecnico.cmov.project.objects.Cache;
import pt.ulisboa.tecnico.cmov.project.objects.WSClient;

public class MainActivity extends AppCompatActivity{
    private Cache cache;
    private static final String wsEndpoint = "ws://192.92.147.96:5000/ws";
    private WebSocketClient webSocketClient = null;

    public MainActivity() {
        //empty constructor
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        startWebSocket();

        if(savedInstanceState==null){
            replaceFragment(new MapFragment(this.cache));
        }

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
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

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    public void startWebSocket() {
        if (webSocketClient != null) {
            webSocketClient.close();
        }

        try {
            webSocketClient = new WSClient(new URI(wsEndpoint),
                    new HashMap<>(), getApplicationContext());
            webSocketClient.connect();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }
}
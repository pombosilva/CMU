package pt.ulisboa.tecnico.cmov.project.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.util.Log;
import android.widget.Toast;

public class NetworkUtils {

    public static boolean hasUnmeteredConnection(Context ctx) {

        if ( ctx == null )
            Log.d("NetworkUtils", "Context is null");

        final ConnectivityManager connMgr = (ConnectivityManager)
                ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        final android.net.NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        final android.net.NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (wifi.isConnectedOrConnecting ()) {
            Log.d("ImageDownloads", "Wifi is being used");
            return true;
        } else {
            Log.d("ImageDownloads", "Mobile3G is being used");
            return false;
        }
    }
}
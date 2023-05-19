package pt.ulisboa.tecnico.cmov.project;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class CustomWindowInfoAdapter implements GoogleMap.InfoWindowAdapter{

    private final View mWindow;
    private Context mContext;

    public CustomWindowInfoAdapter(Context context)
    {
        mContext = context;
        mWindow = LayoutInflater.from(context).inflate(R.layout.activity_library_info_marker, null);
    }

    private void rendowWindowText(Marker marker, View view)
    {
        String title = marker.getTitle();
        TextView tvTitle = (TextView) view.findViewById(R.id.library_name);

        if ( tvTitle !=null && !title.equals(""))
        {
            tvTitle.setText(title);
        }

        String snippet = marker.getSnippet();
        TextView tvSnipppet = (TextView) view.findViewById(R.id.library_description);

        if ( snippet != null && !snippet.equals(""))
        {
            tvSnipppet.setText(snippet);
        }


        ImageView libraryImageView = (ImageView) view.findViewById(R.id.library_image);

        if ( libraryImageView != null )
            libraryImageView.setImageResource(R.drawable.img);

    }

    @Nullable
    @Override
    public View getInfoWindow(@NonNull Marker marker) {
        rendowWindowText(marker, mWindow);
        return mWindow;
    }

    @Nullable
    @Override
    public View getInfoContents(@NonNull Marker marker) {
        rendowWindowText(marker, mWindow);
        return mWindow;
    }
}
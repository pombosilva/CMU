package pt.ulisboa.tecnico.cmov.project.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import android.util.Base64;

import pt.ulisboa.tecnico.cmov.project.R;

public class CustomWindowInfoAdapter implements GoogleMap.InfoWindowAdapter{

    private final View mWindow;
    private Context mContext;

    public CustomWindowInfoAdapter(Context context)
    {
        mContext = context;
        mWindow = LayoutInflater.from(context).inflate(R.layout.info_marker, null);
    }

    private void rendowWindowText(Marker marker, View view)
    {
        String title = marker.getTitle();
        TextView tvTitle = (TextView) view.findViewById(R.id.library_name);

        if ( tvTitle !=null && !title.equals(""))
        {
            tvTitle.setText(title);
        }

//        String snippet = marker.getSnippet().split(":")[1];
//        ImageView libraryImageView = (ImageView) view.findViewById(R.id.library_image);
//        TextView tvSnipppet = (TextView) view.findViewById(R.id.library_description);

//        if ( snippet != null && !snippet.equals(""))
//        {
//            byte[] decodedString = Base64.decode(snippet,Base64.DEFAULT);
//            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
//            libraryImageView.setImageBitmap(decodedByte);
////            tvSnipppet.setText(snippet);
//        }


//        ImageView libraryImageView = (ImageView) view.findViewById(R.id.library_image);
//
//        if ( libraryImageView != null )
//            libraryImageView.setImageResource(R.drawable.img);


//        if ( libraryImageView != null )
//            libraryImageView.setImageResource(R.drawable.img);

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
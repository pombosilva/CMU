package pt.ulisboa.tecnico.cmov.project.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import pt.ulisboa.tecnico.cmov.project.R;

public class CustomMarkerBaseAdapter implements GoogleMap.InfoWindowAdapter{
    private final View mWindow;

    @SuppressLint("InflateParams")
    public CustomMarkerBaseAdapter(Context context) {
        mWindow = LayoutInflater.from(context).
                inflate(R.layout.info_marker, null);
    }

    private void renderMarkerText(Marker marker, View view) {
        String title = marker.getTitle();
        TextView tvTitle = view.findViewById(R.id.library_name);

        if (tvTitle != null) {
            assert title != null;
            if (!title.equals("")) {
                tvTitle.setText(title);
            }
        }
    }

    @Nullable
    @Override
    public View getInfoWindow(@NonNull Marker marker) {
        renderMarkerText(marker, mWindow);
        return mWindow;
    }

    @Nullable
    @Override
    public View getInfoContents(@NonNull Marker marker) {
        renderMarkerText(marker, mWindow);
        return mWindow;
    }
}
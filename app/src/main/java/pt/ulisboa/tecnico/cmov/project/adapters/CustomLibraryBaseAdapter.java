package pt.ulisboa.tecnico.cmov.project.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import pt.ulisboa.tecnico.cmov.project.R;
import pt.ulisboa.tecnico.cmov.project.objects.Library;
import pt.ulisboa.tecnico.cmov.project.utils.ImageUtils;


public class CustomLibraryBaseAdapter extends BaseAdapter {
    Context context;
    ArrayList<Library> list;
    LayoutInflater inflater;

    public CustomLibraryBaseAdapter(Context ctx, ArrayList<Library> list) {
        this.context = ctx;
        this.list = list;
        inflater = LayoutInflater.from(ctx);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @SuppressLint({"ViewHolder", "InflateParams", "SetTextI18n"})
    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        convertView = inflater.inflate(R.layout.info_library_list_view, null);
        TextView textView = convertView.findViewById(R.id.libraryTitle);
        textView.setText(list.get(i).getName());

        ImageView imageCoverIm = convertView.findViewById(R.id.libraryIcon);
        Bitmap bitmap = ImageUtils.decodeBase64ToBitmap(list.get(i).getCover());
        imageCoverIm.setImageBitmap(bitmap);

        TextView distance = convertView.findViewById(R.id.libraryDistance);
        distance.setText((list.get(i).getDistance())+"km");
        return convertView;
    }
}

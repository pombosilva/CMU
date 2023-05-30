package pt.ulisboa.tecnico.cmov.project.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import pt.ulisboa.tecnico.cmov.project.R;
import pt.ulisboa.tecnico.cmov.project.objects.Book;
import pt.ulisboa.tecnico.cmov.project.utils.ImageUtils;


public class CustomBookBaseAdapter extends BaseAdapter {
    Context context;
    ArrayList<Book> list;
    LayoutInflater inflater;

    public CustomBookBaseAdapter(Context ctx, ArrayList<Book> list) {
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

    @SuppressLint({"ViewHolder", "InflateParams"})
    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        convertView = inflater.inflate(R.layout.info_book_list_view, null);
        TextView textView = convertView.findViewById(R.id.textView);
        ImageView fruitImg = convertView.findViewById(R.id.imageIcon);
        textView.setText(list.get(i).getTitle());

        String encodedCover =  list.get(i).getCover();
        fruitImg.setImageBitmap(ImageUtils.decodeBase64ToBitmap(encodedCover));

        return convertView;
    }
}

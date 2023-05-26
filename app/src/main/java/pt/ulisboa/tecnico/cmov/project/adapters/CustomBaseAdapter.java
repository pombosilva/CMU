package pt.ulisboa.tecnico.cmov.project.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
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


public class CustomBaseAdapter extends BaseAdapter {


    Context context;
    ArrayList<Book> bookList;
    LayoutInflater inflater;

    public CustomBaseAdapter(Context ctx, ArrayList<Book> bookList)
    {
        this.context = ctx;
        this.bookList = bookList;
        inflater = LayoutInflater.from(ctx);
    }

    @Override
    public int getCount() {
        return bookList.size();
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
        textView.setText(bookList.get(i).getTitle());

        String encodedCover =  bookList.get(i).getCover();
        fruitImg.setImageBitmap(ImageUtils.decodeBase64ToBitmap(encodedCover));

        return convertView;
    }
}

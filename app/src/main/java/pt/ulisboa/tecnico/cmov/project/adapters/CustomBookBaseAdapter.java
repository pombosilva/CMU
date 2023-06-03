package pt.ulisboa.tecnico.cmov.project.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import pt.ulisboa.tecnico.cmov.project.R;
import pt.ulisboa.tecnico.cmov.project.activities.MainActivity;
import pt.ulisboa.tecnico.cmov.project.fragments.BooksFragment;
import pt.ulisboa.tecnico.cmov.project.objects.Book;
import pt.ulisboa.tecnico.cmov.project.objects.WebConnector;
import pt.ulisboa.tecnico.cmov.project.utils.ImageUtils;


public class CustomBookBaseAdapter extends BaseAdapter {
    Context context;
    ArrayList<Book> list;
    LayoutInflater inflater;

    private Handler handler;


    public CustomBookBaseAdapter(Context ctx, ArrayList<Book> list) {
        this.context = ctx;
        this.list = list;
        inflater = LayoutInflater.from(ctx);
    }


    public void setHandler(Handler handler)
    {
        this.handler = handler;
    }

    public void addListItemAdapter(ArrayList<Book> list)
    {
        this.list.addAll(list);
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
        ImageView bookCoverImgVw = convertView.findViewById(R.id.imageIcon);





        textView.setText(list.get(i).getTitle());

        String encodedCover =  list.get(i).getCover();
        if ( encodedCover == null ) {
            Log.d("ImageDownloads", "A cover e null");
            bookCoverImgVw.setImageResource(Book.unloadedBookCover);
            bookCoverImgVw.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("ImageDownloads", "Cliquei na imagem");
                    Executor executor = Executors.newSingleThreadExecutor();
                    executor.execute( () ->
                            {
                                int bookBarcode = list.get(i).getId();
                                String encodedBookCover = WebConnector.getBookCover(bookBarcode);

                                Message msg = new Message();
                                msg.what = BooksFragment.UPDATE_BOOK_COVER;
                                msg.obj = i+":"+encodedBookCover;

                                handler.sendMessage(msg);
//                                bookCoverImgVw.setImageBitmap(encodedBookCover);
                            }
                    );
                }
            });
        }
        else
            bookCoverImgVw.setImageBitmap(ImageUtils.decodeBase64ToBitmap(encodedCover));
        return convertView;
    }
}

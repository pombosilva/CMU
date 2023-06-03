package pt.ulisboa.tecnico.cmov.project.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import pt.ulisboa.tecnico.cmov.project.R;
import pt.ulisboa.tecnico.cmov.project.activities.BookInfoActivity;
import pt.ulisboa.tecnico.cmov.project.adapters.CustomBookBaseAdapter;
import pt.ulisboa.tecnico.cmov.project.objects.Book;
import pt.ulisboa.tecnico.cmov.project.objects.WebConnector;
import pt.ulisboa.tecnico.cmov.project.utils.ImageUtils;
import pt.ulisboa.tecnico.cmov.project.utils.NetworkUtils;

public class BooksFragment extends Fragment {
    private final ArrayList<Book> bookList = new ArrayList<>();
    private final ArrayList<Book> tempBookList = new ArrayList<>();
    private ListView bookListView;
    private CustomBookBaseAdapter bookListCustomBaseAdapter;
    private final WebConnector webConnector;

    private int currentlyDisplayedBooks=0;

    public BooksFragment(WebConnector webConnector) {
        this.webConnector = webConnector;
        webConnector.setHandler(this.handler);
    }

    @SuppressWarnings("unused")
    public static BooksFragment newInstance(WebConnector webConnector, int columnCount) {
        BooksFragment fragment = new BooksFragment(webConnector);
        Bundle args = new Bundle();
        args.putInt("column-count", columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_books, container, false);

        bookListView = rootView.findViewById(R.id.bookListView);

        bookListCustomBaseAdapter = new CustomBookBaseAdapter(getContext(), bookList);
        bookListCustomBaseAdapter.setHandler(this.handler);
        bookListView.setAdapter(bookListCustomBaseAdapter);

        ftView = inflater.inflate(R.layout.footer_view, null);

        bookListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if ( !isLoading && view.getLastVisiblePosition() == currentlyDisplayedBooks - 1 && currentlyDisplayedBooks!=0)
                {
                    isLoading = true;
                    Log.d("BooksFragment", "Vou correr a thread");
                    Thread thread = new ThreadGetMoreBooks();
                    thread.start();
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }
        });

        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                isLoading = true;
                handler.sendEmptyMessage(ENABLE_LOADING_FOOTER);
                currentlyDisplayedBooks += webConnector.getBooks(-1, 0, NetworkUtils.hasUnmeteredConnection(this.getContext()));
                handler.sendEmptyMessage(DISABLE_LOADING_FOOTER);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        bookListView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(getActivity(), BookInfoActivity.class);
            intent.putExtra("bookTitle", bookList.get(position).getTitle());
            intent.putExtra("bookCover", bookList.get(position).getCover());
            intent.putExtra("bookBarcode", bookList.get(position).getId());
            startActivity(intent);
        });

        // when you click on an item it displays its information
        Button searchButton = rootView.findViewById(R.id.searchBookButton);
        searchButton.setOnClickListener(this::searchBook);

        return rootView;
    }

    public void searchBook(View view) {
        tempBookList.clear();
        EditText searchText = requireView().findViewById(R.id.searchBookText);
        CustomBookBaseAdapter adapter = new CustomBookBaseAdapter(bookListView.getContext(), tempBookList);
        bookListView.setAdapter(adapter);

        for (Book b: bookList) {
            if (b.getTitle().contains(searchText.getText().toString())) {
                tempBookList.add(b);
                adapter.notifyDataSetChanged();
            }
        }

        closeKeyboard(view);
    }

    public void closeKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


    private View  ftView;
    private boolean isLoading = false;

    private static final int NO_INTERNET = 1;
    private static final int UPDATE_BOOK_LIST = 2;

    private static final int ENABLE_LOADING_FOOTER = 3;

    private static final int DISABLE_LOADING_FOOTER = 4;

    public static final int UPDATE_BOOK_COVER = 5;


    private final Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_BOOK_LIST:
                    bookList.add((Book) msg.obj);
                    bookListCustomBaseAdapter.notifyDataSetChanged();
                    return;
                case ENABLE_LOADING_FOOTER:
                    bookListView.addFooterView(ftView);
                    break;
                case DISABLE_LOADING_FOOTER:
                    isLoading = false;
                    bookListView.removeFooterView(ftView);
                    break;
                case UPDATE_BOOK_COVER:
                    String[] objs = ((String) msg.obj).split(":");
                    int bookId = Integer.parseInt(objs[0]);
                    String encodedImage = objs[1];
                    bookList.get(bookId).setCover(encodedImage);
                    Log.d("ImageDownloads", "Vou dar update ha imagem");
                    bookListCustomBaseAdapter.notifyDataSetChanged();
                    break;
                case NO_INTERNET:
                    Toast.makeText(getActivity(), (String) msg.obj, Toast.LENGTH_LONG).show();

            }
        }
    };

    private class ThreadGetMoreBooks extends Thread
    {
        @Override
        public void run()
        {
            handler.sendEmptyMessage(ENABLE_LOADING_FOOTER);
            try {
                currentlyDisplayedBooks += webConnector.getBooks(-1, currentlyDisplayedBooks, NetworkUtils.hasUnmeteredConnection(getContext()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            handler.sendEmptyMessage(DISABLE_LOADING_FOOTER);
        }
    }

}
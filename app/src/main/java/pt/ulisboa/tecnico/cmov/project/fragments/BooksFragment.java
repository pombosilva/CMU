package pt.ulisboa.tecnico.cmov.project.fragments;

import android.content.Context;
import android.content.Intent;
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

import pt.ulisboa.tecnico.cmov.project.Constants.DomainConstants;
import pt.ulisboa.tecnico.cmov.project.R;
import pt.ulisboa.tecnico.cmov.project.activities.BookInfoActivity;
import pt.ulisboa.tecnico.cmov.project.adapters.CustomBookBaseAdapter;
import pt.ulisboa.tecnico.cmov.project.objects.Book;
import pt.ulisboa.tecnico.cmov.project.objects.WebConnector;
import pt.ulisboa.tecnico.cmov.project.utils.NetworkUtils;

public class BooksFragment extends Fragment {

    private ArrayList<Book> allBooks= new ArrayList<>();
    private ArrayList<Book> displayedBooks = new ArrayList<>();
    private ListView bookListView;
    private CustomBookBaseAdapter bookListCustomBaseAdapter;
    private final WebConnector webConnector;

    private int numberOfDisplayedBooks =0;

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

        bookListCustomBaseAdapter = new CustomBookBaseAdapter(getContext(), displayedBooks);
        bookListCustomBaseAdapter.setHandler(this.handler);
        bookListView.setAdapter(bookListCustomBaseAdapter);

        ftView = inflater.inflate(R.layout.footer_view, null);

        bookListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if ( !isLoading && view.getLastVisiblePosition() == numberOfDisplayedBooks - 1 && numberOfDisplayedBooks !=0)
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
                if (NetworkUtils.hasUnmeteredConnection(this.getContext())){
                    numberOfDisplayedBooks += webConnector.getBooks(DomainConstants.BOOKS,-1, 0, "");
                }
                else
                {
                    numberOfDisplayedBooks += webConnector.getBooks(DomainConstants.BOOKS_WITHOUT_IMAGE,-1, 0, "");
                }
                handler.sendEmptyMessage(DISABLE_LOADING_FOOTER);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        bookListView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(getActivity(), BookInfoActivity.class);
            intent.putExtra("bookTitle", displayedBooks.get(position).getTitle());
            intent.putExtra("bookCover", displayedBooks.get(position).getCover());
            intent.putExtra("bookBarcode", displayedBooks.get(position).getId());
            startActivity(intent);
        });

        // when you click on an item it displays its information
        Button searchButton = rootView.findViewById(R.id.searchBookButton);
        searchButton.setOnClickListener(this::searchBook);

        return rootView;
    }

    public void searchBook(View view) {
        String searchText = ((EditText) requireView().findViewById(R.id.searchBookText)).getText().toString().toLowerCase();

        Log.d("SearchBook", "Lista displayed -> " + displayedBooks.toString());
        Log.d("SearchBook", "Lista all -> " + allBooks.toString());


        if ( searchText.equals("") )
        {
            displayedBooks.clear();
            displayedBooks.addAll(allBooks);
            numberOfDisplayedBooks = allBooks.size();
        }
        else
        {
            displayedBooks.removeIf(book -> !book.getTitle().toLowerCase().contains(searchText));
            numberOfDisplayedBooks = displayedBooks.size();
        }

        Log.d("SearchBook", "Lista displayed -> " + displayedBooks.toString());
        Log.d("SearchBook", "Lista all -> " + allBooks.toString());

        bookListCustomBaseAdapter.notifyDataSetChanged();


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
                    displayedBooks.add((Book) msg.obj);
                    allBooks.add((Book) msg.obj);
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
                    displayedBooks.get(bookId).setCover(encodedImage);
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
                if (NetworkUtils.hasUnmeteredConnection(getContext())){
                    numberOfDisplayedBooks += webConnector.getBooks(DomainConstants.BOOKS,-1, numberOfDisplayedBooks, "");
                }
                else
                {
                    numberOfDisplayedBooks += webConnector.getBooks(DomainConstants.BOOKS_WITHOUT_IMAGE,-1, numberOfDisplayedBooks, "");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            handler.sendEmptyMessage(DISABLE_LOADING_FOOTER);
        }
    }

}
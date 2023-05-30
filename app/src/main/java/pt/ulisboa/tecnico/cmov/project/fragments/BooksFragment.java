package pt.ulisboa.tecnico.cmov.project.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
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

public class BooksFragment extends Fragment {
    private final ArrayList<Book> bookList = new ArrayList<>();
    private final ArrayList<Book> tempBookList = new ArrayList<>();
    private ListView bookListView;
    private CustomBookBaseAdapter bookListCustomBaseAdapter;
    private final WebConnector webConnector;

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
        bookListView.setAdapter(bookListCustomBaseAdapter);


        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                webConnector.getBooks(-1);
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

    private static final int NO_INTERNET = 1;
    private static final int UPDATE_BOOK_LIST = 2;

    private final Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_BOOK_LIST:
                    bookList.add((Book) msg.obj);
                    bookListCustomBaseAdapter.notifyDataSetChanged();
                    return;
                case NO_INTERNET:
                    Toast.makeText(getActivity(), (String) msg.obj, Toast.LENGTH_LONG).show();
            }
        }
    };
}
package pt.ulisboa.tecnico.cmov.project.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import pt.ulisboa.tecnico.cmov.project.R;
import pt.ulisboa.tecnico.cmov.project.activities.BookInfo_Activity;
import pt.ulisboa.tecnico.cmov.project.adapters.CustomBaseAdapter;
import pt.ulisboa.tecnico.cmov.project.objects.Book;
import pt.ulisboa.tecnico.cmov.project.objects.WebConnector;

public class BooksFragment extends Fragment {

    private ArrayList<Book> bookList;

    private ListView bookListView;
    private final WebConnector webConnector;

    public BooksFragment(WebConnector webConnector) {
        this.webConnector = webConnector;
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
        Context context = rootView.getContext();

        bookList = new ArrayList<>();
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                bookList.addAll(webConnector.getBooks(-1));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        bookListView = rootView.findViewById(R.id.bookListView);
        bookListView.setAdapter(new CustomBaseAdapter(context, bookList));

        // when you click on an item it displays its information
        bookListView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(getActivity(), BookInfo_Activity.class);
            intent.putExtra("bookTitle", bookList.get(position).getTitle());
            intent.putExtra("bookCover", bookList.get(position).getCover());
            startActivity(intent);
        });

        Button searchButton = rootView.findViewById(R.id.searchBookButton);
        searchButton.setOnClickListener(this::searchBook);

        return rootView;
    }

    public void searchBook(View view) {
        EditText searchText = requireView().findViewById(R.id.searchBookText);
        ArrayList<Book> temp = new ArrayList<>();

        for (Book b: bookList){
            if (b.getTitle().contains(searchText.getText().toString())){
                temp.add(b);
            }
        }

        CustomBaseAdapter adapter = new CustomBaseAdapter(getContext(), temp);
        bookListView.setAdapter(adapter);
        closeKeyboard(view);
    }

    public void closeKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
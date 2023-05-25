package pt.ulisboa.tecnico.cmov.project.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;

import pt.ulisboa.tecnico.cmov.project.adapters.CustomBaseAdapter;
import pt.ulisboa.tecnico.cmov.project.R;
import pt.ulisboa.tecnico.cmov.project.objects.Book;

public class BooksFragment extends Fragment {

    @SuppressWarnings("unused")
    public static BooksFragment newInstance(int columnCount) {
        BooksFragment fragment = new BooksFragment();
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

        ArrayList<Book> bookList = new ArrayList<>();
        bookList.add(new Book(1,"Biblia", "palavra de deus", R.drawable.bible_, 1234567));
        bookList.add(new Book(2,"Harry poter", "feiticos", R.drawable.harry, 1234));
        bookList.add(new Book(3,"Game of thrones", "porrada", R.drawable.gow, 6544));
        bookList.add(new Book(4,"Ben 10", "bue fixe", R.drawable.ben, 98));
        bookList.add(new Book(5,"Geronimo Stilton", "Rolemodel", R.drawable.g_ronimo, 43292));
        bookList.add(new Book(6, "Manual de portugues 8ano", "Camoes glorioso", R.drawable.manual, 1234567));

        ListView bookListView = rootView.findViewById(R.id.bookListView);
        bookListView.setAdapter(new CustomBaseAdapter(context, bookList));

        return rootView;
    }
}
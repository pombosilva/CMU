package pt.ulisboa.tecnico.cmov.project.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
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
import pt.ulisboa.tecnico.cmov.project.activities.BookInfo_Activity;
import pt.ulisboa.tecnico.cmov.project.adapters.CustomBaseAdapter;
import pt.ulisboa.tecnico.cmov.project.objects.Book;
import pt.ulisboa.tecnico.cmov.project.objects.WebConnector;

public class BooksFragment extends Fragment {

    private ArrayList<Book> bookList;
    private CustomBaseAdapter bookListCustomBaseAdapter;

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
        Context context = rootView.getContext();

        bookList = new ArrayList<>();

        ListView bookListView = rootView.findViewById(R.id.bookListView);

        bookListCustomBaseAdapter = new CustomBaseAdapter(getContext(), bookList);
        bookListView.setAdapter(bookListCustomBaseAdapter);


        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
//                bookList.addAll(webConnector.getBooks(-1));
//                sendMessageToHandler(null, UPDATE_UI_MSG);
                Log.d("MensagensDebug", "Vou conectar me");


                webConnector.getBooks(-1);


            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        bookListView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(getActivity(), BookInfo_Activity.class);
            intent.putExtra("bookTitle", bookList.get(position).getTitle());
            intent.putExtra("bookCover", bookList.get(position).getCover());
            startActivity(intent);
        });

        // when you click on an item it displays its information

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

        closeKeyboard(view);
    }

    public void closeKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


    //TODO: Partilhar o mesmo handler por todos os fragmentos da activity princiapl
    private void sendMessageToHandler(Object obj, int msgType) {
        Message msg = new Message();
        msg.obj = obj;
        msg.what = msgType;
        handler.sendMessage(msg);
//        looperThread.mHandler.sendMessage(msg);
    }

    private static final int  UPDATE_UI_MSG= 0;
    private static final int TOAST_MSG = 1;
    private static final int UPDATE_BOOK_LIST = 2;


    private final Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            Log.d("MensagensDebug", "Recebi um book pa actualizar logo o codigo e " + msg.what);
            switch (msg.what) {
                case UPDATE_UI_MSG:
                    bookListCustomBaseAdapter.notifyDataSetChanged();
                    return;
                case UPDATE_BOOK_LIST:
                    Log.d("MensagensDebug", "Recebi um book pa actualizar");
                    bookList.add((Book) msg.obj);
                    bookListCustomBaseAdapter.notifyDataSetChanged();
                    return;
                case TOAST_MSG:
                    Toast.makeText(getActivity(),
                            (String) msg.obj, Toast.LENGTH_LONG).show();
            }
        }
    };

}
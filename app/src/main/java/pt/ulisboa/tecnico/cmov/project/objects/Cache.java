package pt.ulisboa.tecnico.cmov.project.objects;

import android.os.Handler;
import android.os.Message;
import android.util.LruCache;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import pt.ulisboa.tecnico.cmov.project.activities.BookInfoActivity;
import pt.ulisboa.tecnico.cmov.project.activities.LibraryInfoActivity;
import pt.ulisboa.tecnico.cmov.project.fragments.BooksFragment;
import pt.ulisboa.tecnico.cmov.project.fragments.MapFragment;

public class Cache {

    private final static int NUMBER_BOOKS_TO_DISPLAY = 5;

    private boolean loaded = false;
        private static Cache instance;
        private LruCache<Library, ArrayList<Book>> cache;


    private Cache() {
            // Initialize the LruCache with the desired maximum size in bytes
            int maxCacheSize = 10 * 1024 * 1024; // 10 MB
            cache = new LruCache<>(maxCacheSize);
    }

    public static synchronized Cache getInstance() {
        if (instance == null) {
            instance = new Cache();
        }
        return instance;
    }

    public void putEntry(Library key, ArrayList<Book> value)
    {
        this.cache.put(key,value);
    }

    public LruCache<Library, ArrayList<Book>> getCache() {
        return cache;
    }

    public void loadMarkers(Handler handler)
    {

        Map<Library, ArrayList<Book>> snapshot = cache.snapshot();
        for (Library library : snapshot.keySet()) {
            sendMessageToHandler(handler, library, MapFragment.NEW_MARKER);
        }
    }


    public int getBooks( Handler handler, int displayedBooks)
    {
        Map<Library, ArrayList<Book>> snapshot = this.cache.snapshot();
        List<Book> allBooks = new ArrayList<>();
        //NOTE: Podiamos ter um field extra para saber quantos livros cada livraria tem e assim evitavamos fazer todo este processamento desnecessario
        snapshot.values().forEach(allBooks::addAll);

        for ( int i = displayedBooks ; i < displayedBooks + NUMBER_BOOKS_TO_DISPLAY && i < allBooks.size(); i++ )
        {
            sendMessageToHandler(handler, allBooks.get(i), BooksFragment.UPDATE_BOOK_LIST);
            displayedBooks++;
        }

        return displayedBooks;
    }


    public int getLibraryBooks(Handler handler, int libraryId, int displayedBooks)
    {
        Map<Library, ArrayList<Book>> snapshot = this.cache.snapshot();
        List<Book> libraryBooks = snapshot.entrySet().stream()
                .filter(entry -> entry.getKey().getId() == libraryId)
                .flatMap(entry -> entry.getValue().stream())
                .collect(Collectors.toList());

        for ( int i = displayedBooks ; i< displayedBooks + NUMBER_BOOKS_TO_DISPLAY && i < libraryBooks.size(); i ++ )
        {
            sendMessageToHandler(handler, libraryBooks.get(i), LibraryInfoActivity.ADD_UPDATE_BOOK_LIST);
            displayedBooks++;
        }
        return displayedBooks;
    }


    public void getBookLibraries(Handler handler, long bookId)
    {
        Map<Library, ArrayList<Book>> snapshot = this.cache.snapshot();
        for ( Library l : snapshot.keySet() )
        {
            if (bookInLibrary(snapshot.get(l), bookId))
            {
                sendMessageToHandler(handler, l, BookInfoActivity.ADD_LIBRARY_TO_LIST);
            }
        }
    }


    private boolean bookInLibrary(ArrayList<Book> bookList, long bookId)
    {
        for ( Book b : bookList )
        {
            if ( b.getId() == bookId)
                return true;
        }
        return false;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }


    private void sendMessageToHandler(Handler handler, Object obj, int what)
    {
        Message m = new Message();
        m.obj = obj;
        m.what = what;
        handler.sendMessage(m);
    }

}

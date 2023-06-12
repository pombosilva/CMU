package pt.ulisboa.tecnico.cmov.project.Threads;

import android.content.ContextWrapper;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;

import pt.ulisboa.tecnico.cmov.project.activities.MainActivity;
import pt.ulisboa.tecnico.cmov.project.objects.Book;
import pt.ulisboa.tecnico.cmov.project.objects.Cache;
import pt.ulisboa.tecnico.cmov.project.objects.Library;
import pt.ulisboa.tecnico.cmov.project.objects.WebConnector;
import pt.ulisboa.tecnico.cmov.project.utils.InternalStorage;
import pt.ulisboa.tecnico.cmov.project.utils.NetworkUtils;

public class LoadOflineLibraries extends Thread{
    private final ContextWrapper ctxWrp;

    private final Cache cache = Cache.getInstance();

    public LoadOflineLibraries(ContextWrapper ctxWrp)
    {
        this.ctxWrp = ctxWrp;
    }

    @Override
    public void run()
    {

        if ( NetworkUtils.hasInternetConnection(ctxWrp)) {

            try {
                Log.d("InternalStorage", "Vou fazer download das livrarias");
                WebConnector.getContentsWithinRadius(ctxWrp);
                Log.d("InternalStorage", "Fiz download das livrarias");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Library.class, new LoadOflineLibraries.LibraryDeserializer())
                .create();


        String jData = InternalStorage.read("output.json", ctxWrp);

        Log.d("InternalStorage", "jData = " + jData);

//                Library library = gson.fromJson(jData, Library.class);

        Type libraryListType = new TypeToken<ArrayList<Library>>() {
        }.getType();
//
//// Parse JSON array into a list of Library objects
        ArrayList<Library> libraryList = gson.fromJson(jData, libraryListType);
        cache.setLoaded(true);
//// Now you have a list of Library objects
        for (Library library : libraryList) {
            Log.d("InternalStorage", "Library = " + library.getName());
            Log.d("InternalStorage", "Books = " + cache.getCache().get(library).toString());
        }

    }

    public class LibraryDeserializer implements JsonDeserializer<Library> {

        @Override
        public Library deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();

            int id = jsonObject.get("id").getAsInt();
            String name = jsonObject.get("name").getAsString();
            double lat = jsonObject.get("lat").getAsDouble();
            double lng = jsonObject.get("lng").getAsDouble();
            boolean fav = jsonObject.get("fav").getAsBoolean();
            double distance = jsonObject.get("distance").getAsDouble();

            // Create a new Library object with the parsed values
            Library library = new Library(id, name, lat, lng, fav, distance);

            ArrayList<Book> ab = new ArrayList<Book>();
            // Parse the registeredBooks array
            JsonArray registeredBooksArray = jsonObject.getAsJsonArray("registeredBooks");
            for (JsonElement bookElement : registeredBooksArray) {
                Book book = context.deserialize(bookElement, Book.class);
                //TODO: Adicionar aqui codigo para adiconar a cache
                ab.add(book);
            }

            cache.getCache().put(library, ab);
            Log.d("InternalStorage", "Entrys: ");
            Log.d("InternalStorage", cache.getCache().get(library).toString());


            return library;
        }
    }
}

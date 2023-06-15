package pt.ulisboa.tecnico.cmov.project.Threads;

import android.content.ContextWrapper;

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

import pt.ulisboa.tecnico.cmov.project.objects.Book;
import pt.ulisboa.tecnico.cmov.project.objects.Cache;
import pt.ulisboa.tecnico.cmov.project.objects.Library;
import pt.ulisboa.tecnico.cmov.project.objects.WebConnector;
import pt.ulisboa.tecnico.cmov.project.utils.InternalStorage;
import pt.ulisboa.tecnico.cmov.project.utils.NetworkUtils;

public class LoadOfflineLibraries extends Thread{
    private final ContextWrapper ctxWrp;

    private final Cache cache = Cache.getInstance();

    public LoadOfflineLibraries(ContextWrapper ctxWrp)
    {
        this.ctxWrp = ctxWrp;
    }

    @Override
    public void run()
    {
        if ( NetworkUtils.hasInternetConnection(ctxWrp)) {

            try {
                WebConnector.getContentsWithinRadius(ctxWrp);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Library.class, new LoadOfflineLibraries.LibraryDeserializer())
                .create();

        String jData = InternalStorage.read("output.json", ctxWrp);

        Type libraryListType = new TypeToken<ArrayList<Library>>() {
        }.getType();

        gson.fromJson(jData, libraryListType);
        cache.setLoaded(true);
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
            String cover = jsonObject.get("cover").getAsString();
            double distance = jsonObject.get("distance").getAsDouble();

            Library library = new Library(id, name, lat, lng, fav, distance, cover);

            ArrayList<Book> ab = new ArrayList<Book>();
            JsonArray registeredBooksArray = jsonObject.getAsJsonArray("registeredBooks");
            for (JsonElement bookElement : registeredBooksArray) {
                Book book = context.deserialize(bookElement, Book.class);
                ab.add(book);
            }

            cache.putEntry(library, ab);
            return library;
        }
    }
}

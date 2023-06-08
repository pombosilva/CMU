package pt.ulisboa.tecnico.cmov.project.objects;

import android.util.LruCache;

public class Cache {

    private static Cache instance;
    private LruCache<Object, Object> cache;

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

    public void putEntry(Object key, Object value)
    {
        this.cache.put(key,value);
    }

    public LruCache<Object, Object> getCache() {
        return cache;
    }

}

package pt.ulisboa.tecnico.cmov.project.Threads;

import android.os.Handler;

import java.io.IOException;
import java.util.HashMap;

import pt.ulisboa.tecnico.cmov.project.Constants.DomainConstants;
import pt.ulisboa.tecnico.cmov.project.objects.Cache;
import pt.ulisboa.tecnico.cmov.project.utils.NetworkUtils;

public class GetMoreBooks extends Thread{

    private final Handler handler;

    public GetMoreBooks(Handler handler)
    {
        this.handler = handler;
    }

//    @Override
//    public void run()
//    {
//        handler.sendEmptyMessage(ENABLE_LOADING_FOOTER);
//        try {
//            if ( NetworkUtils.hasInternetConnection(getContext()) ) {
//                if (!isFiltered) {
//                    if (NetworkUtils.hasUnmeteredConnection(getContext())) {
//                        numberOfDisplayedBooks += webConnector.getBooks(DomainConstants.BOOKS, -1, numberOfDisplayedBooks, "", UPDATE_BOOK_LIST, NO_INTERNET);
//                    } else {
//                        numberOfDisplayedBooks += webConnector.getBooks(DomainConstants.BOOKS_WITHOUT_IMAGE, -1, numberOfDisplayedBooks, "", UPDATE_BOOK_LIST, NO_INTERNET);
//                    }
//                }
//            }
//            else
//            {
//                Cache cache = Cache.getInstance();
//                numberOfDisplayedBooks += cache.getBooks(handler, numberOfDisplayedBooks);
//
//
//            }
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        handler.sendEmptyMessage(DISABLE_LOADING_FOOTER);
//    }

}

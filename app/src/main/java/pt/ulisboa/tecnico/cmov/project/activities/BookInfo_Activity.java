package pt.ulisboa.tecnico.cmov.project.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import pt.ulisboa.tecnico.cmov.project.R;

public class BookInfo_Activity extends AppCompatActivity {


    public BookInfo_Activity() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.info_book);

        //configureListView();
        loadBookInfo(getIntent());
        configureButtons();
    }


    private void configureListView() {
        ListView listView = findViewById(R.id.bookAvailableLibraries);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            // TODO: Still missing reaction
            Toast.makeText( getApplicationContext(), "Clicked on a library!", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadBookInfo(Intent intent) {
        Bundle intentContents = intent.getExtras();

        if (intentContents != null) {
            String bookTitle = intentContents.getString("bookTitle");
            TextView bookTitleTv = findViewById(R.id.bookTitle);
            bookTitleTv.setText(bookTitle);

            ImageView bookCoverIm = findViewById(R.id.bookCover);
            int bookCover = intentContents.getInt("bookCover");
            bookCoverIm.setImageResource(bookCover);
        }
        else {
            Toast.makeText( getApplicationContext(), "Wasn't able to load book contents", Toast.LENGTH_SHORT).show();
        }
    }

    private void configureButtons()
    {
        Button notificationsButton = findViewById(R.id.buttonFav);
        notificationsButton.setOnClickListener(v -> {
            Toast.makeText( getApplicationContext(), "Added to favourites", Toast.LENGTH_LONG).show();
            // TODO: Adicionar aos favoritos
        });
    }

}
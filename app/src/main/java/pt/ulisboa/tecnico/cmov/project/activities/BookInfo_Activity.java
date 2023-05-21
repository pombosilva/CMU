package pt.ulisboa.tecnico.cmov.project.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import pt.ulisboa.tecnico.cmov.project.objects.Library;
import pt.ulisboa.tecnico.cmov.project.R;

public class BookInfo_Activity extends AppCompatActivity {


    private List<Library> librariesList = new ArrayList<Library>();
    private ArrayAdapter<Library> arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_info);


        configureListView();
        loadBookInfo(getIntent());
        configureButtons();
    }


    private void configureListView()
    {
        ListView listView = (ListView) findViewById(R.id.bookAvailableLibraries);
//        arrayAdapter = new ArrayAdapter<Library>(this, R.layout.activity_library_list_view, R.id.listView_library_name, librariesList);
//        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // TODO: Still missing reaction
                Toast.makeText( getApplicationContext(), "Clicked on a library!", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void loadBookInfo(Intent intent)
    {
        Bundle intentContents = intent.getExtras();

        if ( intentContents != null) {

            String bookTitle = intentContents.getString("bookTitle");
            TextView bookTitleTv = (TextView) findViewById(R.id.bookTitle);
            bookTitleTv.setText(bookTitle);

            ImageView bookCoverIm = (ImageView) findViewById(R.id.bookCover);
            int bookCover = intentContents.getInt("bookCover");
            bookCoverIm.setImageResource(bookCover);
        }
        else
        {
            Toast.makeText( getApplicationContext(), "Wasn't able to load book contents", Toast.LENGTH_SHORT).show();
        }
    }

    private void configureButtons()
    {
        Button notificationsButton = (Button) findViewById(R.id.book_notif_button);
        notificationsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText( getApplicationContext(), "FAVOUTIRED", Toast.LENGTH_LONG).show();

                // TODO: Adicionar aos favoritos

            }
        });
    }

}
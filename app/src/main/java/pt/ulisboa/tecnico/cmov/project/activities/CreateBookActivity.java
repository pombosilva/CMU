package pt.ulisboa.tecnico.cmov.project.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import pt.ulisboa.tecnico.cmov.project.R;
import pt.ulisboa.tecnico.cmov.project.objects.Book;
import pt.ulisboa.tecnico.cmov.project.objects.WebConnector;
import pt.ulisboa.tecnico.cmov.project.utils.ImageUtils;

public class CreateBookActivity extends AppCompatActivity {
    private static final int requestCode = 100;
    ImageView imageView;
    Button takePictureBtn;
    private String base64Picture;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_book);

        imageView = findViewById(R.id.imageView);
        takePictureBtn = findViewById(R.id.takePictureBtn);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, requestCode);
        }
        configureButtons();

    }

    private void configureButtons() {
        configureTakePictureButton();
        configureSaveBookButton();
    }

    private void configureSaveBookButton() {
        Button saveBookBtn = findViewById(R.id.saveBookBtn);
        saveBookBtn.setOnClickListener(v -> {
            TextView bookTitleTv = findViewById(R.id.bookTitleTv);
            TextView bookDescriptionTV = findViewById(R.id.bookDescriptionTv);

            String bookTitle = bookTitleTv.getText().toString();
            String bookDescription = bookDescriptionTV.getText().toString();

            if (isEmpty(bookTitle) || isEmpty(bookDescription)) {
                Toast.makeText(CreateBookActivity.this, "Please fill both the text camps", Toast.LENGTH_LONG).show();
            }
            else {
                if (isEmpty(base64Picture)){ Toast.makeText(CreateBookActivity.this, "Please take a picture of the book", Toast.LENGTH_LONG).show(); }
                else {
                    int bookId = Integer.parseInt(getIntent().getExtras().getString("bookId"));
                    int libraryId = Integer.parseInt(getIntent().getExtras().getString("libraryId"));

                    Executor executor = Executors.newSingleThreadExecutor();
                    executor.execute(() -> {
                        registerBookToCloud(bookId, bookTitle, bookDescription, base64Picture, libraryId);
                        finish();
                        }
                    );
                }
            }
        });
    }


    public void registerBookToCloud(int bookId, String bookTitle, String bookDescription, String bookEncodedImage, int libraryId)
    {
        WebConnector.registerBook(new Book(bookId, bookTitle, bookDescription, bookEncodedImage), libraryId);
    }


    //TODO: change deprecated method
    private void configureTakePictureButton(){
        takePictureBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent,requestCode);
        });
    }

    @Override
    protected void onActivityResult(int requestC, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestC == requestCode) {
            assert data != null;
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(bitmap);
            base64Picture = ImageUtils.encodeBitmapToBase64(bitmap, Bitmap.CompressFormat.JPEG, 64);
        }
    }

    private boolean isEmpty(String string) {
        return string == null || string.equals("");
    }
}
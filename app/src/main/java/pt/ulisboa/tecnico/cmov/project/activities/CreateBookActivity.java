package pt.ulisboa.tecnico.cmov.project.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import pt.ulisboa.tecnico.cmov.project.R;
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
                == PackageManager.PERMISSION_DENIED)
        {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, requestCode);
        }

        configureButtons();

    }


    private void configureButtons()
    {
        configureTakePictureButton();
        configureSaveBookButton();
    }

    private void configureSaveBookButton()
    {
        Button saveBookBtn = (Button) findViewById(R.id.saveBookBtn);
        saveBookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView bookTitleTv = findViewById(R.id.bookTitleTv);
                TextView bookDescriptionTV = findViewById(R.id.bookDescriptionTv);

                String bookTitle = bookTitleTv.getText().toString();
                String bookDescription = bookDescriptionTV.getText().toString();

                if ( isEmpty(bookTitle) || isEmpty(bookDescription) )
                {
                    Toast.makeText(CreateBookActivity.this, "Please fill both the text camps", Toast.LENGTH_LONG).show();
                }
                else
                {
                    if ( isEmpty(base64Picture) ){ Toast.makeText(CreateBookActivity.this, "Please take a picture of the book", Toast.LENGTH_LONG).show(); }
                    else
                    {
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("bookId", getIntent().getExtras().getString("bookId"));
                        resultIntent.putExtra("bookTitle", bookTitle);
                        resultIntent.putExtra("bookDescription", bookDescription);
                        resultIntent.putExtra("bookEncodedImage", base64Picture);

                        setResult(RESULT_OK, resultIntent);
                        finish();
                    }
                }


            }
        });
    }

    private void configureTakePictureButton()
    {
        takePictureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent,requestCode);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestC, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestC == requestCode)
        {
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(bitmap);
            imageView.setImageBitmap(bitmap);
            base64Picture = ImageUtils.encodeBitmapToBase64(bitmap, Bitmap.CompressFormat.JPEG, 64);
        }
    }

    private boolean isEmpty(String string)
    {
        if ( string == null || string.equals("") )
            return true;
        return false;
    }
}
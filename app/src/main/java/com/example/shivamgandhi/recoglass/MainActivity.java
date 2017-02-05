package com.example.shivamgandhi.recoglass;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import com.microsoft.windowsazure.mobileservices.*;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilter;
import com.microsoft.windowsazure.mobileservices.table.TableOperationCallback;

import java.net.MalformedURLException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private MobileServiceClient mClient;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    ImageView mImageView;
    Button button1, button2;
    EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageView = (ImageView)findViewById(R.id.mImageView);
        button1 = (Button)findViewById(R.id.Save_Face);
        button2 = (Button)findViewById(R.id.Search_Face);
        editText = (EditText)findViewById(R.id.editText);

<<<<<<< HEAD
        button1.setOnClickListener(this);
        button2.setOnClickListener(this);
=======
        button.setOnClickListener(this);

        try {

            mClient = new MobileServiceClient("https://recoglass.azurewebsites.net", this);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
>>>>>>> a32e867cf5ef653c9a24cdeb391acd5377535d30
    }

    private void dispatchTakePictureIntent(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null){
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            mImageView.setImageBitmap(imageBitmap);
        }
    }

    @Override
    public void onClick(View v){
        if(v == findViewById(R.id.Save_Face)){
            dispatchTakePictureIntent();
            //Save the picture on disk with the title
            String name = editText.getText().toString();

        }
        else if(v == findViewById(R.id.Search_Face)){
            dispatchTakePictureIntent();

        }
    }

    private void returnName(final Bitmap imageBitmap){
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

    }
}

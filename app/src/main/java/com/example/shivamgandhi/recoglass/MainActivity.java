package com.example.shivamgandhi.recoglass;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import com.microsoft.windowsazure.mobileservices.*;

import java.net.MalformedURLException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private MobileServiceClient mClient;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    ImageView mImageView;
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageView = (ImageView)findViewById(R.id.mImageView);
        button = (Button)findViewById(R.id.button);

        button.setOnClickListener(this);

        try {
            mClient = new MobileServiceClient("https://recoglass.azurewebsites.net", this);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
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
        if(v == findViewById(R.id.button)){
            dispatchTakePictureIntent();
            //Call server with the picture
        }
    }
}

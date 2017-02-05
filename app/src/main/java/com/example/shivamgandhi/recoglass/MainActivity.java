package com.example.shivamgandhi.recoglass;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.microsoft.projectoxford.face.contract.Person;
import com.microsoft.projectoxford.face.contract.TrainingStatus;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    ImageView mImageView;
    Button button1, button2;
    String mCurrentPhotoPath;
    Detection det;
    static final String DEF_PERSON_GROUP = "Peeps";
    EditText txt;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        det = new Detection("5347c2df9ae94876814ff955865024cd");
        setContentView(R.layout.activity_main);
        txt = (EditText) findViewById(R.id.nameField);
        mImageView = (ImageView)findViewById(R.id.mImageView);
        button1 = (Button)findViewById(R.id.Save_Face);
        button2 = (Button)findViewById(R.id.Search_Face);

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSaveButtonClick(v);
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSearchButtonClick(v);
            }
        });
        findViewById(R.id.trainButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                det.train(DEF_PERSON_GROUP);
                det.progress(DEF_PERSON_GROUP, new Consumer<TrainingStatus>() {
                    public void accept(TrainingStatus t) {
                        TextView v = (TextView)findViewById(R.id.textView2);
                        v.setText(t.message);
                    }
                });
            }
        });


    }

    private void dispatchTakePictureIntent(int i){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null){
            startActivityForResult(takePictureIntent, i);
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        System.out.println("Lmao");
        if (requestCode == 1 && resultCode == RESULT_OK){
            Bundle extras = data.getExtras();
            final Bitmap imageBitmap = (Bitmap) extras.get("data");
            mImageView.setImageBitmap(imageBitmap);
            PersonModel.getPerson(txt.getText().toString(), det, DEF_PERSON_GROUP, new Consumer<PersonModel>() {
                public void accept(PersonModel p) {
                    p.addFace(imageBitmap);
                }
            });
            ObjectOutputStream oos = new ObjectOutputStream();
            PersonModel.getPersonMap();
            Intent myIntent = new Intent(this, MainActivity.class);
            startActivity(myIntent);

        }
        else if (requestCode == 2 && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            mImageView.setImageBitmap(imageBitmap);
            final EditText txt1 = txt;
            det.detectAsync(imageBitmap, new Consumer<UUID>() {
                public void accept(UUID u) {
                    det.identifyAsync(DEF_PERSON_GROUP, new UUID[]{u}, 1, 0, new ArrayList<Consumer<UUID[]>>(Arrays.asList(
                            new Consumer<UUID[]>() {
                                public void accept(UUID[] u) {
                                    UUID bestCandidate = u[0];
                                    det.getPersonData(DEF_PERSON_GROUP, bestCandidate, new Consumer<Person>() {
                                        public void accept(Person p) {
                                            txt1.setText(p.name);
                                        }
                                    });
                                }
                            }
                    )));

                }
            });
        }
    }


    public void onSaveButtonClick(View v) {
        dispatchTakePictureIntent(1);

    }

    public void onSearchButtonClick(View v) {
        dispatchTakePictureIntent(2);
    }
    private void returnName(final Bitmap imageBitmap){
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

    }

    private File createImageFile() throws IOException{
        //Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String name = txt.getText().toString();
        String imageFileName = name + "_JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }
}

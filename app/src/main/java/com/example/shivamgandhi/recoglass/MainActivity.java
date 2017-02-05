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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    ImageView mImageView;
    Button button1, button2, trainingButton;
    String mCurrentPhotoPath;
    Detection det;
    static final String DEF_PERSON_GROUP = "peeps";
    EditText txt;
    static final String PEOPLE_FILE = "people.hash";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        det = new Detection("6822f1e2bd8744a4b44206bc3b1dded6");
        //det.createPersonGroup(DEF_PERSON_GROUP, "Peoples!", "");
        setContentView(R.layout.activity_main);
        txt = (EditText) findViewById(R.id.nameField);
        mImageView = (ImageView)findViewById(R.id.mImageView);
        button1 = (Button)findViewById(R.id.Save_Face);
        button2 = (Button)findViewById(R.id.Search_Face);
        trainingButton = (Button) findViewById(R.id.trainButton);
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(this.getFilesDir(), PEOPLE_FILE)));
            HashMap<String, String> s = (HashMap<String, String>)ois.readObject();
            PersonModel.updatePersonMap(s, det, DEF_PERSON_GROUP);
        } catch (Exception e) {
            e.printStackTrace();
        }


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

        final Button trainButton = (Button) findViewById(R.id.trainButton);
        trainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                det.train(DEF_PERSON_GROUP, new Consumer<String>() {
                    public void accept(String s){
                        final Button trainButton = (Button) v;
                        det.progress(DEF_PERSON_GROUP, new Consumer<TrainingStatus>() {
                            public void accept(TrainingStatus t) {


                                if (t == null) {
                                    return;
                                }
                                TextView v = (TextView)findViewById(R.id.textView2);
                                v.setText(t.status.toString());
                            }
                        });
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
        if (requestCode == 1 && resultCode == RESULT_OK){
            Bundle extras = data.getExtras();
            final Bitmap imageBitmap = (Bitmap) extras.get("data");
            mImageView.setImageBitmap(imageBitmap);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            txt.setText("Shivam Gandhi");
            PersonModel.getPerson(txt.getText().toString(), det, DEF_PERSON_GROUP, new Consumer<PersonModel>() {
                public void accept(PersonModel p) {
                    p.addFace(imageBitmap);
                }
            });
            Runnable r = new Runnable() {
                public void run() {
                    try {
                        FileOutputStream fos = new FileOutputStream(new File(MainActivity.this.getFilesDir(), PEOPLE_FILE));
                        ObjectOutputStream oos = new ObjectOutputStream(fos);
                        oos.writeObject(PersonModel.getPersonMap());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            new Thread(r).start();




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
            Intent myIntent = new Intent(this, MainActivity.class);
            startActivity(myIntent);
        }
        button1.setEnabled(true);
        button2.setEnabled(true);
        trainingButton.setEnabled(true);
    }


    public void onSaveButtonClick(View v) {
        v.setEnabled(false);
        dispatchTakePictureIntent(1);
    }

    public void onSearchButtonClick(View v) {
        v.setEnabled(false);
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
        File storageDir = this.getCacheDir();
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }
}

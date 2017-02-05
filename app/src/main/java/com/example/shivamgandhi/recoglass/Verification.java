package com.example.shivamgandhi.recoglass;

/**
 * Created by Cruz on 2/4/17.
 */


import com.example.shivamgandhi.recoglass.Detection;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.contract.Face;
import com.microsoft.projectoxford.face.contract.VerifyResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;


public class Verification extends AppCompatActivity {

    private class VerificationTask extends AsyncTask<Void, String, VerifyResult>{
        private UUID mFaceId0;
        private UUID mFaceId1;

        VerificationTask (UUID faceId0, UUID faceId1){
            mFaceId0 = faceId0;
            mFaceId1 = faceId1;
        }

        @Override
        protected VerifyResult doInBackground(Void... params){
            //need a getter method somewhere to return a FaceServiceClient
            FaceServiceClient faceServiceClient = Detection.getFaceServiceClient();
            try {
                publishProgress("Verifying...");
                //Start verification
                //FaceServiceClient has a method called verify, I don't know if we
                //need to train it
                return faceServiceClient.verify(mFaceId0, mFaceId1);

            } catch (Exception e){
                publishProgress(e.getMessage());
                //CHECK LOG HELPER
                System.out.println(e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPreExecute(){
            //CHECK WHATEVER CLASS THIS IS
            progressDialog.show();
            //CHECK LOG HELPER
            System.out.println("Request: Verifying face" + mFaceId0+ " and face "+ mFaceId1);
        }

        @Override
        protected void onProgressUpdate(String... progress){
            //CHECK LOG/PROGRESS KEEPS US UPDATED ON PROGRESS OF THE APP
            progressDialog.setMessage(progress[0]);
            setInfo(progress[0]);
        }

        @Override
        protected void onPostExecute(VerifyResult result){
            if(result != null){
                System.out.println("Response: Success. Face" + mFaceId0 + " and face" + mFaceId1
                        + (result.isIdentical ? " ": "don't ") + "belong to the same person");
            }
            //show the result on scren when verification is done.
            setUiAfterVerification(result);
        }

    }


    //Background task of face detection.
    private class DetectionTask extends AsyncTask<InputStream, String, Face[]>{
        private int mIndex;
        private boolean mSucceed = true;
        DetectionTask(int index){
            mIndex = index;
        }
        @Override
        protected Face[] doInBackground(InputStream... params){
            FaceServiceClient faceServiceClient = Detection.getFaceServiceClient();
            try{
                publishProgress("Detecting....");
                //DETECTION OF THE FACE?
                return faceServiceClient.detect(params[0], true, false, null);
            } catch (Exception e){
                mSucceed = false;
                publishProgress(e.getMessage());
                //CHECK LOG CLASS
                System.out.println(e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPreExecute(){
            //FIND OUT WHERE DIALOGS ARE COMING FROM AND WHAT THEY ARE FOR
            progressDialog.show();
            //CHECK LOG CLASS, DO WE NEED THIS
            System.out.println("Request: Detecting in image" + mIndex);
        }

        @Override
        protected void onProgressUpdate(String... progress){
            //WHERE IS PROGRESS DIALOG AND SETINFO
            progressDialog.setMessage(progress[0]);
            setInfo(progress[0]);
        }

        @Override
        protected void onPostExecute(Face[] result){
            //SHow the result on screen when detection is done.
        }

    }

    //Flag to indicate which task is to be performed
    private static final int REQUEST_SELECT_IMAGE_0 = 0;
    private static final int REQUEST_SELECT_IMAGE_1 = 1;


    //THE IDS OF THE TWO FACES TO BE VERIFIED
    private UUID mFaceId0;
    private UUID mFaceId1;

    //the two images from where we get the two faces to verify
    private Bitmap mBitmap0;
    private Bitmap mBitmap1;

    //the adapter of the listview which contains the detected faces from the two images.


    //progress dialog popped up when communicating with server
    ProgressDialog progressDialog;

    //when activity is created, set all the member variables to initial state
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Intialize the two listviews which contain the thumbnails of the detected faces
        progressDialog = new ProgressDialog(this);
        //find out where these methods are created
    }


    //start detecting in image specified by index.
    private void detect(Bitmap bitmap, int index){
        //Put the image into an input stream for detection.
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray());
        //Start a background task to detect faces in the image.
        new DetectionTask(index).execute(inputStream);
        //set the status to show that detection starts.
        setInfo("Detecting...");
    }

    //called when image selection is done, beging detecting if the image is selected
    //successfully
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        //Index indicates which of the two images is selected.
        int index;
        if(requestCode == REQUEST_SELECT_IMAGE_0){
            index = 0;
        } else if (requestCode == REQUEST_SELECT_IMAGE_1){
            index = 1;
        } else {
            return;
        }

//        if(resultCode == RESULT_OK){
//            //If image is selected successfully, set the image URI and bitmap.
//            Bitmap bitmap = ImageHelper.loadSizeLimitedBitmapFromUri(data.getData(), getContentResolver());
//            if(bitmap != null){
//                //image is select but not detected, disable verification button.
//                //Set the image to detect.
//                if(index == 0){
//                    mBitmap0 = bitmap;
//                    mFaceId0 = null;
//                } else {
//                    mBitmap1 = bitmap;
//                    mFaceId1 = null;
//                }
//                //add verification log.
//                System.out.println("Image" + index + ":" + data.getData() + " resized to " + bitmap.getWid() +
//                        "x" + bitmap.getHeight());
//                //start detecting in image
//                detect(bitmap, index);
//            }
//        }
    }





    //MAIN METHOD CALL called when the verify button is clicked.
    public void verify(View view){

        new VerificationTask(mFaceId0, mFaceId1).execute();
    }


    //show result on screen when verification is done
    private void setUiAfterVerification(VerifyResult result){
        //verification is done, hide the progress dialog.
        progressDialog.dismiss();
        //Enable all the buttons.
        //show the verification result
        if(result != null){
            DecimalFormat formatter = new DecimalFormat("#0.00");
            String verificationResult = (result.isIdentical ? "the same person":
             "Different persons" + ". The confidence is " + formatter.format(result.confidence));
            setInfo(verificationResult);
        }
    }



    //set the information panel on screen.
    private void setInfo(String info){
        TextView textView = (TextView) findViewById(R.id.info);
        textView.setText(info);
    }




}

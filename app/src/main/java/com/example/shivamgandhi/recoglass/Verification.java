package com.example.shivamgandhi.recoglass;

/**
 * Created by Cruz on 2/4/17.
 */



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
import com.microsoft.projectoxford.face.samples.R;
import com.microsoft.projectoxford.face.samples.helper.ImageHelper;
import com.microsoft.projectoxford.face.samples.helper.LogHelper;
import com.microsoft.projectoxford.face.samples.helper.SampleApp;
import com.microsoft.projectoxford.face.samples.log.VerificationLogActivity;
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
            FaceServiceClient faceServiceClient = SampleApp.getFaceServiceClient();
            try {
                publishProgress("Verifying...");
                //Start verification
                //FaceServiceClient has a method called verify, I don't know if we
                //need to train it
                return faceServiceClient.verify(mFaceId0, mFaceId1);

            } catch (Exception e){
                publishProgress(e.getMessage());
                //CHECK LOG HELPER
                addLog(e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPreExecute(){
            //CHECK WHATEVER CLASS THIS IS
            progressDialog.show();
            //CHECK LOG HELPER
            addLog("Request: Verifying face" + mFaceId0+ " and face "+ mFaceId1);
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
                addLog("Response: Success. Face" + mFaceId0 + " and face" + mFaceId1
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
            FaceServiceClient faceServiceClient = SampleApp.getFaceServiceClient();
            try{
                publishProgress("Detecting....");
                return faceServiceClient.detect(params[0], true, false, null);
            } catch (Exception e){
                mSucceed = false;
                publishProgress(e.getMessage());
                //CHECK LOG CLASS
                addLog(e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPreExecute(){
            //FIND OUT WHERE DIALOGS ARE COMING FROM AND WHAT THEY ARE FOR
            progressDialog.show();
            //CHECK LOG CLASS, DO WE NEED THIS
            addLog("Request: Detecting in image" + mIndex);
        }

    }





}

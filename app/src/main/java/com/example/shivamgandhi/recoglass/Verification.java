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
            setUiAfterDetection(result, mIndex, mSucceed);
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

    //FIND OUT WHERE FACELISTADAPTER IS, AND IF I NEED IT
    protected FaceListAdapter mFaceListAdapter0;
    protected FaceListAdapter mFaceListAdapter1;

    //progress dialog popped up when communicating with server
    ProgressDialog progressDialog;

    //when activity is created, set all the member variables to initial state
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Intialize the two listviews which contain the thumbnails of the detected faces
        initializeFaceList(0);
        initializeFaceList(1);
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.progress_dialog_title));
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

        if(resultCode == RESULT_OK){
            //If image is selected successfully, set the image URI and bitmap.
            Bitmap bitmap = ImageHelper.loadSizeLimitedBitmapFromUri(data.getData(), getContentResolver());
            if(bitmap != null){
                //image is select but not detected, disable verification button.
                //Set the image to detect.
                if(index == 0){
                    mBitmap0 = bitmap;
                    mFaceId0 = null;
                } else {
                    mBitmap1 = bitmap;
                    mFaceId1 = null;
                }
                //add verification log.
                System.out.println("Image" + index + ":" + data.getData() + " resized to " + bitmap.getWid() +
                        "x" + bitmap.getHeight());
                //start detecting in image
                detect(bitmap, index);
            }
        }
    }



    //called when the select image0 button is clicked in face face verification

    public void selectImage0(View view){
        //figure out where the fuck select image is
        selectImage(0);
    }

    public void selectImage1(View view){
        selectImage(1);
    }

    //MAIN METHOD CALL called when the verify button is clicked.
    public void verify(View view){

        new VerificationTask(mFaceId0, mFaceId1).execute();
    }


    //Select the image indicated by index
    private void selectImage(int index){
        Intent intent = new Intent(this, SelectImageActivity.class);
        startActivityForResult(intent, index == 0 ? REQUEST_SELECT_IMAGE_0: REQUEST_SELECT_IMAGE_1);
    }



    //Initialize the ListView which contains the thumbnails of the detected faces
    private void initializeFaceList(final int index){
        ListView listView = (ListView) findViewById(index == 0? R.id.list_faces_0:
         R.id.list_faces_1);
        //when a detect face in the gridview is clicked, the face is selected to verify
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                FaceListAdapter faceListAdapter = index == 0 ? mFaceListAdapter0: mFaceListAdapter1;
                if (!faceListAdapter.faces.get(position).faceId.equals(
                        index == 0 ? mFaceId0: mFaceId1)){
                    if(index == 0){
                        mFaceId0 = faceListAdapter.faces.get(position).faceId;
                    } else {
                        mFaceId1 = faceListAdapter.faces.get(position).faceId;
                    }
                    ImageView imageView = (ImageView) findViewById(index == 0 ? R.id.image_0:
                    R.id.image_1);
                    imageView.setImageBitmap(faceListAdapter.faceThumbnails.get(position));
                    setInfo("");
                }
            }
        });
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
             "Different persons" + ". The confidence is " + formatter.format(result.confidence);
            setInfo(verificationResult);
        }
    }

    //show the result on screen when detection in image that indicated by index is done.
    private void setUiAfterDetection(Face[] result, int index, boolean suceed){
        if(succeed){
            System.out.println("Response: Success. Detected " + result.length + " face(s) in image"
            + index);
            setInfo(result.length + " face" + (result.length != 1 ? "s": "") + " detected");
            //show the detailed list of detected faces
            FaceListAdapter faceListAdapter = new FaceListAdapter(result, index);
            //set the default face id to the id of first face, if one or more faces are detected.
            if(faceListAdapter.faces.size() != 0){
                if (index == 0){
                    mFaceId0 = faceListAdapter.faces.get(0).faceId;
                } else {
                    mFaceId1 = faceListAdapter.faces.get(0).faceId;
                }
                //show the thumbnail of the defail face
                ImageView imageView = (ImageView) findViewById(index == 0?
                R.id.image_0: R.id.image_1);
                imageView.setImageBitmap(faceListAdapter.faceThumbnails.get(0));
            }
            //show the list of detected face thumbnails.
            ListView listView = (ListView) findViewById(index == 0 ? R.id.list_faces_0: R.id.list_faces_1);
            listView.setAdapter(faceListAdapter);
            listView.setVisibility(View.VISIBLE);
            //set the face list adapters and bitmaps.
            if(index == 0){
                mFaceListAdapter0 = faceListAdapter;
                mBitmap0 = null;
            } else {
                mFaceListAdapter1 = faceListAdapter;
                mBitmap1 = null;
            }
        }

        if(result != null && result.length == 0){
            setInfo("No face detected!");
        }

        if((index == 0 && mBitmap1 == null) || (index == 1 && mBitmap0 == null) || index == 2){
            progressDialog.dismiss();
        }
        if(mFaceId0 != null && mFaceId1 != null){
            setVerifyButtonEnabledStatus(true);
        }
    }

    //set the information panel on screen.
    private void setInfo(String info){
        TextView textView = (TextView) findViewById(R.id.info);
        textView.setText(info);
    }


    //the adapter of the gridview which contains the thumbnaisl of the detected faces.
    private class FaceListAdapter extends BaseAdapter{
        //the detected faces
        List<Face> faces;
        int mIndex;
        //the thumbnails of detected faces.
        List<Bitmap> faceThumbnails;
        //Initialize w/ detection result and index indicatin on which image the result is got.
        FaceListAdapter(Face[] detectionResult, int index){
            faces = new ArrayList<>();
            faceThumbnails = new ArrayList<>();
            mIndex = index;
            if(detectionResult != null){
                faces = Arrays.asList(detectionResult);
                for(Face face: faces){
                    try{
                        //crop face thumbnail w/o landmarks drawn
                        faceThumbnails.add(ImageHelper.generateFaceThumbnail(index == 0?
                         mBitmap0: mBitmap1, face.faceRectangle))
                    } catch(IOException e){
                        //show the exception when generating face thumbnail fails.
                        setInfo(e.getMessage());
                    }
                }
            }
        }

        @Override
        public int getCount(){
            return faces.size();
        }

        @Override
        public Object getItem(int position){
            return faces.get(position);
        }
        @Override
        public long getItemId(int position){
            return position;
        }
        @Override
        public View getView(final int position, View convertView, ViewGroup parent){
            if(convertView == null){
                LayoutInflater layoutInflater = (LayoutInflater)getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.item_face, parent, false);
            }
            convertView.setId(position);
            Bitmap thumbnailToShow = faceThumbnails.get(position);
            if(mIndex == 0 && faces.get(position).faceId.equals(mFaceId0)){
                thumbnailToShow = ImageHelper.highlightSelectedFaceThumbnail(thumbnailToShow);
            } else if (mIndex == 1 && faces.get(position).faceId.equals(mFaceId1)){
                thumbnailToShow = ImageHelper.highlightSelectedFaceThumbnail(thumbnailToShow);
            }

            //Show the face thumbnail.
            ((ImageView)convertView.findViewById(R.id.image_face)).setImageBitmap(thumbnailToShow);
            return convertView;
        }
    }

}

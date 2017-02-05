package com.example.shivamgandhi.recoglass;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.FaceServiceRestClient;
import com.microsoft.projectoxford.face.contract.CreatePersonResult;
import com.microsoft.projectoxford.face.contract.Face;
import com.microsoft.projectoxford.face.contract.IdentifyResult;
import com.microsoft.projectoxford.face.contract.Person;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.function.*;
/**
 * Created by Cruz on 2/4/17.
 */

public class Detection {

    public static final int IMAGE_QUALITY = 100;
    private ExecutorService ex;
    private static FaceServiceClient faceServiceClient;
    public Detection(String subscriptionKey) {
        faceServiceClient = new FaceServiceRestClient(subscriptionKey);
        ex = Executors.newFixedThreadPool(5);


    }
    public static FaceServiceClient getFaceServiceClient(){
        return faceServiceClient;
    }
    public static byte[] bitmapToByteArray(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, IMAGE_QUALITY, baos);
        return baos.toByteArray();
    }
    public void detectAsync(Bitmap bmp, Consumer<UUID> faceIdCallback) {
        final Consumer<UUID> faceIdCallbackClone = faceIdCallback;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, IMAGE_QUALITY, baos);
        final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        Runnable r = new Runnable(){
            public void run() {
                Face[] result = null;
                try {
                    System.out.println("Detect Requests");
                    result = faceServiceClient.detect(bais, true, false, null);
                    System.out.println("Detect Request");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (result != null && result.length > 0) {
                    faceIdCallbackClone.accept(result[0].faceId);
                }
            }

        };
        ex.submit(r);
    }

    public void identifyAsync(final String personGroupId, final UUID[] faceIds, final int numCandidates, final float confidence, final List<Consumer<UUID[]>> personIdCallbacks) {
        Runnable r = new Runnable() {
            public void run() {
                IdentifyResult[] results = null;
                try {
                    results = faceServiceClient.identity(personGroupId, faceIds, confidence, numCandidates);
                } catch (Exception e) {
                    // TODO: Exception handling
                }
                if (results == null) {
                    return;
                }
                for (int i = 0; i < results.length; i++) {
                    UUID[] uuids = new UUID[numCandidates];
                    results[i].candidates.toArray(uuids);
                    personIdCallbacks.get(i).accept(uuids);
                }
            }

        };
        ex.submit(r);

    }

    public void createPerson(final String personGroupId, final String name, final String userData, final Consumer<UUID> personIdCallback) {
        Runnable r = new Runnable() {
            public void run() {
                CreatePersonResult res = null;
                try {
                    res = faceServiceClient.createPerson(personGroupId, name, userData);
                } catch (Exception e) {
                    // TODO: Exception handling
                }
                if (res == null) {
                    return;
                }
                personIdCallback.accept(res.personId);

            }
        };
    }

    public void createPersonGroup(final String personGroupId, final String name, final String userData) {
        Runnable r = new Runnable(){
            public void run() {
                try {
                    faceServiceClient.createPersonGroup(personGroupId, name, userData);
                } catch (Exception e) {
                    // TODO: Exception handling
                }
            }


        };
        ex.submit(r);
    }

    public void addPersonFace(final String personGroupId, final UUID personId, final String userData, final byte[] image, final Consumer<UUID> persistentFaceIdConsumer) {
        Runnable r = new Runnable(){
            public void run() {
                InputStream is = new ByteArrayInputStream(image);
                try {
                    faceServiceClient.addPersonFace(personGroupId, personId, is, userData, null);
                } catch (Exception e) {

                }
            }
        };
        ex.submit(r);

    }

    public void listPersons(final String personGroupId, final Consumer<UUID[]> personIdCallbacks) {
        Runnable r = new Runnable() {
            public void run() {
                Person[] peeps = null;
                try {
                    peeps = faceServiceClient.getPersons(personGroupId);
                } catch (Exception e) {
                    // TODO: Exception handling
                }
                if (peeps == null) {
                    return;
                }
                UUID[] uuids = new UUID[peeps.length];
                for (int i = 0; i < peeps.length; i++) {
                   uuids[i] = peeps[i].personId;
                }
                personIdCallbacks.accept(uuids);

            }
        };
        ex.submit(r);
    }

    public void getPersonData(final String personGroupId, final UUID personId, final Consumer<Person> personConsumer) {
        Runnable r = new Runnable() {
            public void run() {
                Person p = null;
                try {
                    p = faceServiceClient.getPerson(personGroupId, personId);
                } catch (Exception e) {
                    // TODO: Add exception handling
                }
                if (p != null) {
                    personConsumer.accept(p);
                }
            }
        };
        ex.submit(r);
    }

}

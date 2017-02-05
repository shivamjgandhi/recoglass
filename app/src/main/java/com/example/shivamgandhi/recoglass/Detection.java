package com.example.shivamgandhi.recoglass;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.FaceServiceRestClient;
import com.microsoft.projectoxford.face.contract.Face;
import com.microsoft.projectoxford.face.contract.IdentifyResult;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
    private FaceServiceClient faceServiceClient;
    public Detection(String subscriptionKey) {
        faceServiceClient = new FaceServiceRestClient(subscriptionKey);
        ex = Executors.newFixedThreadPool(5);


    }

    public void detectAsync(Bitmap bmp, Consumer<UUID> callback) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, IMAGE_QUALITY, baos);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        Runnable r = () -> {
            Face[] result = null;
            try {
                 result = faceServiceClient.detect(bais, true, false, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (result != null && result.length > 0) {
                callback.accept(result[0].faceId);
            }
        };
        ex.submit(r);
    }

    public void identifyAsync(String personGroupId, UUID[] faceIds, int numCandidates, float confidence, Consumer<UUID[]>[] callbacks) {
        Runnable r = ()-> {
            IdentifyResult[] results = null;
            try {
                results = faceServiceClient.identity(personGroupId, faceIds, confidence, numCandidates);
            } catch (Exception e) {
                // TODO: Exception handling
            }
            for (int i = 0; i < results.length; i++) {
                UUID[] uuids = new UUID[numCandidates];
                results[i].candidates.toArray(uuids);
                callbacks[i].accept(uuids);
            }
        };
        ex.submit(r);

    }

}

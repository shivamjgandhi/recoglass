package com.example.shivamgandhi.recoglass;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.Scanner;


/**
 * Created by Cruz on 2/4/17.
 */
// Project minimum is 17; should we do 19?
@TargetApi(19)
public class cognitiveResources {

    public static final Charset CHARACTER_SET = StandardCharsets.UTF_8;
    public static final int IMAGE_QUALITY = 100;
    public static final int MAX_IMAGE_SIZE = 4000000;
    // Please do not fill this in until
    public static final String SUBSCRIPTION_KEY = "a5b5547007d54be7aa5bb75555376661";
    //public static final String FACE_API_URL = "https://westus.api.cognitive.microsoft.com/face/v1.0/";
    public static final String FACE_API_URL = "http://localhost/face/v1.0/";
    enum APIAction {
        DETECT ("detect"),
        C_PERSON_GROUP ("persongroups"),
        PERSON ("persons"),
        PERSISTED_FACES ("persistedFaces"),
        TRAIN ("train"),
        IDENTIFY ("identify");
        private String action;
        APIAction(String s) {
            action = s;
        }
        public String toString() {
            return action;
        }
    }
    private static Map<String, Object> createRequest(String url, Map<String, String> values, byte[] body, String contentType) throws IOException, JSONException {
        Map<String, Object> responseData = new HashMap();
        JSONObject o = rawRequest(url, values, body, contentType);
        Iterator<String> it = o.keys();
        while (it.hasNext()) {
            String key = it.next();
            responseData.put(key, o.get(key));
        }
        System.out.println(responseData);
        return responseData;
    }

    private static JSONObject rawRequest(String url, Map<String, String> values, byte[] body, String contentType) throws IOException, JSONException{
        String queryString = "";
        // Building url
        if (values != null) {
            for (Map.Entry<String, String> entry : values.entrySet()) {
                queryString += "&" + entry.getKey() + "=" + entry.getValue();
            }
            queryString = queryString.substring(1);
        }

        HttpURLConnection connection = (HttpURLConnection) new URL(url + "?" + queryString)
                .openConnection();
        connection.setRequestProperty("Accept-Charset", CHARACTER_SET.toString());
        // MS wants images like this
        connection.setRequestProperty("Content-Type", contentType);
        // Subscription key
        connection.setRequestProperty("Ocp-Apim-Subscription-Key", SUBSCRIPTION_KEY);
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setDoInput(true);
        if (body != null) {
            connection.getOutputStream().write(body);
        }

        try {
            connection.connect();
        } catch (Exception e) {
            // Please add real exception handling
            System.err.println("Could not connect to server");
            System.exit(1);
        }

        Scanner s = new Scanner(connection.getInputStream());
        StringBuilder response = new StringBuilder("");
        while (s.hasNextLine()) {
            response.append(s.nextLine() + "\n");
        }
        s.close();
        String res = response.toString();
        JSONObject o = new JSONObject(res);
        return o;
    }

    public static void train(String personGroupId) throws IOException, JSONException{
        String url = FACE_API_URL + APIAction.C_PERSON_GROUP + "/" + personGroupId + "/" + APIAction.TRAIN;
        createRequest(url, null, null, "application/x-www-form-urlencoded");
    }

    public static String detect(Bitmap bmp) {

        String url = FACE_API_URL + APIAction.DETECT;
        Map<String, String> data = new HashMap();
        data.put("returnFaceId", ""+true);
        Map<String, Object> response = null;
        try {
            response = cognitiveResources.createRequest(url, data, bitmapToByteArray(bmp), "application/octet-stream");
        }
        catch (Exception e) {
            // TODO: Exception handling
        }
        String faceid = (String) response.get("faceId");
        return faceid;
    }

    private static byte[] bitmapToByteArray(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, IMAGE_QUALITY, baos);
        return baos.toByteArray();
    }

    public static String createPerson(String personGroupId, String name, String userData, Bitmap... images) throws JSONException, IOException{
        String url = FACE_API_URL + APIAction.C_PERSON_GROUP + "/" + personGroupId + "/" + APIAction.PERSON;
        Map<String, Object> reqData = new HashMap();
        reqData.put("name", name);
        reqData.put("userData", userData);
        String personId = (String) sendJsonBody(url, null, reqData).get("personId");
        for (Bitmap bmp : images) {
            addPersonFace(personId, personGroupId, "", bmp);
        }
        return personId;
    }

    public static String addPersonFace(String personId, String personGroupId, String userData, Bitmap bmp) throws IOException, JSONException{
        String url = FACE_API_URL + APIAction.C_PERSON_GROUP + "/" + personGroupId + "/" + APIAction.PERSON + "/" + personId + APIAction.PERSISTED_FACES;
        Map<String, String> KVs = new HashMap();
        KVs.put("userData", userData);
        Map<String, Object> data = createRequest(url, KVs, bitmapToByteArray(bmp), "application/octet-stream");
        return (String) data.get("persistedFaceId");
    }

    private static Map<String, Object> sendJsonBody(String url, Map<String, String> urlKVs, Map<String, Object> vals) throws JSONException, IOException{
        JSONObject o = new JSONObject();
        for (Map.Entry<String, Object> e : vals.entrySet()) {
            o.put(e.getKey(), e.getValue());
        }
        return createRequest(url, urlKVs, o.toString().getBytes(), "application/json");
    }

    private static void createPersonGroup(String id, String name, String userData) throws JSONException, IOException{
        String url = FACE_API_URL + APIAction.C_PERSON_GROUP + "/" + id;
        Map<String, String> parameters = new HashMap();
        parameters.put("personGroupId", name);
        Map<String, Object> requestBody = new HashMap();
        requestBody.put("name", name);
        requestBody.put("userData", userData);
        sendJsonBody(url, parameters, requestBody);
    }

    public static String identify(String personGroupId, double confidence, String faceId) throws JSONException, IOException{
        String url = FACE_API_URL + APIAction.IDENTIFY;
        JSONObject data = new JSONObject();
        data.put("personGroupId",personGroupId);
        data.put("faceIds", new String[]{faceId});
        data.put("maxNumOfCandidatesReturned", 1);
        data.put("confidenceThreshold", confidence);
        JSONObject response = rawRequest(url, null, data.toString().getBytes(CHARACTER_SET), "application/json");
        String s = response.keys().next();
        JSONObject face = response.getJSONArray(s).getJSONObject(0);
        JSONArray candidates = face.getJSONArray("candidates");
        JSONObject person = candidates.getJSONObject(0);
        return (String) person.get("personId");
    }

    public static String testDetect() {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bmp = BitmapFactory.decodeFile("C:\\Users\\James\\Desktop\\download.jpg", opt);
        return cognitiveResources.detect(bmp);
    }
}

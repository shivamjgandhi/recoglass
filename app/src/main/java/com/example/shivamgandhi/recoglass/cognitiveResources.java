package com.example.shivamgandhi.recoglass;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.media.Image;

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
    public static final String SUBSCRIPTION_KEY = "";
    public static final String FACE_API_URL = "https://westus.api.cognitive.microsoft.com/face/v1.0/";
    enum APIAction {
        DETECT ("detect");
        private String action;
        private APIAction(String s) {
            action = s;
        }
        public String toString() {
            return action;
        }
    }
    public static Map<String, Object> createRequest(String url, Map<String, String> values, byte[] body, String contentType) throws MalformedURLException, IOException, JSONException {
        String queryString = "";
        // Building url
        for (Map.Entry<String, String> entry : values.entrySet()) {
            queryString += "&" + entry.getKey() + "=" + entry.getValue();
        }
        queryString = queryString.substring(1);
        HttpURLConnection connection = (HttpURLConnection) new URL(url + "?" + queryString)
                .openConnection();
        connection.setRequestProperty("Accept-Charset", CHARACTER_SET.toString());
        // MS wants images like this
        connection.setRequestProperty("Content-Type", contentType);
        // Subscription key
        connection.setRequestProperty("Ocp-Apim-Subscription-Key", SUBSCRIPTION_KEY);
        connection.setRequestMethod("POST");
        connection.getOutputStream().write(body);
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
        Map<String, Object> responseData = new HashMap();
        JSONObject o = new JSONObject(res);
        Iterator<String> it = o.keys();
        while (it.hasNext()) {
            String key = it.next();
            responseData.put(key, o.get(key));
        }
        return responseData;
    }

    public static String detect(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, IMAGE_QUALITY, baos);
        String url = FACE_API_URL + APIAction.DETECT;
        Map<String, String> data = new HashMap();
        data.put("returnFaceId", ""+true);
        Map<String, Object> response = null;
        try {
            response = cognitiveResources.createRequest(url, data, baos.toByteArray(), "application/octet-stream");
        }
        catch (Exception e) {
            // TODO: Exception handling
        }
        String faceid = (String) response.get("faceId");
        return faceid;
    }

    public static String createPerson(Bitmap bmp) {

    }

    private static Map<String, Object> sendJsonBody(Map<String, String> vals) {
        for (Map.Entry e : vals.entrySet()) {

        }
    }
}

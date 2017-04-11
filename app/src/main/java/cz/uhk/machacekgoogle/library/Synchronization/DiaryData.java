package cz.uhk.machacekgoogle.library.Synchronization;

import android.content.ContentProviderClient;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import cz.uhk.machacekgoogle.AstroContract;
import cz.uhk.machacekgoogle.Config;
import cz.uhk.machacekgoogle.Exception.AccessTokenExpiredException;
import cz.uhk.machacekgoogle.Exception.Api400ErrorException;
import cz.uhk.machacekgoogle.Exception.ApiErrorException;
import cz.uhk.machacekgoogle.Model.DiaryObject;
import cz.uhk.machacekgoogle.coordinates.Angle;
import cz.uhk.machacekgoogle.library.Api.Http.Response;

/**
 * @author Jan Macháček
 *         Created on 27.11.2016.
 */
public class DiaryData {

    private ContentProviderClient contentProvider;
    private String access_token;

    private int serverCounter, userId;

    public DiaryData(ContentProviderClient contentProvider, String access_token) {
        this.contentProvider = contentProvider;
        this.access_token = access_token;
    }

    /**
     * @return
     * @throws AccessTokenExpiredException
     * @throws ApiErrorException
     */
    public ArrayList<DiaryObject> getDataFromServer() throws AccessTokenExpiredException, ApiErrorException {

        HttpURLConnection conn = null;
        try {

            Log.d("astro", "URL pro stahovaní dat ze serveru: " + getDiaryDownloadUrl(getClientCounter()));

            URL url = new URL(getDiaryDownloadUrl(getClientCounter()));
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            InputStream in = new BufferedInputStream(conn.getInputStream());
            String json = org.apache.commons.io.IOUtils.toString(in, "UTF-8");

            Log.d("astro", "DATA ZE SERVERU " + json);
            JSONObject jsonObject = new JSONObject(json);
            int serverCounter = jsonObject.getInt("servercounter");
            int userId = jsonObject.getInt("user_id");

            setServerCounter(serverCounter);
            setUserId(userId);

            Log.d("astro", "diary servercounter=" + Integer.toString(serverCounter));

            ArrayList<DiaryObject> newData = new ArrayList<DiaryObject>();

            try {
                JSONArray array = jsonObject.getJSONArray("objects");

                for (int i = 0; i < array.length(); i++) {
                    DiaryObject diaryObject = new DiaryObject();
                    JSONObject json_data = array.getJSONObject(i);
                    diaryObject.setGuid(json_data.getString("guid"));
                    diaryObject.setFrom(json_data.getString("from"));
                    diaryObject.setTo(json_data.getString("to"));
                    diaryObject.setLatitude(new Angle(json_data.getDouble("latitude")));
                    diaryObject.setLongitude(new Angle(json_data.getDouble("longitude")));
                    diaryObject.setRowCounter(json_data.getInt("counter"));
                    diaryObject.setTimestamp(json_data.getString("timestamp"));
                    diaryObject.setDeleted(json_data.getInt("deleted"));
                    diaryObject.setWeather(json_data.getString("weather"));
                    diaryObject.setLog(json_data.getString("log"));
                    diaryObject.setSyncOk(1);
                    newData.add(diaryObject);
                }
            } catch (JSONException e) {
                Log.d("astro", "JsonArray Errror -> " + e.getMessage());
                e.printStackTrace();
            }

            return newData;
        }catch (java.io.IOException e) {
            InputStream errorstream = conn.getErrorStream();
            String response = "";
            int code = 0;
            String line;
            if (null != errorstream) {
                BufferedReader br = new BufferedReader(new InputStreamReader(errorstream));
                try {
                    code = conn.getResponseCode();
                    while ((line = br.readLine()) != null) {
                        response += line;
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (code == 401) {
                throw new AccessTokenExpiredException();
            } else {
                Log.d("astro", "getVersionError " + e.toString() + " " + e.getMessage());
                throw new ApiErrorException(e.getMessage(), e);
            }

        } catch (Exception e) {
            Log.d("astro", "getDataFromServer exception " + e.toString());
            throw new ApiErrorException(e.getMessage(), e);
        }
    }


    /**
     * @param objects
     * @param clientCounter
     */
    public void sendDataToServer(ArrayList<DiaryObject> objects, int clientCounter) throws Api400ErrorException, IOException, JSONException {

        HttpURLConnection conn = null;
        try {


            conn = Response.diarySyncToServer(objects, clientCounter, access_token);
            InputStream in = new BufferedInputStream(conn.getInputStream());
            String json = org.apache.commons.io.IOUtils.toString(in, "UTF-8");
            Log.d("astro", "SEND DIARY TO SERVER RESPONSE: " + json);
        } catch (java.io.IOException e) {
            InputStream errorstream = conn.getErrorStream();
            String response = "";
            int code = 0;
            String line;
            if (null != errorstream) {
                BufferedReader br = new BufferedReader(new InputStreamReader(errorstream));
                try {
                    code = conn.getResponseCode();
                    while ((line = br.readLine()) != null) {
                        response += line;
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (code >= 400) {
                throw new Api400ErrorException(response);
            }
        }
    }

    private String getDiaryDownloadUrl(int client_counter) {
        return Config.API_URL + Config.API_DIARY_DATA + "/" + Integer.toString(client_counter) + "?access_token=" + this.access_token;
    }

    private int getClientCounter() throws RemoteException {

        Uri uri = Uri.parse(AstroContract.DIARY_URI + "/settings");
        int client_counter = 0;

        Cursor c = contentProvider.query(uri, null, null, null, null);

        if (c.moveToFirst()) {
            do {
                if (c.getString(0).equals("client_counter")) {
                    client_counter = c.getInt(1);
                }
            } while (c.moveToNext());
        }

        return client_counter;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getServerCounter() {
        return serverCounter;
    }

    public void setServerCounter(int serverCounter) {
        this.serverCounter = serverCounter;
    }
}

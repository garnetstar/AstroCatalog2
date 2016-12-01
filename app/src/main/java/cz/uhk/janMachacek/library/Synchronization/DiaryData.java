package cz.uhk.janMachacek.library.Synchronization;

import android.content.ContentProvider;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import cz.uhk.janMachacek.AstroContract;
import cz.uhk.janMachacek.Config;
import cz.uhk.janMachacek.Exception.AccessTokenExpiredException;
import cz.uhk.janMachacek.Exception.ApiErrorException;
import cz.uhk.janMachacek.Model.AstroObject;
import cz.uhk.janMachacek.Model.DiaryObject;
import cz.uhk.janMachacek.library.Api.Http.Utils;

/**
 * @author Jan Macháček
 *         Created on 27.11.2016.
 */
public class DiaryData {

    private ContentProviderClient contentProvider;
    private String access_token;

    public DiaryData(ContentProviderClient contentProvider, String access_token) {
        this.contentProvider = contentProvider;
        this.access_token = access_token;
    }

    public void sync() {

        try {
            getDataFromServer();
        } catch (AccessTokenExpiredException e) {
            e.printStackTrace();
        } catch (ApiErrorException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @return
     * @throws AccessTokenExpiredException
     * @throws ApiErrorException
     */
    public ArrayList<DiaryObject> getDataFromServer() throws AccessTokenExpiredException, ApiErrorException {

        try {

            Log.d("astro", "DIary URL = " + getDiaryDownloadUrl(getClientCounter()));

            HttpGet get = new HttpGet(getDiaryDownloadUrl(getClientCounter()));
            HttpClient httpClient = new DefaultHttpClient();
            HttpResponse response = httpClient.execute(get);
            String json = Utils.convertInputStreamToString(response.getEntity().getContent());
            JSONObject jsonObject = new JSONObject(json);
            //kontrola http statusu
            int httpStatus = response.getStatusLine().getStatusCode();
            if (httpStatus == 401) {
                throw new AccessTokenExpiredException();
            }

            JSONArray array = jsonObject.getJSONArray("objects");
            int serverCounter = jsonObject.getInt("servercounter");
            int nextId = jsonObject.getInt("next_id");
            Log.d("astro", "diary servercounter=" + Integer.toString(serverCounter) + " nextId=" + Integer.toString(nextId) + " pocet=" + Integer.toString(array.length()));

            ArrayList<DiaryObject> newData = new ArrayList<DiaryObject>();

            for (int i = 0; i < array.length(); i++) {
                DiaryObject diaryObject = new DiaryObject();
                JSONObject json_data = array.getJSONObject(i);
                diaryObject.setGuid(json_data.getString("guid"));
                diaryObject.setFrom(json_data.getString("from"));
                diaryObject.setTo(json_data.getString("to"));
                diaryObject.setSyncOk(1);

                Log.d("astro", "XXXXX " + diaryObject.getId());
                newData.add(diaryObject);
                Log.d("Response: DIARY", json_data.getString("guid") + " " + json_data.getString("from"));

            }

            Log.d("astro", "newData=" + newData.toString());
            return newData;
        } catch (Exception e) {
            Log.d("astro", "getDiaryData exception " + e.toString());
            throw new ApiErrorException(e.getMessage(), e);
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
                Log.d("astro", c.getString(0) + " " + Integer.toString(c.getInt(1)));
                if (c.getString(0).equals("client_counter")) {
                    client_counter = c.getInt(1);
                }
            } while (c.moveToNext());
        }

        return client_counter;
    }
}

package cz.uhk.machacekgoogle.library.Api.Http;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import cz.uhk.machacekgoogle.AstroContract;
import cz.uhk.machacekgoogle.Config;
import cz.uhk.machacekgoogle.Model.DiaryObject;

/**
 * Created by jan on 1.8.2016.
 */
public class Response {

    public static HttpURLConnection refreshToken(String refreshToken) throws IOException, JSONException {
        URL url = new URL(Config.API_URL + Config.API_TOKEN);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        DataOutputStream wr = new DataOutputStream(conn.getOutputStream());

        JSONObject obj = new JSONObject();
        obj.put(Config.API_REFRESH_TOKEN, refreshToken);
        obj.put(Config.API_GRANT_TYPE, "refresh_token");
        obj.put("client_id", Config.API_CLIENT_ID);

        wr.writeBytes(obj.toString());
        Log.e("astro", "JSON Input" + obj.toString());
        wr.flush();
        wr.close();

        return conn;
    }

    public static HttpURLConnection accessTokenByCredentials(String login, String password) throws IOException, JSONException {

        URL url = new URL(Config.API_URL + Config.API_TOKEN);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
        JSONObject obj = new JSONObject();

        obj.put("login", login);
        obj.put("password", password);
        obj.put(Config.API_GRANT_TYPE, "password");
        obj.put("client_id", Config.API_CLIENT_ID);

        wr.writeBytes(obj.toString());
        Log.d("astro", "JSON Input" + obj.toString());
        wr.flush();
        wr.close();

        return conn;
    }

    public static HttpURLConnection diarySyncToServer(ArrayList<DiaryObject> objects, int lastClientCounter, String access_token) throws JSONException, IOException {

        String urlDiary = Config.API_URL + Config.API_DIARY_DATA + "/" + Integer.toString(lastClientCounter) + "?access_token=" + access_token;
        Log.d("astro", "Url pro odesílání dat na server: " + urlDiary);

        URL url = new URL(urlDiary);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < objects.size(); i++) {
            JSONObject one = new JSONObject();
            one.put("guid", objects.get(i).getGuid());
            one.put("from", objects.get(i).getFrom());
            one.put("to", objects.get(i).getTo());
            one.put("latitude", objects.get(i).getLatitude().getDecimalDegree());
            one.put("longitude", objects.get(i).getLongitude().getDecimalDegree());
            one.put("notice", null);
            one.put("deleted", objects.get(i).getDeleted());
            one.put("row_counter", objects.get(i).getRowCounter());
            one.put("new", objects.get(i).isNew() ? 1 : 0);
            one.put("weather", objects.get(i).getWeather().trim());
            one.put("log", objects.get(i).getLog().trim());

            jsonArray.put(one);
        }
        jsonObject.put("objects", jsonArray);
        Log.d("astro", "Objekty které se budou posílat na server " + jsonObject.toString());

        OutputStream os = conn.getOutputStream();
        BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(os, "UTF-8"));
        writer.write(jsonObject.toString());
        writer.flush();
        writer.close();
        os.close();
        return conn;
    }


    /**
     * @param latitude
     * @param longitude
     * @return
     * @throws IOException
     * @throws JSONException
     */
    public static JSONObject getWeatherData(double latitude, double longitude) throws IOException, JSONException {

        String lat = "&lat=" + Double.toString(latitude);
        String lon = "&lon=" + Double.toString(longitude);
        String weatherUrl = AstroContract.weatherUri + "&appid=" + AstroContract.weatherApiKey + lat + lon;
        Log.d("astro", "WEATHER> " + weatherUrl);
        URL url = new URL(weatherUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        InputStream in = new BufferedInputStream(conn.getInputStream());
        String json = org.apache.commons.io.IOUtils.toString(in, "UTF-8");

        Log.d("astro", "WEATHER API DATA " + json);
        JSONObject jsonObject = new JSONObject(json);
        return jsonObject;
    }
}

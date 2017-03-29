package cz.uhk.machacekgoogle.library.Api.Http;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import cz.uhk.machacekgoogle.AstroContract;
import cz.uhk.machacekgoogle.Config;
import cz.uhk.machacekgoogle.Model.DiaryObject;

/**
 * Created by jan on 1.8.2016.
 */
public class Response {

    public static HttpGet messierDataRequest(String url) {

        HttpGet get = new HttpGet(url);
        return get;
    }

    public static HttpPost refreshToken(String refreshToken) throws UnsupportedEncodingException {
        String url = Config.API_URL + Config.API_TOKEN;

        HttpPost post = new HttpPost(url);

        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);

        nameValuePairs.add(new BasicNameValuePair(Config.API_REFRESH_TOKEN, refreshToken));
        nameValuePairs.add(new BasicNameValuePair(Config.API_GRANT_TYPE, "refresh_token"));
        nameValuePairs.add(new BasicNameValuePair("client_id", Config.API_CLIENT_ID));

        post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
        return post;
    }

    public static HttpPost accessTokenByCredentials(String login, String password) throws UnsupportedEncodingException {

        HttpPost httpPost = new HttpPost(Config.API_URL + Config.API_TOKEN);
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
        nameValuePairs.add(new BasicNameValuePair("login", login));
        nameValuePairs.add(new BasicNameValuePair("password", password));
        nameValuePairs.add(new BasicNameValuePair(Config.API_GRANT_TYPE, "password"));
        nameValuePairs.add(new BasicNameValuePair("client_id", Config.API_CLIENT_ID));

        httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

        return httpPost;
    }

    public static HttpPost diarySyncToServer(ArrayList<DiaryObject> objects, int lastClientCounter, String access_token) throws JSONException, UnsupportedEncodingException {

        String url = Config.API_URL + Config.API_DIARY_DATA + "/" + Integer.toString(lastClientCounter) + "?access_token=" + access_token;
        Log.d("astro", "Url pro odesílání dat na server: " + url);
        HttpPost httpPost = new HttpPost(url);

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

        // je třeba nastavit správné kódování kvůli kompatibilitě se serverem
        httpPost.setEntity(new StringEntity(jsonObject.toString(), "UTF8"));
        httpPost.setHeader("Content-type", "application/json");
        return httpPost;
    }


    /**
     *
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
        HttpGet get = new HttpGet(weatherUrl);
        HttpClient httpClient = new DefaultHttpClient();
        HttpResponse response = httpClient.execute(get);
        String json = Utils.convertInputStreamToString(response.getEntity().getContent());
        Log.d("astro", "WEATHER API DATA " + json);
        JSONObject jsonObject = new JSONObject(json);
        return jsonObject;
    }
}

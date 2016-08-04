package cz.uhk.janMachacek.library.Sync;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

import cz.uhk.janMachacek.Config;
import cz.uhk.janMachacek.Exception.AccessTokenExpiredException;
import cz.uhk.janMachacek.Exception.ApiErrorException;
import cz.uhk.janMachacek.Exception.EmptyCredentialsException;
import cz.uhk.janMachacek.library.Api.Http.Response;
import cz.uhk.janMachacek.library.Api.Http.Utils;
import cz.uhk.janMachacek.library.Api.Facade;

/**
 * Created by jan on 1.8.2016.
 */
public class MessierData {

    private Facade apiFacade;
    private HttpClient httpClient;

    public MessierData(Facade apiFacade) {
        this.apiFacade = apiFacade;
        this.httpClient = new DefaultHttpClient();
    }

    public void sync() {




        String url = getUrl();

        boolean next = true;
        boolean refreshToken = false;
        do {
            try {
                if(refreshToken) {
                    Log.d("Response", "pokus o refersh tokenu");
                    apiFacade.refreshAccessToken();
                    url = getUrl();
                    Log.d("Respone", "url = " + url);
                }
                getData(url);
                next = false;
            } catch (ApiErrorException e) {
                e.printStackTrace();
                Log.d("Response", "1/ " + e.toString());
                next = false;
            } catch (AccessTokenExpiredException e) {

                Log.d("Response", "refersh token in loop" + e.toString());
                refreshToken = true;
                next = true;
            }
        } while (next == true);

    }

    private void getData(String url) throws ApiErrorException, AccessTokenExpiredException {

        try {
            Log.d("Response ", "URL: " + url);
            HttpGet get = Response.messierDataRequest(url);
            HttpResponse response = httpClient.execute(get);
            String json = Utils.convertInputStreamToString(response.getEntity().getContent());
            JSONObject jsonObject = new JSONObject(json);
            //kontrola http statusu
            int httpStatus = response.getStatusLine().getStatusCode();
            if (httpStatus == 401) {
                throw new AccessTokenExpiredException();
            }

            JSONArray array = jsonObject.getJSONArray("list");
            for (int i = 0; i < array.length(); i++) {
                JSONObject json_data = array.getJSONObject(i);
                Log.d("Response: CONST", json_data.getString("messier_id") + " " + json_data.getString("constellation"));
            }

            // polkud hlavička obsahuje "Link" a rel="next" vola metoda rekurzivně same sebe
            HashMap<String, String> headers = Utils.convertHeadersToHashMap(response.getAllHeaders());

            if (null != headers.get("Link")) {
                String[] link = headers.get("Link").split(";");
                if (link[1].matches("rel=\"next\"")) {
                    String nextUrl = link[0].substring(1, link[0].length() - 1);
                    getData(nextUrl);
                }
            }
        } catch (AccessTokenExpiredException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiErrorException(e.getMessage(), e);
        }
    }

    private String getUrl() {
        Log.d("Response", "AT = " + apiFacade.getAccessToken());
        return Config.API_URL + Config.API_MESSIER_DATA + "?" + Config.API_ACCESS_TOKEN + "=" + apiFacade.getAccessToken();
    }
}


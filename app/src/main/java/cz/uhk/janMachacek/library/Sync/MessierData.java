package cz.uhk.janMachacek.library.Sync;

import android.util.Log;

import org.apache.http.Header;
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
import cz.uhk.janMachacek.library.ApiHttp.Response;
import cz.uhk.janMachacek.library.ApiHttp.Utils;
import cz.uhk.janMachacek.model.Connector;

/**
 * Created by jan on 1.8.2016.
 */
public class MessierData {

    private Connector connector;
    private HttpClient httpClient;

    public MessierData(Connector connector) {
        this.connector = connector;
        this.httpClient = new DefaultHttpClient();
    }

    public void sync() throws EmptyCredentialsException {
        try {

            String token = connector.getToken();
            String url = Config.API_URL + Config.API_MESSIER_DATA + "?" + Config.API_ACCESS_TOKEN + "=" + token;

            getData(url);

        } catch (AccessTokenExpiredException e) {

            try {
                connector.refreshAccessToken();
                //getData(url);
                sync();
            } catch (ApiErrorException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }

        } catch (ApiErrorException e) {
            Log.d("Response", e.toString());
        } catch (ClientProtocolException e) {
            Log.d("Response", e.toString());
        } catch (IOException e) {
            Log.d("Response", e.toString());
        } catch (JSONException e) {
            Log.d("Response", e.toString());
        }
    }

    private void getData(String url) throws ApiErrorException, EmptyCredentialsException, IOException, JSONException {

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
    }
}


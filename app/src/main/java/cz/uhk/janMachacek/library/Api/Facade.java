package cz.uhk.janMachacek.library.Api;

import android.content.SharedPreferences;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import cz.uhk.janMachacek.Config;
import cz.uhk.janMachacek.Exception.ApiErrorException;
import cz.uhk.janMachacek.Exception.EmptyCredentialsException;
import cz.uhk.janMachacek.Exception.WrongCredentialsException;
import cz.uhk.janMachacek.library.Api.Http.Response;
import cz.uhk.janMachacek.library.Api.Http.Utils;

/**
 * Created by jan on 22.7.2016.
 */
public class Facade {

    private SharedPreferences preferences;

    public Facade(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    public String getToken() throws EmptyCredentialsException, ApiErrorException {

        String token;

        if (preferences.getString("access_token", null) == null) {
            // ziskat token z api
            //    token = this.getTokenByLogin();
        } else {
            token = preferences.getString("access_token", null);
        }

        //return token;
        return "sss";
    }

    public void getTokenByLogin() throws EmptyCredentialsException, WrongCredentialsException, ApiErrorException {

        String accessToken, refreshToken, login, pass;
        SharedPreferences.Editor editor = preferences.edit();

        login = preferences.getString(Config.API_LOGIN, null);
        pass = preferences.getString(Config.API_PASSWORD, null);

        if (login == null || pass == null)
            throw new EmptyCredentialsException();

        Log.d("Response ", "login: " + login);
        Log.d("Response ", "pass: " + pass);
        HttpClient httpClient = new DefaultHttpClient();

        try {

            HttpPost httpPost = Response.accessTokenByCredentials(login, pass);

            HttpResponse response = httpClient.execute(httpPost);

            String json = convertInputStreamToString(response.getEntity().getContent());
            JSONObject jsonObject = new JSONObject(json);
            //kontrola http statusu
            int httpStatus = response.getStatusLine().getStatusCode();
            if (httpStatus >= 400) {
                String message = jsonObject.getString("message");
                throw new WrongCredentialsException(message + ": wrong credentials");
            }

            Log.d("Response", json);

            accessToken = jsonObject.getString("access_token");
            refreshToken = jsonObject.getString("refresh_token");

            //ulozit novy accessToken do preferenci
            editor.putString("access_token", accessToken);
            editor.putString("refresh_token", refreshToken);
            editor.commit();
        } catch (WrongCredentialsException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiErrorException(e.getMessage(), e);
        }
    }

    public void refreshAccessToken() throws ApiErrorException {

        String refreshToken = preferences.getString(Config.API_REFRESH_TOKEN, null);
        try {
            HttpPost post = Response.refreshToken(refreshToken);
            HttpClient httpClient = new DefaultHttpClient();

            HttpResponse response = httpClient.execute(post);

            String json = Utils.convertInputStreamToString(response.getEntity().getContent());

            Log.d("Response", json);

            JSONObject jsonObject = null;

            jsonObject = new JSONObject(json);

            //kontrola http statusu
            int httpStatus = response.getStatusLine().getStatusCode();
            if (httpStatus >= 400) {
                String message = jsonObject.getString("message");
                throw new ApiErrorException(message + ": wrong credentials");
            }
            String accessToken = jsonObject.getString(Config.API_ACCESS_TOKEN);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(Config.API_ACCESS_TOKEN, accessToken);
            editor.commit();
        } catch (Exception e) {
            throw new ApiErrorException(e.getMessage(), e);
        }
    }

    public String getAccessToken() {
        return preferences.getString(Config.API_ACCESS_TOKEN, null);
    }


    // convert inputstream to String
    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while ((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;
    }
}

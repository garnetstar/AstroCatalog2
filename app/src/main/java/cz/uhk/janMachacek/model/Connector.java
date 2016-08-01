package cz.uhk.janMachacek.model;

import android.content.SharedPreferences;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import cz.uhk.janMachacek.Config;
import cz.uhk.janMachacek.Exception.ApiErrorException;
import cz.uhk.janMachacek.Exception.EmptyCredentialsException;

/**
 * Created by jan on 22.7.2016.
 */
public class Connector {

    private SharedPreferences preferences;

    public Connector(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    public String getToken() throws EmptyCredentialsException, ApiErrorException {

        String token;

        if (preferences.getString("access_token", null) == null) {
                // ziskat token z api
                token = this.getTokenByLogin();
        } else {
            token = preferences.getString("access_token", null);
        }

        return token;
    }

    public String getTokenByLogin() throws EmptyCredentialsException, ApiErrorException {

        String accessToken, refreshToken, login, pass;
        SharedPreferences.Editor editor = preferences.edit();

        login = preferences.getString(Config.API_LOGIN, null);
        pass = preferences.getString(Config.API_PASSWORD, null);

        if(login == null || pass == null)
        throw new EmptyCredentialsException();

        Log.d("Response ", "login: " + login);
        Log.d("Response ", "pass: " + pass);
        HttpClient httpClient = new DefaultHttpClient();

        HttpPost httpPost = new HttpPost(Config.API_URL + Config.API_TOKEN);
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
        nameValuePairs.add(new BasicNameValuePair("login", login));
        nameValuePairs.add(new BasicNameValuePair("password", pass));
        nameValuePairs.add(new BasicNameValuePair(Config.API_GRANT_TYPE, "password"));
        nameValuePairs.add(new BasicNameValuePair("client_id", Config.API_CLIENT_ID));

        try {
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            HttpResponse response = httpClient.execute(httpPost);

            String json = convertInputStreamToString(response.getEntity().getContent());
            JSONObject jsonObject = new JSONObject(json);
            //kontrola http statusu
            int httpStatus = response.getStatusLine().getStatusCode();
            if (   httpStatus >= 400) {
                String message = jsonObject.getString("message");
                throw new ApiErrorException(message + ": wrong credentials");
            }

            Log.d("Response", json);

            accessToken = jsonObject.getString("access_token");
            refreshToken = jsonObject.getString("refresh_token");

            //ulozit novy accessToken do preferenci
            editor.putString("access_token", accessToken);
            editor.putString("refresh_token", refreshToken);
            editor.commit();
            return accessToken;

        }catch (Exception e) {
            throw new ApiErrorException(e.getMessage(), e);
        }
    }

    public String getTokenByRefreshToken() {
        return "refresh token";
    }


    // convert inputstream to String
    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;
    }
}

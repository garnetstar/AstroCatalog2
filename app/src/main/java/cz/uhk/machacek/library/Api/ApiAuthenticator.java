package cz.uhk.machacek.library.Api;

import android.content.SharedPreferences;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import cz.uhk.machacek.Config;
import cz.uhk.machacek.Exception.ApiErrorException;
import cz.uhk.machacek.Exception.InvalidateRefreshTokenException;
import cz.uhk.machacek.Exception.WrongCredentialsException;
import cz.uhk.machacek.library.Api.Http.Response;
import cz.uhk.machacek.library.Api.Http.Utils;

/**
 * Třída zapouzdřuje obstarání auth-tokenu ze serveru
 * Created by jan on 22.7.2016.
 */
public class ApiAuthenticator {

    private SharedPreferences preferences;

    public ApiAuthenticator(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    /**
     * @param login
     * @param password
     * @return
     * @throws WrongCredentialsException
     * @throws ApiErrorException
     */
    public static String[] getTokenByLogin(String login, String password) throws WrongCredentialsException, ApiErrorException {

        Log.d("astro", "GET auth_token");

        HttpClient httpClient = new DefaultHttpClient();

        try {

            HttpPost httpPost = Response.accessTokenByCredentials(login, password);
            HttpResponse response = httpClient.execute(httpPost);

            String json = Utils.convertInputStreamToString(response.getEntity().getContent());
            JSONObject jsonObject = new JSONObject(json);
            //kontrola http statusu
            int httpStatus = response.getStatusLine().getStatusCode();
            if (httpStatus >= 400) {
                String message = jsonObject.getString("message");
                throw new WrongCredentialsException(message + ": wrong credentials");
            }

            Log.d("astro", "getTokenByLogin"  + json);

            String accessToken = jsonObject.getString("access_token");
            String refreshToken = jsonObject.getString("refresh_token");
            String[] ret = new String[2];
            ret[0] = accessToken;
            ret[1] = refreshToken;
            return ret;

        } catch (WrongCredentialsException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiErrorException(e.getMessage(), e);
        }
    }

    public static String[] getAccessTokenByRefreshToken(String refreshToken) throws ApiErrorException, InvalidateRefreshTokenException {

        Log.d("astro", "method> getAccessTokenByRefreshToken");

        HttpPost post = null;
        try {
            post = Response.refreshToken(refreshToken);

            HttpClient httpClient = new DefaultHttpClient();

            HttpResponse response = httpClient.execute(post);

            String json = Utils.convertInputStreamToString(response.getEntity().getContent());

            Log.d("astro", "ssddgg" +  json);

            JSONObject jsonObject = new JSONObject(json);

            //kontrola http statusu
            int httpStatus = response.getStatusLine().getStatusCode();
            if (httpStatus >= 400) {
                String message = jsonObject.getString("message");
                throw new InvalidateRefreshTokenException(message);
            }
            String accessToken = jsonObject.getString(Config.API_ACCESS_TOKEN);
            refreshToken = jsonObject.getString(Config.API_REFRESH_TOKEN);

            String[] ret = new String[2];
            ret[0] = accessToken;
            ret[1] = refreshToken;
            return ret;

        }catch (InvalidateRefreshTokenException e) {
            throw e;
        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

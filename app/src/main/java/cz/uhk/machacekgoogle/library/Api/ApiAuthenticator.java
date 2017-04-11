package cz.uhk.machacekgoogle.library.Api;

import android.content.SharedPreferences;
import android.util.Log;
import org.json.JSONObject;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import cz.uhk.machacekgoogle.Config;
import cz.uhk.machacekgoogle.Exception.ApiErrorException;
import cz.uhk.machacekgoogle.Exception.InvalidateRefreshTokenException;
import cz.uhk.machacekgoogle.Exception.WrongCredentialsException;
import cz.uhk.machacekgoogle.library.Api.Http.Response;

/**
 * Třída zapouzdřuje obstarání auth-tokenu ze serveru
 * Created by jan on 22.7.2016.
 */
public class ApiAuthenticator {

    /**
     * @param login
     * @param password
     * @return
     * @throws WrongCredentialsException
     * @throws ApiErrorException
     */
    public static String[] getTokenByLogin(String login, String password) throws WrongCredentialsException, ApiErrorException {

        Log.d("astro", "GET auth_token");

        HttpURLConnection conn = null;

        try {

            conn = Response.accessTokenByCredentials(login, password);
            InputStream in = new BufferedInputStream(conn.getInputStream());
            String json = org.apache.commons.io.IOUtils.toString(in, "UTF-8");
            JSONObject jsonObject = new JSONObject(json);
            Log.d("astro", "getTokenByLogin"  + json);

            String accessToken = jsonObject.getString("access_token");
            String refreshToken = jsonObject.getString("refresh_token");
            String[] ret = new String[2];
            ret[0] = accessToken;
            ret[1] = refreshToken;
            return ret;

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
                Log.d("astro", "REFRESH TOKEN ERROR 3 " + response);
                throw new WrongCredentialsException(response);
            } else {
                Log.d("astro", "REFRESH TOKEN ERROR 3 " + e.toString() + " " + e.getMessage());
                throw new ApiErrorException(e.getMessage(), e);
            }

        }  catch (Exception e) {
            throw new ApiErrorException(e.getMessage(), e);
        }
    }

    public static String[] getAccessTokenByRefreshToken(String refreshToken) throws ApiErrorException, InvalidateRefreshTokenException {

        Log.d("astro", "method> getAccessTokenByRefreshToken");

        HttpURLConnection conn = null;
        try {
            conn = Response.refreshToken(refreshToken);
            InputStream in = new BufferedInputStream(conn.getInputStream());
            String json = org.apache.commons.io.IOUtils.toString(in, "UTF-8");

            JSONObject jsonObject = new JSONObject(json);
            String accessToken = jsonObject.getString(Config.API_ACCESS_TOKEN);
            refreshToken = jsonObject.getString(Config.API_REFRESH_TOKEN);

            String[] ret = new String[2];
            ret[0] = accessToken;
            ret[1] = refreshToken;
            return ret;

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
            if (code >= 400) {
                throw new InvalidateRefreshTokenException(response);
            } else {
                Log.d("astro", "REFRESH TOKEN ERROR 1 " + e.toString() + " " + e.getMessage());
                throw new ApiErrorException(e.getMessage(), e);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.d("astro", "REFRESH TOKEN ERROR 2 " + e.toString());
            return null;
        }
    }
}

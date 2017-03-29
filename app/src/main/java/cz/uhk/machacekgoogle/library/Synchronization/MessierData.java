package cz.uhk.machacekgoogle.library.Synchronization;

import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import cz.uhk.machacekgoogle.Config;
import cz.uhk.machacekgoogle.Exception.AccessTokenExpiredException;
import cz.uhk.machacekgoogle.Exception.ApiErrorException;
import cz.uhk.machacekgoogle.ObjectListActivity;
import cz.uhk.machacekgoogle.coordinates.Angle;
import cz.uhk.machacekgoogle.library.Api.Http.Response;
import cz.uhk.machacekgoogle.library.Api.Http.Utils;
import cz.uhk.machacekgoogle.library.Api.ApiAuthenticator;
import cz.uhk.machacekgoogle.AstroContract;
import cz.uhk.machacekgoogle.Model.AstroObject;

/**
 * Created by jan on 1.8.2016.
 */
public class MessierData {

    private ApiAuthenticator apiAuthenticator;
    private HttpClient httpClient;
    private Context context;
    private int actualVersion;

    public MessierData(ApiAuthenticator apiAuthenticator, Context context) {
        this.apiAuthenticator = apiAuthenticator;
        this.httpClient = new DefaultHttpClient();
        this.context = context;
    }

    public MessierData() {
        actualVersion = 0;
    }

    public ArrayList<AstroObject> getMessierData(String accessToken, Context context, ContentProviderClient providerClient) throws AccessTokenExpiredException, ApiErrorException {

        Log.d("astro", "static sync start");

        String url = getUrl(accessToken);

        int serverVersion = getVersion(accessToken);
        Log.d("astro", "MS VERSION=" + serverVersion);

        // default preference
        SharedPreferences preferences = context.getSharedPreferences(context.getPackageName() + "_preferences", Context.MODE_PRIVATE);

        int deviceVersion = preferences.getInt("ms_version", 0);
        Log.d("astro", "act>" + deviceVersion + " version>" + serverVersion);
        actualVersion = serverVersion;

        if (deviceVersion < serverVersion) {

            ArrayList<AstroObject> astroObjects = new ArrayList<AstroObject>();

            getData(url, astroObjects);

            try {
                providerClient.update(Uri.parse(AstroContract.CATALOG_URI + "/messier"),null,null, null);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            //aktualizovat verzi katalogu
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt("ms_version", serverVersion);
            editor.commit();
            Log.d("astro", "set new version " + serverVersion);

            //zobrazit aktuální data
            Intent intent = new Intent();
            intent.setAction(ObjectListActivity.REFRESH_OBJECTS_LIST);
            Log.d("Response", "SEND BROADCAST *");
            context.sendBroadcast(intent);
            return astroObjects;
        }
        return null;
    }

    public int getActualVersion() {
        return actualVersion;
    }


    private int getVersion(String accessToken) throws AccessTokenExpiredException, ApiErrorException {

        String url = getVersionUrl(accessToken);
        Log.d("astro ", "GET VERSION MESSIER 1 " + url);

        try {
            HttpGet get = Response.messierDataRequest(url);
            HttpClient httpClient = new DefaultHttpClient();
            HttpResponse response = httpClient.execute(get);
            String json = Utils.convertInputStreamToString(response.getEntity().getContent());
            Log.d("astro", "version > " + json);
            JSONObject jsonObject = new JSONObject(json);
            //kontrola http statusu
            int httpStatus = response.getStatusLine().getStatusCode();
            if (httpStatus == 401) {
                throw new AccessTokenExpiredException();
            }
            int version = jsonObject.getInt("version");
            return version;

        } catch (AccessTokenExpiredException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("astro", "7894554" + e.toString() + " " + e.getMessage());
            throw new ApiErrorException(e.getMessage(), e);
        }
    }

    private ArrayList<AstroObject> getData(String url, ArrayList<AstroObject> astroObjects) throws ApiErrorException, AccessTokenExpiredException {

        try {
            Log.d("Response ", "URL: " + url);
            HttpGet get = Response.messierDataRequest(url);
            HttpClient httpClient = new DefaultHttpClient();
            HttpResponse response = httpClient.execute(get);
            String json = Utils.convertInputStreamToString(response.getEntity().getContent());
            JSONObject jsonObject = new JSONObject(json);
            //kontrola http statusu
            int httpStatus = response.getStatusLine().getStatusCode();
            if (httpStatus == 401) {
                throw new AccessTokenExpiredException();
            }

            JSONArray array = jsonObject.getJSONArray("list");

            AstroObject astroObject = null;

            for (int i = 0; i < array.length(); i++) {
                astroObject = new AstroObject();
                JSONObject json_data = array.getJSONObject(i);
                Log.d("Response: CONST", json_data.getString("messier_id") + " " + json_data.getString("constellation"));

                astroObject.setName("M" + json_data.getString("messier_id"));

                astroObject.setConstellation(json_data.getString("constellation"));

                astroObject.setType(json_data.getInt("type"));

                Integer ra_deg = json_data.getInt("ra_deg");
                Double ra_min = json_data.getDouble("ra_min");
                Angle angleHour = new Angle(ra_deg, ra_min, true);
                astroObject.setRightAscension(new Angle(angleHour.getDecimalDegree() * 15));

                Double dec_min = json_data.getDouble("dec_min");
                Integer dec_deg = Integer.parseInt(json_data.getString("dec_deg").replaceFirst("\\+", ""));
                boolean positive = true;
                if (dec_deg < 0) {
                    positive = false;
                    dec_deg = dec_deg * -1;
                }
                astroObject.setDeclination(new Angle(dec_deg, dec_min, positive));
                astroObject.setMagnitude(json_data.getDouble("magnitude"));
                astroObject.setDistance(json_data.getDouble("distance"));
                astroObjects.add(astroObject);
            }

            // polkud hlavička obsahuje "Link" a rel="next" vola metoda rekurzivně same sebe
            HashMap<String, String> headers = Utils.convertHeadersToHashMap(response.getAllHeaders());

            if (null != headers.get("Link")) {
                String[] link = headers.get("Link").split(";");
                if (link[1].matches("rel=\"next\"")) {
                    String nextUrl = link[0].substring(1, link[0].length() - 1);
                    getData(nextUrl, astroObjects);
                }
            }

        } catch (AccessTokenExpiredException e) {
            throw e;
        } catch (Exception e) {

            Log.d("astro", "getData exception " + e.toString());
            throw new ApiErrorException(e.getMessage(), e);
        }


        return astroObjects;
    }

    private static String getUrl(String accessToken) {
        return Config.API_URL + Config.API_MESSIER_DATA + "?" + Config.API_ACCESS_TOKEN + "=" + accessToken;
    }

    private static String getVersionUrl(String accessToken) {
        return Config.API_URL + Config.API_MESSIER_DATA + "/version?" + Config.API_ACCESS_TOKEN + "=" + accessToken;
    }
}


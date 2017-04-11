package cz.uhk.machacekgoogle.library.Synchronization;

import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import cz.uhk.machacekgoogle.Config;
import cz.uhk.machacekgoogle.Exception.AccessTokenExpiredException;
import cz.uhk.machacekgoogle.Exception.ApiErrorException;
import cz.uhk.machacekgoogle.Model.AstroDbHelper;
import cz.uhk.machacekgoogle.ObjectListActivity;
import cz.uhk.machacekgoogle.coordinates.Angle;
import cz.uhk.machacekgoogle.AstroContract;
import cz.uhk.machacekgoogle.Model.AstroObject;

/**
 * Created by jan on 1.8.2016.
 */
public class MessierData {

    private int actualVersion;

    public MessierData() {
        actualVersion = 0;
    }

    public ArrayList<AstroObject> getMessierData(String accessToken, Context context, ContentProviderClient providerClient) throws AccessTokenExpiredException, ApiErrorException, RemoteException {

        Log.d("astro", "static sync start");

        String url = getUrl(accessToken);

        int serverVersion = getVersion(accessToken);
        Log.d("astro", "MS VERSION=" + serverVersion);
        int deviceVersion = getMessierVersion(providerClient);
        Log.d("astro", "act messier version = " + deviceVersion + " version>" + serverVersion);
        actualVersion = serverVersion;

        if (deviceVersion < serverVersion) {
            ArrayList<AstroObject> astroObjects = new ArrayList<AstroObject>();

            getData(url, astroObjects);

            try {
                providerClient.update(Uri.parse(AstroContract.CATALOG_URI + "/messier"), null, null, null);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            // ulozit serverCounter do clientCounter
            ContentValues val = new ContentValues();
            val.put(AstroDbHelper.KEY_SETTINGS_VALUE, serverVersion);
            providerClient.update(Uri.parse(AstroContract.CATALOG_URI + "/settings"), val, AstroDbHelper.KEY_SETTINGS_KEY + "=?", new String[]{"messier_version"});
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

        String versionUrl = getVersionUrl(accessToken);
        Log.d("astro ", "GET VERSION MESSIER 1 " + versionUrl);
        HttpURLConnection conn = null;
        try {
            URL url = new URL(versionUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            InputStream in = new BufferedInputStream(conn.getInputStream());
            String json = org.apache.commons.io.IOUtils.toString(in, "UTF-8");
            Log.d("astro", "version (HttpUrlConnection) > " + json);
            JSONObject jsonObject = new JSONObject(json);
            int version = jsonObject.getInt("version");
            return version;

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

            if (code == 401) {
                throw new AccessTokenExpiredException();
            } else {
                Log.d("astro", "getVersionError " + e.toString() + " " + e.getMessage());
                throw new ApiErrorException(e.getMessage(), e);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.d("astro", "getVersionError " + e.toString() + " " + e.getMessage());
            throw new ApiErrorException(e.getMessage(), e);
        }
    }

    private ArrayList<AstroObject> getData(String targetUrl, ArrayList<AstroObject> astroObjects) throws ApiErrorException, AccessTokenExpiredException {

        HttpURLConnection conn = null;
        try {
            Log.d("Response ", "URL: " + targetUrl);
            URL url = new URL(targetUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            InputStream in = new BufferedInputStream(conn.getInputStream());
            String json = org.apache.commons.io.IOUtils.toString(in, "UTF-8");


            JSONObject jsonObject = new JSONObject(json);
            //kontrola http statusu
            int httpStatus = conn.getResponseCode();
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
            String headerLink = conn.getHeaderField("Link");

            Log.d("astro", "HEADER LINK: " + headerLink);

            if (null != headerLink) {
                String[] link = headerLink.split(";");
                if (link[1].matches("rel=\"next\"")) {
                    String nextUrl = link[0].substring(1, link[0].length() - 1);
                    getData(nextUrl, astroObjects);
                }
            }

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
            if (code == 401) {
                throw new AccessTokenExpiredException();
            } else {
                Log.d("astro", "getVersionError " + e.toString() + " " + e.getMessage());
                throw new ApiErrorException(e.getMessage(), e);
            }

        }  catch (Exception e) {

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

    private int getMessierVersion(ContentProviderClient providerClient) throws RemoteException {

        Uri uri = Uri.parse(AstroContract.CATALOG_URI + "/settings");
        int messier_version = 0;

        Cursor c = providerClient.query(uri, null, null, null, null);
        if (c.moveToFirst()) {
            do {
                if (c.getString(0).equals("messier_version")) {
                    messier_version = c.getInt(1);
                }
            } while (c.moveToNext());
        }

        return messier_version;
    }
}


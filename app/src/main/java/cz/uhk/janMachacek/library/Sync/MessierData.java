package cz.uhk.janMachacek.library.Sync;

import android.content.Context;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import cz.uhk.janMachacek.Config;
import cz.uhk.janMachacek.Exception.AccessTokenExpiredException;
import cz.uhk.janMachacek.Exception.ApiErrorException;
import cz.uhk.janMachacek.coordinates.Angle;
import cz.uhk.janMachacek.library.Api.Http.Response;
import cz.uhk.janMachacek.library.Api.Http.Utils;
import cz.uhk.janMachacek.library.Api.ApiAuthenticator;
import cz.uhk.janMachacek.library.AstroObject;
import cz.uhk.janMachacek.model.DataFacade;

/**
 * Created by jan on 1.8.2016.
 */
public class MessierData {

    private ApiAuthenticator apiAuthenticator;
    private HttpClient httpClient;
    private Context context;

    public MessierData(ApiAuthenticator apiAuthenticator, Context context) {
        this.apiAuthenticator = apiAuthenticator;
        this.httpClient = new DefaultHttpClient();
        this.context = context;
    }

    public MessierData(){

    }

    public void sync(String accessToken, Context context) throws AccessTokenExpiredException {

        Log.d("astro", "static sync start");

        String url = getUrl(accessToken);

        boolean next = true;
        boolean refreshToken = false;

        try {
//                if (refreshToken) {
//                    Log.d("Response", "pokus o refersh tokenu");
//                    apiAuthenticator.refreshAccessToken();
//                    url = getUrl();
//                    Log.d("Respone", "url = " + url);
//                }

            ArrayList<AstroObject> astroObjects = new ArrayList<AstroObject>();

            getData(url, astroObjects);

            DataFacade db = new DataFacade(context);
            db.stuffMessierData(astroObjects);
            Log.d("Response", "Size = " + Integer.toString(astroObjects.size()));

            next = false;
        } catch (ApiErrorException e) {
            e.printStackTrace();
            Log.d("Response", "1/ " + e.toString());
            next = false;
        }


    }

    public void sync() {


        String url = getUrl();

        boolean next = true;
        boolean refreshToken = false;
        do {
            try {
                if (refreshToken) {
                    Log.d("Response", "pokus o refersh tokenu");
                    apiAuthenticator.refreshAccessToken();
                    url = getUrl();
                    Log.d("Respone", "url = " + url);
                }

                ArrayList<AstroObject> astroObjects = new ArrayList<AstroObject>();

                getData(url, astroObjects);

                DataFacade db = new DataFacade(context);
                db.stuffMessierData(astroObjects);
                Log.d("Response", "Size = " + Integer.toString(astroObjects.size()));

                next = false;
            } catch (ApiErrorException e) {
                e.printStackTrace();
                Log.d("Response", "1/ " + e.toString() + " " + e.getMessage());
                next = false;
            } catch (AccessTokenExpiredException e) {

                Log.d("Response", "refersh token in loop" + e.toString());
                refreshToken = true;
                next = true;
            }
        } while (next == true);

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

    private String getUrl() {
        Log.d("Response", "AT = " + apiAuthenticator.getAccessToken());
        return Config.API_URL + Config.API_MESSIER_DATA + "?" + Config.API_ACCESS_TOKEN + "=" + apiAuthenticator.getAccessToken();
    }

    private static String getUrl(String accessToken) {
        return Config.API_URL + Config.API_MESSIER_DATA + "?" + Config.API_ACCESS_TOKEN + "=" + accessToken;
    }
}


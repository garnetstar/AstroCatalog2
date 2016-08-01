package cz.uhk.janMachacek.library.ApiHttp;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.message.BasicNameValuePair;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import cz.uhk.janMachacek.Config;

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
}

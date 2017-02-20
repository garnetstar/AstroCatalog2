package cz.uhk.janMachacek;

import android.net.Uri;

/**
 * @author Jan Macháček
 *         Created on 12.10.2016.
 */
public class AstroContract {

    public static final String CATALOG_AUTHORITY = "cz.uhk.janMachacek.astro.provider";
    public static final String DIARY_AUTHORITY = "cz.uhk.janMachacek.astro.diaryProvider";

    public static final Uri CATALOG_URI = Uri.parse("content://" + CATALOG_AUTHORITY);
    public static final Uri DIARY_URI =  Uri.parse("content://" + DIARY_AUTHORITY);

    public static final String weatherUri = "http://api.openweathermap.org/data/2.5/weather?units=metric";
    public static final String weatherApiKey = "edcc91c97986f06f830222a5aabf9084";

    public static final String API_CLIENT_ID = "171814397882-qoafbodpid52h7lh0pc98bruc9vv16vs.apps.googleusercontent.com";
}

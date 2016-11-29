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
}

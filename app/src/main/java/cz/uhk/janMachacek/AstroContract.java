package cz.uhk.janMachacek;

import android.net.Uri;

/**
 * @author Jan Macháček
 *         Created on 12.10.2016.
 */
public class AstroContract {

    /**
     * The authority of the lentitems provider.
     */
    public static final String AUTHORITY =
            "cz.uhk.janMachacek.astro";
    /**
     * The content URI for the top-level
     * lentitems authority.
     */
    public static final Uri CONTENT_URI =
            Uri.parse("content://" + AUTHORITY);
}

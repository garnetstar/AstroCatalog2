package cz.uhk.machacekgoogle.Model;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import cz.uhk.machacekgoogle.AstroContract;

/**
 * @author Jan Macháček
 *         Created on 3.10.2016.
 */
/*
 * Define an implementation of ContentProvider that stubs out
 * all methods
 */
public class CatalogContentProvider extends ContentProvider {

    private static final UriMatcher URI_MATCHER;
    private static final int MESSIER = 1;

    // prepare the UriMatcher
    static {
        URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        URI_MATCHER.addURI(AstroContract.CATALOG_AUTHORITY, "messier", MESSIER);
    }

    private AstroDbHelper dbHelper;

    /*
     * Always return true, indicating that the
     * provider loaded correctly.
     */
    @Override
    public boolean onCreate() {
        Context ctx = getContext();
        dbHelper = new AstroDbHelper(ctx);
        return true;
    }

    /*
     * Return no type for MIME type
     */
    @Override
    public String getType(Uri uri) {
        return null;
    }

    /*
     * query() always returns no results
     *
     */
    @Override
    public Cursor query(
            Uri uri,
            String[] projection,
            String selection,
            String[] selectionArgs,
            String sortOrder) {
        return null;
    }

    /*
     * insert() always returns null (no URI)
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int token = URI_MATCHER.match(uri);
        switch (token) {
            case MESSIER: {
                long id = db.insert(dbHelper.TABLE_OBJECT_NAME, null, values);
                Log.d("astro", "INSERT");
                Log.d("astro", values.toString());
                if (id != -1)
                    getContext().getContentResolver().notifyChange(uri, null);
                return AstroContract.CATALOG_URI.buildUpon().appendPath(String.valueOf(id)).build();
            }
            default: {
                throw new UnsupportedOperationException("URI: " + uri + " not supported.");
            }
        }
    }

    /*
     * delete() always returns "no rows affected" (0)
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsDeleted = -1;
        int token = URI_MATCHER.match(uri);

        Log.d("astro", "DDD " + uri);

        switch (token) {
            case MESSIER:
                rowsDeleted = db.delete(dbHelper.TABLE_OBJECT_NAME, selection, selectionArgs);
                Log.d("astro", "delete !!!!<<<???");
                break;
        }
        // Notifying the changes, if there are any
        if (rowsDeleted != -1)
            getContext().getContentResolver().notifyChange(uri, null);
        Log.d("astro", "delete OBSERVER ???");
        return rowsDeleted;
    }
//    content://cz.uhk.machacekgoogle.astro.provider/messier
    /*
     * update() always returns "no rows affected" (0)
     */
    public int update(
            Uri uri,
            ContentValues values,
            String selection,
            String[] selectionArgs) {

        switch (URI_MATCHER.match(uri)) {
            case MESSIER:
                Log.d("astro", "uri " + "messier");
                break;
        }
        Log.d("astro", "URI" + Integer.toString(URI_MATCHER.match(uri)));
        return 0;
    }
}
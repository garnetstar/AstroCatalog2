package cz.uhk.machacek.Model;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import cz.uhk.machacek.AstroContract;

/**
 * @author Jan Macháček
 *         Created on 3.10.2016.
 */
/*
 * Define an implementation of ContentProvider that stubs out
 * all methods
 */
public class DiaryContentProvider extends ContentProvider {

    private static final UriMatcher URI_MATCHER;
    private static final int DIARY = 1;
    private static final int DIARY_LIST = 2;
    private static final int SETTINGS = 3;
    private static final int DELETE_ALL = 4;

    // prepare the UriMatcher
    static {
        URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        URI_MATCHER.addURI(AstroContract.DIARY_AUTHORITY, "diary_edit", DIARY);
        URI_MATCHER.addURI(AstroContract.DIARY_AUTHORITY, "delete_all", DELETE_ALL);
        URI_MATCHER.addURI(AstroContract.DIARY_AUTHORITY, "settings", SETTINGS);
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

        int token = URI_MATCHER.match(uri);

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        switch (token) {
            case DIARY: {
                Cursor c = db.query(AstroDbHelper.TABLE_DIARY_NAME, projection, selection, selectionArgs, null, null, AstroDbHelper.KEY_DIARY_FROM + " " + sortOrder);
                return c;
            }
            case SETTINGS: {
                String[] columns = new String[]{AstroDbHelper.KEY_SETTINGS_KEY, AstroDbHelper.KEY_SETTINGS_VALUE};
                Cursor c = db.query(AstroDbHelper.TABLE_SETTINGS_NAME, columns, null, null, null, null, null);
                return c;
            }
            default: {
                throw new UnsupportedOperationException("URI: " + uri + " not supported. diary_list !!!" + Integer.toString(token));
            }
        }
    }

    /*
     * insert() always returns null (no URI)
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int token = URI_MATCHER.match(uri);

        switch (token) {
            case DIARY: {
                long id = db.insertOrThrow(dbHelper.TABLE_DIARY_NAME, null, values);
                if (id != -1)
                    getContext().getContentResolver().notifyChange(uri, null);
                return AstroContract.CATALOG_URI.buildUpon().appendPath(String.valueOf(id)).build();
            }
            default: {
                throw new UnsupportedOperationException("URI: " + uri + " not supported. diary_edit !!!" + Integer.toString(token));
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

        Log.d("astro", "Diary delete all " + uri);

        switch (token) {
            case DELETE_ALL:
                db.delete(dbHelper.TABLE_OBJECT_NAME, selection, selectionArgs);
                db.delete(dbHelper.TABLE_SETTINGS_NAME, selection, selectionArgs);
                db.delete(dbHelper.TABLE_DIARY_NAME, selection, selectionArgs);
                Log.d("astro", "Diary delete all " + uri);
                break;
        }
        return 1;
    }

    /*
     * update() always returns "no rows affected" (0)
     */
    public int update(
            Uri uri,
            ContentValues values,
            String where,
            String[] whereArgs) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int token = URI_MATCHER.match(uri);

        switch (token) {
            case DIARY: {
                return db.update(AstroDbHelper.TABLE_DIARY_NAME, values, where, whereArgs);
            }
            case SETTINGS: {
                return  db.update(AstroDbHelper.TABLE_SETTINGS_NAME,values,where,whereArgs);
            }
            default: {
                throw new UnsupportedOperationException("URI: " + uri + " not supported. diary_update !!!" + Integer.toString(token));
            }
        }
    }

}
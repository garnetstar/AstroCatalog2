package cz.uhk.janMachacek.Model;

import android.content.ContentProviderClient;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;

import java.util.ArrayList;

import cz.uhk.janMachacek.AstroContract;
import cz.uhk.janMachacek.coordinates.Angle;

/**
 * @author Jan Macháček
 *         Created on 29.11.2016.
 */
public class DiaryFacade {

    private ContentProviderClient providerClient;

    public DiaryFacade(ContentProviderClient providerClient) {
        this.providerClient = providerClient;
    }


    public DiaryObject getOneById(int id) throws RemoteException {

        String selection = AstroDbHelper.KEY_DIARY_ID + "=?";
        String[] selectionArgs = {Integer.toString(id)};

        return getOne(selection, selectionArgs);
    }

    public DiaryObject getOneByGuid(String guid) throws RemoteException {

        String selection = AstroDbHelper.KEY_DIARY_GUID + "=?";
        String[] selectionArgs = {guid};

        return getOne(selection, selectionArgs);
    }

    private DiaryObject getOne(String selection, String[] selectionArgs) throws RemoteException {

        Cursor c = providerClient.query(getUri(), getProjection(), selection, selectionArgs, "DESC");

        if (c.moveToFirst()) {

            DiaryObject object = createObjectFromCursor(c);

            return object;
        }
        return null;
    }

    public ArrayList<DiaryObject> getObjectForSync() throws RemoteException {

        ArrayList<DiaryObject> list = new ArrayList<DiaryObject>();

        String selection = AstroDbHelper.KEY_DIARY_SYNC_OK + "=?";
        String[] selectionArgs = {Integer.toString(0)};
        Cursor cursor = providerClient.query(getUri(),getProjection(),selection,selectionArgs, "ASC");

        if (cursor.moveToFirst()) {
            do {
                DiaryObject o = createObjectFromCursor(cursor);
                Log.d("astro", "TOServer " + o.toString());
                list.add(o);
            } while (cursor.moveToNext());
        }

        return list;
    }

    private String[] getProjection() {
        String[] projection = new String[]{
                AstroDbHelper.KEY_DIARY_ID,
                AstroDbHelper.KEY_DIARY_GUID,
                AstroDbHelper.KEY_DIARY_FROM,
                AstroDbHelper.KEY_DIARY_TO,
                AstroDbHelper.KEY_DIARY_LAT,
                AstroDbHelper.KEY_DIARY_LON,
                AstroDbHelper.KEY_DIARY_SYNC_OK,
                AstroDbHelper.KEY_DIARY_DELETED,
                AstroDbHelper.KEY_DIARY_ROW_COUNTER,
                AstroDbHelper.KEY_DIARY_TIMESTAMP
        };

        return projection;
    }

    private DiaryObject createObjectFromCursor(Cursor c) {
        DiaryObject o = new DiaryObject();

        o.setId(c.getInt(0));
        o.setGuid(c.getString(1));
        o.setFrom(c.getString(2));
        o.setTo(c.getString(3));
        o.setLatitude(new Angle(c.getDouble(4)));
        o.setLognitude(new Angle(c.getDouble(5)));
        o.setSyncOk(c.getInt(6));
        o.setDeleted(c.getInt(7));
        o.setRowCounter(c.getInt(8));
        o.setTimestamp(c.getString(9));
        return o;
    }


    private Uri getUri() {
        return Uri.parse(AstroContract.DIARY_URI + "/diary_edit");
    }
}

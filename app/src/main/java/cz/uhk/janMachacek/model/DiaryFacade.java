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

        String[] projection = new String[]{
                AstroDbHelper.KEY_DIARY_ID,
                AstroDbHelper.KEY_DIARY_GUID,
                AstroDbHelper.KEY_DIARY_FROM,
                AstroDbHelper.KEY_DIARY_TO,
                AstroDbHelper.KEY_DIARY_SYNC_OK,
                AstroDbHelper.KEY_DIARY_LAT,
                AstroDbHelper.KEY_DIARY_LON
        };

        Cursor c = providerClient.query(getUri(), projection, selection, selectionArgs, "DESC");

        if (c.moveToFirst()) {
            DiaryObject object = new DiaryObject();
            object.setId(c.getInt(0));
            object.setGuid(c.getString(1));
            object.setFrom(c.getString(2));
            object.setTo(c.getString(3));
            object.setSyncOk(c.getInt(4));
            object.setLatitude(new Angle(c.getDouble(5)));
            object.setLognitude(new Angle(c.getDouble(6)));

            return object;
        }
        return null;
    }


    private Uri getUri() {
        return Uri.parse(AstroContract.DIARY_URI + "/diary_edit");
    }
}

package cz.uhk.machacek.Model;

import java.util.ArrayList;
import java.util.Set;

import cz.uhk.machacek.coordinates.Angle;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Fasáda pro přístup k databázi
 *
 * @author Jan Macháček
 */
public class ObjectDataFacade {

    private AstroDbHelper helper;

    public ObjectDataFacade(Context context, AssetManager assetManager) {
        super();
        helper = new AstroDbHelper(context, assetManager);
    }

    public ObjectDataFacade(Context context) {
        super();
        helper = new AstroDbHelper(context);
    }

    public Cursor getAll(Set<String> whereType, String whereMagnitude) {

        String wheres[] = new String[2];
        wheres[0] = "(" + createWhereByType(whereType) + ")";
        wheres[1] = "(" + AstroDbHelper.KEY_OBJECT_MAG + " < " + whereMagnitude
                + ")";

        String where = implode(" AND ", wheres);

        String[] columns = new String[]{AstroDbHelper.KEY_OBJECT_ID,
                AstroDbHelper.KEY_OBJECT_NAME, AstroDbHelper.KEY_OBJECT_RA,
                AstroDbHelper.KEY_OBJECT_DEC,
                AstroDbHelper.KEY_OBJECT_CONSTELLATION,
                AstroDbHelper.KEY_OBJECT_TYPE, AstroDbHelper.KEY_OBJECT_MAG};

        Cursor cursor = getDb().query(AstroDbHelper.TABLE_OBJECT_NAME, columns,
                where, null, null, null, null, null);

        return cursor;

    }

    public AstroObject getOneObject(int id) {
        String[] columns = new String[]{AstroDbHelper.KEY_OBJECT_ID,
                AstroDbHelper.KEY_OBJECT_NAME, AstroDbHelper.KEY_OBJECT_RA,
                AstroDbHelper.KEY_OBJECT_DEC,
                AstroDbHelper.KEY_OBJECT_CONSTELLATION,
                AstroDbHelper.KEY_OBJECT_TYPE, AstroDbHelper.KEY_OBJECT_MAG,
                AstroDbHelper.KEY_OBJECT_DIST};

        String where = AstroDbHelper.KEY_OBJECT_ID + " = "
                + Integer.toString(id);

        Cursor cursor = getDb().query(AstroDbHelper.TABLE_OBJECT_NAME, columns,
                where, null, null, null, null, null);
        try {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                AstroObject object = new AstroObject();
                object.setId(cursor.getInt(0));
                object.setName(cursor.getString(1));
                object.setRightAscension(new Angle(cursor.getDouble(2)));
                object.setDeclination(new Angle(cursor.getDouble(3)));
                object.setConstellation(cursor.getString(4));
                object.setType(cursor.getInt(5));
                object.setMagnitude(cursor.getDouble(6));
                object.setDistance(cursor.getDouble(7));
                return object;
            } else
                return null;
        } finally {
            getDb().close();
        }
    }

    public void stuffMessierData(ArrayList<AstroObject> astroObjects) {

        getDb().beginTransaction();
        try {

            getDb().execSQL("delete from " + AstroDbHelper.TABLE_OBJECT_NAME);

            for (AstroObject astroObject : astroObjects) {
                ContentValues cv = new ContentValues(8);
                cv.put(AstroDbHelper.KEY_OBJECT_NAME, astroObject.getName());
                cv.put(AstroDbHelper.KEY_OBJECT_MAG, astroObject.getMagnitude());
                cv.put(AstroDbHelper.KEY_OBJECT_RA, astroObject.getRightAscension().getDecimalDegree());
                cv.put(AstroDbHelper.KEY_OBJECT_DEC, astroObject.getDeclination().getDecimalDegree());
                cv.put(AstroDbHelper.KEY_OBJECT_TYPE, astroObject.getType());
                cv.put(AstroDbHelper.KEY_OBJECT_CONSTELLATION, astroObject.getConstellation());
                cv.put(AstroDbHelper.KEY_OBJECT_DIST, astroObject.getDistance());
                getDb().insert(AstroDbHelper.TABLE_OBJECT_NAME, null, cv);
            }

            getDb().setTransactionSuccessful();
            getDb().endTransaction();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() {
        getDb().close();
    }

    private SQLiteDatabase getDb() {
        return helper.getReadableDatabase();
    }

    private String createWhereByType(Set<String> settingsType) {
        String[] pieces = new String[settingsType.size()];
        int key = 0;
        for (String piece : settingsType) {
            pieces[key++] = AstroDbHelper.KEY_OBJECT_TYPE + "=" + piece;
        }

        String value = implode(" OR ", pieces);

        return value;
    }

    public String implode(String glue, String[] strArray) {
        String ret = "";
        for (int i = 0; i < strArray.length; i++) {
            ret += (i == strArray.length - 1) ? strArray[i] : strArray[i]
                    + glue;
        }
        return ret;
    }

}

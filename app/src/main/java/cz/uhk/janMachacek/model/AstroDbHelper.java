package cz.uhk.janMachacek.Model;

import java.io.IOException;
import java.util.ArrayList;

import cz.uhk.janMachacek.library.AssetParser;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

/**
 * Třída pro vytvoření databuáze a naplnění defaultními daty
 *
 * @author Jan Macháček
 */
public class AstroDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "AstroCatalog.db";
    public static final String DATABASE_FILE_PATH = Environment
            .getExternalStorageState();
    public static final int VERSION = 4;

    public static final String TABLE_OBJECT_NAME = "astroObject";
    public static final String KEY_OBJECT_ID = "_id";
    public static final String KEY_OBJECT_NAME = "name";
    public static final String KEY_OBJECT_CONSTELLATION = "constellation";
    public static final String KEY_OBJECT_TYPE = "type";
    public static final String KEY_OBJECT_DEC = "declination";
    public static final String KEY_OBJECT_RA = "rightAscension";
    public static final String KEY_OBJECT_MAG = "magnitude";
    public static final String KEY_OBJECT_DIST = "distance";

    public static final String TABLE_DIARY_NAME = "diary";
    public static final String KEY_DIARY_ID = "_id";
    public static final String KEY_DIARY_GUID = "guid";
    public static final String KEY_DIARY_USER_ID = "userId";
    public static final String KEY_DIARY_FROM = "timeFrom";
    public static final String KEY_DIARY_TO = "timeTo";
    public static final String KEY_DIARY_LAT = "latitude";
    public static final String KEY_DIARY_LON = "longitude";
    public static final String KEY_DIARY_SYNC_OK = "diarySyncOk";

    public static final String TABLE_SETTINGS_NAME = "settings";
    public static final String KEY_SETTINGS_KEY = "key";
    public static final String KEY_SETTINGS_VALUE = "value";

    public static final String CREATE_TABLE_OBJECT = "create table "
            + TABLE_OBJECT_NAME + "(" + KEY_OBJECT_ID
            + " integer primary key autoincrement, " + KEY_OBJECT_NAME
            + " text not null, " + KEY_OBJECT_CONSTELLATION
            + " text not null, " + KEY_OBJECT_TYPE + " int not null, "
            + KEY_OBJECT_DEC + " decimal not null, " + KEY_OBJECT_RA
            + " decimal not null, " + KEY_OBJECT_DIST + " decimal not null, "
            + KEY_OBJECT_MAG + " decimal)";

    public static final String CREATE_TABLE_DIARY = "create table "
            + TABLE_DIARY_NAME + "(" +
            KEY_DIARY_ID + " integer primary key autoincrement, " +
            KEY_DIARY_GUID + " text unique, " +
            KEY_DIARY_FROM + " text not null, " +
            KEY_DIARY_TO + " text not null, " +
            KEY_DIARY_LAT + " decimal, " +
            KEY_DIARY_LON + " decimal," +
            KEY_DIARY_SYNC_OK + " integer not null)";

    public static final String CREATE_TABLE_SETTINGS = "create table "
            + TABLE_SETTINGS_NAME + "(" +
            KEY_SETTINGS_KEY + " text not null, " +
            KEY_SETTINGS_VALUE + " integer not null)";

    private AssetManager assetManager;

    public AstroDbHelper(Context context, AssetManager manager) {
        super(context, DATABASE_NAME, null, VERSION);

        assetManager = manager;
    }

    public AstroDbHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //	try {
        db.execSQL(CREATE_TABLE_OBJECT);
        db.execSQL(CREATE_TABLE_DIARY);
        db.execSQL(CREATE_TABLE_SETTINGS);

        // naplnění tabulky settings
        ContentValues cv = new ContentValues(2);
        cv.put(KEY_SETTINGS_KEY, "client_counter");
        cv.put(KEY_SETTINGS_VALUE, 0);
        db.insert(TABLE_SETTINGS_NAME, null, cv);

//			for (AstroObject astroObject : getObjectData()) {
//				ContentValues cv = new ContentValues(8);
//				cv.put(KEY_OBJECT_NAME, astroObject.getName());
//				cv.put(KEY_OBJECT_MAG, astroObject.getMagnitude());
//				cv.put(KEY_OBJECT_RA, astroObject.getLognitude().getDecimalDegree());
//				cv.put(KEY_OBJECT_DEC, astroObject.getLatitude().getDecimalDegree());
//				cv.put(KEY_OBJECT_TYPE, astroObject.getType());
//				cv.put(KEY_OBJECT_CONSTELLATION, astroObject.getConstellation());
//				cv.put(KEY_OBJECT_DIST, astroObject.getDistance());
//				db.insert(TABLE_OBJECT_NAME, null, cv);
//			}

        //} catch (IOException e) {
        //e.printStackTrace();
        //}
    }


    private ArrayList<AstroObject> getObjectData() throws IOException {
        AssetParser parser = new AssetParser(assetManager);
        ArrayList<AstroObject> list = parser.parseMessierData();
        return list;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + TABLE_OBJECT_NAME);
        onCreate(db);
    }


}

package cz.uhk.janMachacek.model;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import cz.uhk.janMachacek.library.AssetParser;
import cz.uhk.janMachacek.library.AstroObject;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

/**
 * Tøída pro vytvoøení databáze a naplnìní defaultními daty
 * 
 * @author Jan Macháèek
 *
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

	public static final String CREATE_TABLE_OBJECT = "create table "
			+ TABLE_OBJECT_NAME + "(" + KEY_OBJECT_ID
			+ " integer primary key autoincrement, " + KEY_OBJECT_NAME
			+ " text not null, " + KEY_OBJECT_CONSTELLATION
			+ " text not null, " + KEY_OBJECT_TYPE + " int not null, "
			+ KEY_OBJECT_DEC + " decimal not null, " + KEY_OBJECT_RA
			+ " decimal not null, " + KEY_OBJECT_DIST + " decimal not null, "
			+ KEY_OBJECT_MAG + " decimal)";

	private AssetManager assetManager;

	public AstroDbHelper(Context context, AssetManager manager) {
		super(context, DATABASE_NAME, null, VERSION);
		
		assetManager = manager;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		try {
			db.execSQL(CREATE_TABLE_OBJECT);

			for (AstroObject astroObject : getObjectData()) {
				ContentValues cv = new ContentValues(8);
				cv.put(KEY_OBJECT_NAME, astroObject.getName());
				cv.put(KEY_OBJECT_MAG, astroObject.getMagnitude());
				cv.put(KEY_OBJECT_RA, astroObject.getRightAscension().getDecimalDegree());
				cv.put(KEY_OBJECT_DEC, astroObject.getDeclination().getDecimalDegree());
				cv.put(KEY_OBJECT_TYPE, astroObject.getType());
				cv.put(KEY_OBJECT_CONSTELLATION, astroObject.getConstellation());
				cv.put(KEY_OBJECT_DIST, astroObject.getDistance());
				db.insert(TABLE_OBJECT_NAME, null, cv);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
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

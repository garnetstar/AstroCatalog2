package cz.uhk.janMachacek;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Set;

import cz.uhk.janMachacek.UI.AstroObjectAdapter;
import cz.uhk.janMachacek.coordinates.Angle;
import cz.uhk.janMachacek.coordinates.Coordinates;
import cz.uhk.janMachacek.coordinates.Timer;
import cz.uhk.janMachacek.Model.AstroObject;
import cz.uhk.janMachacek.Model.AstroDbHelper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

/**
 * Activita pro zobrazení seznamu objektů
 *
 * @author Jan Macháček
 */
public class ObjectListActivity extends AbstactBaseActivity implements
        OnSharedPreferenceChangeListener {

    private AstroObjectAdapter adapter;
    public final static String REFRESH_OBJECTS_LIST = "ObjectListActivity.refreshObject";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // pristup k preferencim
        preference.registerOnSharedPreferenceChangeListener(this);

        setContentView(R.layout.astro_list);
    }


    @Override
    protected void onStart() {
        super.onStart();

        // registrace broadcast receiveru pro reload objektů
        registerReceiver(new RefreshBroudcastReceiver(), new IntentFilter(REFRESH_OBJECTS_LIST));
    }

    @Override
    protected void onResume() {
        super.onResume();
        createObjects();
    }

    private void createObjects() {

        String filtering = preference.getString("setting_filtering", null);

        if (filtering.equals("1")) {
            Cursor cursor = getCursorByPreferences();

            ArrayList<AstroObject> astroObjects = new ArrayList<AstroObject>();

            // TODO Android 2 str.217
            if (cursor.moveToFirst()) {
                do {
                    AstroObject object = createObjectFromCursor(cursor);
                    astroObjects.add(object);
                } while (cursor.moveToNext());
            }

            facade.close();

            renderObjects(astroObjects);

        } else {
            findLocation();
        }
    }

    private Cursor getCursorByPreferences() {
        Set<String> s = preference.getStringSet("setting_object_types", null);

        Integer maxMagnitude = preference.getInt("setting_max_magnitude", 0);

        Cursor cursor = facade.getAll(s, Integer.toString(maxMagnitude));

        return cursor;

    }

    private void renderObjects(ArrayList<AstroObject> objects) {
        ListView astroObjectList = (ListView) findViewById(R.id.listAstroObjects);

        adapter = new AstroObjectAdapter(this, objects);

        astroObjectList.setAdapter(adapter);
        astroObjectList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                AstroObject item = adapter.getItem(position);
                Intent intent = new Intent(getApplicationContext(),
                        ObjectDetatilActivity.class);
                intent.putExtra(AstroDbHelper.KEY_OBJECT_ID, item.getId());
                startActivityForResult(intent, 0);
            }
        });
    }

    private AstroObject createObjectFromCursor(Cursor cursor) {
        AstroObject object = new AstroObject();
        object.setId(cursor.getInt(0));
        object.setName(cursor.getString(1));
        object.setRightAscension(new Angle(cursor.getDouble(2)));
        object.setDeclination(new Angle(cursor.getDouble(3)));
        object.setConstellation(cursor.getString(4));
        object.setType(cursor.getInt(5));
        object.setMagnitude(cursor.getDouble(6));
        return object;
    }

    @Override
    public void onLocationChanged(Location location) {

        Calendar actualUT0 = Timer.getActualUTC();
        Angle latitude = new Angle(location.getLatitude());
        Angle longitude = new Angle(location.getLongitude());

        hideProgressDialog();
        showLocation(latitude, longitude);

        Cursor cursor = getCursorByPreferences();

        ArrayList<AstroObject> astroObjects = new ArrayList<AstroObject>();

        if (cursor.moveToFirst()) {
            do {
                Angle rightAscension = new Angle(cursor.getDouble(2));
                Angle declination = new Angle(cursor.getDouble(3));
                Angle hourAngle = Coordinates.getHourAngle(actualUT0,
                        longitude, rightAscension);
                Angle altitude = Coordinates.getAltitude(hourAngle,
                        declination, latitude);
                if (altitude.getDecimalDegree() < 0)
                    continue;

                AstroObject object = createObjectFromCursor(cursor);
                astroObjects.add(object);
            } while (cursor.moveToNext());
        }

        facade.close();

        renderObjects(astroObjects);

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        createObjects();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent();
        intent.setClass(ObjectListActivity.this, SettingsActivity.class);
        startActivityForResult(intent, 0);

        return true;
    }


    private class RefreshBroudcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            createObjects();
        }
    }




}

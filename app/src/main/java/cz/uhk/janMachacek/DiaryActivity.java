package cz.uhk.janMachacek;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import cz.uhk.janMachacek.Model.AstroDbHelper;
import cz.uhk.janMachacek.Model.DiaryObject;
import cz.uhk.janMachacek.UI.DiaryObjectAdapter;
import cz.uhk.janMachacek.coordinates.Angle;

/**
 * @author Jan Macháček
 *         Created on 26.11.2016.
 */
public class DiaryActivity extends AbstactBaseActivity {

    public final static String REFRESH_DIARY_LIST = "DiaryActivity.refreshDiary";

    private DiaryObjectAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.diary);
    }

    /**
     * registrace broadcast receiveru pro zobrazování message z jiných threadů
     */
    @Override
    protected void onStart() {
        super.onStart();
        // registrace broadcast receiveru pro reload objektů
        registerReceiver(new RefreshBroudcastReceiver(), new IntentFilter(REFRESH_DIARY_LIST));
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    public void startAddDiary(View view) {
        Intent intent = new Intent(this, DiaryEditActivity.class);
        startActivity(intent);


    }

    /**
     * This is the fragment-orientated version of {@link #onResume()} that you
     * can override to perform operations in the Activity at the same point
     * where its fragments are resumed.  Be sure to always call through to
     * the super-class.
     */
    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();

        ArrayList<DiaryObject> objects = new ArrayList<DiaryObject>();

        Uri uri = Uri.parse(AstroContract.DIARY_URI + "/diary_edit");

        String[] projection = new String[]{
                AstroDbHelper.KEY_DIARY_ID,
                AstroDbHelper.KEY_DIARY_FROM,
                AstroDbHelper.KEY_DIARY_TO,
                AstroDbHelper.KEY_DIARY_LAT,
                AstroDbHelper.KEY_DIARY_LON,
                AstroDbHelper.KEY_DIARY_GUID,
                AstroDbHelper.KEY_DIARY_SYNC_OK,
                AstroDbHelper.KEY_DIARY_WEATHER,
                AstroDbHelper.KEY_DIARY_LOG};

        String selection = AstroDbHelper.KEY_DIARY_DELETED + "<>?";
        String[] selectionArgs = {"1"};


        Cursor c = getContentResolver().query(uri, projection, selection, selectionArgs, " DESC");

        if (c.moveToFirst()) {
            do {
                DiaryObject o = createObjectFromCursor(c);
                objects.add(o);
            } while (c.moveToNext());
        }

        renderObjects(objects);
    }

    private void renderObjects(ArrayList<DiaryObject> objects) {

        ListView diaryList = (ListView) findViewById(R.id.listDiary);
        adapter = new DiaryObjectAdapter(this, objects);

        diaryList.setAdapter(adapter);

        diaryList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                DiaryObject item = adapter.getItem(position);
                Intent intent = new Intent(getApplicationContext(),
                        DiaryEditActivity.class);
                intent.putExtra(AstroDbHelper.KEY_DIARY_ID, item.getId());
                startActivityForResult(intent, 0);
            }
        });
    }

    private DiaryObject createObjectFromCursor(Cursor c) {
        DiaryObject o = new DiaryObject();

        o.setId(c.getInt(0));
        o.setFrom(c.getString(1));
        o.setTo(c.getString(2));
        o.setLatitude(new Angle(c.getDouble(3)));
        o.setLongitude(new Angle(c.getDouble(4)));
        o.setGuid(c.getString(5));
        o.setSyncOk(c.getInt(6));
        o.setWeather(c.getString(7));
        o.setLog(c.getString(8));
        return o;
    }

    /**
     * Take care of popping the fragment back stack or finishing the activity
     * as appropriate.
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        back();
    }

    private void back() {
        //navrat na vypis objektu
        Intent intent = new Intent(this, HomePage.class);
        startActivity(intent);
    }

    private class RefreshBroudcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            onResumeFragments();
        }
    }
}

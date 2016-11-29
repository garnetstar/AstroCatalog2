package cz.uhk.janMachacek;

import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;

import cz.uhk.janMachacek.Model.AstroDbHelper;
import cz.uhk.janMachacek.Model.AstroObject;
import cz.uhk.janMachacek.Model.DiaryObject;
import cz.uhk.janMachacek.UI.DiaryObjectAdapter;
import cz.uhk.janMachacek.coordinates.Angle;
import cz.uhk.janMachacek.coordinates.Coordinates;
import cz.uhk.janMachacek.coordinates.Utils;

/**
 * @author Jan Macháček
 *         Created on 26.11.2016.
 */
public class DiaryActivity extends AbstactBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.diary);
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
                AstroDbHelper.KEY_DIARY_GUID};

        String selection = null;
        String[] selectionArgs = null;

        Cursor c = getContentResolver().query(uri, projection, selection, selectionArgs, "DESC");

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
        DiaryObjectAdapter adapter = new DiaryObjectAdapter(this, objects);

        diaryList.setAdapter(adapter);



    }

    private DiaryObject createObjectFromCursor(Cursor c) {
        DiaryObject o = new DiaryObject();

        o.setFrom(c.getString(1));
        o.setTo(c.getString(2));
        o.setLatitude(new Angle(c.getDouble(3)));
        o.setLognitude(new Angle(c.getDouble(4)));
        o.setGuid(c.getString(5));
        return o;
    }
}

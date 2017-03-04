package cz.uhk.machacek;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

/**
 * @author Jan Macháček
 *         Created on 13.10.2016.
 */
public class HomePage extends AbstactBaseActivity  {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    public void startCatalog(View view) {
        Intent intent = new Intent(this, ObjectListActivity.class);
        startActivity(intent);
    }

    public void startSettings(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void startDiary(View view) {
        Intent intent = new Intent(this, DiaryActivity.class);
        startActivity(intent);
    }


}

package cz.uhk.janMachacek;


import cz.uhk.janMachacek.coordinates.Angle;
import cz.uhk.janMachacek.coordinates.Utils;
import cz.uhk.janMachacek.model.DataFacade;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Abstraktn? activity implementuj?c? spole?n? metody
 *
 * @author Jan Mach??ek
 *
 */
abstract public class AbstactBaseActivity extends FragmentActivity implements
		LocationListener {

	protected LocationManager locationManager;
	protected DataFacade facade;
	protected SharedPreferences preference;
	protected String locationProviderName = "none";

	protected ProgressDialog progresDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// nastaven? defaultn?ch settings
		PreferenceManager
				.setDefaultValues(this, R.xml.preference_screen, false);
		// pristup k preferencim
		preference = PreferenceManager.getDefaultSharedPreferences(this);

		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

		facade = new DataFacade(this, getAssets());


	}

	protected void showProgressDialog(String title, String message) {
		this.progresDialog = ProgressDialog.show(this, title, message, true);
	}

	protected void hideProgressDialog() {
		try {
			this.progresDialog.dismiss();
		} catch (Exception e) {
			Toast.makeText(getBaseContext(),
					"ERROR: " + e.getMessage(),
					Toast.LENGTH_LONG).show();
		}
	}

	protected void findLocation() {
		try {
			showProgressDialog("", "Ur?ov?n? aktu?ln? polohy");
			Criteria c = new Criteria();
			c.setAccuracy(Criteria.ACCURACY_COARSE);
			c.setPowerRequirement(Criteria.POWER_LOW);
			locationProviderName = locationManager.getBestProvider(c, true);
			locationManager.requestSingleUpdate(c, this, null);
		} catch (Exception e) {
			hideProgressDialog();
			Toast.makeText(getBaseContext(),
					"ERROR: " + e.getMessage() + e.getLocalizedMessage(),
					Toast.LENGTH_LONG).show();
		}
	}

	@Override
	abstract public void onLocationChanged(Location location);

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		return;

	}

	@Override
	public void onProviderEnabled(String provider) {
		return;

	}

	@Override
	public void onProviderDisabled(String provider) {
		Toast.makeText(getBaseContext(),
				"ERROR: provider " + provider + " is disabled!",
				Toast.LENGTH_LONG).show();
		return;
	}

	protected void showLocation(Angle latitude, Angle longitude) {
		TextView location = (TextView) findViewById(R.id.actual_location);
		location.setText("Lat: " + Utils.getFormatedDegree(latitude) + " Lon: "
				+ Utils.getFormatedDegree(longitude));
		TextView locatinoProvider = (TextView) findViewById(R.id.actual_location_provider);
		locatinoProvider.setText(locationProviderName);
	}

	@Override
	protected void onStop() {
		super.onStop();
		locationManager.removeUpdates(this);
	}
}

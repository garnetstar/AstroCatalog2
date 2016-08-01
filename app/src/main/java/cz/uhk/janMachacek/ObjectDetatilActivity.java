package cz.uhk.janMachacek;

import java.util.Calendar;

import cz.uhk.janMachacek.coordinates.Angle;
import cz.uhk.janMachacek.coordinates.Coordinates;
import cz.uhk.janMachacek.coordinates.Timer;
import cz.uhk.janMachacek.coordinates.Utils;
import cz.uhk.janMachacek.library.AstroObject;
import cz.uhk.janMachacek.library.ResourceHelper;
import cz.uhk.janMachacek.model.AstroDbHelper;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Handler;
import android.content.Intent;
import android.util.SparseArray;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Activity pro zobrazení detailu objektu
 * 
 * @author Jan Macháèek
 *
 */
public class ObjectDetatilActivity extends AbstactBaseActivity implements
		LocationListener {

	public static final String KEY_POINTER = "key_to_pointer";
	public static final String AZIMUTH = "azimuth_to_pointer";
	public static final String ALTITUDE = "altitude_to_pointer";

	private AstroObject object;

	private Location location;
	private Angle azimuth, altitude;

	/**
	 * atribut urèující zda je objekt viditelný
	 */
	private boolean isVisible = true;

	private Handler handler = new Handler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_object_detatil);

		Intent intent = getIntent();

		if (null != intent) {
			int id = intent.getIntExtra(AstroDbHelper.KEY_OBJECT_ID, 0);
			object = facade.getOneObject(id);
			findLocation();
		}

		handler.postDelayed(new Runnable() {

			@Override
			public void run() {
				showDetail();
				handler.post(this);
			}
		}, 1000);
	}

	private void showDetail() {
		TextView nameView = (TextView) findViewById(R.id.detail_object_name);
		TextView rightAscensionView = (TextView) findViewById(R.id.detail_object_right_ascension);
		TextView declinationView = (TextView) findViewById(R.id.detail_object_declination);
		TextView magnitudeView = (TextView) findViewById(R.id.detail_object_magnitude);
		TextView distanceView = (TextView) findViewById(R.id.detail_object_distance);
		TextView typeView = (TextView) findViewById(R.id.detail_object_type);
		TextView constellationView = (TextView) findViewById(R.id.detail_constellation);

		nameView.setText(object.getName());
		rightAscensionView.setText(Utils.getFormatedHour(object
				.getRightAscension()));
		declinationView
				.setText(Utils.getFormatedDegree(object.getDeclination()));
		magnitudeView.setText(Double.toString(object.getMagnitude()));
		distanceView.setText(Double.toString(object.getDistance()));
		SparseArray<String> typeMap = ResourceHelper.getObjectTypes(this);
		typeView.setText(typeMap.get(object.getType()));
		constellationView.setText(object.getConstellation());

		if (null != location) {
			Angle latitude = new Angle(location.getLatitude());
			Angle longitude = new Angle(location.getLongitude());

			Calendar actualUT0 = Timer.getActualUTC();
			Angle hourAngle = Coordinates.getHourAngle(actualUT0, longitude,
					object.getRightAscension());
			altitude = Coordinates.getAltitude(hourAngle,
					object.getDeclination(), latitude);
			azimuth = Coordinates.getAzimuth(latitude,
					object.getDeclination(), altitude, hourAngle);

			TextView hourAngleView = (TextView) findViewById(R.id.detail_object_hour_angle);
			TextView altitudeView = (TextView) findViewById(R.id.detail_object_altitude);
			TextView azimuthView = (TextView) findViewById(R.id.detail_object_azimuth);

			if (altitude.getDecimalDegree() <= 0) {
				this.isVisible = false;
			}

			if (this.isVisible == false) {

				int aletrColor = getResources().getColor(R.color.alert);

				altitudeView.setTextColor(aletrColor);
				hourAngleView.setTextColor(aletrColor);
				azimuthView.setTextColor(aletrColor);

			} else {
				LinearLayout pointerButtonView = (LinearLayout) findViewById(R.id.pointer_button_view);
				Button pointerButton = (Button) findViewById(R.id.detail_object_pointer_button);
				pointerButtonView.setVisibility(View.VISIBLE);

				pointerButton.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						Intent intent = new Intent(getBaseContext(),
								PointerActivity.class);
						intent.putExtra(KEY_POINTER, object.getId());
						intent.putExtra(AZIMUTH, azimuth.getDecimalDegree());
						intent.putExtra(ALTITUDE, altitude.getDecimalDegree());
						startActivityForResult(intent, 0);
					}
				});
			}

			hourAngleView.setText(Utils.getFormatedHour(hourAngle));
			altitudeView.setText(Utils.getFormatedDegree(altitude));
			azimuthView.setText(Utils.getFormatedDegree(azimuth));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		getMenuInflater().inflate(R.menu.object_detatil, menu);
		return true;
	}

	@Override
	public void onLocationChanged(Location location) {
		hideProgressDialog();
		this.location = location;
	}

	private Runnable periodTimer = new Runnable() {

		@Override
		public void run() {
			showDetail();
			handler.postDelayed(this, 1000);
		}
	};

}

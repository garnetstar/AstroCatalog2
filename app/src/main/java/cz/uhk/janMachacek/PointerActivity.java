package cz.uhk.janMachacek;

import java.util.List;

import cz.uhk.janMachacek.UI.CompasView;
import cz.uhk.janMachacek.UI.HorizontView;
import cz.uhk.janMachacek.library.AstroObject;
import cz.uhk.janMachacek.library.ResourceHelper;
import cz.uhk.janMachacek.model.DataFacade;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.SparseArray;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activita pro zobrazení prvkù pro navigaci na objekt
 * 
 * @author Jan Macháèek
 *
 */
public class PointerActivity extends Activity implements 
		SensorEventListener {

	private AstroObject object;

	private SensorManager sensorManager;

	private float[] accelVals = new float[3];
	private float[] magVals = new float[3];

	private double azimuth, altitude;

	private CompasView compassView;
	private HorizontView horizontView;
	
	private DataFacade facade;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);

		checkMagneticFieldSupported(getBaseContext());

		setContentView(R.layout.activity_pointer);
		
		facade = new DataFacade(this, getAssets());

		Intent intent = getIntent();

		if (null != intent) {
			int id = intent.getIntExtra(ObjectDetatilActivity.KEY_POINTER, 0);
			azimuth = intent.getDoubleExtra(ObjectDetatilActivity.AZIMUTH, 0);
			altitude = intent.getDoubleExtra(ObjectDetatilActivity.ALTITUDE, 0);
			object = facade.getOneObject(id);
			TextView objectName = (TextView) findViewById(R.id.object_name);
			TextView constellation = (TextView) findViewById(R.id.object_constellation);
			TextView type = (TextView) findViewById(R.id.object_type);
			objectName.setText(object.getName());
			constellation.setText(object.getConstellation());
			SparseArray<String> typeMap = ResourceHelper.getObjectTypes(this);
			type.setText(typeMap.get(object.getType()));
			facade.close();
		}

		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

		compassView = (CompasView) findViewById(R.id.compas);
		horizontView = (HorizontView) findViewById(R.id.horizont);
	}

	public void updateOrientation(float axis_x, float axis_y, float axis_z) {
		compassView.setAngleX(axis_x);
		compassView.setAzimuth(azimuth);
		compassView.invalidate();
		horizontView.setAngle(axis_y, axis_z);
		horizontView.setAltitude((float) altitude);
		horizontView.invalidate();
	}

	@Override
	protected void onResume() {
		super.onResume();
		sensorManager.registerListener(this,
				sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_FASTEST);
		sensorManager.registerListener(this,
				sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
				SensorManager.SENSOR_DELAY_FASTEST);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.pointer, menu);
		return true;
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		switch (event.sensor.getType()) {
		case Sensor.TYPE_ACCELEROMETER: {
			accelVals = event.values.clone();
			break;
		}
		case Sensor.TYPE_MAGNETIC_FIELD: {
			magVals = event.values.clone();
			break;
		}
		}

		calculateOrientation();
	}
	
	private void checkMagneticFieldSupported(Context context) {
		SensorManager sm = (SensorManager) context
				.getSystemService(Context.SENSOR_SERVICE);
		List<Sensor> sensors = sm.getSensorList(Sensor.TYPE_MAGNETIC_FIELD);
		if (sensors.size() < 1) {
			Toast.makeText(
					getBaseContext(),
					"Váš pøístroj nepobsahuje senzor Magnetic Field, funkce k urèení orientace nebudou fungovat správnì.",
					Toast.LENGTH_LONG).show();
		}
	}

	private void calculateOrientation() {

		if (azimuth >= 0) {
			float[] rotationMatrix = new float[9];
			float[] values = new float[3];

			SensorManager.getRotationMatrix(rotationMatrix, null, accelVals,
					magVals);

			SensorManager.getOrientation(rotationMatrix, values);

			if (!(values[0] == 0 && values[1] == 0 && values[2] == 0)) {

				values[0] = (float) Math.toDegrees(values[0]);
				values[1] = (float) Math.toDegrees(values[1]);
				values[2] = (float) Math.toDegrees(values[2]);

				// prevraceni osy y
				values[1] *= -1;

				updateOrientation(values[0], values[1], values[2]);

			}
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
	}
	

}

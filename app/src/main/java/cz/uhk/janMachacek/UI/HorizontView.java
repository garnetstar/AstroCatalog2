package cz.uhk.janMachacek.UI;

import cz.uhk.janMachacek.R;
import cz.uhk.janMachacek.library.MovingAverage;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Vlastní View pro urèení výšky nad obzorem
 * 
 * @author Jan Macháèek
 *
 */
public class HorizontView extends View {

	private Paint textPaint, circlePaint, pointer, circlePaintBlack;

	private MovingAverage smooth;

	private float angle = 0;
	private float altitude = 0;

	public HorizontView(Context context) {
		super(context);
		initHorizontView();
	}

	public HorizontView(Context context, AttributeSet attr) {
		super(context, attr);
		initHorizontView();
	}

	public HorizontView(Context context, AttributeSet attr, int defaultStyle) {

		super(context, attr, defaultStyle);
		initHorizontView();

	}

	public void setAngle(float axis_y, float axis_z) {

		if (axis_z > 90) {
			axis_y = 90;
		}
		if (axis_y < 0)
			axis_y = 0f;

		smooth.pushValue(axis_y);

		this.angle = (float) smooth.getValue();
	}

	public void setAltitude(float altitude) {
		this.altitude = altitude;
	}

	public void initHorizontView() {
		setFocusable(true);

		Resources r = this.getResources();

		circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		circlePaint.setColor(r.getColor(R.color.alert));
		circlePaint.setStrokeWidth(4);
		circlePaint.setStyle(Paint.Style.FILL_AND_STROKE);
		
		circlePaintBlack = new Paint(Paint.ANTI_ALIAS_FLAG);
		circlePaintBlack.setColor(r.getColor(R.color.black));
		circlePaintBlack.setStrokeWidth(4);
		circlePaintBlack.setStyle(Paint.Style.FILL_AND_STROKE);

		textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		textPaint.setColor(r.getColor(R.color.alert));
		textPaint.setTextSize(18);

		pointer = new Paint(Paint.ANTI_ALIAS_FLAG);
		pointer.setColor(r.getColor(R.color.white));
		pointer.setStrokeWidth(3);
		pointer.setStyle(Paint.Style.FILL_AND_STROKE);
	
		smooth = new MovingAverage(50);
	}

	@Override
	protected void onDraw(Canvas canvas) {

		super.onDraw(canvas);

		float offsetTop = 10;
		float offsetLeft = 12;
		int height = getMeasuredHeight();
		float realHeight = height / 90 * (angle + 1);
		float point = height / 90;

		// stupnice výšky nad obzorem
		for (int i = 0; i <= 90; i += 15) {
			float iHeight = i * point;
			canvas.drawLine(5 + offsetLeft, iHeight + offsetTop,
					15 + offsetLeft, iHeight + offsetTop, circlePaint);
			canvas.drawText(Integer.toString(i), 20 + offsetLeft, iHeight + 6
					+ offsetTop, textPaint);
		}
		
		canvas.drawLine(10 + offsetLeft,  0 + offsetTop, 10 + offsetLeft, point
				* 90 + offsetTop, circlePaint);
		
		// požadovaná výška nad obzorem
		canvas.drawCircle(10 + offsetLeft, altitude * point + offsetTop, 16,
				circlePaint);
		
		canvas.drawCircle(10 + offsetLeft, altitude * point + offsetTop, 12,
				circlePaintBlack);
		
		// pohyblivý ukazatel výšky nad obzorem
		canvas.drawCircle(10 + offsetLeft, realHeight + offsetTop, 10,
				pointer);

		canvas.save();

	}
}

package cz.uhk.janMachacek.UI;

import cz.uhk.janMachacek.R;
import cz.uhk.janMachacek.library.MovingAverageAzimuth;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Vlastí View pro zobrazení kompasu 
 *  
 * @author Jan Macháèek
 *
 */
public class CompasView extends View {

	private MovingAverageAzimuth smootingAverage;
	private float axisX;

	double azimuth = 0;
	private Paint markerPaint, textPaint, circlePaint, templateLinePaint;
	private String northString, southString, westString, eastString;
	private int textHeight;

	public CompasView(Context context) {
		super(context);
		initCompasView();
	}

	public CompasView(Context context, AttributeSet attr) {
		super(context, attr);
		initCompasView();
	}

	public CompasView(Context context, AttributeSet attr, int defaultStyle) {

		super(context, attr, defaultStyle);
		initCompasView();
	}
	
	public void setAzimuth(double azimuth) {
		this.azimuth = azimuth;
	}

	public void initCompasView() {
		setFocusable(true);

		Resources r = this.getResources();

		circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		circlePaint.setColor(r.getColor(R.color.black));
		circlePaint.setStrokeWidth(1);
		circlePaint.setStyle(Paint.Style.FILL_AND_STROKE);

		northString = r.getString(R.string.cardinal_north);
		southString = r.getString(R.string.cardinal_south);
		eastString = r.getString(R.string.cardinal_east);
		westString = r.getString(R.string.cardinal_west);

		textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		textPaint.setColor(r.getColor(R.color.alert));
		textPaint.setTextSize(30);

		textHeight = (int) textPaint.measureText("yY");

		markerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		markerPaint.setColor(r.getColor(R.color.white));
		
		templateLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		templateLinePaint.setColor(r.getColor(R.color.alert));
		
		smootingAverage = new MovingAverageAzimuth(50);
	}

	@Override
	protected void onDraw(Canvas canvas) {

		super.onDraw(canvas);

		int px = getMeasuredWidth() / 2;
		int py = getMeasuredHeight() / 2;

		int radius = Math.min(px, py);

		canvas.drawCircle(px, py, radius, circlePaint);

		canvas.save();
		canvas.rotate(-axisX, px, py);
		canvas.rotate(0, px, py);

		int textWidth = (int) textPaint.measureText("W");
		int cardinalX = px - textWidth / 2;
		int cardinalY = py - radius + textHeight;
		for (int i = 0; i < 24; i++) {
			canvas.drawLine(px, py - radius, px, py - radius + 10, markerPaint);
			canvas.save();
			canvas.translate(0, textHeight);

			if (i % 6 == 0) {
				String dirString = "";
				switch (i) {
				case (0): {
					dirString = northString;
					int arrowY = 2 * textHeight;
					canvas.drawLine(px, arrowY, px - 5, 3 * textHeight,
							markerPaint);
					canvas.drawLine(px, arrowY, px + 5, 3 * textHeight,
							markerPaint);
					break;
				}
				case (6):
					dirString = eastString;
					break;
				case (12):
					dirString = southString;
					break;
				case (18):
					dirString = westString;
					break;
				}
				canvas.drawText(dirString, cardinalX, cardinalY, textPaint);
			}

			else if (i % 3 == 0) {
				String angle = String.valueOf(i * 15);
				float angleTextWidth = textPaint.measureText(angle);

				int angleTextX = (int) (px - angleTextWidth / 2);
				int angleTextY = py - radius + textHeight;
				canvas.drawText(angle, angleTextX, angleTextY, textPaint);
			}
			canvas.restore();
			canvas.rotate(15, px, py);
		}

		canvas.rotate((float)azimuth, px, py);
		markerPaint.setStrokeWidth(5);
		canvas.drawLine(px, 0, px, py, markerPaint);
		canvas.drawLine(px, 0, px-8, 30, markerPaint);
		canvas.drawLine(px, 0, px+8, 30, markerPaint);
		canvas.restore();
	}
	
	@Override
	public void draw(Canvas canvas) {

		super.draw(canvas);
		int px = getMeasuredWidth() / 2;
		int py = getMeasuredHeight() / 2;
		templateLinePaint.setStrokeWidth(3);
		int templatesWidth = 10;
		canvas.drawLine(px - templatesWidth, 0, px - templatesWidth, py, templateLinePaint);
		canvas.drawLine(px + templatesWidth, 0, px + templatesWidth, py, templateLinePaint);
		
		canvas.drawCircle(px, py, 12, markerPaint);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		int measureHeight = measure(heightMeasureSpec);
		int measureWidth = measure(widthMeasureSpec);

		int d = Math.min(measureHeight, measureWidth);

		setMeasuredDimension(d, d);
	}

	private int measure(int measureSpec) {
		int result = 0;
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);
		if (specMode == MeasureSpec.UNSPECIFIED) {
			result = 200;
		} else {
			result = specSize;
		}
		return result;
	}

	public void setAngleX(float angleX) {
		smootingAverage.pushValue(Math.toRadians(angleX));
		this.axisX = (float)Math.toDegrees(smootingAverage.getValue());
	}
}

package cz.uhk.janMachacek.library;

/**
 * Tøída pro vyhlazení dat pro azimut metodou SMA
 * 
 * @author Jan Macháèek
 *
 */
public class MovingAverageAzimuth {
	private double circularBuffer[][];
	private double avg[];
	private int circularIndex;
	private int count;

	public MovingAverageAzimuth(int k) {
		circularBuffer = new double[k][2];
		count = 0;
		circularIndex = 0;
		avg = new double[2];
	}

	public double getValue() {

		double angle = Math.atan2(avg[0], avg[1]);
		return angle;
	}

	public void pushValue(double angle) {

		double[] x = new double[2];
		x[0] = Math.sin(angle);
		x[1] = Math.cos(angle);

		if (count++ == 0) {
			primeBuffer(x);
		}
		double lastValue[] = circularBuffer[circularIndex];
		avg[0] = avg[0] + (x[0] - lastValue[0]) / circularBuffer.length;
		avg[1] = avg[1] + (x[1] - lastValue[1]) / circularBuffer.length;
		circularBuffer[circularIndex] = x;
		circularIndex = nextIndex(circularIndex);
	}

	private void primeBuffer(double[] val) {
		for (int i = 0; i < circularBuffer.length; ++i) {
			circularBuffer[i] = val;
		}
		avg = val;
	}

	private int nextIndex(int curIndex) {
		if (curIndex + 1 >= circularBuffer.length) {
			return 0;
		}
		return curIndex + 1;
	}
}

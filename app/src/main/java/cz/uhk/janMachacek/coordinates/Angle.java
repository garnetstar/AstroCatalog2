package cz.uhk.janMachacek.coordinates;

/**
 * Tøída reprezentující úhel
 * 
 * @author Jan Macháèek
 *
 */
public class Angle {
	private double angle;

	public Angle(double angle) {
		this.angle = angle;
	}

	public Angle(int degree, int minute, int second, boolean positive) {
		this.angle = toDecimal(degree, minute, second);
		if (!positive) {
			this.angle = this.angle * -1.;
		}
	}

	public Angle(int hour, double minute, boolean positive) {

		double cuttedMinute = Math.floor(minute);
		double second = (minute - cuttedMinute) * 60;
		this.angle = toDecimal(hour, (int) cuttedMinute, (int) second);
		if (!positive) {
			this.angle = this.angle * -1.;
		}
	}

	public double getDecimalDegree() {
		return angle;
	}

	public double getRadians() {
		return angle * Math.PI / 180;
	}

	public int[] getDegree() {
		return decimalToDegree(this.angle);
	}

	public int[] getHour() {
		return decimalToDegree(angle / 15);
	}

	private int[] decimalToDegree(double decimalAngle) {
		boolean negative = false;
		if (decimalAngle < 0) {
			negative = true;
			decimalAngle = decimalAngle * -1.;
		}
		int[] angle = new int[3];
		angle[0] = (int) Math.floor(decimalAngle);
		double second = (decimalAngle - (double) angle[0]) * 3600;
		angle[1] = (int) Math.floor(second / 60);
		angle[2] = (int) Math.round(second - angle[1] * 60);
		if (angle[2] == 60) {
			angle[2] = 0;
			angle[1]++;
		}
		if (negative) {
			angle[0] = angle[0] * -1;
		}
		return angle;
	}

	public double toDecimal(int degree, int minute, double second) {
		return (double) degree + (double) minute / 60 + second / 3600;
	}

}

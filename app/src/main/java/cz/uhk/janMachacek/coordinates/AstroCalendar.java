package cz.uhk.janMachacek.coordinates;

import java.util.Calendar;

/**
 * Tøída pro výpoèty týkající se rùzných kalendáøù
 * 
 * @author Jan Macháèek
 *
 */
public class AstroCalendar {

	public static double julianDay(int year, int month, int day, int hour,
			int minute) {

		double y = (double) year;
		double m = (double) month;
		double d = getDecimalDay(day, hour, minute);
		double a, b, jd;

		if (m <= 2) {
			m += 12;
			y -= 1;
		}

		a = Math.floor(y / 100);
		b = 2 - a + Math.floor(a / 4);

		jd = Math.floor(365.25 * (y + 4716)) + Math.floor(30.6001 * (m + 1))
				+ d + b - 1524.5;

		return jd;
	}

	public static Angle getLocalSiderealTime(Calendar time, Angle longitude) {

		double st = getSiderealTime(time);

		Angle stAngle = new Angle(st * 15);

		double lstDec = stAngle.getDecimalDegree()
				+ longitude.getDecimalDegree();

		return new Angle(lstDec);

	}

	public static double getSiderealTime(Calendar time) {
		// tøída Calendar oznaèuje mìsíce indexem zaèínajícím na 0,
		// proto je
		// potøeba vždy pøièíst 1
		return getSiderealTime(time.get(Calendar.YEAR),
				time.get(Calendar.MONTH) + 1, time.get(Calendar.DAY_OF_MONTH),
				time.get(Calendar.HOUR_OF_DAY), time.get(Calendar.MINUTE),
				time.get(Calendar.SECOND));
	}

	public static double getSiderealTime(int year, int month, int day,
			int hour, int minute, int second) {
		double sdUT0 = getSiderealUT0(year, month, day);
		double offset = 1.00273790935 * (hour * 60 * 60 + minute * 60 + second);
		double time = (sdUT0 * 3600 + offset) / 60 / 60;

		while (time > 24) {
			time -= 24;
		}
		
		return time;
	}

	public static double getSiderealUT0(int year, int month, int day) {

		double jd = julianDay(year, month, day,0,0);
		double t = (jd - 2451545) / 36525;
		double degreeTime = 100.46061837 + 36000.770053608 * t + 0.000387933
				* (t * t) - (t * t * t) / 38710000;
		// vysledek je ve stupnich, proto se musi vydelit 15 pro prevod na cas
		double time = degreeTime / 15;

		while (time < 0) {
			time = time + 24;
		}

		return time;
	}
	
	private static double getDecimalDay(int day, int hour, int minute) {
		double minutes = hour * 60 + minute;
		return (double) day + minutes / (24 * 60);
	}


}

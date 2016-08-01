package cz.uhk.janMachacek.coordinates;

import java.util.Calendar;

/**
 * Tøída pro výpoèet dynamických astronomických souøadnic
 * 
 * @author Jan Macháèek
 *
 */
public class Coordinates {
	
	public static Angle getHourAngle(Calendar time, Angle longitude, Angle rightAscension) {

		Angle lst = AstroCalendar.getLocalSiderealTime(time, longitude);
		
		double hourAngle = lst.getDecimalDegree() - rightAscension.getDecimalDegree();
		
		while (hourAngle < 0) {
			hourAngle += 360;
		}
		
		return new Angle(hourAngle);
	}
	
	public static Angle getAltitude(Angle hourAngle, Angle declination, Angle latitude) {
		double sina = Math.sin(declination.getRadians()) * Math.sin(latitude.getRadians()) + Math.cos(declination.getRadians())
				* Math.cos(latitude.getRadians()) * Math.cos(hourAngle.getRadians());
		double aRad = Math.asin(sina);

		return new Angle(Utils.radianToDegree(aRad));
	}
	
	public static Angle getAzimuth(Angle latitude, Angle declination, Angle altitude, Angle hourAngle) {
		double cosA = (Math.sin(declination.getRadians()) - Math.sin(latitude.getRadians()) * Math.sin(altitude.getRadians()))
				/ (Math.cos(latitude.getRadians()) * Math.cos(altitude.getRadians()));
		double A = Utils.radianToDegree(Math.acos(cosA));

		double sinH = Math.sin(hourAngle.getRadians());
		if (sinH < 0) {
			return new Angle(A);
		} else {
			return new Angle(360 - A);
		}
	}
}

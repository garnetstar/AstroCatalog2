package cz.uhk.machacekgoogle.coordinates;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Třída pro určováné aktuálního času
 * 
 * @author Jan Macháček
 *
 */
public class Timer {

	/**
	 * Zjištění aktuálního univerzálního času (UTC)
	 * @return Calendar
	 */
	public static Calendar getActualUTC() {
		// časová zóna odpovídající nultému poledníku
		TimeZone timeZone = TimeZone.getTimeZone("Etc/GMT+0");

		Calendar now = Calendar.getInstance();
		now.setTimeZone(timeZone);

		return now;
	}

	/**
	 * vrací aktuální datum ve formátu SQL timestamp
	 * @return String
     */
	public static String getTimestamp() {
		int hour, minute, year, month, day, sec;
		final Calendar c = Calendar.getInstance();
		hour = c.get(Calendar.HOUR_OF_DAY);
		minute = c.get(Calendar.MINUTE);
		year = c.get(Calendar.YEAR);
		month = c.get(Calendar.MONTH) + 1;
		day = c.get(Calendar.DAY_OF_MONTH);
		sec = c.get(Calendar.SECOND);

		String date = String.format("%d-%02d-%02d %02d:%02d:%02d", year, month, day, hour, minute, sec);
		return date;
	}

}

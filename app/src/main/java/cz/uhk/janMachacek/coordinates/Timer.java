package cz.uhk.janMachacek.coordinates;

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

}

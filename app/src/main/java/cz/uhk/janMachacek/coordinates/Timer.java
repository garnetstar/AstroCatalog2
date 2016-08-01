package cz.uhk.janMachacek.coordinates;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Tøída pro urèování aktuálního èasu
 * 
 * @author Jan Macháèek
 *
 */
public class Timer {

	/**
	 * Zjištìní aktuálního univerzálního èasu (UTC)
	 * @return Calendar
	 */
	public static Calendar getActualUTC() {
		// èasová zóna odpovídající nultému poledníku
		TimeZone timeZone = TimeZone.getTimeZone("Etc/GMT+0");

		Calendar now = Calendar.getInstance();
		now.setTimeZone(timeZone);

		return now;
	}

}

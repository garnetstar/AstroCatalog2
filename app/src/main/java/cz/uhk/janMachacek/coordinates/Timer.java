package cz.uhk.janMachacek.coordinates;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * T��da pro ur�ov�n� aktu�ln�ho �asu
 * 
 * @author Jan Mach��ek
 *
 */
public class Timer {

	/**
	 * Zji�t�n� aktu�ln�ho univerz�ln�ho �asu (UTC)
	 * @return Calendar
	 */
	public static Calendar getActualUTC() {
		// �asov� z�na odpov�daj�c� nult�mu poledn�ku
		TimeZone timeZone = TimeZone.getTimeZone("Etc/GMT+0");

		Calendar now = Calendar.getInstance();
		now.setTimeZone(timeZone);

		return now;
	}

}

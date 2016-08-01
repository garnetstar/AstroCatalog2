package cz.uhk.janMachacek.coordinates;

/**
 * Utility pro formátování dat z tøídy Angle
 * 
 * @author Jan Macháèek
 *
 */
public class Utils {

	public static String getFormatedHour(Angle angle) {
		int[] hour = angle.getHour();
		String template = "%dh %dm %ds";
		return String.format(template, hour[0], hour[1], hour[2]);
	}

	public static double radianToDegree(double radian) {
		return radian * 180 / Math.PI;
	}
	
	public static String getFormatedDegree(Angle angle) {
		int[] degree = angle.getDegree();
		String template = "%d° %d' %d''";
		return String.format(template, degree[0], degree[1], degree[2]);
	}
}

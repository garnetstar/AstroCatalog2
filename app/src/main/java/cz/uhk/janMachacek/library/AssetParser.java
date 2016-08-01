package cz.uhk.janMachacek.library;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.StringTokenizer;
import android.content.res.AssetManager;
import cz.uhk.janMachacek.coordinates.Angle;

/**
 * Tøída pro pøevod vstupních dat z plain textu do ArrayList<AstroObject>
 * 
 * @author Jan Macháèek
 *
 */
public class AssetParser {

	private static final int COL_NAME = 0;
	private static final int COL_NGC = 1;
	private static final int COL_CONSTELLATION = 2;
	private static final int COL_TYPE = 3;
	private static final int COL_RA_DEG = 4;
	private static final int COL_RA_MIN = 5;
	private static final int COL_DEC_DEG = 6;
	private static final int COL_DEC_MIN = 7;
	private static final int COL_MAGNITUDE = 8;
	private static final int COL_DISTANCE = 9;


	public AssetParser(AssetManager manager) {
		this.manager = manager;
	}
	
	private AssetManager manager;

	public ArrayList<AstroObject> parseMessierData() throws IOException {

		InputStream input = manager.open("messier.txt");

		byte[] data = new byte[5000];
		input.read(data);
		input.close();

		String rawData = new String(data);
		return parseMessierDataString(rawData);
	}

	private ArrayList<AstroObject> parseMessierDataString(String rawData) {
		ArrayList<AstroObject> list = new ArrayList<AstroObject>();

		AstroObject current = null;

		StringTokenizer st = new StringTokenizer(rawData, "? \n");

		int state = COL_NAME;
		int degree = 0;
		double minute = 0;

		while (st.hasMoreTokens()) {
			switch (state) {
			case COL_NAME:
				current = new AstroObject();
				current.name = "M" + Integer.parseInt(st.nextToken());
				state = COL_NGC;
				break;
			case COL_NGC:
				st.nextToken();
				state = COL_CONSTELLATION;
				break;
			case COL_CONSTELLATION:
				current.setConstellation(st.nextToken());
				state = COL_TYPE;
				break;
			case COL_TYPE:
				current.setType(Integer.parseInt(st.nextToken()));
				state = COL_RA_DEG;
				break;
			case COL_RA_DEG:
				degree = Integer.parseInt(st.nextToken());
				state = COL_RA_MIN;
				break;
			case COL_RA_MIN:
				minute = Double.parseDouble(st.nextToken());
				//pøevod z hodin na stupnì
				Angle angleHour = new Angle(degree, minute, true);
				current.setRightAscension(new Angle(angleHour.getDecimalDegree()*15));
				state = COL_DEC_DEG;
				break;
			case COL_DEC_DEG:
				String token = st.nextToken();
				degree = Integer.parseInt(token.replaceFirst("\\+", ""));
				state = COL_DEC_MIN;
				break;
			case COL_DEC_MIN:
				minute = Double.parseDouble(st.nextToken());
				boolean positive = true;
				if (degree < 0) {
					positive = false;
					degree = degree * -1;
				}
				current.setDeclination(new Angle(degree, minute, positive));
				state = COL_MAGNITUDE;
				break;
			case COL_MAGNITUDE:
				current.setMagnitude(Double.parseDouble(st.nextToken()));
				state = COL_DISTANCE;
				break;
			case COL_DISTANCE:
				Double distance;
				try {
					distance = Double.parseDouble(st.nextToken());
				} catch (Exception e) {
					distance = 0.;
				}
				current.setDistance(distance);
				state = COL_NAME;
				list.add(current);
				break;
			}
		}
		return list;
	}
}

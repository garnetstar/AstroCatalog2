package cz.uhk.janMachacek.library;

import cz.uhk.janMachacek.coordinates.Angle;

/**
 * Tøída reprezentující jeden astronomický objekt
 * 
 * @author Jan Macháèek
 *
 */
public class AstroObject {
	int id;
	String name;
	String constellation;
	int type;
	cz.uhk.janMachacek.coordinates.Angle rightAscension;
	Angle declination;
	double magnitude;
	double distance;
	String azimuth;
	String altitude;
	String hourAngle;
	
	

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getConstellation() {
		return constellation;
	}

	public void setConstellation(String constellation) {
		this.constellation = constellation;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public Angle getRightAscension() {
		return rightAscension;
	}

	public void setRightAscension(Angle rightAscension) {
		this.rightAscension = rightAscension;
	}

	public Angle getDeclination() {
		return declination;
	}

	public void setDeclination(Angle declination) {
		this.declination = declination;
	}

	public double getMagnitude() {
		return magnitude;
	}

	public void setMagnitude(double magnitude) {
		this.magnitude = magnitude;
	}

	public String getAzimuth() {
		return azimuth;
	}

	public void setAzimuth(String azimuth) {
		this.azimuth = azimuth;
	}

	public String getAltitude() {
		return altitude;
	}

	public void setAltitude(String altitude) {
		this.altitude = altitude;
	}

	public String getHourAngle() {
		return hourAngle;
	}

	public void setHourAngle(String hourAngle) {
		this.hourAngle = hourAngle;
	}
	
	
	
}

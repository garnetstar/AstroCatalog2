package cz.uhk.janMachacek.library;

import cz.uhk.janMachacek.coordinates.Angle;

/**
 * T��da reprezentuj�c� jeden astronomick� objekt
 *
 * @author Jan Mach��ek
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

    public AstroObject setName(String name) {
        this.name = name;
        return this;
    }

    public String getConstellation() {
        return constellation;
    }

    public AstroObject setConstellation(String constellation) {
        this.constellation = constellation;
        return this;
    }

    public int getType() {
        return type;
    }

    public AstroObject setType(int type) {
        this.type = type;
        return this;
    }

    public Angle getRightAscension() {
        return rightAscension;
    }

    public AstroObject setRightAscension(Angle rightAscension) {
        this.rightAscension = rightAscension;
        return this;
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

    public AstroObject setMagnitude(double magnitude) {
        this.magnitude = magnitude;
        return this;
    }

    public String getAzimuth() {
        return azimuth;
    }

    public AstroObject setAzimuth(String azimuth) {
        this.azimuth = azimuth;
        return this;
    }

    public String getAltitude() {
        return altitude;
    }

    public AstroObject setAltitude(String altitude) {
        this.altitude = altitude;
        return this;
    }

    public String getHourAngle() {
        return hourAngle;
    }

    public void setHourAngle(String hourAngle) {
        this.hourAngle = hourAngle;
    }


}

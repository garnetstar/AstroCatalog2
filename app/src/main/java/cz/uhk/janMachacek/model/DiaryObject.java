package cz.uhk.janMachacek.Model;

import android.content.ContentValues;

import java.text.ParseException;

import cz.uhk.janMachacek.coordinates.Angle;

/**
 * Třída reprezentující jeden záznam v tabulce Diary
 *
 * @author Jan Macháček
 *         Created on 23.10.2016.
 */
public class DiaryObject {

    private int id;
    private String guid;
    private String from;
    private String to;
    private Angle latitude;
    private Angle lognitude;
    private Integer syncOk;
    private DateTimeObject fromObject, toObject;

    public void setFrom(String from) {
        this.from = from;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public void setLatitude(Angle latitude) {
        this.latitude = latitude;
    }

    public void setLognitude(Angle lognitude) {
        this.lognitude = lognitude;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public Angle getLatitude() {
        return latitude;
    }

    public Angle getLognitude() {
        return lognitude;
    }

    public void setSyncOk(Integer syncOk) {
        this.syncOk = syncOk;
    }

    public Integer getSyncOk() {
        return this.syncOk;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public DateTimeObject getFromObject() throws ParseException {
        DateTimeObject object = new DateTimeObject(getFrom());
        return object;
    }

    public DateTimeObject getToObject() throws ParseException {
        DateTimeObject object = new DateTimeObject(getTo());
        return object;
    }

    public ContentValues getContentValues() {
        ContentValues cv = new ContentValues(8);
//        cv.put(AstroDbHelper.KEY_DIARY_ID, getId());
        cv.put(AstroDbHelper.KEY_DIARY_GUID, getGuid());
        cv.put(AstroDbHelper.KEY_DIARY_FROM, getFrom());
        cv.put(AstroDbHelper.KEY_DIARY_TO, getTo());
        cv.put(AstroDbHelper.KEY_DIARY_LAT, getLatitude() == null ? null : getLatitude().getDecimalDegree());
        cv.put(AstroDbHelper.KEY_DIARY_LON, getLognitude() == null ? null : getLognitude().getDecimalDegree());
        cv.put(AstroDbHelper.KEY_DIARY_SYNC_OK, getSyncOk());
        return cv;
    }

    public String toString() {
        return
                "id=" + getId() + " guid=" + getGuid() +
                ", from=" + getFrom() +
                ", to =" + getTo() +
                ", syncOK=" + Integer.toString(getSyncOk())
                ;
    }
}

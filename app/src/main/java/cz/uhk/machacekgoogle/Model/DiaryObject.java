package cz.uhk.machacekgoogle.Model;

import android.content.ContentValues;

import java.text.ParseException;

import cz.uhk.machacekgoogle.coordinates.Angle;

/**
 * Třída reprezentující jeden záznam v tabulce Diary
 *
 * @author Jan Macháček
 *         Created on 23.10.2016.
 */
public class DiaryObject {

    private Angle latitude, longitude;
    private int rowCounter, deleted, syncOk, id;
    private boolean isNew;
    private String guid, from, to, timestamp, weather, log;

    public void setFrom(String from) {
        this.from = from;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public void setLatitude(Angle latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(Angle longitude) {
        this.longitude = longitude;
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

    public Angle getLongitude() {
        return longitude;
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

    public int getDeleted() {
        return deleted;
    }

    public void setDeleted(int deleted) {
        this.deleted = deleted;
    }

    public DateTimeObject getFromObject() throws ParseException {
        DateTimeObject object = new DateTimeObject(getFrom());
        return object;
    }

    public DateTimeObject getToObject() throws ParseException {
        DateTimeObject object = new DateTimeObject(getTo());
        return object;
    }

    public int getRowCounter() {
        return rowCounter;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setIsNew(boolean isNew) {
        this.isNew = isNew;
    }

    public void setRowCounter(int rowCounter) {
        this.rowCounter = rowCounter;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    public String getWeather() {
        return weather;
    }

    public void setWeather(String weather) {
        this.weather = weather;
    }

    public ContentValues getContentValues() {
        ContentValues cv = new ContentValues(8);
//        cv.put(AstroDbHelper.KEY_DIARY_ID, getId());
        cv.put(AstroDbHelper.KEY_DIARY_GUID, getGuid());
        cv.put(AstroDbHelper.KEY_DIARY_FROM, getFrom());
        cv.put(AstroDbHelper.KEY_DIARY_TO, getTo());
        cv.put(AstroDbHelper.KEY_DIARY_LAT, getLatitude() == null ? null : getLatitude().getDecimalDegree());
        cv.put(AstroDbHelper.KEY_DIARY_LON, getLongitude() == null ? null : getLongitude().getDecimalDegree());
        cv.put(AstroDbHelper.KEY_DIARY_SYNC_OK, getSyncOk());
        cv.put(AstroDbHelper.KEY_DIARY_DELETED, getDeleted());
        cv.put(AstroDbHelper.KEY_DIARY_ROW_COUNTER, getRowCounter());
        cv.put(AstroDbHelper.KEY_DIARY_TIMESTAMP, getTimestamp());
        cv.put(AstroDbHelper.KEY_DIARY_WEATHER, getWeather());
        cv.put(AstroDbHelper.KEY_DIARY_LOG, getLog());

        return cv;
    }

    public String toString() {
        return
                "id=" + getId() + " guid=" + getGuid() +
                ", from=" + getFrom() +
                ", to =" + getTo() +
                ", syncOK=" + Integer.toString(getSyncOk()) +
                ", deleted=" + Integer.toString(getDeleted()) +
                        ", row_counter=" + Integer.toString(getRowCounter()) +
                        ", log=" + getLog() + ", weather=" + getWeather();
    }
}


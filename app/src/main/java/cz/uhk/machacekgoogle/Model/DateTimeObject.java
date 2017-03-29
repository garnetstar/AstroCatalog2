package cz.uhk.machacekgoogle.Model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Jan Macháček
 *         Created on 30.11.2016.
 */
public class DateTimeObject {

    private int year, month, day, hour, minute;

    private SimpleDateFormat format;

    private Date date;

    /**
     * datum ve formatu yyyy-MM-dd HH:mm:ss
     * @param dateTime
     */
    public DateTimeObject(String dateTime) throws ParseException {
        SimpleDateFormat original = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        date = original.parse(dateTime);
        format = new SimpleDateFormat();
    }

    public int getYear() {
        format.applyPattern("yyyy");
        return toInt();
    }

    public int getMonth() {
        format.applyPattern("MM");
        return toInt();

    }

    public int getDay() {
        format.applyPattern("dd");
        return toInt();
    }

    public int getHour() {
        format.applyPattern("HH");
        return toInt();
    }

    public int getMinute() {
        format.applyPattern("mm");
        return toInt();
    }

    public String getDateStringFormat() {
        format.applyPattern("dd.MM.yyyy");
        return format.format(date);
    }

    public String getTimeStringFormat() {
        format.applyPattern("HH:mm");
        return format.format(date);
    }

    public String getDateToString() {
        format.applyPattern("yyyy-MM-dd");
        return format.format(date);
    }

    public String toString() {
        format.applyPattern("yyyy-MM-dd HH:mm:ss");
        return format.format(date);
    }

    private int toInt(){
        return Integer.parseInt(format.format(date));
    }
}

package cz.uhk.janMachacek.Model;

import android.content.ContentValues;

/**
 * @author Jan Macháček
 *         Created on 27.11.2016.
 */
public class SettingsObject {

    private String key;
    private Integer value;

    public void setKey(String key) {
        this.key = key;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public String getKey() {

        return key;
    }

    public Integer getValue() {
        return value;
    }

    public ContentValues getContentValues() {
        ContentValues cv = new ContentValues(2);
        cv.put(AstroDbHelper.KEY_SETTINGS_KEY, getKey());
        cv.put(AstroDbHelper.KEY_SETTINGS_VALUE, getValue());
        return cv;
    }
}

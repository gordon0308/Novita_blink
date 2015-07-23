package com.aevi.simpleexample;

import android.content.Context;

import java.util.Set;

/**
 * Class for temporary storing Shared Preferences in the SharedPreferenceAdapter
 */
public class SharedPreferenceItem {

    // Must match the  order in R.string.shared_preference_types
    public enum SharedPreferencesType {
        STRING, BOOLEAN, LONG, INTEGER, FLOAT, STRING_SET;
    }


    private final String key;
    private Object value;

    public SharedPreferenceItem(String key, Object value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public Object getValue() {
        return value;
    }

    public String getValueToString() {
        return value.toString();
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public static String getTypeString(Context context, SharedPreferencesType type) {
        String[] typeStrings = context.getResources().getStringArray(R.array.shared_preference_types);
        return typeStrings[type.ordinal()];
    }

    public String getTypeString(Context context) {
        String[] typeStrings = context.getResources().getStringArray(R.array.shared_preference_types);
        return typeStrings[getType().ordinal()];
    }


    public static SharedPreferencesType getType(Object value) {

        if (value instanceof String) {
            return SharedPreferencesType.STRING;
        } else if (value instanceof Boolean) {
            return SharedPreferencesType.BOOLEAN;
        } else if (value instanceof Integer) {
            return SharedPreferencesType.INTEGER;
        } else if (value instanceof Long) {
            return SharedPreferencesType.LONG;
        } else if (value instanceof Float) {
            return SharedPreferencesType.FLOAT;
        } else if (value instanceof Set) {
            return SharedPreferencesType.STRING_SET;
        } else {
            return SharedPreferencesType.STRING;
        }
    }

    public SharedPreferencesType getType() {
        return getType(value);
    }

}

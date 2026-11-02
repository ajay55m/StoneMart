package com.app.stonemart.data.model;

import org.json.JSONObject;

/**
 * Holiday - Typed model representing an official company or public calendar holiday.
 */
public class Holiday {

    private final String date;
    private final String name;
    private final String type; // Public, Company

    public Holiday(String date, String name, String type) {
        this.date = date;
        this.name = name;
        this.type = type;
    }

    public static Holiday fromJson(JSONObject json) {
        if (json == null) return null;
        String date = json.optString("date", "");
        String name = json.optString("name", "Holiday");
        String type = json.optString("type", "Company");
        return new Holiday(date, name, type);
    }

    public String getDate() { return date; }
    public String getName() { return name; }
    public String getType() { return type; }
}


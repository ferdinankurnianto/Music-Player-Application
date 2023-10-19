package com.example.musicplayer;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Session {
    private final SharedPreferences prefs;

    public Session(Context cntx) {
        prefs = PreferenceManager.getDefaultSharedPreferences(cntx);
    }

    public void setLastSong(String lastSong) {
        prefs.edit().putString("lastSong", lastSong).apply();
    }

    public String getLastSong() {
        String lastSong = prefs.getString("lastSong", "");
        return lastSong;
    }

    public void setPref(Integer pref) {
        prefs.edit().putInt("pref", pref).apply();
    }

    public Integer getPref() {
        Integer pref = prefs.getInt("pref", 0);
        return pref;
    }

    public boolean checksession(String key) {
        boolean result = prefs.contains(key);
        return result;
    }

    public void setId(String id) {
        prefs.edit().putString("id", id).apply();
    }

    public String getId() {
        String id = prefs.getString("id", "-1");
        return id;
    }

    public void setType(String type) {
        prefs.edit().putString("type", type).apply();
    }

    public String getType() {
        String type = prefs.getString("type", "");
        return type;
    }

    public void removeLastSong() {
        prefs.edit().remove("lastSong").apply();
    }

    public void removePref() {
        prefs.edit().remove("pref").apply();
    }
}

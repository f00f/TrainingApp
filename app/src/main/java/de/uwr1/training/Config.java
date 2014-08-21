package de.uwr1.training;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by f00f on 03.07.2014.
 */
public class Config {
    public static final String KEY_BASE_URL = "BASE_URL";
    public static final String KEY_JSON_BASE_URL = "JSON_BASE_URL";
    public static final String KEY_JSON_URL = "JSON_URL";
    public static final String KEY_JSON_PLAYERS_URL = "JSON_PLAYERS_URL";
    public static final String KEY_REFRESH_URL = "REFRESH_URL";
    public static final String KEY_REPLY_URL_YES = "REPLY_URL_YES";
    public static final String KEY_REPLY_URL_NO = "REPLY_URL_NO";
    public static final String KEY_PHOTO_URL = "PHOTO_URL";
    public static final String KEY_PHOTO_THUMB_URL = "PHOTO_THUMB_URL";

    private static final Map<String, String> APP_CONFIG;
    static {
        Map<String, String> aMap = new HashMap<String, String>();
        aMap.put(KEY_BASE_URL, "http://%club_id%.uwr1.de/training/");
        aMap.put(KEY_JSON_BASE_URL, aMap.get(KEY_BASE_URL) + "json/");
        aMap.put(KEY_JSON_URL, aMap.get(KEY_JSON_BASE_URL) + "training.json");
        aMap.put(KEY_JSON_PLAYERS_URL, aMap.get(KEY_JSON_BASE_URL) + "all-players.json");
        aMap.put(KEY_REFRESH_URL, aMap.get(KEY_BASE_URL) + "training.php");
        aMap.put(KEY_REPLY_URL_YES, aMap.get(KEY_REFRESH_URL) + "?zusage=1&text=");
        aMap.put(KEY_REPLY_URL_NO, aMap.get(KEY_REFRESH_URL) + "?absage=1&text=");
        aMap.put(KEY_PHOTO_URL, aMap.get(KEY_BASE_URL) + "badbilder/${location}/");
        aMap.put(KEY_PHOTO_THUMB_URL, aMap.get(KEY_BASE_URL) + "badbilder/thumbs/${location}/");
        APP_CONFIG = Collections.unmodifiableMap(aMap);
    }

    private static String getAppConfigValue(String key) {
        if (APP_CONFIG.containsKey(key)) {
            return APP_CONFIG.get(key);
        }

        Log.d("UWR_Training::Config", "Parameter " + key + " not set in app config.");
        return null;
    }
    private static String getUserConfigValue(Context ctx, String key) {
        return getUserConfigValue(ctx, key, "");
    }
    private static String getUserConfigValue(Context ctx, String key, String defaultValue) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
        return sharedPref.getString(key, defaultValue);
    }

    public static String getClubId(Context ctx) {
        return Config.getUserConfigValue(ctx, SettingsActivity.KEY_PREF_CLUB);
    }
    public static String getReplyUrl(Context ctx, String text, boolean isZusage) {
        String url = null;
        String url_key = isZusage ? KEY_REPLY_URL_YES : KEY_REPLY_URL_NO;
        try {
            url = Config.getURL(ctx, url_key) + URLEncoder.encode(text, "UTF-8");
        } catch (UnsupportedEncodingException e) { /* empty */ }
        return url;
    }
    public static String getURL(Context ctx, String key) {
        String club_id = Config.getClubId(ctx);
        String url = Config.getAppConfigValue(key);
        url = url.replace("%club_id%", club_id);
        return url;
    }
    public static String getUsername(Context ctx) {
        return Config.getUserConfigValue(ctx, SettingsActivity.KEY_PREF_USERNAME).trim();
    }
    public static String getVersion(Fragment f) {
        return getVersion(f.getActivity());
    }
    public static String getVersion(Context ctx) {
        String myVersionName = "not available"; // initialize String

        try {
            myVersionName = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return myVersionName;
    }
}
